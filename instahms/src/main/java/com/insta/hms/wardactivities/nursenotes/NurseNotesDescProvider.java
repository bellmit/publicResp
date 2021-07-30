package com.insta.hms.wardactivities.nursenotes;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class NurseNotesDescProvider.
 */
public class NurseNotesDescProvider implements AuditLogDescProvider {
  /**
   * Gets the audit log desc.
   *
   * @param tableName the tableName
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {

    Map<String, String> finalizedMap = new LinkedHashMap<String, String>();
    finalizedMap.put("Y", "Yes");
    finalizedMap.put("N", "No");

    Map<String, String> htoverMap = new LinkedHashMap<String, String>();
    htoverMap.put("H", "Hand over");
    htoverMap.put("T", "Take over");
    
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("notes", "Nurse Notes");
    desc.addField("note_type", "Note Type");
    desc.addFieldValue("finalized", finalizedMap);
    desc.addFieldValue("note_type", htoverMap);

    return desc;
  }

}
