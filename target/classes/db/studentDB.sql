create table student(
    sid varchar not null primary key,
    sname varchar not null,
    class_group varchar not null,
    email varchar,
    phone varchar
);

create table images(
    sid varchar not null,
    img_path varchar not null,
    created_at varchar not null DEFAULT (datetime('now')),
    constraint images_pk primary key (sid, img_path),
    constraint images_fk1 foreign key(sid) references student(sid)
);

create table session(
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_name  varchar NOT NULL,
    session_date varchar NOT NULL DEFAULT (date('now')),
    class_group varchar not null,
    start_time varchar not null,
    end_time varchar, 
    location varchar,
    is_active INTEGER not null default 1
    CHECK (is_active IN (0,1))
);

create table session_roster(
    session_id INTEGER not null,
    sid varchar not null,
    constraint session_roster_pk primary key(session_id, sid),
    constraint session_roster_fk1 foreign key(session_id) references session(session_id),
    constraint session_roster_fk2 foreign key(sid) references student(sid)
);

create table attendance_logs(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER not null,
    sid varchar not null,
    event_type varchar not null,
    timestamp text NOT NULL DEFAULT (datetime('now')),
    confidence REAL,
    constraint attendance_logs_fk1 foreign key(session_id) references session(session_id),
    constraint attendance_logs_fk2 foreign key(sid) references student(sid)
);

CREATE TABLE session_attendance(
    session_id INTEGER NOT NULL,
    sid varchar NOT NULL,
    status varchar NOT NULL,        -- 'Pending','Present','Late','Absent'
    first_seen varchar,                 -- first detection timestamp (nullable)
    method varchar,                 -- 'face','manual' etc.
    PRIMARY KEY(session_id, sid),
    FOREIGN KEY(session_id) REFERENCES session(session_id),
    FOREIGN KEY(sid)        REFERENCES student(sid),
    CHECK (status IN ('Pending','Present','Late','Absent'))
);

INSERT INTO student (sid, sname, class_group, email, phone) VALUES
('S001', 'John Doe', 'CS101', 'john.doe@example.com', '123-456-7890'),
('S002', 'Jane Smith', 'CS101', 'jane.smith@example.com', '987-654-3210'),
('S003', 'Michael Johnson', 'CS102', 'michael.johnson@example.com', '555-123-4567'),
('S004', 'Emily Davis', 'CS103', 'emily.davis@example.com', '555-987-6543'),
('S005', 'Daniel Lee', 'CS104', 'daniel.lee@example.com', '555-111-2222');

-- Sample Images for Students
INSERT INTO images (sid, img_path, created_at) VALUES
('S001', 'images/john_doe.jpg', datetime('now')),
('S002', 'images/jane_smith.jpg', datetime('now')),
('S003', 'images/michael_johnson.jpg', datetime('now')),
('S004', 'images/emily_davis.jpg', datetime('now')),
('S005', 'images/daniel_lee.jpg', datetime('now'));

-- Sample Sessions
INSERT INTO session (course_name, session_date, class_group, start_time, end_time, location, is_active) VALUES
('Intro to Computer Science', '2025-10-25', 'CS101', '09:00', '11:00', 'Room 101', 1),
('Data Structures', '2025-10-26', 'CS102', '10:00', '12:00', 'Room 102', 1),
('Algorithms', '2025-10-27', 'CS103', '11:00', '13:00', 'Room 103', 1);

-- Sample Session Roster (which associates students with sessions)
INSERT INTO session_roster (session_id, sid) VALUES
(1, 'S001'),
(1, 'S002'),
(2, 'S003'),
(2, 'S004'),
(3, 'S005');

-- Sample Attendance Logs
INSERT INTO attendance_logs (session_id, sid, event_type, timestamp, confidence) VALUES
(1, 'S001', 'Present', datetime('now'), 0.95),
(1, 'S002', 'Absent', datetime('now'), 0.50),
(2, 'S003', 'Present', datetime('now'), 0.90),
(2, 'S004', 'Late', datetime('now'), 0.85),
(3, 'S005', 'Present', datetime('now'), 1.00);

-- Sample Session Attendance
INSERT INTO session_attendance (session_id, sid)