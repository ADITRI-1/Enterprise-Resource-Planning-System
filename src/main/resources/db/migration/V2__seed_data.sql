-- Demo data. Passwords (bcrypt-hashed below):
--   admin1 / Admin@123     inst1, inst2 / Inst@123     stu1, stu2 / Stu@123

INSERT INTO auth_db.user_auth (user_id, username, role, password_hash, status) VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin1', 'admin',      '$2a$12$SHVSbSykZInCKh2yoJ3FUu.cG9RJHUdnMF39aCyzBFDhlOXuUNanC', 'active'),
    ('22222222-2222-2222-2222-000000000001', 'inst1',  'instructor', '$2a$12$vVsKejW/0ztQWC/tH6elruNDPrUYjD2YjUgKQaGcRsc.V2jv3JsQy', 'active'),
    ('22222222-2222-2222-2222-000000000002', 'inst2',  'instructor', '$2a$12$vVsKejW/0ztQWC/tH6elruNDPrUYjD2YjUgKQaGcRsc.V2jv3JsQy', 'active'),
    ('33333333-3333-3333-3333-000000000001', 'stu1',   'student',    '$2a$12$zQjcPTwLEutRPc0BqwfwreIPoIPaHpN7D77l5NXhjl8oL3GMwOJVa', 'active'),
    ('33333333-3333-3333-3333-000000000002', 'stu2',   'student',    '$2a$12$zQjcPTwLEutRPc0BqwfwreIPoIPaHpN7D77l5NXhjl8oL3GMwOJVa', 'active');

INSERT INTO erp_db.department (dept_id, dept_desc) VALUES
    ('CSE', 'Computer Science and Engineering'),
    ('MTH', 'Mathematics');

INSERT INTO erp_db.program (program_id, program_desc, program_type, duration) VALUES
    ('BTECH-CSE', 'B.Tech in Computer Science and Engineering', 'B.Tech', 4);

INSERT INTO erp_db.rooms (room_id, building, capacity, type) VALUES
    ('C101',    'Academic Block', 60,  'Classroom'),
    ('C102',    'Academic Block', 40,  'Classroom'),
    ('LHC-201', 'Lecture Hall Complex', 120, 'Lecture Hall');

INSERT INTO erp_db.student (student_id, student_name, roll_no, date_of_birth, program_id, year) VALUES
    ('33333333-3333-3333-3333-000000000001', 'Aarav Sharma', '2024001', '15-08-2005', 'BTECH-CSE', 2024),
    ('33333333-3333-3333-3333-000000000002', 'Diya Verma',   '2024002', '02-03-2006', 'BTECH-CSE', 2024);

INSERT INTO erp_db.instructor (instructor_id, instructor_code, instructor_name, date_of_birth, dept_id, email, phone) VALUES
    ('22222222-2222-2222-2222-000000000001', 'INS001', 'Rohan Mehta', '10-01-1980', 'CSE', 'rohan.mehta@univ.edu', '9800000001'),
    ('22222222-2222-2222-2222-000000000002', 'INS002', 'Priya Nair',  '22-07-1984', 'MTH', 'priya.nair@univ.edu',  '9800000002');

INSERT INTO erp_db.course_details (course_id, title, credits, dept_id) VALUES
    ('CSE101', 'Introduction to Programming', 4, 'CSE'),
    ('CSE201', 'Data Structures and Algorithms', 4, 'CSE'),
    ('CSE301', 'Operating Systems', 4, 'CSE'),
    ('MTH101', 'Linear Algebra', 4, 'MTH');

-- CSE201 has capacity 2 and both students in it, so it demonstrates "Section full".
INSERT INTO erp_db.sections (section_id, course_id, instructor_code, semester, year, capacity, enrollment_count, is_active, registration_deadline) VALUES
    ('CSE101-INS001-26-1', 'CSE101', 'INS001', 'MONSOON', 2026, 60, 1, TRUE, DATE '2027-01-31'),
    ('CSE201-INS001-26-1', 'CSE201', 'INS001', 'MONSOON', 2026, 2,  2, TRUE, DATE '2027-01-31'),
    ('MTH101-INS002-26-1', 'MTH101', 'INS002', 'MONSOON', 2026, 40, 0, TRUE, DATE '2027-01-31');

INSERT INTO erp_db.class_schedules (section_id, day_of_week, start_time, end_time, room_id) VALUES
    ('CSE101-INS001-26-1', 'MONDAY',    '09:00', '10:30', 'C101'),
    ('CSE101-INS001-26-1', 'WEDNESDAY', '09:00', '10:30', 'C101'),
    ('CSE201-INS001-26-1', 'TUESDAY',   '11:00', '12:30', 'C102'),
    ('CSE201-INS001-26-1', 'THURSDAY',  '11:00', '12:30', 'C102'),
    ('MTH101-INS002-26-1', 'MONDAY',    '11:00', '12:30', 'LHC-201'),
    ('MTH101-INS002-26-1', 'FRIDAY',    '14:00', '15:30', 'LHC-201');

INSERT INTO erp_db.enrollments (enrollment_id, student_id, section_id, status) VALUES
    ('44444444-4444-4444-4444-000000000001', '33333333-3333-3333-3333-000000000001', 'CSE101-INS001-26-1', 'enrolled'),
    ('44444444-4444-4444-4444-000000000002', '33333333-3333-3333-3333-000000000001', 'CSE201-INS001-26-1', 'enrolled'),
    ('44444444-4444-4444-4444-000000000003', '33333333-3333-3333-3333-000000000002', 'CSE201-INS001-26-1', 'enrolled');

-- Weighting rule for CSE101: Quiz 20% / Midsem 30% / Endsem 50%
INSERT INTO erp_db.assessment_components (component_id, section_id, name, weight, score_out_of) VALUES
    ('55555555-5555-5555-5555-000000000001', 'CSE101-INS001-26-1', 'Quiz',   0.2000, 20),
    ('55555555-5555-5555-5555-000000000002', 'CSE101-INS001-26-1', 'Midsem', 0.3000, 50),
    ('55555555-5555-5555-5555-000000000003', 'CSE101-INS001-26-1', 'Endsem', 0.5000, 100);

INSERT INTO erp_db.assessment_scores (enrollment_id, component_id, score, updated_by) VALUES
    ('44444444-4444-4444-4444-000000000001', '55555555-5555-5555-5555-000000000001', 17, 'inst1'),
    ('44444444-4444-4444-4444-000000000001', '55555555-5555-5555-5555-000000000002', 41, 'inst1'),
    ('44444444-4444-4444-4444-000000000001', '55555555-5555-5555-5555-000000000003', 80, 'inst1');
