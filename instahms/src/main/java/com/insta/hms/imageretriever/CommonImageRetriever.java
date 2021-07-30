package com.insta.hms.imageretriever;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericImageMaster.GenericImageDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.servlet.ServletException;

// TODO: Auto-generated Javadoc
/**
 * The Class CommonImageRetriever.
 *
 * @author krishna
 */
public class CommonImageRetriever implements ImageRetriever {

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
  public InputStream retrieve(String imgUrl) throws SQLException, IOException {
    if (imgUrl.contains("GenericImageMaster.do?_method=view")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String key = imgUrl.split("&")[1].split("=")[0];
      String value = imgUrl.split("&")[1].split("=")[1];
      BasicDynaBean bean = null;
      if (key.equalsIgnoreCase("name")) {
        bean = new GenericImageDAO().getGeneracImageUsingName(value);
      } else {
        bean = new GenericImageDAO().getGeneracImage(Integer.parseInt(value));
      }
      InputStream is = (InputStream) bean.get("image_content");

      return is;
    } else if (imgUrl.contains("UserImage.do?_method=view")) {
      imgUrl = imgUrl.replace("&amp;", "&");
      String key = imgUrl.split("&")[1].split("=")[0];
      String value = imgUrl.split("&")[1].split("=")[1];
      GenericDAO dao = new GenericDAO("user_images");
      BasicDynaBean bean = dao.getBean();
      String colName = key.equals("user_name") ? "emp_username" : "doctor_id";
      dao.loadByteaRecords(bean, colName, value);
      InputStream is = (InputStream) bean.get("signature");

      return is;
    }
    return null;
  }
}
