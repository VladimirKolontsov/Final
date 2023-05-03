--liquibase formatted sql

--changeset kmpk:init_schema
DROP TABLE IF EXISTS USER_ROLE;
DROP TABLE IF EXISTS CONTACT;
DROP TABLE IF EXISTS MAIL_CASE;
DROP TABLE IF EXISTS PROFILE;
DROP TABLE IF EXISTS TASK_TAG;
DROP TABLE IF EXISTS TASK_TAGS;
DROP TABLE IF EXISTS USER_BELONG;
DROP TABLE IF EXISTS ACTIVITY;
DROP TABLE IF EXISTS TASK;
DROP TABLE IF EXISTS SPRINT;
DROP TABLE IF EXISTS PROJECT;
DROP TABLE IF EXISTS REFERENCE;
DROP TABLE IF EXISTS ATTACHMENT;
DROP TABLE IF EXISTS USERS;

create table PROJECT
(
    ID          bigserial primary key,
    CODE        varchar(32)   not null
        constraint UK_PROJECT_CODE unique,
    TITLE       varchar(1024) not null,
    DESCRIPTION varchar(4096) not null,
    TYPE_CODE   varchar(32)   not null,
    STARTPOINT  timestamp,
    ENDPOINT    timestamp,
    PARENT_ID   bigint,
    constraint FK_PROJECT_PARENT foreign key (PARENT_ID) references PROJECT (ID) on delete cascade
);

create table MAIL_CASE
(
    ID        bigserial primary key,
    EMAIL     varchar(255) not null,
    NAME      varchar(255) not null,
    DATE_TIME timestamp    not null,
    RESULT    varchar(255) not null,
    TEMPLATE  varchar(255) not null
);

create table SPRINT
(
    ID          bigserial primary key,
    STATUS_CODE varchar(32)   not null,
    STARTPOINT  timestamp,
    ENDPOINT    timestamp,
    TITLE       varchar(1024) not null,
    PROJECT_ID  bigint        not null,
    constraint FK_SPRINT_PROJECT foreign key (PROJECT_ID) references PROJECT (ID) on delete cascade
);

create table REFERENCE
(
    ID         bigserial primary key,
    CODE       varchar(32)   not null,
    REF_TYPE   smallint      not null,
    ENDPOINT   timestamp,
    STARTPOINT timestamp,
    TITLE      varchar(1024) not null,
    AUX        varchar,
    constraint UK_REFERENCE_REF_TYPE_CODE unique (REF_TYPE, CODE)
);

create table USERS
(
    ID           bigserial primary key,
    DISPLAY_NAME varchar(32)  not null
        constraint UK_USERS_DISPLAY_NAME unique,
    EMAIL        varchar(128) not null
        constraint UK_USERS_EMAIL unique,
    FIRST_NAME   varchar(32)  not null,
    LAST_NAME    varchar(32),
    PASSWORD     varchar(128) not null,
    ENDPOINT     timestamp,
    STARTPOINT   timestamp
);

create table PROFILE
(
    ID                 bigint primary key,
    LAST_LOGIN         timestamp,
    LAST_FAILED_LOGIN  timestamp,
    MAIL_NOTIFICATIONS bigint,
    constraint FK_PROFILE_USERS foreign key (ID) references USERS (ID) on delete cascade
);

create table CONTACT
(
    ID    bigint       not null,
    CODE  varchar(32)  not null,
    VALUE varchar(256) not null,
    primary key (ID, CODE),
    constraint FK_CONTACT_PROFILE foreign key (ID) references PROFILE (ID) on delete cascade
);

