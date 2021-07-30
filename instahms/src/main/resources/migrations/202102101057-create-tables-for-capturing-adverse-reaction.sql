-- liquibase formatted sql
-- changeset dattuvs:table-for-capturing-adverse-reaction failOnError:false

CREATE SEQUENCE adverse_reaction_for_vaccination_seq START 1;
COMMENT ON SEQUENCE adverse_reaction_for_vaccination_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

CREATE TABLE adverse_reaction_for_vaccination (
  adverse_reaction_id INTEGER PRIMARY KEY,
  adverse_reaction_monitoring_for_id INTEGER,
  adverse_reaction_onset_id INTEGER,
  adverse_reaction_corelation_id INTEGER,
  adverse_reaction_actions_id INTEGER,
  adverse_start_date timestamp without time zone,
  adverse_end_date timestamp without time zone,
  adverse_remarks CHARACTER VARYING(5000),
  FOREIGN KEY(adverse_reaction_monitoring_for_id) REFERENCES adverse_reaction_monitoring_for(id),
  FOREIGN KEY(adverse_reaction_onset_id) REFERENCES adverse_reaction_onset(id),
  FOREIGN KEY(adverse_reaction_corelation_id) REFERENCES adverse_reaction_corelation(id),
  FOREIGN KEY(adverse_reaction_actions_id) REFERENCES adverse_reaction_actions(id)
);

COMMENT ON table adverse_reaction_for_vaccination IS '{ "type": "Txn", "comment": "To Capture Adverse Reaction for Vaccination if there are " }';

ALTER TABLE patient_vaccination ADD COLUMN adverse_reaction_id INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(adverse_reaction_id) REFERENCES adverse_reaction_for_vaccination(adverse_reaction_id);

CREATE SEQUENCE adverse_reaction_symptom_severity_mapping_seq START 1;
COMMENT ON SEQUENCE adverse_reaction_symptom_severity_mapping_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

CREATE TABLE adverse_reaction_symptom_severity_mapping(
  adverse_symptom_severity_id INTEGER PRIMARY KEY,
  adverse_reaction_for_vaccination_id INTEGER,
  severity_of_reaction_id INTEGER,
  number_of_occurrences INTEGER,
  adverse_reaction_symptoms_list_id INTEGER,
  FOREIGN KEY(adverse_reaction_for_vaccination_id) REFERENCES adverse_reaction_for_vaccination(adverse_reaction_id),
  FOREIGN kEY(adverse_reaction_symptoms_list_id) REFERENCES adverse_reaction_symptoms_list(id)
);

COMMENT ON table adverse_reaction_symptom_severity_mapping IS '{ "type": "Txn", "comment": "To Create a mapping of Symptoms and Severity" }';
