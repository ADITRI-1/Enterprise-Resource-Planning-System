-- Schema for the University ERP. Column names/types mirror the JPA entities in
-- com.iiitd.erp.persistent.entity; Hibernate runs with ddl-auto=validate against it.

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE SCHEMA IF NOT EXISTS auth_db;
CREATE SCHEMA IF NOT EXISTS erp_db;

-- ---------------------------------------------------------------- auth_db

CREATE TABLE auth_db.user_auth (
    user_id          UUID         PRIMARY KEY,
    username         VARCHAR(100) NOT NULL UNIQUE,
    role             VARCHAR(20)  NOT NULL CHECK (role IN ('student', 'instructor', 'admin')),
    password_hash    VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'locked')),
    last_login       TIMESTAMPTZ  NULL,
    password_history JSONB        NULL
);

-- ---------------------------------------------------------------- erp_db

CREATE TABLE erp_db.settings (
    key   VARCHAR(100) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);

CREATE TABLE erp_db.program (
    program_id   VARCHAR(50)  PRIMARY KEY,
    program_desc VARCHAR(255) NOT NULL,
    program_type VARCHAR(100) NOT NULL,
    duration     INTEGER      NULL CHECK (duration > 0)
);

CREATE TABLE erp_db.department (
    dept_id   VARCHAR(80)  PRIMARY KEY,
    dept_desc VARCHAR(255) NOT NULL
);

CREATE TABLE erp_db.rooms (
    room_id  VARCHAR(50)  PRIMARY KEY,
    building VARCHAR(100) NOT NULL,
    capacity INTEGER      NOT NULL CHECK (capacity > 0),
    type     VARCHAR(40)  NULL
);

CREATE TABLE erp_db.student (
    student_id    UUID         PRIMARY KEY REFERENCES auth_db.user_auth (user_id) ON DELETE CASCADE,
    student_name  VARCHAR(50)  NOT NULL,
    roll_no       VARCHAR(50)  NOT NULL UNIQUE,
    date_of_birth VARCHAR(10)  NOT NULL,
    program_id    VARCHAR(120) NOT NULL REFERENCES erp_db.program (program_id),
    year          INTEGER      NOT NULL
);

CREATE TABLE erp_db.instructor (
    instructor_id   UUID         PRIMARY KEY REFERENCES auth_db.user_auth (user_id) ON DELETE CASCADE,
    instructor_code VARCHAR(30)  NOT NULL UNIQUE,
    instructor_name VARCHAR(80)  NOT NULL,
    date_of_birth   VARCHAR(10)  NOT NULL,
    dept_id         VARCHAR(80)  NOT NULL REFERENCES erp_db.department (dept_id),
    email           VARCHAR(255) NULL,
    phone           VARCHAR(255) NULL
);

CREATE TABLE erp_db.course_details (
    course_id  VARCHAR(64)  PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    credits    INTEGER      NOT NULL CHECK (credits > 0),
    dept_id    VARCHAR(80)  NOT NULL REFERENCES erp_db.department (dept_id),
    change_log JSONB        NULL
);

-- section_id is built by the service as COURSE-INSTRUCTOR-YY-TERM, e.g. CSE101-INS001-26-1
CREATE TABLE erp_db.sections (
    section_id            VARCHAR(80) PRIMARY KEY,
    course_id             VARCHAR(64) NOT NULL REFERENCES erp_db.course_details (course_id),
    instructor_code       VARCHAR(50) NOT NULL REFERENCES erp_db.instructor (instructor_code) ON UPDATE CASCADE,
    semester              VARCHAR(20) NOT NULL CHECK (semester IN ('MONSOON', 'WINTER')),
    year                  INTEGER     NOT NULL CHECK (year BETWEEN 2000 AND 2100),
    capacity              INTEGER     NOT NULL CHECK (capacity > 0),
    enrollment_count      INTEGER     NOT NULL DEFAULT 0 CHECK (enrollment_count >= 0),
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    registration_deadline DATE        NOT NULL,
    CHECK (enrollment_count <= capacity)
);

CREATE INDEX idx_sections_course_term ON erp_db.sections (course_id, semester, year);
CREATE INDEX idx_sections_instructor  ON erp_db.sections (instructor_code);

CREATE TABLE erp_db.class_schedules (
    schedule_id UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id  VARCHAR(80) NOT NULL REFERENCES erp_db.sections (section_id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL
        CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    room_id     VARCHAR(50) NOT NULL REFERENCES erp_db.rooms (room_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CHECK (end_time > start_time)
);

-- A room can't hold two classes at overlapping times on the same day.
ALTER TABLE erp_db.class_schedules
    ADD CONSTRAINT ex_room_day_no_overlap
    EXCLUDE USING gist (
        room_id WITH =,
        day_of_week WITH =,
        int4range(
            (EXTRACT(HOUR FROM start_time)::int * 60 + EXTRACT(MINUTE FROM start_time)::int),
            (EXTRACT(HOUR FROM end_time)::int * 60 + EXTRACT(MINUTE FROM end_time)::int),
            '[)'
        ) WITH &&
    );

CREATE INDEX idx_schedules_section  ON erp_db.class_schedules (section_id);
CREATE INDEX idx_schedules_room_day ON erp_db.class_schedules (room_id, day_of_week);

CREATE TABLE erp_db.enrollments (
    enrollment_id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID        NOT NULL REFERENCES erp_db.student (student_id) ON DELETE CASCADE,
    section_id      VARCHAR(80) NOT NULL REFERENCES erp_db.sections (section_id) ON DELETE CASCADE,
    enrollment_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    status          VARCHAR(16) NOT NULL CHECK (status IN ('enrolled', 'dropped', 'completed')),
    UNIQUE (student_id, section_id)
);

CREATE INDEX idx_enrollments_section_status ON erp_db.enrollments (section_id, status);

CREATE TABLE erp_db.assessment_components (
    component_id UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id   VARCHAR(80)  NOT NULL REFERENCES erp_db.sections (section_id) ON DELETE CASCADE,
    name         VARCHAR(80)  NOT NULL,
    weight       NUMERIC(6,4) NOT NULL CHECK (weight >= 0 AND weight <= 1),
    score_out_of INTEGER      NOT NULL CHECK (score_out_of > 0),
    due_date     DATE         NULL,
    UNIQUE (section_id, name)
);

CREATE TABLE erp_db.assessment_scores (
    score_id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID         NOT NULL REFERENCES erp_db.enrollments (enrollment_id) ON DELETE CASCADE,
    component_id  UUID         NOT NULL REFERENCES erp_db.assessment_components (component_id) ON DELETE CASCADE,
    score         NUMERIC(7,3) NOT NULL CHECK (score >= 0),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(64)  NOT NULL,
    UNIQUE (enrollment_id, component_id)
);

-- Final grades: one row per enrollment with component = 'FINAL', written by "compute grades".
CREATE TABLE erp_db.grades (
    grade_id       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id  UUID         NOT NULL REFERENCES erp_db.enrollments (enrollment_id) ON DELETE CASCADE,
    component      VARCHAR(50)  NOT NULL,
    score          NUMERIC(7,3) NOT NULL CHECK (score >= 0),
    final_grade    VARCHAR(5)   NULL,
    weighted_score NUMERIC(9,4) NULL,
    UNIQUE (enrollment_id, component)
);

INSERT INTO erp_db.settings (key, value) VALUES
    ('maintenanceMode', 'false'),
    ('workingHours.start', '08:00'),
    ('workingHours.end', '20:00');
