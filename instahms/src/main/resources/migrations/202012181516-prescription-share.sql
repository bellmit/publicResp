-- liquibase formatted sql
-- changeset manasaparam:patient-prescription-sharing

insert into message_events Values('prescription_share','Patient Prescription', 'Event used for triggering prescription with patient');

insert into message_events Values('prescription_share_auto','Patient Prescription', 'Event used for auto triggering prescription with patient');

insert into message_types values('email_prescription_manual','Patient Prescription(Manual)','Prescription is shared with patient via email',null,null,'Prescription','Please find your prescription for ${visit_no}, dated ${visit_date} attached with this mail.
<br/><br/>${_report_content}',null,null,'prescription_share','EMAIL','I',3,'general','A',null,'N','Patient','Patient Prescription');

insert into message_types values('email_prescription_auto','Patient Prescription(Auto)','Prescription is shared Automaticaly with patient via email when consultation is saved',null,null,'Prescription','Please find your prescription for ${visit_no}, dated ${visit_date} attached with this mail.
<br/><br/>${_report_content}',null,null,'prescription_share_auto','EMAIL','I',3,'general','A',null,'N','Patient','Patient Prescription');

ALTER TABLE doctor_consultation add column visit_mode character varying(1);

update doctor_consultation set visit_mode ='I';