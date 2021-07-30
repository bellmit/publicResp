-- liquibase formatted sql
-- changeset SirishaRL:missing_default_center_suppliers 
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT max_centers_inc_default FROM generic_preferences

delete from supplier_center_master WHERE center_id = 0 ;
insert into supplier_center_master (select nextval('supplier_center_master_seq') as supp_center_id ,0 as center_id,supplier_code,status from supplier_master );
