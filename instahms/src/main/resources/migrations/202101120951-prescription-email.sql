-- liquibase formatted sql
-- changeset manasaparam:updated-default-email-body

update message_types set message_body='Please find your prescription for ${visit_id}, dated ${visit_date} attached with this mail' where message_group_name ='Patient Prescription';