package com.insta.hms.mdm.codesets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageMaster extends VirtualTable {

  private static final String[] LANG_CODES = Locale.getISOLanguages();

  public LanguageMaster(String tablename, String entityname, String entityid) {
    super(tablename, entityname, entityid);
    createLanguageMapList();
  }

  /**
   * Creates a map of Language codes with ids.
   * 
   */
  public void createLanguageMapList() {
    List<Map<String, Object>> langList = new ArrayList<>();
    List<Integer> langIds = new ArrayList<>();
    for (String langCode : LANG_CODES) {
      Locale langLocale = Locale.forLanguageTag(langCode);
      String langDisplay = langLocale.getDisplayLanguage(langLocale);
      int entityId = generateEntityId(langCode);
      langIds.add(entityId);
      langList.add(getMasterMap(langDisplay, entityId));
    }
    langList
    .sort((o1, o2) -> ((Integer) o1.get("entity_id")).compareTo((Integer) o2.get("entity_id")));
    setMasterList(langList);
    setEntityIds(langIds);
  }

  private int generateEntityId(String langCode) {
    char[] langCodeArr = langCode.toCharArray();
    StringBuffer id = new StringBuffer("");
    for (char a : langCodeArr) {
      id.append(String.valueOf(a - 'a' + 1));
    }
    return Integer.parseInt(id.toString());
  }

}
