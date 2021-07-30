package com.insta.hms.insurance.sponsorreceipts;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class SponsorReceiptsAction.
 */
public class SponsorReceiptsAction extends DispatchAction {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(SponsorReceiptsAction.class);

  /** The sr DAO. */
  SponsorReceiptsDAO srDAO = new SponsorReceiptsDAO();

  /** The center dao. */
  CenterMasterDAO centerDao = new CenterMasterDAO();

  /**
   * Lists all the sponsor receipt as a filtered search result.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, Exception {
    String sponsorId = req.getParameter("sponsor_id");
    PagedList list = null;
    Integer maxCentersIncDefault =
        (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      req.setAttribute("centers", centerDao.getAllCenters());
    }
    req.setAttribute("visibilityStatus", false);
    if (sponsorId != null) {
      Map requestParams = new HashMap();
      requestParams.putAll(req.getParameterMap());
      list = srDAO.getSponsorReceiptsList(requestParams,
          ConversionUtils.getListingParameter(requestParams));
      req.setAttribute("visibilityStatus", true);// for list action list visibility is true.
      if (list.getDtoList().size() == 0) {
        req.setAttribute("visibilityStatus", false);
        req.setAttribute("error", "No pending bills.");
      }
    }
    req.setAttribute("pagedList", list);
    return mapping.findForward("list");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, Exception {
    boolean success = true;
    HttpSession session = RequestContext.getSession();
    AbstractPaymentDetails bpImpl =
        AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
    Connection con = null;
    Map params = req.getParameterMap();
    String sponsorId = req.getParameter("sponsor_id");
    String updateDetails = req.getParameter("updateDetails");
    String userid = (String) session.getAttribute("userid");
    String counterId = (session.getAttribute("billingcounterId") != null)
        ? (String) session.getAttribute("billingcounterId") : null;
    Integer maxCentersIncDefault =
        (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      req.setAttribute("centers", centerDao.getAllCenters());
    }
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BillDAO billDAO = new BillDAO(con);
      String[] consolidatedBillNo = (String[]) params.get("consolidated_bill_no");
      String[] recdAmt = (String[]) params.get("recdAmt");

      List<Receipt> receiptList = new ArrayList<Receipt>();
      Integer rptsCollId =
          DataBaseUtil.getIntValueFromDb("SELECT nextval('receipts_collection_seq')");
      Receipt receipt = null;
      if ((consolidatedBillNo != null)) {
        for (int billIndex = 0; billIndex < consolidatedBillNo.length; billIndex++) {
          String consBillNo = consolidatedBillNo[billIndex];
          BigDecimal recdAmtforMrNo = (BigDecimal) (recdAmt[billIndex].equals("")
              ? BigDecimal.ZERO : new BigDecimal(recdAmt[billIndex]));
          BigDecimal recdAmtforClaimTotal = recdAmtforMrNo;

          List<BasicDynaBean> bills = srDAO.getBillDetails(consBillNo);

          for (BasicDynaBean bean : bills) {
            // recdAmtforMr_no ==0 means, no amount need to update against the bills.
            if (recdAmtforMrNo.compareTo(BigDecimal.ZERO) == 0) {
              break;
            }
            final String billNo = (String) bean.get("bill_no");
            // create new receipt for bill.
            receipt = new Receipt();
            receipt.setSponsorId(sponsorId);
            receipt.setReceiptType("S");// for Receipt.PRIMARY_SPONSOR_SETTLEMENT
            receipt.setSponsorIndex("P");
            receipt.setPaymentType("S");
            if (receipt.getReceiptDate() == null) {
              receipt.setReceiptDate(DateUtil.getCurrentTimestamp());
            }
            receipt.setUsername(userid);
            receipt.setCounter(counterId);

            // get Patient amount for bill (total_claim -primary_total_sponsor_receipts)
            BigDecimal sponcorDueBill = ((BigDecimal) bean.get("total_claim"))
                .subtract((BigDecimal) bean.get("primary_total_sponsor_receipts"));
            if (sponcorDueBill.compareTo(BigDecimal.ZERO) == 0) {
              continue;
            }
            receipt.setBillNo(billNo);
            BigDecimal receiptAmt = recdAmtforMrNo.min(sponcorDueBill);
            receipt.setAmount(receiptAmt);
            recdAmtforMrNo = recdAmtforMrNo.subtract(receiptAmt);
            receiptList.add(receipt);
            Bill bill = billDAO.getBill(billNo);
            success = bpImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), "A",
                rptsCollId, userid);
            receiptList.clear();
          }
        }
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (null != updateDetails) {
      req.setAttribute("visibilityStatus", false);
    }
    req.setAttribute("pagedList", new PagedList());
    return mapping.findForward("list");
  }

}
