-- export affidavit numbers
select distinct(affidavit#>>'{}') as num
from submissions,
     jsonb_path_query(submissions.input_data, '$.students[*].studentHomeschoolAffidavitNumber') affidavit
where submitted_at is not null
  and affidavit is not null;

-- mega query
with total_submitted as (
  -- "step 0" - how many total apps were submitted?
  select id from submissions where submitted_at is not null
),

total_with_eligible_student as (
  -- "step 1" - how many apps have at least one eligible student (born after 7/1/01 and before 5/11/17)
  select
    submissions.id,
    input_data
  from submissions
  inner join total_submitted on total_submitted.id = submissions.id
  where input_data @? '$.students[*] ? ('
    '(@.studentBirthdayYear > "2001" || (@.studentBirthdayYear == "2001" && @.studentBirthdayMonth >= "07"))'
    ' && '
    '(@.studentBirthdayYear < "2017" || (@.studentBirthdayYear == "2017" && @.studentBirthdayMonth < "05") || (@.studentBirthdayYear == "2017" && @.studentBirthdayMonth == "05" && @.studentBirthdayDay <= "11"))'
    ')'
),
total_with_affidavit_number as (
 select distinct(id) as id
 from submissions,
      jsonb_path_query(input_data, '$.students[*].studentHomeschoolAffidavitNumber') affidavit
 where submitted_at is not null
   and affidavit <> '""'
),
total_with_valid_affidavit_number as (
  -- "step 2.1" - how many apps can be matched to a homeschool affidavit number
  select distinct(id) as id
  from submissions,
       jsonb_path_query(input_data, '$.students[*].studentHomeschoolAffidavitNumber') affidavit
  inner join homeschool_affidavit_number_matches m on affidavit#>>'{}' = m.original
  where submitted_at is not null
    and affidavit is not null
),
total_with_virtual_school as (
  select distinct(id) as id
  from submissions,
       jsonb_path_query(submissions.input_data, '$.students[*].studentVirtualSchoolName') virtual
  where submitted_at is not null
    and virtual <> '""'
)
select
  count(*) as count_total,
  count(*) filter (where total_submitted.id is not null) as count_submitted,
  -- step 1:
  count(*) filter (where total_with_eligible_student.id is not null) as count_eligible_student,
  count(*) filter (where total_with_eligible_student.id is not null and total_with_affidavit_number.id is not null) as count_eligible_student_and_affidavit,
  -- combined step 1 + 2.1:
  count(*) filter (where total_with_eligible_student.id is not null and total_with_valid_affidavit_number.id is not null) as count_eligible_student_and_valid_affidavit,
  -- combined step 1 without 2.1 but with a virtual school (take 18% of this number and add it to
  -- count_eligible_student_and_affidavit) to the overall approval rate estimate.
  count(*) filter (
    where total_with_eligible_student.id is not null
      and total_with_valid_affidavit_number.id is null
      and total_with_virtual_school.id is not null
  ) as count_eligible_student_and_virtual_school_only
from submissions
left outer join total_submitted on total_submitted.id = submissions.id
left outer join total_with_eligible_student on total_with_eligible_student.id = submissions.id
left outer join total_with_affidavit_number on total_with_affidavit_number.id = submissions.id
left outer join total_with_valid_affidavit_number on total_with_valid_affidavit_number.id = submissions.id
left outer join total_with_virtual_school on total_with_virtual_school.id = submissions.id
where flow = 'pebt';

-- cross reference this list against:
-- https://docs.google.com/spreadsheets/d/1TbGl2k4sw0i5xfujjd5JNVZYlsWMTqsr/edit#gid=227639467
select
  id,
  virtual
from submissions,
  jsonb_path_query(submissions.input_data, '$.students[0].studentVirtualSchoolName') virtual
where submitted_at is not null and virtual <> '""' order by id asc limit 100
