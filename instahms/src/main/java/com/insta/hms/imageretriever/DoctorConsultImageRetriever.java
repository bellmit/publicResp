package com.insta.hms.imageretriever;

import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.SectionFields.SectionFieldsDAO;
import com.insta.hms.outpatient.CrownStatusesDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.DoctorConsultImagesDAO;
import com.insta.hms.outpatient.RootStatusesDAO;
import com.insta.hms.outpatient.SurfaceMaterialDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.insta.hms.outpatient.ToothImageDetails.Tooth;
import com.insta.hms.outpatient.ToothImageDetails.ToothPart;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Map;

import javax.imageio.ImageIO;

// TODO: Auto-generated Javadoc
/**
 * The Class DoctorConsultImageRetriever.
 *
 * @author krishna.t
 */
public class DoctorConsultImageRetriever extends PatientImageRetriever implements ImageRetriever {

  /** The fields dao. */
  SectionFieldsDAO fieldsDao = new SectionFieldsDAO();

  /** The crown statuses DAO. */
  CrownStatusesDAO crownStatusesDAO = new CrownStatusesDAO();

  /** The root statuses DAO. */
  RootStatusesDAO rootStatusesDAO = new RootStatusesDAO();

  /** The sur matrl DAO. */
  SurfaceMaterialDAO surMatrlDAO = new SurfaceMaterialDAO();

