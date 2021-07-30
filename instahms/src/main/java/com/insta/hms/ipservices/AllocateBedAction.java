package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.orders.OrderBO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class AllocateBedAction.
 */
public class AllocateBedAction extends BaseAction {

  /** The js. */
  final JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The visit dao. */
  final VisitDetailsDAO visitDao = new VisitDetailsDAO();
  
  private static final GenericDAO bedTypeDAO = new GenericDAO("bed_types");
  private static final GenericDAO admissionDAO = new GenericDAO("admission");
  
  /**
   * Gets the bed allocation screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the bed allocation screen
   * @throws Exception the exception
   */
  public ActionForward getBedAllocationScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patientid");

    BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    req.setAttribute("patient", patientDetails.getMap());

    Bill openCreditBill = BillDAO.getVisitCreditBill(patientId, true);

    req.setAttribute("bill", openCreditBill);

    // todo: required?
    BedMasterDAO bdao = new BedMasterDAO();
    String orgId = (String) patientDetails.get("org_id");
    req.setAttribute("normalbed_initialpayments", js.serialize(bdao.getBedWardCharges(orgId)));
    req.setAttribute("icubed_initialpayments",
        js.serialize(bdao.getIcuWardCharges((String) patientDetails.get("bill_bed_type"), orgId)));

    Map keys = new HashMap<String, String>();
    keys.put("status", "A");
    req.setAttribute("bedTypes",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedType())));
    req.setAttribute("bedtypesJSON", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(bedTypeDAO.listAll(null, keys, null))));

    req.setAttribute("allbedtypesJSON",
        js.serialize(ConversionUtils
            .copyListDynaBeansToMap(bedTypeDAO.listAll())));

    Map prefs = new IPPreferencesDAO().getPreferences().getMap();
    req.setAttribute("ip_preferences", prefs);
    req.setAttribute("ipPrefsJSON", js.serialize(prefs));
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
        
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    req.setAttribute("freeBeds", js.serialize(ConversionUtils.copyListDynaBeansToMap(BedMasterDAO
        .getAllFreeBedNames(multiCentered, centerId))));

    Bill bill = BillDAO.getVisitCreditBill(patientId, true);
    req.setAttribute("billAmtDetailsJson", js.serialize(bill));
    req.setAttribute("existingbedDetails", IPBedDAO.getActiveBedDetails(patientId));
    req.setAttribute("visitTotalPatientDue", BillDAO.getVisitPatientDue(patientId));
    req.setAttribute("creditLimitDetailsJSON",
        js.serialize(visitDao.getCreditLimitDetails(patientId)));
    req.setAttribute("dutyDoctors", DoctorMasterDAO.getAllCenterActiveDoctors(centerId));

    String bystander = req.getParameter("bystander");
    req.setAttribute("isBystander", bystander != null && bystander.equals("Y"));

    if (multiCentered && centerId == 0) {
      req.setAttribute("error", "Bed Allocation is allowed only for center users.");
    }
    req.setAttribute("multiCentered", multiCentered);
    req.setAttribute("bedChargeJobDetails", BedChargeJobDetails.getJobTimeSummery());
    ActionForward returnForward = mapping.findForward("AllocateBed");
    return returnForward;
  }

  /**
   * Allocate bed.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward allocateBed(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, Exception {

    String patientId = req.getParameter("patient_id");
    GenericDAO ipbedDAO = new GenericDAO("ip_bed_details");
    HttpSession session = req.getSession();
    String userid = (String) session.getAttribute("userid");

    Connection con = null;
    boolean success = false; 
    boolean hasAdmission = false;
    String billNo = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      BasicDynaBean ipBedBean = ipbedDAO.getBean(); // todo: make proper DAO
      ActionForward errForward = copyToDynaBean(req, res, ipBedBean);
      if (errForward != null) {
        return errForward;
      }

      BasicDynaBean admBean = admissionDAO.getBean();
      errForward = copyToDynaBean(req, res, admBean);
      if (errForward != null) {
        return errForward;
      }
      if (req.getParameter("daycare_status") == null) {
        // checkbox can be null if unchecked
        admBean.set("daycare_status", "N");
      }

      admBean.set("mr_no", ipBedBean.get("mrno"));
      admBean.set("admit_date", DateUtil.getDatePart((Timestamp) ipBedBean.get("start_date")));
      admBean.set("admit_time", ipBedBean.get("start_date"));

      // possible for a baby and readmiting patient
      BasicDynaBean admissionBean = admissionDAO.findByKey("patient_id", patientId);
      if (admissionBean != null) {
        hasAdmission = true;
      }

      Bill bill = BillDAO.getVisitCreditBill(patientId, true);
      if (bill == null) {
        return errorResponse(req, res, "Patient has no primary Bill Later bill that is open");
      }

      BasicDynaBean finalizedBed = new IPBedDAO().getIpBedDetails(con, patientId, "A", "F");

      finalizedBed = (finalizedBed == null ? new IPBedDAO().getIpBedDetails(con, patientId, "C",
          "F") : finalizedBed);

      if (finalizedBed == null && !(Boolean) ipBedBean.get("is_bystander")
          && (hasAdmission && (Integer) admissionBean.get("bed_id") != 0)) {
        return errorResponse(req, res, "Patient is occupying a bed");
      }

      OrderBO order = new OrderBO();
      billNo = bill.getBillNo();
      String err = order.setBillInfo(con, patientId, bill.getBillNo(), false, (String) req
          .getSession().getAttribute("userId"));
      if (err != null) {
        return errorResponse(req, res, err);
      }

      if (ipBedBean.get("end_date") == null) {
        ipBedBean.set("end_date", ipBedBean.get("start_date"));
      }

      ipBedBean.set("ref_admit_id", ((Boolean) ipBedBean.get("is_bystander") 
          || admBean.get("daycare_status").equals("Y")) ? null
               : ipBedBean.get("ref_admit_id"));

      order.setUserName(userid);
      err = order.allocateBed(con, ipBedBean, admBean, hasAdmission);
      if (err != null) {
        return errorResponse(req, res, err);
      }

      err = order.recalculateBedCharges(con, patientId);
      if (err != null) {
        return errorResponse(req, res, err);
      }

      success = true;

    } finally {
      DataBaseUtil.commitClose(con, success);
      if (success) {
        if (billNo != null && !billNo.equals("")) {
          BillDAO.resetTotalsOrReProcess(billNo);
        }
        new SponsorBO().recalculateSponsorAmount(patientId);
      }
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("allocateBedRedirect"));
    FlashScope flash = FlashScope.getScope(req);
    String genPrefRule = (String) GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule");
    String visitType = VisitDetailsDAO.getVisitType(patientId);
    BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(patientId);
    BigDecimal availableCreditLimit = visitDao.getAvailableCreditLimit(patientId, false);
    if (success && visitType.equals("i") && (genPrefRule.equals("W") || genPrefRule.equals("B"))
        && availableCreditLimit.compareTo(BigDecimal.ZERO) < 0) {
      flash.info("The current patient outstanding is : " + visitPatientDue
          + " Available Credit Limit is : " + availableCreditLimit);
      redirect.addParameter("patientid", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    return mapping.findForward("beddetailsscreen");
  }

}
