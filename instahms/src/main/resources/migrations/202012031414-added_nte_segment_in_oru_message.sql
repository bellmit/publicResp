-- liquibase formatted sql
-- changeset javalkarvinay:added_nte_segment_in_oru_message

INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,12,7,true,'A');

