package com.insta.hms.imageretriever;

import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class UserImageRetriever.
 *
 * @author krishna
 */
public class UserImageRetriever extends PatientImageRetriever {

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
    if (imgUrl.contains("Images.do?_method=viewImage")) {
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
