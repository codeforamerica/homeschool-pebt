# frozen_string_literal: true
require 'csv'
require 'pry'
require 'json'

=begin
select s.*,t.flow as "t_flow",t.id as "t_id" from submissions s
left outer join transmissions t on s.id = t.submission_id
where s.flow = 'docUpload'
  and input_data->>'signature' is not null
  and submitted_at is not null;
=end
CSV_FILE = File.expand_path('~/Documents/quarantine/flow_switch_pebt_to_docupload.csv')
OUTPUT_FILE = File.expand_path('./output-flow-switch.sql')

class FlowSwitchResolver
  def initialize(data)
    @data = data
  end

  def resolution_sql
    return to_enum(:resolution_sql) unless block_given?

    @data.each do |row|
      yield <<~SQL
        UPDATE submissions SET flow = 'pebt', updated_at = NOW() WHERE id = '#{row["id"]}';
        UPDATE transmissions SET submitted_to_state_at = null, submitted_to_state_filename = null, updated_at = NOW() WHERE id = '#{row["t_id"]}';
      SQL
    end
  end
end

class Outputter
  def initialize(filename)
    @filename = filename
  end

  def write_lines(enumerable)
    count = 0
    File.open(@filename, 'w') do |f|
      f.puts "BEGIN;"
      enumerable.each do |entry|
        f.puts entry
        count += 1
      end
      f.puts "COMMIT;"
    end

    puts "Outputted #{count} entries to #{@filename}"
  end
end

resolver = FlowSwitchResolver.new(CSV.read(CSV_FILE, headers: :first_row)).resolution_sql
Outputter.new(OUTPUT_FILE).write_lines(resolver)
