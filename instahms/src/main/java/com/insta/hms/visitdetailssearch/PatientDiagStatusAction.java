package com.insta.hms.visitdetailssearch;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.laboratory.ExternalReportAction;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.sun.mail.iap.ConnectionException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientDiagStatusAction.
 */
public class PatientDiagStatusAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(PatientDiagStatusAction.class);

  /**
   * Search.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, Exception {

    String dateRange = request.getParameter("date_range");
    String startDate = null;
    if (dateRange != null && dateRange.equals("month")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      Date openDt = cal.getTime();
      startDate = dateFormat.format(openDt);
    }
    ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
    if (dateRange != null && dateRange.equals("month") 
        && request.getParameter("reg_date") == null) {
      addParameter("reg_date", startDate, forward);
    }
    request.setAttribute("initialScreen", "true");
    Integer userCenterId = RequestContext.getCenterId();
    String errorMsg = CenterHelper.centerUserApplicability(userCenterId);
    if (errorMsg != null) {
      request.setAttribute("error", errorMsg);
    }
    return forward;
  }

  /**
   * List.
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
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, Exception, ParseException {

    /*
     * User's department for defaulting the list
     */
    HttpSession session = request.getSession(false);
    String userId = (String) session.getAttribute("userid");
    String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    String department = request.getParameter("ddept_id");
    ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
    if (department == null) {
      if (userDept != null && !userDept.equals("")) {
        department = userDept;
      }
    }
    String errorMsg = CenterHelper.centerUserApplicability(centerId);
    // in center schema only for the center users allow the user to see the results.
    if (errorMsg != null) {
      request.setAttribute("error", errorMsg);
      return forward;
    }

    request.setAttribute("userDept", department);
    Map paramMap = new HashMap(request.getParameterMap());
    Map listing = ConversionUtils.getListingParameter(paramMap);
    listing.put(LISTING.PAGESIZE, 10);
    if (centerId != 0) {
      paramMap.put("center_id", new String[] { centerId + "" });
      paramMap.put("center_id@type", new String[] { "integer" });
    }

    PagedList pagedList = PatientDiagStatusDAO.getPatientDiagVisits(paramMap, listing);
    request.setAttribute("pagedList", pagedList);
    if (pagedList.getDtoList().size() > 0) {
      // details (reports/tests) contained in each visit
      List<String> visitIds = new ArrayList<String>();
      for (Map obj : (List<Map>) pagedList.getDtoList()) {
        visitIds.add((String) obj.get("patient_id"));
      }

      List<BasicDynaBean> details = PatientDiagStatusDAO.getPatientDiagDetails(paramMap, visitIds);
      Map visitDetails = ConversionUtils.listBeanToMapListListBean(details, "patient_id",
          "report_id");
      request.setAttribute("visitDetails", visitDetails);
    }
    request.setAttribute("external_report_integration_details",
        new InstaIntegrationDao().getActiveBean("itdose_external_report"));
    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
    request.setAttribute("islabNoReq", (String) diagGenericPref.get("autogenerate_labno"));
    request.setAttribute("optimizedLabReportPrint",
        (String) diagGenericPref.get("optimized_lab_report_print"));

    return forward;
  }

  /**
   * Gets the externalreport.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the externalreport
   * @throws ConnectionException
   *           the connection exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getexternalreport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ConnectionException,
      Exception {

    String error = "";
    String visitNo = request.getParameter("_external_visit_id");
    ExternalReportAction er = new ExternalReportAction();

    error = er.getExternalReportData(response, visitNo, error);

    if (!error.equals("")) {
      FlashScope flash = FlashScope.getScope(request);
      ActionRedirect redirect = new ActionRedirect("/PatientDiagStatus.do?_method=list");
      flash.error(error);
      redirect.addParameter("mr_no", request.getParameter("mr_no"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    return null;
  }

}