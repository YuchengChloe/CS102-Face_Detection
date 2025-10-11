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
    created_at varchar default CURRENT_TIMESTAMP,
    constraint images_pk primary key (sid, img_path),
    constraint images_fk1 foreign key(sid) references student(sid)
);

create table session(
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    class_group varchar not null,
    start_time varchar not null,
    end_time varchar, 
    is_active boolean not null default 1
);

create table session_roster(
    session_id int not null,
    sid varchar not null,
    constraint session_roster_pk primary key(session_id, sid),
    constraint session_roster_fk1 foreign key(session_id) references session(session_id),
    constraint session_roster_fk2 foreign key(sid) references student(sid)
);

create table attendance_logs(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id int not null,
    sid varchar not null,
    event_type varchar not null,
    timestamp varchar NOT NULL DEFAULT (datetime('now')),
    confidence REAL,
    constraint attendance_logs_fk1 foreign key(session_id) references session(session_id),
    constraint attendance_logs_fk2 foreign key(sid) references student(sid)
);