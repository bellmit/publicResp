package com.bob.hms.diag.incomingsamplependingbills;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class IncomingSamplePendingBillAction.
 *
 * @author lakshmi.p
 */
public class IncomingSamplePendingBillAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(IncomingSamplePendingBillAction.class);

  /**
   * Gets the incoming sample pending bills list.
   *
   * @param am      the am
   * @param form    the form
   * @param request the request
   * @param res     the res
   * @return the incoming sample pending bills list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIncomingSamplePendingBillsList(ActionMapping am, ActionForm form,
      HttpServletRequest request, HttpServletResponse res) throws SQLException, ParseException {
    IncomingSamplePendingBillForm incomingSampleForm = (IncomingSamplePendingBillForm) form;
    String billNo = null;
    String patientName = null;
    String hospName = null;
    String patOtherInfo = null;

    if ((incomingSampleForm.getBillNo() != null) && !incomingSampleForm.getBillNo().equals("")) {
      billNo = incomingSampleForm.getBillNo();
    }

    if ((incomingSampleForm.getPatName() != null) && !incomingSampleForm.getPatName().equals("")) {
      patientName = incomingSampleForm.getPatName();
    }

    if ((incomingSampleForm.getLabName() != null) && !incomingSampleForm.getLabName().equals("")) {
      hospName = incomingSampleForm.getLabName();
    }

    if ((incomingSampleForm.getPatOtherInfo() != null)
        && !incomingSampleForm.getPatOtherInfo().equals("")) {
      patOtherInfo = incomingSampleForm.getPatOtherInfo();
    }

    ArrayList status = null;
    if (!incomingSampleForm.isStatusAll()) {
      status = new ArrayList();
      if (incomingSampleForm.isStatusOpen()) {
        status.add(Bill.BILL_STATUS_OPEN);
      }
      if (incomingSampleForm.isStatusClosed()) {
        status.add(Bill.BILL_STATUS_CLOSED);
      }
    }

    ArrayList type = null;
    if (!incomingSampleForm.isTypeAll()) {
      type = new ArrayList();
      if (incomingSampleForm.isTypeBillNow()) {
        type.add(Bill.BILL_TYPE_PREPAID);
      }
      if (incomingSampleForm.isTypeBillLater()) {
        type.add(Bill.BILL_TYPE_CREDIT);
      }
    }

    int pageNum = 1;
    if (incomingSampleForm.getPageNum() != null && !incomingSampleForm.getPageNum().equals("")) {
      pageNum = Integer.parseInt(incomingSampleForm.getPageNum());
    }

    String formSort = incomingSampleForm.getSortOrder();
    String formSortRep = null;
    if (formSort != null) {
      if (formSort.equals("billno")) {
        formSort = "isr.billno";
        formSortRep = "billno";
      }
      if (formSort.equals("patientname")) {
        formSort = "isr.patient_name";
      }
      if (formSort.equals("hospname")) {
        formSort = "ih.hospital_name";
      }

    }

    String requestCenter = request.getSession(false).getAttribute("centerId").toString();

    if (request.getParameter("center_id") != null
        && !request.getParameter("center_id").equals("")) {
      requestCenter = request.getParameter("center_id");
    }

    String category = am.getProperty("category");
    JSONSerializer js = new JSONSerializer().exclude("class");
    java.sql.Date fromDate = DataBaseUtil.parseDate(incomingSampleForm.getFdate());
    java.sql.Date toDate = DataBaseUtil.parseDate(incomingSampleForm.getTdate());
    PagedList pagedList = IncomingSamplePendingBillDAO.getIncomingSamplePendingBillsList(billNo,
        status, type, pageNum, patientName, hospName, fromDate, toDate, formSort,
        incomingSampleForm.getSortReverse(), category, patOtherInfo, requestCenter);
    request.setAttribute("incomingHospitalJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("incoming_hospitals").listAll(null, "status", "A", "hospital_name"))));
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("category", category);
    request.setAttribute("centers", new CenterMasterDAO().listAll());
    request.setAttribute("requestCenter", requestCenter);

    return am.findForward("getIncomingSamplePendingBillScreen");
  }

  /**
   * Gets the sample pending bills.
   *
   * @param am      the am
   * @param form    the form
   * @param request the request
   * @param res     the res
   * @return the sample pending bills
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getSamplePendingBills(ActionMapping am, ActionForm form,
      HttpServletRequest request, HttpServletResponse res) throws SQLException, ParseException {

    String billNo = request.getParameter("billno");
    String incomingVisitId = request.getParameter("incomingvisitid");

    BillBO bo = new BillBO();
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
    request.setAttribute("pref", printPref);

    BasicDynaBean patient = IncomingSamplePendingBillDAO.getIncomingPatientDetails(incomingVisitId);
    request.setAttribute("patientDetails", patient);

    String category = request.getParameter("category");
    request.setAttribute("sampleDetails",
        IncomingSamplePendingBillDAO.getSampleDetailsList(billNo, category));

    request.setAttribute("billNo", billNo);
    BillDetails billDetails = bo.getBillDetails(billNo);
    request.setAttribute("billDetails", billDetails);

    return am.findForward("getIncomingPendingBillCollectionScreen");
  }

  /**
   * Collect sample bill payments.
   *
   * @param am      the am
   * @param form    the form
   * @param request the request
   * @param res     the res
   * @return the action forward
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public ActionForward collectSampleBillPayments(ActionMapping am, ActionForm form,
      HttpServletRequest request, HttpServletResponse res)
      throws SQLException, ParseException, IOException {

    AbstractPaymentDetails ipdImpl = AbstractPaymentDetails
        .getReceiptImpl(AbstractPaymentDetails.INCOMING_PAYMENT);
    List<Receipt> receiptList = null;
    Map<String, String[]> requestParams = request.getParameterMap();
    Map printParamMap = null;
    FlashScope flash = FlashScope.getScope(request);

    IncomingSamplePendingBillForm incomingSampleform = (IncomingSamplePendingBillForm) form;
    HttpSession session = request.getSession();
    String username = (String) session.getAttribute("userid");
    String category = am.getProperty("category");

    BillBO bo = new BillBO();
    Connection con = null;
    boolean success = true;
    String message = null;

    String billNo = incomingSampleform.getBillNo();
    Bill bill = bo.getBill(billNo);

    boolean close = incomingSampleform.isClose();
    String action = incomingSampleform.getAction();
    String printerTypeStr = request.getParameter("printer");

    if ("Print".equals(action)) {
      printParamMap = new HashMap();
      printParamMap.put("printerTypeStr", printerTypeStr);

      printParamMap.put("incomingVisitId", incomingSampleform.getPatientId());
      printParamMap.put("billType", bill.getBillType());
      printParamMap.put("billNo", billNo);
      printParamMap.put("category", category);
      printParamMap.put("BILLPRINT", "Y");

      List<String> printURLs = ipdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
      request.getSession(false).setAttribute("printURLs", printURLs);

      ActionRedirect redirect = new ActionRedirect(am.findForward("billPaymentSuccess"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("billno", billNo);
      redirect.addParameter("category", category);
      redirect.addParameter("incomingvisitid", incomingSampleform.getPatientId());

      return redirect;

    } else if ("Reopen".equals(action)) {
      success = IncomingSamplePendingBillDAO
          .checkTestsConductedInBill(incomingSampleform.getBillNo());
      if (success) {
        success = new BillBO().reopenBill(incomingSampleform.getBillNo(),
            incomingSampleform.getBillRemarks(), username, null);
        if (success) {
          message = "Bill reopened successfully";
        } else {
          message = "Cannot reopen bill. (Maybe one Bill Later bill is active?)";
        }
      } else {
        message = "Cannot reopen bill. Test(s) in the bill are conducted ";
      }
      ActionRedirect redirect = new ActionRedirect(am.findForward("billPaymentSuccess"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      flash.info(message);
      redirect.addParameter("billno", billNo);
      redirect.addParameter("category", category);
      redirect.addParameter("incomingvisitid", incomingSampleform.getPatientId());
      return redirect;

    } else {

      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);

        BillDAO bdao = new BillDAO(con);
        IncomingSamplePendingBillDAO isdao = new IncomingSamplePendingBillDAO(con);

        receiptList = ipdImpl.processReceiptParams(requestParams);

        if (receiptList != null && receiptList.size() > 0) {
          success = ipdImpl.createReceipts(con, receiptList, bill, bill.getVisitType(),
              bill.getStatus());
          if (success) {
            printParamMap = new HashMap();
            printParamMap.put("printerTypeStr", printerTypeStr);

            printParamMap.put("incomingVisitId", incomingSampleform.getPatientId());
            printParamMap.put("billType", bill.getBillType());
            printParamMap.put("billNo", billNo);
            printParamMap.put("category", category);

            List<String> printURLs = ipdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
            request.getSession(false).setAttribute("printURLs", printURLs);
          }
        }

        if (success && close) {

          bill.setBillRemarks(incomingSampleform.getBillRemarks());
          success = success && bdao.updateBill(bill);
          if (success) {
            success = success && isdao.updateBillStatus(con, billNo, username);
          }
        }

      } finally {
        DataBaseUtil.commitClose(con, success);
      }

      if (success && close) {
        ActionRedirect redirect = new ActionRedirect(am.findForward("billPaymentFailure"));
        redirect.addParameter("msg", message);
        redirect.addParameter("category", category);
        return redirect;

      } else {
        ActionRedirect redirect = new ActionRedirect(am.findForward("billPaymentSuccess"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.addParameter("billno", billNo);
        redirect.addParameter("category", category);
        redirect.addParameter("incomingvisitid", incomingSampleform.getPatientId());
        return redirect;
      }
    }
  }
}
