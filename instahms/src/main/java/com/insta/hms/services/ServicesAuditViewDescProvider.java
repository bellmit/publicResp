package com.insta.hms.services;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class ServicesAuditViewDescProvider.
 */
public class ServicesAuditViewDescProvider extends InstaSectionAuditViewDescProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.auditlog.BasicAuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = getCommonFeilds(tableName);
    desc.addField("section_item_id", "Service Presc. Id");
    return desc;
  }
}
