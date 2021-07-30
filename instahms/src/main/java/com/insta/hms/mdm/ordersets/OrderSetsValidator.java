package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterDetailsValidator;

import org.springframework.stereotype.Component;

@Component
public class OrderSetsValidator extends MasterDetailsValidator {
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "package_name", "status",
      "visit_applicability", "gender_applicability" };
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  private static final String[] MAX_LENGTH_FIELDS = new String[] { "package_name" };
  private static final ValidationRule MAX_LENGTH_RULE = new MaximumLengthRule(100);

  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "package_id",
      "package_name", "status", "visit_applicability", "gender_applicability" };

  private static final String[] PACKAGE_CONTENTS_NOT_NULL_FIELDS_INSERT = new String[] {
      "package_id", "activity_qty" };

  private static final String[] PACKAGE_CONTENTS_NOT_NULL_FIELDS_UPDATE = new String[] {
      "package_content_id", "package_id", "activity_qty" };

  private static final String[] DEPT_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_INSERT = new String[] {
      "package_id", "dept_id" };

  private static final String[] DEPT_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_UPDATE = new String[] {
      "dept_package_id", "package_id", "dept_id" };

  private static final String[] CENTER_PACK_APPLICABILITY_NOT_NULL_FIELDS_INSERT = new String[] {
      "package_id", "center_id" };

  private static final String[] CENTER_PACK_APPLICABILITY_NOT_NULL_FIELDS_UPDATE = new String[] {
      "center_package_id", "package_id", "center_id" };

  private static final String[] TPA_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_INSERT = new String[] {
      "package_id", "tpa_id" };

  private static final String[] TPA_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_UPDATE = new String[] {
      "tpa_package_id", "package_id", "tpa_id" };

  private static final ValidationRule DOC_CONS_TYPE_RULE = new DoctorConsultationTypeRule();

  private static final ValidationRule QUANTITY_RULE = new QuantityRule();

  /**
   * Instantiates a new order sets validator.
   */
  public OrderSetsValidator() {
    super();
    /* INSERT VALIDATION RULES. */
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule(MAX_LENGTH_RULE, MAX_LENGTH_FIELDS);
    addInsertRule("package_contents", NOT_NULL_RULE, PACKAGE_CONTENTS_NOT_NULL_FIELDS_INSERT);
    addInsertRule("package_contents", QUANTITY_RULE, null);
    addInsertRule("package_contents", DOC_CONS_TYPE_RULE, null);
    addInsertRule("dept_package_applicability", NOT_NULL_RULE,
        DEPT_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_INSERT);
    addInsertRule("center_package_applicability", NOT_NULL_RULE,
        CENTER_PACK_APPLICABILITY_NOT_NULL_FIELDS_INSERT);
    addInsertRule("tpa_package_applicability", NOT_NULL_RULE,
        TPA_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_INSERT);

    /* UPDATE VALIDATION RULES */
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addUpdateRule(MAX_LENGTH_RULE, MAX_LENGTH_FIELDS);
    addUpdateRule("package_contents", NOT_NULL_RULE, PACKAGE_CONTENTS_NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("package_contents", QUANTITY_RULE, null);
    addUpdateRule("package_contents", DOC_CONS_TYPE_RULE, null);
    addUpdateRule("dept_package_applicability", NOT_NULL_RULE,
        DEPT_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("center_package_applicability", NOT_NULL_RULE,
        CENTER_PACK_APPLICABILITY_NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("tpa_package_applicability", NOT_NULL_RULE,
        TPA_PACKAGE_APPLICABILITY_NOT_NULL_FIELDS_UPDATE);
  }
}
