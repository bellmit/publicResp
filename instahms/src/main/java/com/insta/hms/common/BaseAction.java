package com.insta.hms.common;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.Preferences;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BaseAction.
 *
 * @author krishna.t
 */
public class BaseAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(BaseAction.class);

  /**
   * use this method when u dint call getParameterMap on request. If u already have request map use
   * other form of this method(passing request map).
   *
   * @param request the request
   * @param key     the key
   * @return the parameter
   * @throws FileUploadException the file upload exception
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws SQLException        the SQL exception
   */
  public String getParameter(HttpServletRequest request, String key)
      throws FileUploadException, IOException, SQLException {
    return getParameter(getParameterMap(request), key);
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key    the key
   * @return the parameter
   */
  public String getParameter(Map params, String key) {
    Object[] obj = (Object[]) params.get(key);
    if (obj == null || obj[0] == null) {
      return null;
    }
    return obj[0].toString();
  }

  /**
   * Gets the parameter values. use this method when u dint call getParameterMap on request. If u
   * already have request map use other form of this method(passing request map).
   *
   * @param request the request
   * @param key     the key
   * @return the parameter values
   * @throws FileUploadException the file upload exception
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws SQLException        the SQL exception
   */
  public String[] getParameterValues(HttpServletRequest request, String key)
      throws FileUploadException, IOException, SQLException {
    return getParameterValues(getParameterMap(request), key);
  }

  /**
   * Gets the parameter values.
   *
   * @param params the params
   * @param key    the key
   * @return the parameter values
   */
  public String[] getParameterValues(Map params, String key) {
    return (String[]) params.get(key);
  }

  /**
   * Gets the values.
   *
   * @param request the request
   * @param key     the key
   * @return the values
   * @throws FileUploadException the file upload exception
   * @throws IOException         Signals that an I/O exception has occurred.
   */
  /*
   * use this method when u dint call getParameterMap on request. If u already have request map use
   * other form of this method(passing request map).
   */
  public Object[] getValues(HttpServletRequest request, String key)
      throws FileUploadException, IOException {
    return getValues(request.getParameterMap(), key);
  }

  /**
   * Gets the values.
   *
   * @param params the params
   * @param key    the key
   * @return the values
   */
  public Object[] getValues(Map params, String key) {
    return (Object[]) params.get(key);
  }

  /**
   * Gets the parameter map.
   *
   * @param request the request
   * @return the parameter map
   * @throws FileUploadException the file upload exception
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws SQLException        the SQL exception
   */
  @SuppressWarnings("unchecked")
  public Map getParameterMap(HttpServletRequest request)
      throws FileUploadException, IOException, SQLException {
    Map<String, Object[]> params = new HashMap();
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);

    if (isMultipart) {
      FileItemFactory factory = new DiskFileItemFactory();
      ServletFileUpload upload = new ServletFileUpload(factory);
      int fileSizeLimit = GenericPreferencesDAO.getGenericPreferences().getUploadLimitInMB();
      upload.setFileSizeMax(fileSizeLimit * 1024 * 1024);

      List<FileItem> items = null;
      try {
        items = upload.parseRequest(request);
      } catch (FileSizeLimitExceededException fe) {
        copyStringToMap(params, "fileSizeError",
            "Unable to upload the file: " + "file size greater than 10 MB");
        log.error("", fe);
      }
      if (items == null) {
        return params;
      }

      Iterator iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = (FileItem) iter.next();

        if (item.isFormField()) {
          String name = item.getFieldName();
          String value = item.getString("UTF-8");
          copyStringToMap(params, name, value);

        } else {
          String fieldName = item.getFieldName();
          String fileName = item.getName();

          if (!fileName.equals("")) {

            copyObjectToMap(params, fieldName, item.getInputStream());
            copyStringToMap(params, "fileName", fileName);
            // params.put("fileName",new String[] {fileName});
            if (fileName != null && !fileName.equals("")) {
              if (fileName.contains(".")) {
                String extension = fileName.substring(fileName.indexOf(".") + 1);
                copyStringToMap(params, "original_extension", extension);

                if (extension.equals("odt") || extension.equals("ods")) {
                  copyObjectToMap(params, "content_type",
                      "application/vnd.oasis.opendocument.text");
                } else {
                  copyObjectToMap(params, "content_type",
                      MimeTypeDetector.getMimeTypes(item.getInputStream()));
                }
              } else {
                copyObjectToMap(params, "content_type",
                    MimeTypeDetector.getMimeTypes(item.getInputStream()));
              }
            }
          }
        }
      }

    } else {
      params.putAll(request.getParameterMap());
      log.debug("In getParameterMap: normal (not multipart) request, ignoring all file inputs");
    }
    return params;
  }

  /**
   * Gets the param as list.
   *
   * @param request the request
   * @param param   the param
   * @return the param as list
   */
  public List<String> getParamAsList(Map request, String param) {
    return ConversionUtils.getParamAsList(request, param);
  }

  /**
   * Gets the listing parameter.
   *
   * @param request the request
   * @return the listing parameter
   */
  public Map<LISTING, Object> getListingParameter(Map request) {
    return ConversionUtils.getListingParameter(request);
  }

  /**
   * Flatten.
   *
   * @param in the in
   * @return the map
   */
  public Map flatten(Map in) {
    return ConversionUtils.flatten(in);
  }

  /**
   * Copy to dyna bean.
   *
   * @param from        the from
   * @param bean        the bean
   * @param errorFields the error fields
   */
  public void copyToDynaBean(Map from, DynaBean bean, List errorFields) {
    ConversionUtils.copyToDynaBean(from, bean, errorFields);
  }

  /**
   * Copy to dyna bean.
   *
   * @param req  the req
   * @param res  the res
   * @param bean the bean
   * @return the action forward
   */
  public ActionForward copyToDynaBean(HttpServletRequest req, HttpServletResponse res,
      DynaBean bean) {
    ArrayList errors = new ArrayList();
    copyToDynaBean(req.getParameterMap(), bean, errors);

    if (errors.size() > 0) {
      FlashScope flash = FlashScope.getScope(req);
      ActionRedirect redirect = new ActionRedirect(
          req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      flash.error("Parameter conversion error");
      return redirect;
    }
    return null;
  }

  /**
   * Copy index to dyna bean.
   *
   * @param from        the from
   * @param index       the index
   * @param bean        the bean
   * @param errorFields the error fields
   */
  public void copyIndexToDynaBean(Map from, int index, DynaBean bean, List errorFields) {
    ConversionUtils.copyIndexToDynaBean(from, index, bean, errorFields);
  }

  /**
   * Copy string fields.
   *
   * @param from        the from
   * @param to          the to
   * @param fieldNames  the field names
   * @param errorFields the error fields
   */
  public void copyStringFields(Map from, Map to, String[] fieldNames, List errorFields) {
    ConversionUtils.copyStringFields(from, to, fieldNames, errorFields);
  }

  /**
   * Copy numeric fields.
   *
   * @param from        the from
   * @param to          the to
   * @param fieldNames  the field names
   * @param errorFields the error fields
   */
  public void copyNumericFields(Map from, Map to, String[] fieldNames, List errorFields) {
    ConversionUtils.copyNumericFields(from, to, fieldNames, errorFields);
  }

  /**
   * Parses the timestamp.
   *
   * @param dateStr the date str
   * @param timeStr the time str
   * @return the java.sql. timestamp
   * @throws ParseException the parse exception
   */
  public java.sql.Timestamp parseTimestamp(String dateStr, String timeStr) throws ParseException {
    return DateUtil.parseTimestamp(dateStr, timeStr);
  }

  /**
   * Copy string to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public void copyStringToMap(Map params, String key, String value) {

    if (params.containsKey(key)) {
      String[] obj = (String[]) params.get(key);
      String[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new String[] { value });
    }
  }

  /**
   * Copy object to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  public void copyObjectToMap(Map params, String key, Object value) {

    if (params.containsKey(key)) {
      Object[] obj = (Object[]) params.get(key);
      Object[] newArray = Arrays.copyOf(obj, obj.length + 1);
      newArray[obj.length] = value;
      params.put(key, newArray);

    } else {
      params.put(key, new Object[] { value });
    }
  }

  /**
   * Fill bill no search. Based on number of digits in Bill No prefix this method returns %000xx
   * when passed xx
   *
   * @param req    the req
   * @param billNo the bill no
   * @return the string
   */
  public String fillBillNoSearch(HttpServletRequest req, String billNo) {
    if ((billNo == null) || (billNo.equals(""))) {
      return null;
    }

    StringBuilder sb = new StringBuilder("%");
    Preferences prefs = (Preferences) req.getSession().getAttribute("preferences");
    int billNoDigits = prefs.getBillNoDigits();
    for (int i = 0; i < (billNoDigits - billNo.length()); i++) {
      sb.append("0");
    }
    sb.append(billNo);
    return sb.toString();
  }

  /**
   * Gets the param default.
   *
   * @param req          the req
   * @param paramName    the param name
   * @param defaultValue the default value
   * @return the param default
   */
  public String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
    String value = req.getParameter(paramName);
    if ((value == null) || value.trim().equals("")) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Error response.
   *
   * @param req   the req
   * @param res   the res
   * @param error the error
   * @return the action forward
   */
  public ActionForward errorResponse(HttpServletRequest req, HttpServletResponse res,
      String error) {
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.error(error);
    return redirect;
  }

  /**
   * Adds the parameter.
   *
   * @param key     the key
   * @param value   the value
   * @param forward the forward
   */
  public void addParameter(String key, String value, ActionForward forward) {
    StringBuffer sb = new StringBuffer(forward.getPath());
    if (key == null || key.length() < 1) {
      return;
    }
    if (forward.getPath().indexOf('?') == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }
    sb.append(key + "=" + value);
    forward.setPath(sb.toString());
  }

}
