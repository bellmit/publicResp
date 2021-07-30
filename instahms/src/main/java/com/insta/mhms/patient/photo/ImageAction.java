package com.insta.mhms.patient.photo;

import com.bob.hms.common.LoginAction;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.master.DoctorMaster.DoctorImagesDAO;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 */
public class ImageAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Get Photo.
   *
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException throws Servlet Exception
   */
  public ActionForward getPhoto(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException {
    logger.info("getting image data");
    Map<String, Object> responseMap = new HashMap<String, Object>();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    HttpSession session = request.getSession(false);
    JSONSerializer js = JsonProcessor.getJSONParser();
    if (session != null) {
      String sesHospitalId = (String) session.getAttribute("sesHospitalId");
      String successMsg = "";
      String returnCode = "";
      if (sesHospitalId == null || "".equals(sesHospitalId)) {
        successMsg = "Session is expired, please login again";
        logger.info("Session is expired, please login again");
        responseMap.put("return_code", "1001");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      String imageType = request.getParameter("type");
      String doctorId = request.getParameter("doctor_id");
      if (imageType == null
          || imageType.equals("")
          || (imageType != null
              && imageType.equalsIgnoreCase("D")
              && (doctorId == null || doctorId.equals("")))) {
        successMsg = "Mandatory fields are not supplied";
        responseMap.put("return_code", "1002");
        responseMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      if (imageType != null
          && !imageType.equalsIgnoreCase("D")
          && !imageType.equalsIgnoreCase("P")) {
        successMsg = "Invalid input parameters";
        responseMap.put("return_code", "1023");
        responseMap.put("return_message", successMsg);
        logger.info(successMsg);
        logger.info("sending the response back to the requesting server");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      String mrNo = (String) session.getAttribute("mobile_user_id");
      try {
        InputStream photo = null;
        if (imageType.equalsIgnoreCase("D")) {
          photo = DoctorImagesDAO.getDoctorsPhoto(doctorId);
        } else {
          photo = PatientDetailsDAO.getPatientPhoto(mrNo);
        }
        if (photo != null) {
          OutputStream os = response.getOutputStream();
          response.setContentType("image/gif");
          byte[] bytes = new byte[4096];
          int len = 0;
          while ((len = photo.read(bytes)) > 0) {
            os.write(bytes, 0, len);
          }
          os.flush();
          photo.close();
          response.setStatus(HttpServletResponse.SC_OK);
          return null;
        } else {
          response.setContentType("application/json");
          response.setHeader("Cache-Control", "no-cache");
          responseMap.put("return_message", "No photo found");
          responseMap.put("return_code", "1022");
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
      } catch (Exception exception) {
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache");
        responseMap.put("return_message", "Failed to retrieve photo");
        responseMap.put("return_code", "1021");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();

    return null;
  }
}
