--- Minimal set of rate masters needed for testing
DELETE FROM doctor_consultation_charge;
DELETE FROM doctor_op_consultation_charge;
DELETE FROM doctors;
insert into department values ('DEP0009','General Surgery','A');

CREATE SEQUENCE t1 START 1 ;
INSERT INTO doctors (doctor_id , doctor_name , dept_id, specialization , doctor_type , doctor_address , doctor_mobile ,  doctor_mail_id   , op_consultation_validity , status, ot_doctor_flag, consulting_doctor_flag, doctor_payments_in_percent, doctor_payment_operations, doctor_payment_ip, doctor_payment_op, payment_category) VALUES (('DOC' ||to_char(nextval('t1'),'FM0000')),'Dr. Senthilnathan','DEP0009','','CONSULTANT','','','','0','A','Y','Y','','0','0','0','1');
INSERT INTO doctor_op_consultation_charge (doctor_id , org_id  , op_charge , op_revisit_charge , private_cons_charge , private_cons_revisit_charge ) VALUES (('DOC' ||to_char(currval('t1'),'FM0000')),'ORG0001','10','0','0','0');
INSERT INTO doctor_consultation_charge (doctor_name, organization, bed_type, doctor_ip_charge, night_ip_charge, ot_charge, co_surgeon_charge, assnt_surgeon_charge ) VALUES (('DOC' ||to_char(currval('t1'),'FM0000')),'ORG0001','GENERAL','10','10', '1000','0','0' );
INSERT INTO doctor_consultation_charge (doctor_name, organization, bed_type, doctor_ip_charge, night_ip_charge, ot_charge, co_surgeon_charge, assnt_surgeon_charge ) VALUES (('DOC' ||to_char(currval('t1'),'FM0000')),'ORG0001','SEMI-PVT','20','20', '1500','0','0' );
INSERT INTO doctor_consultation_charge (doctor_name, organization, bed_type, doctor_ip_charge, night_ip_charge, ot_charge, co_surgeon_charge, assnt_surgeon_charge ) VALUES (('DOC' ||to_char(currval('t1'),'FM0000')),'ORG0001','PRIVATE','40','40', '2000','0','0' );
DROP SEQUENCE t1 ; 

DELETE FROM service_master_charges;
DELETE FROM service_org_details;
DELETE FROM services;
DELETE FROM services_departments;

CREATE SEQUENCE t1 START 1 ;
INSERT INTO services_departments VALUES ('GENERAL');
INSERT INTO services VALUES (('SERV' ||to_char(nextval('t1'),'FM0000')),'DRESSING (SILVEREX)','GENERAL','Once','0','GEN005','A');
INSERT INTO service_org_details VALUES (('SERV' ||to_char(currval('t1'),'FM0000')),'ORG0001','t',('SRV' ||to_char(currval('t1'),'FM0000')));
INSERT INTO service_master_charges VALUES (('SERV' ||to_char(currval('t1'),'FM0000')),'GENERAL','ORG0001','35');
INSERT INTO service_master_charges VALUES (('SERV' ||to_char(currval('t1'),'FM0000')),'SEMI-PVT','ORG0001','50');
INSERT INTO service_master_charges VALUES (('SERV' ||to_char(currval('t1'),'FM0000')),'PRIVATE','ORG0001','100');
DROP SEQUENCE t1 ; 


DELETE FROM operation_charges;
DELETE FROM operation_org_details ;
DELETE FROM operation_master;
CREATE SEQUENCE t1 START 1 ;
INSERT INTO operation_master(op_id,operation_name,dept_id,operation_code,status) VALUES(('OPID' ||to_char(nextval('t1'),'FM0000')),'APPENDICECTOMY  under  SA','DEP0009',('OPID' ||to_char(currval('t1'),'FM0000')),'A');
INSERT INTO operation_org_details(operation_id, org_id, applicable,item_code) VALUES(('OPID' ||to_char(currval('t1'),'FM0000')),'ORG0001','t',('OPID' ||to_char(currval('t1'),'FM0000')));
INSERT INTO operation_charges(op_id,org_id,bed_type,surgeon_charge,anesthetist_charge,surg_asstance_charge) VALUES(('OPID' ||to_char(currval('t1'),'FM0000')),'ORG0001','GENERAL','3600','2700','2700');
INSERT INTO operation_charges(op_id,org_id,bed_type,surgeon_charge,anesthetist_charge,surg_asstance_charge) VALUES(('OPID' ||to_char(currval('t1'),'FM0000')),'ORG0001','SEMI-PVT','4080','3060','3060');
INSERT INTO operation_charges(op_id,org_id,bed_type,surgeon_charge,anesthetist_charge,surg_asstance_charge) VALUES(('OPID' ||to_char(currval('t1'),'FM0000')),'ORG0001','PRIVATE','5760','4320','4320');
DROP SEQUENCE t1 ; 

