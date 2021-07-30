-- liquibase formatted sql
-- changeset tejakilaru:inserting-data-into-code_system_categories

INSERT INTO code_system_categories(id, label) VALUES 
(1, 'gender'), 
(2, 'religion'), 
(3, 'marital_status'), 
(4, 'department'), 
(5, 'discharge_type'),
(6, 'country_master');

insert into events_hl7 (id,event) values 
(1,'IP_New_Patient'),
(2,'IP_Existing_Patient'),
(3,'Physical_Discharge'),
(4,'OP_New_Patient'),
(5,'OP_Existing_Patient'),
(6,'Pre_Registration'),
(7,'OP_IP_Conversion'),
(8,'Edit_Patient_Details'),
(9,'Edit_Visit_Details'),
(10,'ReAdmit_OP_Patient'),
(11,'Merge_Patient'),
(12,'ReAdmit_IP_Patient');