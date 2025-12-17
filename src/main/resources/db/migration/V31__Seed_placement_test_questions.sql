-- A1 Questions (Beginner)
INSERT INTO placement_test_questions (id, content, option_a, option_b, option_c, option_d, correct_option, difficulty_level, category, created_at, updated_at)
VALUES 
(gen_random_uuid(), 'I _____ from Vietnam.', 'am', 'is', 'are', 'be', 'A', 'A1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'She _____ to work every day.', 'go', 'goes', 'going', 'gone', 'B', 'A1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'What time _____ it?', 'is', 'am', 'are', 'be', 'A', 'A1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'I have a meeting _____ Monday.', 'in', 'at', 'on', 'to', 'C', 'A1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'Please _____ the email.', 'send', 'sends', 'sending', 'sent', 'A', 'A1', 'SITUATIONAL', NOW(), NOW());

-- A2/B1 Questions (Intermediate)
INSERT INTO placement_test_questions (id, content, option_a, option_b, option_c, option_d, correct_option, difficulty_level, category, created_at, updated_at)
VALUES 
(gen_random_uuid(), 'If I _____ you, I would accept the offer.', 'was', 'am', 'were', 'be', 'C', 'B1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'We look forward to _____ from you.', 'hear', 'hearing', 'heard', 'hears', 'B', 'B1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'The project was _____ successful than we expected.', 'more', 'most', 'much', 'many', 'A', 'B1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'Could you please _____ me know when you arrive?', 'let', 'make', 'allow', 'give', 'A', 'B1', 'SITUATIONAL', NOW(), NOW()),
(gen_random_uuid(), 'I apologize _____ the delay.', 'of', 'for', 'about', 'with', 'B', 'B1', 'VOCABULARY', NOW(), NOW());

-- B2/C1 Questions (Advanced)
INSERT INTO placement_test_questions (id, content, option_a, option_b, option_c, option_d, correct_option, difficulty_level, category, created_at, updated_at)
VALUES 
(gen_random_uuid(), 'Not only _____ the deadline, but he also exceeded expectations.', 'he met', 'did he meet', 'he did meet', 'met he', 'B', 'C1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'The company is currently _____ a major restructuring.', 'undergoing', 'undertaking', 'overcoming', 'upholding', 'A', 'C1', 'VOCABULARY', NOW(), NOW()),
(gen_random_uuid(), 'Had I known about the issue, I _____ it immediately.', 'would fix', 'will fix', 'would have fixed', 'had fixed', 'C', 'C1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'It is essential that he _____ present at the meeting.', 'is', 'be', 'will be', 'was', 'B', 'C1', 'GRAMMAR', NOW(), NOW()),
(gen_random_uuid(), 'The proposal was rejected on the _____ that it was too expensive.', 'grounds', 'basis', 'reasons', 'causes', 'A', 'C1', 'VOCABULARY', NOW(), NOW());
