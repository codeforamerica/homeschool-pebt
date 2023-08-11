require 'aws-sdk-s3'
require 'pry'
require 'csv'
raise "need to specify AWS_ACCESS_KEY_ID" unless ENV['AWS_ACCESS_KEY_ID'] && ENV['AWS_SECRET_ACCESS_KEY']

#inputs
# 1. csv of docUpload-only submissions
# 2. csv of all completed submissions - (firstName, lastName, confirmationNumber)
DOCUPLOADS_ONLY = File.expand_path('~/Documents/quarantine/docuplad_submissions_that_had_errored.csv')
COMPLETED_SUBMISSIONS = File.expand_path('~/Documents/quarantine/csv_of_all_completed_submissions_with_confirmationNumber.csv')
OUTPUT_FILE = File.expand_path('~/Documents/quarantine/manual_laterdoc_investigation_2023-08-09.sql')

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

def match_docupload_to_submission_data
  rows = CSV.read(DOCUPLOADS_ONLY, headers: :first_row)
  to_skip = already_processed_submissions
  rows.each do |row|
    next if to_skip.include?(row['submission_id'])
    files = download_files(row['submission_id'])

    puts "Files: #{files.map(&:path).join("\n")}"
    application_number = nil

    while application_number.nil?
      puts "Enter First name (q to skip): "
      first_name = $stdin.gets.strip
      break if first_name == 'q'

      puts "Enter Last name: "
      last_name = $stdin.gets.strip

      application_number = find_matching_submission(first_name, last_name)
    end

    append_query(row['submission_id'], row['input_data'], first_name, last_name, application_number)
  end
end

def find_matching_submission(first_name, last_name)
  CSV.read(COMPLETED_SUBMISSIONS, headers: :first_row).each do |row|
    return row['confirmation_number'] if normalize_name(row['first_name']) == normalize_name(first_name) && normalize_name(row['last_name']) == normalize_name(last_name)
  end

  nil
end

def normalize_name(name)
  return nil if name.nil?
  name.strip.downcase
end

def append_query(submission_id, input_data, first_name, last_name, application_number)
  File.open(OUTPUT_FILE, 'a') do |f|
    if application_number.nil?
      f.puts "-- no match: #{submission_id}"
    else
      new_input_data = JSON.generate(JSON.parse(input_data).merge('firstName' => first_name, 'lastName' => last_name, 'applicationNumber' => application_number)).gsub("'", "''")
      f.puts "-- found match: #{submission_id}"
      f.puts "UPDATE submissions SET input_data = '#{new_input_data}' WHERE id='#{submission_id}';"
    end
  end
end

def already_processed_submissions
  return [] unless File.exist?(OUTPUT_FILE)
  File.readlines(OUTPUT_FILE).map do |line|
    line.match('-- (found|no) match: (.*)') && $~[2]
  end.compact
end

match_docupload_to_submission_data

# for each docUpload-only submissions:
# download the docs
# user reviews the docs
# the script asks for the firstName and lastName to attach to that laterdoc
# (tbd) search for applications with that name to find a confirmation number
# script outputs SQL to run to add those fields
