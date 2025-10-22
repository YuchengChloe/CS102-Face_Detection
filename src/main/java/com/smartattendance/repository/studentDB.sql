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