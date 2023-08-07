SELECT sss.id AS matching_submission,
       sss.signature,
       sss.flow AS matching_submission_flow,
       sss.created_at,
       sss.input_data AS matching_submission_input_data,
       s.id AS unsubmitted_row_id,
       s.flow AS unsubmitted_row_flow,
       s.input_data as unsubmitted_row_input_data,
       s.created_at
FROM submissions s
       inner join (WITH split_session_submissions AS (SELECT s.id,
                                                             s.flow,
                                                             s.created_at,
                                                             s.input_data ->> 'firstName'             AS first_name,
                     s.input_data ->> 'hasMoreThanOneStudent' as has_more_than_one_student,
                     s.input_data ->> 'signature'             AS signature,
                     s.input_data,
                     (
                     s.input_data ->> 'hasMoreThanOneStudent' is null
                     OR s.input_data ->> 'firstName' is null
                     OR s.input_data ->> 'signature' is null
                     )                                    AS affected_by_split_session
                   FROM submissions s
                   WHERE s.submitted_at is not null
                     AND s.input_data::text not ilike '{}')
SELECT *
from split_session_submissions s
where affected_by_split_session = true) as sss
on lower(concat(trim(s.input_data ->> 'firstName'), ' ', trim(s.input_data ->> 'lastName'))) =
  trim(lower(sss.signature))


WHERE s.submitted_at is null
  AND s.input_data::text not ilike '{}'
  AND s.input_data ->> 'firstName' is not null
  AND s.input_data ->> 'lastName' is not null;


-- toms merge
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
    coalesce(trim(lower(s.input_data->>'signature')), lower(concat(trim(s.input_data ->> 'firstName'), ' ', trim(s.input_data ->> 'lastName')))) as normalized_name
  from incomplete_submissions s
)
select
  s.id,
  normalized_name,
  submitted_at,
  flow,
  input_data
from submissions s
inner join submissions_with_name on s.id = submissions_with_name.id
where submissions_with_name.normalized_name <> ' '
order by normalized_name;


select *
from submissions
where flow = 'docUpload'
   AND input_data ->> 'hasMoreThanOneStudent' is not null
   AND input_data ->> 'firstName' is not null
   AND input_data ->> 'signature' is not null
   AND input_data ->> 'docUpload' is null
