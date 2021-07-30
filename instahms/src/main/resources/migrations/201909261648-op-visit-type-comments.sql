-- liquibase formatted sql
-- changeset adeshatole:comments-for-op-visit-type-rules

COMMENT ON TABLE op_visit_type_rule_master IS '{ "type": "Master", "comment": "Holds rules to determine op visit type" }';
COMMENT ON TABLE op_visit_type_rule_details IS '{ "type": "Master", "comment": "Holds ranges of days and respective visit type" }';
COMMENT ON TABLE op_visit_type_rule_applicability IS '{ "type": "Master", "comment": "Holds criteria and applicable rule" }';
COMMENT ON SEQUENCE op_visit_type_rule_applicability_seq IS '{ "type": "Master", "comment": "Holds sequence for op visit type rule applicability master" }';
COMMENT ON SEQUENCE op_visit_type_rule_master_seq IS '{ "type": "Master", "comment": "Holds sequence for op visit type rule master" }';
COMMENT ON SEQUENCE op_visit_type_rule_details_seq IS '{ "type": "Master", "comment": "Holds sequence for op visit type rule details master" }';
