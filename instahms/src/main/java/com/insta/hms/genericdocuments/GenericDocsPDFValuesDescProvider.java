/**
 *
 */

package com.insta.hms.genericdocuments;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocsPDFValuesDescProvider.
 *
 * @author lakshmi.p
 */
public class GenericDocsPDFValuesDescProvider implements AuditLogDescProvider {


  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("doc_id", "Doc Id", false);
    desc.addField("field_name", "Field Name", false);
    desc.addField("field_value", "Field Value", false);

    return desc;
  }

}
