-- Script to initial the master data for rajah hospital

insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0003', 'Free Card','A', 'cp',3);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0004', 'Free OP Card','A', 'cp',3);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0005', 'Free Maternity Card','A', 'cp',3);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0002', 'Green Card','A', '',0);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0006', 'Green OP Card','A', '',0);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0007', 'Green Maternity Card','A', '',0);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0008', 'General OP Card','A', '',0);
insert into organization_details (org_id, org_name, status,pharmacy_basis,pharmacy_discount_percentage) values ('ORG0009', 'General Maternity Card','A', '',0);

insert into patient_category_master (category_id, category_name, status, seperate_num_seq, rate_plan_id,  allowed_rate_plans,code) values (nextval('patient_category_master_seq'), 'Free', 'A', 'Y', 'ORG0003', 'ORG0003,ORG0004,ORG0005','fcard');
insert into patient_category_master (category_id, category_name, status, seperate_num_seq, rate_plan_id,  allowed_rate_plans,code) values (nextval('patient_category_master_seq'), 'Green', 'A', 'Y', 'ORG0002', 'ORG0002,ORG0006,ORG0007','gcard');
update patient_category_master set allowed_rate_plans='ORG0001,ORG0008,ORG0009' where category_id=1;

insert into unique_number (type_number,start_number,prefix,pattern) values ('fcard',1,'FC','000000');
insert into unique_number (type_number,start_number,prefix,pattern) values ('gcard',1,'GC','000000');

create sequence hospital_id_fcard START 1;
create sequence hospital_id_gcard START 1;

update registration_preferences set patient_category_field_label='Patient Category';
update registration_preferences set category_expiry_field_label='Category Validity';

--
-- Insert missing values into the each of the charges table. 
--
INSERT INTO diagnostic_charges
 SELECT m.test_id, m.org_id, 
  COALESCE((SELECT charge FROM diagnostic_charges dc WHERE dc.org_name = 'ORG0001' AND 
   dc.bed_type = m.bed_type AND dc.test_id = m.test_id and dc.priority = m.priority
  ),0) AS charge, m.bed_type, m.priority
 FROM missing_test_charges_view m where m.org_id in ('ORG0008','ORG0009')
;
INSERT INTO diagnostic_charges
 SELECT m.test_id, m.org_id, 
  COALESCE((SELECT charge FROM diagnostic_charges dc WHERE dc.org_name = 'ORG0002' AND 
   dc.bed_type = m.bed_type AND dc.test_id = m.test_id and dc.priority = m.priority
  ),0) AS charge, m.bed_type, m.priority
 FROM missing_test_charges_view m where m.org_id in ('ORG0006','ORG0007')
;
INSERT INTO diagnostic_charges
 SELECT m.test_id, m.org_id, 
  COALESCE((SELECT charge FROM diagnostic_charges dc WHERE dc.org_name = 'ORG0003' AND 
   dc.bed_type = m.bed_type AND dc.test_id = m.test_id and dc.priority = m.priority
  ),0) AS charge, m.bed_type, m.priority
 FROM missing_test_charges_view m where m.org_id in ('ORG0004','ORG0005')
;


INSERT INTO service_master_charges
 SELECT m.service_id, m.bed_type, m.org_id, 
   COALESCE((select unit_charge from service_master_charges sc where m.service_id = sc.service_id AND sc.bed_type=m.bed_type AND sc.org_id='ORG0001' ),0) as unit_charge
  FROM missing_service_charges_view m where m.org_id in ('ORG0008','ORG0009')
;
INSERT INTO service_master_charges
 SELECT m.service_id, m.bed_type, m.org_id, 
   COALESCE((select unit_charge from service_master_charges sc where m.service_id = sc.service_id AND sc.bed_type=m.bed_type AND sc.org_id='ORG0002' ),0) as unit_charge
  FROM missing_service_charges_view m where m.org_id in ('ORG0006','ORG0007')
;
INSERT INTO service_master_charges
 SELECT m.service_id, m.bed_type, m.org_id, 
   COALESCE((select unit_charge from service_master_charges sc where m.service_id = sc.service_id AND sc.bed_type=m.bed_type AND sc.org_id='ORG0003' ),0) as unit_charge
  FROM missing_service_charges_view m where m.org_id in ('ORG0004','ORG0005')
;

INSERT INTO equipement_charges 
 SELECT m.eq_id, m.org_id,m.bed_type, 
   COALESCE((select daily_charge from equipement_charges ec 
		where m.eq_id = ec.equip_id AND ec.bed_type=m.bed_type AND ec.org_id='ORG0001' ),0) as daily_charge,
   0,0,0
  FROM missing_equipment_charges_view m where m.org_id in ('ORG0008','ORG0009')
