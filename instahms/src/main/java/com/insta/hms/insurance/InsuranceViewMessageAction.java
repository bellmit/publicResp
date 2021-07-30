package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InsuranceViewMessageAction.
 */
public class InsuranceViewMessageAction extends DispatchAction {

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

    GenericDAO dao = new GenericDAO("insurance_transaction");
    String transactionId = request.getParameter("transaction_id");
    BasicDynaBean bean = dao.findByKey("transaction_id", Integer.parseInt(transactionId));

    List<BasicDynaBean> attachbean = InsuranceDAO
        .getTransactionDocs(Integer.parseInt(transactionId));

    request.setAttribute("msgInfo", bean);
    request.setAttribute("DocsInfo", attachbean);
    request.setAttribute("visit_id", request.getParameter("visit_id"));
    request.setAttribute("mr_no", request.getParameter("mr_no"));
    request.setAttribute("insurance_id", request.getParameter("insurance_id"));
    return mapping.findForward("show");
  }

  /**
   * Gets the document.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the document
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   * @throws Exception        the exception
   */
  public ActionForward getDocument(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException, Exception {

    String docType = request.getParameter("doc_type");
    String attachmentId = request.getParameter("attachment_id");
    String mrNo = request.getParameter("mr_no");
    byte[] bytes = null;
    String fileName = "";

    if (docType.equals("R")) {

      GenericDAO dao = new GenericDAO("insurance_tpa_docs");
      BasicDynaBean bean = dao.getBean();
      dao.loadByteaRecords(bean, "tpa_doc_id", Integer.parseInt(attachmentId));
      if (bean.get("content_type") == null) {
        response.setContentType("image/gif");
      } else {
        response.setContentType(bean.get("content_type").toString());
      }

      fileName = mrNo + "_" + bean.get("document_name").toString();

      if (bean.get("original_extension") != null) {
        fileName = fileName + "." + bean.get("original_extension").toString();

      }

      response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
      bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("doc_content"));

    } else {

      GenericDAO dao = new GenericDAO("insurance_transaction_attachments");
      BasicDynaBean bean = dao.getBean();
      dao.loadByteaRecords(bean, "attachment_id", Integer.parseInt(attachmentId));
      response.setContentType(bean.get("content_type").toString());
      fileName = mrNo + "_" + bean.get("doc_title").toString();

      if (bean.get("content_type").equals("application/rtf")) {
        fileName = fileName + ".rtf";
      } else {
        fileName = fileName + ".pdf";
      }

      response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
      bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("attachment_data"));
    }

    OutputStream stream = response.getOutputStream();
    stream.write(bytes);
    stream.flush();
    stream.close();

    return null;

  }
}
