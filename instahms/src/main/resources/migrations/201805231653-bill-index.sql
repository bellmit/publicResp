-- liquibase formatted sql
-- changeset adityabhatia02:adding-indexes-for-bill-open-date failOnError:false

CREATE INDEX idx_bill_open_date ON bill USING btree(date(open_date));
CREATE index idx_insurance_submission_batch_is_resubmission ON insurance_submission_batch USING btree(is_resubmission);
