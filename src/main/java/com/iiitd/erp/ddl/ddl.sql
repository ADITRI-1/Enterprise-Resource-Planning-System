-- In the SAME database
CREATE SCHEMA IF NOT EXISTS auth_db;
CREATE SCHEMA IF NOT EXISTS erp_db;

DROP TABLE if exists auth_db.user_auth cascade;

CREATE TABLE auth_db.user_auth (
	user_id uuid NOT NULL,
	username varchar(100) NOT NULL,
	"role" varchar(20) NOT NULL,
	password_hash varchar(255) NOT NULL,
	status varchar(20) DEFAULT 'active'::character varying NOT NULL,
	last_login timestamptz NULL,
	password_history jsonb NULL,
	CONSTRAINT user_auth_pkey PRIMARY KEY (user_id),
	CONSTRAINT user_auth_role_check CHECK (((role)::text = ANY ((ARRAY['student'::character varying, 'instructor'::character varying, 'admin'::character varying])::text[]))),
	CONSTRAINT user_auth_status_check CHECK (((status)::text = ANY ((ARRAY['active'::character varying, 'inactive'::character varying, 'locked'::character varying])::text[]))),
	CONSTRAINT user_auth_username_key UNIQUE (username)
);

DROP TABLE if exists erp_db.settings cascade;
DROP TABLE if exists erp_db.program cascade;
DROP TABLE if exists erp_db.rooms;
DROP TABLE if exists erp_db.student cascade;
DROP TABLE if exists erp_db.department cascade;
DROP TABLE if exists erp_db.instructor cascade;
DROP TABLE if exists erp_db.course_details cascade;
DROP TABLE if exists erp_db.sections cascade;


CREATE TABLE erp_db.settings (
  key   VARCHAR(100) PRIMARY KEY,
  value VARCHAR(255) NOT NULL
);

INSERT INTO erp_db.settings (key, value)
VALUES ('maintenanceMode', 'false')
ON CONFLICT (key) DO NOTHING;

CREATE TABLE erp_db.program (
	program_id varchar(50) NOT null,
	program_desc varchar(255) not null,
	program_type varchar(50) NOT NULL,
	duration int4 NOT null,
	CONSTRAINT program_pkey PRIMARY KEY (program_id)
);

CREATE TABLE erp_db.rooms (
  room_id      UUID PRIMARY key,
  room_number  VARCHAR(20)  NOT NULL UNIQUE,
  building     VARCHAR(100) NOT NULL,
  capacity     INTEGER      NOT NULL CHECK (capacity > 0),
  type         VARCHAR(40)  NULL
);

CREATE TABLE erp_db.department (
	dept_id varchar(50) NOT null,
	dept_desc varchar(255) not null,
	CONSTRAINT department_pkey PRIMARY KEY (dept_id)
);

CREATE TABLE erp_db.student (
	student_id uuid NOT null references auth_db.user_auth(user_id),
	student_name varchar(50) not null,
	roll_no varchar(50) NOT NULL,
	program_id varchar(120) NOT null references erp_db.program (program_id),
	year int4 NOT NULL,
	CONSTRAINT student_pkey PRIMARY KEY (student_id),
	CONSTRAINT student_roll_no_key UNIQUE (roll_no),
	CONSTRAINT student_year_check CHECK (((year >= 1) AND (year <= 10)))
);

CREATE TABLE erp_db.instructor (
	instructor_id uuid NOT null	references auth_db.user_auth(user_id) ,
	instructor_name varchar(50) not null,
	dept_id varchar(80) NOT null references erp_db.department(dept_id),
	email varchar(100),
	instructor_code varchar(50) not null,
	CONSTRAINT instructor_pkey PRIMARY KEY (instructor_id),
	constraint instructor_code_key unique (instructor_code)
);

CREATE TABLE erp_db.course_details (
	course_id varchar(255) NOT NULL,
	title varchar(255) NOT NULL,
	credits int4 NOT NULL,
	dept_id varchar(80) not null references erp_db.department(dept_id),
	change_log jsonb NULL,
	CONSTRAINT course_detail_pkey PRIMARY KEY (course_id)
);

CREATE TABLE erp_db.sections (
  section_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id           	VARCHAR(64)  NOT null REFERENCES erp_db.course_details(course_id),
  instructor_id         UUID null REFERENCES erp_db.instructor(instructor_id),
  semester              VARCHAR(20)  NOT NULL,
  year                  INTEGER      NOT NULL CHECK (year BETWEEN 2000 AND 2100),
  capacity              INTEGER      NOT NULL CHECK (capacity > 0),
  enrollment_count      INTEGER      NOT NULL DEFAULT 0 CHECK (enrollment_count >= 0),
  is_active				boolean      not null default true,
  registration_deadline DATE         NOT NULL,
  CHECK (enrollment_count <= capacity)
);

