-- liquibase formatted sql
-- changeset rajendratalekar:update-store-grn-details-expiry-date-to-last-day-of-month

UPDATE store_grn_details set exp_dt = (date_trunc('month', exp_dt) + interval '1 month' - interval '1 day')::date where exp_dt > '2017-01-01';
