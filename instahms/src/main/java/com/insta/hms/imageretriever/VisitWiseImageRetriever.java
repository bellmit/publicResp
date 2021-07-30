package com.insta.hms.imageretriever;

import com.insta.hms.Registration.PatientDetailsDAO;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class VisitWiseImageRetriever.
 */
public class VisitWiseImageRetriever extends PatientImageRetriever implements ImageRetriever {

  /**
   * Retrieve.
   *
   * @param imgUrl
   *          the img Url
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public InputStream retrieve(String imgUrl) throws SQLException, IOException {
    if (imgUrl.contains("GeneralRegistrationPlanCard.do?_method=viewInsuranceCardImage")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String patientId = imgUrl.split("&")[1].split("=")[1];
      InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "I");
      return is;
    }
    if (imgUrl.contains("GeneralRegistrationCorporateCard.do?_method=viewCorporateCardImage")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String patientId = imgUrl.split("&")[1].split("=")[1];
      InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "C");
      return is;
    }
    if (imgUrl.contains("GeneralRegistrationNationalCard.do?_method=viewNationalCardImage")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String patientId = imgUrl.split("&")[1].split("=")[1];
      InputStream is = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "N");
      return is;
    } else {
      return super.retrieve(imgUrl);
    }
  }
}
