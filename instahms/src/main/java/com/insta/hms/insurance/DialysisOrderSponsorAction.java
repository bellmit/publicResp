package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.DialysisOrderDao;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class DialysisOrderSponsorAction.
 */
public class DialysisOrderSponsorAction extends DispatchAction {

  /** The dav spon dao. */
  static DavitaSponsorDAO davSponDao = new DavitaSponsorDAO();

  /** The visit DAO. */
  static VisitDetailsDAO visitDAO = new VisitDetailsDAO();

  /**
   * Gets the dialysis order sponsor amounts.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the dialysis order sponsor amounts
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws Exception        the exception
   */
  public ActionForward getDialysisOrderSponsorAmounts(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, Exception {
    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String mrNo = request.getParameter("mr_no");
    String visitId = request.getParameter("visit_id");

    String visitOrgId = request.getParameter("visitOrgId");

    final String type = request.getParameter("type");
    final String chargeType = request.getParameter("chargeType");
    final String visitType = "o"; // In dialysis order case it is 'o' always.

    String orgId = visitOrgId;

    final String bedType = "GENERAL";
    if (orgId == null || orgId.isEmpty()) {
      orgId = "ORG0001";
    }
    String mainVisitId = null;

    BasicDynaBean mainVisitBean = visitDAO.getMainVisitOfCurrentMonth(mrNo);
    BasicDynaBean visitDetBean = visitDAO.findByKey("patient_id", visitId);

    mainVisitId = mainVisitBean != null ? (String) mainVisitBean.get("main_visit_id") : null;
    if (mainVisitId == null) {
      mainVisitId = visitDetBean != null ? (String) visitDetBean.get("main_visit_id") : null;
    }
    String itemId = request.getParameter("itemId");
    BasicDynaBean ratePlanBean = getRatePlanBean(mrNo, mainVisitId, itemId);
    String newlyAddedApprovalLmtValues = request.getParameter("newlyAddedApprovalLmtValues");
    String[] newlyAddedApprovalLmtValuesList = newlyAddedApprovalLmtValues.split(",");
    String serviceGrp = request.getParameter("serviceGrp");
    String newlyAddedItemDetailsIds = request.getParameter("newlyAddedApprovalDetailsIds");
    String[] newlyAddedItemDetailIdsList = newlyAddedItemDetailsIds.split(",");
    BasicDynaBean aprvlLtsBean = getApprovalSponsorLimits(mrNo, serviceGrp, itemId, mainVisitId,
        newlyAddedItemDetailIdsList, newlyAddedApprovalLmtValuesList);
    int[] planIds = new int[1];
    planIds[0] = (Integer) (new PlanMasterDAO()
        .findPlan("plan_name", "Default Insurance Company Plan").get("plan_id"));
    // Take item level/approval level orgId if exists else take the visit level orgId
    orgId = (aprvlLtsBean != null) && ((String) aprvlLtsBean.get("org_id") != null
        || !((String) aprvlLtsBean.get("org_id")).equals("")) ? (String) aprvlLtsBean.get("org_id")
            : orgId;
    // if item doesn't have approval limit, then get its rate from the highest priority rate
    // plan for the item
    if (aprvlLtsBean == null && ratePlanBean != null) {
      orgId = (String) ratePlanBean.get("org_id");
    }
    BigDecimal orderQty = new BigDecimal((String) request.getParameter("quantity"));
    Map rateMap = davSponDao.getApplicableRates(orgId, itemId, bedType, type, chargeType, visitType,
        orderQty, true, planIds, visitId, false);
    BigDecimal orderAmt = ((BigDecimal) rateMap.get("item_rate")).multiply(orderQty);

    BasicDynaBean consumBean = null;

    if (aprvlLtsBean != null) {
      consumBean = davSponDao.getConsumedQtyOrAmt(aprvlLtsBean, mainVisitId);
    }

    BigDecimal aprvdQtyOrAmt = BigDecimal.ZERO;
    BigDecimal remQtyOrAmt = BigDecimal.ZERO;
    String aprvlType = null;
    String copayType = null;
    BigDecimal copayPerOrAmt = BigDecimal.ZERO;
    Map<String, Object> chgMap = new HashMap<String, Object>();
    Integer approvalDetId = 0;

    if (aprvlLtsBean != null) {
      approvalDetId = (Integer) aprvlLtsBean.get("sponsor_approval_detail_id");
      // get the remaining qty or amt
      aprvdQtyOrAmt = (BigDecimal) aprvlLtsBean.get("limit_value");
      aprvlType = (String) aprvlLtsBean.get("limit_type");

      if (consumBean != null) {
        if (aprvlType.equals("Q")) {
          remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal) consumBean.get("used_qty"));
        } else {
          remQtyOrAmt = aprvdQtyOrAmt.subtract((BigDecimal) consumBean.get("used_amt"));
        }
      } else {
        remQtyOrAmt = aprvdQtyOrAmt;
      }

      // Get the remaining Quantity after newly added items
      for (int i = 0; i < newlyAddedItemDetailIdsList.length; i++) {
        Integer newItemDetailId = new Integer(newlyAddedItemDetailIdsList[i]);
        if (newItemDetailId == 0) {
          continue;
        }
        if (newItemDetailId.equals(approvalDetId)) {
          remQtyOrAmt = remQtyOrAmt.subtract(new BigDecimal(newlyAddedApprovalLmtValuesList[i]));
        }
      }

      // get the copay type and copay amount
      copayPerOrAmt = (BigDecimal) aprvlLtsBean.get("copay_value");
      copayType = (String) aprvlLtsBean.get("copay_type");

      // do the sponsor calculation
      /*
       * BasicDynaBean chargeBean = davSponDao.calculateSponsorAmount(itemId , remQtyOrAmt ,
       * orderQty , orderAmt , aprvlType , copayType , copayPerOrAmt); chgMap = chargeBean.getMap();
       */

    }
    BasicDynaBean chargeBean = davSponDao.calculateSponsorAmount(itemId, remQtyOrAmt, orderQty,
        orderAmt, aprvlType, copayType, copayPerOrAmt);

