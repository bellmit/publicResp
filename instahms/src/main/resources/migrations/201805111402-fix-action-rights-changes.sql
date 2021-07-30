-- liquibase formatted sql

-- changeset raeshmika:insert-appointmentplanner-action-rights failOnError:false

INSERT INTO action_rights (select role_id, 'add_edit_appointment_plan', 'N' from u_role);
