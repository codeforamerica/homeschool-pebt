SELECT sss.id AS matching_submission,
       sss.signature,
       sss.flow,
       sss.created_at,
       sss.input_data AS matching_submission_input_data,
       s.id AS unsubmitted_row_id,
       s.flow,
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
from split_session_submission s
where affected_by_split_session = true) as sss
on lower(concat(trim(s.input_data ->> 'firstName'), ' ', trim(s.input_data ->> 'lastName'))) =
  trim(lower(sss.signature))


WHERE s.submitted_at is null
  AND s.input_data::text not ilike '{}'
  AND s.input_data ->> 'firstName' is not null
  AND s.input_data ->> 'lastName' is not null
