-- liquibase formatted sql
-- changeset janakivg:action-rights-for-vital-measurements

update url_action_rights set action_id='vital_measurements'  where action_id='vital_measurement';