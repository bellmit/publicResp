package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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

/**
 * The Class UploadReceivedDocsAction.
 *
 * @author pragna.p
 */
public class UploadReceivedDocsAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(UploadReceivedDocsAction.class);

  /** The tpadocsdao. */
  private static GenericDAO tpadocsdao = new GenericDAO("insurance_tpa_docs");

  /**
   * Adds the.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {

    request.setAttribute("insurance_id", request.getParameter("insurance_id"));
    request.setAttribute("mr_no", request.getParameter("mr_no"));
    request.setAttribute("visit_id", request.getParameter("visit_id"));

    return mapping.findForward("add");
  }

  /**
   * Upload.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws ServletException    the servlet exception
   * @throws SQLException        the SQL exception
   * @throws FileUploadException the file upload exception
   * @throws ParseException      the parse exception
   */
  public ActionForward upload(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, FileUploadException, ParseException {

    Map<String, Object[]> params = getParameterMap(request);

    String path = "UploadReceivedDocs.do?_method=add";
    ActionRedirect redirect = new ActionRedirect(path);
    redirect.addParameter("insurance_id", params.get("insurance_id")[0]);
    redirect.addParameter("mr_no", params.get("mr_no")[0]);
    redirect.addParameter("visit_id", params.get("visit_id")[0]);
    FlashScope flash = FlashScope.getScope(request);

    String error = null;
    String success = null;

    if (params.get("fileSizeError") != null) {
      // if the file size is greater than 10 MB prompting the user with the failure message.
      error = (String) params.get("fileSizeError")[0];
      flash.put("error", error);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    List errors = new ArrayList();

    boolean result = false;

    params.put("doc_recd_date",
        new Object[] { new java.sql.Date((new java.util.Date()).getTime()) });

    params.put("created_by",
        new Object[] { request.getSession(false).getAttribute("userid").toString() });
    BasicDynaBean bean = tpadocsdao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errors);

    if (params.get("fileName")[0].toString().contains(".")) {
      String fileName = params.get("fileName")[0].toString();
      String extension = fileName.substring(fileName.indexOf(".") + 1);
      bean.set("original_extension", extension);

      if (extension.equals("odt") || extension.equals("ods")) {
        bean.set("content_type", "application/vnd.oasis.opendocument.text");
      }
    }

    if (errors.isEmpty()) {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try {

        // insert
        bean.set("tpa_doc_id", tpadocsdao.getNextSequence());
        result = tpadocsdao.insert(con, bean);

        if (result) {
          success = "Uploaded successfully..";
        } else {
          error = "Failed to upload..";
        }

      } finally {
        DataBaseUtil.commitClose(con, result);
      }
    } else {
      error = "Incorrectly formatted details supplied..";
    }
    flash.put("success", success);
    flash.put("error", error);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Gets the parameter map.
   *
   * @param request the request
   * @return the parameter map
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws FileUploadException the file upload exception
   */
  public Map<String, Object[]> getParameterMap(HttpServletRequest request)
      throws IOException, FileUploadException {
    Map<String, Object[]> params = new HashMap<String, Object[]>();

    if (request.getContentType() != null
        && request.getContentType().split("/")[0].equals("multipart")) {
      // Create a factory for disk-based file items
      DiskFileItemFactory factory = new DiskFileItemFactory();

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      List<FileItem> items = upload.parseRequest(request);

      // Process the uploaded items
      Iterator iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = (FileItem) iter.next();

        if (item.isFormField()) {
          String name = item.getFieldName();
          String value = item.getString();
          params.put(name, new Object[] { value });

        } else {
          String fieldName = item.getFieldName();
          String fileName = item.getName();
          boolean isInMemory = item.isInMemory();
          long sizeInBytes = item.getSize();

          if (!fileName.equals("")) {
            if (sizeInBytes > 10 * 1024 * 1024) {
              params.put("fileSizeError",
                  new Object[] { "Unable to upload the file: " + "file size greater than 10 MB" });
            }
            params.put(fieldName, new InputStream[] { item.getInputStream() });
            params.put("content_type",
                new Object[] { MimeTypeDetector.getMimeTypes(item.getInputStream()) });
            params.put("fileName", new String[] { fileName });
          }
        }
      }
    } else {
      params.putAll(request.getParameterMap());
    }

    return params;
  }
}