-- Helpful indexes for common queries
--CREATE INDEX IF NOT EXISTS idx_sections_course_sem ON public.sections (course_code, semester, year);
--CREATE INDEX IF NOT EXISTS idx_sections_deadline   ON public.sections (registration_deadline);
-- Helpful indexes for scheduling/search
--CREATE INDEX IF NOT EXISTS idx_rooms_building ON erp_db.rooms (building);
--CREATE INDEX IF NOT EXISTS idx_rooms_capacity ON erp_db.rooms (capacity);


 DROP TABLE if exists erp_db.class_schedules;

-- Use ENUM for clean day validation (or skip to VARCHAR version below)

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'weekday') THEN
    CREATE TYPE weekday AS ENUM ('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday');
  END IF;
END$$;

CREATE TABLE erp_db.class_schedules (
  schedule_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  section_id   UUID    NOT NULL
              REFERENCES erp_db.sections(section_id),
  day_of_week  weekday NOT NULL,
  start_time   TIME    NOT NULL,
  end_time     TIME    NOT NULL,
  room_id      UUID    NOT NULL
              REFERENCES erp_db.rooms(room_id)
              ON UPDATE CASCADE ON DELETE RESTRICT,
  CHECK (end_time > start_time)
);

-- Prevent overlapping bookings for the SAME room and day.
-- Uses an expression to build a minute-range: [start, end)
ALTER TABLE erp_db.class_schedules
  ADD CONSTRAINT ex_room_day_no_overlap
  EXCLUDE USING gist (
    room_id WITH =,
    day_of_week WITH =,
    int4range(
      (date_part('hour', start_time)::int * 60 + date_part('minute', start_time)::int),
      (date_part('hour', end_time)::int   * 60 + date_part('minute', end_time)::int),
      '[)'
    ) WITH &&
  );

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_schedules_section   ON erp_db.class_schedules (section_id);
CREATE INDEX IF NOT EXISTS idx_schedules_room_day  ON erp_db.class_schedules (room_id, day_of_week);




--DROP TABLE IF EXISTS erp_db.enrollments;

CREATE TABLE erp_db.enrollments (
  enrollment_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id      UUID NOT NULL
                    REFERENCES erp_db.student(student_id)
                    ON UPDATE CASCADE ON DELETE CASCADE,
  section_id      UUID NOT NULL
                    REFERENCES erp_db.sections(section_id)
                    ON UPDATE CASCADE ON DELETE CASCADE,
  enrollment_date TIMESTAMPTZ NOT NULL DEFAULT now(),
  status          VARCHAR(16) NOT NULL
                    CHECK (status IN ('enrolled','dropped','completed')),
  UNIQUE (student_id, section_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student ON erp_db.enrollments (student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_section ON erp_db.enrollments (section_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status  ON erp_db.enrollments (status);

-- Run once per DB if you want UUID defaults
--CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create table (no inline UNIQUE)
CREATE TABLE erp_db.grades (
  grade_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  enrollment_id   UUID NOT NULL
                  REFERENCES erp_db.enrollments(enrollment_id)
                  ON UPDATE CASCADE ON DELETE CASCADE,
  component       VARCHAR(50) NOT NULL,                  -- 'Quiz 1', 'Midterm', etc.
  score           NUMERIC(7,3) NOT NULL CHECK (score >= 0),
  final_grade     VARCHAR(5) NULL,                       -- can be NULL until computed
  weighted_score  NUMERIC(9,4) NULL
);

-- Enforce no duplicate components per enrollment (unique pair)
CREATE UNIQUE INDEX uq_grades_enrollment_component
  ON erp_db.grades (enrollment_id, component);

-- Helpful indexes
CREATE INDEX idx_grades_enrollment ON erp_db.grades (enrollment_id);
CREATE INDEX idx_grades_component  ON erp_db.grades (component);


-- (Run once per DB if you want UUID defaults)
--CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE erp_db.assessment_components (
  component_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  section_id   UUID NOT NULL
               REFERENCES erp_db.sections(section_id)
               ON UPDATE CASCADE ON DELETE CASCADE,
  name         VARCHAR(80) NOT NULL,          -- e.g., 'Quiz 1', 'Midterm'
  weight       NUMERIC(6,4) NOT NULL CHECK (weight >= 0 AND weight <= 1),
  max_score    INTEGER NOT NULL CHECK (max_score > 0),
  due_date     DATE NULL
);

-- Prevent duplicate component names within the same section
CREATE UNIQUE INDEX uq_assessment_components_section_name
  ON erp_db.assessment_components (section_id, name);

-- Helpful indexes
CREATE INDEX idx_assessment_components_section  ON erp_db.assessment_components (section_id);
CREATE INDEX idx_assessment_components_due_date ON erp_db.assessment_components (due_date);

SET search_path TO som;

INSERT INTO auth_db.user_auth (user_id, username, role, password_hash, status, last_login, password_history)
VALUES (
  gen_random_uuid(),                 -- user_id
  'student1',                        -- username
  'student',                         -- role must match your CHECK ('student'/'instructor'/'admin')
  '$2a$12$lZZLsyQ.MkvPB7d.RzmVbOP0/JZVMDKoEEI2gr9.KtefXLxoiytNu',  -- BCrypt hash generated in DB
  'active',                          -- status must match your CHECK ('active'/'inactive'/'locked')
  now(),
  NULL
);