-- liquibase formatted sql
-- changeset utkarshjindal:firing-orderSets-trigger-to-update-visit-applicability

update orderable_item oi set visit_type = foo.visit_applicability from (select visit_applicability, package_id::text from packages) as foo where oi.entity_id = foo.package_id and entity = 'Order Sets';
