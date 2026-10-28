-- A course can require others to have been passed (final grade other than F) before registering.
CREATE TABLE erp_db.course_prerequisites (
    course_id       VARCHAR(64) NOT NULL REFERENCES erp_db.course_details (course_id) ON DELETE CASCADE,
    prerequisite_id VARCHAR(64) NOT NULL REFERENCES erp_db.course_details (course_id) ON DELETE CASCADE,
    PRIMARY KEY (course_id, prerequisite_id),
    CHECK (course_id <> prerequisite_id)
);

CREATE TABLE erp_db.notifications (
    notification_id UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES auth_db.user_auth (user_id) ON DELETE CASCADE,
    type            VARCHAR(30)   NOT NULL,
    title           VARCHAR(200)  NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    link            VARCHAR(255)  NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    read_at         TIMESTAMPTZ   NULL
);
CREATE INDEX idx_notifications_user_created ON erp_db.notifications (user_id, created_at DESC);

-- Every state-changing API call (and every login attempt): who, what, when, outcome.
-- Request bodies are deliberately not stored (they can contain passwords).
CREATE TABLE erp_db.audit_log (
    audit_id  BIGSERIAL    PRIMARY KEY,
    at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actor     VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NULL,
    method    VARCHAR(10)  NOT NULL,
    path      VARCHAR(255) NOT NULL,
    status    INTEGER      NOT NULL,
    ip        VARCHAR(64)  NULL
);
CREATE INDEX idx_audit_log_at ON erp_db.audit_log (at DESC);

-- Demo data: CSE201 needs CSE101; CSE301 needs CSE201 and has a section nobody qualifies for yet.
INSERT INTO erp_db.course_prerequisites (course_id, prerequisite_id) VALUES
    ('CSE201', 'CSE101'),
    ('CSE301', 'CSE201');

INSERT INTO erp_db.sections (section_id, course_id, instructor_code, semester, year, capacity, enrollment_count, is_active, registration_deadline) VALUES
    ('CSE301-INS001-26-1', 'CSE301', 'INS001', 'MONSOON', 2026, 30, 0, TRUE, DATE '2027-01-31');

INSERT INTO erp_db.class_schedules (section_id, day_of_week, start_time, end_time, room_id) VALUES
    ('CSE301-INS001-26-1', 'FRIDAY', '09:00', '10:30', 'C102');
