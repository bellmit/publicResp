-- liquibase formatted sql
-- changeset rajendratalekar:create-test-result-center-idx

create index test_results_center_status_idx on test_results_center(status);
create index test_results_center_resultlabel_id_idx on test_results_center(resultlabel_id);