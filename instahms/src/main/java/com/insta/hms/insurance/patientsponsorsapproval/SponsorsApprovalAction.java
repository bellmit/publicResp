package com.insta.hms.insurance.patientsponsorsapproval;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.MasterAction;
import com.insta.hms.master.MasterDAO;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class SponsorsApprovalAction.
 *
 * @author prasanna.kumar
 */

public class SponsorsApprovalAction extends MasterAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SponsorsApprovalAction.class);

  /** The sponsor approval dao. */
  SponsorApprovalDAO sponsorApprovalDao = new SponsorApprovalDAO();

  /** The sponsor approval details dao. */
  SponsorApprovalDetailsDAO sponsorApprovalDetailsDao = new SponsorApprovalDetailsDAO();

  /** The center dao. */
  CenterMasterDAO centerDao = new CenterMasterDAO();

  /** The sponsor approval doc DAO. */
  GenericDAO sponsorApprovalDocDAO = new GenericDAO("patient_sponsor_approvals_docs");
  
  private static final GenericDAO masterTimeStampDAO = new GenericDAO("master_timestamp");

  /**
   * Get Master DAO.
   *
   * @return the master dao
   */
  @Override
  public MasterDAO getMasterDao() {
    return sponsorApprovalDao;
  }

  /**
   * Returns getDetailsDAO.
   *
   * @return the details dao
   */
  @Override
  public MasterDAO getDetailsDao() {
    return sponsorApprovalDetailsDao;
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {

    ActionForward forward = super.show(mapping, form, req, resp);
    setShowReqAttributes(req);
    return forward;
  }

  /**
   * Sets the show req attributes.
   *
   * @param req the new show req attributes
   * @throws SQLException the SQL exception
   */
  private void setShowReqAttributes(HttpServletRequest req) throws SQLException {
    BasicDynaBean bean = (BasicDynaBean) req.getAttribute("bean");

    String mrNo = req.getParameter("mr_no");
    if (mrNo == null || mrNo.equals("")) {
      mrNo = (String) bean.get("mr_no");
    }

    String sponApprId = req.getParameter("sponsor_approval_id");
    BasicDynaBean fileBean = sponsorApprovalDocDAO.getBean();
    sponsorApprovalDocDAO.loadByteaRecords(fileBean, "sponsor_approval_id",
        Integer.parseInt(sponApprId));
    req.setAttribute("fileBean", fileBean);

    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    BasicDynaBean mst = masterTimeStampDAO.getRecord();
    req.setAttribute("masterTimeStamp", mst.get("master_count"));

    List<BasicDynaBean> patientgls = sponsorApprovalDao.getSponsorApprovals(mrNo);
    req.setAttribute("patientgls", patientgls);

    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("patientGlsJson",
        js.deepSerialize(ConversionUtils.listBeanToListMap(patientgls)));

    req.setAttribute("orgNames",
        ConversionUtils.listBeanToListMap(sponsorApprovalDao.getRatePlanList(sponApprId)));

    Integer maxCentersIncDefault =
        (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      req.setAttribute("centers", centerDao.getAllCenters());
    }
  }

  /**
   * Add.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {

    ActionForward forward = super.add(mapping, form, req, resp);
    setAddReqAttributes(req);
    return forward;
  }

  /**
   * Sets the adds the req attributes.
   *
   * @param req the new adds the req attributes
   * @throws SQLException the SQL exception
   */
  private void setAddReqAttributes(HttpServletRequest req) throws SQLException {
    String mrNo = req.getParameter("mr_no");

    List<BasicDynaBean> patientgls = sponsorApprovalDao.getSponsorApprovals(mrNo);
    req.setAttribute("patientgls", patientgls);

    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    BasicDynaBean mst = masterTimeStampDAO.getRecord();
    req.setAttribute("masterTimeStamp", mst.get("master_count"));

    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("patientGlsJson",
        js.deepSerialize(ConversionUtils.listBeanToListMap(patientgls)));

    req.setAttribute("orgNames",
        ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations()));

    Integer maxCentersIncDefault =
        (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
    } else {
      req.setAttribute("centers", centerDao.getAllCenters());
    }

    BasicDynaBean primaryCenter = sponsorApprovalDao.getPrimaryCenter(mrNo);
    Integer primarycenterId =
        primaryCenter == null ? null : (Integer) primaryCenter.get("primary_center_id");
    req.setAttribute("primaryCenter", primarycenterId);
  }

  /**
   * Create.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {

    ActionForward forward = super.create(mapping, form, req, resp);
    insertUpdateDocument(form, req);

    return forward;
  }

  /**
   * update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param resp the resp
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {

    ActionForward forward = super.update(mapping, form, req, resp);
    insertUpdateDocument(form, req);

    return forward;
  }

  /**
   * Insert update document.
   *
   * @param form the form
   * @param req the req
   * @throws SQLException the SQL exception
   * @throws FileNotFoundException the file not found exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void insertUpdateDocument(ActionForm form, HttpServletRequest req)
      throws SQLException, FileNotFoundException, IOException {

    Connection con = null;
    boolean success = false;
    int count = 0;
    boolean fileRecordExists = false;
    BasicDynaBean documentBean = sponsorApprovalDocDAO.getBean();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      documentBean = insertUpdateDocumentBean(form, req);
      if (null != documentBean) {
        fileRecordExists = sponsorApprovalDocDAO.exist("sponsor_approval_id",
            documentBean.get("sponsor_approval_id"));
        if (fileRecordExists) {
          count = sponsorApprovalDocDAO.updateWithName(con, documentBean.getMap(),
              "sponsor_approval_id");
        } else {
          success = sponsorApprovalDocDAO.insert(con, documentBean);
        }
      }
    } finally {
      if (count > 0 || success) {
        success = true;
      }
      DataBaseUtil.commitClose(con, success);
    }
  }

  /**
   * Insert update document bean.
   *
   * @param form the form
   * @param req the req
   * @return the basic dyna bean
   * @throws FileNotFoundException the file not found exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean insertUpdateDocumentBean(ActionForm form, HttpServletRequest req)
      throws FileNotFoundException, IOException, SQLException {

    SponsorApprovalDocForm docForm = (SponsorApprovalDocForm) form;
    BasicDynaBean bean = (BasicDynaBean) req.getAttribute("bean");
    Integer sponsorApprovalId =
        (null != bean) ? (Integer) bean.get("sponsor_approval_id") : Integer.valueOf(0);

    BasicDynaBean docBean = null;
    FormFile sponsorDoc = docForm.getSponsor_doc();
    if (null != sponsorDoc.getFileName() && !sponsorDoc.getFileName().equals("")) {
      docBean = sponsorApprovalDocDAO.getBean();
      docBean.set("sponsor_approval_id", sponsorApprovalId);
      docBean.set("attachment", sponsorDoc.getInputStream());
      docBean.set("attachment_content_type", sponsorDoc.getContentType());
      String fileName = sponsorDoc.getFileName();
      String extension = null;
      if (fileName.contains(".")) {
        extension = fileName.substring(fileName.indexOf(".") + 1);
      }
      docBean.set("attachment_extension", extension);
      docBean.set("file_name", sponsorDoc.getFileName());
    }
    return docBean;
  }

  /**
   * Show sponsor document.
   *
   * @param mapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showSponsorDocument(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String apprId = req.getParameter("sponsor_approval_id");

    BasicDynaBean uploadfileBean = sponsorApprovalDocDAO.getBean();
    sponsorApprovalDocDAO.loadByteaRecords(uploadfileBean, "sponsor_approval_id",
        Integer.parseInt(apprId));
    Map<String, Object> uploadMap = uploadfileBean.getMap();

    String contentType = (String) uploadMap.get("attachment_content_type");
    res.setContentType(contentType);

    OutputStream os = res.getOutputStream();

    InputStream is = (InputStream) uploadMap.get("attachment");
    if (is != null) {
      byte[] bytes = new byte[4096];
      int len = 0;
      while ((len = is.read(bytes)) > 0) {
        os.write(bytes, 0, len);
      }
      os.flush();
      is.close();
    }
    
    return null;
  }

  /**
   * Process previous months orders.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward processPreviousMonthsOrders(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String mrNo = (String) req.getParameter("mr_no");
    String validityStartDate = (String) req.getParameter("validity_start");
    String validityEndDate = (String) req.getParameter("validity_end");
    SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");

    Date validityStartDt = formatDate.parse(validityStartDate);
    Date validityEndDt = formatDate.parse(validityEndDate);

    Calendar cal = Calendar.getInstance();
    cal.setTime(validityEndDt);
    int maxday = cal.getActualMaximum(cal.DAY_OF_MONTH);
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), maxday);

    // get the previous month date range
    java.sql.Date[] prevMonthDateRange = DateUtil.getDateRange("pm");
    /**
     * this method we will use to process the last month approvals with creating credit notes
     * and new bills.
     */
    Set<String> newBillNos = sponsorApprovalDao
        .processAndCalculatePreviousMonthOrders(mrNo, prevMonthDateRange[0], prevMonthDateRange[1]);

    // process the current month
    java.sql.Date[] thisMonthDateRange = DateUtil.getDateRange("tm");
    /**
     * this method we will use for recalculating the approvals for current month. this will
     * directly upadtes the charges. it will not create the credit notes or new bills.
     */
    newBillNos.addAll(sponsorApprovalDao.processPreviousMonthsOrders(mrNo, thisMonthDateRange[0],
        thisMonthDateRange[1]));

    sponsorApprovalDao.processAllPreviousMonthBills(newBillNos);
    
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mrNo", mrNo);
    redirect.addParameter("sponsor_approval_id",
        (String) req.getParameter("sponsor_approval_id"));
    return redirect;

  }
}
