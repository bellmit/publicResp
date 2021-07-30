-- liquibase formatted sql
-- changeset sreenivasayashwanth:insert_all_theatres_for_all_users

insert into user_theatres (emp_username,theatre_id)
SELECT u.emp_username,tm.theatre_id
FROM u_user u JOIN theatre_master tm ON (u.center_id = tm.center_id);
