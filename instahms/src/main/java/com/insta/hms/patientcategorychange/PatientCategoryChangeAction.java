/**
 * @author lakshmi.p
 */

package com.insta.hms.patientcategorychange;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChangeRatePlanBO;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

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
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class PatientCategoryChangeAction.
 */
public class PatientCategoryChangeAction extends DispatchAction {

  /** The log 4 j logger. */
  static Logger log4jLogger = LoggerFactory
      .getLogger(PatientCategoryChangeAction.class);

  /**
   * Edits the category details.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward editCategoryDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, ParseException {

    String visitId = request.getParameter("patient_id");

    BasicDynaBean visitbean = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);

    // set the category in request so that the category based rateplans and sponsors are available
    // in the screen.
    Integer patientCategory = (Integer) visitbean.get("patient_category");

    String categoryId = request.getParameter("patient_category_id");
    if (categoryId != null && !categoryId.equals("")) {
      patientCategory = new Integer(categoryId);
    }
    if (patientCategory != null && patientCategory != 0) {
      PatientCategoryChangeDAO.setCategoryDetails(patientCategory, request);
    }
    request.setAttribute("patient_category", patientCategory);

    request.setAttribute("regPrefFields", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("patient_id", visitId);
    request.setAttribute("visitbean", visitbean.getMap());
    List visitCreditBills = BillDAO.getActiveHospitalBills(visitId, BillDAO.bill_type.CREDIT);
    request.setAttribute("categories_list",
        PatientCategoryDAO.getAllCategoriesIncSuperCenter((Integer) visitbean.get("center_id")));
    request.setAttribute("patientHasCreditBill",
        visitCreditBills != null && !visitCreditBills.isEmpty());

    return mapping.findForward("editpatientcategory");
  }

  /**
   * Save category.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward saveCategory(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    VisitDetailsDAO visitdao = new VisitDetailsDAO();
    Connection con = null;
    boolean success = true;
    ActionRedirect redirect = new ActionRedirect(am.findForward("editpatientcategoryredirect"));
    FlashScope flash = FlashScope.getScope(req);
    String visitId = req.getParameter("patient_id");
    String categoryId = req.getParameter("patient_category_id");
    String categoryexpirydate = req.getParameter("category_expiry_date");
    Integer patientcategory = new Integer(categoryId);

    String regdate = req.getParameter("reg_date");

    Date regDate = DateUtil.parseDate(regdate);

    BasicDynaBean patientbean = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);

    String mrNo = (String) patientbean.get("mr_no");

    HttpSession session = req.getSession();
    String userName = (String) session.getAttribute("userid");

    Date expiryDate = null;
    boolean valid = false;

    if (patientbean != null) {
      if (patientbean.get("category_expiry_date") != null) {
        expiryDate = (Date) patientbean.get("category_expiry_date");
      }
      if (expiryDate == null || (expiryDate != null && expiryDate.compareTo(regDate) >= 0)) {
        valid = true;
      }
    }

    if (!valid) {
      flash.error("Patient category validity expired according to patient registration date... ");
      redirect.addParameter("patient_id", visitId);
      redirect.addParameter("patient_category_id", patientcategory);
      return redirect;
    }

    // In order to copy the Main Visit Insurance details changes to all Follow Up Visits, get all
    // episode followup visits.
    String opType = patientbean.get("op_type") != null ? (String) patientbean.get("op_type") : "M";
    List<BasicDynaBean> followupVisits = new ArrayList<BasicDynaBean>();
    if (opType != null && (opType.equals("M") || opType.equals("R"))) {
      followupVisits = visitdao.getEpisodeAllFollowUpVisitsOnly(visitId);
    }

    String orgId = "ORG0001";
    String orgid = req.getParameter("org_id");
    if (orgid != null && !orgid.equals("")) {
      orgId = orgid;
    }

    String insCompId = null;
    String insuranceCoId = req.getParameter("insurance_co_id");
    if (insuranceCoId != null && !insuranceCoId.equals("")) {
      insCompId = insuranceCoId;
    }

    ChangeRatePlanBO chRatePlanBO = new ChangeRatePlanBO();
    String billNo = null;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      // update patient_details with category_id, category expiry date.
      BasicDynaBean pbean = new PatientDetailsDAO().getBean();

      if (categoryexpirydate != null && !categoryexpirydate.equals("")) {
        Date categoryExpiryDate = DateUtil.parseDate(categoryexpirydate);
        pbean.set("category_expiry_date", categoryExpiryDate);
      }

      pbean.set("patient_category_id", patientcategory);
      Map keys = new HashMap();
      keys.put("mr_no", mrNo);

      new PatientDetailsDAO().update(con, pbean.getMap(), keys);

      String err = EditVisitDetailsDAO.removeInsurance(con, visitId, userName);
      removePatientPlans(con, visitId);
      if (err != null) {
        success = false;
        log4jLogger.error("Error while removing insurance...");
        flash.error("Insurance Case removal failed...");
        return redirect;
      }

      // List of all visits (Follow up and the Main visit)
      List<BasicDynaBean> allVisits = new ArrayList<BasicDynaBean>();

      // List of all bills (Followup visit bills and the Main visit bills)
      List<BasicDynaBean> allBills = new ArrayList<BasicDynaBean>();

      // Main Visit
      BasicDynaBean mainVisit = visitdao.findByKey(con, "patient_id", visitId);
      allVisits.add(mainVisit);

      // Main Visit bills
      List<BasicDynaBean> billList = BillDAO.getActiveHospitalBills(visitId,
          BillDAO.bill_type.BOTH, false);
      allBills.addAll(billList);

      // Followup Visit bills
      if (followupVisits != null && followupVisits.size() > 0) {
        for (BasicDynaBean b : followupVisits) {

          String followUpVisitId = (String) b.get("patient_id");
          List<BasicDynaBean> bills = BillDAO.getActiveHospitalBills(followUpVisitId,
              BillDAO.bill_type.BOTH, false);
          allBills.addAll(bills);
        }
        allVisits.addAll(followupVisits);
      }

      int insuranceId = patientbean.get("insurance_id") != null ? (Integer) patientbean
          .get("insurance_id") : 0;

      StringBuilder successMsg = new StringBuilder();
      ArrayList<String> ratePlanNotApplicableList = new ArrayList<String>();
      successMsg.append("Updated Charges for Open Bill(s) : <br/>");
      chRatePlanBO.setSuccessMsg(successMsg);
      chRatePlanBO.setRatePlanNotApplicableList(ratePlanNotApplicableList);
      chRatePlanBO.setEditVisits(true);

      if (allBills != null && allBills.size() > 0) {
        err = EditVisitDetailsDAO.updateVisitAndBillInsuranceDetails(con, mainVisit, allVisits,
            allBills, userName, insuranceId, null, null, null, null, null, 0, null, null, null,
            null, null, null, null, null, null, null, orgId, "N", "N");

        if (err == null && allVisits != null && allVisits.size() > 0) {
          for (BasicDynaBean visitBean : allVisits) {
            BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
            boolean shouldUpdateCharge = "Y"
                .equals(genericPrefs.get("update_charge_on_rate_plan_change"));
            if (shouldUpdateCharge) {
              err = chRatePlanBO.updateChargesBedAndRateWise(con,
                  (String) visitBean.get("patient_id"), null);
              if (err != null) {
                success = false;
                log4jLogger.error("Error while updating rate plan...");
                flash.error("Rate plan updation failed...");
                return redirect;
              }
            }
          }
        }
      }

      if (allBills != null && allBills.size() > 0) {
        for (BasicDynaBean billBean : allBills) {
          new GenericDAO("bill_claim").delete(con, "bill_no", (String) billBean.get("bill_no"));
          new BillChargeClaimDAO().delete(con, "bill_no", (String) billBean.get("bill_no"));
        }
      }

      if (err != null) {
        success = false;
        log4jLogger.error("Error while removing insurance...");
        flash.error("Insurance Case removal failed...");
        return redirect;
      }

      // update patient_registration with category_id, org_id
      BasicDynaBean visitbean = new VisitDetailsDAO().getBean();
      visitbean.set("patient_category_id", patientcategory);
      visitbean.set("org_id", orgId);
      keys = new HashMap();
      keys.put("patient_id", visitId);

      new VisitDetailsDAO().update(con, visitbean.getMap(), keys);

      DataBaseUtil.commitClose(con, true);

      // apply discount rule on bills
      List openBills = BillDAO.getAllActiveBills(visitId);
      DiscountPlanBO discBo = new DiscountPlanBO();
      String openbillNo = null;
      for (int i = 0; i < openBills.size(); i++) {
        // get all charges from open patient bills...
        openbillNo = ((Bill) openBills.get(i)).getBillNo();
        discBo.applyDiscountRule(openbillNo);
      }

    } catch (Exception exc) {
      flash.error("Patient category updation failed... ");
      throw exc;
    } finally {
      if (con != null && !con.isClosed()) {
        DataBaseUtil.commitClose(con, success);
      }
    }

    flash.info(chRatePlanBO.getSuccessMsg().toString());

    if (chRatePlanBO.getRatePlanNotApplicableList() != null
        && chRatePlanBO.getRatePlanNotApplicableList().size() > 0) {
      BasicDynaBean orgBean = new GenericDAO("organization_details").findByKey("org_id", "ORG0001");
      String newRatePlanName = (String) orgBean.get("org_name");
      StringBuffer sb = new StringBuffer(
          "There are some charges in bill(s) which are not applicable for rate plan: "
              + newRatePlanName + "</br>");
      if (chRatePlanBO.getRatePlanNotApplicableList().size() > 5) {
        flash.warning(sb.toString());
      } else {
        for (String chargeDesc : (ArrayList<String>) chRatePlanBO.getRatePlanNotApplicableList()) {
          sb.append(chargeDesc + "</br>");
        }
        flash.warning(sb.toString());
      }
    }

    redirect.addParameter("patient_id", visitId);
    redirect.addParameter("patient_category_id", patientcategory);
    return redirect;
  }

  /*
   * Removes policy,plan,plan doc details. Policy delte will be checked for another visit reference
   * to it before deleting. Used while category update from Edit visit details screen.
   */

