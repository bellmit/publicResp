-- 1_IP_New_Patient (ADT_A28, ADT_01)
INSERT INTO message_mapping_hl7 VALUES (1,'ADTA28251',1,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (1,'ADTA01251',2,0,1,1,'A');

-- 2_IP_Existing_Patient (ADT_A01)
INSERT INTO message_mapping_hl7 VALUES (2,'ADTA01251',1,0,1,1,'A');

-- 3_Physical_Discharge (ADT_A03)
INSERT INTO message_mapping_hl7 VALUES (3,'ADTA03251',1,0,1,1,'A');

-- 4_OP_New_Patient (ADT_A28, ADT_A04)
INSERT INTO message_mapping_hl7 VALUES (4,'ADTA28251',1,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (4,'ADTA04251',2,0,1,1,'A');

-- 5_OP_Existing_Patient (ADT_A04)
INSERT INTO message_mapping_hl7 VALUES (5,'ADTA04251',1,0,1,1,'A');

-- 6_Pre_Registration (ADT_A28)
INSERT INTO message_mapping_hl7 VALUES (6,'ADTA28251',1,0,1,1,'A');

-- 7_OP_IP_Conversion (ADT_A06)
INSERT INTO message_mapping_hl7 VALUES (7,'ADTA06251',1,0,1,1,'A');

-- 8_Edit_Patient_Details (ADT_A31)
INSERT INTO message_mapping_hl7 VALUES (8,'ADTA31251',1,0,1,1,'A');

-- 9_Edit_Visit_Details (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (9,'ADTA08251',1,0,1,1,'A');

-- 10_ReAdmit_OP_Patient (ADT_A13)
INSERT INTO message_mapping_hl7 VALUES (10,'ADTA13251',1,0,1,1,'A');

-- 11_Merge_Patient (ADT_A40)
INSERT INTO message_mapping_hl7 VALUES (11,'ADTA40251',1,0,1,1,'A');

-- 12_ReAdmit_IP_Patient (ADT_A13)
INSERT INTO message_mapping_hl7 VALUES (12,'ADTA13251',1,0,1,1,'A');

-- 13_Patient_Problem_OP (PPR_PC1, PPR_PC2, PPR_PC3)
INSERT INTO message_mapping_hl7 VALUES (13,'PPRPC1251',1,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (13,'PPRPC2251',2,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (13,'PPRPC3251',3,0,1,1,'A');

-- 14_Patient_Problem_IP (PPR_PC1, PPR_PC2, PPR_PC3)
INSERT INTO message_mapping_hl7 VALUES (14,'PPRPC1251',1,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (14,'PPRPC2251',2,0,1,1,'A');
INSERT INTO message_mapping_hl7 VALUES (14,'PPRPC3251',3,0,1,1,'A');

-- 15_Diagnosis_OP (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (15,'ADTA08251',1,0,1,1,'A');

-- 16_Allergies_OP (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (16,'ADTA08251',1,0,1,1,'A');

-- 17_Edit_Insurance_Details (ADT_A31)
INSERT INTO message_mapping_hl7 VALUES (17,'ADTA31251',1,0,1,1,'A');

-- 18_Visit_Close (ADT_A03)
INSERT INTO message_mapping_hl7 VALUES (18,'ADTA03251',1,0,1,1,'A');

-- 19_Diagnosis_IP (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (19,'ADTA08251',1,0,1,1,'A');

-- 20_Allergies_IP (ADT_A31)
INSERT INTO message_mapping_hl7 VALUES (20,'ADTA31251',1,0,1,1,'I');

-- 21_Diagnosis_Inactive_Visit (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (21,'ADTA08251',1,0,1,1,'A');

-- 22_Allergies_Inactive_Visit (ADT_A31)
INSERT INTO message_mapping_hl7 VALUES (22,'ADTA31251',1,0,1,1,'I');

-- 23_Visit_Close_When_Diagnosis_Not_Available (ADT_A11)
INSERT INTO message_mapping_hl7 VALUES (23,'ADTA11251',1,0,1,1,'A');

-- 24_Physical_Discharge_When_Diagnosis_Not_Available (ADT_A11)
INSERT INTO message_mapping_hl7 VALUES (24,'ADTA11251',1,0,1,1,'A');

-- 25_Surgery (ADT_A08)
INSERT INTO message_mapping_hl7 VALUES (25,'ADTA08251',1,0,1,1,'A');

-- 26_Medicine_Presc_Insert (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (26,'OMPO09251',1,0,1,1,'A');

-- 27_Medicine_Presc_Update (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (27,'OMPO09251',1,0,1,1,'A');

-- 28_Medicine_Presc_Delete (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (28,'OMPO09251',1,0,1,1,'A');

-- 29_Medicine_Dispense (RDS_O13)
INSERT INTO message_mapping_hl7 VALUES (29,'RDSO13251',1,0,1,1,'A');

-- 30_Test_SignedOff (ORU_R01)
INSERT INTO message_mapping_hl7 VALUES (30,'ORUR01251',1,0,1,1,'A');

-- 31_Test_Amend (ORU_R01)
INSERT INTO message_mapping_hl7 VALUES (31,'ORUR01251',1,0,1,1,'A');

-- 32_Discharge_Medication_Insert (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (32,'OMPO09251',1,0,1,1,'A');

-- 33_Discharge_Medication_Update (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (33,'OMPO09251',1,0,1,1,'A');

-- 34_Discharge_Medication_Delete (OMP_O09)
INSERT INTO message_mapping_hl7 VALUES (34,'OMPO09251',1,0,1,1,'A');

-- 35_Medicine_Dispense_Discharge_Med (RDS_O13)
INSERT INTO message_mapping_hl7 VALUES (35,'RDSO13251',1,0,1,1,'A');