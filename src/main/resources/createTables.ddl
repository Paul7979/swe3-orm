create table if not exists teachers
(
    id        varchar not null
        constraint teachers_pk
            primary key,
    salary    integer,
    hiredate  varchar,
    counter   integer,
    name      varchar,
    firstname varchar,
    birthdate varchar,
    gender    varchar
);

create unique index if not exists teachers_id_uindex
    on teachers (id);

create table if not exists student
(
    id        varchar not null
        constraint student_pk
            primary key,
    name      varchar,
    firstname varchar,
    birthdate varchar,
    gender    varchar,
    grade     integer,
    hiredate  varchar
);


create unique index if not exists student_id_uindex
    on student (id);

create table if not exists class
(
    id         varchar not null
        constraint class_pk
            primary key,
    name       varchar,
    fk_teacher varchar
        constraint fk_teacher
            references teachers
);


create unique index if not exists class_id_uindex
    on class (id);

create table if not exists courses
(
    id         varchar not null
        constraint table_name_pk
            primary key,
    name       varchar,
    active     boolean,
    teacher_fk varchar
        constraint teacher_fk
            references teachers
);


create unique index if not exists table_name_id_uindex
    on courses (id);

create table student_classes
(
    fk_class   varchar
        constraint student_courses_class_id_fk
            references class,
    fk_student varchar
        constraint student_courses_student_id_fk
            references student
);

create unique index student_classes_fk_class_fk_student_uindex
    on student_classes (fk_class, fk_student);

create table student_courses
(
    fk_course  varchar
        constraint student_courses_class_id_fk
            references class,
    fk_student varchar
        constraint student_courses_student_id_fk
            references student
);

create unique index student_courses_fk_course_fk_student_uindex
    on student_courses (fk_course, fk_student);



