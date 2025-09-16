create table student(
    sid text not null primary key,
    sname text not null,
    class_group char(5) not null,
    email text,
    phone int
);

create table images(
    sid text not null,
    img_path text not null,
    created_at text default CURRENT_TIMESTAMP,
    constraint images_pk primary key (sid, img_path),
    constraint images_fk1 foreign key(sid) references student(sid)
);

create table session(
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    class_group char(5) not null,
    start_time text not null,
    end_time text, 
    is_active boolean not null default 1
);

create table session_roster(
    session_id int not null,
    sid text not null,
    constraint session_roster_pk primary key(session_id, sid),
    constraint session_roster_fk1 foreign key(session_id) references session(session_id),
    constraint session_roster_fk2 foreign key(sid) references student(sid)
);

create table attendance_logs(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id int not null,
    sid text not null,
    event_type text not null,
    timestamp text NOT NULL DEFAULT (datetime('now')),
    confidence REAL,
    constraint attendance_logs_fk1 foreign key(session_id) references session(session_id),
    constraint attendance_logs_fk2 foreign key(sid) references student(sid)
);