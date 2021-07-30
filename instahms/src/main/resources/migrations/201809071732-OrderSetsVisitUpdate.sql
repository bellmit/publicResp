-- liquibase formatted sql
-- changeset utkarshjindal:Updating-order-sets-to-fire-orderable-item-trigger

update packages p1 set status = foo.status from (select status,package_id from packages  ) as foo where p1.package_id = foo.package_id;
