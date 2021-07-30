package com.insta.hms.medicalrecorddepartment;

import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
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

/**
 * The Class MRDCasefileSearchAction.
 */
public class MRDCasefileSearchAction extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(MRDCasefileSearchAction.class);

  /**
   * Search casefiles.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ServletException
   *           the servlet exception
   * @throws FileUploadException
   *           the file upload exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  
  @IgnoreConfidentialFilters
  public ActionForward searchCasefiles(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, ServletException, FileUploadException, IOException, ParseException {
    Map filter = getParameterMap(req);

    String dateRange = req.getParameter("date_range");
    String weekIssueDate = null;
    String weekRequestedDate = null;
    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      weekIssueDate = dateFormat.format(openDt);
      weekRequestedDate = dateFormat.format(openDt);
      String[] type = { "timestamp" };
      filter.put("mca.issued_on", new String[] { weekIssueDate, "" });
      filter.put("mca.issued_on@op", new String[] { "ge,le" });
      filter.put("mca.issued_on@cast", new String[] { "y" });
      filter.put("mca.issued_on@type", type);
      filter.put("request_date", new String[] { weekRequestedDate, "" });
      filter.put("request_date@op", new String[] { "ge,le" });
      filter.put("request_date@cast", new String[] { "y" });
      filter.put("request_date@type", type);
      filter.remove("date_range");
    } else {
      String[] issuedDate = (String[]) filter.get("issued_on");
      String[] issuedTime = (String[]) filter.get("issued_on_time");
      String[] issuedOn = new String[2];
      String[] requestedDate = (String[]) filter.get("requested_date");
      String[] requestTime = (String[]) filter.get("requested_time");
      String[] requestDate = new String[2];
      if (issuedDate != null && issuedDate[0] != null && !issuedDate[0].equals("")) {

        issuedOn[0] = issuedDate[0] + " " + issuedTime[0] + ":00";
        issuedOn[1] = issuedDate[1] + " " + issuedTime[1] + ":00";
        filter.put("mca.issued_on", issuedOn);
        String[] op = { "ge", "le" };
        filter.put("mca.issued_on@op", op);
        filter.put("mca.issued_on@cast", new String[] { "y" });
        String[] type = { "timestamp" };
        filter.put("mca.issued_on@type", type);
      }
      if (requestedDate != null && requestedDate[0] != null && !requestedDate[0].equals("")) {

        requestDate[0] = requestedDate[0] + " " + requestTime[0] + ":00";
        requestDate[1] = requestedDate[1] + " " + requestTime[1] + ":00";
        filter.put("request_date", requestDate);
        String[] op = { "ge", "le" };
        filter.put("request_date@op", op);
        filter.put("request_date@cast", new String[] { "y" });
        String[] type = { "timestamp" };
        filter.put("request_date@type", type);
      }
    }
    filter.remove("issued_on");
    filter.remove("issued_on_time");
    filter.remove("requested_time");
    filter.remove("requested_date");

    PagedList pagedList = MRDCaseFileIssueDAO.searchMRDCaseList(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()), false, "search");

    req.setAttribute("pagedList", pagedList);
    req.setAttribute("GMRDDetails", GenericPreferencesDAO.getGenericPreferences());

    ActionForward forward = new ActionForward(mapping.findForward("searchCasefile").getPath());
    // when ever user uses a pagination pres_date should not append again.
    if (dateRange != null && dateRange.equals("week") && req.getParameter("issued_on") == null
        && req.getParameter("requested_date") == null) {
      addParameter("issued_on", weekIssueDate, forward);
      addParameter("requested_date", weekRequestedDate, forward);
    }

    return forward;

  }

  /**
   * Redirect screens.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws FileUploadException
   *           the file upload exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws ParseException
   *           the parse exception
   */
  
  @IgnoreConfidentialFilters
  public ActionForward redirectScreens(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, FileUploadException, IOException, ServletException, ParseException {
    HttpSession session = req.getSession();
    String userName = (String) session.getAttribute("userid");
    req.setAttribute("userName", userName);
    List depUnit = MRDCasefileIndentDAO.getDepartmentUnits();
    String[] mrnos = req.getParameterValues("_hiddenMrno");
    String[] casefileNo = req.getParameterValues("_hiddenCaseNo");
    String[] fileStatus = req.getParameterValues("_fileStatus");
    String[] patientName = req.getParameterValues("_patientName");
    String[] deptName = req.getParameterValues("_department");
    String[] deptId = req.getParameterValues("_deptId");
    String[] issuedToDept = req.getParameterValues("_issuedToDept");
    String[] issuedToUser = req.getParameterValues("_issuedToUser");
    String[] requestedBy = req.getParameterValues("_requestedBy");
    String[] requestedByDept = req.getParameterValues("_requestedByDept");
    String[] casefileWith = req.getParameterValues("_casefileWith");
    String[] regDate = req.getParameterValues("_regDate");
    String[] regTime = req.getParameterValues("_regTime");
    String[] indented = req.getParameterValues("_indented");
    String[] mlcStatus = req.getParameterValues("_mlcStatus");
    String[] indentDateTime = req.getParameterValues("_indentDateTimeHid");
    String[] indentDate = req.getParameterValues("_indentDateHid");
    String[] indentTime = req.getParameterValues("_indentTimeHid");
    String action = req.getParameter("_action");

    List list = new ArrayList();

    Map filter = getParameterMap(req);

    String[] fromRequestedDate = (String[]) filter.get("requested_date0");
    String[] fromRequestTime = (String[]) filter.get("requested_time0");
    String[] toRequestedDate = (String[]) filter.get("requested_date1");
    String[] toRequestTime = (String[]) filter.get("requested_time1");

    String[] requestDate = new String[2];
    if (fromRequestedDate[0] != null && toRequestedDate[0] != null
        && !fromRequestedDate[0].equals("") && !toRequestedDate[0].equals("")) {

      requestDate[0] = fromRequestedDate[0] + " " + fromRequestTime[0] + ":00";
      requestDate[1] = toRequestedDate[0] + " " + toRequestTime[0] + ":00";
      filter.put("request_date", requestDate);
      String[] op = { "ge", "le" };
      String[] type = { "timestamp" };
      filter.put("request_date@op", op);
      filter.put("request_date@type", type);
    }
    filter.remove("requested_date0");
    filter.remove("requested_time0");
    filter.remove("requested_date1");
    filter.remove("requested_time1");

    String[] fromIssuedDate = (String[]) filter.get("issued_on0");
    String[] toIssuedDate = (String[]) filter.get("issued_on1");
    String[] fromIssuedTime = (String[]) filter.get("issued_on_time0");
    String[] toIssuedTime = (String[]) filter.get("issued_on_time1");

    String[] issuedDate = new String[2];
    if (fromIssuedDate[0] != null && toIssuedDate[0] != null && !fromIssuedDate[0].equals("")
        && !toIssuedDate[0].equals("")) {

      issuedDate[0] = fromIssuedDate[0] + " " + fromIssuedTime[0] + ":00";
      issuedDate[1] = toIssuedDate[0] + " " + toIssuedTime[0] + ":00";
      filter.put("mca.issued_on", issuedDate);
      String[] op = { "ge", "le" };
      String[] type = { "timestamp" };
      filter.put("mca.issued_on@op", op);
      filter.put("mca.issued_on@type", type);
    }
    filter.remove("issued_on0");
    filter.remove("issued_on1");
    filter.remove("issued_on_time0");
    filter.remove("issued_on_time1");

    String selectItems = req.getParameter("_selectItem");
    if (selectItems.equals("singleFile")) {
      for (int i = 0; i < mrnos.length; i++) {
        if (!mrnos[i].equals("")) {
          Map map = new HashMap();
          map.put("mr_no", mrnos[i].toString());
          map.put("casefile_no", casefileNo[i].toString());
          map.put("file_status", fileStatus[i].toString());
          map.put("patient_full_name", patientName[i].toString());
          map.put("dept_name", deptName[i].toString());
          map.put("dept_id", deptId[i].toString());
          map.put("issued_to_dept", issuedToDept[i].toString());
          map.put("issued_to_user", issuedToUser[i].toString());
          map.put("requesting_dept", requestedBy[i].toString());
          map.put("req_dept_id", requestedByDept[i].toString());
          map.put("casefile_with", casefileWith[i].toString());
          map.put("regdate", regDate[i].toString());
          map.put("regtime", regTime[i].toString());
          map.put("indented", indented[i].toString());
          map.put("mlc_status", mlcStatus[i].toString());
          map.put("indent_date", indentDateTime[i].toString());
          map.put("ind_date", indentDate[i].toString());
          map.put("ind_time", indentTime[i].toString());
          list.add(map);
        }
      }
    } else {
      PagedList pl = MRDCaseFileIssueDAO.searchMRDCaseList(filter,
          ConversionUtils.getListingParameter(filter), true, action);
      list = pl.getDtoList();
    }

    JSONSerializer js = new JSONSerializer();
    if (action.equals("indent")) {
      req.setAttribute("contentMap", list);
      req.setAttribute("depUnitList", new JSONSerializer().serialize(depUnit));
      req.setAttribute("deptlist", js
          .serialize(ConversionUtils.copyListDynaBeansToMap(MRDCasefileIndentDAO.getDeptNames())));
      return mapping.findForward("indent");
    } else if (action.equals("issue")) {
      req.setAttribute("contentMap", list);
      req.setAttribute("depUnitList", new JSONSerializer().serialize(depUnit));
      req.setAttribute("GMRDDetails", GenericPreferencesDAO.getGenericPreferences());
      // req.setAttribute("mrdUserNameList", new JSONSerializer().serialize(mrdUserNames));
      return mapping.findForward("issue");
    } else if (action.equals("return")) {
      req.setAttribute("contentMap", list);
      return mapping.findForward("return");
    } else if (action.equals("close")) {
      req.setAttribute("contentMap", list);
      return mapping.findForward("close");
    } else {
      return null;
    }
  }

  /**
   * Prints the.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws DocumentException
   *           the document exception
   * @throws TemplateException
   *           the template exception
   * @throws FileUploadException
   *           the file upload exception
   */
  
  @IgnoreConfidentialFilters
  public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ServletException, IOException, SQLException, ParseException,
      DocumentException, TemplateException, FileUploadException {
    Map filter = getParameterMap(req);
    String[] issuedDate = (String[]) filter.get("issued_on");
    String[] issuedTime = (String[]) filter.get("issued_on_time");
    String[] issuedOn = new String[2];
    if (issuedDate != null && issuedDate[0] != null && !issuedDate[0].equals("")) {

      issuedOn[0] = issuedDate[0] + " " + issuedTime[0] + ":00";
      issuedOn[1] = issuedDate[1] + " " + issuedTime[1] + ":00";
      filter.put("mca.issued_on", issuedOn);
      String[] op = { "ge", "le" };
      String[] type = { "timestamp" };
      filter.put("mca.issued_on@op", op);
      filter.put("mca.issued_on@type", type);
    }
    filter.remove("issued_on");
    filter.remove("issued_on_time");

    String[] requestedDate = (String[]) filter.get("requested_date");
    String[] requestTime = (String[]) filter.get("requested_time");
    String[] requestDate = new String[2];
    if (requestedDate != null && requestedDate[0] != null && !requestedDate[0].equals("")) {

      requestDate[0] = requestedDate[0] + " " + requestTime[0] + ":00";
      requestDate[1] = requestedDate[1] + " " + requestTime[1] + ":00";
      filter.put("request_date", requestDate);
      String[] op = { "ge", "le" };
      String[] type = { "timestamp" };
      filter.put("request_date@op", op);
      filter.put("request_date@type", type);
    }

    filter.remove("requested_time");
    filter.remove("requested_date");

    PagedList listBean = MRDCaseFileIssueDAO.searchMRDCaseList(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()), true, "search");
    BasicDynaBean prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
    Template template = AppInit.getFmConfig().getTemplate("MRDCaseFilePatients.ftl");
    Map ftlParams = new HashMap();

    Map mrdcaseMap = ConversionUtils.listBeanToMapListBean(listBean.getDtoList(), "created_date");
    ftlParams.put("patientsList", listBean.getDtoList());
    ftlParams.put("yearMapkey", mrdcaseMap.keySet());
    StringWriter writer = new StringWriter();
    try {
      template.process(ftlParams, writer);
    } catch (TemplateException te) {
      log.error("", te);
      throw te;
    }
    HtmlConverter converter = new HtmlConverter();
    OutputStream os = res.getOutputStream();
    /*
     * res.setContentType("text/html"); os.write(writer.toString().getBytes());
     */
    res.setContentType("application/pdf");
    converter.writePdf(os, writer.toString());
    os.close();
    return null;
  }

}
