/**
 *
 */

package com.insta.hms.genericdocuments;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocsDescProvider.
 *
 * @author lakshmi.p
 */
public class GenericDocsDescProvider implements AuditLogDescProvider {

  /**
   * Instantiates a new generic docs desc provider.
   */
  public GenericDocsDescProvider() {}

  /** (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("mr_no", "Mr No.");
    desc.addField("doc_id", "Doc Id", false);
    desc.addField("doc_name", "Doc Name", false);
    desc.addField("doc_date", "Doc Date", false);

    return desc;
  }
}
