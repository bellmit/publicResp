package com.insta.hms.imageretriever;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.PatientGeneralImageDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientImageRetriever.
 *
 * @author krishna
 */
public class PatientImageRetriever extends CommonImageRetriever implements ImageRetriever {
  
  private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");

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
    if (imgUrl.contains("PatientGeneralImageAction.do?_method=view")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String imageId = imgUrl.split("&")[1].split("=")[1];

      BasicDynaBean bean = new PatientGeneralImageDAO()
          .getPatientGeneralImage(Integer.parseInt(imageId));
      InputStream is = (InputStream) bean.get("image_content");

      return is;
    } else if (imgUrl.contains("GeneralRegistrationPatientPhoto.do?_method=viewPatientPhoto")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String mrNo = imgUrl.split("&")[1].split("=")[1];
      PatientDetailsDAO patientDao = new PatientDetailsDAO();
      BasicDynaBean bean = patientDao.getBean();
      patientDao.loadByteaRecords(bean, "mr_no", mrNo);
      InputStream is = (InputStream) bean.get("patient_photo");

      return is;
    } else if (imgUrl.contains("PatientInsurenceCard.do?_method=getPatientInsureneceCard")) {
      InputStream is = null;
      imgUrl = imgUrl.replace("&amp;", "&");
      String patientId = imgUrl.split("&")[1].split("=")[1];
      BasicDynaBean visitBean = patientRegistrationDAO.findByKey("patient_id",
          patientId);
      if (visitBean != null) {
        String mainVisitId = (String) visitBean.get("main_visit_id");
        BasicDynaBean mainVisitBean = patientRegistrationDAO.findByKey("patient_id",
            mainVisitId);
        Integer patientPolicyId = PatientDetailsDAO.getPatientPolicyId(patientId);

        if (patientPolicyId != null && patientPolicyId != 0) {
          is = PatientDetailsDAO.getCurrentPatientCardImage(mainVisitId, "I");
        } else if (mainVisitBean != null && null != mainVisitBean.get("patient_corporate_id")
            && ((Integer) mainVisitBean.get("patient_corporate_id")) != 0) {
          is = PatientDetailsDAO.getCurrentPatientCardImage(mainVisitId, "C");
        } else if (mainVisitBean != null && null != mainVisitBean.get("patient_national_sponsor_id")
            && ((Integer) mainVisitBean.get("patient_national_sponsor_id")) != 0) {
          is = PatientDetailsDAO.getCurrentPatientCardImage(mainVisitId, "N");
        }
      }
      return is;
    } else {
      return super.retrieve(imgUrl);
    }
  }

}
