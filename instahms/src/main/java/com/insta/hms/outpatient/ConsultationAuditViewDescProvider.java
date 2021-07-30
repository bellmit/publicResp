package com.insta.hms.outpatient;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

/**
 * The Class ConsultationAuditViewDescProvider.
 */
public class ConsultationAuditViewDescProvider extends InstaSectionAuditViewDescProvider {
  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = getCommonFeilds(tableName);
    desc.addField("section_item_id", "Cons. Id");
    return desc;
  }
}
