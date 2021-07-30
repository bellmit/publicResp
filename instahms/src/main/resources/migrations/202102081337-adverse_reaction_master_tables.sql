-- liquibase formatted sql
-- changeset dattuvs:master-tables-for-adverse-reaction failOnError:false

-- To capture severity of vaccine like Mild, Moderate
CREATE TABLE severity_of_reaction (
  id SMALLSERIAL PRIMARY KEY,
  severity_name character varying(200) NOT NULL
);
COMMENT ON table severity_of_reaction IS '{ "type": "Master", "comment": "Severity of Reaction" }';
COMMENT ON SEQUENCE severity_of_reaction_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO severity_of_reaction ("severity_name") VALUES ('Mild');
INSERT INTO severity_of_reaction ("severity_name") VALUES ('Moderate');
INSERT INTO severity_of_reaction ("severity_name") VALUES ('Life Threatening');
INSERT INTO severity_of_reaction ("severity_name") VALUES ('Others');

-- To capture approx time of event
CREATE TABLE adverse_reaction_onset (
  id SMALLSERIAL PRIMARY KEY,
  on_set_desc character varying(200) NOT NULL
);
COMMENT ON table adverse_reaction_onset IS '{ "type": "Master", "comment": "When adverse reaction was Observed first" }';
COMMENT ON SEQUENCE adverse_reaction_onset_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO adverse_reaction_onset ("on_set_desc") VALUES ('Acute (Less than 30 Minutes)');
INSERT INTO adverse_reaction_onset ("on_set_desc") VALUES ('Sub-Acute (After 30 minutes with in a day)');
INSERT INTO adverse_reaction_onset ("on_set_desc") VALUES ('Latent (after a day)');

-- To Capture List of symptoms for vaccine
CREATE TABLE adverse_reaction_symptoms_list (
  id SMALLSERIAL PRIMARY KEY,
  symptom_name character varying(200) NOT NULL
);
COMMENT ON table adverse_reaction_symptoms_list IS '{ "type": "Master", "comment": "Symptoms for Adverse reaction for any vaccine" }';
COMMENT ON SEQUENCE adverse_reaction_symptoms_list_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Pain');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Cough');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Headache');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Swelling');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Fatigue, Asthenia');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Myalgia');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Dyspnea');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Constipation');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Redness');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Pruritus');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Nausea');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Diarrhea');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Rash');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Arthralgia');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Dysphagia');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Anorexia');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Acute Allergic Reaction');
INSERT INTO adverse_reaction_symptoms_list ("symptom_name") VALUES ('Other Symptoms');

-- To capture action taken on vaccine administered
CREATE TABLE adverse_reaction_actions (
  id SMALLSERIAL PRIMARY KEY,
  actions_desc character varying(200) NOT NULL
);
COMMENT ON table adverse_reaction_actions IS '{ "type": "Master", "comment": "What actions were taken on Adverse reaction" }';
COMMENT ON SEQUENCE adverse_reaction_actions_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO adverse_reaction_actions ("actions_desc") VALUES ('Inpatient Admission');
INSERT INTO adverse_reaction_actions ("actions_desc") VALUES ('Outpatient without medication');
INSERT INTO adverse_reaction_actions ("actions_desc") VALUES ('Prescription');
INSERT INTO adverse_reaction_actions ("actions_desc") VALUES ('Self-medication');

-- How likely vaccine and reaction related
CREATE TABLE adverse_reaction_corelation (
  id SMALLSERIAL PRIMARY KEY,
  corelation_desc character varying(200) NOT NULL
);
COMMENT ON table adverse_reaction_corelation IS '{ "type": "Master", "comment": "How adverse reaction is co-related" }';
COMMENT ON SEQUENCE adverse_reaction_corelation_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO adverse_reaction_corelation ("corelation_desc") VALUES ('Possibly related');
INSERT INTO adverse_reaction_corelation ("corelation_desc") VALUES ('Probably related');
INSERT INTO adverse_reaction_corelation ("corelation_desc") VALUES ('Definitely related');
INSERT INTO adverse_reaction_corelation ("corelation_desc") VALUES ('Unlikely related');
INSERT INTO adverse_reaction_corelation ("corelation_desc") VALUES ('Certainly Not related');

-- To capture reason for adverse reaction
CREATE TABLE adverse_reaction_monitoring_for (
  id SMALLSERIAL PRIMARY KEY,
  monitoring_for character varying(200) NOT NULL
);
COMMENT ON table adverse_reaction_monitoring_for IS '{ "type": "Master", "comment": "Monitoring for which Type of Vaccine" }';
COMMENT ON SEQUENCE adverse_reaction_monitoring_for_id_seq IS '{ "type": "Master", "comment": "Generate Auto Increment Id" }';

INSERT INTO adverse_reaction_monitoring_for ("monitoring_for") VALUES ('First Dose of New Medication');
INSERT INTO adverse_reaction_monitoring_for ("monitoring_for") VALUES ('Post-Operative Care');
INSERT INTO adverse_reaction_monitoring_for ("monitoring_for") VALUES ('Vaccination/Immunization Shot');
INSERT INTO adverse_reaction_monitoring_for ("monitoring_for") VALUES ('Electrolyte Balance');