create table TASK
(
    ID            bigserial primary key,
    TITLE         varchar(1024) not null,
    DESCRIPTION   varchar(4096) not null,
    TYPE_CODE     varchar(32)   not null,
    STATUS_CODE   varchar(32)   not null,
    PRIORITY_CODE varchar(32)   not null,
    ESTIMATE      integer,
    UPDATED       timestamp,
    PROJECT_ID    bigint,
    SPRINT_ID     bigint,
    PARENT_ID     bigint,
    STARTPOINT    timestamp,
    ENDPOINT      timestamp,
    constraint FK_TASK_SPRINT foreign key (SPRINT_ID) references SPRINT (ID) on delete set null,
    constraint FK_TASK_PROJECT foreign key (PROJECT_ID) references PROJECT (ID) on delete cascade,
    constraint FK_TASK_PARENT_TASK foreign key (PARENT_ID) references TASK (ID) on delete cascade
);

create table ACTIVITY
(
    ID            bigserial primary key,
    AUTHOR_ID     bigint not null,
    TASK_ID       bigint not null,
    UPDATED       timestamp,
    COMMENT       varchar(4096),
--     history of task field change
    TITLE         varchar(1024),
    DESCRIPTION   varchar(4096),
    ESTIMATE      integer,
    TYPE_CODE     varchar(32),
    STATUS_CODE   varchar(32),
    PRIORITY_CODE varchar(32),
    constraint FK_ACTIVITY_USERS foreign key (AUTHOR_ID) references USERS (ID),
    constraint FK_ACTIVITY_TASK foreign key (TASK_ID) references TASK (ID) on delete cascade
);

create table TASK_TAG
(
    TASK_ID bigint      not null,
    TAG     varchar(32) not null,
    constraint UK_TASK_TAG unique (TASK_ID, TAG),
    constraint FK_TASK_TAG foreign key (TASK_ID) references TASK (ID) on delete cascade
);

create table USER_BELONG
(
    ID             bigserial primary key,
    OBJECT_ID      bigint      not null,
    OBJECT_TYPE    smallint    not null,
    USER_ID        bigint      not null,
    USER_TYPE_CODE varchar(32) not null,
    STARTPOINT     timestamp,
    ENDPOINT       timestamp,
    constraint FK_USER_BELONG foreign key (USER_ID) references USERS (ID)
);
create unique index UK_USER_BELONG on USER_BELONG (OBJECT_ID, OBJECT_TYPE, USER_ID, USER_TYPE_CODE);
create index IX_USER_BELONG_USER_ID on USER_BELONG (USER_ID);

create table ATTACHMENT
(
    ID          bigserial primary key,
    NAME        varchar(128) not null,
    FILE_LINK   varchar(2048) not null,
    OBJECT_ID   bigint        not null,
    OBJECT_TYPE smallint      not null,
    USER_ID     bigint        not null,
    DATE_TIME   timestamp,
    constraint FK_ATTACHMENT foreign key (USER_ID) references USERS (ID)
);

create table USER_ROLE
(
    USER_ID bigint   not null,
    ROLE    smallint not null,
    constraint UK_USER_ROLE unique (USER_ID, ROLE),
    constraint FK_USER_ROLE foreign key (USER_ID) references USERS (ID) on delete cascade
);

CREATE TABLE token
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    token      VARCHAR(255),
    token_type VARCHAR(255),
    revoked    BOOLEAN                                 NOT NULL,
    expired    BOOLEAN                                 NOT NULL,
    user_id    BIGINT,
    STARTPOINT     timestamp,
    ENDPOINT       timestamp,
    CONSTRAINT pk_token PRIMARY KEY (id)
);

ALTER TABLE token
    ADD CONSTRAINT uc_token_token UNIQUE (token);

ALTER TABLE token
    ADD CONSTRAINT FK_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);


--changeset kmpk:populate_data

insert into USERS (EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, DISPLAY_NAME)
values ('user@gmail.com', '{noop}password', 'userFirstName', 'userLastName', 'userDisplayName'),
       ('admin@gmail.com', '{noop}admin', 'adminFirstName', 'adminLastName', 'adminDisplayName'),
       ('guest@gmail.com', '{noop}guest', 'guestFirstName', 'guestLastName', 'guestDisplayName');

-- 0 DEV
-- 1 ADMIN
insert into USER_ROLE (ROLE, USER_ID)
values (0, 1),
       (1, 2),
       (0, 2);


