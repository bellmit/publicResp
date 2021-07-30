-- liquibase formatted sql
-- changeset janakivg:center_id-to-scheduler_item_master

alter table scheduler_item_master add column center_id integer;

update scheduler_item_master  set center_id = 0 where resource_type IN('DOC', 'SUDOC', 'ANEDOC', 'PAEDDOC', 'ASUDOC', 'LABTECH', 'SRID') and resource_id != '*';

update scheduler_item_master sim set center_id = tq.center_id
from test_equipment_master tq where tq.eq_id=sim.resource_id::integer and sim.resource_type='EQID' and sim.resource_id != '*';

update scheduler_item_master sim set center_id = tm.center_id
from theatre_master tm where tm.theatre_id=sim.resource_id and sim.resource_type='THID' and sim.resource_id != '*';

update scheduler_item_master set center_id = 0 where resource_id = '*';
