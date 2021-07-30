package com.insta.hms.diagnosticsmasters.outhousemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.modules.ModulesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/**
 * The Class OutHouseMasterAction.
 */
public class OutHouseMasterAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OutHouseMasterAction.class);
  private static final GenericDAO diagOutsourceMasterDAO = new GenericDAO("diag_outsource_master");

  /** The bo. */
  static OutHouseMasterBO bo = new OutHouseMasterBO();

  /**
   * Gets the out house master details.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the out house master details
   * @throws Exception
   *           the exception
   */
  public ActionForward getOutHouseMasterDetails(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    getDetails(mapping, af, request, response);
    return mapping.findForward("success");
  }

  /**
   * Gets the details.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the details
   * @throws Exception
   *           the exception
   */
  public ActionForward getDetails(ActionMapping mapping, ActionForm af, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    OutHouseMasterDAO dao = new OutHouseMasterDAO();
    CenterMasterDAO centerDao = new CenterMasterDAO();
    Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
    listingParams.put(LISTING.PAGESIZE, 10);
    PagedList pagedList = dao.getOutHouseDetails(request.getParameterMap(), listingParams);

    if (pagedList.getDtoList().size() > 0) {
      List<String> testIDs = new ArrayList<String>();
      for (Map obj : (List<Map>) pagedList.getDtoList()) {
        testIDs.add((String) obj.get("test_id"));
      }

      List<BasicDynaBean> details = new OutHouseMasterDAO()
          .getOutHouseTestDetails(request.getParameterMap(), testIDs);
      Map outsourceDetails = ConversionUtils.listBeanToMapListListBean(details, "test_id",
          "source_center_id");
      request.setAttribute("outsourceDetails", outsourceDetails);
    }

    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      request.setAttribute("centers", centerDao.getAllCenters());
    }
    request.setAttribute("outHouseNames", OutHouseMasterDAO.getAllOutSources());
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("centralLabModule",
        new ModulesDAO().findByKey("module_id", "mod_central_lab"));
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("testNames", js.serialize(dao.getAllTests()));
    return mapping.findForward("success");
  }

  /**
   * Adds the new out house.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward addNewOutHouse(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    OutHouseMasterDAO dao = new OutHouseMasterDAO();

    request.setAttribute("templateNames", dao.getAllTemplateNames());

    request.setAttribute("method", "addNewOutHouseDetails");
    request.setAttribute("activeOutHouseTestsExist", false);
    return mapping.findForward("outhouseDetails");
  }

  /**
   * Adds the new out house details.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward addNewOutHouseDetails(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map params = request.getParameterMap();
    List errors = new ArrayList();

    OutHouseMasterDAO dao = new OutHouseMasterDAO();
    BasicDynaBean bean = dao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    Connection con = null;
    String instaOuthouse = request.getParameter("insta_outhouse");
    String error = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (errors.isEmpty()) {
        boolean exists = dao.exist("oh_name", ((String) bean.get("oh_name")));

        if (exists) {
          error = "This Out House already exists.....";
        } else {
          bean.set("oh_id", dao.getNextOutHouseID());
          success = dao.insert(con, bean);
          if (!success) {
            error = "Fail to add Out House...";
          }
        }
      } else {
        error = "Incorrectly formatted values supplied..";
      }

      if (success) {
        BasicDynaBean outSourceBean = diagOutsourceMasterDAO.getBean();
        outSourceBean.set("outsource_dest_id", diagOutsourceMasterDAO.getNextSequence());
        outSourceBean.set("outsource_dest", bean.get("oh_id"));
        if (instaOuthouse != null && instaOuthouse.equals("IO")) {
          outSourceBean.set("outsource_dest_type", "IO");
        } else {
          outSourceBean.set("outsource_dest_type", "O");
        }
        outSourceBean.set("status", "A");
        success = diagOutsourceMasterDAO.insert(con, outSourceBean);
        request.setAttribute("outSourceBean", outSourceBean);
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addOutHouseDetails"));
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showOutHouseDetails"));
      redirect.addParameter("ohId", bean.get("oh_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Edits the outhouse details.
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
   */
  public ActionForward editOuthouseDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("internalLab"));
    return redirect;
  }

  /**
   * Gets the out house details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the out house details
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getOutHouseDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    OutHouseMasterDAO dao = new OutHouseMasterDAO();

    String ohId = (String) request.getParameter("ohId");
    BasicDynaBean bean = dao.findByKey("oh_id", ohId);
    request.setAttribute("bean", bean);
    request.setAttribute("templateName", (String) bean.get("template_name"));
    BasicDynaBean outSourceDestBean = diagOutsourceMasterDAO.findByKey("outsource_dest", ohId);
    int outSourceDestId = (Integer) outSourceDestBean.get("outsource_dest_id");

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("outsource_dest_id", outSourceDestId);
    keys.put("status", "A");

    List<BasicDynaBean> outhouseTestList = diagOutsourceMasterDAO.listAll(null,
        keys, null);
    boolean activeOutHouseTestsExist = false;
    activeOutHouseTestsExist = null != outhouseTestList && outhouseTestList.size() > 0;
    request.setAttribute("templateNames", dao.getAllTemplateNames());
    request.setAttribute("activeOutHouseTestsExist", activeOutHouseTestsExist);
    request.setAttribute("outSourceDestBean", outSourceDestBean);
    return mapping.findForward("outhouseDetails");
  }

  /**
   * Update out house details.
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
   */
  public ActionForward updateOutHouseDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    Connection con = null;
    OutHouseMasterDAO dao = new OutHouseMasterDAO();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = request.getParameterMap();
      List errors = new ArrayList();

      BasicDynaBean bean = dao.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);
      BasicDynaBean diagOutsourceBean =
          diagOutsourceMasterDAO.findByKey("outsource_dest", bean.get("oh_id"));
      String instaOuthouse = request.getParameter("insta_outhouse");

      String key = (String) request.getParameter("oh_id");
      Map<String, String> keys = new HashMap<String, String>();
      keys.put("oh_id", key);
      Map<String, String> diagOutsourceKeys = new HashMap<String, String>();
      diagOutsourceKeys.put("outsource_dest", (String) bean.get("oh_id"));
      FlashScope flash = FlashScope.getScope(request);

      if (errors.isEmpty()) {
        int success = dao.update(con, bean.getMap(), keys);
        if (diagOutsourceBean != null) {
          diagOutsourceBean.set("status", bean.get("status"));
          if (instaOuthouse != null && instaOuthouse.equals("IO")) {
            diagOutsourceBean.set("outsource_dest_type", "IO");
          } else {
            diagOutsourceBean.set("outsource_dest_type", "O");
          }
          success &=
              diagOutsourceMasterDAO.update(con, diagOutsourceBean.getMap(), diagOutsourceKeys);
        }
        if (success > 0) {
          con.commit();
          flash.success("Out house details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update out house details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showOutHouseDetails"));
      redirect.addParameter("ohId", key.toString());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Adds the new test to out house.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   */
  // To get Add Test to outhouse screen
  public ActionForward addNewTestToOutHouse(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    OutHouseMasterDAO dao = new OutHouseMasterDAO();
    CenterMasterDAO centerDao = new CenterMasterDAO();
    JSONSerializer json = new JSONSerializer().exclude("class");

    request.setAttribute("outhousedetail", OutHouseMasterDAO.getAllActiveOutSourceName());
    request.setAttribute("testDetails", dao.getAllTestNames());
    request.setAttribute("centerOutsourcesJSON",
        json.serialize(OutHouseMasterBO.getOutsourcesRespectToCenter()));
    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      request.setAttribute("centers", centerDao.getAllCenters());
    }

    return mapping.findForward("outhouseTestDetails");
  }

  /**
   * Insert test to outhouse.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  // To insert Outhouse Test Details
  public ActionForward insertTestToOuthouse(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    OutHouseMasterDAO outDao = new OutHouseMasterDAO();
    Map params = request.getParameterMap();
    String testID = (String) request.getParameter("test_id");

    String error = null;
    boolean success = false;

    if (outDao.isOuthouseTestExist(testID)) {
      error = "This Test already associated with outsources.....";
    } else {
      String errMessage = "";
      try {
        success = new OutHouseMasterBO().saveTestsAndOutsourceDetails(params);
      } catch (SQLException exp) {
        if ("P0001".equals(exp.getSQLState())) {
          errMessage = exp.getLocalizedMessage();
        } else {
          throw exp;
        }
      }
      if (!success) {
        if (!"".equals(errMessage)) {
          error = "Fail to add Test to  Out House..." + errMessage;
        } else {
          error = "Fail to add Test to  Out House...";
        }
      }
    }
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addNewTest"));
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showTest"));
      redirect.addParameter("testId", testID);
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;

  }

  /**
   * Show outhouse test details.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   */
  // To Show update Test charge screen
  public ActionForward showOuthouseTestDetails(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    CenterMasterDAO centerDao = new CenterMasterDAO();
    JSONSerializer json = new JSONSerializer().exclude("class");
    int centerID = (Integer) request.getSession(false).getAttribute("centerId");
    String outsourcedestId = (String) request.getParameter("outsourceDestId");
    String testId = (String) request.getParameter("testId");
    OutHouseMasterDAO outSourceDao = new OutHouseMasterDAO();
    List<BasicDynaBean> outhouseTests = outSourceDao.getOuthouseTestDetails(testId);
    request.setAttribute("outhouseTests", outhouseTests);
    request.setAttribute("centerOutsourcesJSON",
        json.serialize(OutHouseMasterBO.getOutsourcesRespectToCenter()));
    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      request.setAttribute("centers", centerDao.getAllCenters());
    }

    return mapping.findForward("outhouseTestDetails");
  }

  /**
   * Update test charge.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  // To update edited charges of test
  public ActionForward updateTestCharge(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String outSourceDestId = (String) request.getParameter("outsourceDestId");
    String testId = (String) request.getParameter("test_id");
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = request.getParameterMap();
      List errors = new ArrayList();

      /*
       * BasicDynaBean bean = dao.getBean(); ConversionUtils.copyToDynaBean(params, bean, errors);
       * Map<String, Object> keys = new HashMap<String, Object>(); keys.put("outsource_dest_id",
       * Integer.parseInt(outSourceDestId)); keys.put("test_id", testId);
       */
      FlashScope flash = FlashScope.getScope(request);

      if (errors.isEmpty()) {
        boolean success = false;
        String errMessage = "";
        try {
          success = new OutHouseMasterBO().saveTestsAndOutsourceDetails(params);
        } catch (SQLException exp) {
          if ("P0001".equals(exp.getSQLState())) {
            errMessage = exp.getLocalizedMessage();
          } else {
            throw exp;
          }
        }
        if (success) {
          con.commit();
          flash.success("Test charges updated successfully..");
        } else {
          con.rollback();

          if (!"".equals(errMessage)) {
            flash.error("Failed to update Test charge details.." + errMessage);
          } else {
            flash.error("Failed to update Test charge details..");
          }
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showTest"));
      // redirect.addParameter("outsourceDestId", outSourceDestId);
      redirect.addParameter("testId", testId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Populate value to array.
   *
   * @param ohouse
   *          the ohouse
   * @param outhouse
   *          the outhouse
   * @return the string[]
   */
  public String[] populateValueToArray(String[] ohouse, ArrayList<String> outhouse) {
    Iterator<String> it = outhouse.iterator();
    String[] ohouseNew = new String[outhouse.size()];
    int counter = 0;
    while (it.hasNext()) {
      ohouseNew[counter++] = it.next();
    }
    return ohouseNew;
  }

  /**
   * Adds the new internal lab.
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
   */
  public ActionForward addNewInternalLab(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    CenterMasterDAO centerDAO = new CenterMasterDAO();

    request.setAttribute("centers", centerDAO.getAllCentersExceptSuper());
    request.setAttribute("activeOutHouseTestsExist", false);
    return mapping.findForward("internalLab");
  }

  /**
   * Show internal lab.
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
   */
  public ActionForward showInternalLab(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    String outsourceDestId = (String) request.getParameter("outsourceDestId");
    BasicDynaBean bean =
        diagOutsourceMasterDAO.findByKey("outsource_dest_id", Integer.parseInt(outsourceDestId));
    request.setAttribute("bean", bean);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("outsource_dest_id", Integer.parseInt(outsourceDestId));
    keys.put("status", "A");
    BasicDynaBean outhouseTest = new GenericDAO("diag_outsource_detail").findByKey(keys);
    boolean activeOutHouseTestsExist = (null != outhouseTest);
    request.setAttribute("activeOutHouseTestsExist", activeOutHouseTestsExist);
    return mapping.findForward("internalLab");
  }

  /**
   * Creates the internal lab.
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
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward createInternalLab(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, SQLException, IOException, ParseException {

    Map params = request.getParameterMap();
    List errors = new ArrayList();

    BasicDynaBean bean = diagOutsourceMasterDAO.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    Connection con = null;

    String error = null;
    boolean success = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (errors.isEmpty()) {
        boolean exists =
            diagOutsourceMasterDAO.exist("outsource_dest", ((String) (bean.get("outsource_dest"))));
        if (exists) {
          error = "This Internal Lab already exists.....";
        } else {
          bean.set("outsource_dest_id", diagOutsourceMasterDAO.getNextSequence());
          bean.set("outsource_dest_type", "C");
          success = diagOutsourceMasterDAO.insert(con, bean);
          if (!success) {
            error = "Fail to add Internal Lab....";
          }
        }
      } else {
        error = "Incorrectly formatted values supplied..";
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      redirect = new ActionRedirect(mapping.findForward("addInternalLab"));
      flash.error(error);

    } else {
      redirect = new ActionRedirect(mapping.findForward("showInternalLab"));
      redirect.addParameter("outsourceDestId", bean.get("outsource_dest_id"));
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Update internal lab.
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
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward updateInternalLab(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, SQLException, IOException, ParseException {

    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map params = request.getParameterMap();
      List errors = new ArrayList();

      BasicDynaBean bean = diagOutsourceMasterDAO.getBean();
      ConversionUtils.copyToDynaBean(params, bean, errors);

      Integer key = Integer.parseInt(request.getParameter("outsource_dest_id"));
      Map<String, Integer> keys = new HashMap<String, Integer>();
      keys.put("outsource_dest_id", key);
      FlashScope flash = FlashScope.getScope(request);

      if (errors.isEmpty()) {
        int success = diagOutsourceMasterDAO.update(con, bean.getMap(), keys);
        if (success > 0) {
          con.commit();
          flash.success("Internal lab details updated successfully..");
        } else {
          con.rollback();
          flash.error("Failed to update internal lab details..");
        }
      } else {
        flash.error("Incorrectly formatted values supplied");
      }
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showInternalLab"));
      redirect.addParameter("outsourceDestId", key.toString());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The out house details handler. */
  private static TableDataHandler outHouseDetailsHandler = null;

  /**
   * Gets the out house details data handler.
   *
   * @return the out house details data handler
   */
  protected TableDataHandler getOutHouseDetailsDataHandler() {
    if (outHouseDetailsHandler == null) {
      outHouseDetailsHandler = new TableDataHandler("outhouse_master", // table name
          new String[] { "oh_id" }, // keys
          new String[] { "oh_name", "template_name", "clia_no", "oh_address", "status" },
          new String[][] { /* masters */ }, null);
    }
    outHouseDetailsHandler.setIdValAsString(true);
    outHouseDetailsHandler.setSequenceName("outhouseid_sequence");
    return outHouseDetailsHandler;
  }

  /**
   * Export out house details.
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
   * @throws Exception
   *           the exception
   */
  public ActionForward exportOutHouseDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    getOutHouseDetailsDataHandler().exportTable(res);
    return null;
  }

  /**
   * Import out house details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward importOutHouseDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    FlashScope flash = FlashScope.getScope(req);
    String referer = req.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    OutHouseMasterForm uploadForm = (OutHouseMasterForm) form;
    OutHouseMasterDAO outDao = new OutHouseMasterDAO();
    InputStreamReader isReader = new InputStreamReader(
        uploadForm.getUploadOutHouseDetailsFile().getInputStream());

    StringBuilder infoMsg = new StringBuilder();
    String error = getOutHouseDetailsDataHandler().importTable(isReader, infoMsg);

    String outHouseId = outDao.getNewlyAddedOuthouseId();
    if (null != outHouseId) {
      Connection con = null;
      boolean success = false;
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        BasicDynaBean bean = diagOutsourceMasterDAO.getBean();
        bean.set("outsource_dest_id", diagOutsourceMasterDAO.getNextSequence());
        bean.set("outsource_dest", outHouseId);
        bean.set("outsource_dest_type", "O");
        bean.set("status", "A");
        success = diagOutsourceMasterDAO.insert(con, bean);
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

    if (error != null) {
      flash.put("error", error);
      return redirect;
    }

    flash.put("info", infoMsg.toString());
    return redirect;
  }

  /** The charge handler. */
  private static TableDataHandler chargeHandler = null;

  /**
   * Gets the charge data handler.
   *
   * @return the charge data handler
   * @throws SQLException
   *           the SQL exception
   */
  protected TableDataHandler getChargeDataHandler() throws SQLException {
    if (chargeHandler == null) {
      chargeHandler = new TableDataHandler("diag_outsource_detail", // table name
          new String[] { "outsource_dest_id", "test_id", "source_center_id" }, // keys
          new String[] { "default_outsource", "status", "charge" }, new String[][] { /* masters */
              // our field ref table ref table id field ref table name field
              { "outsource_dest_id", "outsource_names", "outsource_dest_id", "outsource_name" },
              { "test_id", "diagnostics", "test_id", "test_name" },
              { "source_center_id", "hospital_center_master", "center_id", "center_name" } },
          new String[] { "diag_outsource_detail.status = 'A'" });
    }
    chargeHandler.setAlias("source_center_id", "source_center_name");
    chargeHandler.setMasterDataForCorrValue(new Object[][] { { "outsource_dest_id",
        "source_center_id",
        OutHouseMasterDAO.listBeanToMapListVal(OutHouseMasterDAO.getOutsourceIDsRespectToCenter(),
            "source_center_id", "outsource_dest_id"),
        "Outsource is not associated with source center.." } });
    return chargeHandler;
  }

  /**
   * Export charges.
   *
   * @param actionMapping
   *          the action mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward exportCharges(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    getChargeDataHandler().exportTable(res);
    return null;
  }

  /**
   * Import charges.
   *
   * @param actioMapping
   *          the actio mapping
   * @param actionForm
   *          the action form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward importCharges(ActionMapping actioMapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    FlashScope flash = FlashScope.getScope(req);
    String referer = req.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    OutHouseMasterForm uploadForm = (OutHouseMasterForm) actionForm;
    InputStreamReader isReader = new InputStreamReader(
        uploadForm.getUploadChargeFile().getInputStream());

    StringBuilder infoMsg = new StringBuilder();
    String error = getChargeDataHandler().importTable(isReader, infoMsg);

    if (error != null) {
      flash.put("error", error);
      return redirect;
    }

    flash.put("info", infoMsg.toString());
    return redirect;
  }

  /**
   * Gets the out house list.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the out house list
   * @throws Exception
   *           the exception
   */
  public ActionForward getOutHouseList(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    OutHouseMasterDAO dao = new OutHouseMasterDAO();
    Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
    PagedList pagedList = dao.getOutHouseLIst(request.getParameterMap(), listingParams);

    request.setAttribute("outHouseNames", OutHouseMasterDAO.getAllOutSources());
    request.setAttribute("pagedList", pagedList);
    request.setAttribute("centralLabModule",
        new ModulesDAO().findByKey("module_id", "mod_central_lab"));
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("testNames", js.serialize(dao.getAllTests()));

    return mapping.findForward("list");
  }

}
