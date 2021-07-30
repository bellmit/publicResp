
-- liquibase formatted sql
-- changeset Allabakash:coder-claim-review-migration

ALTER TABLE IF EXISTS codification_message_types RENAME TO review_types;

ALTER SEQUENCE IF EXISTS coder_claim_review_activity_seq  RENAME TO review_activity_seq;
ALTER SEQUENCE IF EXISTS coder_claim_review_activity_changeset_seq  RENAME TO review_activity_changeset_seq;

ALTER SEQUENCE IF EXISTS codification_message_category_seq RENAME TO review_categories_seq;
ALTER SEQUENCE IF EXISTS category_role_seq RENAME TO review_category_role_seq;

ALTER TABLE IF EXISTS coder_claim_review_activity RENAME TO review_activity;

ALTER TABLE IF EXISTS codification_message_category RENAME TO review_categories;

ALTER TABLE IF EXISTS category_role RENAME TO review_category_role;

ALTER SEQUENCE IF EXISTS codification_message_type_role_id_seq RENAME TO review_type_role_id_seq;

ALTER TABLE IF EXISTS codification_ticket_details RENAME TO review_details;

ALTER SEQUENCE IF EXISTS codification_ticket_details_id_seq RENAME TO review_details_id_seq;

ALTER SEQUENCE IF EXISTS codification_message_types_id_seq RENAME TO review_types_id_seq;

ALTER SEQUENCE IF EXISTS tickets_id_seq RENAME TO reviews_id_seq;

ALTER TABLE IF EXISTS tickets RENAME TO reviews;

ALTER TABLE IF EXISTS review_activity RENAME TO review_activities;

ALTER TABLE IF EXISTS ticket_comments RENAME TO review_comments;

ALTER SEQUENCE IF EXISTS ticket_comments_ticket_id_seq RENAME TO review_comments_id_seq;

ALTER SEQUENCE IF EXISTS ticket_recipients_id_seq RENAME TO review_recipients_id_seq;

ALTER TABLE IF EXISTS ticket_recipients RENAME TO review_recipients;

-- Altering review_type columns
ALTER TABLE review_types RENAME COLUMN message_type_id TO review_type_id;

ALTER TABLE review_types RENAME COLUMN message_type TO review_type;

ALTER TABLE review_types RENAME COLUMN message_title TO review_title;

ALTER TABLE review_types RENAME COLUMN message_content TO review_content;

ALTER TABLE review_types RENAME COLUMN message_category TO review_category;

ALTER TABLE review_types RENAME COLUMN message_category_id TO review_category_id;

ALTER TABLE review_details RENAME COLUMN message_type_id TO review_type_id;

ALTER TABLE review_type_center_role RENAME COLUMN message_type_id TO review_type_id;
