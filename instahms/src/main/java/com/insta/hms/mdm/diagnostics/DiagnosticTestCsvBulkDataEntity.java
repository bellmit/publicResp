package com.insta.hms.mdm.diagnostics;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;


/**
 * The Class DiagnosticTestCSVBulkDataEntity.
 *
 * @author anil.n
 */

@Component("diagTestCSVEntity")
public class DiagnosticTestCsvBulkDataEntity extends CsVBulkDataEntity {
  /** The Constant KEYS. */
  private static final String[] KEYS = new String[] { "test_id" };

  /** The Constant FIELDS. */
  private static final String[] FIELDS = new String[] { "test_id", "test_name", "sample_needed",
      "ddept_id", "type_of_specimen", "conduction_format", "status", "service_sub_group_id",
      "service_group_id", "conduction_applicable", "conducting_doc_mandatory",
      "mandate_additional_info", "test_id", "results_entry_applicable", "diag_code",
      "insurance_category_id", "prior_auth_required", "allow_rate_increase", "allow_rate_decrease",
      "hl7_export_code" };

  /**
   * The Constant MASTERS with fields referencedField, referencedTable, referencedTablePK and
   * referencedTableNameField.
   */
  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("ddept_id", "diagnostics_departments", "ddept_id", "ddept_id"),
      new BulkDataMasterEntity("service_sub_group_id", "service_sub_groups",
          "service_sub_group_id", "service_sub_group_id"),
      new BulkDataMasterEntity("service_group_id", "service_groups", "service_group_id",
          "service_group_id"),
      new BulkDataMasterEntity("insurance_category_id", "item_insurance_categories",
          "insurance_category_id", "insurance_category_id"),
      new BulkDataMasterEntity("test_id", "diagnostic_charges", "test_id", "charge") };

  /**
   * Instantiates a new stores CSV bulk data entity.
   */
  public DiagnosticTestCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
    super.setAlias("sample_type", "type_of_specimen");
  }

}
