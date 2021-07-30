package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.common.StdReportDesc.JoinTable;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.scheduledreport.StrutsDescParser;

import flexjson.JSONContext;
import flexjson.JSONSerializer;
import flexjson.Path;
import flexjson.TypeContext;
import flexjson.transformer.AbstractTransformer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/**
 * The Class StdReportDescXmlProvider.
 */
public class StdReportDescXmlProvider implements StdReportDescProvider {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportDescXmlProvider.class);

  /** The digester. */
  protected static Digester digester;

  /** The base dir. */
  protected String baseDir;

  static {

    digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("report-desc", "com.insta.hms.common.StdReportDesc");
    digester.addSetProperties("report-desc");

    digester.addCallMethod("report-desc/query", "addQuery", 1);
    digester.addCallParam("report-desc/query", 0);

    digester.addCallMethod("report-desc/tableName", "addTable", 1);
    digester.addCallParam("report-desc/tableName", 0);

    digester.addObjectCreate("report-desc/queryUnit",
        "com.insta.hms.common.StdReportDesc$QueryUnit");
    digester.addSetProperties("report-desc/queryUnit");
    /*
     * FactoryCreate is useful when the Java class with which you wish to create an object instance
     * does not have a no-arguments constructor, or where you wish to perform other setup processing
     * before the object is handed over to the Digester.
     */
    digester.addFactoryCreate("report-desc/queryUnit/joinTable",
        "com.insta.hms.common.StdReportDesc$JoinTable");
    digester.addSetNext("report-desc/queryUnit/joinTable", "addJoinTable",
        "com.insta.hms.common.StdReportDesc$JoinTable");
    digester.addFactoryCreate("report-desc/queryUnit/joinInclude",
        "com.insta.hms.common.StdReportDesc$JoinInclude");
    digester.addSetNext("report-desc/queryUnit/joinInclude", "addIncludeTojoinTables",
        "com.insta.hms.common.StdReportDesc$JoinInclude");
    digester.addSetNestedProperties("report-desc/queryUnit/groupBy");
    digester.addCallMethod("report-desc/queryUnit/groupBy", "addGroupBy");

    digester.addSetNext("report-desc/queryUnit", "addQueryUnit",
        "com.insta.hms.common.StdReportDesc$QueryUnit");

    digester.addCallMethod("report-desc/description", "setDescription", 0);

    digester.addCallMethod("report-desc/include", "addInclude", 0);

    digester.addObjectCreate("report-desc/queryUnit/fieldOverride",
        "com.insta.hms.common.StdReportDesc$FieldOverride");
    digester.addSetProperties("report-desc/queryUnit/fieldOverride/fieldOverides");
    digester.addSetNext("report-desc/queryUnit/fieldOverride/fieldOverides", "setFieldOverrides",
        "com.insta.hms.common.StdReportDesc$FieldOverride");

    digester.addObjectCreate("report-desc/fields/field",
        "com.insta.hms.common.StdReportDesc$Field");
    digester.addSetProperties("report-desc/fields/field");
    digester.addSetNext("report-desc/fields/field", "addField",
        "com.insta.hms.common.StdReportDesc$Field");

    digester.addCallMethod("report-desc/fields/field/value", "addValue", 0);
    digester.addCallMethod("report-desc/fields/field/value-query", "setAllowedValuesQuery", 0);

    digester.addCallMethod("report-desc/default-show/field", "addNewDefaultShowField", 0);

    /*
     * The following is called at the end of the default-show node, after all
     * addNewDefaultShowFields have been called. This starts using the fields added using
     * addNewDefaultShowField and overwrites the defaultShow=true specified in the fields. For
     * backward compatibility we need to retain and support the defaultShow=true attribute in the
     * field. Thus, if the default-show node does not exist, the old style will be used.
     */
    digester.addCallMethod("report-desc/default-show", "useNewDefaultShows");

  }

  /**
   * Instantiates a new std report desc xml provider.
   */
  public StdReportDescXmlProvider() {
    baseDir = AppInit.getRootRealPath();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportDescProvider#getReportDesc(java.lang.String)
   */
  public StdReportDesc getReportDesc(String descName) throws Exception {
    return getReportDescription(descName);
  }

  /**
   * Gets the report description.
   *
   * @param descName the desc name
   * @return the report description
   * @throws Exception the exception
   */
  public StdReportDesc getReportDescription(String descName) throws Exception {

    String fileName = baseDir;
    fileName = fileName + "/WEB-INF/srxml/" + descName;
    StdReportDesc desc = (StdReportDesc) digester.parse(new File(fileName));
    getReportIncludes(desc);
    updateCustomFields(desc);
    return desc;
  }

  /**
   * Gets the report desc for string.
   *
   * @param descName the desc name
   * @return the report desc for string
   * @throws Exception the exception
   */
  public StdReportDesc getReportDescForString(String descName) throws Exception {

    InputStream is = new ByteArrayInputStream(descName.getBytes("UTF-8"));
    StdReportDesc desc = (StdReportDesc) digester.parse(is);
    // cannot have includes in non-file based srxml
    // custom fields are not supported for custom reports.
    return desc;
  }

  /**
   * Gets the report includes.
   *
   * @param desc the desc
   * @throws Exception the exception
   */
  public void getReportIncludes(StdReportDesc desc) throws Exception {

    if (desc.getIncludes() != null && desc.getIncludes().size() > 0) {
      Map<String, Field> fields = new HashMap<String, Field>();
      /*
       * When fields in multiple includes and/or the main srxml exist, then, the fields in the
       * subsequent includes, and finally the main will take priority.
       */
      List<String> includes = desc.getIncludes();
      for (String include : includes) {
        fields.putAll(getReportDesc(include.trim()).getFields());
      }
      for (Map.Entry<String, StdReportDesc.Field> e : desc.getFields().entrySet()) {
        StdReportDesc.Field field = e.getValue();
        if (field.getDisplayName() != null && !field.getDisplayName().equals("")) {
          // the field desc in the main srxml can override/remove the one in the include
          fields.put(e.getKey(), field);
        } else {
          fields.remove(e.getKey());
        }
      }
      desc.setFields(fields);

      /**
       * If the report contains query units with Includes, then we append the joinTables of the
       * Include at the point of specification (specified via the joinInclude marker) or towards the
       * end of the joinTables list if not marked.
       */
      List<StdReportDesc.QueryUnit> qu = new ArrayList<StdReportDesc.QueryUnit>();
      List<StdReportDesc.JoinTable> newJoinTableList = new ArrayList<StdReportDesc.JoinTable>();
      List<String> includesAdded = new ArrayList();
      qu = desc.getQueryUnits();
      if (qu != null && !qu.isEmpty()) {
        for (StdReportDesc.QueryUnit q : qu) {
          newJoinTableList = new ArrayList<StdReportDesc.JoinTable>();
          List<StdReportDesc.JoinTable> joinTables = q.getJoinTables();
          for (StdReportDesc.JoinTable jt : joinTables) {
            newJoinTableList.add(jt);
            if (jt.getIsInclude()) {
              newJoinTableList.addAll(
                  (getReportDesc(jt.getIncludeName()).getQueryUnits().get(0).getJoinTables()));
              includesAdded.add(jt.getIncludeName());
            }

            for (String include : includes) {
              if (!includesAdded.contains(include)) {
                newJoinTableList
                    .addAll((getReportDesc(include).getQueryUnits().get(0).getJoinTables()));
              }
            }

            q.setJoinTables(removeDuplicateJoins(newJoinTableList));
          }
        }
        desc.setQueryUnits(qu);
      }

    }

  }

  /**
   * Removes the duplicate joinss.
   *
   * @param list the list
   * @return the list
   */
  public static List<StdReportDesc.JoinTable> removeDuplicateJoinss(
      List<StdReportDesc.JoinTable> list) {
    List<StdReportDesc.JoinTable> dupList = new ArrayList<StdReportDesc.JoinTable>(list);
    Set<StdReportDesc.JoinTable> set = new HashSet<StdReportDesc.JoinTable>();
    List<StdReportDesc.JoinTable> newList = new ArrayList<StdReportDesc.JoinTable>();
    for (Iterator iter = dupList.iterator(); iter.hasNext();) {
      StdReportDesc.JoinTable element = (StdReportDesc.JoinTable) iter.next();
      if (set.add(element)) {
        newList.add(element);
      }
    }
    dupList.clear();
    dupList.addAll(newList);
    return dupList;
  }

  /**
   * Removes the duplicate joins.
   *
   * @param list the list
   * @return the list
   */
  public static List<StdReportDesc.JoinTable> removeDuplicateJoins(
      List<StdReportDesc.JoinTable> list) {
    if (list == null || list.isEmpty()) {
      return list;
    }
    List<StdReportDesc.JoinTable> dupsremoved = new ArrayList<StdReportDesc.JoinTable>();
    Map<String, String> addedJoins = new HashMap<String, String>();
    for (int i = 0; i < list.size(); i++) {
      JoinTable joinTable = list.get(i);
      if (!addedJoins.containsKey(joinTable.getAlias())) {
        dupsremoved.add(joinTable);
        addedJoins.put(joinTable.getAlias(), joinTable.getName());
      }
    }
    return dupsremoved;
  }

  /**
   * Update custom fields.
   *
   * @param desc the desc
   * @return the std report desc
   * @throws SQLException the SQL exception
   */
  public StdReportDesc updateCustomFields(StdReportDesc desc) throws SQLException {

    boolean modAdvInsurance = false;

    HttpSession session = RequestContext.getSession();
    Preferences prefs = null;
    if (session == null) {
      Connection con = DataBaseUtil.getConnection();
      PreferencesDao prefsDao = new PreferencesDao(con);
      prefs = prefsDao.getPreferences();
    } else {
      prefs = (Preferences) session.getAttribute("preferences");
    }
    Map groups = prefs.getModulesActivatedMap();

    if (groups.containsKey("mod_adv_ins") && "Y".equals(groups.get("mod_adv_ins"))) {
      modAdvInsurance = true;
    }

    BasicDynaBean regBean = new RegistrationPreferencesDAO().getRecord();
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();

    HashMap<String, Object> customNamesMap = new HashMap<String, Object>();

    customNamesMap.put("custom_field1", regBean.get("custom_field1_label"));
    customNamesMap.put("custom_field2", regBean.get("custom_field2_label"));
    customNamesMap.put("custom_field3", regBean.get("custom_field3_label"));

    customNamesMap.put("custom_field4", regBean.get("custom_field4_label"));
    customNamesMap.put("custom_field5", regBean.get("custom_field5_label"));

    customNamesMap.put("custom_field6", regBean.get("custom_field6_label"));
    customNamesMap.put("custom_field7", regBean.get("custom_field7_label"));
    customNamesMap.put("custom_field8", regBean.get("custom_field8_label"));
    customNamesMap.put("custom_field9", regBean.get("custom_field9_label"));
    customNamesMap.put("custom_field10", regBean.get("custom_field10_label"));

    customNamesMap.put("custom_field11", regBean.get("custom_field11_label"));
    customNamesMap.put("custom_field12", regBean.get("custom_field12_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field13_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field14_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field15_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field16_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field17_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field18_label"));
    customNamesMap.put("custom_field13", regBean.get("custom_field19_label"));
    customNamesMap.put("custom_list1_value", regBean.get("custom_list1_name"));
    customNamesMap.put("custom_list2_value", regBean.get("custom_list2_name"));
    customNamesMap.put("custom_list3_value", regBean.get("custom_list3_name"));
    customNamesMap.put("custom_list4_value", regBean.get("custom_list4_name"));
    customNamesMap.put("custom_list5_value", regBean.get("custom_list5_name"));
    customNamesMap.put("custom_list6_value", regBean.get("custom_list6_name"));
    customNamesMap.put("custom_list7_value", regBean.get("custom_list7_name"));
    customNamesMap.put("custom_list8_value", regBean.get("custom_list8_name"));
    customNamesMap.put("custom_list9_value", regBean.get("custom_list9_name"));

    customNamesMap.put("family_id", regBean.get("family_id"));
    customNamesMap.put("member_id", regBean.get("member_id_label"));
    customNamesMap.put("policy_validity_start", regBean.get("member_id_valid_from_label"));
    customNamesMap.put("policy_validity_end", regBean.get("member_id_valid_to_label"));

    customNamesMap.put("government_identifier", regBean.get("government_identifier_label"));
    customNamesMap.put("identifier_id", regBean.get("government_identifier_type_label"));

    customNamesMap.put("passport_no", regBean.get("passport_no"));
    customNamesMap.put("passport_issue_country", regBean.get("passport_issue_country"));
    customNamesMap.put("passport_validity", regBean.get("passport_validity"));
    customNamesMap.put("visa_validity", regBean.get("visa_validity"));

    for (int i = 1; i <= 5; i++) {
      setDocCustomFields(customNamesMap, "" + i, (String) genPrefs.get("doctors_custom_field" + i));
    }

    if ((Integer) genPrefs.get("max_centers_inc_default") == 1) {
      customNamesMap.put("center_name", "");
      customNamesMap.put("bill_center_name", "");
      customNamesMap.put("from_center_name", "");
      customNamesMap.put("to_center_name", "");
      customNamesMap.put("collection_center_name", "");
    }

    if (!modAdvInsurance) {
      customNamesMap.put("case_policy_validity_start", "");
      customNamesMap.put("case_policy_validity_end", "");
      customNamesMap.put("plan_name", "");
      customNamesMap.put("policy_validity_start", "");
      customNamesMap.put("policy_validity_end", "");
      customNamesMap.put("policy_holder_name", "");
      customNamesMap.put("patient_relationship", "");
    }

    List allFieldNames = desc.getFieldNames();
    for (int i = 0; i < allFieldNames.size(); i++) {
      String fieldName = (String) allFieldNames.get(i);
      StdReportDesc.Field field = desc.getField(allFieldNames.get(i).toString());
      if (customNamesMap.containsKey(fieldName)) {
        if (customNamesMap.get(fieldName) == null || customNamesMap.get(fieldName).equals("")) {
          desc.removeField(fieldName);
        } else {
          field.setDisplayName(customNamesMap.get(fieldName).toString());
        }
      }
    }
    return desc;
  }

  /**
   * Sets the doc custom fields.
   *
   * @param namesMap  the names map
   * @param suffix    the suffix
   * @param fieldName the field name
   */
  private void setDocCustomFields(HashMap namesMap, String suffix, String fieldName) {
    if (fieldName != null && !fieldName.equals("")) {
      namesMap.put("dr_custom" + suffix, "Doc " + fieldName);
      namesMap.put("ref_custom" + suffix, "Ref Doc " + fieldName);
      namesMap.put("cond_doc_custom" + suffix, "Cond Doc " + fieldName);
      namesMap.put("pres_doc_custom" + suffix, "Pres Doc " + fieldName);
    } else {
      // have to put null, or the unused fields won't get removed
      namesMap.put("dr_custom" + suffix, null);
      namesMap.put("ref_custom" + suffix, null);
      namesMap.put("cond_doc_custom" + suffix, null);
      namesMap.put("pres_doc_custom" + suffix, null);
    }
  }

  /**
   * The Class ExcludeNullTransformer.
   */
  // transformer to ignore null fields
  public class ExcludeNullTransformer extends AbstractTransformer {

    /*
     * (non-Javadoc)
     * 
     * @see flexjson.transformer.AbstractTransformer#isInline()
     */
    public Boolean isInline() {
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see flexjson.transformer.Transformer#transform(java.lang.Object)
     */
    public void transform(Object object) {
      // Do nothing, null objects are not serialized.
      return;
    }
  }

  /**
   * Transformer to ignore empty lists.
   */
  public class ExcludeEmptyListTransformer extends AbstractTransformer {

    /*
     * (non-Javadoc)
     * 
     * @see flexjson.transformer.AbstractTransformer#isInline()
     */
    @Override
    public Boolean isInline() {
      return Boolean.TRUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see flexjson.transformer.Transformer#transform(java.lang.Object)
     */
    @Override
    public void transform(Object obj) {
      try {
        List lst = (ArrayList) obj;
        if (lst == null || lst.isEmpty()) {
          // Do nothing, null/empty lists are not serialized.
          return;
        } else {
          TypeContext typeContext = getContext().peekTypeContext();
          JSONContext context = getContext();
          // get the json path parsed till now:
          Path path = context.getPath();
          // if not the first object, write a comma
          if (!typeContext.isFirst()) {
            getContext().writeComma();
          }
          // output the last parsed object name (i.e. the list name)
          getContext().writeName(path.getPath().get(path.getPath().size() - 1));
          // start array output
          getContext().writeOpenArray();
          typeContext.setFirst(true);
          // comma concatenate and add list objects
          for (int i = 0; i < lst.size(); i++) {
            if (!typeContext.isFirst()) {
              getContext().writeComma();
            }
            typeContext.setFirst(false);
            getContext().transform(lst.get(i));
          }
          // close array output
          getContext().writeCloseArray();

        }

      } catch (Exception exception) {
        // ignore exceptions while serializing
        // the serializer will catch them later...
      }
    }
  }

  /**
   * Creates the report JSON file from srxml.
   *
   * @param desc     the desc
   * @param descName the desc name
   * @throws Exception the exception
   */
  public void createReportJSONFileFromSrxml(StdReportDesc desc, String descName) throws Exception {
    JSONSerializer js = new JSONSerializer().exclude("*.class").exclude("JoinInclude")
        .include("allowNoDate")
        .include("report-desc", "title", "description", "tableName", "query", "queryList",
            "queryParams", "queryUnits", "defaultOrder", "dateFields.*", "reportGroup",
            "allowNoDate", "defaultDate", "includes", "defaultShowFields", "newDefaultShowFields",
            "queryUnits.mainTableName", "queryUnits.mainTableAlias", "queryUnits.joinTables",
            "queryUnits.joinTables.type", "queryUnits.joinTables.expression",
            "queryUnits.joinTables.dependsOn", "queryUnits.joinTables.name",
            "queryUnits.joinTables.alias", "queryUnits.groupBy", "queryUnits.whereExpression",
            "queryUnits.fieldOverrides.*", "fields", "fields.*")
        .transform(new ExcludeNullTransformer(), void.class)
        .transform(new ExcludeEmptyListTransformer(), ArrayList.class).exclude("*")
        .prettyPrint(true);
    // serialize the descriptor
    String descJSON = js.deepSerialize(desc);

    // Remove the spaces added in excess while pretty printing...
    String[] toBeReplaced = { "\t", "\t\t", "\n\n", "\n\n\n", ".srxml" };
    String[] replacements = { "  ", "\t", "\n", " ", ".srjs" };
    for (int i = 0; i < toBeReplaced.length; i++) {
      descJSON = descJSON.replace(toBeReplaced[i], replacements[i]);
    }

    // Create the srjs directory, if it doesn't exist
    String dirName = baseDir + "/WEB-INF/srjs/";
    File dir = new File(dirName);
    dir.mkdirs();
    dir.setWritable(true);

    // create the new srjs file.
    String filename = dirName + descName.replace("srxml", "srjs");
    File actFile = new File(filename);
    if (!actFile.exists()) {
      actFile.createNewFile();
    }
    if (dir.canWrite() && actFile.exists() && !actFile.isDirectory()) {
      FileOutputStream fileStream = new FileOutputStream(actFile);
      fileStream.write(descJSON.getBytes());
      fileStream.flush();
      fileStream.close();
    }
  }

  /**
   * Function to convert all srxml files to srjs All srxml files can be converted by calling the
   * method: new StdReportDescXmlProvider().convertAllSrxmlFilesToJson();
   * Note: ignore the includes and custom fields processing while fetching the descriptor.
   * @throws Exception the exception
   */
  public void convertAllSrxmlFilesToJson() throws Exception {
    StdReportDesc desc;
    // Parse the struts-config and get all report names and their providers.
    Map<String, String[]> reportMetaData = StrutsDescParser.getsrXmlReportInfoMap();
    String[] reportKeySet = reportMetaData.keySet().toArray(new String[reportMetaData.size()]);

    List convertedReports = new ArrayList();

    // Get the descriptor for all reports and convert to srjs.
    for (int i = 0; i < reportKeySet.length; i++) {
      String reportActionId = reportKeySet[i];
      String[] reportInfo = StrutsDescParser.getStdReportInfo(reportActionId);

      String reportDesc = reportInfo[0];
      String providerClass = reportInfo[1];

      if (providerClass == null) {
        providerClass = "com.insta.hms.common.StdReportDescXmlProvider";
      }

      Constructor cls = Class.forName(providerClass).getConstructor();
      StdReportDescProvider prov = (StdReportDescProvider) cls.newInstance();
      desc = prov.getReportDesc(reportDesc);

      createReportJSONFileFromSrxml(desc, reportDesc);
      convertedReports.add(reportDesc);
    }

    // To convert srxmls (i.e includes) not specified in struts

    String fileName = baseDir;
    fileName = fileName + "/WEB-INF/srxml/";
    File dir = new File(fileName);
    dir.mkdirs();
    dir.setWritable(true);

    // Parse the srxml directory
    if (dir.isDirectory()) {
      File[] allSrxmls = dir.listFiles();
      for (int i = 0; i < allSrxmls.length; i++) {
        File srxmlFile = allSrxmls[i];
        if (srxmlFile != null && srxmlFile.isFile() && srxmlFile.exists()) {
          // If the srxml file has not been converted to srjs, convert it
          if (!convertedReports.contains(srxmlFile.getName())
              && srxmlFile.getName().contains(".srxml")) {
            String providerClass = "com.insta.hms.common.StdReportDescXmlProvider";
            Constructor kls = Class.forName(providerClass).getConstructor();
            StdReportDescProvider provider = (StdReportDescProvider) kls.newInstance();
            desc = provider.getReportDesc(srxmlFile.getName());

            createReportJSONFileFromSrxml(desc, srxmlFile.getName());

            convertedReports.add(srxmlFile.getName());
          }
        }
      }
    }

  }

}
