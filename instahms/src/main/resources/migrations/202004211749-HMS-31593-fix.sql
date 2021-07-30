-- liquibase formatted sql
-- changeset dilipsaikumar:To update orderable_item table to make it  searchable by package_code and order_code

UPDATE orderable_item o
SET item_codes=(select lower(concat(p.package_code, ' ', p.order_code))
FROM packages p
WHERE o.entity_id::INTEGER=p.package_id)
WHERE o.entity IN ('Package', 'MultiVisitPackage' );
