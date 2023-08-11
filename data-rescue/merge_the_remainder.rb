# frozen_string_literal: true
require 'csv'
require 'pry'
require 'json'
require 'damerau-levenshtein'
require 'aws-sdk-s3'
raise "need to specify AWS_ACCESS_KEY_ID" unless ENV['AWS_ACCESS_KEY_ID'] && ENV['AWS_SECRET_ACCESS_KEY']
CSV_FILE = File.expand_path('~/Documents/quarantine/all_data.csv')
OUTPUT_FILE = File.expand_path('~/Documents/quarantine/manual_resolution_of_everything-2023-08-11.sql')

# Logic copied from submissions_matcher.rb
class SubmissionMerger
  MERGE_STRATEGIES = {
    # a = old value, b = new value
    # Whatever is returned goes in the new field. If it returns `nil` then the record is different and we raise an error.
    equality: ->(a, b) { b if a == b },
    string: ->(a, b) { b if a.gsub(" ", "").downcase == b.gsub(" ", "").downcase },
    uploads: ->(a, b) { JSON.generate(JSON.parse(a).concat(JSON.parse(b))) },
    dont_overwrite: ->(a, _) { a },
  }
  # For some fields, we can afford to be a bit looser with equality checking / merge logic.
  FIELD_MERGE_STRATEGIES = {
    'firstName' => MERGE_STRATEGIES[:string],
    'lastName' => MERGE_STRATEGIES[:string],
    'signature' => MERGE_STRATEGIES[:string],
    'residentialAddressCity' => MERGE_STRATEGIES[:string],
    'residentialAddressStreetAddress1' => MERGE_STRATEGIES[:string],
    'residentialAddressStreetAddress2' => MERGE_STRATEGIES[:string],

    'identityFiles' => MERGE_STRATEGIES[:uploads],
    'enrollmentFiles' => MERGE_STRATEGIES[:uploads],
    'incomeFiles' => MERGE_STRATEGIES[:uploads],
    'unearnedIncomeFiles' => MERGE_STRATEGIES[:uploads],

    'students' => MERGE_STRATEGIES[:dont_overwrite],
    'income' => MERGE_STRATEGIES[:dont_overwrite],
    'household' => MERGE_STRATEGIES[:dont_overwrite],
    'confirmationNumber' => MERGE_STRATEGIES[:dont_overwrite],
    'feedbackText' => MERGE_STRATEGIES[:dont_overwrite],
  }

  def self.merge_records(submitted_record, unsubmitted_records)
    unsubmitted_records.each do |row|
      row['input_data'].each do |k, v|
        old_val = submitted_record['input_data'][k]
        if old_val.nil?
          submitted_record['input_data'][k] = v
        else
          new_val = FIELD_MERGE_STRATEGIES.fetch(k, MERGE_STRATEGIES[:equality]).call(old_val, v)
          if new_val.nil?
            puts "Merge conflict in field #{k}. Old: #{old_val} / New: #{v}"
            puts "What value should be used?"
            new_val = $stdin.gets.strip
          end
          submitted_record['input_data'][k] = new_val
        end
      end
    end

    [submitted_record, unsubmitted_records.map { |record| record['submission_id'] }]
  end
end

def download_files(submission_id)
  s3 = Aws::S3::Client.new(region: 'us-west-1')
  s3.list_objects(bucket: 'homeschool-pebt-production', prefix: submission_id).each do |response|
    return response.contents.map do |s3_file|
      extension = File.extname(s3_file['key'])
      f = Tempfile.new(['download', extension])
      s3.get_object(
        bucket: 'homeschool-pebt-production',
        key: s3_file['key']
      ) do |chunk|
        f.write chunk
      end

      f
    end
  end
end

def normalized_name(app)
  if app['input_data']['signature']
    app['input_data']['signature']
      .downcase
      .gsub(/^[ ]*|[ ]*$|(?<= )[ ]*/, '')
      .gsub(/([a-z]+) [a-z]+ ([a-z]+)/, '\1 \2')
      .gsub(" ", "").tr("áéíóú", "aeiou")
  elsif app['input_data']['firstName']
    (app['input_data']['firstName'].strip + ' ' + app['input_data']['lastName'].strip)
     .downcase
     .gsub(/([a-z]+) [a-z]+ ([a-z]+)/, '\1 \2')
     .gsub(" ", "").tr("áéíóú", "aeiou")
  end
end

class OutputFile
  def initialize(filename)
    @filename = filename
  end

  def already_processed_rows
    return [] unless File.exist?(@filename)

    File.readlines(@filename).map do |line|
      line.match('-- id: (.*)') && $~[1]
    end.compact
  end

  def write_sql(id, sql)
    File.open(@filename, 'a') do |f|
      f.puts "-- id: #{id}"
      f.puts sql
    end
  end
end

