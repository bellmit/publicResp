package com.insta.hms.integration.salucro;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SalucroLocationDescProvider.
 */
public class SalucroLocationDescProvider implements AuditLogDescProvider {

  /* (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("name", "Name");
    desc.addField("counter_id", "Counter Id");
    desc.addField("id", "Id");
    
    desc.addField("center_id", "Center Id");
    desc.addField("salucro_location_mapping_id", "Salucro Location Mapping Id");
        
    Map<String, String> activeInactive = new HashMap<>();
    activeInactive.put("A", "Active");
    activeInactive.put("I", "In Active");

    desc.addFieldValue("status", activeInactive);
    
    return desc;
  }

}

