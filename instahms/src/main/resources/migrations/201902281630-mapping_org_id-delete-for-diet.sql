-- liquibase formatted sql
-- changeset anandpatel:deleting-data-for-dietary-and-order-sets-from-mapping_org_id
CREATE TABLE temp_mapping_org_id_diet_order_set AS
  (SELECT *
   FROM   mapping_org_id
   WHERE  orderable_item_id IN (SELECT orderable_item_id
                                FROM   orderable_item
                                WHERE  entity IN ( 'Meal', 'Order Sets' ))); 
COMMENT ON table temp_mapping_org_id_diet_order_set is '{ "type": "Master", "comment": "Temporary table of mapping_org_id for mapping of diet and order sets" }';
DELETE FROM mapping_org_id
WHERE  orderable_item_id IN (SELECT orderable_item_id
                             FROM   orderable_item
                             WHERE  entity IN ( 'Meal', 'Order Sets' )); 
