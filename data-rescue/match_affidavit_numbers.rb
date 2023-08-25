# This script fuzzy-matches the affidavit numbers given to us in the app against
# the list of official ones. This is a bit too slow to do in SQL, so it's imported
# into its own table.

LIST_FROM_CDSS = File.expand_path('~/Downloads/CfA Copy for CSV Export - Affidavit Info for P-EBT 4.0 User Testing.xlsx - 2022-23 Private School Data.csv')
LIST_FROM_PROD = File.expand_path('~/Documents/quarantine/export_affidavit_numbers.csv')
# import this into the db via
#   delete from homeschool_affidavit_number_matches;
#   \copy homeschool_affidavit_number_matches from '~/Documents/quarantine/for_import_affidavit_number_matches_2023-08-25.csv' delimiter ',' csv header;
OUTPUT_FILE = File.expand_path('~/Documents/quarantine/for_import_affidavit_number_matches_2023-08-25.csv')

require 'csv'
require 'damerau-levenshtein'
require 'ruby-progressbar'

class Merger
  def initialize(official_list, prod_list)
    @official_list = official_list.map { |row| row['num'] }
    @prod_list = prod_list.to_a
    @progressbar = ProgressBar.create(total: @prod_list.length)
  end

  def process
    @prod_list.each do |row|
      @progressbar.increment

      # look for edit distance <= 6 since omitting the "-ABCD" suffix will be 5, so
      # let's tolerate that as well as one additional character typo.
      matches = @official_list.find_all { |official| DamerauLevenshtein.distance(row['num'], official) <= 6 }
      best_match, distance = matches.map { |match| [match, DamerauLevenshtein.distance(row['num'], match)] }.min_by(&:last)

      yield [row['num'], best_match, distance] if best_match
    end
  end
end

CSV.open(OUTPUT_FILE, 'w', headers: ['original', 'match', 'distance'], write_headers: true) do |csv|
  Merger.new(
    CSV.open(LIST_FROM_CDSS, headers: :first_row),
    CSV.open(LIST_FROM_PROD, headers: :first_row),
  ).process do |original, match, distance|
    csv << [original, match, distance]
  end
end
