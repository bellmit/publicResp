-- liquibase formatted sql
-- changeset sanjana:center-default-timings

CREATE SEQUENCE center_availability_seq 
	START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE center_availability (
	center_availability_id integer primary key default nextval('center_availability_seq'),
	center_id integer not null,
	day_of_week integer not null,
	from_time time without time zone not null,
	to_time time without time zone not null,
	availability_status varchar(1) not null,
	CONSTRAINT chk_status CHECK (availability_status IN ('Y','N')),
	UNIQUE(center_id,day_of_week)
);


INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,0,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,1,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,2,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,3,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,4,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,5,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');
INSERT INTO center_availability (center_id,day_of_week,from_time,to_time,availability_status)
VALUES(0,6,'00:00'::time without time zone ,'23:59'::time without time zone,'Y');

COMMENT ON table center_availability is '{ "type": "Master", "comment": "Store day wise availability timings for center where day_of_week starts with sunday shown as 0" }';

COMMENT ON sequence center_availability_seq is '{ "type": "Txn", "comment": "center availability sequence" }';
