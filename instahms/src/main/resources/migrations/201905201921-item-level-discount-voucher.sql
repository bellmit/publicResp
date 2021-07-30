-- liquibase formatted sql
-- changeset satishl2772:adding-item-level-discount-voucher-generic-preference

ALTER TABLE generic_preferences ADD COLUMN item_level_discount_voucher character(1) NOT NULL DEFAULT 'N';
COMMENT ON COLUMN generic_preferences.item_level_discount_voucher IS 'Enabling(Y) or disabling(N) item level discounts posting into accounting table';