  /** The img DAO. */
  GenericDAO imgDAO = new GenericDAO("patient_section_images");

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DoctorConsultImageRetriever.class);

  /**
   * retrieve.
   *
   * @param imgUrl
   *          the image Url
   * @return the Input Stream
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public InputStream retrieve(String imgUrl) throws IOException, SQLException {
    // TODO Auto-generated method stub
    if (imgUrl.contains("OPPrescribeAction.do?method=viewImage")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String imageId = imgUrl.split("&")[1].split("=")[1];
      BasicDynaBean bean = new DoctorConsultImagesDAO().getImage(Integer.parseInt(imageId));
      return (InputStream) bean.get("image");
    } else if (imgUrl.contains("PhysicianFieldsImage.do?_method=viewImage")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String[] params = imgUrl.split("&");
      boolean masterImage = true;
      String id = null;
      for (int i = 1; i < params.length; i++) {
        String[] param = params[i].split("=");
        String key = param[0];
        if (key.equals("image_id")) {
          if (id != null && !id.equals("") && Integer.parseInt(param[1]) != 0) {
            id = param[1];
            masterImage = false;
          }
          continue;
        }
        id = param[1];
      }

      Map imgDetails = null;
      if (masterImage) {
        imgDetails = fieldsDao.getImageDetails(Integer.parseInt(id));
        return (InputStream) imgDetails.get("file_content");
      } else {
        BasicDynaBean bean = imgDAO.getBean();
        imgDAO.loadByteaRecords(bean, "image_id", Integer.parseInt(id));
        return (InputStream) bean.get("file_content");
      }

    } else if (imgUrl.contains("PhysicianFieldsImageMarkers.do?_method=view")) {
      ImageMarkerDAO dao = new ImageMarkerDAO();
      imgUrl = imgUrl.replace("&amp;", "&");
      String imageId = imgUrl.split("&")[1].split("=")[1];
      BasicDynaBean bean = dao.getBean();
      dao.loadByteaRecords(bean, "image_id", Integer.parseInt(imageId));

      return (InputStream) bean.get("file_content");
    } else if (imgUrl.contains("Consultation.do?_method=getDentalChart")) {

      BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
      FileInputStream stream = null;

      String numberingSystem = (String) prefs.get("tooth_numbering_system");
      String dentalChart = numberingSystem.equals("U") ? "DentalChart_Adult_UNV.png"
          : "DentalChart_Adult_FDI.png";
      stream = new FileInputStream(AppInit.getRootRealPath() + "/images/Dental/" + dentalChart);

      return stream;
    } else if (imgUrl.contains("Consultation.do?getDentalChartMarkerImage")) {
      String imageName = "";

      imgUrl = imgUrl.replace("&amp;", "&");
      String[] params = imgUrl.split("&");

      String mrNo = null;
      String toothNumber = "";
      String statusId = "";
      String materialId = "";
      String toothPart = "";
      for (int i = 1; i < params.length; i++) {
        String[] param = params[i].split("=");

        if (param[0].equals("mr_no")) {
          mrNo = param[1].trim();
        } else if (param[0].equals("dc_unv_number")) {
          toothNumber = param[1].trim();
        } else if (param[0].equals("dc_status_id")) {
          statusId = param[1].trim();
        } else if (param[0].equals("dc_material_id")) {
          materialId = param[1].trim();
        } else if (param[0].equals("dc_tooth_part")) {
          toothPart = param[1].trim();
        }
      }

      ToothImageDetails desc = null;
      try {
        desc = DentalChartHelperDAO.getToothImageDetails(true);
      } catch (IOException ioexe) {
        log.error("", ioexe);
        return null;
      }
      for (Map.Entry<String, Tooth> entry : desc.getTeeth().entrySet()) {
        if (toothNumber.equals(entry.getKey())) {

          for (Map.Entry<String, ToothPart> tpEntry : entry.getValue().getToothPart().entrySet()) {
            ToothPart part = tpEntry.getValue();
            if (tpEntry.getKey().equals(toothPart)) {
              imageName = part.getImage_name();
              break;
            }
          }
        }
      }

      BasicDynaBean bean = null;
      if (toothPart.equals("crown")) {
        bean = crownStatusesDAO.findByKey("crown_status_id", Integer.parseInt(statusId));

      } else if (toothPart.equals("root")) {
        bean = rootStatusesDAO.findByKey("root_status_id", Integer.parseInt(statusId));
      } else if (!materialId.equals("")) {
        bean = surMatrlDAO.findByKey("material_id", Integer.parseInt(materialId));
      }

      String fileName = "adult/" + toothNumber + "/" + imageName;
      BufferedImage image = ImageIO
          .read(new File(AppInit.getRootRealPath() + "/images/Dental/" + fileName));
      if (bean != null) {
        String colorCode = (String) bean.get("color_code");
        if (toothPart.equals("root")) {
          image = DentalChartHelperDAO.changeColor(image,
              DentalChartHelperDAO.stringToColor("#F7E7BB"),
              DentalChartHelperDAO.stringToColor(colorCode.toUpperCase()));
        } else {
          image = DentalChartHelperDAO.changeColor(image,
              DentalChartHelperDAO.stringToColor("#FFFFFF"),
              DentalChartHelperDAO.stringToColor(colorCode.toUpperCase()));
        }
      }
      FileOutputStream os = new FileOutputStream("/tmp/marker.png");
      ImageIO.write(image, "png", os);
      return new FileInputStream("/tmp/marker.png");

    } else if (imgUrl.contains("Images.do?_method=viewImage")) {
      int reportId = 0;
      String title = null;
      int prescribedId = 0;

      imgUrl = imgUrl.replace("&amp;", "&");
      String[] params = imgUrl.split("[&?]");

      for (String param : params) {
        String[] paramValue = param.split("=");
        if (paramValue.length > 1) {
          if (paramValue[0].equals("reportId")) {
            reportId = paramValue[1] != null ? Integer.parseInt(paramValue[1]) : 0;
          } else if (paramValue[0].equals("titleName")) {
            title = paramValue[1];
            title = URLDecoder.decode(title, "UTF-8");
          } else if (paramValue[0].equals("prescribedId")) {
            prescribedId = paramValue[1] != null ? Integer.parseInt(paramValue[1]) : 0;
          }
        }
      }

      if ((reportId != 0 || prescribedId != 0) && (title != null)) {
        InputStream is = LaboratoryDAO.getImageStream(prescribedId, reportId, title);
        return is;
      }
    } else {

      return super.retrieve(imgUrl);
    }
    return null;
  }
}
