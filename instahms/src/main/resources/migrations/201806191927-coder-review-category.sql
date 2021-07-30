-- liquibase formatted sql
-- changeset AdeshAtole:coder-review-changes


create sequence codification_message_category_seq;
create table codification_message_category (
    category_id integer primary key default nextval('codification_message_category_seq'),
    category_name character varying(50),
    category_type character varying(3),
    send_email character varying(2),
    status character(1),
    created_by character varying(50),
    created_at timestamp without time zone default now(),
    modified_by character varying(50),
    modified_at timestamp without time zone
);

create sequence category_role_seq;
create table category_role (
    id integer primary key default nextval('category_role_seq'),
    category_id integer references codification_message_category(category_id),
    role_id integer references u_role(role_id),
    created_by character varying(50),
    created_at timestamp without time zone default now(),
    modified_by character varying(50),
    modified_at timestamp without time zone,
    unique (category_id, role_id)
);



ALTER TABLE IF EXISTS codification_message_types ADD COLUMN message_category_id  integer references codification_message_category(category_id);
ALTER TABLE IF EXISTS codification_message_types ADD COLUMN tat  integer;
ALTER TABLE IF EXISTS codification_message_types ADD COLUMN created_by character varying(50);
ALTER TABLE IF EXISTS codification_message_types add column created_at timestamp without time zone default now();
ALTER TABLE IF EXISTS codification_message_types ADD COLUMN modified_by character varying(50);
ALTER TABLE IF EXISTS codification_message_types ADD COLUMN modified_at timestamp without time zone;

ALTER TABLE IF EXISTS codification_message_types ADD COLUMN status character(1) DEFAULT 'A';

ALTER TABLE IF EXISTS codification_message_types RENAME id TO message_type_id;
CREATE SEQUENCE review_type_center_role_seq;
CREATE TABLE IF NOT EXISTS review_type_center_role (
    id integer default nextval('review_type_center_role_seq') primary key,
    message_type_id integer not null references codification_message_types (message_type_id),
    role_id integer not null references u_role(role_id),
    center_id integer not null references hospital_center_master(center_id),
    created_by character varying(50),
    created_at timestamp without time zone default now(),
    modified_by character varying(50),
    modified_at timestamp without time zone,
    unique (message_type_id,center_id)
);


INSERT INTO saved_searches values('Review Types',  nextval('saved_searches_seq'), 'System', 'Active Review Types', true,
'status=A', 'InstaAdmin', 'InstaAdmin', now(), now());
INSERT INTO saved_searches values('Review Types',  nextval('saved_searches_seq'), 'System', 'Inactive Review Types', false,
'status=I', 'InstaAdmin', 'InstaAdmin', now(), now());
INSERT INTO saved_searches values('Review Category',  nextval('saved_searches_seq'), 'System', 'Active Review Category', true,
'status=A', 'InstaAdmin', 'InstaAdmin', now(), now());
INSERT INTO saved_searches values('Review Category',  nextval('saved_searches_seq'), 'System', 'Inactive Review Category', false,
'status=I', 'InstaAdmin', 'InstaAdmin', now(), now());