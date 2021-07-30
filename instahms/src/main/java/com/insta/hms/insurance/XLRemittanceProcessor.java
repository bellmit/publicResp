package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class XLRemittanceProcessor.
 */
public abstract class XLRemittanceProcessor extends RemittanceAdviceProcessor {

  /** The column map. */
  private Map<String, String> columnMap = new HashMap<String, String>();

  /**
   * Instantiates a new XL remittance processor.
   *
   * @param columnMap the column map
   */
  public XLRemittanceProcessor(Map<String, String> columnMap) {
    this.columnMap.clear();
    this.columnMap.putAll(columnMap);
  }

  /**
   * Instantiates a new XL remittance processor.
   */
  public XLRemittanceProcessor() {
    this.columnMap.clear();
  }

  /**
   * Gets the default column map.
   *
   * @param remittance the remittance
   * @return the default column map
   */
  protected Map<String, String> getDefaultColumnMap(BasicDynaBean remittance) {

    Map<String, String> defaultMap = new HashMap<String, String>();
    // Map<String, String> tpaColumnMap = null;

    // What was initialized is used, first
    if (null != columnMap && !columnMap.isEmpty()) {
      return Collections.unmodifiableMap(columnMap);
    }
    // TPA specific column map, next, if one exists
    /*
     * if (null != remittance) { tpaColumnMap =
     * getTPAColumnMap((String)remittance.get("insurance_co_id"), (String)remittance.get("tpa_id"));
     * }
     * 
     * if (null != tpaColumnMap && !tpaColumnMap.isEmpty()) { return tpaColumnMap; }
     */

    // Hard-coded default one, last
    defaultMap.put("amount_heading", "amount");
    defaultMap.put("denial_remarks_heading", "denial_remarks");
    defaultMap.put("payer_id_heading", "payer_id");
    defaultMap.put("payment_ref_type", "PerItem"); // This is the default
    defaultMap.put("payment_reference_heading", "payment_reference");
    defaultMap.put("claim_id_heading", "claim_id");

    defaultMap.put("detail_level", (String) remittance.get("detail_level"));
    defaultMap.put("tpa_id", (String) remittance.get("tpa_id"));

    return Collections.unmodifiableMap(defaultMap);
    /*
     * defaultMap.put("tpa_id", req.getParameter("tpa_id")); defaultMap.put("payment_ref_type",
     * req.getParameter("payment_ref_type"));
     * 
     * if (req.getParameter("payment_ref_type").equals("PerItem")) {
     * defaultMap.put("payment_reference_heading", req.getParameter("payment_reference_heading"));
     * }else { defaultMap.put("payment_reference", req.getParameter("payment_reference")); }
     * 
     * defaultMap.put("worksheet_index", req.getParameter("worksheet_index"));
     * defaultMap.put("detail_level", req.getParameter("detail_level")); return defaultMap;
     */
  }

  /**
   * Gets the TPA column map.
   *
   * @param insuranceId the insurance id
   * @param tpaId       the tpa id
   * @return the TPA column map
   */
  protected Map<String, String> getTPAColumnMap(String insuranceId, String tpaId) {
    return null; // TPA specific column maps not available right now
  }

  /**
   * Process.
   *
   * @param remittanceBean the remittance bean
   * @param rform          the rform
   * @param columnMap      the column map
   * @param errorMap       the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  public RemittanceAdvice process(BasicDynaBean remittanceBean, RemittanceForm rform,
      Map<String, String> columnMap, Map errorMap) throws Exception {
    return process(remittanceBean, rform, 1, columnMap, errorMap);
  }

  /**
   * Process.
   *
   * @param remittanceBean the remittance bean
   * @param rform          the rform
   * @param errorMap       the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  public RemittanceAdvice process(BasicDynaBean remittanceBean, RemittanceForm rform, Map errorMap)
      throws Exception {
    return process(remittanceBean, rform, 1, errorMap);
  }

  /**
   * Process.
   *
   * @param remittanceBean the remittance bean
   * @param rform          the rform
   * @param workSheetIndex the work sheet index
   * @param errorMap       the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  public RemittanceAdvice process(BasicDynaBean remittanceBean, RemittanceForm rform,
      int workSheetIndex, Map errorMap) throws Exception {
    return process(remittanceBean, rform, workSheetIndex, null, errorMap);
  }

  /**
   * Process.
   *
   * @param remittanceBean the remittance bean
   * @param rform          the rform
   * @param workSheetIndex the work sheet index
   * @param columnMap      the column map
   * @param errorMap       the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  public RemittanceAdvice process(BasicDynaBean remittanceBean, RemittanceForm rform,
      int workSheetIndex, Map<String, String> columnMap, Map errorMap) throws Exception {

    if (null == remittanceBean) {
      errorMap.put("error", "Missing remittance information: Please provide insurance company,"
          + " TPA, received date and account group information");
      return null;
    }

    columnMap = this.columnMap;

    if (null == columnMap || columnMap.isEmpty()) {
      columnMap = getDefaultColumnMap(remittanceBean);
    }

    int selectedSheetNo = (workSheetIndex > 0) ? (workSheetIndex - 1) : 0;

    HSSFWorkbook workBook = new HSSFWorkbook(rform.getRemittance_metadata().getInputStream());

    XLRemittanceProvider xlRemittanceProvider = getXLRemittanceProvider();
    RemittanceAdvice advice = xlRemittanceProvider.getRemittanceAdvice(columnMap, workBook,
        selectedSheetNo, errorMap);

    if (null == advice) {
      if (errorMap.get("error") != null
          && ((String) errorMap.get("error")).startsWith("Column Not Found")) {
        String invalidColumn = ((String) errorMap.get("error")).split(":")[1].trim();
        // TODO : Need to put back the complete error message
        String error = "Required columns are not found (or) incorrect column names.<br/>"
            + "Please check the Excel file <b>" + rform.getRemittance_metadata().getFileName()
            + "</b> sheet number : <b>" + (selectedSheetNo + 1) + "</b> .<br/>";
        // +"Column Not Found "+req.getParameter(invalidColumn+"_lbl")+" : <b>
        // "+req.getParameter(invalidColumn)+"</b>";
        errorMap.put("error", error);
      }
    }

    return advice;
  }

  /**
   * Gets the XL remittance provider.
   *
   * @return the XL remittance provider
   */
  public abstract XLRemittanceProvider getXLRemittanceProvider();

}
