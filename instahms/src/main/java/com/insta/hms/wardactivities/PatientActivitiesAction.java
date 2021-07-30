package com.insta.hms.wardactivities;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.usermanager.UserDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class PatientActivitiesAction.
 *
 * @author krishna
 */
public class PatientActivitiesAction extends DispatchAction {
  
  /** The rdm DAO. */
  RecurrenceDailyMasterDAO rdmDAO = new RecurrenceDailyMasterDAO();
  
  /** The p activity DAO. */
  PatientActivitiesDAO patActivityDAO = new PatientActivitiesDAO();
  
  /** The serv dao. */
  GenericDAO servDao = new GenericDAO("services_prescribed");
  
  /** The test DAO. */
  GenericDAO testDAO = new GenericDAO("tests_prescribed");

  /** The Package DAO. */
  GenericDAO packageDAO = new GenericDAO("package_prescribed");
  
  /** The visit dao. */
  VisitDetailsDAO visitDao = new VisitDetailsDAO();
  
  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException {
    String patientId = request.getParameter("patient_id");
    List activitiesList = null;
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean patbean = regDao.findByKey("patient_id", patientId);
      if (patbean == null) {
        String format = request.getParameter("format");
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      activitiesList = PatientActivitiesDAO.getActivities(patientId, false);
      request.setAttribute("patbean", patbean.getMap());
      request.setAttribute("visitTotalPatientDue", BillDAO.getVisitPatientDue(patientId));
      request.setAttribute("creditLimitDetailsJSON",
          js.serialize(visitDao.getCreditLimitDetails(patientId)));
    }

    String username = (String) request.getSession(false).getAttribute("userid");
    BasicDynaBean userBean = new GenericDAO("u_user").findByKey("emp_username", username);
    if (userBean != null) {
      request.setAttribute("isSharedLogIn", userBean.get("is_shared_login"));
      request.setAttribute("roleId", userBean.get("role_id"));
    }
    request.setAttribute("patient_activities", activitiesList);
    
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
    request.setAttribute("frequencies", rdmDAO.listAll(null, filterMap, null));
    JSONSerializer js = new JSONSerializer().exclude(".class");
    request.setAttribute("users", js.deepSerialize(ConversionUtils
        .copyListDynaBeansToMap(new UserDAO().getAllActiveUsersDynaList())));
    request.setAttribute("actionId", mapping.getProperty("action_id"));
    return mapping.findForward("list");
  }

