-- liquibase formatted sql
-- changeset anandpatel:create-user_center_billing_counters-table-and-migration-of-billing-counters

CREATE SEQUENCE user_center_billing_counters_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE user_center_billing_counters (
    center_counter_id integer DEFAULT nextval('user_center_billing_counters_seq'::regclass),
    emp_username character varying(30) NOT NULL REFERENCES u_user(emp_username),
    center_id integer NOT NULL REFERENCES hospital_center_master(center_id),
	counter_id character varying(20) NOT NULL REFERENCES counters(counter_id),
	default_counter BOOLEAN default false,
	created_by character varying(30) NOT NULL REFERENCES u_user(emp_username),
	created_at timestamp without time zone DEFAULT now() NOT NULL,
	updated_by character varying(30),
	updated_at timestamp without time zone,
	PRIMARY KEY (emp_username,center_id,counter_id),
	UNIQUE(center_counter_id)
);


COMMENT ON SEQUENCE user_center_billing_counters_seq IS
	'{ "type": "Master", "comment": "Sequence ID of the user_center_billing_counters table for generating pkey" }';

COMMENT ON table user_center_billing_counters is '{ "type": "Master", "comment": "Table for user center billing counter mapping" }';


insert into user_center_billing_counters (emp_username,center_id,counter_id,default_counter,created_by,created_at) 
select emp_username,c.center_id,u.counter_id,true,'auto_update',created_timestamp 
from u_user u
JOIN counters c on (c.counter_id = u.counter_id)
where u.counter_id !='' order by emp_username;
