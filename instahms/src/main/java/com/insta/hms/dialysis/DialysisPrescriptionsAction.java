package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Dialysis.DialAccessTypesDAO;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class DialysisPrescriptionsAction.
 */
public class DialysisPrescriptionsAction extends BaseAction {

  /** The js. */
  JSONSerializer js = new JSONSerializer();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws FileUploadException the file upload exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response)
      throws SQLException, ParseException, FileUploadException, IOException {

    Map map = getParameterMap(req);

    PagedList list = DialysisPrescriptionsDAO.getAllMRNOPrescriptions(map,
        ConversionUtils.getListingParameter(map));

    req.setAttribute("pagedList", list);

    return mapping.findForward("list");
  }

  /**
   * Search.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response) throws SQLException, ParseException {

    return mapping.findForward("list");
  }

  /**
   * Adds the.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  /*
   * add: returns the "add" screen for adding a new Prescription
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    req.setAttribute("permanentAccessTypes", DialAccessTypesDAO.getActiveAvalDialAccessTypes());
    return addShow(mapping, form, req, res);
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  /*
   * show: returns the "edit" screen for showing/editing an existing Prescription.
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {
    req.setAttribute("permanentAccessTypes", DialAccessTypesDAO.getActiveAvalDialAccessTypes());
    return addShow(mapping, form, req, res);
  }

  /**
   * Adds the show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  /*
   * Common method for add/show. Returns an add/show screen.
   */
  private ActionForward addShow(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException {

    String mrno = req.getParameter("mr_no");
    String method = req.getParameter("_method");

    String prescriptionId = req.getParameter("dialysis_presc_id");
    String infoMessage = null;
    List<String> status = new ArrayList<>();
    String exisitngStatus = null;

    List<BasicDynaBean> presDetailsList = new ArrayList<>();

    if (method.equals("show")) {
      presDetailsList = DialysisPrescriptionsDAO.getPrescriptionDetails(mrno, null);
      req.setAttribute("method", "update");
      if (prescriptionId == null || prescriptionId.equals("")) {
        prescriptionId = presDetailsList.get(0).get("dialysis_presc_id").toString();
      }
      req.setAttribute("temporaryAccesses",
          DialysisPrescriptionsDAO.getTemporaryAccesses(mrno, Integer.parseInt(prescriptionId)));
      req.setAttribute("permanentAccesses",
          DialysisPrescriptionsDAO.getPermanentAccesses(mrno, Integer.parseInt(prescriptionId)));

    } else {
      if (null != mrno && !mrno.equals("")) {
        presDetailsList = DialysisPrescriptionsDAO.getPrescriptionDetails(mrno, null);
      }
      if (presDetailsList.size() > 0) {
        req.setAttribute("method", "update");
        if (prescriptionId == null || prescriptionId.equals("")) {
          prescriptionId = presDetailsList.get(0).get("dialysis_presc_id").toString();
        }
        req.setAttribute("temporaryAccesses",
            DialysisPrescriptionsDAO.getTemporaryAccesses(mrno, Integer.parseInt(prescriptionId)));
        req.setAttribute("permanentAccesses",
            DialysisPrescriptionsDAO.getPermanentAccesses(mrno, Integer.parseInt(prescriptionId)));
      } else {
        req.setAttribute("method", "create");
      }
    }

    if (presDetailsList.size() > 0) {
      req.setAttribute("presDetails", presDetailsList.get(0).getMap());
    }

    List dyalisatebean = new GenericDAO("dialysate_type").listAll();
    req.setAttribute("DialysateDetails",
        js.exclude("class").serialize(ConversionUtils.listBeanToListMap(dyalisatebean)));
    req.setAttribute("mr_no", mrno);

    return mapping.findForward("addshow");

  }

  /**
   * create: POST method to create a new Prescription.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {
    FlashScope flash = FlashScope.getScope(req);

    // error redirect: to the same page; from the referer header.
    ActionRedirect errRedirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    errRedirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    // success redirect: back to the dashboard or wherever "add" was called from
    ActionRedirect redirect;
    if (req.getParameter("Referer") != null) {
      redirect = new ActionRedirect(
          req.getParameter("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    } else {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    ArrayList errors = new ArrayList();
    String userid = (String) req.getSession(false).getAttribute("userid");
    DialysisPrescriptionsDAO dao = new DialysisPrescriptionsDAO();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors);
    int prescriptionId = dao.getNextSequence();
    bean.set("dialysis_presc_id", prescriptionId);
    bean.set("username", userid);
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    if (req.getParameter("status").equals("I")) {
      bean.set("deactivated_by", userid);
      bean.set("deactivated_time", DateUtil.getCurrentTimestamp());
    }

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      // check for active|| Pending Prescription if exists
      String status = req.getParameter("status");
      String mrno = req.getParameter("mr_no");
      boolean statusExists = dao.checkForDuplicateStatus(con, mrno, status);
      status = status.equals("A") ? "Active" : "Pending";
      // Insert the Prescription
      if (statusExists) {
        flash.put("error", "MRNO: " + mrno + " with status " + status + " already exists");
        return errRedirect;
      }
      boolean success = dao.insert(con, bean);
      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }
      success = saveAccessTypes(req, res, new Integer(prescriptionId).toString(), con);
      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }

      allSuccess = true;
      flash.put("success", "Prescription Added Successfully");
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    return redirect;
  }

  /**
   * update: POST method to update an existing Prescription..
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {

    FlashScope flash = FlashScope.getScope(req);

    // error redirect: to the same page; from the referer header.
    ActionRedirect errRedirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    errRedirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    // success redirect: back to the dashboard or wherever "add" was called from
    ActionRedirect redirect;
    if (req.getParameter("Referer") != null) {
      redirect = new ActionRedirect(
          req.getParameter("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    } else {
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    String user = (String) req.getSession(false).getAttribute("userid");
    ArrayList errors = new ArrayList();
    DialysisPrescriptionsDAO dao = new DialysisPrescriptionsDAO();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(req.getParameterMap(), bean, errors);

    String heparintype = req.getParameter("heparin_type");
    if (heparintype.equals("h")) {
      bean.set("low_heparin_initial_dose", null);
      bean.set("low_heparin_intrim_dose", null);
    }

    String prescriptionId = req.getParameter("dialysis_presc_id");
    bean.set("username", user);
    if (req.getParameter("status").equals("I")) {
      bean.set("deactivated_by", user);
      bean.set("deactivated_time", DateUtil.getCurrentTimestamp());
    }

    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      // Update the Prescription
      boolean success = (1 == dao.update(con, bean.getMap(), "dialysis_presc_id",
          Integer.parseInt(prescriptionId)));
      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }
      success = saveAccessTypes(req, res, prescriptionId, con);
      if (!success) {
        flash.put("error", "Transaction Failure");
        return errRedirect;
      }

      allSuccess = true;
      flash.put("success", "Prescription updated successfully");

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    return redirect;
  }

  /**
   * Gets the mrno prescription details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the mrno prescription details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getMrnoPrescriptionDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

    String mrno = req.getParameter("mr_no");
    String prescriptionId = req.getParameter("dialysis_presc_id");

    List<BasicDynaBean> presDetailsList = DialysisPrescriptionsDAO.getPrescriptionDetails(mrno,
        prescriptionId);

    if (presDetailsList.size() > 0) {
      req.setAttribute("presDetails", presDetailsList.get(0));
    }

    req.setAttribute("mr_no", mrno);
    return mapping.findForward("addshow");

  }

  /**
   * Save access types.
   *
   * @param request the request
   * @param response the response
   * @param prescriptionId the prescription id
   * @param con the con
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private boolean saveAccessTypes(HttpServletRequest request, HttpServletResponse response,
      String prescriptionId, Connection con) throws SQLException, IOException {

    GenericDAO tempAccessDAO = new GenericDAO("temporary_access_types");
    GenericDAO permntAccessDAO = new GenericDAO("permanent_access_types");
    ArrayList errorFields = new ArrayList();
    String[] isNewTemp = request.getParameterValues("added4temp");
    String[] isDeletedTemp = request.getParameterValues("selectedrow4temp");
    String[] isNewPmnt = request.getParameterValues("added4per");
    String[] isDeletedPmnt = request.getParameterValues("selectedrow4per");
    String userid = (String) request.getSession(false).getAttribute("userid");

    boolean accessTypeSuccess = false;

    for (int i = 0; i < request.getParameterValues("access_type_id_t").length - 1; i++) {
      BasicDynaBean tempAccessBean = tempAccessDAO.getBean();
      String[] tempAccessIds = request.getParameterValues("temporary_access_type_id");
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, tempAccessBean,
          errorFields);
      tempAccessBean.set("username", userid);
      if (isNewTemp[i].equalsIgnoreCase("Y") && isDeletedTemp[i].equalsIgnoreCase("false")) {
        tempAccessBean.set("temporary_access_type_id", tempAccessDAO.getNextSequence());
        tempAccessBean.set("mr_no", request.getParameter("mr_no"));
        tempAccessBean.set("dialysis_presc_id", Integer.parseInt(prescriptionId));

        accessTypeSuccess = tempAccessDAO.insert(con, tempAccessBean);
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      } else if (isNewTemp[i].equalsIgnoreCase("N") && isDeletedTemp[i].equalsIgnoreCase("true")) {
        accessTypeSuccess = tempAccessDAO.delete(con, "temporary_access_type_id",
            Integer.parseInt(tempAccessIds[i]));
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      } else if (isNewTemp[i].equalsIgnoreCase("N")) {
        accessTypeSuccess = (1 == tempAccessDAO.update(con, tempAccessBean.getMap(),
            "temporary_access_type_id", Integer.parseInt(tempAccessIds[i])));
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      }

    }

    for (int i = 0; i < request.getParameterValues("access_type_id_p").length - 1; i++) {
      BasicDynaBean pmntAccessBean = permntAccessDAO.getBean();
      String[] pmntAccessIds = request.getParameterValues("permanent_access_type_id");
      ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, pmntAccessBean,
          errorFields);
      pmntAccessBean.set("username", userid);
      if (isNewPmnt[i].equalsIgnoreCase("Y") && isDeletedPmnt[i].equalsIgnoreCase("false")) {
        pmntAccessBean.set("permanent_access_type_id", tempAccessDAO.getNextSequence());
        pmntAccessBean.set("mr_no", request.getParameter("mr_no"));
        pmntAccessBean.set("dialysis_presc_id", Integer.parseInt(prescriptionId));

        accessTypeSuccess = permntAccessDAO.insert(con, pmntAccessBean);
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      } else if (isNewPmnt[i].equalsIgnoreCase("N") && isDeletedPmnt[i].equalsIgnoreCase("true")) {
        accessTypeSuccess = permntAccessDAO.delete(con, "permanent_access_type_id",
            Integer.parseInt(pmntAccessIds[i]));
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      } else if (isNewPmnt[i].equalsIgnoreCase("N")) {
        accessTypeSuccess = (1 == permntAccessDAO.update(con, pmntAccessBean.getMap(),
            "permanent_access_type_id", Integer.parseInt(pmntAccessIds[i])));
        if (!accessTypeSuccess) {
          return accessTypeSuccess;
        }
      }

    }
    return true;

  }
}
