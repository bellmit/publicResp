-- liquibase formatted sql
-- changeset sanjana.goyal:drop-obsolete-generic-preference-columns

alter table generic_preferences drop column shafafiya_preauth_password_obsolete;
alter table generic_preferences drop column shafafiya_password_obsolete; 
alter table generic_preferences drop column dhpo_facility_password_obsolete;

alter table generic_preferences drop column shafafiya_user_id_obsolete;
alter table generic_preferences drop column shafafiya_pbm_active_obsolete;
alter table generic_preferences drop column dhpo_facility_user_id_obsolete;
alter table generic_preferences drop column shafafiya_preauth_user_id_obsolete;
alter table generic_preferences drop column shafafiya_preauth_active_obsolete;