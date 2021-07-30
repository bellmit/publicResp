-- liquibase formatted sql
-- changeset asif:diag_report_sharing

insert into message_events Values('email_diag_report_share','Diagnostic Reports Share', 'Event used for triggering diagnostic reports with patient');
update message_types set event_id='email_diag_report_share' where message_type_id='email_diag_report';