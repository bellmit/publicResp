package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InsuranceHistoryAction.
 */
public class InsuranceHistoryAction extends DispatchAction {

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

    String mrNo = request.getParameter("mr_no");
    String patientId = request.getParameter("visit_id");
    String insuranceId = request.getParameter("insurance_id");

    List list = InsuranceDAO.getInsHistory(Integer.parseInt(insuranceId));
    List attachments = InsuranceDAO.getInsAttachements(Integer.parseInt(insuranceId));

    request.setAttribute("visit_id", patientId);
    request.setAttribute("mr_no", mrNo);
    request.setAttribute("insurance_id", insuranceId);
    request.setAttribute("HistoryList", list);
    request.setAttribute("AttacchmentsList", attachments);

    return mapping.findForward("show");
  }

  /**
   * Delete recd doc.
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
  public ActionForward deleteRecdDoc(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException, Exception {

    String error = null;
    String success = null;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean result = false;

    String[] strDocIds = request.getParameterValues("deleteRecdDoc");

    try {
      GenericDAO dao = new GenericDAO("insurance_tpa_docs");
      for (int i = 0; i < strDocIds.length; i++) {
        result = dao.delete(con, "tpa_doc_id", Integer.parseInt(strDocIds[i]));

        if (!result) {
          break;
        }
      }

      if (result) {
        success = "Document Deleted Successfully..";
      } else {
        error = "Failed to delete..";
      }

    } finally {
      DataBaseUtil.commitClose(con, result);
    }

    String path = "InsuranceHistory.do?_method=show";
    ActionRedirect redirect = new ActionRedirect(path);
    redirect.addParameter("insurance_id", request.getParameter("insurance_id"));
    redirect.addParameter("mr_no", request.getParameter("mr_no"));
    redirect.addParameter("visit_id", request.getParameter("visit_id"));
    FlashScope flash = FlashScope.getScope(request);

    flash.put("success", success);
    flash.put("error", error);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }
}
