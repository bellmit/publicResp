package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;

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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class SendToTpaAction.
 */
public class SendToTpaAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SendToTpaAction.class);

  /** The dao. */
  private static GenericDAO dao = new GenericDAO("insurance_transaction");

  /**
   * Show.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   * @throws Exception        the exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException, Exception {

    String mrno = request.getParameter("mr_no");
    String patientid = request.getParameter("visit_id");
    String insuranceid = request.getParameter("insurance_id");

    List<EMRDoc> allDocs = new ArrayList<EMRDoc>();

    if (patientid != null && !(patientid.equals(""))) {
      for (EMRInterface.Provider provider : EMRInterface.Provider.values()) {
        List<EMRDoc> list = provider.getProviderImpl().listDocumentsByVisit(patientid);
        if (list != null && !list.isEmpty()) {
          allDocs.addAll(list);
        }
      }
    } else {
      for (EMRInterface.Provider provider : EMRInterface.Provider.values()) {
        List<EMRDoc> list = provider.getProviderImpl().listDocumentsByMrno(mrno);
        if (list != null && !list.isEmpty()) {
          allDocs.addAll(list);
        }
      }
    }
    // Insurance Documents

    List<EMRDoc> list = new InsuranceDocumentsProvider().listDocuments(insuranceid);
    if (list != null && !list.isEmpty()) {
      allDocs.addAll(list);
    }

    List docTypes = DocumentTypeMasterDAO.getDocTypeNames();

    request.setAttribute("patient_id", patientid);
    request.setAttribute("mr_no", mrno);
    request.setAttribute("insurance_id", insuranceid);
    request.setAttribute("docList", allDocs);
    request.setAttribute("docTypes", ConversionUtils.listBeanToMapBean(docTypes, "doc_type_id"));
    request.setAttribute("TPAEmailIds", InsuranceDAO.getTPAEmailIds(insuranceid));
    return mapping.findForward("show");
  }

  /**
   * Send to tpa.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param req      the req
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward sendToTpa(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response) throws Exception {

    String error = null;
    String success = null;
    boolean result = false;

    String[] strTransIds = req.getParameterValues("sendToTpa");

    Map params = req.getParameterMap();
    BasicDynaBean bean = dao.getBean();

    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    bean.set("user_id", req.getSession(false).getAttribute("userid").toString());
    if (errors.isEmpty()) {
      try {
        result = new InsuranceBO().sendToTPA(con, bean, strTransIds);
        if (result) {
          success = "Documents Successfully sent to TPA";
        } else {
          error = "Mail Sent Failed..";
        }
      } finally {
        DataBaseUtil.commitClose(con, result);
      }
    } else {
      error = "Mail Sent Failed..";
    }
    FlashScope flash = FlashScope.getScope(req);
    flash.put("success", success);
    flash.put("error", error);

    String path = "SendToTpa.do?_method=show";
    ActionRedirect redirect = new ActionRedirect(path);
    redirect.addParameter("insurance_id", req.getParameter("insurance_id"));
    redirect.addParameter("mr_no", req.getParameter("mr_no"));
    redirect.addParameter("visit_id", req.getParameter("patient_id"));

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }
}
