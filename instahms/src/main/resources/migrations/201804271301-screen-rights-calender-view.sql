-- liquibase formatted sql
-- changeset janakivg:insert-doc_scheduler_calender_view-screen_rights

insert into screen_rights (select role_id, 'doc_scheduler_calender_view', rights from screen_rights where screen_id in ('doc_scheduler'));
insert into url_action_rights (select role_id, 'doc_scheduler_calender_view', rights from url_action_rights where action_id in ('doc_scheduler'));
