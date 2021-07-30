-- liquibase formatted sql
-- changeset rajendratalekar:update-store-sales-details-expiry-date-to-last-day-of-month

UPDATE store_sales_details set expiry_date = (date_trunc('month', expiry_date) + interval '1 month' - interval '1 day')::date where expiry_date > '2017-01-01';
