-- liquibase formatted sql
-- changeset rajendratalekar:update-store-item-batch-details-expiry-date-to-last-day-of-month

UPDATE store_item_batch_details set exp_dt = (date_trunc('month', exp_dt) + interval '1 month' - interval '1 day')::date where exp_dt > '2017-01-01';
