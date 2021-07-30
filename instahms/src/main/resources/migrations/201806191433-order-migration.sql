-- liquibase formatted sql
-- changeset chetankasireddy:inserting-new-order-action-rights

delete from url_action_rights where action_id in ('new_op_order', 'new_ip_order');
insert into url_action_rights (select role_id, 'new_op_order', rights from url_action_rights where action_id = 'order');
insert into url_action_rights (select role_id, 'new_ip_order', rights from url_action_rights where action_id = 'order');

delete from action_rights where action in ('new_op_order', 'new_ip_order');
insert into action_rights (select role_id, 'new_op_order', rights from action_rights where action = 'order');
insert into action_rights (select role_id, 'new_ip_order', rights from action_rights where action = 'order');

delete from screen_rights where screen_id in ('new_op_order', 'new_ip_order');
insert into screen_rights (select role_id, 'new_op_order', rights from screen_rights where screen_id = 'order');
insert into screen_rights (select role_id, 'new_ip_order', rights from screen_rights where screen_id = 'order');
