package com.insta.hms.outpatient;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.TestDocumentDTO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;

import java.io.IOException;
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
 * The Class ChangeConsultingDoctorAction.
 *
 * @author krishna
 */
public class ChangeConsultingDoctorAction extends DispatchAction {

  /** The bill dao. */
  BillDAO billDao = new BillDAO();

  /** The consult dao. */
  DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

  /** The visit dao. */
  VisitDetailsDAO visitDao = new VisitDetailsDAO();

  /** The orderbo. */
  OrderBO orderbo = new OrderBO();

  /** The js. */
  JSONSerializer js = new JSONSerializer();

  /** The doctor dao. */
  DoctorMasterDAO doctorDao = new DoctorMasterDAO();

  /**
   * Gets the screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the screen
   * @throws SQLException the SQL exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException {
    String consultationId = request.getParameter("consultation_id");

    BasicDynaBean billBean = billDao.getBillDetailsForConsultId(Integer.parseInt(consultationId));
    BasicDynaBean consultBean = consultDao.findConsultationExt(Integer.parseInt(consultationId));
    Map doctormap = new HashMap();
    doctormap.put("doctors",
        ConversionUtils.copyListDynaBeansToMap(new DoctorMasterDAO().getDoctorsAndDepts()));
    /*
     * available consultation statuses are A, P, C, U U- Unnecessary. these consulatations will not
     * come to this screen because in oplist we filtering on (!= U). C/P- completed/Partial. not
     * allowed to change the consultation. A- allowed to change the consultation always indenpendent
     * of dept.
     */
    MessageResources bundle = (MessageResources) request.getAttribute(Globals.MESSAGES_KEY);
    if (consultBean.get("status").equals("C")) {
      request.setAttribute("error",
          "Consultation is completed. Hence you can't change the doctor.");
    } else if (consultBean.get("status").equals("P")
        || consultBean.get("initial_assessment_status").equals("P")) {
      request.setAttribute("error",
          bundle.getMessage("patient.outpatientlist.changeconsultingdoctor.details.message"));
    }
    request.setAttribute("billBean", billBean);
    request.setAttribute("consultation_bean", consultBean);
    request.setAttribute("AllDoctorsList", js.deepSerialize(doctormap));
    BasicDynaBean packageBean = consultDao.getPackageName(Integer.parseInt(consultationId));
    request.setAttribute("package_bean", packageBean);
    return mapping.findForward("ChangeConsultingDoctor");
  }

  /**
   * Change consulting doctor.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */

  public ActionForward changeConsultingDoctor(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    /*
     * this method is used to change the consulting Doctor. 1) if the new and old doctor charges are
     * same then allow changing doctor. 2) if the new and old doctor charges different then allow
     * changing doctor only when bill status is 'open' and payment status is 'unpaid'. else do not
     * allow changing doctor.
     */
    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
    redirect.addParameter("consultation_id", consultationId);

    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    if (consultBean == null) {
      flash.put("error", "Consultation does not exists");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = false;
    String error = null;
    String existingChargeId = null;
    String newChargeId = null;
    BasicDynaBean billBean = billDao.getBillDetailsForConsultId(consultationId);
    try {
      if (billBean == null) {
        // if no bill created for the consultation, just udpate the doctor id in doctor consultation
        // table.
        BasicDynaBean updatebean = consultDao.getBean();
        updatebean.set("doctor_name", request.getParameter("modified_doctor_id"));
        success = consultDao.update(con, updatebean.getMap(), "consultation_id",
            consultationId) > 0;

        if (request.getParameter("update_admitting_doctor") != null) {
          BasicDynaBean patientDetails = visitDao.getBean();
          patientDetails.set("doctor", request.getParameter("modified_doctor_id"));
          patientDetails.set("dept_name", request.getParameter("modified_doctor_dept_id"));
          patientDetails.set("patient_id", (String) consultBean.get("patient_id"));
          patientDetails.set("user_name", userName);
          success &= visitDao.updatePatientRegistration(con, patientDetails);
        }
        if (!success) {
          flash.put("error", "Failed to change consulting doctor.");
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        }
        new SponsorBO().recalculateSponsorAmount((String) consultBean.get("patient_id"));
        redirect = new ActionRedirect(mapping.findForward("successRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }

      BasicDynaBean bean = consultDao.getBean();
      bean.set("head", consultBean.get("head").toString());
      bean.set("doctor_name", request.getParameter("modified_doctor_id"));
      bean.set("presc_date", DateUtil.getCurrentTimestamp());
      bean.set("visited_date", DateUtil.getCurrentTimestamp());
      bean.set("status", "A");
      bean.set("package_ref", consultBean.get("package_ref")); // if it is part of a package.

      ArrayList<BasicDynaBean> beanList = new ArrayList<BasicDynaBean>();
      beanList.add(bean);

      error = orderbo.setBillInfo(con, (String) consultBean.get("patient_id"),
          (String) billBean.get("bill_no"), false, userName);
      if (error != null) {
        flash.put("error", error);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }

      Boolean flag = false;
      if (((String) consultBean.get("head")).equals("0")
          || ((Integer) billBean.get("consultation_type_id")).intValue() == 0) {
        // if the doctor is part of a package-package it will be not chargable, so directly change
        // the doctor. without checking the bill status,
        // and amounts.
        flag = allowChangingDoctor(con, consultationId, request, beanList, userName, consultBean,
            billBean, true);

      } else {

        BasicDynaBean bill = orderbo.getBill();
        BasicDynaBean doctorChargeBean = DoctorMasterDAO.getDoctorCharges(
            request.getParameter("modified_doctor_id"), (String) bill.get("org_id"),
            (String) bill.get("bed_type"));
        BasicDynaBean consTypeBean = orderbo
            .getConsultationTypeBean((Integer) billBean.get("consultation_type_id"));

        orderbo.setPlanIds(
            new PatientInsurancePlanDAO().getPlanIds(con, (String) bill.get("visit_id")));
        // construct the charge dto for the modified doctor
        List<ChargeDTO> charges = orderbo.getDoctorConsCharges(doctorChargeBean, consTypeBean,
            (String) bill.get("visit_type"),
            OrgMasterDao.getOrgdetailsDynaBean((String) bill.get("org_id")), BigDecimal.ONE,
            orderbo.isInsurance(), orderbo.getPlanIds(), (String) bill.get("bed_type"),
            (String) bill.get("visit_id"), null);

        // get charge dto for the current doctor.
        ChargeDTO chargeDto = new ChargeDAO(con).getCharge((String) billBean.get("charge_id"));

        BigDecimal changedDoctorAmt = charges.get(0).getAmount();
        BigDecimal currentDoctorAmt = chargeDto.getAmount();
        // compare whether two doctors amounts or equal or not.
        if (changedDoctorAmt.compareTo(currentDoctorAmt) == 0) {
          flag = allowChangingDoctor(con, consultationId, request, beanList, userName, consultBean,
              billBean, false);
        } else {
          if (billBean.get("status").equals("A")) {
            if (billBean.get("payment_status").equals("U")) {
              flag = allowChangingDoctor(con, consultationId, request, beanList, userName,
                  consultBean, billBean, false);
            } else {
              flash.put("error",
                  "Current and Modified Doctor amounts are different and Bill is paid."
                      + " Hence you can't change the doctor.");
              redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
              return redirect;
            }
          } else {
            flash.put("error",
                "Current and Modified Doctor amounts are different and Bill is not Open."
                    + " Hence you can't change the doctor.");
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
        }
        existingChargeId = (String) billBean.get("charge_id");
        newChargeId = ChargeDAO.getChargeId(con, (String) billBean.get("bill_no"));
      }

      success = flag;
      success &= !(consultBean.get("cancel_status") != null
          && consultBean.get("cancel_status").equals("C"));

    } finally {
      DataBaseUtil.commitClose(con, success);
      if (billBean != null) {
        if (billBean.get("bill_no") != null && !((String) billBean.get("bill_no")).equals("")) {
          BillDAO.resetTotalsOrReProcess((String) billBean.get("bill_no"));
        }
        new SponsorBO().recalculateSponsorAmount((String) consultBean.get("patient_id"));
        //update the charge id in bill charge receipt allocation
        if (existingChargeId != null && newChargeId != null) {
          updateBillChargeReceiptAllocation(existingChargeId,newChargeId);
        }
      }
    }
    if (!success || error != null) {
      flash.put("error", "Failed to change the consulting doctor.");
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    redirect = new ActionRedirect(mapping.findForward("successRedirect"));
    return redirect;
  }

  /**
   * Allow changing doctor.
   *
   * @param con            the con
   * @param consultationId the consultation id
   * @param request        the request
   * @param beanList       the bean list
   * @param userName       the user name
   * @param consultBean    the consult bean
   * @param billBean       the bill bean
   * @param docPartOfPack  the doc part of pack
   * @return true, if successful
   * @throws Exception the exception
   */
  /*
   * if doctor is part of a package-package, do not cancel the charge, but delete the activity.
   */
  private boolean allowChangingDoctor(Connection con, int consultationId,
      HttpServletRequest request, List<BasicDynaBean> beanList, String userName,
      BasicDynaBean consultBean, BasicDynaBean billBean, Boolean docPartOfPack) throws Exception {
    List cancelItemChargeOrders = new ArrayList();
    BasicDynaBean cancelbean = orderbo.getCancelBean("Doctor", consultationId + "", userName);
    cancelbean.set("package_ref", consultBean.get("package_ref"));
    cancelItemChargeOrders.add(cancelbean);

    boolean flag = orderbo.updateOrders(con, cancelItemChargeOrders, true, !docPartOfPack, false,
        true, null, null, null) == null;

    if (request.getParameter("update_admitting_doctor") != null && flag) {
      flag = false;
      BasicDynaBean patientDetails = visitDao.getBean();
      patientDetails.set("doctor", request.getParameter("modified_doctor_id"));
      patientDetails.set("dept_name", request.getParameter("modified_doctor_dept_id"));
      patientDetails.set("patient_id", (String) consultBean.get("patient_id"));
      patientDetails.set("user_name", userName);
      flag = visitDao.updatePatientRegistration(con, patientDetails);
    }

    if (flag) {
      flag = false;

      if (docPartOfPack) {
        orderbo.setPackageChargeId((String) billBean.get("charge_id"));
      }

      String error = orderbo.orderItems(con, beanList, new ArrayList<String>(),
          new ArrayList<Integer>(), new ArrayList<String>(), new ArrayList<String>(), null, null,
          null, null, 0, !docPartOfPack, true, new ArrayList<String>(), new ArrayList<Integer>(),
          null, new ArrayList<List<TestDocumentDTO>>());
      flag = (error == null);
    }
    // run the payment rules when bill status is not in open and cancelled state,
    // to make sure the newly added doctor gets payment.
    if (flag && !billBean.get("status").equals("A") && !billBean.get("status").equals("X")) {
      flag = PaymentEngine.updateAllPayoutAmounts(con,
          docPartOfPack ? (String) billBean.get("charge_id")
              : ChargeDAO.getChargeId(con, (String) billBean.get("bill_no")));
    }
    if (flag) {
      // copy the data from the cancelled consultation to the new consultation.
      int newConsultationId = (Integer) (beanList.get(0)).get("consultation_id");
      flag = DoctorConsultationDAO.replaceOldWithNewConsultation(con, consultationId,
          newConsultationId);
    }
    return flag;
  }

  /**
   * update charge id in bill charge receipt allocation.
   *
   * @param existingChargeId     the existing charge
   * @param newChargeId  the new charge id
   * @throws SQLException the SQL exception
   * @throws IOException  the IO exception
  */
  public void updateBillChargeReceiptAllocation(String existingChargeId, String newChargeId)
      throws SQLException, IOException {
    // update into bill charge receipt allocation.
    boolean success = true;
    Map columndata = new HashMap();
    Map keys = new HashMap();
    try (Connection con = DataBaseUtil.getReadOnlyConnection();) {
      GenericDAO bcraDao = new GenericDAO("bill_charge_receipt_allocation");
      columndata.put("charge_id", newChargeId);
      keys.put("charge_id", existingChargeId);
      success = bcraDao.update(con, columndata, keys) >= 0;
    }
  }
}
