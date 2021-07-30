-- liquibase formatted sql
-- changeset tejakilaru:<section-mandatory-migration>

UPDATE section_master sm SET section_mandatory = true WHERE exists (SELECT true FROM section_field_desc sfd WHERE sfd.section_id=sm.section_id AND sfd.is_mandatory=true LIMIT 1);
