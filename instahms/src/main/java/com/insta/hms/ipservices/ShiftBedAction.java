package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.orders.OrderBO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ShiftBedAction.
 */
public class ShiftBedAction extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(ShiftBedAction.class);
  
  /** The js. */
  final JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The visit dao. */
  final VisitDetailsDAO visitDao = new VisitDetailsDAO();


  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  private static final GenericDAO bedNamesDAO = new GenericDAO("bed_names");
  private static final GenericDAO wardNamesDAO = new GenericDAO("ward_names");
  private static final GenericDAO bedTypesDAO = new GenericDAO("bed_types");
  
  /**
   * Gets the shift bed screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the shift bed screen
   * @throws Exception the exception
   */
  public ActionForward getShiftBedScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patientid");

    // patient Info
    BasicDynaBean patientDetails = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    req.setAttribute("patient", patientDetails.getMap());

    // Preferences
    Map prefs = new IPPreferencesDAO().getPreferences().getMap();
    req.setAttribute("ip_preferences", prefs);
    req.setAttribute("ipPrefsJSON", js.serialize(prefs));
    String bystander = req.getParameter("bystander");
    req.setAttribute("isBystander", bystander != null && bystander.equals("Y"));

    Map keys = new HashMap<String, String>();
    keys.put("status", "A");
    // all bed types
    req.setAttribute("bedtypesJSON", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(bedTypesDAO.listAll(null, keys, null))));
    req.setAttribute("allbedtypesJSON",
        js.serialize(ConversionUtils
            .copyListDynaBeansToMap(bedTypesDAO.listAll())));

    // free beds
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    int centerId = (Integer) req.getSession().getAttribute("centerId");
    List freeBeds = BedMasterDAO.getAllFreeBedNames(multiCentered, centerId);
    List retainedBeds = IPBedDAO.getRetainedBeds(patientId);
    List blockedBeds = IPBedDAO.getBlockedBeds(patientId);
    freeBeds.addAll(retainedBeds);
    freeBeds.addAll(blockedBeds);
    req.setAttribute("freeBeds", js.serialize(ConversionUtils.listBeanToListMap(freeBeds)));
    req.setAttribute("existingbedDetails", IPBedDAO.getActiveBedDetails(patientId).getMap());
    req.setAttribute("bedTypes",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllBedType())));
    Bill bill = BillDAO.getVisitCreditBill(patientId, true);
    req.setAttribute("billAmtDetailsJson", js.serialize(bill));
    req.setAttribute("visitTotalPatientDue", BillDAO.getVisitPatientDue(patientId));
    req.setAttribute("creditLimitDetailsJSON",
        js.serialize(visitDao.getCreditLimitDetails(patientId)));

    if (multiCentered && centerId == 0) {
      req.setAttribute("error", "Bed Shift is allowed only for center users.");
    }
    req.setAttribute("multiCentered", multiCentered);
    req.setAttribute("bedChargeJobDetails", BedChargeJobDetails.getJobTimeSummery());
    return mapping.findForward("ShiftBed");
  }

  /**
   * Shift bed.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward shiftBed(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String patientId = req.getParameter("patient_id");
    GenericDAO ipbedDAO = new GenericDAO("ip_bed_details");
    GenericDAO admDAO = new GenericDAO("admission");
    String currentBedAction = req.getParameter("current_bed_action");
    String billNo = null;
    String userName = (String) req.getSession().getAttribute("userid");
    
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      List<BasicDynaBean> ipBedDetailsList = ipbedDAO.listAll(null, "patient_id", patientId, null);
      boolean hasBystander = false;

      for (BasicDynaBean ipbedBean : ipBedDetailsList) {
        hasBystander = ipbedBean.get("bed_state").equals("O")
            && (Boolean) ipbedBean.get("is_bystander");
        if (hasBystander == true) {
          break;
        }
      }

      /**
       * Some server side validations are required before shift bed 1.Patient should has a open bill
       * later bill, and should be active 2.If tries to shift to a blocked bed he can retain the
       * current bed. 3.Can not shift from ICU to general bed unless bystander is released (if
       * bystander_availability is 'I') 4.Need all childs to be free for parent occupancy 5.shift is
       * not allowed for inactive patients.
       */

      // 1.Validate open bill
      Bill bill = BillDAO.getVisitCreditBill(patientId, true);
      billNo = bill.getBillNo();
      if (!validateBill(bill)) {
        return errorResponse(req, res, "Patient has no open primary Bill Later bill");
      }

      // setBillInfo will return error if patient is not active.
      OrderBO order = new OrderBO();
      String err = order.setBillInfo(con, patientId, bill.getBillNo(), false, userName);
      if (err != null) {
        return errorResponse(req, res, err);
      }

      BasicDynaBean newBedBean = ipbedDAO.getBean();
      ActionForward errForward = copyToDynaBean(req, res, newBedBean);
      if (errForward != null) {
        return errForward;
      }

      newBedBean.set("is_bystander", false);
      // ICU status of shift to bed.

      // 2. Do not allow retaining existing bed if shift is to Blocked bed
      if (!validateRetain(currentBedAction, (Integer) newBedBean.get("bed_id"))) {
        return errorResponse(req, res,
            "Retain is not allowed while shifting to a blocked bed, failed to shift bed.");
      }

      BasicDynaBean existingBed = IPBedDAO.getActiveBedDetails(con, patientId);
      if (validateBystanderICU(currentBedAction, (Integer) existingBed.get("bed_id"))) {
        return errorResponse(req, res, "ICU bed can not be a Bystander bed.");
      }

      BasicDynaBean admBean = admDAO.findByKey("patient_id", patientId);
      if (validateBystander(currentBedAction, patientId, (String) admBean.get("daycare_status"))) {
        return errorResponse(req, res, "Bystander is not allowed for Daycare patient.");
      }

      BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
      boolean isToBedICU = new BedMasterDAO().isIcu((Integer) newBedBean.get("bed_id"));
      
      if (prefs.get("bystander_availability").equals("I") && currentBedAction.equals("B")
          && !isToBedICU) {
        return errorResponse(req, res, "Bystander is available only for ICU patient.");
      }
      // 3.Do not allow to shift from ICU if he has bystander
      if (!validateShift(existingBed, newBedBean, hasBystander)) {
        return errorResponse(req, res, "Bystander bed needs to be released before shift");
      }

      // 4.check if all childs are free
      if (!validateParentBed((Integer) newBedBean.get("bed_id"),
          (Integer) existingBed.get("bed_id"))) {
        return errorResponse(req, res,
            "Child bed of the selected bed is not free,failed to shift bed.");
      }

      // passed all validations
      Timestamp shiftDate = (Timestamp) newBedBean.get("start_date");

      /*
       * Bed shifting has two (or three) parts: 1. Do something to the previous bed: cancel/finalize
       * [2]. If we are shifting to one of our own retained beds, then close that also 3. Allocate
       * the new bed to which we are shifting [4].If retaining existing bed, allocate it back as a
       * retained bed
       */

      // 1. Do something to the previous bed based on current bed action
      if (currentBedAction.equals("F") || currentBedAction.equals("R")
          || currentBedAction.equals("B")) {
        // retain/shift: finalize & release the current bed
        err = order.finalizeBed(con, existingBed, shiftDate, true, false);
        if (err != null) {
          return errorResponse(req, res, err);
        }

      } else {
        // cancel: cancel the current bed
        if (((String) existingBed.get("status")).equals("P")) {
          admBean = null;
        }
        if (existingBed.get("ref_admit_id") == null) {
          newBedBean.set("ref_admit_id", null);
        }
        // if thread start bed is canceled shifting bed shld not ref to that bed

        err = order.cancelBed(con, existingBed, admBean);
        if (err != null) {
          return errorResponse(req, res, err);
        }
      }

      // 2. If shifting to retained bed, close that as well
      List<BasicDynaBean> retainedBeds = IPBedDAO.getRetainedBeds(patientId);
      for (BasicDynaBean retainedBed : retainedBeds) {
        if (((Integer) retainedBed.get("bed_id")).intValue() == ((Integer) newBedBean.get("bed_id"))
            .intValue()) {
          err = order.finalizeBed(con, retainedBed, shiftDate.before((Timestamp) retainedBed
              .get("start_date")) ? (Timestamp) retainedBed.get("start_date") : shiftDate, true,
              true);
          if (err != null) {
            return errorResponse(req, res, err);
          }
        }
      }

      // 3. allocate the new bed and move into it.
      newBedBean.set("username", userName);
      newBedBean.set("admitted_by", userName);

      if (newBedBean.get("ref_admit_id") != null
          && !new IPBedDAO().sameBedType(con, (Integer) newBedBean.get("ref_admit_id"),
              (Integer) newBedBean.get("bed_id")) || prefs.get("merge_beds").equals("N")) {
        // ICU is a new Thread again
        newBedBean.set("ref_admit_id", null);
      }

      if (newBedBean.get("end_date") == null) {
        newBedBean.set("end_date", newBedBean.get("start_date"));
      }

      order.setUserName(userName);
      err = order.allocateBed(con, newBedBean, admBean, true);
      if (err != null) {
        return errorResponse(req, res, err);
      }
      // 4.if retain action is chosen add record for this as retained
      if (currentBedAction.equals("R") || currentBedAction.equals("B")) {
        newBedBean.set("status", "R");
        newBedBean.set("bed_id", (Integer) existingBed.get("bed_id"));
        newBedBean.set("charged_bed_type", (String) existingBed.get("charged_bed_type"));
        newBedBean.set("ref_admit_id", null);

        if (currentBedAction.equals("B")) {
          newBedBean.set("is_bystander", true);
        }

        if (currentBedAction.equals("R")) {
          newBedBean.set("is_retained", true);
        }

        order.setUserName(userName);
        err = order.allocateBed(con, newBedBean, admBean, true);
        if (err != null) {
          return errorResponse(req, res, err);
        }
      }

      err = order.recalculateBedCharges(con, patientId);
      if (err != null) {
        return errorResponse(req, res, err);
      }

      success = true;
      int oldBedId = (Integer) existingBed.get("bed_id");
      Map<String, String> wardShiftMap = new HashMap<String, String>();
      BasicDynaBean oldBedBean = bedNamesDAO.findByKey("bed_id", oldBedId);
      if (oldBedBean != null) {
        if (oldBedBean.get("ward_no") != null) {
          String oldWardNo = (String) oldBedBean.get("ward_no");
          BasicDynaBean oldWardNameBean = wardNamesDAO.findByKey("ward_no",
              oldWardNo);
          wardShiftMap.put("old_ward", (String) oldWardNameBean.get("ward_name"));
        }
        if (oldBedBean.get("bed_name") != null) {
          wardShiftMap.put("old_bed", (String) oldBedBean.get("bed_name"));
        }
      }
      int newBedId = (Integer) newBedBean.get("bed_id");
      BasicDynaBean newWardBedBean = bedNamesDAO.findByKey("bed_id", newBedId);
      if (newWardBedBean != null) {
        if (newWardBedBean.get("ward_no") != null) {
          String newWardNo = (String) newWardBedBean.get("ward_no");
          BasicDynaBean newWardNameBean = wardNamesDAO.findByKey("ward_no",
              newWardNo);
          wardShiftMap.put("new_ward", (String) newWardNameBean.get("ward_name"));
        }
        if (newWardBedBean.get("bed_name") != null) {
          wardShiftMap.put("new_bed", (String) newWardBedBean.get("bed_name"));
        }
      }
      wardShiftMap.put("patient_id", patientId);
      if (MessageUtil.allowMessageNotification(req, "general_message_send")) {
        MessageManager mgr = new MessageManager();
        mgr.processEvent("ward_bed_shift", wardShiftMap);
      }
    } catch ( Exception ex ) {
      success = false;
      throw ex;
    } finally {
      DataBaseUtil.commitClose(con, success);
      if (success) {
        if (billNo != null && !billNo.equals("")) {
          BillDAO.resetTotalsOrReProcess(billNo);
        }
        new SponsorBO().recalculateSponsorAmount(patientId);
        int centerId = (Integer) req.getSession().getAttribute("centerId");
        // Call the allocation job and update the bill total_amount.
        allocationService.updateBillTotal(billNo);
        // Call the Allocation method.
        allocationService.allocate(billNo, centerId);
      }
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("shiftBedRedirect"));
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

  /**
   * Validate bill.
   *
   * @param bill the bill
   * @return true, if successful
   */
  public boolean validateBill(Bill bill) {
    boolean openBill = true;
    if (bill == null) {
      openBill = false;
    }
    return openBill;
  }

  /**
   * Validate retain.
   *
   * @param bedAction the bed action
   * @param bedId the bed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateRetain(String bedAction, int bedId) throws SQLException {

    boolean allowRetain = true;
    BasicDynaBean statusBean = new IPBedDAO().getBedStatus(bedId);
    if (statusBean == null) {
      return true;
    }
    String bedStatus = (String) new IPBedDAO().getBedStatus(bedId).get("status");
    if (bedStatus == null) {
      return true;
    }

    // check if bed status is blocked
    if (bedStatus.equals("B") && bedAction.equals("R")) {
      allowRetain = false;
    }
    return allowRetain;
  }

  /**
   * Validate bystander ICU.
   *
   * @param bedAction the bed action
   * @param bedId the bed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateBystanderICU(String bedAction, int bedId) throws SQLException {

    boolean cuurntBedIsIcu = new BedMasterDAO().isIcu(bedId);
    boolean notValid = bedAction.equals("B") && cuurntBedIsIcu;
    return notValid;

  }

  /**
   * Validate bystander.
   *
   * @param bedAction the bed action
   * @param patientId the patient id
   * @param daycareStatus the daycare status
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateBystander(String bedAction, String patientId, String daycareStatus)
      throws SQLException {
    return daycareStatus.equals("Y") && bedAction.equals("B");
  }

  /**
   * Validate shift.
   *
   * @param fromBed the from bed
   * @param toBed the to bed
   * @param hasBystander the has bystander
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateShift(BasicDynaBean fromBed, BasicDynaBean toBed, boolean hasBystander)
      throws SQLException {

    Map prefs = new IPPreferencesDAO().getPreferences().getMap();
    String bystanderAvbl = (String) prefs.get("bystander_availability");

    boolean allowShift = true;

    if (!hasBystander) {
      return true;
    }

    // do not allow shift if shift from ICU to GENERAL and prefernce says bystander_availability to
    // ICU
    allowShift = allowShift && hasBystander;

    if (bystanderAvbl.equals("I")) {
      allowShift = allowShift
          && (new IPBedDAO().isBystanderBed((String) fromBed.get("patient_id"),
              (Integer) toBed.get("bed_id")) || isShiftToICU(fromBed, toBed));
    }
    return allowShift;
  }

  /**
   * Checks if is shift to ICU.
   *
   * @param fromBed the from bed
   * @param toBed the to bed
   * @return true, if is shift to ICU
   * @throws SQLException the SQL exception
   */
  public boolean isShiftToICU(BasicDynaBean fromBed, BasicDynaBean toBed) throws SQLException {
    BasicDynaBean fromBedBean = BedMasterDAO.getBedDetailsBean((Integer) fromBed.get("bed_id"));
    BasicDynaBean toBedBean = BedMasterDAO.getBedDetailsBean((Integer) toBed.get("bed_id"));

    return BedMasterDAO.isIcuBedType((String) fromBedBean.get("bed_type"))
        && BedMasterDAO.isIcuBedType((String) toBedBean.get("bed_type"));
  }

  /**
   * Validate parent bed.
   *
   * @param newBedId the new bed id
   * @param existbedId the existbed id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean validateParentBed(int newBedId, int existbedId) throws SQLException {
    return !new IPBedDAO().isAnyChildOccupied(newBedId, existbedId);
  }

}
