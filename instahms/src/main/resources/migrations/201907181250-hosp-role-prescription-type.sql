-- liquibase formatted sql
-- changeset sandeep:hospital-role-prescription-type-mapping

CREATE TABLE hospital_role_prescription_types (
    hosp_role_id INTEGER NOT NULL,
    prescription_type VARCHAR(10) NOT NULL,
    PRIMARY KEY(hosp_role_id, prescription_type)
);
COMMENT ON TABLE hospital_role_prescription_types IS '{ "type": "Txn", "comment": "Holds the default prescription types to search for the hospital role" }';
