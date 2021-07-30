package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.OrderDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class IpBedAction.
 */
public class IpBedAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(IpBedAction.class);

  /** The Constant BED_DETAILS_SCREEN_URL. */
  private static final String BED_DETAILS_SCREEN_URL = "ipbeddetails.do"
      + "?method=getIpBedDetailsScreen";

  /** The js. */
  final JSONSerializer js = new JSONSerializer().exclude("class");

  /** The visit dao. */
  final VisitDetailsDAO visitDao = new VisitDetailsDAO();
  
  private static final GenericDAO ipBedDetailsDAO = new GenericDAO("ip_bed_details");
  private static final GenericDAO admissionDAO = new GenericDAO("admission");

  /** The order. */
  final OrderBO order = new OrderBO();

  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

  /**
   * Gets the ip bed details screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the ip bed details screen
   * @throws Exception
   *           the exception
   */
  public ActionForward getIpBedDetailsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    saveToken(request);
    BasicDynaBean details = VisitDetailsDAO.getPatientVisitDetailsBean(request
        .getParameter("patientid"));
    request.setAttribute("admissiondetails",
        IPBedDAO.getCurrentBeddetails(request.getParameter("patientid")));
    request.setAttribute("patientvisitdetails", details);
    request.setAttribute("nonICUBedTypes",
        js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getNonIcuBedtypes())));
    List prvBeds = new DashBoardDAO().getPreviousBeds((String) details.get("mr_no"),
        (String) details.get("patient_id"));
    request.setAttribute("prvbeds", prvBeds);
    request.setAttribute("bedTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(BedMasterDAO.getAllBedType())));
    request.setAttribute("ip_preferences",
        new GenericDAO("ip_preferences").getRecord().getMap());

    Bill bill = BillDAO.getVisitCreditBill(request.getParameter("patientid"), true);
    if (bill == null) {
      FlashScope flash = FlashScope.getScope(request);
      String url = "/pages/ipservices/adt.do?_method=getADTScreen";
      ActionRedirect redirect = new ActionRedirect(url);
      flash.error("Patient doesnot have open credit bill");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;
    if (multiCentered && centerId == 0) {
      request.setAttribute("error", "Bed Actions are allowed only for center users.");
    }
    request.setAttribute("multiCentered", multiCentered);
    request.setAttribute("creditBill", bill);

    return mapping.findForward("beddetailsscreen");
  }

  /**
   * Ajax equipment finalization check.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward ajaxEquipmentFinalizationCheck(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String visitId = request.getParameter("visitId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(OrderDAO.getEquipmentDetailsNotFinalized(visitId));

    return null;
  }

  /**
   * Update bed details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public ActionForward updateBedDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      Exception {

    Map params = request.getParameterMap();
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(BED_DETAILS_SCREEN_URL);

    String patientId = (String) getFirstValueFromParams(params, "patient_id", false);
    int action = (Integer) getFirstValueFromParams(params, "actions", true);
    String actionDate = (String) getFirstValueFromParams(params, "action_date", false);
    String actionTime = (String) getFirstValueFromParams(params, "action_time", false);
    BigDecimal extendDays = new BigDecimal((Integer) getFirstValueFromParams(params,
        "extendeddays", true));
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
    String[] edited = (String[]) params.get("edited");
    String[] released = (String[]) params.get("released");
    String[] cancelledArray = ((String[]) params.get("cancelled"));

    List<BasicDynaBean> prvBedbeans = new ArrayList<BasicDynaBean>();
    List errors = new ArrayList();
    String err = null;
    setBedBeans(params, prvBedbeans, errors);

    if (errors.size() > 0) {
      flash.error("Parameter conversion error.");
      String referer = request.getHeader("Referer");
      referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
      redirect.addParameter("patientid", patientId);
      redirect.addParameter("patientid", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return null;
    }
    Connection con = null;
    boolean success = false;
    boolean reCalCharges = false;
    /**
     * should handle the following actions 1.Cancle bed,if selected for cancllation 2.Update duty
     * doctor. 3.Releasing the retained bed 4.Shifting to ratined bed 5.Perform bill action ,switch
     * to selected action
     */

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      reCalCharges = needChargeRecalculation(released, edited , cancelledArray);

      BasicDynaBean billBean = BillDAO.getActiveBill(patientId);
      err = order.setBillInfo(con, patientId, billBean.get("bill_no").toString(), false,
          (String) request.getSession().getAttribute("userId"));
      if (err != null) {
        return errorResponse(request, response, err);
      }
      // 1.Canceling the beds selected for cancellation
      err = cancelBeds(con, prvBedbeans, params);
      if (err != null) {
        return errorResponse(request, response, err);
      }

      // 2.Update duty doctor and set payee doctor
      err = updateBeds(con, prvBedbeans, params);
      if (err != null) {
        return errorResponse(request, response, err);
      }

      // 3.finalize the bed and release
      err = releaseBeds(con, prvBedbeans, params);
      if (err != null) {
        return errorResponse(request, response, err);
      }

      BasicDynaBean admBean = admissionDAO.findByKey(con, "patient_id", patientId);

      int shiftAdmintId = Integer.parseInt(request.getParameter("shift_admit_id"));
      // 4.If current bed is selected for cancellation,shift to
      // Retained/Bystander bed
      if (shiftAdmintId != 0 && validateShift(params, prvBedbeans, shiftAdmintId)) {

        BasicDynaBean shiftIpBean = ipBedDetailsDAO.findByKey("admit_id", shiftAdmintId);

        if ((Integer) admBean.get("bed_id") == 0
            && (shiftIpBean != null && !shiftIpBean.get("status").equals("X"))) {
          reCalCharges = true;

          Timestamp endDateForRetained = DateUtil.parseTimestamp(actionDate, actionTime);
          if (((Timestamp) shiftIpBean.get("start_date")).after(endDateForRetained)) {
            endDateForRetained = (Timestamp) shiftIpBean.get("start_date");
          }
          // can not finalize to back date.
          // finalize & release the selected bed for shift
          err = order.finalizeBed(con, shiftIpBean, endDateForRetained, true, true);
          if (err != null) {
            return errorResponse(request, response, err);
          }

          Date endDate = (Date) DateUtil.getExpectedDateTime(
              DateUtil.parseTimestamp(actionDate, actionTime).toString(), 1, "D", false, true);

          shiftIpBean.set("status", "C");
          shiftIpBean.set("bed_state", "O");
          shiftIpBean.set("start_date", DateUtil.parseTimestamp(actionDate, actionTime));
          shiftIpBean.set("is_bystander", false);
          shiftIpBean.set("end_date", new Timestamp(endDate.getTime()));

          BasicDynaBean bedBean = new BedMasterDAO()
              .getBedDetailsBean((Integer) shiftIpBean.get("bed_id"));

          if (bedBean.get("is_icu").equals("Y") || admBean.get("daycare_status").equals("Y")
              || prefs.get("merge_beds").equals("N")) {
            // ICU/Non_Merged/Daycare is a new Thread again
            shiftIpBean.set("ref_admit_id", null);
          }

          order.setUserName((String) request.getSession().getAttribute("userId"));
          err = order.allocateBed(con, shiftIpBean, admBean, true);
          if (err != null) {
            return errorResponse(request, response, err);
          }
        }
      }

      BasicDynaBean existingBed = IPBedDAO.getActiveBedDetails(con, patientId);
      BasicDynaBean currentIpBedbean = null;
      if (existingBed != null) {
        currentIpBedbean = ipBedDetailsDAO.findByKey(con, "admit_id", existingBed.get("admit_id"));
        currentIpBedbean
            .set("updated_date", DateUtil.parseTimestamp(actionDate + " " + actionTime));
      }

      // Bill actions only on alive bed.
      if (currentIpBedbean != null) {
        if (action > 0) {
          reCalCharges = true;
        }
        // 5.swithing to the selected option
        switch (action) {
          // update bed actions
          case 1:
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("last_updated", currentIpBedbean.get("updated_date"));
            admissionDAO.update(con, values, "patient_id", patientId);
  
            List<BasicDynaBean> occupiedBeds = IPBedDAO.getVisitBeds(con, patientId, "O");
            for (BasicDynaBean occupiedBed : occupiedBeds) {
  
              values = new HashMap<String, Object>();
              values.put("end_date", currentIpBedbean.get("updated_date"));
  
              ipBedDetailsDAO.update(con, values, "admit_id", occupiedBed.get("admit_id"));
            }

            break;
          // convert to ip
          case 2:

            values = new HashMap<String, Object>();
            values.put("daycare_status", "N");
            admissionDAO.update(con, values, "patient_id", patientId);
    
            List<BasicDynaBean> visitMainBeds = new IPBedDAO().getVistMainBeds(patientId);
            List<ChargeDTO> mainCharges = null;
            ChargeDAO chargedao = new ChargeDAO(con);
    
            for (BasicDynaBean mainBed : visitMainBeds) {
              String chargeId = BillActivityChargeDAO.getChargeId("BED",
                  (Integer) mainBed.get("admit_id"));
              ChargeDAO chrgDao = new ChargeDAO(con);
              mainCharges = chrgDao.getChargeAndRefs(chargeId);
    
              for (ChargeDTO chargedto : mainCharges) {
                chargedto.setStatus("X");
              }
    
              chargedao.updateChargeAmountsList(mainCharges);
            }

            break;
          // finalize current bed
          case 3:
            List<BasicDynaBean> currentBeds =
                ipBedDetailsDAO.findAllByKey(con, "patient_id", patientId);
            for (BasicDynaBean bed : currentBeds) {
              if (((String) bed.get("bed_state")).equals("O")) {

                bed.set("bed_state", "F");
                bed.set("end_date", currentIpBedbean.get("end_date"));
                bed.set("updated_date", currentIpBedbean.get("end_date"));
                err = order.finalizeBed(con, bed,
                    DateUtil.parseTimestamp(actionDate + " " + actionTime), false,
                    bed.get("status").equals("R"));
                if (err != null) {
                  return errorResponse(request, response, err);
                }
              }
            }

            // update admission table with finalization date
            admBean.set("finalized_time", currentIpBedbean.get("end_date"));
            admissionDAO.update(con, admBean.getMap(), "patient_id",
                (String) admBean.get("patient_id"));
            break;

          default:
            break;

        }
      }

      if (reCalCharges) {

        err = order.recalculateBedCharges(con, patientId);
        if (err != null) {
          return errorResponse(request, response, err);
        }
      }
      success = true;

    } finally {
      DataBaseUtil.commitClose(con, success);
      if (success) {
        BasicDynaBean billbean = order.getBill();
        if (billbean != null) {
          String billNo = (String) billbean.get("bill_no");
          if (billNo != null && !("").equals(billNo)) {
            BillDAO.resetTotalsOrReProcess(billNo);
          }
          new SponsorBO().recalculateSponsorAmount(patientId);
          
          // Update the bill total amount.
          allocationService.updateBillTotal(billNo);
          
          // Call the Allocation method to update bill_charge changes.
          Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
          allocationService.allocate(billNo, centerId);
          
          
        }
      }
    }
    
    

    String genPrefRule = (String) GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule");
    String visitType = VisitDetailsDAO.getVisitType(patientId);
    BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(patientId);
    BigDecimal availableCreditLimit = visitDao.getAvailableCreditLimit(patientId, false);
    if (success && reCalCharges && visitType.equals("i")
        && (genPrefRule.equals("W") || genPrefRule.equals("B"))
        && availableCreditLimit.compareTo(BigDecimal.ZERO) < 0) {
      flash.info("The current patient outstanding is : " + visitPatientDue
          + " Available Credit Limit is : " + availableCreditLimit);
      redirect.addParameter("patientid", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    redirect.addParameter("patientid", patientId);
    redirect.addParameter("patientid", patientId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;

  }

  /**
   * Sets the bed beans.
   *
   * @param params
   *          the params
   * @param bedsList
   *          the beds list
   * @param errorsList
   *          the errors list
   * @throws Exception
   *           the exception
   */
  private void setBedBeans(Map params, List bedsList, List errorsList) throws Exception {

    String[] bedId = (String[]) params.get("bed_id");
    String[] endDate = (String[]) params.get("end_date_dt");
    String[] startDate = (String[]) params.get("start_date_dt");

    BasicDynaBean ipBedbean = null;

    for (int i = 0; i < bedId.length; i++) {
      ipBedbean = ipBedDetailsDAO.getBean();
      ConversionUtils.copyIndexToDynaBean(params, i, ipBedbean, errorsList);

      ipBedbean.set("end_date", DateUtil.parseTimestamp(endDate[i]));
      ipBedbean.set("start_date", DateUtil.parseTimestamp(startDate[i]));
      bedsList.add(ipBedbean);

    }
  }

  /**
   * Checks if is occupied bed.
   *
   * @param ipBedBean
   *          the ip bed bean
   * @return true, if is occupied bed
   */
  private boolean isOccupiedBed(BasicDynaBean ipBedBean) {

    return ((String) ipBedBean.get("status")).equals("A")
        || ((String) ipBedBean.get("status")).equals("C");

  }

  /**
   * Cancel beds.
   *
   * @param con
   *          the con
   * @param prvBedbeans
   *          the prv bedbeans
   * @param params
   *          the params
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private String cancelBeds(Connection con, List<BasicDynaBean> prvBedbeans, Map params)
      throws IOException, SQLException, ParseException {

    String err = null;
    String[] cancelled = (String[]) params.get("cancelled");
    for (int i = 0; i < prvBedbeans.size(); i++) {
      if (cancelled[i].equals("Y")) {
        BasicDynaBean ipBedBean = prvBedbeans.get(i);
        BasicDynaBean admitBedBean = null;

        if (isOccupiedBed(ipBedBean)) {
          admitBedBean = admissionDAO.findByKey(con, "patient_id",
              ipBedBean.get("patient_id"));
          admitBedBean.set("daycare_status", "N");
        }
        // cancel: cancel the selected bed
        err = order.cancelBed(con, ipBedBean, admitBedBean);
      }
    }
    return err;
  }

  /**
   * Update beds.
   *
   * @param con
   *          the con
   * @param prvBedbeans
   *          the prv bedbeans
   * @param params
   *          the params
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  private String updateBeds(Connection con, List<BasicDynaBean> prvBedbeans, Map params)
      throws IOException, SQLException, Exception {
    String err = null;
    String[] edited = (String[]) params.get("edited");
    String[] dutyDoctorId = (String[]) params.get("duty_doctor_id");
    String[] remarks = (String[]) params.get("remarks");
    String[] chargedBedType = (String[]) params.get("charged_bed_type");

    for (int i = 0; i < prvBedbeans.size(); i++) {
      if (edited[i].equals("Y")) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("duty_doctor_id", dutyDoctorId[i]);
        values.put("remarks", remarks[i]);
        values.put("charged_bed_type", chargedBedType[i]);

        BasicDynaBean ipBedBean = prvBedbeans.get(i);
        ipBedDetailsDAO.update(con, values, "admit_id", (Integer) ipBedBean.get("admit_id"));

      }
    }
    return err;
  }

  /**
   * Release beds.
   *
   * @param con
   *          the con
   * @param prvBedbeans
   *          the prv bedbeans
   * @param params
   *          the params
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  private String releaseBeds(Connection con, List<BasicDynaBean> prvBedbeans, Map params)
      throws IOException, SQLException, ParseException {

    String err = null;
    String[] patientId = (String[]) params.get("patient_id");
    BasicDynaBean existingBed = IPBedDAO.getActiveBedDetails(patientId[0]);
    String[] released = (String[]) params.get("released");
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
    for (int i = 0; i < prvBedbeans.size(); i++) {
      if (released[i].equals("Y")) {
        BasicDynaBean ipBedBean = prvBedbeans.get(i);
        ipBedBean.set("status", "P");
        ipBedBean.set("bed_state", "F");
        err = order.finalizeBed(con, ipBedBean, (Timestamp) ipBedBean.get("end_date"), true, true);
      }
    }
    return err;
  }

  /**
   * Gets the first value from params.
   *
   * @param params
   *          the params
   * @param key
   *          the key
   * @param isInt
   *          the is int
   * @return the first value from params
   */
  private Object getFirstValueFromParams(Map params, String key, boolean isInt) {
    String[] values = (String[]) params.get(key);
    Object value = null;
    if (values == null) {
      if (isInt) {
        value = 0;
      } else {
        value = null;
      }
    } else {
      if (isInt) {
        String valueStr = ((String[]) params.get(key))[0];
        value = new BigDecimal(valueStr.isEmpty() ? "0" : valueStr).intValue();
      } else {
        value = "" + ((Object[]) params.get(key))[0];
      }
    }
    return value;
  }

  /**
   * Conevrt to ip bill.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @param date
   *          the date
   * @return the string
   * @throws Exception
   *           the exception
   */
  private String conevrtToIpBill(Connection con, String patientId, String date) throws Exception {

    List<BasicDynaBean> currentIpBedbeans = ipBedDetailsDAO.findAllByKey(
        "patient_id", patientId);
    BasicDynaBean existingBed = IPBedDAO.getActiveBedDetails(con, patientId);
    String err = null;
    List<ChargeDTO> origCharges = null;
    BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);

    ChargeDAO chargedao = new ChargeDAO(con);
    for (BasicDynaBean ipBedBean : currentIpBedbeans) {
      if (((String) ipBedBean.get("bed_state")).equals("O")) {
        ipBedBean.set("updated_date", DateUtil.parseTimestamp(date));
        ipBedBean.set("end_date", DateUtil.parseTimestamp(date));

        int admitId = (Integer) ipBedBean.get("admit_id");
        String chargeId = BillActivityChargeDAO.getChargeId("BED", admitId);
        ChargeDAO chrgDao = new ChargeDAO(con);
        origCharges = chrgDao.getChargeAndRefs(chargeId);

        for (ChargeDTO chargedto : origCharges) {
          chargedto.setStatus("X");
        }

        BasicDynaBean bedBean = BedMasterDAO.getBedDetailsBean(con,
            (Integer) ipBedBean.get("bed_id"));
        String isIcu = (String) bedBean.get("is_icu");
        String chargedBedType = (String) ipBedBean.get("charged_bed_type");
        String baseBedType = (String) pd.get("bill_bed_type");
        String orgId = (String) pd.get("org_id");
        Timestamp from = (Timestamp) ipBedBean.get("start_date");
        Timestamp to = DateUtil.getCurrentTimestamp();

        List<ChargeDTO> newCharges = null;
        if (isIcu.equals("Y")) {
          BasicDynaBean bedRates = new BedMasterDAO().getIcuBedChargesBean(chargedBedType,
              baseBedType, orgId);
          newCharges = order.getBedCharges("ICU", bedRates, from, to, null,
              (Boolean) ipBedBean.get("is_bystander"), (String) existingBed.get("daycare_status"),
              ipBedBean.get("status").equals("R"), ipBedBean.get("status").equals("A"),
              (String) ipBedBean.get("bed_state"), null); // TODO: admititng or shifting bed

        } else {
          BasicDynaBean bedRates = new BedMasterDAO()
              .getNormalBedChargesBean(chargedBedType, orgId);
          newCharges = order.getBedCharges("BED", bedRates, from, to, null,
              (Boolean) ipBedBean.get("is_bystander"), (String) existingBed.get("daycare_status"),
              ipBedBean.get("status").equals("R"), ipBedBean.get("status").equals("A"),
              (String) ipBedBean.get("bed_state"), null);
        }

        chargedao.updateChargeAmountsList(origCharges);// cancle day care charges
        String deleteChargeId = origCharges.get(0).getChargeId();

        chargedao.updateHasActivityStatus(deleteChargeId, false, true); // true: refs also
        new BillActivityChargeDAO(con).deleteActivity("BED", ipBedBean.get("admit_id").toString());

        for (ChargeDTO chargeDTO : newCharges) {
          /** newCharges will have bed type as act_description which needs to be bed name **/
          chargeDTO.setActDescription((String) bedBean.get("bed_name"));
          chargeDTO.setActDescriptionId(bedBean.get("bed_id").toString());
          if (chargeDTO.getActQuantity().compareTo(BigDecimal.ZERO) == 0) {
            chargeDTO.setActQuantity(BigDecimal.ONE);
          }
        }

        DateUtil dateUtil = new DateUtil();
        String remarks = dateUtil.getTimeStampFormatter().format(ipBedBean.get("start_date"))
            + " to "
            + dateUtil.getTimeStampFormatter().format(
                ipBedBean.get("end_date") == null ? ipBedBean.get("updated_date") : ipBedBean
                    .get("end_date"))
            + (ipBedBean.get("bed_state").equals("F") ? " (Finalized)" : ipBedBean.get("status")
                .equals("R") ? " (Retained) " : "(Occupied) ");
        order.insertOrderCharges(con, newCharges, "BED", (Integer) ipBedBean.get("admit_id"),
            remarks, null, (Timestamp) ipBedBean.get("start_date"), "N", null, null, null, null);

      }
    }

    return err;
  }

  /**
   * Need charge recalculation.
   *
   * @param released
   *          the released
   * @param edited
   *          the edited
   * @return true, if successful
   */
  public boolean needChargeRecalculation(String[] released, String[] edited, String[] cancelled) {
    boolean needCalculation = false;
    for (int i = 0; i < released.length; i++) {
      if (released[i].equals("Y") || edited[i].equals("Y") || cancelled[i].equals("Y")) {
        needCalculation = true;
        break;
      }
    }
    return needCalculation;
  }

  /**
   * Validate shift.
   *
   * @param params
   *          the params
   * @param prvBedbeans
   *          the prv bedbeans
   * @param shiftAdmitId
   *          the shift admit id
   * @return true, if successful
   */
  public boolean validateShift(Map params, List<BasicDynaBean> prvBedbeans, int shiftAdmitId) {
    String[] cancelled = (String[]) params.get("cancelled");
    boolean valid = true;

    for (int i = 0; i < prvBedbeans.size(); i++) {
      if (cancelled[i].equals("Y")
          && (Integer) prvBedbeans.get(i).get("admit_id") == shiftAdmitId) {
        valid = false;
        // retained bed also selected for cancellation,so no need to shift on it
        break;
      }
    }

    return valid;
  }
}
