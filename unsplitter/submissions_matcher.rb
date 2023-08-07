# frozen_string_literal: true
require 'csv'
require 'pry'
require 'json'
CSV_FILE = '/Users/amedrano@codeforamerica.org/Documents/quarantine/mismatched_apps_2023_08_03.csv'

class SubmissionsMatcher
  def initialize(csv_file)
    @csv = CSV.open(csv_file, headers: :first_row)
    @new_data =[ ["submission_id", "submission_flow", "submission_input_data", "matched_submission_id", "matched_submission_flow", "matched_submission_input_data", "combined_input_data", "errors"] ]
  end

  def process
    @csv.each do |row|
      submission_id = row['matching_submission']

      new_input_data = JSON.parse(row['matching_submission_input_data'])
      unsubmitted_input_data = JSON.parse(row['unsubmitted_row_input_data'])
      errors = []
      unsubmitted_input_data.keys.each do |key|
        error = {}
        if(new_input_data[key])
          if(unsubmitted_input_data[key].to_s == new_input_data[key].to_s)
            next
          elsif key == 'firstName' || key == 'lastName'
            next if unsubmitted_input_data[key].to_s.strip.downcase == new_input_data[key].to_s.strip.downcase
            error['type'] = "key value mismatched"
            error['key'] = key
            error['notes'] = {
              submission_value: new_input_data[key].to_s,
              matched_submission_value: unsubmitted_input_data[key].to_s
            }
            errors << error
          else
            error['type'] = "key value mismatched"
            error['key'] = key
            error['notes'] = {
              submission_value: new_input_data[key].to_s,
              matched_submission_value: unsubmitted_input_data[key].to_s
            }
            errors << error
          end
          next
        else
          new_input_data[key] = unsubmitted_input_data[key]
        end
      end

      @new_data << [row['matching_submission'], row['matching_submission_flow'], row['matching_submission_input_data'], row['unsubmitted_row_id'], row['unsubmitted_row_flow'], row['unsubmitted_row_input_data'], new_input_data, errors]
    end

    File.write("/Users/amedrano@codeforamerica.org/Documents/quarantine/new_output.csv", @new_data.map(&:to_csv).join)
  end
end

SubmissionsMatcher.new(CSV_FILE).process
