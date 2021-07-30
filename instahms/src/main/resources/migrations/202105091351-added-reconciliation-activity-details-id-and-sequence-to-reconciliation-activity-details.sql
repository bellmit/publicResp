-- liquibase formatted sql
-- changeset rajrajeshwarsinghrathore:added-reconciliation-activity-details-id-and-sequence-to-reconciliation-activity-details-jira-HMS-38554

CREATE SEQUENCE reconciliation_activity_details_id_sequence START 1;

COMMENT ON SEQUENCE reconciliation_activity_details_id_sequence IS '{ "type": "Txn", "comment": "Holds sequence for reconciliation_activity_details" }';

ALTER TABLE ONLY reconciliation_activity_details
    ADD reconciliation_activity_details_id BIGINT DEFAULT nextval('reconciliation_activity_details_id_sequence'::regclass) NOT NULL
        CONSTRAINT reconciliation_activity_details_pkey PRIMARY KEY