--============ References =================
insert into REFERENCE (CODE, TITLE, REF_TYPE)
-- TASK
values ('task', 'Task', 2),
       ('story', 'Story', 2),
       ('bug', 'Bug', 2),
       ('epic', 'Epic', 2),
-- TASK_STATUS
       ('icebox', 'Icebox', 3),
       ('backlog', 'Backlog', 3),
       ('ready', 'Ready', 3),
       ('in progress', 'In progress', 3),
       ('done', 'Done', 3),
-- SPRINT_STATUS
       ('planning', 'Planning', 4),
       ('implementation', 'Implementation', 4),
       ('review', 'Review', 4),
       ('retrospective', 'Retrospective', 4),
-- USER_TYPE
       ('admin', 'Admin', 5),
       ('user', 'User', 5),
-- PROJECT
       ('scrum', 'Scrum', 1),
       ('task tracker', 'Task tracker', 1),
-- CONTACT
       ('skype', 'Skype', 0),
       ('tg', 'Telegram', 0),
       ('mobile', 'Mobile', 0),
       ('phone', 'Phone', 0),
       ('website', 'Website', 0),
       ('vk', 'VK', 0),
       ('linkedin', 'LinkedIn', 0),
       ('github', 'GitHub', 0),
-- PRIORITY
       ('critical', 'Critical', 7),
       ('high', 'High', 7),
       ('normal', 'Normal', 7),
       ('low', 'Low', 7),
       ('neutral', 'Neutral', 7);

insert into REFERENCE (CODE, TITLE, REF_TYPE, AUX)
-- MAIL_NOTIFICATION
values ('assigned', 'Assigned', 6, '1'),
       ('three_days_before_deadline', 'Three days before deadline', 6, '2'),
       ('two_days_before_deadline', 'Two days before deadline', 6, '4'),
       ('one_day_before_deadline', 'One day before deadline', 6, '8'),
       ('deadline', 'Deadline', 6, '16'),
       ('overdue', 'Overdue', 6, '32');

insert into PROFILE (ID, LAST_FAILED_LOGIN, LAST_LOGIN, MAIL_NOTIFICATIONS)
values (1, null, null, 49),
       (2, null, null, 14);

insert into CONTACT (ID, CODE, VALUE)
values (1, 'skype', 'userSkype'),
       (1, 'mobile', '+01234567890'),
       (1, 'website', 'user.com'),
       (2, 'github', 'adminGitHub'),
       (2, 'tg', 'adminTg'),
       (2, 'vk', 'adminVk');

--changeset kriffer:add_dashboard

INSERT INTO project (id, code, title, description, type_code, startpoint, endpoint, parent_id) VALUES (2, 'task tracker', 'PROJECT-1', 'test project', 'task tracker', null, null, null);

INSERT INTO sprint (id, status_code, startpoint, endpoint, title, project_id) VALUES
(1, 'planning', '2023-04-09 23:05:05.000000', '2023-04-12 23:05:12.000000', 'Sprint-1', 2)
                                                                                   ,(2, 'review', '2023-04-23 23:05:05.000000', '2023-05-12 23:05:12.000000', 'Sprint-2', 2);

