package com.insta.hms.core.insurance;

import com.insta.hms.common.UrlUtil;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ClaimValidationHelper {

  private HashMap urlActionMap;

  private String path;

  public HashMap getUrlActionMap() {
    return urlActionMap;
  }

  public void setUrlActionMap(HashMap urlActionMap) {
    this.urlActionMap = urlActionMap;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Generates the clickable url based on the error message type.
   *
   * @param type the type
   * @param id   the id
   * @param name the name
   * @return the string
   */
  public String urlString(String type, String id, String name) {

    String url = "";
    path = path + "/";
    String targetStr = "<b><a target='_blank' href='";
    String endStr = "</a></b>";

    if (type.equals("diagnosis") || type.equals("drg")) {
      url = (String) urlActionMap.get("update_mrd");
      url = targetStr + path + url + "?_method=getMRDUpdateScreen&patient_id=" + id + "'>" + id
          + endStr;
    } else if (type.equals("bill")) {
      url = (String) urlActionMap.get("credit_bill_collection");
      url = targetStr + path + url + "?_method=getCreditBillingCollectScreen&billNo=" + id + "'>"
          + id + endStr;
    } else if (type.equals("claim")) {
      url = (String) urlActionMap.get("insurance_claim_reconciliation");
      url = targetStr + path + url + "?_method=getClaimBillsActivities&claim_id=" + id + "'>" + id
          + endStr;
    } else if (type.equals("attachment")) {
      url = (String) urlActionMap.get("insurance_claim_reconciliation");
      url = targetStr + path + url + "?_method=addOrEditAttachment&claim_id=" + id + "'>" + id
          + endStr;
    } else if (type.equals("doctor")) {
      url = (String) urlActionMap.get("mas_doctors_detail");
      url = targetStr + path + url + "?_method=getDoctorDetailsScreen&mode=update&doctor_id=" + id
          + "'>" + name + endStr;
    } else if (type.equals("referral")) {
      url = (String) urlActionMap.get("mas_ref_doctors");
      url = targetStr + path + url + "?_method=show&referal_no=" + id + "'>" + name + endStr;
    } else if (type.equals("patient")) {
      url = (String) urlActionMap.get("edit_visit_details");
      url = targetStr + path + url + "?_method=getPatientVisitDetails&ps_status=all&patient_id="
          + id + "'>" + id + endStr;
    } else if (type.equals("account-group")) {
      url = UrlUtil.buildURL("accounting_group_master", UrlUtil.SHOW_URL_VALUE,
          "account_group_id=" + id, null, id);
      url = targetStr + url + "'>" + name + " Group</a></b>";
    } else if (type.equals("center-name")) {
      url = UrlUtil.buildURL("mas_centers", UrlUtil.SHOW_URL_VALUE, "center_id=" + id, null, id);
      url = targetStr + url + "'>" + name + " Center</a></b>";
    } else if (type.equals("submission")) {
      url = (String) urlActionMap.get("insurance_claim_reconciliation");
      url = targetStr + path + url + "?_method=list&status=&submission_batch_id=" + id + "'>" + id
          + endStr;
    } else if (type.equals("pre-registration")) {
      url = (String) urlActionMap.get("reg_general");
      url = targetStr + path + url + "?_method=show&regType=regd&mr_no=" + id + "&mrno=" + id + "'>"
          + id + endStr;
    } else if (type.equals("drug")) {
      url = (String) urlActionMap.get("pharma_sale_edit_bill");
      url = targetStr + path + url + "?_method=getSaleDetails&sale_item_id=" + id + "'>" + name
          + endStr;
    } else if (type.equals("adt")) {
      url = (String) urlActionMap.get("adt");
      url = targetStr + path + url
          + "?_method=getADTScreen&_searchMethod=getADTScreen&mr_no%40op=ilike&_actionId=adt&mr_no="
          + id + "'>" + id + endStr;
    } else if (type.equals("bill-remittance")) {
      url = (String) urlActionMap.get("bill_remittance");
      url = targetStr + path + url + "?_method=getBillRemittance&billNo=" + id + "'>" + name
          + endStr;
    } else if (type.equals("ins-remittance")) {
      url = (String) urlActionMap.get("ins_remittance_xl");
      url = targetStr + path + url + "?_method=show&remittance_id=" + id + "'>" + name + endStr;
    } else if (type.equals("insurance")) {
      url = (String) urlActionMap.get("change_visit_tpa");
      url = targetStr + path + url + "?_method=changeTpa&visitId=" + id + "'>" + id + endStr;
    } else if (type.equals("sponsor")) {
      url = (String) urlActionMap.get("mas_ins_tpas");
      url = targetStr + path + url + "?_method=show&tpa_id=" + id + "'>" + name + endStr;
    } else if (type.equals("company")) {
      url = (String) urlActionMap.get("mas_insurance_comp");
      url = targetStr + path + url + "?_method=show&insurance_co_id=" + id + "'>" + name + endStr;
    }
    return url;
  }
}
