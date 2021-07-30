/**
 *
 */

package com.insta.hms.genericdocuments;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsAuditDescProvider.
 *
 * @author lakshmi.p
 */
public class GenericDocumentsAuditDescProvider extends MultiAuditLogDescProvider {

  /** The Constant GENERIC_DOCUMENTS_VIEW_TABLES. */
  private static final String[] GENERIC_DOCUMENTS_VIEW_TABLES = new String[] {
      "patient_general_docs_audit_log", "patient_pdf_form_doc_values_audit_log"};

  /**
   * Instantiates a new generic documents audit desc provider.
   */
  public GenericDocumentsAuditDescProvider() {
    super(GENERIC_DOCUMENTS_VIEW_TABLES);
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.auditlog.BasicAuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = super.getAuditLogDesc(tableName);

    // First add all the key fields
    desc.addField("mr_no", "Mr No.");
    desc.addField("doc_id", "Doc Id", false);
    desc.addField("doc_name", "Doc Name", false);
    desc.addField("doc_date", "Doc Date", false);

    return desc;
  }
}
