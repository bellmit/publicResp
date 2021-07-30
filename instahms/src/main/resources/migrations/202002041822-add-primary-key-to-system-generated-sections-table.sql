-- liquibase formatted sql
-- changeset javalkarvinay:Added primary key to system generated sections table

ALTER TABLE system_generated_sections ADD PRIMARY KEY (section_id);
