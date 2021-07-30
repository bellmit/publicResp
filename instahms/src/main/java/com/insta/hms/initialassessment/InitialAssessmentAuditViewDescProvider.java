package com.insta.hms.initialassessment;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class InitialAssessmentAuditViewDescProvider.
 */
public class InitialAssessmentAuditViewDescProvider extends InstaSectionAuditViewDescProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.auditlog.BasicAuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    AuditLogDesc desc = getCommonFeilds(tableName);
    desc.addField("section_item_id", "Cons. Id");
    return desc;
  }
}
