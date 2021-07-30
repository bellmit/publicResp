-- liquibase formatted sql
-- changeset raeshmika:<migration-scheduler-master-for-dept-column>

UPDATE scheduler_master SET dept='*' WHERE dept IS NULL OR dept='';
