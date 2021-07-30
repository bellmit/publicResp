package com.insta.hms.slida;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.Encoder;
import com.insta.hms.integration.connectors.FileConnector;
import com.insta.hms.integration.connectors.FtpFileConnector;
import com.insta.hms.integration.connectors.SimpleFileConnector;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  Action handling communication with Sidexis Dental Xray Software.
 *  Requires mod_slida to be enabled.
 */
public class SlidaAction extends DispatchAction {

  static final Logger log = LoggerFactory.getLogger(SlidaAction.class);

  /**
   * Send Registration data to Sidexis Dental Xray Software.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward register(ActionMapping mapping, ActionForm form, 
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Integer centerId = RequestContext.getCenterId();

    Map<String, String> resultMap = new HashMap<String, String>();
    if (null == centerId || centerId == 0) {
      resultMap.put("code", "4");
      resultMap.put("message", "SIDEXIS configuration is center specific. "
          + "Patient Details will be exported only when logged into a specific center");
      toJSONResponse(resultMap, response);
      return null;
    }

    BasicDynaBean prefBean  = CenterPreferencesDAO.getAllCenterPrefs(centerId);
    String mailSlotPath = (String)prefBean.get("slida_url");
    String protocol = (String)prefBean.get("slida_protocol");
    InetAddress remoteAddr = InetAddress.getByName(request.getRemoteHost());
    String stationName = remoteAddr.getHostName();
    log.debug("Remote Host Name : " + stationName);
    log.debug("Mailslot path :" + mailSlotPath + ", Protocol: " + protocol);
    if (stationName.length() > 9) {
      resultMap.put("code", "5");
      resultMap.put("message", "Host name of your computer '" + stationName 
          + "'exceeds 9 characters. Request could not be processed.");
      toJSONResponse(resultMap, response);
      return null;
    }
    char gender = 'O';
    if (null != mailSlotPath && !mailSlotPath.trim().equals("")) {

      String mrNo = request.getParameter("mr_no");
      if (mrNo == null || mrNo.trim().equals("")) {
        resultMap.put("code", "1");
        resultMap.put("message", "MR No. not specified. Please specify Patient MR No");
        toJSONResponse(resultMap, response);
        return null;
      }

      PatientDetailsDAO patientDao = new PatientDetailsDAO();
      Map<String, Object> patientMap = new HashMap<String, Object>();
      Connection con = null;
      BasicDynaBean patientBean = null;
      String indexNumber = null;

      try {
        con = DataBaseUtil.getConnection();
        String[] columns = new String[] {"mr_no", "last_name", "dateofbirth", "patient_name",
            "patient_gender", "oldmrno", "expected_dob"};
        List<String> columnList = Arrays.asList(columns);
        patientMap.put("mr_no", mrNo);
        patientBean = patientDao.findByKey(con, columnList, patientMap);

        if (null != patientBean) {
          indexNumber = (String)patientBean.get("oldmrno");

          if (null == indexNumber || indexNumber.trim().equals("")) {
            // Patient not registered in sidexis, 
            // generate card index number and update it as old mr no.
            int cardIndex = DataBaseUtil.getNextSequence("slida_card_index_sequence");
            indexNumber = String.valueOf(cardIndex);
            patientMap.put("oldmrno", indexNumber);
            patientDao.updateWithName(con, patientMap, "mr_no");
          }
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }

      if (null == patientBean) {
        resultMap.put("code", "2");
        resultMap.put("message", "Incorrect MR No, patient is not found in the database");
        toJSONResponse(resultMap, response);
        return null;
      }

      String name = (String)patientBean.get("last_name");
      Date dob = (Date)patientBean.get("dateofbirth");
      if (null == dob) {
        dob = (Date)patientBean.get("expected_dob");
      }
      String firstName = (String)patientBean.get("patient_name");
      String patientGender = (String)patientBean.get("patient_gender");
      if (null != patientGender && !patientGender.trim().equals("")) {
        gender = patientGender.charAt(0);
      }

      // set name and firstname to a single blank, if not present. 
      // Without this activation message does not get processed.

      if (null == name || name.trim().equals("")) {
        name = "-";
      }

      if (null == firstName || firstName.trim().equals("")) {
        firstName = "-";
      }

      String doctor = "";
      NMessage nmsg = new NMessage(indexNumber, name, firstName, dob, gender, doctor);
      byte[] nmsgData = nmsg.format();

      if (!writeToMailSlot(protocol, mailSlotPath, nmsgData)) {
        resultMap.put("code", "3");
        resultMap.put("message", "Could not write to mailsot, "
            + "please check connection and file permission");
        toJSONResponse(resultMap, response);
        return null;
      }

      AMessage amsg = new AMessage(indexNumber, name, firstName, dob, stationName);
      byte[] amsgData = amsg.format();

      if (!writeToMailSlot(protocol, mailSlotPath, amsgData)) {
        resultMap.put("code", "3");
        resultMap.put("message", "Could not write to mailsot, "
            + "please check connection and file permission");
        toJSONResponse(resultMap, response);
        return null;
      }
      resultMap.put("code", "0");
      resultMap.put("message", "Patient details have been posted to SIDEXIS successfully."
          + "\n\nPlease click on the SIDEXIS window to continue.");
      toJSONResponse(resultMap, response);
      return null; // we are done.
    }
    resultMap.put("code", "4");
    resultMap.put("message", "SIDEXIS mailslot has not been configured in preferences.");
    toJSONResponse(resultMap, response);
    return null;
  }

  /**
   * Write file to local filesystem or FTP
   * @param  protocol     protocol (Either FTP or FILE)
   * @param  mailSlotPath absolute path or complete ftp url ending with siomin.sdx 
   * @param  data         data that needs to be written to file
   * @return              true if operation succeeded, false otherwise
   */
  private boolean writeToMailSlot(String protocol, String mailSlotPath, byte[] data) {
    OutputStream os = null;
    FileConnector connector = null;
    try {
      connector = getFileConnector(protocol, mailSlotPath);
      boolean opened = connector.open(mailSlotPath);
      if (opened) {
        log.debug("Preparing the output stream ...");
        os = connector.getOutputStream();
        if (null != os) {
          log.debug("Writing out ...");
          os.write(data);
          os.close();
        }
        log.debug("Writing Done...");
        connector.close();
        return true;
      }
      log.debug("Output stream closed...");
    } catch (FileNotFoundException ex) {
      log.error("Mail slot file does not exist:" + mailSlotPath + " :" + ex.getMessage());
    } catch (IOException ex) {
      log.error("Could not read mail slot: " + mailSlotPath + " " + ex.getMessage());
    }
    return false;
  }

  /**
   * Gets File Connector based on protocol.
   * @param  protocol protocol (Either FTP or FILE)
   * @param  url      absolute path or complete ftp url ending with siomin.sdx 
   * @return          FileConnector for FTP or local FILE
   */
  private FileConnector getFileConnector(String protocol, String url) {
    log.debug("File Protocol : " + protocol);
    if (null != protocol && !protocol.trim().isEmpty()
        && "FTP".equalsIgnoreCase(protocol.trim())) {
      return new FtpFileConnector();
    } else {
      return new SimpleFileConnector();
    }
  }

  /**
   * Convert object to JSON and send it as response
   * @param  obj         Object
   * @param  response    HTTPServletResponse object on which json object is to be sent.
   * @throws  Exception if not able to access writer in response object
   */
  private void toJSONResponse(Object obj, HttpServletResponse response) throws IOException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(obj));
  }
}
