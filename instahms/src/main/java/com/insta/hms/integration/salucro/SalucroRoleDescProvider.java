package com.insta.hms.integration.salucro;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SalucroRoleDescProvider.
 */
public class SalucroRoleDescProvider implements AuditLogDescProvider {

  /* (non-Javadoc)
   * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
   */
  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("role", "Role");
    desc.addField("permissions", "Permissions");
    desc.addField("emp_username", "Employee Username");
    desc.addField("salucro_role_mapping_id", "Salucro Role Mapping Id");
    
    desc.addField("center_id", "Center Id");
    desc.addField("user_name", "User Name");
        
    Map<String, String> activeInactive = new HashMap<>();
    activeInactive.put("A", "Active");
    activeInactive.put("I", "In Active");

    desc.addFieldValue("status", activeInactive);
    return desc;
  }

}

