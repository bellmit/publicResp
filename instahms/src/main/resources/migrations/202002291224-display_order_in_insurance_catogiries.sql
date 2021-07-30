-- liquibase formatted sql
-- changeset sirisharl:<display_order_on_categories>
ALTER TABLE item_insurance_categories ADD COLUMN display_order integer;
comment on column item_insurance_categories.display_order IS 'Usefull to display categories in Plan details OP,IP,OSP registration  screens';