  /**
   * Update activities.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws Exception the exception
   */
  public ActionForward updateActivities(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException, Exception {

    String patientId = request.getParameter("patient_id");
    String[] activityIds = request.getParameterValues("activity_id");
    String[] activityRemarks = request.getParameterValues("activity_remarks");
    String[] activityTypes = request.getParameterValues("activity_type");
    String[] dueDateTimeAr = request.getParameterValues("due_date");
    String[] completedDateAr = request.getParameterValues("completed_date");
    String[] isAlreadyCompleted = request.getParameterValues("is_already_completed");
    String[] isAlreadyCancelled = request.getParameterValues("is_already_cancelled");
    String[] isAlreadyOrdered = request.getParameterValues("is_already_ordered");
    String[] raiseOrder = request.getParameterValues("raise_order");
    String[] medBatches = request.getParameterValues("med_batch");
    String[] medExpiryDate = request.getParameterValues("med_expiry_date");
    String[] activityStatuses = request.getParameterValues("activity_status");
    String[] completedByUsers = request.getParameterValues("completed_by");
    String[] edited = request.getParameterValues("edited");
    // is not the logged in user from session. is the the user who is doing this transaction
    FlashScope flash = FlashScope.getScope(request);
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    boolean isNewOrdersExist = false;
    String error = null;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean flag = false;
    try {
      if (request.getParameter("isSharedLogIn").equals("Y")) {
        userName = request.getParameter("authUser");
      }
      txn: {
        if (activityIds != null) {
          for (int i = 0; i < activityIds.length - 1; i++) {
            String activityId = activityIds[i];
            String activityStatus = activityStatuses[i];
            if (activityId.equals("_")) {
              Timestamp dueDateTime = null;
              if (!dueDateTimeAr[i].equals("")) {
                dueDateTime = DateUtil.parseTimestamp(dueDateTimeAr[i]);
              }
              Timestamp completionTime = null;
              if (!completedDateAr[i].equals("")) {
                completionTime = DateUtil.parseTimestamp(completedDateAr[i]);
              }
              BasicDynaBean activitybean = patActivityDAO.getBean();
              activitybean.set("activity_id", patActivityDAO.getNextSequence());
              activitybean.set("patient_id", patientId);
              activitybean.set("activity_remarks", activityRemarks[i]);
              activitybean.set("gen_activity_details", request.getParameterValues("item_name")[i]);
              activitybean.set("activity_type", activityTypes[i]);
              activitybean.set("due_date", dueDateTime);
              activitybean.set("added_by", userName);
              activitybean.set("username", userName);
              activitybean.set("med_batch", medBatches[i]);
              if (medExpiryDate[i] != null && !medExpiryDate[i].equals("")) {
                activitybean.set("med_exp_date", DateUtil.parseDate(medExpiryDate[i]));
              }
              activitybean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
              activitybean.set("activity_status", activityStatuses[i]);
              if (activityStatus.equals("D")) {
                activitybean.set("completed_date", completionTime);
                if (request.getParameter("isSharedLogIn").equals("Y")) {
                  completedByUsers[i] = userName;
                }
                activitybean.set("completed_by", completedByUsers[i]);
              }
              if (!patActivityDAO.insert(con, activitybean)) {
                break txn;
              }
            } else {
              Timestamp dueDateTime = null;
              if (!dueDateTimeAr[i].equals("")) {
                dueDateTime = DateUtil.parseTimestamp(dueDateTimeAr[i]);
              }
              Timestamp completionTime = null;
              if (!completedDateAr[i].equals("")) {
                completionTime = DateUtil.parseTimestamp(completedDateAr[i]);
              }
              BasicDynaBean activitybean = patActivityDAO.getBean();
              activitybean.set("activity_remarks", activityRemarks[i]);
              activitybean.set("due_date", dueDateTime);
              activitybean.set("med_batch", medBatches[i]);
              if (edited[i].equals("true")) {
                activitybean.set("username", userName);
              }
              if (medExpiryDate[i] != null && !medExpiryDate[i].equals("")) {
                activitybean.set("med_exp_date", DateUtil.parseDate(medExpiryDate[i]));
              }
              activitybean.set("activity_status", activityStatuses[i]);

              if (activityTypes[i].equals("P")) {
                activitybean.set("prescription_id",
                    Integer.parseInt(request.getParameterValues("prescription_id")[i]));
                activitybean.set("prescription_type",
                    request.getParameterValues("prescription_type")[i]);
                activitybean.set("presc_doctor_id",
                    request.getParameterValues("presc_doctor_id")[i]);
                if ((activitybean.get("prescription_type").equals("S") || activitybean.get(
                    "prescription_type").equals("I"))
                    && !new Boolean(isAlreadyOrdered[i]) && new Boolean(raiseOrder[i])) {
                  Bill bill = BillDAO.getVisitCreditBill(patientId, true);
                  Timestamp dtTime = DataBaseUtil.getDateandTime();
                  OrderBO orderBo = new OrderBO();
                  String billNo = null;
                  if (bill != null && bill.getPaymentStatus().equals("U")) {
                    billNo = bill.getBillNo();
                  }
                  String itemId = request.getParameterValues("item_id")[i];
                  String isPkg = request.getParameterValues("is_pkg") [i];
                  BasicDynaBean bean = null;
                  String presIdCol = null;
                  if (activitybean.get("prescription_type").equals("S")) {
                    bean = servDao.getBean();
                    bean.set("service_id", itemId);
                    bean.set("presc_date", dtTime);
                    ServiceMasterDAO serviceDAO = new ServiceMasterDAO();
                    Boolean conduction = (Boolean) serviceDAO.findByKey("service_id", itemId).get(
                        "conduction_applicable");
                    if (!conduction) {
                      bean.set("conducted", "C");
                    } else {
                      bean.set("conducted", "N");
                    }
                    bean.set("quantity", new BigDecimal(1));
                    bean.set("doctor_id", activitybean.get("presc_doctor_id"));
                    presIdCol = "prescription_id";
                  } else if (activitybean.get("prescription_type").equals("I") 
                      && !new Boolean(isPkg)) {
                    bean = testDAO.getBean();
                    bean.set("test_id", itemId);
                    bean.set("pres_date", dtTime);
                    bean.set("doc_presc_id", activitybean.get("prescription_id"));
                    Boolean conduction = (Boolean) new GenericDAO("diagnostics").findByKey(
                        "test_id", itemId).get("conduction_applicable");
                    if (!conduction) {
                      bean.set("conducted", "C");
                    } else {
                      bean.set("conducted", "N");
                    }
                    bean.set("pres_doctor", activitybean.get("presc_doctor_id"));
                    presIdCol = "prescribed_id";
                  } else if (activitybean.get("prescription_type").equals("I") 
                      && new Boolean(isPkg)) {
                    bean = packageDAO.getBean();
                    bean.set("package_id", Integer.parseInt(itemId));
                    bean.set("presc_date", dtTime);
                    bean.set("doc_presc_id", activitybean.get("prescription_id"));
                    bean.set("doctor_id", activitybean.get("presc_doctor_id"));
                    presIdCol = "prescription_id";
                    bean.set(presIdCol, 
                        DataBaseUtil.getNextSequence("package_prescribed_sequence"));
                  }

                  error = orderBo.setBillInfo(con, patientId, billNo, false, userName);
                  if (error != null) {
                    break txn;
                  }

                  List<BasicDynaBean> orders = new ArrayList<BasicDynaBean>();
                  orders.add(bean);
                  List<String> firstOfCategoryList = new ArrayList<String>();
                  List<String> condDoctrsList = new ArrayList<String>();
                  List<String> newPreAuths = new ArrayList<String>();
                  List<Integer> newPreAuthModes = new ArrayList<Integer>();
                  boolean ordersuccess = (orderBo.orderItems(con, orders, newPreAuths,
                      newPreAuthModes, firstOfCategoryList, condDoctrsList, null, 0) == null);
                  if (!ordersuccess) {
                    error = "Failed to insert the order details..";
                    break txn;
                  }
                  isNewOrdersExist = true;
                  activitybean.set("ordered_datetime", dtTime);
                  activitybean.set("ordered_by", userName);
                  activitybean.set("order_no", bean.get(presIdCol));
                }

              }

              if ((activityStatus.equals("D") && !new Boolean(isAlreadyCompleted[i]))
                  || (activityStatus.equals("X") && !new Boolean(isAlreadyCancelled[i]))) {

                if (activityStatus.equals("D")) {
                  if (completionTime == null) {
                    completionTime = new java.sql.Timestamp(new java.util.Date().getTime());
                  }
                  activitybean.set("completed_date", completionTime);
                  if (request.getParameter("isSharedLogIn").equals("Y")) {
                    completedByUsers[i] = userName;
                  }
                  activitybean.set("completed_by", completedByUsers[i]);
                }

                if (activityTypes[i].equals("P")) {
                  BasicDynaBean currentActivityBean = patActivityDAO.findByKey(con, "activity_id",
                      Integer.parseInt(activityId));
                  Integer prescriptionId = (Integer) currentActivityBean.get("prescription_id");
                  Boolean discontinued = false;
                  if (prescriptionId != null) {
                    // check the activity discontinued for standing prescriptions.
                    PatientPrescriptionDAO prescDAO = new PatientPrescriptionDAO();
                    BasicDynaBean prescBean = prescDAO.findByKey(con, "patient_presc_id",
                        prescriptionId);
                    discontinued = ((String) prescBean.get("discontinued")).equals("Y");

                    if (!discontinued && prescBean.get("no_of_occurrences") != null) {
                      int activityNum = ((Integer) currentActivityBean.get("activity_num")) + 1;
                      discontinued = activityNum > (Integer) prescBean.get("no_of_occurrences");
                    } else if (!discontinued && prescBean.get("end_datetime") != null) {
                      java.sql.Timestamp nextDueDateTime = rdmDAO.getNextDueDateForActivity(Integer
                          .parseInt(activityId));
                      java.sql.Timestamp endDateTime = (Timestamp) prescBean.get("end_datetime");
                      discontinued = nextDueDateTime.getTime() > endDateTime.getTime();
                    }

                  }
                  /*
                   * when user cancels the order of completed activity, then we are marking the
                   * status of completed activity to 'Inprogress'. so now we have two pending
                   * activities, one is inserted when we marked it as completed, and another we
                   * cancel the completed activity. In this case, if a user marks the activity as
                   * completed for reopened activity, it should not generate one more pending
                   * activity. We should generate pending activity, when he marks the later activity
                   * as completed.
                   */
                  if (!discontinued
                      && patActivityDAO.getActivity(con, prescriptionId,
                          ((Integer) currentActivityBean.get("activity_num"))) == null) {
                    java.sql.Timestamp nextDueDateTime = rdmDAO.getNextDueDateForActivity(Integer
                        .parseInt(activityId));
                    BasicDynaBean nextDueActivityBean = patActivityDAO.getBean();
                    PatientActivitiesBO.copyToDynaBean(currentActivityBean.getMap(),
                        nextDueActivityBean);
                    nextDueActivityBean.set("activity_id", patActivityDAO.getNextSequence());
                    nextDueActivityBean.set("due_date", nextDueDateTime);
                    nextDueActivityBean.set("activity_num",
                        ((Integer) currentActivityBean.get("activity_num")) + 1);
                    nextDueActivityBean.set("ordered_datetime", null);
                    nextDueActivityBean.set("ordered_by", null);
                    nextDueActivityBean.set("activity_status", "P");
                    nextDueActivityBean.set("order_no", null);
                    nextDueActivityBean.set("med_exp_date", null);
                    nextDueActivityBean.set("activity_remarks", null);
                    nextDueActivityBean.set("med_batch", null);

                    if (!patActivityDAO.insert(con, nextDueActivityBean)) {
                      break txn;
                    }
                  }
                }
              }

              Map keys = new HashMap();
              keys.put("activity_id", Integer.parseInt(activityId));
              if (patActivityDAO.update(con, activitybean.getMap(), keys) == 0) {
                break txn;
              }
            }
          }
        }

        flag = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, flag);
      new SponsorBO().recalculateSponsorAmount(patientId);
    }

    String genPrefRule = (String) GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule");
    String visitType = VisitDetailsDAO.getVisitType(patientId);
    BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(patientId);
    BigDecimal availableCreditLimit = visitDao.getAvailableCreditLimit(patientId, false);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    if (flag && visitType.equals("i") && isNewOrdersExist
        && (genPrefRule.equals("W") || genPrefRule.equals("B"))
        && availableCreditLimit.compareTo(BigDecimal.ZERO) < 0) {
      flash.info("The current patient outstanding is : " + visitPatientDue
          + " Available Credit Limit is : " + availableCreditLimit);
      redirect.addParameter("patient_id", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    if (!flag) {
      flash.error(error == null ? "Failed to update activities" : error);
    }
    redirect.addParameter("patient_id", patientId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Prints the activities.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws DocumentException the document exception
   */
  public ActionForward printActivities(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      TemplateException, XPathExpressionException, TransformerException, DocumentException {
    String patientId = request.getParameter("patient_id");
    String printerIdStr = request.getParameter("printerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }
    String userName = (String) request.getSession(false).getAttribute("userid");
    PatientActivitiesBO bo = new PatientActivitiesBO();

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      bo.getWardActivitiesReport(patientId, PatientActivitiesBO.ReturnType.PDF, prefs, os,
          userName);
      os.close();

    } else {
      String textReport = new String(bo.getWardActivitiesReport(patientId,
          PatientActivitiesBO.ReturnType.TEXT_BYTES, prefs, null, userName));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    }
    return null;
  }

}