INSERT INTO task (title, description, type_code, status_code, priority_code, estimate, updated, project_id, sprint_id, parent_id, startpoint, endpoint) VALUES
( 'Оперативная гибридный интерфейс', 'Cumque asperiores officia nobis distinctio explicabo quis velit. Molestiae odit facilis odit maxime dolore voluptas omnis. Excepturi alias incidunt eos tempora ipsam corrupti.', 'task', 'in progress', 'high', 1, null, 2, 1, null, '2023-04-23 12:52:08.000000', '2023-04-23 12:39:13.761882')
,( 'Запрашиваемого отказоустойчивый подходить', 'Eum quis assumenda quam perferendis. Neque et velit fuga magnam vel. Veniam id molestiae dolore modi tempora iusto ratione odit.', 'bug', 'ready', 'normal', 1, null, 2, 1, null, '2023-04-23 12:00:08.000000', '2023-04-23 12:52:08.000000')
,( 'Горизонтальной материальный синергия', 'Nesciunt ea amet numquam et deleniti ullam accusantium. Dignissimos debitis qui non quisquam omnis quo.', 'bug', 'in progress', 'normal', 1, null, 2, 1, null, '2023-04-23 12:52:08.000000', null)
,( 'Обратная национальный решение', 'Aut ab eum dolor ut quod. Veritatis eum deserunt enim minus rem. Nesciunt eveniet voluptatum impedit nulla qui possimus. Consequuntur assumenda quia enim dolorem voluptas laboriosam.', 'task', 'done', 'low', 1, null, 2, 1, null, '2023-04-23 12:52:08.000000', null)
,( 'Ключ системный иерархия', 'Nesciunt sunt optio magnam ea aperiam adipisci. Aut temporibus nostrum ut. Ut quam natus dolorem illo aut ut. Architecto autem autem accusantium nam et.', 'task', 'done', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Клонировать охватывающий парадигмы', 'Distinctio perferendis et ratione. Rerum porro adipisci natus atque. Consectetur aliquam dolorem quas molestiae quisquam facere dolorem enim.', 'task', 'in progress', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', '2023-04-23 12:52:08.000000')
,( 'Низовые однородный емкость', 'Aut sint dolore reiciendis qui non omnis reprehenderit. Vel quae non id tenetur praesentium fugit est enim. Nam libero delectus quae deleniti amet.', 'task', 'ready', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Сеть выделенный хранилищ данных', 'Quasi consequatur quisquam odio. Rem minus eum in numquam eligendi est labore cum. Est aliquam quidem debitis quis optio dolores libero repellat. Est quo vel non aut.', 'task', 'done', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Предприятия фон определение', 'Minus sed ea explicabo. Voluptatem molestiae et vero et ut commodi blanditiis. Id eveniet voluptas laudantium culpa consequatur neque fugit quod.', 'task', 'reaty', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Повторно контекст человеческий ресурс синергия', 'Qui maiores et voluptatibus et neque. Dolor labore ipsa corrupti id voluptatem. Eligendi qui quis veritatis magni rerum alias et. Ratione maiores veniam quia harum molestias.', 'task', 'done', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', '2023-04-23 12:52:08.000000')
,( 'Инициативный основанныйнапотребностях экстранет', 'Aut distinctio eos nam voluptas quos dolores nulla et. Sit atque quas rerum in consequatur.', 'task', 'in progress', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Целенаправленный гибридный инициатива', 'Veniam dolor maxime exercitationem doloribus. Sed et nobis est sit aut. Velit laborum laborum voluptatem sed.', 'task', 'ready', 'normal', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', '2023-04-23 12:52:08.000000')
,( 'Переключение ориентированный на пользователя база данных', 'Odio iste omnis atque qui maiores. Nesciunt dolore quam soluta veritatis. Atque repudiandae sit reprehenderit vitae magni maxime.', 'task', 'done', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Универсальный вторичный стандартизации', 'Aut natus voluptas blanditiis ea et maiores. Nesciunt esse sint soluta. Voluptatem ipsum ad ipsa et excepturi et dolorum. Dolorem ad sit omnis pariatur nulla.', 'task', 'in progress', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Программируемый эффективный основы', 'Ut voluptas ea amet dignissimos accusamus modi a voluptate. Vel consequatur labore officiis corrupti iste asperiores. Minus similique veniam sit aut nihil nulla.', 'task', 'ready', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Дополнительно следующее поколение согласование', 'Ab veritatis est doloremque ut perspiciatis. Non et ut quo dolor. Porro repellat sint similique atque blanditiis quia qui. Consectetur voluptatem et et voluptatum ipsum nobis.', 'task', 'done', 'high', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Функции 24/7 программное обеспечение', 'Voluptatem maxime sequi modi maxime facere et. Deleniti perspiciatis porro odio dolor dolor. Et est aliquid magnam.', 'task', 'in progress', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Обязательно мониторинг пропускной способности архив', 'Itaque itaque asperiores et aspernatur et eaque. Ad dolorem omnis deserunt aut pariatur labore suscipit.', 'task', 'ready', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Децентрализации включенный в сетку иерархия', 'Omnis et consectetur earum molestiae voluptate facilis et. Vel odit quia voluptatem quo similique. Dolore tempora qui est totam alias repellendus.', 'task', 'done', 'low', 1, null, 2, null, null, '2023-04-23 12:52:08.000000', null)
,( 'Снижается хорошо модулированный методика', 'Voluptatem quia maxime tempore sint. Odit aut facilis expedita aperiam non voluptatum eius. Ut rem sit id amet possimus. Corrupti necessitatibus velit consequuntur ducimus illo corporis.', 'task', 'in progress', 'low', 1, null, 2, 2, null, '2023-04-23 12:52:08.000000', null)
,( 'Удобный нейтральный число', 'Omnis est enim facilis quia illo. Quaerat nulla asperiores ut facilis vel perferendis. Placeat cum aspernatur earum ipsam molestiae.', 'task', 'ready', 'low', 1, null, 2, 2, null, '2023-04-23 12:52:08.000000', '2023-04-24 12:52:08.000000')
,( 'Реорганизации 3-го поколения инициатива', 'Quo cum in porro suscipit sit voluptate. Itaque dolore enim voluptatem temporibus vel sit ullam. Sit laudantium fugiat voluptatem voluptatem sunt sit. Perferendis at non quis et.', 'task', 'done', 'low', 1, null, 2, 2, null, '2023-04-23 12:52:08.000000', null);

