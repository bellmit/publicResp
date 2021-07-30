package com.insta.hms.insurance;

import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.stores.PurchaseOrderDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SponsorAction.
 */
public class SponsorAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(SponsorAction.class);

  /** The pip dao. */
  final PatientInsurancePlanDAO pipDao = new PatientInsurancePlanDAO();
  
  private static final GenericDAO visitInsuranceDetailsViewDAO =
      new GenericDAO("visit_insurance_details_view");

  /**
   * Gets the bill charge claims.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the bill charge claims
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward getBillChargeClaims(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String visitId = request.getParameter("visitID");
    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    String[] chargeIds = request.getParameterValues("chargeId");
    String[] chargeHeadIds = request.getParameterValues("chargeHeadId");
    String[] amt = request.getParameterValues("amt");
    String[] taxAmt = taxAmt = request.getParameterValues("original_tax_amt");
    if (null == taxAmt) {
      taxAmt = request.getParameterValues("tax_amt");
    }
    String[] returnAmt = request.getParameterValues("returnAmt");
    String[] disc = request.getParameterValues("disc");
    String[] edited = request.getParameterValues("edited");
    String[] insuranceCategoryIds = request.getParameterValues("insuranceCategoryId");
    String[] deleted = request.getParameterValues("delCharge");
    String[] isClaimLocked = request.getParameterValues("isClaimLocked");
    String[] priInsClaimAmt = request.getParameterValues("priInsClaimAmt");
    String[] secInsClaimAmt = request.getParameterValues("secInsClaimAmt");
    String[] priInsClaimTaxAmt = request.getParameterValues("priInsClaimTaxAmt");
    String[] secInsClaimTaxAmt = request.getParameterValues("secInsClaimTaxAmt");
    String[] chargeGroupIds = request.getParameterValues("chargeGroupId");
    String[] priIncludeInClaimCalc = request.getParameterValues("priIncludeInClaim");
    String[] secIncludeInClaimCalc = request.getParameterValues("secIncludeInClaim");
    String[] packageIds = request.getParameterValues("packageId");
    String[] descriptionId = {};
    if (null != request.getParameterValues("descriptionId")) {
      descriptionId = request.getParameterValues("descriptionId");
    } else {
      descriptionId = request.getParameterValues("item_id");
    }

    String[] consultationTypeId = request.getParameterValues("consultation_type_id");
    String[] opId = request.getParameterValues("op_id");

    List<Map<String, Object>> newCharges = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> editedCharges = new ArrayList<Map<String, Object>>();

    Map<String, List<BasicDynaBean>> subGrpCodesMap = new HashMap<String, List<BasicDynaBean>>();
    for (int i = 0; i < chargeIds.length; i++) {

      List<BasicDynaBean> subGrpCodes = new ArrayList<BasicDynaBean>();
      Map<String, Object> chargeMap = new HashMap<String, Object>();

      if ((chargeIds[i] == null) || chargeIds[i].equals("")) {
        continue;
      }

      if (chargeGroupIds[i].equals("MED") || chargeGroupIds[i].equals("RET")) {
        continue;
      }
      chargeMap.put("charge_id", chargeIds[i]);
      chargeMap.put("charge_head_id", chargeHeadIds[i]);
      chargeMap.put("charge_group_id", chargeGroupIds[i]);

      if (chargeGroupIds[i].equals("ITE")) {
        returnAmt[i] = (returnAmt[i] == null || returnAmt[i].equals("")) ? "0.00" : returnAmt[i];
        chargeMap.put("amount", new BigDecimal(amt[i]).add(new BigDecimal(returnAmt[i])));
      } else {
        chargeMap.put("amount", new BigDecimal(amt[i]));
      }

      if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
        chargeMap.put("amount", ((BigDecimal) chargeMap.get("amount")).add(new BigDecimal(
            (taxAmt[i] == null || (taxAmt[i].trim()).equals("")) ? "0.00" : taxAmt[i])));
      }

      chargeMap.put("discount", new BigDecimal(disc[i]));
      chargeMap.put("insurance_category_id", Integer.parseInt(insuranceCategoryIds[i]));

      Boolean chargeHeadPayable = isChargeHeadPayable(chargeHeadIds[i], chargeGroupIds[i]);
      chargeMap.put("is_insurance_payable", chargeHeadPayable);

      chargeMap.put("edited", edited[i]);
      chargeMap.put("is_claim_locked", isClaimLocked[i].equals("true"));
      chargeMap.put("store_item_category_payable", true);

      if (null != priInsClaimAmt) {
        chargeMap.put("primclaimAmt", priInsClaimAmt[i].equals("") ? "0" : priInsClaimAmt[i]);
      } else {
        chargeMap.put("primclaimAmt", "0");
      }

      if (null != priIncludeInClaimCalc && null != priIncludeInClaimCalc[i]) {
        chargeMap.put("pri_include_in_claim",
            priIncludeInClaimCalc[i].equals("") ? "Y" : priIncludeInClaimCalc[i]);
      }

      if (null != secInsClaimAmt && null != secInsClaimAmt[i]) {
        chargeMap.put("secclaimAmt", secInsClaimAmt[i].equals("") ? "0" : secInsClaimAmt[i]);
        if (null != secIncludeInClaimCalc && null != secIncludeInClaimCalc[i]) {
          chargeMap.put("sec_include_in_claim",
              secIncludeInClaimCalc[i].equals("") ? "Y" : secIncludeInClaimCalc[i]);
        }
      }

      chargeMap.put("descriptionId", descriptionId[i]);
      chargeMap.put("consultationTypeId", consultationTypeId[i]);
      chargeMap.put("op_id", opId[i]);
      chargeMap.put("package_id",
          StringUtils.isNotBlank(packageIds[i]) && StringUtils.isNumeric(packageIds[i])
              ? Integer.parseInt(packageIds[i])
              : null);

      if (null != priInsClaimTaxAmt && !priInsClaimTaxAmt[i].equals("")) {
        chargeMap.put("priInsClaimTaxAmt", new BigDecimal(priInsClaimTaxAmt[i]));
      } else {
        chargeMap.put("priInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (null != secInsClaimTaxAmt && !secInsClaimTaxAmt[i].equals("")) {
        chargeMap.put("secInsClaimTaxAmt", new BigDecimal(secInsClaimTaxAmt[i]));
      } else {
        chargeMap.put("secInsClaimTaxAmt", BigDecimal.ZERO);
      }

      chargeMap.put("claim_amount_includes_tax", isClaimAmtIncludesTax);
      chargeMap.put("limit_includes_tax", isLimitIncludesTax);

      if (!chargeIds[i].startsWith("_") && !edited[i].equals("true")
          && !deleted[i].equals("true")) {
        continue;
      }

      if (chargeIds[i].startsWith("_")) {
        newCharges.add(chargeMap);
      } else if (edited[i].equals("true")) {
        if (deleted[i].equals("true")) {
          chargeMap.put("amount", BigDecimal.ZERO);
          chargeMap.put("discount", BigDecimal.ZERO);
          if (isClaimLocked[i].equals("true")) {
            chargeMap.put("primclaimAmt", "0");
            chargeMap.put("secclaimAmt", "0");
          }
        }
        editedCharges.add(chargeMap);
      }

      String prefix = chargeIds[i].startsWith("_") ? chargeIds[i].substring(1) : chargeIds[i];
      String[] subGroupIds = request.getParameterValues(prefix + "_sub_group_id");

      if (null != subGroupIds) {
        for (int k = 0; k < subGroupIds.length; k++) {
          BasicDynaBean subGrpBean = new BillChargeTaxDAO()
              .getMasterSubGroupDetails(Integer.parseInt(subGroupIds[k]));
          subGrpCodes.add(subGrpBean);
        }
      }

      subGrpCodesMap.put(chargeIds[i], subGrpCodes);
    }
    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<Integer, Map<Integer, Integer>>();
    Map<Integer, Object> sponsorTaxMap = new HashMap<Integer, Object>();

    Map<Integer, List<BasicDynaBean>> billChargeClaimsMap = new SponsorBO()
        .getHospitalSponosorAmount(newCharges, editedCharges, visitId, adjMap, sponsorTaxMap,
            subGrpCodesMap);

    Map<Integer, Map> billChargesMap = new HashMap<Integer, Map>();
    Map adjTaxMap = new HashMap<String, String>();
    for (Integer key : billChargeClaimsMap.keySet()) {
      // process the tax amount
      Map<String, Object> chargeAndSponsorTaxMap = (Map<String, Object>) sponsorTaxMap.get(key);
      List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);
      for (BasicDynaBean billChgBean : billChgList) {
        String chargeId = (String) billChgBean.get("charge_id");
        BigDecimal totSponsorTax = BigDecimal.ZERO;
        BigDecimal sponsorAmount = (BigDecimal) billChgBean.get("insurance_claim_amt");
        totSponsorTax = (BigDecimal) billChgBean.get("tax_amt") == null ? BigDecimal.ZERO
            : (BigDecimal) billChgBean.get("tax_amt");
        Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>) chargeAndSponsorTaxMap
            .get(chargeId);
        String adjTaxAmt = "N";
        if (sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
          totSponsorTax = BigDecimal.ZERO;
          sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null)
              ? (BigDecimal) sponsorTaxAndSplitMap.get("sponsorAmount")
              : (BigDecimal) billChgBean.get("insurance_claim_amt");

          Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>) sponsorTaxAndSplitMap
              .get("subGrpSponTaxDetailsMap");
          for (Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
            Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
            Map<String, String> subgrpTaxDetails = (Map<String, String>) subGrpTaxAmountsMap
                .getValue();
            totSponsorTax = totSponsorTax
                .add(new BigDecimal((String) subgrpTaxDetails.get("amount")));
            if (null != subgrpTaxDetails.get("adjTaxAmt")
                && subgrpTaxDetails.get("adjTaxAmt").equals("Y")) {
              adjTaxAmt = "Y";
            }
          }
        }
        billChgBean.set("insurance_claim_amt", sponsorAmount);
        billChgBean.set("tax_amt", totSponsorTax);
        adjTaxMap.put(chargeId, adjTaxAmt);
      }
    }

    for (Integer key : billChargeClaimsMap.keySet()) {
      List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);

      List listmap = ConversionUtils.listBeanToListMap(billChgList);

      billChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
    }

    billChargesMap.put(-1, adjMap);
    billChargesMap.put(-2, adjTaxMap);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.deepSerialize(billChargesMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Checks if is charge head payable.
   *
   * @param chargeHead the charge head
   * @param chargeGroup the charge group
   * @return the boolean
   * @throws SQLException the SQL exception
   */
  private Boolean isChargeHeadPayable(String chargeHead, String chargeGroup) throws SQLException {
    if (null != chargeGroup && "PKG".equals(chargeGroup)) {
      chargeHead = "PKGPKG";
    }
    BasicDynaBean chargeHeadBean = new GenericDAO("chargehead_constants").findByKey("chargehead_id",
        chargeHead);
    Boolean isChargeHeadPayable = true;
    if (null != chargeHeadBean) {
      String chargeHeadPayable = (String) chargeHeadBean.get("insurance_payable");
      if (null != chargeHeadPayable && chargeHeadPayable.equals("N")) {
        isChargeHeadPayable = false;
      }
    }
    return isChargeHeadPayable;
  }
  
  private BasicDynaBean getChargeHeadBean(String chargeHead) throws SQLException {
    return new GenericDAO("chargehead_constants").findByKey("chargehead_id",
        chargeHead);
  }

  /**
   * Gets the medicine sales charge claims.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the medicine sales charge claims
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward getMedicineSalesChargeClaims(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String visitId = request.getParameter("visitId");
    String billType = request.getParameter("billType");
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    String[] chargeId = request.getParameterValues("temp_charge_id");
    String[] itemCode = request.getParameterValues("itemCode");
    String[] origRate = request.getParameterValues("origRate");
    String[] batchNo = request.getParameterValues("batchNo");
    String[] qty = request.getParameterValues("qty");
    String[] medDisc = request.getParameterValues("medDiscRS");
    String[] medDiscType = request.getParameterValues("medDiscType");
    String[] amt = request.getParameterValues("amt");
    String[] orgTaxAmt = request.getParameterValues("orgTaxAmt");
    String[] taxAmt = request.getParameterValues("tax");
    String[] primclaimAmt = request.getParameterValues("primclaimAmt");
    String[] secclaimAmt = request.getParameterValues("secclaimAmt");
    String[] insuranceCategoryId = request.getParameterValues("insuranceCategoryId");
    String[] itemBatchId = request.getParameterValues("itemBatchId");
    String[] isClaimLocked = request.getParameterValues("is_claim_locked");
    String[] priIncludeInClaimCalc = request.getParameterValues("priIncludeInClaim");
    String[] secIncludeInClaimCalc = request.getParameterValues("secIncludeInClaim");
    String[] medicineId = request.getParameterValues("medicineId");
    String[] priInsClaimTaxAmt = request.getParameterValues("priInsClaimTaxAmt");
    String[] secInsClaimTaxAmt = request.getParameterValues("secInsClaimTaxAmt");
    String[] itemExcludedFromDoc = request.getParameterValues("item_excluded_from_doctor");
    String[] itemExcludedFromDocRemarks = request
        .getParameterValues("item_excluded_from_doctor_remarks");

    Map<String, String[]> requestMap = new HashMap<String, String[]>(request.getParameterMap());
    List groupList = PurchaseOrderDAO.getAllGroups();
    Map<String, List<BasicDynaBean>> subGrpCodesMap = new HashMap<String, List<BasicDynaBean>>();

    int index = 0;
    List<Map<String, Object>> newCharges = new ArrayList<Map<String, Object>>();

    for (String chargeid : chargeId) {

      if (chargeid == null || chargeid.equals("")) {
        index++;
        continue;
      }
      List<BasicDynaBean> subGroupsList = new ArrayList<BasicDynaBean>();
      List<String> subGroupIds = new ArrayList<String>();
      for (int j = 0; j < groupList.size(); j++) {
        BasicDynaBean groupBean = (BasicDynaBean) groupList.get(j);
        int groupId = groupBean.get("item_group_id") != null
            ? (Integer) groupBean.get("item_group_id")
            : 0;
        if (requestMap.get("taxsubgroupid" + groupId) != null
            && requestMap.get("taxsubgroupid" + groupId)[index] != null
            && !requestMap.get("taxsubgroupid" + groupId)[index].isEmpty()) {
          String taxSubgroupId = requestMap.get("taxsubgroupid" + groupId)[index];
          subGroupIds.add(taxSubgroupId);
        }
      }
      Iterator<String> subGroupsIdIterator = subGroupIds.iterator();
      while (subGroupsIdIterator.hasNext()) {
        String subGroupId = subGroupsIdIterator.next();
        if (null != subGroupId && !subGroupId.trim().isEmpty()) {
          // BasicDynaBean subGrpBean = new
          // GenericDAO("item_sub_groups").
          // findByKey("item_subgroup_id",Integer.parseInt(subGroupId));
          BasicDynaBean subGrpBean = new BillChargeTaxDAO()
              .getMasterSubGroupDetails(Integer.parseInt(subGroupId));
          subGroupsList.add(subGrpBean);
        }
      }
      subGrpCodesMap.put(chargeid, subGroupsList);

      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal taxAmount = BigDecimal.ZERO;
      BigDecimal amountWithTax = BigDecimal.ZERO;
      amount = new BigDecimal(amt[index]).subtract(new BigDecimal(taxAmt[index]));
      if (null != orgTaxAmt && null != orgTaxAmt[index]) {
        amountWithTax = amount.add(new BigDecimal(orgTaxAmt[index]));
      }
      taxAmount = new BigDecimal(taxAmt[index]);
      // amount = amount.subtract(taxAmount);
      // salesMap.put("charge_id", "_"+(i+1)); // nothing but sale_item_id
      Map<String, Object> salesMap = new HashMap<String, Object>();
      salesMap.put("charge_id", chargeId[index]);
      salesMap.put("charge_group_id", "MED");
      salesMap.put("item_code", itemCode[index]);
      salesMap.put("rate", origRate[index]);
      salesMap.put("batch_no", batchNo[index]);
      salesMap.put("quantity", qty[index]);
      salesMap.put("discount", medDisc[index]);
      salesMap.put("discount_type", medDiscType[index]);
      salesMap.put("primclaimAmt", primclaimAmt[index].equals("") ? "0" : primclaimAmt[index]);
      salesMap.put("secclaimAmt", secclaimAmt[index].equals("") ? "0" : secclaimAmt[index]);
      salesMap.put("insurance_category_id", insuranceCategoryId[index]);
      salesMap.put("item_batch_id", itemBatchId[index]);
      // TODO : this should come from the
      // page
      salesMap.put("is_insurance_payable", Boolean.TRUE);
      salesMap.put("is_claim_locked", new Boolean(isClaimLocked[index]));
      salesMap.put("store_item_category_payable", true);
      salesMap.put("item_excluded_from_doctor", itemExcludedFromDoc[index]);
      salesMap.put("item_excluded_from_doctor_remarks", itemExcludedFromDocRemarks[index]);
      if (null != priInsClaimTaxAmt && !priInsClaimTaxAmt[index].equals("")) {
        salesMap.put("priInsClaimTaxAmt", new BigDecimal(priInsClaimTaxAmt[index]));
      } else {
        salesMap.put("priInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (null != secInsClaimTaxAmt && !secInsClaimTaxAmt[index].equals("")) {
        salesMap.put("secInsClaimTaxAmt", new BigDecimal(secInsClaimTaxAmt[index]));
      } else {
        salesMap.put("secInsClaimTaxAmt", BigDecimal.ZERO);
      }

      salesMap.put("claim_amount_includes_tax", isClaimAmtIncludesTax);
      salesMap.put("limit_includes_tax", isLimitIncludesTax);

      if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
        salesMap.put("amount", String.valueOf(amountWithTax));
      } else {
        salesMap.put("amount", String.valueOf(amount));
      }

      salesMap.put("primclaimAmt", primclaimAmt[index].equals("") ? "0" : primclaimAmt[index]);
      if (null != priIncludeInClaimCalc && null != priIncludeInClaimCalc[index]) {
        salesMap.put("pri_include_in_claim",
            priIncludeInClaimCalc[index].equals("") ? "Y" : priIncludeInClaimCalc[index]);
      }

      if (null != secclaimAmt && null != secclaimAmt[index]) {
        salesMap.put("secclaimAmt", secclaimAmt[index].equals("") ? "0" : secclaimAmt[index]);
        if (null != secIncludeInClaimCalc && null != secIncludeInClaimCalc[index]) {
          salesMap.put("sec_include_in_claim",
              secIncludeInClaimCalc[index].equals("") ? "Y" : secIncludeInClaimCalc[index]);
        }
      }

      if (billType.equalsIgnoreCase("BN") || billType.equalsIgnoreCase("BN-I")) {
        salesMap.put("charge_head_id", "PHMED");
      } else {
        salesMap.put("charge_head_id", "PHCMED");
      }

      salesMap.put("descriptionId", medicineId[index]);
      newCharges.add(salesMap);
      index++;
    }

    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<Integer, Map<Integer, Integer>>();
    Map<Integer, Object> sponsorTaxMap = new HashMap<Integer, Object>();

    Map<Integer, List<BasicDynaBean>> saleItemClaimsMap = new SponsorBO()
        .getPharmacySponosorAmount(newCharges, visitId, adjMap, sponsorTaxMap, subGrpCodesMap);
    Map adjTaxMap = new HashMap<String, String>();
    for (Integer key : saleItemClaimsMap.keySet()) {
      // process the tax amount
      Map<String, Object> chargeAndSponsorTaxMap = (Map<String, Object>) sponsorTaxMap.get(key);
      List<BasicDynaBean> saleChgList = saleItemClaimsMap.get(key);
      for (BasicDynaBean saleChgBean : saleChgList) {
        String chargeIdStr = (String) saleChgBean.get("charge_id");
        BigDecimal totSponsorTax = BigDecimal.ZERO;
        BigDecimal sponsorAmount = (BigDecimal) saleChgBean.get("insurance_claim_amt");
        totSponsorTax = (BigDecimal) saleChgBean.get("tax_amt") == null ? BigDecimal.ZERO
            : (BigDecimal) saleChgBean.get("tax_amt");
        Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>) chargeAndSponsorTaxMap
            .get(chargeIdStr);
        String adjTaxAmt = "N";
        if (sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
          totSponsorTax = BigDecimal.ZERO;
          sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null)
              ? (BigDecimal) sponsorTaxAndSplitMap.get("sponsorAmount")
              : (BigDecimal) saleChgBean.get("insurance_claim_amt");

          Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>) sponsorTaxAndSplitMap
              .get("subGrpSponTaxDetailsMap");
          for (Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
            Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
            Map<String, String> subgrpTaxDetails = (Map<String, String>) subGrpTaxAmountsMap
                .getValue();
            totSponsorTax = totSponsorTax
                .add(new BigDecimal((String) subgrpTaxDetails.get("amount")));
            if (null != subgrpTaxDetails.get("adjTaxAmt")
                && subgrpTaxDetails.get("adjTaxAmt").equals("Y")) {
              adjTaxAmt = "Y";
            }
          }
        }
        saleChgBean.set("insurance_claim_amt", sponsorAmount);
        saleChgBean.set("tax_amt", ConversionUtils.setScale(totSponsorTax));
        adjTaxMap.put(chargeIdStr, adjTaxAmt);
      }
    }

    Map<Integer, Map> saleChargesMap = new HashMap<Integer, Map>();

    for (Integer key : saleItemClaimsMap.keySet()) {
      List<BasicDynaBean> saleChgList = saleItemClaimsMap.get(key);

      List listmap = ConversionUtils.listBeanToListMap(saleChgList);

      saleChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
      for (BasicDynaBean bean : saleChgList) {
        log.info(bean.get("charge_id") + "  " + bean.get("insurance_claim_amt") + "  " + "  "
            + bean.get("insurance_category_id"));
      }
    }

    saleChargesMap.put(-2, adjTaxMap);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.deepSerialize(saleChargesMap));
    response.flushBuffer();

    // response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(newCharges)));
    return null;
  }

  /**
   * Gets the issues charge claims.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the issues charge claims
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward getIssuesChargeClaims(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String visitId = request.getParameter("visitId");
    String billType = request.getParameter("billType");
    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    String[] chargeId = request.getParameterValues("temp_charge_id");
    String[] medDisc = request.getParameterValues("discountAmtHid");
    String[] orgTaxAmt = request.getParameterValues("original_tax");
    String[] taxAmt = request.getParameterValues("tax_amt");
    String[] amt = request.getParameterValues("amt");
    String[] priPatIncClaimAmt = request.getParameterValues("pri_ins_amt");
    String[] secPatIncClaimAmt = request.getParameterValues("sec_ins_amt");
    String[] insurancecategory = request.getParameterValues("insurancecategory");
    String[] hdeleted = request.getParameterValues("hdeleted");
    String[] storeItemCategories = request.getParameterValues("category");
    String[] priInsClaimTaxAmt = request.getParameterValues("pri_ins_tax");
    String[] secInsClaimTaxAmt = request.getParameterValues("sec_ins_tax");
    String[] medicineId = request.getParameterValues("medicine_id");

    Map<String, String[]> requestMap = new HashMap<String, String[]>(request.getParameterMap());
    List groupList = PurchaseOrderDAO.getAllGroups();
    Map<String, List<BasicDynaBean>> subGrpCodesMap = new HashMap<String, List<BasicDynaBean>>();

    int chargeIndex = 0;
    List<Map<String, Object>> newCharges = new ArrayList<Map<String, Object>>();

    for (String chargeid : chargeId) {

      if (chargeid == null || chargeid.equals("") || hdeleted[chargeIndex].equals("true")) {
        chargeIndex++;
        continue;
      }
      List<BasicDynaBean> subGroupsList = new ArrayList<BasicDynaBean>();
      List<String> subGroupIds = new ArrayList<String>();
      for (int groupIndex = 0; groupIndex < groupList.size(); groupIndex++) {
        BasicDynaBean groupBean = (BasicDynaBean) groupList.get(groupIndex);
        int groupId = groupBean.get("item_group_id") != null
            ? (Integer) groupBean.get("item_group_id")
            : 0;
        if (requestMap.get("taxsubgroupid" + groupId) != null
            && requestMap.get("taxsubgroupid" + groupId)[chargeIndex] != null
            && !requestMap.get("taxsubgroupid" + groupId)[chargeIndex].isEmpty()) {
          String taxSubgroupId = requestMap.get("taxsubgroupid" + groupId)[chargeIndex];
          subGroupIds.add(taxSubgroupId);
        }
      }
      Iterator<String> subGroupsIdIterator = subGroupIds.iterator();
      while (subGroupsIdIterator.hasNext()) {
        String subGroupId = subGroupsIdIterator.next();
        if (null != subGroupId && !subGroupId.trim().isEmpty()) {
          // BasicDynaBean subGrpBean = new
          // GenericDAO("item_sub_groups").
          // findByKey("item_subgroup_id",Integer.parseInt(subGroupId));
          BasicDynaBean subGrpBean = new BillChargeTaxDAO()
              .getMasterSubGroupDetails(Integer.parseInt(subGroupId));
          subGroupsList.add(subGrpBean);
        }
      }
      subGrpCodesMap.put(chargeid, subGroupsList);
      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal taxAmount = BigDecimal.ZERO;
      BigDecimal amountWithTax = BigDecimal.ZERO;
      amount = new BigDecimal(amt[chargeIndex]).subtract(new BigDecimal(taxAmt[chargeIndex]));
      amountWithTax = amount.add(new BigDecimal(orgTaxAmt[chargeIndex]));
      taxAmount = new BigDecimal(taxAmt[chargeIndex]);

      Map<String, Object> issueMap = new HashMap<String, Object>();

      issueMap.put("claim_amount_includes_tax", isClaimAmtIncludesTax);
      issueMap.put("limit_includes_tax", isLimitIncludesTax);
      issueMap.put("charge_id", chargeId[chargeIndex]);
      issueMap.put("charge_head_id", "INVITE");
      issueMap.put("charge_group_id", "ITE");
      issueMap.put("amount", amt[chargeIndex]);
      issueMap.put("primclaimAmt",
          priPatIncClaimAmt[chargeIndex].equals("") ? "0" : priPatIncClaimAmt[chargeIndex]);
      issueMap.put("secclaimAmt",
          secPatIncClaimAmt[chargeIndex].equals("") ? "0" : secPatIncClaimAmt[chargeIndex]);
      issueMap.put("discount", medDisc[chargeIndex].equals("") ? "0" : medDisc[chargeIndex]);
      issueMap.put("insurance_category_id", insurancecategory[chargeIndex]);
      // TODO : this should come from the
      // page
      issueMap.put("is_insurance_payable", Boolean.TRUE);
      issueMap.put("is_claim_locked", Boolean.FALSE);
      Boolean storeItemCategoryPayable = isStoreItemCatgeoryPayable(
          storeItemCategories[chargeIndex]);
      issueMap.put("store_item_category_payable", storeItemCategoryPayable);
      issueMap.put("pri_include_in_claim", true);
      issueMap.put("sec_include_in_claim", true);
      if (null != priInsClaimTaxAmt && !priInsClaimTaxAmt[chargeIndex].equals("")) {
        issueMap.put("priInsClaimTaxAmt", new BigDecimal(priInsClaimTaxAmt[chargeIndex]));
      } else {
        issueMap.put("priInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (null != secInsClaimTaxAmt && !secInsClaimTaxAmt[chargeIndex].equals("")) {
        issueMap.put("secInsClaimTaxAmt", new BigDecimal(secInsClaimTaxAmt[chargeIndex]));
      } else {
        issueMap.put("secInsClaimTaxAmt", BigDecimal.ZERO);
      }

      if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
        issueMap.put("amount", String.valueOf(amountWithTax));
      } else {
        issueMap.put("amount", String.valueOf(amount));
      }
      issueMap.put("descriptionId", medicineId[chargeIndex]);
      issueMap.put("tax_amt", taxAmount);

      newCharges.add(issueMap);
      chargeIndex++;
    }
    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<Integer, Map<Integer, Integer>>();
    Map<Integer, Object> sponsorTaxMap = new HashMap<Integer, Object>();
    Map<Integer, List<BasicDynaBean>> issueItemClaimsMap = new SponsorBO()
        .getPharmacySponosorAmount(newCharges, visitId, adjMap, sponsorTaxMap, subGrpCodesMap);
    Map adjTaxMap = new HashMap<String, String>();
    for (Integer key : issueItemClaimsMap.keySet()) {
      // process the tax amount
      Map<String, Object> chargeAndSponsorTaxMap = (Map<String, Object>) sponsorTaxMap.get(key);
      List<BasicDynaBean> saleChgList = issueItemClaimsMap.get(key);
      for (BasicDynaBean saleChgBean : saleChgList) {
        String chargeIdStr = (String) saleChgBean.get("charge_id");
        BigDecimal totSponsorTax = BigDecimal.ZERO;
        BigDecimal sponsorAmount = (BigDecimal) saleChgBean.get("insurance_claim_amt");
        totSponsorTax = (BigDecimal) saleChgBean.get("tax_amt") == null ? BigDecimal.ZERO
            : (BigDecimal) saleChgBean.get("tax_amt");
        Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>) chargeAndSponsorTaxMap
            .get(chargeIdStr);
        String adjTaxAmt = "N";
        if (sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
          totSponsorTax = BigDecimal.ZERO;
          sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null)
              ? (BigDecimal) sponsorTaxAndSplitMap.get("sponsorAmount")
              : (BigDecimal) saleChgBean.get("insurance_claim_amt");

          Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>) sponsorTaxAndSplitMap
              .get("subGrpSponTaxDetailsMap");
          for (Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
            Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
            Map<String, String> subgrpTaxDetails = (Map<String, String>) subGrpTaxAmountsMap
                .getValue();
            totSponsorTax = totSponsorTax
                .add(new BigDecimal((String) subgrpTaxDetails.get("amount")));
            if (null != subgrpTaxDetails.get("adjTaxAmt")
                && subgrpTaxDetails.get("adjTaxAmt").equals("Y")) {
              adjTaxAmt = "Y";
            }
          }
        }
        saleChgBean.set("insurance_claim_amt", sponsorAmount);
        saleChgBean.set("tax_amt", totSponsorTax);
        adjTaxMap.put(chargeIdStr, adjTaxAmt);
      }
    }
    Map<Integer, Map> issueChargesMap = new HashMap<Integer, Map>();

    for (Integer key : issueItemClaimsMap.keySet()) {
      List<BasicDynaBean> saleChgList = issueItemClaimsMap.get(key);

      List listmap = ConversionUtils.listBeanToListMap(saleChgList);

      issueChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
      for (BasicDynaBean bean : saleChgList) {
        log.info(bean.get("charge_id") + "  " + bean.get("insurance_claim_amt") + "  "
            + bean.get("insurance_category_id"));
      }
    }
    issueChargesMap.put(-2, adjTaxMap);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.deepSerialize(issueChargesMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Checks if is store item catgeory payable.
   *
   * @param storeItemCategory the store item category
   * @return the boolean
   * @throws SQLException the SQL exception
   */
  private Boolean isStoreItemCatgeoryPayable(String storeItemCategory) throws SQLException {
    BasicDynaBean storeCatBean = new GenericDAO("store_category_master").findByKey("category_id",
        Integer.parseInt(storeItemCategory));
    Boolean categoryClaimable = (Boolean) storeCatBean.get("claimable");
    return categoryClaimable;
  }

  /**
   * Gets the bill charge claims for order items.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the bill charge claims for order items
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward getBillChargeClaimsForOrderItems(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String visitId = request.getParameter("visitID");
    String regScreen = request.getParameter("regScreen");

    List<Map<String, Object>> newCharges = new ArrayList<Map<String, Object>>();

    // get tpa credentials
    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    BasicDynaBean sponsorDetailsBean = pipDao.getPrimarySponsorDetails(visitId);
    if (sponsorDetailsBean != null) {
      isClaimAmtIncludesTax = (String) sponsorDetailsBean.get("claim_amount_includes_tax");
      isLimitIncludesTax = (String) sponsorDetailsBean.get("limit_includes_tax");
    }

    String[] itemTypes = request.getParameterValues("type");
    String[] itemIds = request.getParameterValues("item_id");
    String[] chargehead = request.getParameterValues("chargeHead");
    String[] chargeGroupIds = request.getParameterValues("chargeGroupId");
    String[] insCategoryIds = request.getParameterValues("orderCategory");
    String[] amount = request.getParameterValues("orderAmount");
    String[] discount = request.getParameterValues("orderDiscount");
    String[] taxAmt = request.getParameterValues("orderTax");
    String[] newItems = request.getParameterValues("new");

    if (null != regScreen && regScreen.equals("Y")) {
      Map<String, Object> docChargeMap = new HashMap<String, Object>();
      setDoctorChargeMap(request, docChargeMap, newCharges);
    }

    if (null != itemTypes) {
      for (int i = 0; i < itemTypes.length; i++) {
        if (null != itemTypes[i] && !itemTypes[i].equals("")) {
          if (null != newItems && newItems[i].equals("Y")) {
            Map<String, Object> chargeMap = new HashMap<String, Object>();
            chargeMap.put("charge_id", "_" + (i + 1));
            chargeMap.put("charge_head_id", chargehead[i]);
            
            BasicDynaBean chargeHeadBean = getChargeHeadBean(chargehead[i]);
            String chargeGroup = (String)chargeHeadBean.get("chargegroup_id");
            chargeMap.put("charge_group_id", chargeGroup);
            
            chargeMap.put("amount",
                null != amount[i] && !amount[i].equals("") ? new BigDecimal(amount[i])
                    : BigDecimal.ZERO);

            if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
              chargeMap.put("amount",
                  null != amount[i] && !amount[i].equals("") ? new BigDecimal(amount[i])
                      : BigDecimal.ZERO);
            } else {
              chargeMap.put("amount",
                  ((BigDecimal) chargeMap.get("amount")).subtract(new BigDecimal(
                      (taxAmt[i] == null || (taxAmt[i].trim()).equals("")) ? "0.00" : taxAmt[i])));
            }

            chargeMap.put("discount",
                null != discount[i] && !discount[i].equals("") ? new BigDecimal(discount[i])
                    : BigDecimal.ZERO);
            chargeMap.put("insurance_category_id", Integer.parseInt(insCategoryIds[i]));
            Boolean chargeHeadPayable = isChargeHeadPayable(chargehead[i],
                chargeGroupIds == null ? null : chargeGroupIds[i]);
            chargeMap.put("is_insurance_payable", chargeHeadPayable);
            chargeMap.put("is_claim_locked", false);
            chargeMap.put("primclaimAmt", "0");
            chargeMap.put("secclaimAmt", "0");
            chargeMap.put("store_item_category_payable", true);
            chargeMap.put("pri_include_in_claim", true);
            chargeMap.put("sec_include_in_claim", true);
            chargeMap.put("descriptionId", itemIds[i]);
            if (itemTypes[i].equalsIgnoreCase("Doctor")) {
              String consTypeId = request.getParameterValues("doctor.head")[0];
              if (null != consTypeId && !consTypeId.isEmpty()) {
                chargeMap.put("consultationTypeId", consTypeId);
              } else {
                chargeMap.put("consultationTypeId", "0");
              }
            } else {
              chargeMap.put("consultationTypeId", "0");
            }
            chargeMap.put("claim_amount_includes_tax", isClaimAmtIncludesTax);
            chargeMap.put("limit_includes_tax", isLimitIncludesTax);
            newCharges.add(chargeMap);
          }
        }
      }
    }
    Map<Integer, Map<Integer, Integer>> adjMap = new HashMap<Integer, Map<Integer, Integer>>();
    Map<Integer, List<BasicDynaBean>> billChargeClaimsMap = 
        new HashMap<Integer, List<BasicDynaBean>>();
    Map<Integer, Object> sponsorTaxMap = new HashMap<Integer, Object>();
    if (null != regScreen && regScreen.equals("Y")) {
      List<BasicDynaBean> visitInsDetails = getVisitInsDetails(request);
      billChargeClaimsMap = new SponsorBO().getRegScreenOrderItemsSponosorAmount(newCharges,
          visitInsDetails, visitId, adjMap);
    } else {
      List<Map<String, Object>> editedCharges = new ArrayList<Map<String, Object>>();
      billChargeClaimsMap = new SponsorBO().getHospitalSponosorAmount(newCharges, editedCharges,
          visitId, adjMap, sponsorTaxMap, null);
    }
    Map adjTaxMap = new HashMap<String, String>();
    for (Integer key : billChargeClaimsMap.keySet()) {
      // process the tax amount
      Map<String, Object> chargeAndSponsorTaxMap = (Map<String, Object>) sponsorTaxMap.get(key);
      List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);
      for (BasicDynaBean billChgBean : billChgList) {
        String chargeId = (String) billChgBean.get("charge_id");
        BigDecimal totSponsorTax = BigDecimal.ZERO;
        BigDecimal sponsorAmount = (BigDecimal) billChgBean.get("insurance_claim_amt");
        totSponsorTax = (BigDecimal) billChgBean.get("tax_amt") == null ? BigDecimal.ZERO
            : (BigDecimal) billChgBean.get("tax_amt");
        Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>) chargeAndSponsorTaxMap
            .get(chargeId);
        String adjTaxAmt = "N";
        if (sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
          totSponsorTax = BigDecimal.ZERO;
          sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null)
              ? (BigDecimal) sponsorTaxAndSplitMap.get("sponsorAmount")
              : (BigDecimal) billChgBean.get("insurance_claim_amt");

          Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>) sponsorTaxAndSplitMap
              .get("subGrpSponTaxDetailsMap");
          for (Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
            Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
            Map<String, String> subgrpTaxDetails = (Map<String, String>) subGrpTaxAmountsMap
                .getValue();
            totSponsorTax = totSponsorTax
                .add(new BigDecimal((String) subgrpTaxDetails.get("amount")));
            if (null != subgrpTaxDetails.get("adjTaxAmt")
                && subgrpTaxDetails.get("adjTaxAmt").equals("Y")) {
              adjTaxAmt = "Y";
            }
          }
        }
        billChgBean.set("insurance_claim_amt", sponsorAmount);
        billChgBean.set("tax_amt", totSponsorTax);
        adjTaxMap.put(chargeId, adjTaxAmt);
      }
    }

    Map<Integer, Map> billChargesMap = new HashMap<Integer, Map>();

    for (Integer key : billChargeClaimsMap.keySet()) {
      List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);

      List listmap = ConversionUtils.listBeanToListMap(billChgList);

      billChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
    }

    billChargesMap.put(-1, adjMap);
    billChargesMap.put(-2, adjTaxMap);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.deepSerialize(billChargesMap));
    response.flushBuffer();

    return null;

  }

  /**
   * Sets the doctor charge map.
   *
   * @param request      the request
   * @param docChargeMap the doc charge map
   * @param newCharges   the new charges
   */
  private void setDoctorChargeMap(HttpServletRequest request, Map<String, Object> docChargeMap,
      List<Map<String, Object>> newCharges) {

    String chargeHead = request.getParameter("doc_chargehead");
    String insCatId = request.getParameter("doc_insCategoryId");
    String amount = request.getParameter("doc_amount");
    String discount = request.getParameter("doc_discount");

    if (null != chargeHead && !chargeHead.equals("") && null != insCatId && !insCatId.equals("")
        && null != amount && !amount.equals("")) {
      docChargeMap.put("charge_id", "_0");
      docChargeMap.put("charge_head_id", chargeHead);
      docChargeMap.put("amount",
          null != amount && !amount.equals("") ? new BigDecimal(amount) : BigDecimal.ZERO);
      docChargeMap.put("discount",
          null != discount && !discount.equals("") ? new BigDecimal(discount) : BigDecimal.ZERO);
      docChargeMap.put("insurance_category_id", Integer.parseInt(insCatId));
      // TODO : this should come from
      // the page
      docChargeMap.put("is_insurance_payable", Boolean.TRUE);
      docChargeMap.put("is_claim_locked", false);
      docChargeMap.put("primclaimAmt", "0");
      docChargeMap.put("secclaimAmt", "0");
      docChargeMap.put("pri_include_in_claim", true);
      docChargeMap.put("sec_include_in_claim", true);
      newCharges.add(docChargeMap);
    }

  }

  /**
   * Gets the visit ins details.
   *
   * @param request the request
   * @return the visit ins details
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> getVisitInsDetails(HttpServletRequest request) throws SQLException {

    List<BasicDynaBean> visitInsDetails = new ArrayList<BasicDynaBean>();
    setInsDetails(request, "primary", "P", visitInsDetails);
    setInsDetails(request, "secondary", "S", visitInsDetails);

    return visitInsDetails;
  }

  /**
   * Sets the ins details.
   *
   * @param request         the request
   * @param insType         the ins type
   * @param prefix          the prefix
   * @param visitInsDetails the visit ins details
   * @throws SQLException the SQL exception
   */
  private void setInsDetails(HttpServletRequest request, String insType, String prefix,
      List<BasicDynaBean> visitInsDetails) throws SQLException {

    String visitID = request.getParameter("visitID");
    String planId = request.getParameter(insType + "_plan_id");

    String opType = request.getParameter("op_type");
    if (null != opType && !opType.equals("") && (opType.equals("F") || opType.equals("D"))) {

      String prevVisitPlan = null;
      if (prefix.equals("P")) {
        prevVisitPlan = request.getParameter("gPreviousPlan");
      } else {
        prevVisitPlan = request.getParameter("gPreviousSecPlan");
      }

      planId = null != prevVisitPlan && !prevVisitPlan.equals("") ? prevVisitPlan : planId;
    }

    String planLimit = request.getParameter(insType + "_plan_limit");
    String visitLimit = request.getParameter(insType + "_visit_limit");
    String visitDeductible = request.getParameter(insType + "_visit_deductible");
    String visitCopay = request.getParameter(insType + "_visit_copay");
    String visitMaxCopay = request.getParameter(insType + "_max_copay");
    String perDayLimit = request.getParameter(insType + "_perday_limit");

    String includeFollowUp = request.getParameter(insType + "_limits_include_followUps");

    String[] categories = request.getParameterValues(prefix + "_cat_id");
    String[] itemDeductible = request.getParameterValues(prefix + "_item_deductible");
    String[] copayPercent = request.getParameterValues(prefix + "_copay_percent");
    String[] catDeductible = request.getParameterValues(prefix + "_cat_deductible");
    String[] sponsorLimit = request.getParameterValues(prefix + "_sponser_limit");
    String[] maxCopay = request.getParameterValues(prefix + "_max_copay");
    String[] insPayable = request.getParameterValues(prefix + "_ins_payable");

    BasicDynaBean planBean = new GenericDAO("insurance_plan_main").findByKey("plan_id",
        Integer.parseInt(planId));

    if (null != categories) {
      for (int i = 0; i < categories.length; i++) {
        BasicDynaBean bean = visitInsuranceDetailsViewDAO.getBean();

        bean.set("visit_id", visitID);
        bean.set("plan_id", Integer.parseInt(planId));
        bean.set("insurance_category_id", Integer.parseInt(categories[i]));
        bean.set("patient_amount",
            null != itemDeductible[i] && !itemDeductible[i].equals("")
                ? new BigDecimal(itemDeductible[i])
                : BigDecimal.ZERO);
        bean.set("patient_percent",
            null != copayPercent[i] && !copayPercent[i].equals("") ? new BigDecimal(copayPercent[i])
                : BigDecimal.ZERO);
        bean.set("patient_amount_cap",
            null != maxCopay[i] && !maxCopay[i].equals("") ? new BigDecimal(maxCopay[i])
                : BigDecimal.ZERO);
        bean.set("per_treatment_limit",
            null != sponsorLimit[i] && !sponsorLimit[i].equals("") ? new BigDecimal(sponsorLimit[i])
                : BigDecimal.ZERO);
        bean.set("patient_type", "o");
        bean.set("patient_amount_per_category",
            null != catDeductible[i] && !catDeductible[i].equals("")
                ? new BigDecimal(catDeductible[i])
                : BigDecimal.ZERO);

        String copayApplOnPostDiscountedAmt = isCopayApplOnPostDiscountedAmt(planBean);
        String limitType = getPlanLimitType(planBean);

        bean.set("is_copay_pc_on_post_discnt_amt", copayApplOnPostDiscountedAmt);
        bean.set("limit_type", limitType);

        bean.set("priority", prefix.equals("P") ? 1 : 2);

        bean.set("is_category_payable", insPayable[i].equals("Y"));

        bean.set("plan_limit",
            null != planLimit && !planLimit.equals("") ? new BigDecimal(planLimit)
                : BigDecimal.ZERO);
        bean.set("visit_limit",
            null != visitLimit && !visitLimit.equals("") ? new BigDecimal(visitLimit)
                : BigDecimal.ZERO);
        bean.set("visit_deductible",
            null != visitDeductible && !visitDeductible.equals("") ? new BigDecimal(visitDeductible)
                : BigDecimal.ZERO);
        bean.set("visit_copay_percentage",
            null != visitCopay && !visitCopay.equals("") ? new BigDecimal(visitCopay)
                : BigDecimal.ZERO);
        bean.set("visit_max_copay_percentage",
            null != visitMaxCopay && !visitMaxCopay.equals("") ? new BigDecimal(visitMaxCopay)
                : BigDecimal.ZERO);
        bean.set("visit_per_day_limit",
            null != perDayLimit && !perDayLimit.equals("") ? new BigDecimal(perDayLimit)
                : BigDecimal.ZERO);
        bean.set("limits_include_followup", includeFollowUp);

        String planCategoryPayable = isPlanCategoryPayable(Integer.parseInt(planId),
            Integer.parseInt(categories[i]));
        bean.set("plan_category_payable", planCategoryPayable);

        visitInsDetails.add(bean);
      }
    }
  }

  /**
   * Gets the plan limit type.
   *
   * @param planBean the plan bean
   * @return the plan limit type
   */
  private String getPlanLimitType(BasicDynaBean planBean) {
    String limitType = "C";

    if (null != planBean) {
      if (null != planBean.get("limit_type")) {
        limitType = (String) planBean.get("limit_type");
      }
    }
    return limitType;
  }

  /**
   * Checks if is plan category payable.
   *
   * @param planId     the plan id
   * @param categoryId the category id
   * @return the string
   * @throws SQLException the SQL exception
   */
  private String isPlanCategoryPayable(int planId, int categoryId) throws SQLException {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("plan_id", planId);
    keys.put("insurance_category_id", categoryId);
    keys.put("patient_type", "o");
    BasicDynaBean bean = new GenericDAO("insurance_plan_details").findByKey(keys);
    String planCategoryPayable = (String) bean.get("category_payable");
    return planCategoryPayable;
  }

  /**
   * Checks if is copay appl on post discounted amt.
   *
   * @param planBean the plan bean
   * @return the string
   * @throws SQLException the SQL exception
   */
  private String isCopayApplOnPostDiscountedAmt(BasicDynaBean planBean) throws SQLException {
    String copayApplOnPostDiscAmt = "Y";

    if (null != planBean) {
      if (null != planBean.get("is_copay_pc_on_post_discnt_amt")) {
        copayApplOnPostDiscAmt = (String) planBean.get("is_copay_pc_on_post_discnt_amt");
      }
    }
    return copayApplOnPostDiscAmt;
  }

}
