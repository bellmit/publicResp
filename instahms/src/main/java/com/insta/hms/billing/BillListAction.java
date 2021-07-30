/**
 *
 */
package com.insta.hms.billing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

public class BillListAction extends BaseAction {
  
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  
  private static final GenericDAO sampleCollectionCenters = new GenericDAO("sample_collection_centers");

  @IgnoreConfidentialFilters
  public ActionForward getBills(ActionMapping mapping, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    Map map = getParameterMap(req);

    if (req.getParameter("bill_no") != null && !req.getParameter("bill_no").equals("")) {
      String[] billNo = { fillBillNoSearch(req, req.getParameter("bill_no")) };
      map.put("bill_no", billNo);
    }
    int centerId = (Integer) req.getSession(false).getAttribute("centerId");
    if (centerId != 0) {
      map.put("center_id", new String[] { centerId + "" });
      map.put("center_id@type", new String[] { "integer" });
    }
    map.remove("collectionCenterId");
    String collectionCenterId = req.getParameter("collectionCenterId");
    int userSampleCollectionCenterId = (Integer) req.getSession(false)
        .getAttribute("sampleCollectionCenterId");
    if (null != collectionCenterId && !collectionCenterId.equals("")) {
      map.put("collection_center_id", new String[] { collectionCenterId + "" });
      map.put("collection_center_id@type", new String[] { "integer" });
    } else {
      if (userSampleCollectionCenterId != -1) {
        map.put("collection_center_id", new String[] { userSampleCollectionCenterId + "" });
        map.put("collection_center_id@type", new String[] { "integer" });
      }
    }
    if (req.getParameter("claim_id") != null && !req.getParameter("claim_id").equals("")) {
      map.remove("claim_id");
    }

    // patientWriteoff and sponsorWriteoff should be removed while searching.
    if (req.getParameter("patientWriteoff") != null
        && !req.getParameter("patientWriteoff").equals("")) {
      map.remove("patientWriteoff");
    }

    if (req.getParameter("sponsorWriteoff") != null
        && !req.getParameter("sponsorWriteoff").equals("")) {
      map.remove("sponsorWriteoff");
    }

    List<BasicDynaBean> collectionCenters =
        sampleCollectionCenters.findAllByKey("center_id", centerId);
    req.setAttribute("collectionCenters", collectionCenters);
    BasicDynaBean colcenterBean = sampleCollectionCenters.findByKey("collection_center_id", -1);
    req.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
    // whenever we open the 'Open Bills' link, it will display the last 7 days records by default.
    // and No Change for 'Search Bills' link
    String date_range = req.getParameter("date_range");
    String week_start_date = null;
    if (date_range != null && date_range.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      week_start_date = dateFormat.format(openDt);

      map.put("open_date", new String[] { week_start_date, "" });
      map.put("open_date@op", new String[] { "ge,le" });
      map.put("open_date@cast", new String[] { "y" });
      map.remove("date_range");
    }

    PagedList list = BillDAO.searchBillsExtended(req.getParameter("claim_id"), map,
        ConversionUtils.getListingParameter(map));
    req.setAttribute("pagedList", list);
    req.setAttribute("bedTypes", new BedMasterDAO().getUnionOfAllBedTypes());
    req.setAttribute("bedNames",
        new JSONSerializer()
            .serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getBedNamesAndTypes(centerId,
                GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1))));
    req.setAttribute("seperate_pharmacy_credit",
        GenericPreferencesDAO.getGenericPreferences().getPharmacySeperateCreditbill());
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

    ActionForward forward = new ActionForward(mapping.findForward("showlist").getPath());
    // when ever user uses a pagination open_date should not append again.
    if (date_range != null && date_range.equals("week") && req.getParameter("open_date") == null) {
      addParameter("open_date", week_start_date, forward);
    }

    return forward;
  }

  public ActionForward getBillTemplateScreen(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, Exception, ServletException {

    String billNo = req.getParameter("billNo");

    req.setAttribute("templateList", BillPrintTemplateDAO.getBillTemplateList());
    req.setAttribute("billbean", BillDAO.getBillBean(billNo));

    req.setAttribute("genPrefs", new GenericPreferencesDAO().getGenericPreferences());
    return map.findForward("showTemplate");
  }

  /*
   * Retrieve the basic search screen only: initial default search values, no search executed.
   */
  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm f, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    request.setAttribute("bedTypes", new BedMasterDAO().getUnionOfAllBedTypes());
    request.setAttribute("bedNames",
        new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(BedMasterDAO
            .getBedNamesAndTypes((Integer) request.getSession(false).getAttribute("centerId"),
                GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1))));
    List<BasicDynaBean> collectionCenters =
        sampleCollectionCenters.findAllByKey("center_id", centerId);
    request.setAttribute("collectionCenters", collectionCenters);
    BasicDynaBean colcenterBean = sampleCollectionCenters.findByKey("collection_center_id", -1);
    request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));
    return mapping.findForward("showlist");
  }

  @IgnoreConfidentialFilters
  public ActionForward getBillsWriteOffList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    getWriteOffList(request, "P");
    HttpSession session = request.getSession();
    String userId = (String) session.getAttribute("userid");
    String writeOffLimit = DataBaseUtil.getStringValueFromDb(
        "select writeoff_limit from u_user where emp_username=?", userId);
    request.setAttribute("writeOffLimit", writeOffLimit);
    return mapping.findForward("billsWriteOffList");
  }

  @IgnoreConfidentialFilters
  public ActionForward getBillsSpnrWriteOffList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    getWriteOffList(request, "S");
    return mapping.findForward("billsSpnrWriteOffList");
  }

  private void getWriteOffList(HttpServletRequest request, String writeOffType)
      throws SQLException, Exception {

    Map map = getParameterMap(request);
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    if (centerId != 0) {
      map.put("center_id", new String[] { centerId + "" });
      map.put("center_id@type", new String[] { "integer" });
    }

    PagedList list = BillDAO.getWriteOffBillList(map, ConversionUtils.getListingParameter(map),
        writeOffType);
    request.setAttribute("pagedList", list);

  }

  public ActionForward approveWriteOff(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    String error = null;

    error = approveWriteoff(request, "P");
    updateWriteOffRemarks(request, "P");
    createWriteOffReceipt(request, "P");
    
    // Trigger allocation job
    String[] approvebillNos = request.getParameterValues("approve");
    // Call the Allocation method to update bill_charge changes.
    Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
    for(String billNo : approvebillNos) {
      allocationService.allocate(billNo, centerId);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("billsWriteOffRedirect"));
    redirect.addParameter("patient_writeoff", "M");
    redirect.addParameter("error", error);
    return redirect;
  }

  private String approveWriteoff(HttpServletRequest request, String writeOffType)
      throws SQLException, Exception {
    String[] approvebillNos = request.getParameterValues("approve");
    Preferences pref = (Preferences) request.getSession(false).getAttribute("preferences");
    String userId = (String) request.getSession(false).getAttribute("userid");
    String error = null;
    for (int i = 0; i < approvebillNos.length; i++) {
      if (writeOffType.equals("P"))
        BillDAO.updatePatientWriteOffStatus(approvebillNos[i]);
      else
        BillDAO.updateSponsorWriteOffStatus(approvebillNos[i]);
      /*
       * Close bills automatically on writeoff approval when there is no other(patient/sponsor) due
       * amount against a bill .
       */
      error = new BillBO().closeBillAutomaticallyOnWriteOffApproval(approvebillNos[i], writeOffType,
          userId, pref);
    }
    return error;

  }

  public ActionForward approveSpnrWriteOff(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    FlashScope flash = FlashScope.getScope(request);
    String error = null;

    error = approveWriteoff(request, "S");
    updateWriteOffRemarks(request, "S");
    createWriteOffReceipt(request, "S");
    
    // trigger allocation job.
    String username = (String) request.getSession(false).getAttribute("userid");
    String[] approvebillNos = request.getParameterValues("approve");
    Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
    for(String billNo : approvebillNos) {
      // Call the Allocation method to update bill_charge changes.
      allocationService.allocate(billNo, centerId);
    }
    
    
    flash.put("error", error);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("billsSpnrWriteOffRedirect"));
    redirect.addParameter("sponsor_writeoff", "M");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  private void updateWriteOffRemarks(HttpServletRequest request, String writeOffType)
      throws SQLException {
    String[] approvebillNos = request.getParameterValues("approve");
    String[] remarks = request.getParameterValues("writeOffRemarks");

    for (int j = 0; j < approvebillNos.length; j++) {
      BillDAO.updateWriteOffRemarks(approvebillNos[j], remarks[j], writeOffType);
      if (writeOffType.equals("S")) {
        ClaimDAO.updateWriteOffClaimClose(approvebillNos[j]);
      }

    }
  }

  private void createWriteOffReceipt(HttpServletRequest request, String writeOffType)
      throws SQLException, ParseException {
    String[] approvebillNos = request.getParameterValues("approve");
    Connection con = null;
    boolean success = true;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);
      BasicDynaBean basicDynaBean = null;
      BasicDynaBean creditNotesDynaBean = null;

      for (int j = 0; j < approvebillNos.length; j++) {
        Receipt receiptObj = new Receipt();

        basicDynaBean = BillDAO.getWriteOffBillAmount(approvebillNos[j]);
        creditNotesDynaBean = BillDAO.getCreditNotesAmount(approvebillNos[j]);
        
        if (writeOffType.equals("S")) {
          if (basicDynaBean.get("primary_tpa_id") != null
              && basicDynaBean.get("primary_sponsor_writeoff_amt") != null) {
            receiptObj.setSponsorIndex("P");
            BigDecimal receiptAmount = (BigDecimal) basicDynaBean.get("primary_sponsor_writeoff_amt");
            BigDecimal creditNoteAmount  = BigDecimal.ZERO;
            if (null != creditNotesDynaBean) {
              // The credit note amount will be negative so will have to add to the writeoff amt.
              creditNoteAmount = (BigDecimal) creditNotesDynaBean.get("primary_sponsor_amount");
            }
            success = createWriteOffReceipt(approvebillNos[j], request, receiptObj, writeOffType,
                receiptAmount.add(creditNoteAmount), receiptDAO,
                (String) basicDynaBean.get("primary_tpa_id"), (String)basicDynaBean.get("mr_no"));
          }
          if (basicDynaBean.get("secondary_tpa_id") != null
              && basicDynaBean.get("secondry_sponsor_writeoff_amt") != null) {
            receiptObj.setSponsorIndex("S");
            BigDecimal receiptAmount = (BigDecimal) basicDynaBean.get("secondry_sponsor_writeoff_amt");
            BigDecimal creditNoteAmount  = BigDecimal.ZERO;
            if (null != creditNotesDynaBean) {
              // The credit note amount will be negative so will have to add to the writeoff amt.
              creditNoteAmount = (BigDecimal) creditNotesDynaBean.get("secondary_sponsor_amount");
            }
            success = createWriteOffReceipt(approvebillNos[j], request, receiptObj, writeOffType,
                receiptAmount.add(creditNoteAmount), receiptDAO,
                (String) basicDynaBean.get("secondary_tpa_id"), (String)basicDynaBean.get("mr_no"));
          }
        } else if (writeOffType.equals("P") && basicDynaBean.get("patient_writeoff_amt") != null) {
          BigDecimal receiptAmount = (BigDecimal) basicDynaBean.get("patient_writeoff_amt");
          BigDecimal creditNoteAmount  = BigDecimal.ZERO;
          if (null != creditNotesDynaBean) {
            // The credit note amount will be negative so will have to add to the writeoff amt.
            creditNoteAmount = (BigDecimal) creditNotesDynaBean.get("patient_amount");
          }
          success = createWriteOffReceipt(approvebillNos[j], request, receiptObj, writeOffType,
              receiptAmount.add(creditNoteAmount), receiptDAO, null, (String)basicDynaBean.get("mr_no"));
        }
      }
    } finally

    {
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Creates the write off receipt.
   *
   * @param billNo the bill no
   * @param request the request
   * @param receiptObj the receipt obj
   * @param writeOffType the write off type
   * @param amount the amount
   * @param receiptDAO the receipt DAO
   * @param tpaId the tpa id
   * @param mrNo the mr no
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private boolean createWriteOffReceipt(String billNo, HttpServletRequest request,
      Receipt receiptObj, String writeOffType, BigDecimal amount, ReceiptRelatedDAO receiptDAO,
      String tpaId, String mrNo) throws SQLException {
    String receiptNo = BillDAO.getNextWriteOffId(writeOffType);
    receiptObj.setReceiptNo(receiptNo);
    receiptObj.setReceiptType("W");
    receiptObj.setMrno(mrNo);
    if (writeOffType.equals("S")) {
      receiptObj.setSponsorId(tpaId);
    }
    receiptObj.setAmount(amount);
    receiptObj.setUsername((String) request.getSession(false).getAttribute("userid"));
    receiptObj.setIsSettlement(true);
    receiptObj.setIsDeposit(false);
    receiptObj.setReceiptDate(DataBaseUtil.getDateandTime());
    receiptObj.setBillNo(billNo);

    return receiptDAO.createReceipt(receiptObj);
  }

}
