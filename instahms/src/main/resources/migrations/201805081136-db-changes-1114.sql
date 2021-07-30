-- liquibase formatted sql

-- changeset irshad.mohammed:db-changes-1114.sql

CREATE SEQUENCE order_kit_details_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;
     
ALTER TABLE order_kit_details ADD COLUMN order_kit_details_id integer DEFAULT nextval('order_kit_details_seq') NOT NULL;