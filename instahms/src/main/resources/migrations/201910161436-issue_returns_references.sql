-- liquibase formatted sql
-- changeset sirisharl:<commit-message-describing-this-database-change>
ALTER TABLE store_issue_returns_details ADD PRIMARY KEY (item_return_no);

CREATE TABLE patient_issue_returns_issue_charge_details(
    patient_id character varying(15) REFERENCES patient_registration(patient_id),
    item_issue_no integer REFERENCES stock_issue_details(item_issue_no),
    issue_charge_id character varying(15) REFERENCES bill_charge(charge_id),
    item_return_no integer REFERENCES store_issue_returns_details(item_return_no),
    return_charge_id character varying(15) REFERENCES bill_charge(charge_id)
);
COMMENT ON TABLE patient_issue_returns_issue_charge_details IS 
'{ "type": "Txn", "comment": "Stores Original issues against which returns ahppened"}';
COMMENT ON COLUMN patient_issue_returns_issue_charge_details.issue_charge_id IS
'{ "type": "Txn", "comment": "Issue Charge id againest which return has happened"}';
COMMENT ON COLUMN patient_issue_returns_issue_charge_details.return_charge_id IS 
'{ "type": "Txn", "comment": "Issue returns Charge id"}';