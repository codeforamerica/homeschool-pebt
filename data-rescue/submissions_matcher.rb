# frozen_string_literal: true
require 'csv'
require 'pry'
require 'json'
CSV_FILE = File.expand_path('~/Documents/quarantine/toms_merge.csv')

class SubmissionsMatcher
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

  def initialize(csv_file)
    @csv = CSV.open(csv_file, headers: :first_row)
  end

  def process
    data = @csv.to_a
    data.each { |row| row['input_data'] = JSON.parse(row['input_data']) }
    data = data.group_by { |row| row['normalized_name'] }.keep_if { |_name, rows| rows.length > 1 }
    puts "Read #{data.length} groups of people from CSV"

    submitted_rows = Hash[data.filter { |_name, rows| rows.any? { |row| row['submitted_at'] } }]
    puts "Found #{submitted_rows.length} groups with a submitted_at"

    sql_commands = ['BEGIN;']
    submitted_rows.each do |k, rows|
      begin
        merged_record, other_ids = merge_records(rows)
        sql_commands << generate_sql_command(merged_record, other_ids)
      rescue => ex
        binding.pry if ex.message.match?('implicit')
        $stderr.puts "Error merging #{k}: #{ex.message}"
      end
    end
    sql_commands << 'COMMIT;'

    File.open('out.sql', 'w') do |f|
      f.write(sql_commands.join("\n"))
    end
    puts "Wrote #{sql_commands.length} SQL commands to 'out.sql'"
  end

  def generate_sql_command(record, other_ids)
    <<~SQL
    UPDATE submissions SET input_data = '#{JSON.generate(record['input_data']).gsub("'", "''")}' WHERE id = '#{record['id']}';
    UPDATE submissions SET merged_into_submission_id='#{record['id']}' WHERE id in ('#{other_ids.join('\',\'')}');
    UPDATE submissions SET updated_at = NOW() WHERE id = '#{record['id']}' OR id in ('#{other_ids.join('\',\'')}');
    SQL
  end

  def merge_records(rows)
    # find the submitted record
    submitted_record = rows.find { |row| row['submitted_at'] }

    # merge
    unsubmitted_records = rows.find_all { |row| row['id'] != submitted_record['id'] }.sort_by { |row| DateTime.parse(row['updated_at']) }.reverse
    unsubmitted_records.each do |row|
      row['input_data'].each do |k, v|
        old_val = submitted_record['input_data'][k]
        if old_val.nil?
          submitted_record['input_data'][k] = v
        else
          new_val = FIELD_MERGE_STRATEGIES.fetch(k, MERGE_STRATEGIES[:equality]).call(old_val, v)
          if new_val.nil?
            raise "key #{k} does not match (#{old_val.inspect} vs #{v.inspect})"
          end
          submitted_record['input_data'][k] = new_val
        end
      end
    end

    [submitted_record, unsubmitted_records.map { |record| record['id'] }]
  end
end

SubmissionsMatcher.new(CSV_FILE).process
