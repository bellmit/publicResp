-- liquibase formatted sql
-- changeset raeshmika:<diagnostics-test-equipment-master-mapping>

CREATE SEQUENCE diagnostics_test_equipment_master_mapping_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE TABLE diagnostics_test_equipment_master_mapping (
    diagnostics_test_equipment_master_mapping_id INTEGER DEFAULT nextval('diagnostics_test_equipment_master_mapping_seq'::regclass) NOT NULL,
    test_id VARCHAR(50) NOT NULL,
    eq_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY(diagnostics_test_equipment_master_mapping_id),
    UNIQUE(test_id, eq_id)
);

COMMENT ON TABLE diagnostics_test_equipment_master_mapping is '{"type": "Txn", "comment": "Holds test and test resource mapped data" }';
COMMENT ON SEQUENCE diagnostics_test_equipment_master_mapping_seq is '{"type": "Txn", "comment" : "Holds test and test resource mapped sequence"}';
