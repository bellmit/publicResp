-- liquibase formatted sql
-- changeset dattuvs:To-give-prescription-access-rights-for-all-roles failOnError:false
delete from insta_section_rights where section_id=-7;
insert into insta_section_rights (section_id, role_id) select -7, role_id from u_role;
