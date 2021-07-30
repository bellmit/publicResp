-- liquibase formatted sql
-- changeset sanjana.goyal:merging-messaging-mod


insert into url_action_rights (select distinct role_id ,'mas_message_type', rights from url_action_rights where action_id in ('mas_general_message_type','mas_scheduler_message_type'));

insert into url_action_rights (select distinct role_id ,'message_send', rights from url_action_rights where action_id in ('scheduler_message_send','general_message_send'));

insert into screen_rights (select distinct role_id ,'mas_message_type', rights from screen_rights where screen_id in ('mas_general_message_type','mas_scheduler_message_type'));

insert into screen_rights (select distinct role_id ,'message_send', rights from screen_rights where screen_id in ('scheduler_message_send','general_message_send'));

delete from url_action_rights where action_id in ('mas_general_message_type','mas_scheduler_message_type','scheduler_message_send','general_message_send');

delete from screen_rights where screen_id in ('mas_general_message_type','mas_scheduler_message_type','scheduler_message_send','general_message_send');
