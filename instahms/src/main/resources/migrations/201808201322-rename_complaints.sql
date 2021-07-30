-- liquibase formatted sql
-- changeset yashwantkumar:Renamed-complaints-display-message-of-system-section.

update system_generated_sections set  display_name = 'Complaints' where section_id = -1;