insert into TASK_TAG (TASK_ID, TAG) values
(2,'OneTag')
,(2,'TwoTag')
,(16,'discription')
,(7,'aliquid')
,(7,'animi')
,(8,'incidunt')
,(4,'quos')
,(3,'eos')
,(9,'distinctio')
,(11,'eveniet')
,(6,'et')
,(21,'sed')
,(8,'laudantium')
,(19,'earum')
,(20,'in')
,(11,'sint')
,(7,'possimus')
,(18,'ettt')
,(13,'dolores')
,(9,'esse')
,(5,'cum')
,(19,'quasi');

INSERT INTO user_belong (id, object_id, object_type, user_id, user_type_code, startpoint, endpoint) VALUES (3, 2, 2, 2, 'admin', null, null);
INSERT INTO user_belong (id, object_id, object_type, user_id, user_type_code, startpoint, endpoint) VALUES (4, 3, 2, 2, 'admin', null, null);
INSERT INTO user_belong (id, object_id, object_type, user_id, user_type_code, startpoint, endpoint) VALUES (5, 4, 2, 2, 'admin', null, null);

INSERT INTO user_belong (id, object_id, object_type, user_id, user_type_code, startpoint, endpoint) VALUES (6, 5, 2, 2, 'admin', null, null);

INSERT INTO activity (id, author_id, task_id, updated, comment, title, description, estimate, type_code, status_code, priority_code) VALUES (1, 2, 1, '2023-04-27 12:14:17.474668', null, 'Оперативная гибридный интерфейс', null, 1, 'task', 'backlog', 'high');
INSERT INTO activity (id, author_id, task_id, updated, comment, title, description, estimate, type_code, status_code, priority_code) VALUES (2, 2, 1, '2023-04-27 12:15:31.433669', null, 'Оперативная гибридный интерфейс', null, 1, 'task', 'icebox', 'high');
INSERT INTO activity (id, author_id, task_id, updated, comment, title, description, estimate, type_code, status_code, priority_code) VALUES (3, 2, 22, '2023-04-27 12:56:25.762251', null, 'Реорганизации 3-го поколения инициатива', 'Quo cum in porro suscipit sit voluptate. Itaque dolore enim voluptatem temporibus vel sit ullam. Sit laudantium fugiat voluptatem voluptatem sunt sit. Perferendis at non quis et.', 1, 'task', 'backlog', 'low');
