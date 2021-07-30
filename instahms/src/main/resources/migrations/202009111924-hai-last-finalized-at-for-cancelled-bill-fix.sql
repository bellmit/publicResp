-- liquibase formatted sql
-- changeset rajendratalekar:hai-last-finalized-at-for-cancelled-bill-fix

update bill set last_finalized_at = mod_time where status='X' and last_finalized_at is not null;
