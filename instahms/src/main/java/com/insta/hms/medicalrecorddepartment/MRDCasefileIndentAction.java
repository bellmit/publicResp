package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MRDCasefileIndentAction extends BaseAction {

  static Logger log = LoggerFactory
      .getLogger(MRDCasefileIndentAction.class);

  /**
   * Raise MRD indent screen.
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
   * @throws SQLException
   *           the SQL exception
   */
  
  @IgnoreConfidentialFilters
  public ActionForward raiseMRDIndentScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, SQLException {
    HttpSession session = req.getSession();
    JSONSerializer js = new JSONSerializer();
    String userName = (String) session.getAttribute("userid");
    List depUnit = MRDCasefileIndentDAO.getDepartmentUnits();
    req.setAttribute("deptlist",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(MRDCasefileIndentDAO.getDeptNames())));
    req.setAttribute("depUnitList", js.serialize(depUnit));
    req.setAttribute("userName", userName);
    return mapping.findForward("raiseIndent");
  }

  /**
   * Raise MRD casefile indent.
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
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   * @throws FileUploadException
   *           the file upload exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward raiseMRDCasefileIndent(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws ServletException, SQLException, FileUploadException, IOException, ParseException {
    MRDCasefileIndentDAO indentDao = new MRDCasefileIndentDAO();
    BasicDynaBean indentBean = indentDao.getBean();
    String[] mrnos = req.getParameterValues("mrNo");
    String indentDate = req.getParameter("indent_date");
    String indentTime = req.getParameter("indent_time");
    String remarks = req.getParameter("remarks");
    String[] reqDept = req.getParameterValues("deptId");
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = null;
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < mrnos.length; i++) {
        if (mrnos[i] != null && !mrnos[i].equals("")) {
          Map indentMap = new HashMap();

          indentMap.put("indented", "Y");
          indentMap.put("requested_by", req.getSession().getAttribute("userid").toString());
          indentMap.put("request_date", DateUtil.parseTimestamp(indentDate, indentTime));
          indentMap.put("remarks", remarks);
          indentMap.put("requesting_dept", reqDept[i]);

          Map keys = new HashMap();
          keys.put("mr_no", mrnos[i].toString());
          int update = indentDao.update(con, indentMap, keys);

          if (update > 0) {
            success = true;
          } else {
            success = false;
          }
        }
      }
      if (success) {
        con.commit();
        flash.success("Indent Raised successfully..");
        redirect = new ActionRedirect(mapping.findForward("success"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        con.rollback();
        flash.error("Failed to Raise Indent..");
        return mapping.findForward("closeindentscreen");
      }

    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  /**
   * Gets the MRD casefiles.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the MRD casefiles
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  
  @IgnoreConfidentialFilters
  public ActionForward getMRDCasefiles(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException, ParseException {
    String mrno = req.getParameter("mrno");
    List listBean = MRDCasefileIndentDAO.getMRDCasefileDetails(mrno);
    List listMap = ConversionUtils.listBeanToListMap(listBean);
    JSONSerializer js = new JSONSerializer();
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(listMap));

    return null;
  }

  /**
   * Close indent casefile screen.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward closeIndentCasefileScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException {
    HttpSession session = req.getSession();
    String userName = (String) session.getAttribute("userid");
    req.setAttribute("userName", userName);
    return mapping.findForward("closeindentscreen");
  }

  /**
   * Close casefile indent.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward closeCasefileIndent(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, SQLException, ParseException {
    MRDCasefileIndentDAO indentDao = new MRDCasefileIndentDAO();
    BasicDynaBean indentBean = indentDao.getBean();
    String[] mrnos = req.getParameterValues("mrNo");
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = null;
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      for (int i = 0; i < mrnos.length; i++) {
        if (mrnos[i] != null && !mrnos[i].equals("")) {
          Map indentMap = new HashMap();
          indentMap.put("indented", "N");
          indentMap.put("requested_by", "");
          indentMap.put("request_date", null);
          indentMap.put("requesting_dept", null);

          Map keys = new HashMap();
          keys.put("mr_no", mrnos[i].toString());

          int update = indentDao.update(con, indentMap, keys);

          if (update > 0) {
            success = true;
          } else {
            success = false;
          }
        }
      }

      if (success) {
        con.commit();
        flash.success("Indent Closed successfully..");
        redirect = new ActionRedirect(mapping.findForward("success"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        con.rollback();
        flash.error("Failed to Close Indent..");
        return mapping.findForward("closeindentscreen");
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }
}