    chgMap = chargeBean.getMap();
    chgMap.put("approval_detail_id", approvalDetId);
    chgMap.put("limit_type", aprvlType == null ? "" : aprvlType);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.deepSerialize(chgMap));
    response.flushBuffer();

    return null;
  }

  /*
   * private BasicDynaBean getConsumedQtyOrAmt(String mainVisitId, String itemId) { // TODO
   * Auto-generated method stub Connection con = null; PreparedStatement ps = null; try { con =
   * DataBaseUtil.getConnection(); davSponDao.getConsumedQtyOrAmt(mainVisitId,) } finally {
   * 
   * } return null; }
   */

  /**
   * Gets the rate plan bean.
   *
   * @param mrNo        the mr no
   * @param mainVisitId the main visit id
   * @param itemId      the item id
   * @return the rate plan bean
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public BasicDynaBean getRatePlanBean(String mrNo, String mainVisitId, String itemId)
      throws SQLException, Exception {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return new DialysisOrderDao().getRatePlanBean(con, mrNo, mainVisitId, itemId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the approval sponsor limits.
   *
   * @param mrNo                            the mr no
   * @param serviceGrp                      the service grp
   * @param itemId                          the item id
   * @param mainVisitId                     the main visit id
   * @param newlyAddedItemDetailIdsList     the newly added item detail ids list
   * @param newlyAddedApprovalLmtValuesList the newly added approval lmt values list
   * @return the approval sponsor limits
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public BasicDynaBean getApprovalSponsorLimits(String mrNo, String serviceGrp, String itemId,
      String mainVisitId, String[] newlyAddedItemDetailIdsList,
      String[] newlyAddedApprovalLmtValuesList) throws SQLException, Exception {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return new DialysisOrderDao().getSponsorApprovalDetails(con, mrNo, serviceGrp, itemId,
          mainVisitId, newlyAddedItemDetailIdsList, newlyAddedApprovalLmtValuesList);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
}
