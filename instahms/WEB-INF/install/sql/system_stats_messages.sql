 --- Pump the messages into the system_messages table
insert into system_messages values (nextval('system_messages_seq'), '$2: Bills Now bills open more than 1 day = $1', 'Stats1','W',1,'search_bills',
	(select count(*) from bill where status ='A' and date(open_date) < current_date and bill_type ='P') ,'');
insert into system_messages values (nextval('system_messages_seq'), '$2: Pharmacy Retail bills open more than 1 day = $1', 'Stats2','W',1,'pharma_sales',
	(select count(*) from bill where status ='A' and date(open_date) < current_date and bill_type ='M' and visit_type ='r') ,'');
insert into system_messages values 
	(nextval('system_messages_seq'), '$2: Lab Tests pending for conduction more than 1 day = $1', 'Stats3','W',1,'lab_diag_report_List',
	(select count(*) from tests_prescribed tp join diagnostics d using (test_id) join diagnostics_departments dd using (ddept_id) 
		where conducted in ('N','P') and pres_date < current_date and dd.category='DEP_LAB') ,'');
insert into system_messages values 
	(nextval('system_messages_seq'), '$2: Radiology Tests pending for conduction more than 1 day = $1', 'Stats4','W',1,'radio_diag_report_list',
	(select count(*) from tests_prescribed tp join diagnostics d using (test_id) join diagnostics_departments dd using (ddept_id) 
		where conducted in ('N','P') and pres_date < current_date and dd.category='DEP_RAD') ,'');
insert into system_messages values (nextval('system_messages_seq'), '$2: Services pending for conduction more than 1 day = $1', 'Stats5','W',1,'serive_list',
	(select count(*) from services_prescribed where conducted ='N' and date(presc_date) < current_date ) ,'');
insert into system_messages values (nextval('system_messages_seq'), '$2: OP Visits not closed more than 1 day = $1', 'Stats6','W',1,'reg_patient_visit_search',
	(select count(*) from patient_registration where status ='A' and visit_type ='o' and date(reg_date) < current_date ) ,'');
insert into system_messages values (nextval('system_messages_seq'), '$2: Beds pending to be allocated more than 1 day = $1', 'Stats7','W',1,'adt',
        (select count(*) from patient_registration pr where pr.status='A' and pr.visit_type ='i' and date(pr.reg_date) < current_date and patient_id not in (select patient_id from admission)) ,'');
	


--- 