DELETE FROM test_results_master ;
DELETE FROM test_template_master ;
DELETE FROM test_org_details ;
DELETE FROM diagnostic_charges ;
DELETE FROM diagnostics ; 

---miss this at your own peril
ALTER SEQUENCE resultlabel_seq RESTART WITH 1;

CREATE SEQUENCE t1 START 1 ;
INSERT INTO diagnostics (test_id ,test_name, ddept_id, sample_needed, house_status , type_of_specimen , diag_code , conduct_in_reportformat ) VALUES (('DGC' ||to_char(nextval('t1'),'FM0000')),'COMPLETE BLOOD COUNT','DDept0003','y','I','BLOOD',('DGC' ||to_char(currval('t1'),'FM0000')),'N');
INSERT INTO test_org_details (test_id ,org_id, applicable, item_code ) VALUES (('DGC' ||to_char(currval('t1'),'FM0000')),'ORG0001','t',('DGC' ||to_char(currval('t1'),'FM0000')));
INSERT INTO diagnostic_charges (test_id , org_name, charge, bed_type, priority ) VALUES (('DGC' ||to_char(currval('t1'),'FM0000')),'ORG0001','40','GENERAL','R');
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'HEAMOGLOBIN','','11-16gms%','1',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'TOTAL WBC COUNT (TC)','','4000-11000cells/cmm','2',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'DIFFERENTIAL WBC COUNT ( DC)','','','3',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'Poly.','','55-65%','4',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'Lymph.','','35-45%','5',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'Eosin.','','05-06%','6',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'Mono.','','01-03%','7',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'E.S.R','','12mm/hr','8',(nextval('resultlabel_seq')));
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'HB','',' 13.5 - 16.5','9',(nextval('resultlabel_seq')));

INSERT INTO diagnostics (test_id ,test_name, ddept_id, sample_needed, house_status , type_of_specimen , diag_code , conduct_in_reportformat ) VALUES (('DGC' ||to_char(nextval('t1'),'FM0000')),'X-RAY 10*12 (MEDIUM)','DDept0002','n','I','',('DGC' ||to_char(currval('t1'),'FM0000')),'Y');
INSERT INTO test_org_details (test_id ,org_id, applicable, item_code ) VALUES (('DGC' ||to_char(currval('t1'),'FM0000')),'ORG0001','t',('DGC' ||to_char(currval('t1'),'FM0000')));
INSERT INTO diagnostic_charges (test_id , org_name, charge, bed_type, priority ) VALUES (('DGC' ||to_char(currval('t1'),'FM0000')),'ORG0001','0','PRIVATE','R');
INSERT INTO test_results_master VALUES (('DGC' ||to_char(currval('t1'),'FM0000')), 'X-RAY 10*12 (MEDIUM)','','','1',(nextval('resultlabel_seq')));
DROP SEQUENCE t1 ; 


insert into test_template_master (test_id, format_name)
select test_id, 'FORMAT_DEF' from diagnostics where conduct_in_reportformat ='Y' and test_id not in (select test_id from test_template_master ) ;

UPDATE diag_test_timestamp SET test_timestamp = test_timestamp+1;


delete from theatre_charges;
delete from theatre_master;

INSERT INTO theatre_master VALUES ('THID0001', 'Ot-1', 'A', 0, 0, true);
INSERT INTO theatre_master VALUES ('THID0002', 'Ot-2', 'A', 0, 0, true);
INSERT INTO theatre_charges VALUES ('THID0001', 'ORG0001', 'GENERAL', 8000.00, 0.00, 0.00, 0.00);
INSERT INTO theatre_charges VALUES ('THID0001', 'ORG0001', 'SEMI-PVT', 8000.00, 0.00, 0.00, 0.00);
INSERT INTO theatre_charges VALUES ('THID0001', 'ORG0001', 'PRIVATE', 8000.00, 0.00, 0.00, 0.00);
INSERT INTO theatre_charges VALUES ('THID0002', 'ORG0001', 'GENERAL', 5000.00, 0.00, 0.00, 0.00);
INSERT INTO theatre_charges VALUES ('THID0002', 'ORG0001', 'SEMI-PVT', 5000.00, 0.00, 0.00, 0.00);
INSERT INTO theatre_charges VALUES ('THID0002', 'ORG0001', 'PRIVATE', 5000.00, 0.00, 0.00, 0.00);

delete from equipment_master ;
delete from equipement_charges;

insert into equipment_master values ('EQID0001','Ventilator','DEP0009','A','2','1','','t');
insert into equipement_charges values ('EQID0001','ORG0001','GENERAL',1000,250,100,0);
insert into equipement_charges values ('EQID0001','ORG0001','SEMI-PVT',1500,300,100,0);
insert into equipement_charges values ('EQID0001','ORG0001','PRIVATE',2000,400,150,0);