  /**
   * Removes the patient plans.
   *
   * @param con
   *          the con
   * @param visitID
   *          the visit ID
   * @throws SQLException
   *           the SQL exception
   */
  private void removePatientPlans(Connection con, String visitID) throws SQLException {

    PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
    GenericDAO policyDAO = new GenericDAO("patient_policy_details");
    GenericDAO planDocDetailsDAO = new GenericDAO("plan_docs_details");
    GenericDAO patientDocsDAO = new GenericDAO("patient_documents");
    List<BasicDynaBean> plansList = planDAO.findAllByKey("patient_id", visitID);
    BasicDynaBean planDocDetailsBean = null;
    Integer patientPolicyID = null;

    if (plansList != null && plansList.size() > 0) {
      for (BasicDynaBean bean : plansList) {

        patientPolicyID = (Integer) bean.get("patient_policy_id");
        if (patientPolicyID != null && patientPolicyID.intValue() > 0) {

          // if the same policy is been used in another visit,don't delete it
          boolean policyForOtherVistsAswell = (planDAO.getOtherPatientPlanDetails(con,
              patientPolicyID, visitID) != null);
          if (!policyForOtherVistsAswell) {
            policyDAO.delete(con, "patient_policy_id", patientPolicyID);
          }

          planDocDetailsBean = planDocDetailsDAO.findByKey(con, "patient_policy_id",
              patientPolicyID);
          if (planDocDetailsBean != null && planDocDetailsBean.get("doc_id") != null) {
            patientDocsDAO.delete(con, "doc_id", planDocDetailsBean.get("doc_id"));
            planDocDetailsDAO.delete(con, "patient_policy_id", patientPolicyID);
          }

        }

        planDAO.delete(con, "patient_policy_id", patientPolicyID, "patient_id",
            bean.get("patient_id"));
      }
    }
  }

}