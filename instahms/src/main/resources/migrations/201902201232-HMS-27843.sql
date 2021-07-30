-- liquibase formatted sql
-- changeset krishnasameerachanta:<Add billing screen action rights>
insert into screen_rights (role_id, screen_id, rights) select role_id, 'new_op_bill', rights
from screen_rights where screen_id = 'credit_bill_collection';

insert into url_action_rights (role_id, action_id, rights) select role_id, 'new_op_bill', rights
from url_action_rights where action_id = 'credit_bill_collection';

insert into screen_rights (role_id, screen_id, rights) select role_id, 'new_ip_bill', rights
from screen_rights where screen_id = 'credit_bill_collection';

insert into url_action_rights (role_id, action_id, rights) select role_id, 'new_ip_bill', rights
from url_action_rights where action_id = 'credit_bill_collection';