;
INSERT INTO equipement_charges 
 SELECT m.eq_id, m.org_id,m.bed_type, 
   COALESCE((select daily_charge from equipement_charges ec 
		where m.eq_id = ec.equip_id AND ec.bed_type=m.bed_type AND ec.org_id='ORG0002' ),0) as daily_charge,
   0,0,0
  FROM missing_equipment_charges_view m where m.org_id in ('ORG0006','ORG0007')
;
INSERT INTO equipement_charges 
 SELECT m.eq_id, m.org_id,m.bed_type, 
   COALESCE((select daily_charge from equipement_charges ec 
		where m.eq_id = ec.equip_id AND ec.bed_type=m.bed_type AND ec.org_id='ORG0003' ),0) as daily_charge,
   0,0,0
  FROM missing_equipment_charges_view m where m.org_id in ('ORG0004','ORG0005')
;

INSERT INTO doctor_consultation_charge
 SELECT m.doctor_id, m.bed_type, 
  COALESCE((select dc.doctor_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0001'),0), 
  COALESCE((select dc.night_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0001'),0), 
  m.org_id,
  COALESCE((select dc.ot_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0001'),0), 
  COALESCE((select dc.co_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0001'),0),
  COALESCE((select dc. assnt_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0001'),0) 
  FROM missing_doctor_charges_view m where org_id in ('ORG0008','ORG0009')
;

INSERT INTO doctor_consultation_charge
 SELECT m.doctor_id, m.bed_type, 
  COALESCE((select dc.doctor_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0002'),0), 
  COALESCE((select dc.night_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0002'),0), 
  m.org_id,
  COALESCE((select dc.ot_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0002'),0), 
  COALESCE((select dc.co_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0002'),0),
  COALESCE((select dc. assnt_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0002'),0) 
  FROM missing_doctor_charges_view m where org_id in ('ORG0006','ORG0007')
;

INSERT INTO doctor_consultation_charge
 SELECT m.doctor_id, m.bed_type, 
  COALESCE((select dc.doctor_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0003'),0), 
  COALESCE((select dc.night_ip_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0003'),0), 
  m.org_id,
  COALESCE((select dc.ot_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0003'),0), 
  COALESCE((select dc.co_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0003'),0),
  COALESCE((select dc. assnt_surgeon_charge from doctor_consultation_charge dc where dc.doctor_name = m.doctor_id AND 
   dc.bed_type=m.bed_type AND dc.organization='ORG0003'),0) 
  FROM missing_doctor_charges_view m where org_id in ('ORG0004','ORG0005')
;
 
INSERT INTO operation_charges
 SELECT m.op_id, m.org_id, m.bed_type, 
   COALESCE((select oc.surg_asstance_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0001'), 0), 
   COALESCE((select oc.surgeon_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0001'), 0), 
   COALESCE((select oc.anesthetist_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0001'), 0)
  FROM missing_operation_charges_view m where org_id in ('ORG0008','ORG0009')
;
 
INSERT INTO operation_charges
 SELECT m.op_id, m.org_id, m.bed_type, 
   COALESCE((select oc.surg_asstance_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0002'), 0), 
   COALESCE((select oc.surgeon_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0002'), 0), 
   COALESCE((select oc.anesthetist_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0002'), 0)
  FROM missing_operation_charges_view m where org_id in ('ORG0006','ORG0007')
;
 
INSERT INTO operation_charges
 SELECT m.op_id, m.org_id, m.bed_type, 
   COALESCE((select oc.surg_asstance_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0003'), 0), 
   COALESCE((select oc.surgeon_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0003'), 0), 
   COALESCE((select oc.anesthetist_charge from operation_charges oc where oc.op_id=m.op_id AND
   oc.bed_type=m.bed_type AND oc.org_id='ORG0003'), 0)
  FROM missing_operation_charges_view m where org_id in ('ORG0004','ORG0005')
;

INSERT INTO theatre_charges
 SELECT m.theatre_id, m.org_id, m.bed_type, 
   COALESCE((select tc.daily_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0001'),0),
   COALESCE((select tc.min_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0001'),0),
   COALESCE((select tc.incr_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0001'),0)
  FROM missing_theatre_charges_view m  where org_id in ('ORG0008','ORG0009')
;

INSERT INTO theatre_charges
 SELECT m.theatre_id, m.org_id, m.bed_type, 
   COALESCE((select tc.daily_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0002'),0),
   COALESCE((select tc.min_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0002'),0),
   COALESCE((select tc.incr_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0002'),0)
  FROM missing_theatre_charges_view m  where org_id in ('ORG0006','ORG0007')
;

INSERT INTO theatre_charges
 SELECT m.theatre_id, m.org_id, m.bed_type, 
   COALESCE((select tc.daily_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0003'),0),
   COALESCE((select tc.min_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0003'),0),
   COALESCE((select tc.incr_charge from theatre_charges tc where tc.theatre_id = m.theatre_id AND 
   tc.bed_type=m.bed_type AND tc.org_id='ORG0003'),0)
  FROM missing_theatre_charges_view m  where org_id in ('ORG0004','ORG0005')
;
 
INSERT INTO doctor_op_consultation_charge
 SELECT m.doctor_id, m.org_id, 
  COALESCE((select doc.op_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0001'),0), 
  COALESCE((select doc.op_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0001'),0),
  COALESCE((select doc.private_cons_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0001'),0), 
  COALESCE((select doc.private_cons_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0001'),0)
  FROM missing_doctor_op_charges_view m where org_id in ('ORG0008','ORG0009')
;
 
INSERT INTO doctor_op_consultation_charge
 SELECT m.doctor_id, m.org_id, 
  COALESCE((select doc.op_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0002'),0), 
  COALESCE((select doc.op_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0002'),0),
  COALESCE((select doc.private_cons_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0002'),0), 
  COALESCE((select doc.private_cons_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0002'),0)
  FROM missing_doctor_op_charges_view m where org_id in ('ORG0006','ORG0007')
;
 
INSERT INTO doctor_op_consultation_charge
 SELECT m.doctor_id, m.org_id, 
  COALESCE((select doc.op_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0003'),0), 
  COALESCE((select doc.op_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0003'),0),
  COALESCE((select doc.private_cons_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0003'),0), 
  COALESCE((select doc.private_cons_revisit_charge from doctor_op_consultation_charge doc where doc.doctor_id = m.doctor_id  
    AND doc.org_id='ORG0003'),0)
  FROM missing_doctor_op_charges_view m where org_id in ('ORG0004','ORG0005')
;

INSERT INTO bed_details
 SELECT m.bed_type, 
  COALESCE((select bed_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select nursing_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select initial_payment from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select duty_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select maintainance_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0),
  m.org_id, 'G','A',0,'N'
  FROM missing_bed_charges_view m where org_id in ('ORG0008','ORG0009')
;

INSERT INTO bed_details
 SELECT m.bed_type, 
  COALESCE((select bed_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select nursing_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select initial_payment from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select duty_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select maintainance_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0),
  m.org_id, 'G','A',0,'N'
  FROM missing_bed_charges_view m where org_id in ('ORG0006','ORG0007')
;

INSERT INTO bed_details
 SELECT m.bed_type, 
  COALESCE((select bed_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select nursing_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select initial_payment from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select duty_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select maintainance_charge from bed_details bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0),
  m.org_id, 'G','A',0,'N'
  FROM missing_bed_charges_view m where org_id in ('ORG0004','ORG0005')
;

INSERT INTO icu_bed_charges
 SELECT m.bed_type, 
  COALESCE((select bed_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select nursing_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select initial_payment from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select duty_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0), 
  COALESCE((select maintainance_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0001'),0),
  m.org_id, 'G','A',0,m.intensive_bed_type
  FROM missing_icu_bed_charges_view m where org_id in ('ORG0008','ORG0009')
;

INSERT INTO icu_bed_charges
 SELECT m.bed_type, 
  COALESCE((select bed_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select nursing_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select initial_payment from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select duty_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0), 
  COALESCE((select maintainance_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0002'),0),
  m.org_id, 'G','A',0,m.intensive_bed_type
  FROM missing_icu_bed_charges_view m where org_id in ('ORG0006','ORG0007')
;

INSERT INTO icu_bed_charges
 SELECT m.bed_type, 
  COALESCE((select bed_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select nursing_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select initial_payment from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select duty_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0), 
  COALESCE((select maintainance_charge from icu_bed_charges bd where bd.bed_type = m.bed_type AND bd.organization='ORG0003'),0),
  m.org_id, 'G','A',0,m.intensive_bed_type
  FROM missing_icu_bed_charges_view m where org_id in ('ORG0004','ORG0005')
;
---
--- Insert missing rows into the org_details tables
---
INSERT INTO test_org_details
 SELECT m.test_id, m.org_id FROM missing_test_org_view m 
;

INSERT INTO service_org_details
 SELECT m.service_id, m.org_id FROM missing_service_org_view m 
;

INSERT INTO operation_org_details
 SELECT m.op_id, m.org_id FROM missing_operation_org_view m 
;

---
--- Update operations to set them as available only under OP Card 
---
update operation_org_details set applicable ='f' where org_id in ('ORG0001','ORG0002','ORG0003','ORG0005','ORG0007','ORG0009');

