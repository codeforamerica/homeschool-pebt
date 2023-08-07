# frozen_string_literal: true
require 'csv'
require 'pry'
require 'json'
CSV_FILE = '/Users/achoi@codeforamerica.org/Documents/quarantine/toms_merge.csv'

class SubmissionsMatcher
  def initialize(csv_file)
    @csv = CSV.open(csv_file, headers: :first_row)
  end

  def process
    data = @csv.to_a
    data.each { |row| row['input_data'] = JSON.parse(row['input_data']) }
    data = data.group_by { |row| row['normalized_name'] }
    puts "Read #{data.length} groups of people from CSV"

    submitted_rows = Hash[data.filter { |k, rows| rows.any? { |row| row['submitted_at'] } }]
    puts "Found #{submitted_rows.length} groups with a submitted_at"

    sql_commands = []
    submitted_rows.each do |k, rows|
      begin
        merged_record, other_ids = merge_records(rows)
        sql_commands << generate_sql_command(merged_record, other_ids)
        binding.pry
      rescue => ex
        $stderr.puts "Error merging #{k}: #{ex.message}"
      end
    end
  end

  def generate_sql_command(record, other_ids)
    <<~SQL
    UPDATE submissions SET input_data = '#{JSON.generate(record['input_data'])}' WHERE id = '#{record['id']}';
    UPDATE submissions SET merged_into_submission_id='#{record['id']}' WHERE id in ('#{other_ids.join('\',\'')}');
    SQL
  end

  def merge_records(rows)
    # find the submitted record
    submitted_record = rows.find { |row| row['submitted_at'] }
    unsubmitted_records = rows.find_all { |row| row['id'] != submitted_record['id'] }
    unsubmitted_records.each do |row|
      row['input_data'].each do |k, v|
        val = submitted_record['input_data'][k]
        if !val.nil? && val != v
          raise "key #{k} does not match: #{val} and #{v}"
        else
          submitted_record['input_data'][k] = v
        end
      end
    end
    [submitted_record, unsubmitted_records.map { |record| record['id'] }]
  end
end

SubmissionsMatcher.new(CSV_FILE).process
