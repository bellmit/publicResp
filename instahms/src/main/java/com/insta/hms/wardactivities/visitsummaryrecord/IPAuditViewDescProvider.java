package com.insta.hms.wardactivities.visitsummaryrecord;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.instaforms.InstaSectionAuditViewDescProvider;

public class IPAuditViewDescProvider extends InstaSectionAuditViewDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    return getCommonFeilds(tableName);
  }
}
