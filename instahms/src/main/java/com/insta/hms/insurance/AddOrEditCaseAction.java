package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class AddOrEditCaseAction.
 *
 * @author pragna.p
 */

public class AddOrEditCaseAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AddOrEditCaseAction.class);

  /** The tpa dao. */
  private static TpaMasterDAO tpaDao = new TpaMasterDAO();

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {
    req.setAttribute("whichScreen", "AddNewCase");
    req.setAttribute("info", req.getParameter("info"));
    return mapping.findForward("addshow");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {
    String mrNo = req.getParameter("mr_no");
    if ((mrNo != null) && !mrNo.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(req);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }
    ActionRedirect redirect = new ActionRedirect("AddOrEditCase.do?_method=addshow");
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    redirect.addParameter("whichScreen", "AddNewCase");
    return redirect;
  }

  /**
   * Connectcase.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward connectcase(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {
    req.setAttribute("referalDetails", ReferalDoctorDAO.getReferencedoctors());
    HashMap<String, Object> filter = new HashMap<String, Object>();
    filter.put("mr_no", req.getParameter("mr_no"));
    filter.put("name", req.getParameter("name"));
    filter.put("tpa_id", ConversionUtils.getParamAsList(req.getParameterMap(), "tpa_id"));
    filter.put("insurance_id", req.getParameter("insurance_id"));
    filter.put("status", ConversionUtils.getParamAsList(req.getParameterMap(), "status"));
    filter.put("visit_type", ConversionUtils.getParamAsList(req.getParameterMap(), "visit_type"));
    filter.put("gen_reg_date0", DataBaseUtil.parseDate(req.getParameter("gen_reg_date0")));
    filter.put("gen_reg_date1", DataBaseUtil.parseDate(req.getParameter("gen_reg_date1")));
    Map listing = ConversionUtils.getListingParameter(req.getParameterMap());
    listing.put(ConversionUtils.LISTING.PAGESIZE, 10);
    PagedList list = InsuranceDAO.getAllUnconnectedInsuranceCases(filter, listing);
    ArrayList<BasicDynaBean> dtolist = (ArrayList<BasicDynaBean>) list.getDtoList();
    for (BasicDynaBean b : dtolist) {
      String visitId = (String) b.get("patient_id");
      if (visitId != null && !visitId.equals("")) {
        String billNo = InsuranceDAO.getCaseBillsForMainVisit(visitId);
        b.set("bill_no", billNo);
      }
    }
    List pgdList = ConversionUtils.copyListDynaBeansToMap(dtolist);
    list.setDtoList(pgdList);
    req.setAttribute("pagedList", list);
    return mapping.findForward("caseconnect");
  }

  /**
   * Gets the any un used case present.
   *
   * @param mrNo        the mr no
   * @param visitBean   the visit bean
   * @param insuranceId the insurance id
   * @return the any un used case present
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getAnyUnUsedCasePresent(String mrNo, BasicDynaBean visitBean,
      String insuranceId) throws SQLException {
    Boolean unUsedCasePresnt = false;
    BasicDynaBean caseBean = null;
    Connection con = DataBaseUtil.getConnection();
    List<BasicDynaBean> patientCases = new InsuranceDAO().findAllByKey("mr_no", mrNo);
    for (BasicDynaBean patientCase : patientCases) {
      BasicDynaBean insurVisitBean = new VisitDetailsDAO().findByKey("insurance_id",
          (Integer) patientCase.get("insurance_id"));
      if (insurVisitBean == null
          && (patientCase.get("tpa_id").equals(visitBean.get("primary_sponsor_id")))
          || (insuranceId != null
              && (patientCase.get("insurance_id").toString()).equals(insuranceId))) {
        unUsedCasePresnt = true;
        caseBean = patientCase;
      }
    }
    DataBaseUtil.closeConnections(con, null);
    if (unUsedCasePresnt) {
      return caseBean;
    } else {
      return null;
    }
  }

  /**
   * Adds the to case.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward addToCase(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      String mrno = req.getParameter("mr_no");
      String visitId = req.getParameter("visit_id");
      String insuranceIdStr = req.getParameter("insurance_id");
      BasicDynaBean caseBean = null;
      boolean success = false;
      ActionRedirect redirect = null;
      FlashScope flash = FlashScope.getScope(req);
      BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", visitId);
      caseBean = getAnyUnUsedCasePresent(mrno, visitBean, insuranceIdStr);
      if (caseBean != null) {
        if (insuranceIdStr != null && !insuranceIdStr.equals("") && !insuranceIdStr.equals("0")) {
          caseBean = new InsuranceDAO().findByKey("insurance_id", Integer.parseInt(insuranceIdStr));
        }

        int insuranceId = (Integer) caseBean.get("insurance_id");
        // if a case already exists, connect to case and redirect to add/edit case screen
        if (visitId != null && !visitId.equals("")) {
          // an active visit exists, then connect to this visit
          success = InsuranceDAO.updatePatientRegistration(con, caseBean, visitId) > 0;
          caseBean.set("case_added_date", DateUtil.getCurrentTimestamp());
          if (success && insuranceId != 0) {
            InsuranceDAO.editInsuranceCase(con, caseBean);
          }
        } else {
          // no active visit exists, don't update patient registration
          req.setAttribute("info", "No Active Visit/Bill found...<br/>Case Added For MR No...");
          caseBean.set("case_added_date", DateUtil.getCurrentTimestamp());
          InsuranceDAO.editInsuranceCase(con, caseBean);
        }
        redirect = new ActionRedirect("AddOrEditCase.do?_method=addshow");
        redirect.addParameter("insurance_id", caseBean.get("insurance_id"));
        redirect.addParameter("mr_no", caseBean.get("mr_no"));
        redirect.addParameter("visit_id", visitId);
        redirect.addParameter("whichScreen", "AddOrEditDashboard");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;

      } else {
        // if no case exists redirect to add new case screen
        redirect = new ActionRedirect("AddOrEditCase.do?_method=addshow");
        redirect.addParameter("mr_no", req.getParameter("mr_no"));
        redirect.addParameter("visit_id", visitId);
        redirect.addParameter("whichScreen", "AddNewCase");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /**
   * Addshow.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward addshow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {

    String mrno = req.getParameter("mr_no");
    String visitId = req.getParameter("visit_id");
    String insuranceId = req.getParameter("insurance_id");
    VisitDetailsDAO visitdao = new VisitDetailsDAO();
    GenericDAO billdao = new GenericDAO("bill");

    BasicDynaBean bean;
    BasicDynaBean visitBean = null;
    String infoMessage = null;
    List<BasicDynaBean> allBills = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> insuredBills = null;
    if ((insuranceId != null) && !insuranceId.equals("")) {
      bean = new InsuranceDAO().getCaseDetails(Integer.parseInt(insuranceId));
    } else if (visitId != null) {
      bean = new InsuranceDAO().getVisitCaseDetails(visitId);
    } else {
      bean = new InsuranceDAO().getPatientCaseDetails(mrno);
    }

    if (visitId != null && !visitId.equals("")) {

      // Main Visit bills
      List<BasicDynaBean> billList = billdao.findAllByKey("visit_id", visitId);
      allBills.addAll(billList);

      List<BasicDynaBean> followupVisits = visitdao.getEpisodeAllFollowUpVisitsOnly(visitId);
      // Followup Visit bills
      if (followupVisits != null && followupVisits.size() > 0) {
        for (BasicDynaBean b : followupVisits) {

          String followUpVisitId = (String) b.get("patient_id");
          List<BasicDynaBean> bills = billdao.findAllByKey("visit_id", followUpVisitId);
          allBills.addAll(bills);
        }
      }

      if (allBills != null && allBills.size() > 0) {
        insuredBills = new ArrayList<BasicDynaBean>();
        for (BasicDynaBean bill : allBills) {
          if (!((String) bill.get("status")).equals("X") && (Boolean) bill.get("is_tpa")) {
            insuredBills.add(bill);
          }
        }
      }
    }

    boolean isBillOpen = false;
    // find if any active bill exits amongst the bills connected to case, if found disallow
    // case close.
    if (insuredBills != null && insuredBills.size() > 0) {
      for (BasicDynaBean bill : insuredBills) {
        if (((String) bill.get("status")).equals("A")) {
          isBillOpen = true;
          break;
        }
      }
    }

    if (mrno != null) {
      req.setAttribute("mr_no", mrno);
    } else if (bean != null) {
      req.setAttribute("mr_no", bean.get("mr_no"));
    }

    if (insuranceId == null) {
      /*
       * Current Insurance Id obtained from page is null, but an insurance id, is present for the
       * visit implies, that a new case is to be added.
       */
      infoMessage = "Case creation is  for MR No...<br/>Please connect case to visit using"
          + " <i>Connect to Case</i> dashboard...";
      if (bean != null && bean.get("insurance_id") != null
          && !(bean.get("insurance_id").toString().equals(""))) {
        if (bean.get("patient_id") != null && !bean.get("patient_id").equals("")) {
          visitBean = new VisitDetailsDAO().findByKey("patient_id",
              (String) bean.get("patient_id"));
          if (visitBean.get("insurance_id") != null
              && Integer.parseInt(visitBean.get("insurance_id").toString()) > 0) {
            infoMessage = "Case with Visit already Exists...<br/>Case Creation is for MR NO";
            req.setAttribute("casePresent", "Y");
          }
        }
      }
      bean = new InsuranceDAO().getPatientCaseDetails(mrno);

      req.setAttribute("info", infoMessage);
      if (bean != null) {
        bean.set("insurance_id", null);
        Integer patientCategoryId = (Integer) bean.get("patient_category_id");
        if (patientCategoryId != null) {
          req.setAttribute("allowedTpas", PatientCategoryDAO.getAllowedSponsors(patientCategoryId,
              (String) visitBean.get("visit_type")));
        }
      } else {
        req.setAttribute("allowedTpas", null);
      }
    }

    boolean isCaseConnected = false;
    List<BasicDynaBean> allVisits = new VisitDetailsDAO().findAllByKey("mr_no", mrno);
    if (allVisits != null) {
      for (BasicDynaBean visit : allVisits) {
        String status = (String) visit.get("status");
        int insuranceID = visit.get("insurance_id") != null ? (Integer) visit.get("insurance_id")
            : 0;
        String opType = (String) visit.get("op_type");
        if (opType.equals("M") && insuranceID > 0) {
          isCaseConnected = true;
          break;
        }
      }
    }

    req.setAttribute("tpanames", tpaDao.listAll(null, "status", "A"));// tpa names and ids
    req.setAttribute("insuredBills", insuredBills);
    req.setAttribute("visitId", visitId);
    req.setAttribute("whichScreen", req.getParameter("whichScreen"));
    req.setAttribute("isBillOpen", isBillOpen ? "Y" : "N");
    req.setAttribute("isCaseConnected", isCaseConnected ? "Y" : "N");

    req.setAttribute("insDetails", bean.getMap());
    if (visitBean != null && visitBean.get("primary_sponsor_id") != null) {
      req.setAttribute("default_tpa", visitBean.get("primary_sponsor_id"));
    }

    return mapping.findForward("addshow");
  }

  /**
   * Adds the or edit.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward addOrEdit(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException {

    int insuranceId = req.getParameter("insurance_id") == null
        || req.getParameter("insurance_id").equals("") ? 0
            : Integer.parseInt(req.getParameter("insurance_id"));
    int result;

    FlashScope flash = FlashScope.getScope(req);
    InsuranceDAO dao = new InsuranceDAO();
    BasicDynaBean bean = dao.getBean();
    Map params = req.getParameterMap();
    String patientId = req.getParameter("visit_id");
    List errors = new ArrayList();

    String connectCaseTo = req.getParameter("connectCaseTo");

    ConversionUtils.copyToDynaBean(params, bean, errors);

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    try {
      if (insuranceId != 0 && connectCaseTo != null && !connectCaseTo.equals("M")) {
        bean.set("bill_nos", null);
        result = InsuranceDAO.editInsuranceCase(con, bean);

      } else {
        bean.set("case_added_date", DateUtil.getCurrentTimestamp());
        boolean resVal;
        if (insuranceId != 0) {
          bean.set("insurance_id", insuranceId);
          resVal = InsuranceDAO.editInsuranceCase(con, bean) > 0;
        } else {
          bean.set("insurance_id", new InsuranceDAO().getNextSequence());
          resVal = InsuranceDAO.addInsuranceCase(con, bean);
        }
        if (resVal) {
          result = 1;
        } else {
          result = 0;
        }
      }
      if (result > 0) {
        if (patientId != null && !patientId.equals("")
            && (connectCaseTo == null || connectCaseTo.equals("") || connectCaseTo.equals("V"))) {
          int regResult = InsuranceDAO.updatePatientRegistration(con, bean, patientId);
          if (regResult > 0 && insuranceId == 0) {
            con.commit();
            flash.success("Successfully Created Case for MRNO:" + req.getParameter("mr_no"));
          } else if (regResult > 0 && insuranceId != 0) {
            con.commit();
            flash.success("Successfully Updated Case Details");
          } else {
            con.rollback();
            flash.error("Failed to Create a Case.");
          }
        } else {
          con.commit();
          if (insuranceId != 0) {
            flash.success("Successfully Updated Case Details");
          } else {
            flash.success("Successfully Created Case for MRNO:" + req.getParameter("mr_no"));
          }
        }
      } else {
        con.rollback();
        flash.error("Failed to Create a Case.");
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    ActionRedirect redirect = new ActionRedirect("AddOrEditCase.do?_method=addshow");
    redirect.addParameter("insurance_id", bean.get("insurance_id"));
    redirect.addParameter("mr_no", bean.get("mr_no"));
    if (patientId != null && !patientId.equals("")) {
      redirect.addParameter("visit_id", patientId);
    }
    redirect.addParameter("whichScreen", "AddOrEditDashboard");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * List.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param req      the req
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException {

    req.setAttribute("referalDetails", ReferalDoctorDAO.getReferencedoctors());

    HashMap filter = new HashMap();
    filter.put("mr_no", req.getParameter("mr_no"));
    filter.put("name", req.getParameter("name"));
    filter.put("phone", req.getParameter("phone"));
    filter.put("oldReg", req.getParameter("oldReg"));

    filter.put("status", ConversionUtils.getParamAsList(req.getParameterMap(), "patstatus"));
    filter.put("visit_type", ConversionUtils.getParamAsList(req.getParameterMap(), "visit_type"));

    filter.put("doctor_id", ConversionUtils.getParamAsList(req.getParameterMap(), "doctor_id"));
    filter.put("referrer", ConversionUtils.getParamAsList(req.getParameterMap(), "referrer"));

    filter.put("fdate", DataBaseUtil.parseDate(req.getParameter("fdate")));
    filter.put("tdate", DataBaseUtil.parseDate(req.getParameter("tdate")));

    PagedList list = InsuranceDAO.getAllMrnos(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()));
    req.setAttribute("pagedList", list);
    return mapping.findForward("list");
  }
}
