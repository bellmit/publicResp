-- liquibase formatted sql
-- changeset Allabakash:coder-review-comments

COMMENT ON TABLE review_types IS '{ "type": "Master", "comment": "review types master data" }';
COMMENT ON TABLE reviews IS '{ "type": "Txn", "comment": "reviews transaction table" }';
COMMENT ON TABLE review_details IS '{ "type": "Txn", "comment": "review details transction table" }';
COMMENT ON TABLE review_comments IS '{ "type": "Txn", "comment": "review comments transction table" }';
COMMENT ON TABLE review_recipients IS '{ "type": "Txn", "comment": "review receipients transction table" }';
COMMENT ON TABLE review_activities IS '{ "type": "Txn", "comment": "review activities transction table" }';
COMMENT ON TABLE review_categories IS '{ "type": "Master", "comment": "review categories master table" }';
COMMENT ON TABLE review_category_role IS '{ "type": "Master", "comment": "review category roles master table" }';
COMMENT ON TABLE review_type_center_role IS '{ "type": "Master", "comment": "review type center roles" }';

COMMENT ON SEQUENCE review_type_role_id_seq IS '{ "type": "Txn", "comment": "review type role id sequence" }';
COMMENT ON SEQUENCE review_types_id_seq IS '{ "type": "Master", "comment": "review types id sequence" }';
COMMENT ON SEQUENCE review_details_id_seq IS '{ "type": "Txn", "comment": "review details id sequence" }';
COMMENT ON SEQUENCE review_comments_id_seq IS '{ "type": "Txn", "comment": "review comments id sequence" }';
COMMENT ON SEQUENCE review_recipients_id_seq IS '{ "type": "Txn", "comment": "review receipients id sequence" }';
COMMENT ON SEQUENCE reviews_id_seq IS '{ "type": "Txn", "comment": "review id sequence" }';
COMMENT ON SEQUENCE review_activity_seq IS '{ "type": "Txn", "comment": "review activity sequence" }';
COMMENT ON SEQUENCE review_activity_changeset_seq IS '{ "type": "Txn", "comment": "review activity changeset sequence" }';
COMMENT ON SEQUENCE review_categories_seq IS '{ "type": "Master", "comment": "review categories sequence" }';
COMMENT ON SEQUENCE review_category_role_seq IS '{ "type": "Master", "comment": "review category role sequence" }';
COMMENT ON SEQUENCE review_type_center_role_seq IS '{ "type": "Master", "comment": "review type center role sequence" }';

