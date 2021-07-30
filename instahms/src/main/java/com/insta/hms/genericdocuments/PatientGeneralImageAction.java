/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



// TODO: Auto-generated Javadoc
/**
 * The Class PatientGeneralImageAction.
 *
 * @author krishna.t
 */
public class PatientGeneralImageAction extends DispatchAction {

  /** The patientimagedao. */
  private static final PatientGeneralImageDAO patientimagedao = new PatientGeneralImageDAO();

  /** The genericimagedao. */
  private static final GenericDAO genericimagedao = new GenericDAO("doc_hosp_images");

  /**
   * Gets the patient images.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the patient images
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws FileUploadException the file upload exception
   */
  public ActionForward getPatientImages(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, FileUploadException {

    String mrNo = request.getParameter("mr_no");
    request.setAttribute("pagedList", patientimagedao.getPatientImages(mrNo));

    return mapping.findForward("patientimages");
  }

  /**
   * Adds the patient image.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward addPatientImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException {

    return mapping.findForward("addpatientimage");
  }

  /**
   * Creates the patient image.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws FileUploadException the file upload exception
   */
  public ActionForward createPatientImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, FileUploadException {

    String error = "";
    Map params = getParameterMap(request);
    if (!getValueFromMap(params, "content_type", 0).split("/")[0].equals("image")) {
      error = "Invalid File Type was selected..";
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", error);
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("imageAddRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("mr_no", getValueFromMap(params, "mr_no", 0));
      return redirect;
    } else if (getValueFromMap(params, "fileSizeError", 0) != null) {
      error = "Unable to upload the file: file size greater than 10 MB";
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", error);
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("imageAddRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("mr_no", getValueFromMap(params, "mr_no", 0));
      return redirect;

    }
    List errorFields = new ArrayList();
    BasicDynaBean bean = patientimagedao.getBean();
    ConversionUtils.copyToDynaBean(params, bean, errorFields);

    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (errorFields.isEmpty()) {
        boolean exists =
            patientimagedao.exist("image_name", bean.get("image_name"), "mr_no", bean.get("mr_no"));
        if (!exists) {
          int imageId = patientimagedao.getNextSequence();
          bean.set("image_id", imageId);
          boolean success = patientimagedao.insert(con, bean);
          if (success) {
            con.commit();
            FlashScope flash = FlashScope.getScope(request);
            flash.put("success", "Image inserted successfully..");
            ActionRedirect redirect = new ActionRedirect(mapping.findForward("imageListRedirect"));
            redirect.addParameter("mr_no", getValueFromMap(params, "mr_no", 0));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          } else {
            con.rollback();
            error = "Failed to add  Image..";
          }
        } else {
          error = "Image name already exists";
        }
      } else {
        error = "Incorrectly formatted values supplied";
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", error);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("imageAddRedirect"));
    redirect.addParameter("mr_no", getValueFromMap(params, "mr_no", 0));
    redirect.addParameter("patient_id", getValueFromMap(params, "patient_id", 0));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * View.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException {

    String imageId = request.getParameter("image_id");
    BasicDynaBean bean = patientimagedao.getBean();
    boolean loaded = patientimagedao.loadByteaRecords(bean, "image_id", Integer.parseInt(imageId));

    response.setContentType(bean.get("content_type").toString());
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("image_content"));
    OutputStream stream = response.getOutputStream();
    stream.write(bytes);
    stream.flush();
    stream.close();
    return null;
  }

  /**
   * Delete images.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward deleteImages(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, Exception {
    String[] chkBoxValues = request.getParameterValues("delete_image");
    Connection con = null;
    String msg = null;
    String error = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      if (chkBoxValues != null) {
        boolean success = true;
        for (String mrnoImageId : chkBoxValues) {
          String[] splitValues = mrnoImageId.split(",");
          String imageId = splitValues[0];
          String mrNo = null;
          try {
            mrNo = splitValues[1];
          } catch (ArrayIndexOutOfBoundsException aie) {
            // do not throw this exception. b'cause for generic images mrNo will not be there.
          }
          if (mrNo != null && !mrNo.equals("")) {
            if (patientimagedao.delete(con, "image_id", Integer.parseInt(imageId))) {
              success = true;
            } else {
              success = false;
              break;
            }
          } else {
            if (genericimagedao.delete(con, "image_id", Integer.parseInt(imageId))) {
              success = true;
            } else {
              success = false;
              break;
            }

          }
        }
        if (success) {
          con.commit();
          msg = ((chkBoxValues.length > 1) ? "Images" : "Image") + " deleted successfully..";
        } else {
          con.rollback();
          error = "Failed to delete " + ((chkBoxValues.length > 1) ? "Images" : "Image..");
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", error);
    flash.put("success", msg);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("imageListRedirect"));
    redirect.addParameter("mr_no", request.getParameter("mr_no"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the image list JS.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the image list JS
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  /**
   * returns a proper js script file (not JSON). This is what TinyMCE editor requires for a list of
   * URLs that have images.
   */
  public ActionForward getImageListJS(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException, Exception {

    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
    String diaImgPrf = (String) diagGenericPref.get("diag_images");
    List<String> diagImages = new ArrayList<String>(Arrays.asList(diaImgPrf.split("\\s*,\\s*")));
    String mrNo = request.getParameter("mr_no");
    if (mrNo == null || mrNo.equals("")) {
      String patientId = request.getParameter("patient_id");
      if (patientId == null || patientId.equals("")) {
        return null;
      } else {
        GenericDAO dao = new GenericDAO("patient_registration");
        BasicDynaBean bean = dao.findByKey("patient_id", patientId);
        if (bean == null) {
          return null;
        } else {
          mrNo = bean.get("mr_no").toString();
        }
      }
    }


    List<Map> imagesList = patientimagedao.getPatientAndGenericImages(mrNo, diagImages);

    /*
     * construct the javascript as a string. It should look like: var tinyMCEImageList = new Array(
     * // Name, URL ["Logo 1", "/path/logo.jpg"], ["Logo 2 Over", "/path/logo_2_over.jpg"] );
     */
    StringBuffer buf = new StringBuffer();
    buf.append("var tinyMCEImageList = new Array(\n");

    boolean first = true;
    for (Map image : imagesList) {
      String title = (String) image.get("image_name");
      String viewUrl = (String) image.get("viewUrl");

      if (!first) {
        buf.append(",\n");
      }
      buf.append("  ['").append(title).append("', ").append("'").append(request.getContextPath())
          .append(viewUrl).append("']\n");
      first = false;
    }

    InputStream patientPhoto = PatientDetailsDAO.getPatientPhoto(mrNo);
    if (patientPhoto != null && patientPhoto.available() > 0) {
      StringBuilder path = new StringBuilder(request.getContextPath());;
      path.append("/Registration/GeneralRegistration.do?method=viewPatientPhoto");
      path.append("&mrno=" + mrNo);

      if (!first) {
        buf.append(",\n");
      }
      buf.append(" ['").append("patientPhoto").append("', ").append("'").append(path)
          .append("']\n");
    }

    buf.append(");\n");

    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setContentType("text/javascript");
    response.getWriter().write(buf.toString());
    return null;
  }


  /**
   * Gets the parameter map.
   *
   * @param request the request
   * @return the parameter map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws FileUploadException the file upload exception
   */
  public Map<String, Object[]> getParameterMap(HttpServletRequest request) throws IOException,
      FileUploadException {
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
          params.put(name, new Object[] {value});

        } else {
          String fieldName = item.getFieldName();
          String fileName = item.getName();
          boolean isInMemory = item.isInMemory();
          long sizeInBytes = item.getSize();

          if (!fileName.equals("")) {
            if (sizeInBytes > 10 * 1024 * 1024) {
              params.put("fileSizeError", new Object[] {"Unable to upload the file: "
                  + "file size greater than 10 MB"});
            }
            params.put(fieldName, new InputStream[] {item.getInputStream()});
            params.put("content_type",
                new Object[] {MimeTypeDetector.getMimeTypes(item.getInputStream())});
          }
        }
      }
    } else {
      params.putAll(request.getParameterMap());
    }

    return params;
  }

  /**
   * Gets the value from map.
   *
   * @param params the params
   * @param key the key
   * @param index the index
   * @return the value from map
   */
  public String getValueFromMap(Map params, String key, int index) {
    Object[] object = (Object[]) params.get(key);
    if (object != null && object[index] != null && !object[index].equals("")) {
      return object[index].toString();
    }
    return null;
  }

}
