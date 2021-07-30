-- liquibase formatted sql
-- changeset sanjana:resource-override-centerid-fix

update sch_resource_availability_details set center_id=0 where res_avail_details_id in (select res_avail_details_id from sch_resource_availability_details where availability_status='A' and center_id is null);