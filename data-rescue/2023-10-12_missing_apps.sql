-- 001088490 - one straggler app that needed to be merged.
-- but it's a duplicate of 001116170 anyway so we can just send the duplicate one again.
update submissions set updated_at = now() where input_data ->> 'confirmationNumber' in ('001116170');
update transmissions
set submitted_to_state_at = null, submitted_to_state_filename = null, updated_at = now()
where confirmation_number in ('001116170');

-- 001756112 - typo of 001756122
select * from transmissions where confirmation_number like '0017561%';
select * from submissions where input_data->>'confirmationNumber' = '001756122'; -- fixed some NBSP characters in the street address
update submissions set updated_at = now() where input_data ->> 'confirmationNumber' in ('001756122');
update transmissions
set submitted_to_state_at = null, submitted_to_state_filename = null, updated_at = now()
where confirmation_number in ('001756122');

-- 112 missing apps - assume they will render properly when resent
update transmissions
set submitted_to_state_at       = null,
    submitted_to_state_filename = null,
    updated_at                  = now()
where confirmation_number in
      ('001000748', '001002180', '001002447', '001002751', '001003691', '001004411', '001009018', '001009385', '001011623', '001017393', '001018573', '001036837', '001039011',
       '001040243', '001047156', '001047262', '001051254', '001067623', '001072933', '001073292', '001087629', '001088490', '001095512', '001096416', '001118862', '001160028',
       '001168520', '001193265', '001215636', '001228069', '001236559', '001236918', '001237591', '001238613', '001239318', '001239829', '001246270', '001248581', '001250281',
       '001258576', '001260749', '001275067', '001291470', '001295714', '001298396', '001311479', '001318338', '001320833', '001322660', '001329982', '001334115', '001338087',
       '001352734', '001353825', '001358740', '001360061', '001363635', '001363845', '001368569', '001377747', '001382750', '001387795', '001389880', '001402259', '001404013',
       '001412142', '001453049', '001455627', '001468499', '001505477', '001527580', '001538633', '001539982', '001540271', '001567711', '001581152', '001590753', '001620050',
       '001642696', '001653280', '001659113', '001660364', '001676583', '001682029', '001684125', '001686074', '001687379', '001691680', '001698333', '001705138', '001708231',
       '001710987', '001713037', '001715381', '001718558', '001728077', '001756122', '001789618', '001789719', '001792376', '001794861', '001795426', '001800820', '001808481',
       '001809670', '001810845', '001810993', '001811671', '001812928', '001824042', '001828294', '001848899'
        );
