package com.insta.hms.insurance.sponsorreceipts;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class CollectionApprovalAction.
 */
public class CollectionApprovalAction extends DispatchAction {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(CollectionApprovalAction.class);
  
  private static final GenericDAO receiptsCollectionDAO = new GenericDAO("receipts_collection");
  private static final GenericDAO billReceiptsDAO = new GenericDAO("bill_receipts");


  /**
   * Lists all the Server Request as a filtered search result.
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
    ReceiptsCollectionDAO receiptsCollectionDAO = new ReceiptsCollectionDAO();
    Map requestParams = new HashMap();
    requestParams.putAll(req.getParameterMap());
    PagedList list = receiptsCollectionDAO.getReceiptCollectionList(requestParams,
        ConversionUtils.getListingParameter(requestParams));
    req.setAttribute("pagedList", list);
    return mapping.findForward("list");
  }

  /**
   * update: POST method to update an existing server request and charges.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, Exception {

    String[] completedchecks = req.getParameterValues("_updateReceiptCollection");
    Boolean success = false;
    HttpSession session = RequestContext.getSession();
    String userid = (String) session.getAttribute("userid");
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BillDAO billDAO = new BillDAO(con);
      for (int i = 0; i < completedchecks.length; i++) {
        int collID = Integer.parseInt(completedchecks[i]);
        List<BasicDynaBean> receiptCollectionBeanList =
            receiptsCollectionDAO.listAll(null, "collection_id", collID, null);
        
        for (BasicDynaBean receiptCollectionBean : receiptCollectionBeanList) {
          receiptCollectionBean.set("status", "A");// approved
          receiptCollectionBean.set("username", userid);
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("collection_id", collID);
          keys.put("receipt_id", receiptCollectionBean.get("receipt_id"));
          int numRowsUpdated = receiptsCollectionDAO.update(con,
              receiptCollectionBean.getMap(), keys);
          if (numRowsUpdated > 0) {
            success = true;
          }
          BasicDynaBean billReceiptsBean =
              billReceiptsDAO.findByKey("receipt_no", receiptCollectionBean.get("receipt_id"));
          String billNumber = (String) billReceiptsBean.get("bill_no");
          Bill bill = billDAO.getBill(billNumber);
          if (!Bill.BILL_STATUS_CLOSED.equals(bill.getStatus())) {
            Preferences pref = (Preferences) req.getSession(false).getAttribute("preferences");
            String userId = (String) req.getSession(false).getAttribute("userid");
            closeReceiptBill(billNumber, userId, pref);
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    return redirect;
  }

  /**
   * Close receipt bill.
   *
   * @param billNumber the bill n
   * @param userId the user id
   * @param pref the pref
   * @return the string
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  private String closeReceiptBill(String billNumber, String userId, Preferences pref)
      throws SQLException, Exception {
    BigDecimal sponsorDue = new ReceiptsCollectionDAO().getSponsorDue(billNumber);
    BigDecimal patientDue = new ReceiptsCollectionDAO().getPatientDue(billNumber);
    String error = null;
    GenericDAO dao = new GenericDAO("bill");
    BasicDynaBean billBean = dao.findByKey("bill_no", billNumber);
    String patientWriteOff = (String) billBean.get("patient_writeoff");
    if (sponsorDue.compareTo(BigDecimal.ZERO) == 0
        && (patientDue.compareTo(BigDecimal.ZERO) == 0 || patientWriteOff.equals("A"))) {
      error = new BillBO().closeBill(billNumber, userId, true, true, pref);
    }
    return error;
  }

  /**
   * Gets the collection approval details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the collection approval details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getCollectionApprovalDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, ParseException {

    ReceiptsCollectionDAO receiptsCollectionDAO = new ReceiptsCollectionDAO();
    Map requestParams = new HashMap();
    requestParams.putAll(request.getParameterMap());
    String collectionId = request.getParameter("collection_id");
    PagedList pagedList = receiptsCollectionDAO.getReceiptCollection(
        Integer.parseInt(collectionId), ConversionUtils.getListingParameter(requestParams));
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(pagedList, response.getWriter());
    return null;
  }

}
