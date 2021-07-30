-- liquibase formatted sql
-- changeset harishm18:create-hospital-role-order-control

CREATE SEQUENCE hospital_roles_order_controls_seq
START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE hospital_roles_order_controls (
    hospital_role_control_id integer DEFAULT nextval('hospital_roles_order_controls_seq'::regclass) NOT NULL,
    role_id integer NOT NULL,
    service_group_id integer DEFAULT -9,
    service_sub_group_id integer DEFAULT -9,
    item_id character varying(50) DEFAULT '*',
    entity character varying(50) DEFAULT '*',
    PRIMARY KEY (hospital_role_control_id),
    FOREIGN KEY (role_id) REFERENCES hospital_roles_master(hosp_role_id),
    UNIQUE (role_id, service_group_id, service_sub_group_id,item_id, entity)
);

COMMENT ON table hospital_roles_order_controls IS '{ "type": "Master", "comment": "Hospital role order controls table" }';

COMMENT ON sequence hospital_roles_order_controls_seq IS '{ "type": "Master", "comment": "Hospital role order controls sequence" }';
