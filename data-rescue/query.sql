-- the output of this goes into submissions_matcher.rb (as "toms_merge.csv")
with incomplete_submissions as (
  select *
  from submissions s
  where (
    s.flow = 'pebt' AND (
      s.input_data ->> 'hasMoreThanOneStudent' is null
      OR s.input_data ->> 'firstName' is null
      OR s.input_data ->> 'signature' is null
    )
    OR
    s.flow = 'docUpload' AND (
      s.input_data ->> 'firstName' is null
      OR s.input_data ->> 'lastName' is null
      OR s.input_data ->> 'applicationNumber' is null
    )
  )
), submissions_with_name as (
  select
    id,
    regexp_replace(
      regexp_replace(
        translate(
          coalesce(
            trim(lower(s.input_data ->> 'signature')),
            lower(concat(trim(s.input_data ->> 'firstName'), ' ', trim(s.input_data ->> 'lastName'))))
          , 'áéíóú-', 'aeiou ')
      , '[\.]', '')
    , '([a-z]*) [a-z]* ([a-z]*)', '\1 \2')
    as normalized_name
  from incomplete_submissions s
)
select
  s.id,
  normalized_name,
  submitted_at,
  updated_at,
  flow,
  input_data
from submissions s
inner join submissions_with_name on s.id = submissions_with_name.id
where submissions_with_name.normalized_name <> ' '
order by normalized_name;

-- the output of this goes into s3_review.rb as docuplad_submissions_that_had_errored.csv
select * from submissions s
                inner join transmissions t on s.id = t.submission_id
where t.last_transmission_failure_reason = 'skip_incomplete'
  and s.input_data->>'docUpload' is not null
  and s.flow = 'docUpload';

-- the output of this goes into s3_review.rb as csv_of_all_completed_submissions_with_confirmationNumber.csv
select s.input_data->>'firstName' as first_name, s.input_data->>'lastName' as last_name, s.input_data->>'confirmationNumber' as confirmation_number
from submissions s
where s.submitted_at is not null
  and s.input_data->>'confirmationNumber' is not null;
