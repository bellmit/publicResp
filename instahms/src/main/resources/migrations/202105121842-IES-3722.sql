-- liquibase formatted sql
-- changeset manjular:updated-transaction-sequence-to-package-related-tables failOnError:false


COMMENT ON  SEQUENCE package_content_charges_seq IS
	'{ "type": "Master", "comment": "Package Content Charges Seq" }';

COMMENT ON SEQUENCE package_charges_id_seq IS 
	'{ "type": "Master", "comment": "Package Charges Seq" }';

COMMENT ON SEQUENCE package_item_sub_groups_id_seq IS
	'{ "type": "Master", "comment": "Package Item Sub Group Seq" }';

COMMENT ON SEQUENCE packages_insurance_category_mapping_id_seq IS
	'{ "type": "Master", "comment": "Package Ins Category Seq" }';