class SubmissionsMatcher
  def initialize(csv_file, output_file)
    @data = CSV.open(csv_file, headers: :first_row).to_a
    @data.each { |row| row['input_data'] = JSON.parse(row['input_data']) }
    @output_file = output_file
  end

  def process
    incomplete_apps =
      @data
        .find_all { |r| r['last_transmission_failure_reason'] == 'skip_incomplete' }
        .find_all { |r| !@output_file.already_processed_rows.include?(r['submission_id']) }
    puts "Found #{incomplete_apps.length} incomplete apps to process..."

    incomplete_apps.each do |app|
      next if app['input_data'].keys == ['docUpload'] # handled separately by s3_review script

      similar = similar_apps(app)
      loop do
        print_apps_table(similar, app)

        puts "Options: [M]erge / [D]elete this app (if a duplicate succeeded) / [S]earch / Merge with [O]ther / [F]ile download"
        case $stdin.gets.chomp.downcase
        when 'm'
          $stdout.write "Merge with which application? "
          merge_idx = $stdin.gets.chomp.to_i
          merge_app = similar[merge_idx - 1]
          result, merged_submission_ids = SubmissionMerger.merge_records(app, [merge_app])
          @output_file.write_sql(app['submission_id'], <<~SQL)
            UPDATE submissions SET input_data = '#{JSON.generate(result['input_data']).gsub("'", "''")}' WHERE id = '#{result['submission_id']}';
            UPDATE submissions SET merged_into_submission_id='#{result['submission_id']}' WHERE id in ('#{merged_submission_ids.join('\',\'')}');
            UPDATE submissions SET updated_at = NOW() WHERE id = '#{result['submission_id']}' OR id in ('#{merged_submission_ids.join('\',\'')}');
          SQL
          break
        when 'd'
          @output_file.write_sql(app['submission_id'], <<~SQL)
            DELETE FROM transmissions WHERE submission_id = '#{app['submission_id']}';
            DELETE FROM user_files WHERE submission_id = '#{app['submission_id']}';
            DELETE FROM submissions WHERE id = '#{app['submission_id']}';
          SQL
          break
        when 's'
          puts "\nMost closely matching 20 apps:"
          similar = @data
                      .map { |o| [o, DamerauLevenshtein.distance(app['normalized_name'], o['normalized_name'])] unless o['normalized_name'].nil? }
                      .compact
                      .sort_by { |_app, rank| rank }
                      .first(20)
                      .map(&:first)
        when 'o'
          puts "Merge with which submission id? "
          merge_id = $stdin.gets.chomp
          merge_app = @data.find { |app| app['submission_id'] == merge_id }
          if merge_app.nil?
            puts "Could not find submission ID = #{merge_id}"
            next
          end
          result, merged_submission_ids = SubmissionMerger.merge_records(app, [merge_app])
          @output_file.write_sql(app['submission_id'], <<~SQL)
            UPDATE submissions SET input_data = '#{JSON.generate(result['input_data']).gsub("'", "''")}' WHERE id = '#{result['submission_id']}';
            UPDATE submissions SET merged_into_submission_id='#{result['submission_id']}' WHERE id in ('#{merged_submission_ids.join('\',\'')}');
            UPDATE submissions SET updated_at = NOW() WHERE id = '#{result['submission_id']}' OR id in ('#{merged_submission_ids.join('\',\'')}');
          SQL
          break
        when 'f'
          files = download_files(app['submission_id'])
          puts "Attached files:"
          puts files.map(&:path)
        end
      end
    end
  end

  def similar_apps(app)
    name = normalized_name(app)

    @data.find_all do |other|
      other['normalized_name'] ||= normalized_name(other)
      other['normalized_name'] == name && other['submission_id'] != app['submission_id']
    end
  end

  FIELDS = ->(app) do
    {
      'applicant_name' => [app['input_data'].fetch('firstName', nil), app['input_data'].fetch('lastName', nil)].compact.join(' '),
      'signature' => app['input_data']['signature'],
      'students' => app['input_data'].fetch('students', []).map { |s| [s['studentFirstName'], s['studentLastName']].compact.join(' ') }.join(', '),
      'submitted_at' => app['submitted_at'],
      'submitted_to_state_at' => app['submitted_to_state_at'],
      'num_fields' => app['input_data'].length.to_s,
      'id_prefix' => app['submission_id'][0..8]
    }
  end
  def print_apps_table(apps, target)
    fields = apps.map { |app| FIELDS.call(app) }
    target_fields = FIELDS.call(target)
    max_len_by_field = (fields.dup.append(target_fields)).flat_map(&:entries).each_with_object({}) { |(field_name, value), max_len| max_len[field_name] ||= field_name.length; max_len[field_name] = value.length if value && value.length > max_len[field_name] }

    # output header
    row_length = 0
    row_length += $stdout.write("#  ")
    max_len_by_field.keys.each do |field_name|
      row_length += $stdout.write(" | " + field_name.ljust(max_len_by_field[field_name]))
    end
    $stdout.write("\n")
    $stdout.write('-' * row_length + "\n")
    # output rows
    fields.each_with_index.each do |app, i|
      $stdout.write("%2d." % (i + 1))
      app.each do |field_name, value|
        $stdout.write(" | " + (value || "").ljust(max_len_by_field[field_name]))
      end
      $stdout.write("\n")
    end
    $stdout.write("\n")
    $stdout.write('--- INCOMPLETE APP: ' + '-' * (row_length - 21))
    $stdout.write("\n   ")
    FIELDS.call(target).each do |field_name, value|
      $stdout.write(" | " + (value || "").ljust(max_len_by_field[field_name]))
    end
    $stdout.write("\n")
  end
end

puts "Loading data..."
output_file = OutputFile.new(OUTPUT_FILE)
SubmissionsMatcher.new(CSV_FILE, output_file).process
