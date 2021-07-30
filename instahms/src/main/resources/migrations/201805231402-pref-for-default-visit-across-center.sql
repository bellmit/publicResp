-- liquibase formatted sql
-- changeset kartikag01:add-reg-pref-default-visit-details

alter table registration_preferences ADD default_visit_details_across_center character(1) default 'Y';