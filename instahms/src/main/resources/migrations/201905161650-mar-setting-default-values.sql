-- liquibase formatted sql
-- changeset tejakilaru:infusionsite-and-activityremarks-default-master-values

INSERT INTO iv_infusionsites(site_name, username) VALUES
('Left Arm', 'InstaAdmin'),
('Right Arm', 'InstaAdmin'),
('Left Hand', 'InstaAdmin'),
('Right Hand', 'InstaAdmin'),
('Left Leg', 'InstaAdmin'),
('Right Leg', 'InstaAdmin'),
('Left Feet', 'InstaAdmin'),
('Right Feet', 'InstaAdmin'),
('Neck', 'InstaAdmin'),
('Chest', 'InstaAdmin'),
('Groin', 'InstaAdmin');

INSERT INTO medication_serving_remarks(remark_name, username) VALUES
('Patient Discharged', 'InstaAdmin'),
('Patient Order to be Discontinued', 'InstaAdmin'),
('No Intravenous Access', 'InstaAdmin'),
('Patient Nauseated', 'InstaAdmin'),
('Patient On NPO', 'InstaAdmin'),
('Patient in Procedure', 'InstaAdmin'),
('Drug Not Available', 'InstaAdmin'),
('Patient Refused', 'InstaAdmin'),
('Patient Family refused', 'InstaAdmin'),
('Patient Complained of Adverse Reaction', 'InstaAdmin'),
('Scheduling Issues with MAR', 'InstaAdmin'),
('Serum Concentration too high', 'InstaAdmin'),
('Awaiting Diagnostic Results', 'InstaAdmin');
