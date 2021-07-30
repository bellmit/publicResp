-- liquibase formatted sql
-- changeset sonam009:<vital-section-enable-for-ip-in-system-gen-section>

UPDATE system_generated_sections SET ip='Y' WHERE section_id='-4' ;