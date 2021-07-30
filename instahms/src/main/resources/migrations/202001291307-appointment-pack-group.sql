-- liquibase formatted sql
-- changeset manasaparam:migrating-appointment-pack-group-id-for-zero

update scheduler_appointments set appointment_pack_group_id =null where appointment_pack_group_id=0;
