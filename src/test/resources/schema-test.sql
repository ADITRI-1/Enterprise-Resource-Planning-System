-- Clean
DROP SCHEMA IF EXISTS auth_db CASCADE;
DROP SCHEMA IF EXISTS erp_db CASCADE;

CREATE SCHEMA auth_db;
CREATE SCHEMA erp_db;

-- Auth
CREATE TABLE auth_db.user_auth (
  user_id uuid PRIMARY KEY,
  username varchar(100) UNIQUE NOT NULL,
  role varchar(20) NOT NULL CHECK (role IN ('student','instructor','admin')),
  password_hash varchar(255) NOT NULL,
  status varchar(20) NOT NULL CHECK (status IN ('active','inactive','locked')),
  last_login timestamptz NULL,
  password_history jsonb NULL
);

-- ERP
CREATE TABLE erp_db.program (
  program_id varchar(50) PRIMARY KEY,
  program_desc varchar(255) NOT NULL,
  program_type varchar(50) NOT NULL,
  duration int NOT NULL
);

CREATE TABLE erp_db.department (
  dept_id varchar(50) PRIMARY KEY,
  dept_desc varchar(255) NOT NULL
);

CREATE TABLE erp_db.student (
  student_id uuid PRIMARY KEY REFERENCES auth_db.user_auth(user_id),
  student_name varchar(50) NOT NULL,
  roll_no varchar(50) UNIQUE NOT NULL,
  program_id varchar(120) NOT NULL REFERENCES erp_db.program(program_id),
  year int NOT NULL CHECK (year BETWEEN 1 AND 10)
);

CREATE TABLE erp_db.instructor (
  instructor_id uuid PRIMARY KEY REFERENCES auth_db.user_auth(user_id),
  instructor_name varchar(50) NOT NULL,
  dept_id varchar(80) NOT NULL REFERENCES erp_db.department(dept_id),
  email varchar(100),
  instructor_code varchar(50) UNIQUE NOT NULL
);

CREATE TABLE erp_db.course_details (
  course_id varchar(255) PRIMARY KEY,
  title varchar(255) NOT NULL,
  credits int NOT NULL,
  dept_id varchar(80) NOT NULL REFERENCES erp_db.department(dept_id),
  change_log jsonb NULL
);

CREATE TABLE erp_db.sections (
  section_id uuid PRIMARY KEY,
  course_id varchar(64) NOT NULL REFERENCES erp_db.course_details(course_id),
  instructor_id uuid NULL REFERENCES erp_db.instructor(instructor_id),
  semester varchar(20) NOT NULL,
  year int NOT NULL CHECK (year BETWEEN 2000 AND 2100),
  capacity int NOT NULL CHECK (capacity > 0),
  enrollment_count int NOT NULL DEFAULT 0 CHECK (enrollment_count >= 0),
  is_active boolean NOT NULL DEFAULT true,
  registration_deadline date NOT NULL,
  CHECK (enrollment_count <= capacity)
);

CREATE TABLE erp_db.enrollments (
  enrollment_id uuid PRIMARY KEY,
  student_id uuid NOT NULL REFERENCES erp_db.student(student_id) ON DELETE CASCADE,
  section_id uuid NOT NULL REFERENCES erp_db.sections(section_id) ON DELETE CASCADE,
  enrollment_date timestamptz NOT NULL DEFAULT now(),
  status varchar(16) NOT NULL CHECK (status IN ('enrolled','dropped','completed')),
  UNIQUE (student_id, section_id)
);

-- Seed users (password = "password", bcrypt cost 12)
INSERT INTO auth_db.user_auth (user_id, username, role, password_hash, status) VALUES
 ('00000000-0000-0000-0000-0000000000a1','admin1','admin','$2a$12$0G9wYyV.z8uzdhmN.8vK7eX4xM89V/PDb/R3x4s46HoPazTA/gkBS','active'),
 ('00000000-0000-0000-0000-0000000000b1','inst1','instructor','$2a$12$0G9wYyV.z8uzdhmN.8vK7eX4xM89V/PDb/R3x4s46HoPazTA/gkBS','active'),
 ('00000000-0000-0000-0000-0000000000c1','stu1','student','$2a$12$0G9wYyV.z8uzdhmN.8vK7eX4xM89V/PDb/R3x4s46HoPazTA/gkBS','active'),
 ('00000000-0000-0000-0000-0000000000d1','stu2','student','$2a$12$0G9wYyV.z8uzdhmN.8vK7eX4xM89V/PDb/R3x4s46HoPazTA/gkBS','active');

INSERT INTO erp_db.program (program_id, program_desc, program_type, duration) VALUES
 ('BTECH-CS','B.Tech Computer Science','BTECH',4);

INSERT INTO erp_db.department (dept_id, dept_desc) VALUES
 ('CSE','Computer Science and Engineering');

INSERT INTO erp_db.student (student_id, student_name, roll_no, program_id, year) VALUES
 ('00000000-0000-0000-0000-0000000000c1','Student One','2024CS101','BTECH-CS',2),
 ('00000000-0000-0000-0000-0000000000d1','Student Two','2024CS102','BTECH-CS',2);

INSERT INTO erp_db.instructor (instructor_id, instructor_name, dept_id, email, instructor_code) VALUES
 ('00000000-0000-0000-0000-0000000000b1','Instructor One','CSE','inst1@univ.edu','INST1');

INSERT INTO erp_db.course_details (course_id, title, credits, dept_id) VALUES
 ('CS101','Intro to CS',4,'CSE');

INSERT INTO erp_db.sections (section_id, course_id, instructor_id, semester, year, capacity, enrollment_count, is_active, registration_deadline) VALUES
 ('11111111-1111-1111-1111-111111111111','CS101','00000000-0000-0000-0000-0000000000b1','Fall',2024,50,0,true,CURRENT_DATE + INTERVAL '30 days');

INSERT INTO erp_db.enrollments (enrollment_id, student_id, section_id, status) VALUES
 ('22222222-2222-2222-2222-222222222222','00000000-0000-0000-0000-0000000000c1','11111111-1111-1111-1111-111111111111','enrolled'),
 ('33333333-3333-3333-3333-333333333333','00000000-0000-0000-0000-0000000000d1','11111111-1111-1111-1111-111111111111','enrolled');

UPDATE erp_db.sections
SET enrollment_count = (SELECT COUNT(*) FROM erp_db.enrollments WHERE section_id = erp_db.sections.section_id);
