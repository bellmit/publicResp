package com.insta.hms.scheduledreport;

import com.insta.hms.common.AppInit;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class StrutsDescParser {
  
  /** The sr xml report info map. */
  /*
   * Report info Maps: 
   * key --> action-id of struts-config 
   * value --> an array comprising of report
   * information The value specification of each of these maps is as below: 
   * srXmlReportInfoMap -->
   * (SrXml Report Descriptor, Desc. Provider) 
   * jrXmlReportInfoMap --> (Report Type, Report Name)
   * ftlReportInfoMap --> (Report Type, Report Name) allReportsInfoMap --> (Report Type, Report
   * Name, [Provider])
   */
  private static Map<String, String[]> srXmlReportInfoMap = null;
  
  /** The jr xml report info map. */
  private static Map<String, String[]> jrXmlReportInfoMap = null;
  
  /** The ftl report info map. */
  private static Map<String, String[]> ftlReportInfoMap = null;
  
  /** The all reports info map. */
  private static Map<String, String[]> allReportsInfoMap = null;

  /** The Constant JRXML_REPORTS_PROVIDER. */
  private static final String JRXML_REPORTS_PROVIDER = "com.insta.hms.common.CommonReportAction";
  
  /** The Constant FTL_REPORTS_PROVIDER. */
  private static final String FTL_REPORTS_PROVIDER = "com.insta.hms.common.ftl.FtlReportAction";
  
  /** The Constant SRXML_REPORTS_PROVIDER. */
  private static final String SRXML_REPORTS_PROVIDER = 
      "com.insta.hms.common.StdReportDescXmlProvider";

  /**
   * Inits the all maps.
   *
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void initAllMaps() throws SAXException, ParserConfigurationException, IOException {
    if (allReportsInfoMap == null) {
      String strutsFile = AppInit.getRootRealPath() + "/WEB-INF/struts-config.xml";
      getReportInfoMap(strutsFile);
    }
  }

  /**
   * Gets the std report info.
   *
   * @param actionId the action id
   * @return the std report info
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String[] getStdReportInfo(String actionId)
      throws SAXException, ParserConfigurationException, IOException {
    initAllMaps();
    return srXmlReportInfoMap.get(actionId);
  }

  /**
   * Gets the sr xml report info map.
   *
   * @return the sr xml report info map
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Map getsrXmlReportInfoMap()
      throws SAXException, ParserConfigurationException, IOException {
    initAllMaps();
    return srXmlReportInfoMap;
  }

  /**
   * Gets the jr xml report info map.
   *
   * @return the jr xml report info map
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Map getjrXmlReportInfoMap()
      throws SAXException, ParserConfigurationException, IOException {
    initAllMaps();
    return jrXmlReportInfoMap;
  }

  /**
   * Gets the ftl report info map.
   *
   * @return the ftl report info map
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Map getFtlReportInfoMap()
      throws SAXException, ParserConfigurationException, IOException {
    initAllMaps();
    return ftlReportInfoMap;
  }

  /**
   * Gets the report info map.
   *
   * @param strutsFile the struts file
   * @throws SAXException the SAX exception
   * @throws ParserConfigurationException the parser configuration exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void getReportInfoMap(String strutsFile)
      throws SAXException, ParserConfigurationException, IOException {

    srXmlReportInfoMap = new HashMap<String, String[]>();
    jrXmlReportInfoMap = new HashMap<String, String[]>();
    ftlReportInfoMap = new HashMap<String, String[]>();
    allReportsInfoMap = new HashMap<String, String[]>();

    // Get the DOM of the struts-config
    File strutsConfigFile = new File(strutsFile);
    Digester digester = new Digester();
    digester.setValidating(false);
    /*
     * disable validating the struts config(it is already validated when starting tomcat.) against
     * the dtd. if u dont include the below features, it will checks against the dtd, this will fail
     * when client machine doen't have the internet connection.
     */
    digester.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    digester.addObjectCreate("struts-config", StrutsConfig.class);

    // A new Action instance for the action tag
    digester.addObjectCreate("struts-config/action-mappings/action", Action.class);

    // Set the attribute values as properties
    digester.addSetProperties("struts-config/action-mappings/action");

    digester.addCallMethod("struts-config/action-mappings/action/set-property", "setProperty", 2);
    digester.addCallParam("struts-config/action-mappings/action/set-property", 0, "key");
    digester.addCallParam("struts-config/action-mappings/action/set-property", 1, "value");

    digester.addSetNext("struts-config/action-mappings/action", "addAction");
    // Parse the XML file to get an Struts-Config instance
    StrutsConfig config = (StrutsConfig) digester.parse(strutsConfigFile);

  }

  /**
   * The Class StrutsConfig.
   */
  public static class StrutsConfig {

    /**
     * Adds the action.
     *
     * @param action the action
     */
    public void addAction(Action action) {

      String type = action.getType();
      Map properties = action.getProperty();
      String actionId = (String) properties.get("action_id");
      String reportName = (String) properties.get("report-name");
      String reportDesc = (String) properties.get("report_desc");
      String descProvider = (String) properties.get("desc_provider");
      if (actionId != null) {
        if (type != null && type.equals(JRXML_REPORTS_PROVIDER)) {
          jrXmlReportInfoMap.put(actionId, new String[] { "jrxml", reportName });
          allReportsInfoMap.put(actionId, new String[] { "jrxml", reportName, "" });
        } else if (type != null && type.equals(FTL_REPORTS_PROVIDER)) {
          ftlReportInfoMap.put(actionId, new String[] { "ftl", reportName });
          allReportsInfoMap.put(actionId, new String[] { "ftl", reportName, "" });
        }
      }
      if (reportDesc != null) {
        // there is a report_desc, which means it is a report. But don't rely
        // on report_desc to be non-empty. Purely class based providers don't need a
        // report_desc
        srXmlReportInfoMap.put(actionId, new String[] { reportDesc, descProvider });
        allReportsInfoMap.put(actionId, new String[] { "srxml", reportDesc,
            descProvider == null ? SRXML_REPORTS_PROVIDER : descProvider });
      }

    }

  }

  /**
   * The Class Action.
   */
  public static class Action {
    
    /** The type. */
    public String type;
    
    /** The keys. */
    public Map keys = new HashMap();

    /**
     * Sets the property.
     *
     * @param key the key
     * @param value the value
     */
    public void setProperty(String key, String value) {
      keys.put(key, value);
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public Map getProperty() {
      return keys;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
      this.type = type;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
      return type;
    }

  }

}
