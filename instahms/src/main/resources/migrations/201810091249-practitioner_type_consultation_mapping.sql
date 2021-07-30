-- liquibase formatted sql
-- changeset utkarshjindal:creating-practitionerConsultationMapping

CREATE SEQUENCE practitioner_type_consultation_mapping_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE TABLE practitioner_type_consultation_mapping (
  practitioner_type_consultation_mapping_id INTEGER DEFAULT nextval('practitioner_type_consultation_mapping_seq'::regclass) NOT NULL,
  consultation_type_id integer,
  practitioner_type_id integer,
  created_at TIMESTAMP DEFAULT NOW(),
  modified_at TIMESTAMP,
  FOREIGN KEY(consultation_type_id) references consultation_types(consultation_type_id),
  FOREIGN KEY(practitioner_type_id) references practitioner_types(practitioner_id),
  PRIMARY KEY(practitioner_type_consultation_mapping_id),
  UNIQUE (consultation_type_id, practitioner_type_id)
);
