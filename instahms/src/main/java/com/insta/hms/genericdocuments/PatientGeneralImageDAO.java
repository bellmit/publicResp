/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientGeneralImageDAO.
 *
 * @author krishna.t
 */
public class PatientGeneralImageDAO extends GenericDAO {

  /** The Constant table. */
  private static final String table = "patient_general_images";

  /** The genericimagedao. */
  GenericDAO genericimagedao = new GenericDAO("doc_hosp_images");

  /**
   * Instantiates a new patient general image DAO.
   */
  public PatientGeneralImageDAO() {
    super(table);
  }

  /**
   * returns patient images as well as generic images.
   *
   * @param mrNo is required to get the patient images.
   * @return the patient and generic images
   * @throws SQLException the SQL exception
   */
  public List getPatientAndGenericImages(String mrNo) throws SQLException {

    List<String> pcolumns = new ArrayList<String>();
    pcolumns.add("image_id");
    pcolumns.add("mr_no");
    pcolumns.add("image_name");
    pcolumns.add("content_type");

    List<String> gcolumns = new ArrayList<String>();
    gcolumns.add("image_id");
    gcolumns.add("image_name");
    gcolumns.add("content_type");
    List<BasicDynaBean> genericImagesList = genericimagedao.listAll(gcolumns);

    List<Map> allImages = new ArrayList<Map>();
    List<BasicDynaBean> patientImagesList = listAll(pcolumns, "mr_no", mrNo);
    for (BasicDynaBean bean : patientImagesList) {
      Map map = new HashMap(bean.getMap());
      map.put(
          "viewUrl",
          "/pages/GenericDocuments/PatientGeneralImageAction.do?_method=view&image_id="
              + bean.get("image_id"));
      allImages.add(map);
    }

    for (BasicDynaBean bean : genericImagesList) {
      Map map = new HashMap(bean.getMap());
      map.put("viewUrl",
          "/master/GenericImageMaster.do?_method=view&image_id=" + bean.get("image_id"));
      allImages.add(map);
    }
    return allImages;

  }

  /**
   * Gets the patient and generic images.
   *
   * @param mrNo the mr no
   * @param diagimages the diagimages
   * @return the patient and generic images
   * @throws SQLException the SQL exception
   */
  public List getPatientAndGenericImages(String mrNo, List<String> diagimages) throws SQLException {

    List<String> pcolumns = new ArrayList<String>();
    pcolumns.add("image_id");
    pcolumns.add("mr_no");
    pcolumns.add("image_name");
    pcolumns.add("content_type");
    List<BasicDynaBean> patientImagesList = null;
    if (diagimages.contains("P")) {
      patientImagesList = listAll(pcolumns, "mr_no", mrNo);
    }
    List<String> gcolumns = new ArrayList<String>();
    gcolumns.add("image_id");
    gcolumns.add("image_name");
    gcolumns.add("content_type");
    List<BasicDynaBean> genericImagesList = null;
    if (diagimages.contains("G")) {
      genericImagesList = genericimagedao.listAll(gcolumns);
    }
    List<Map> allImages = new ArrayList<Map>();
    if (null != patientImagesList) {
      for (BasicDynaBean bean : patientImagesList) {
        Map map = new HashMap(bean.getMap());
        map.put(
            "viewUrl",
            "/pages/GenericDocuments/PatientGeneralImageAction.do?_method=view&image_id="
                + bean.get("image_id"));
        allImages.add(map);
      }
    }
    if (null != genericImagesList) {
      for (BasicDynaBean bean : genericImagesList) {
        Map map = new HashMap(bean.getMap());
        map.put("viewUrl",
            "/master/GenericImageMaster.do?_method=view&image_id=" + bean.get("image_id"));
        allImages.add(map);
      }
    }
    return allImages;

  }

  /**
   * Gets the patient images.
   *
   * @param mrNo the mr no
   * @return the patient images
   * @throws SQLException the SQL exception
   */
  public List getPatientImages(String mrNo) throws SQLException {
    List<String> columns = new ArrayList();
    columns.add("image_id");
    columns.add("image_name");
    columns.add("mr_no");
    columns.add("content_type");
    List<BasicDynaBean> imageList = listAll(columns, "mr_no", mrNo);
    List tmpImageList = new ArrayList();
    for (BasicDynaBean bean : imageList) {
      Map map = new HashMap(bean.getMap());
      map.put(
          "viewUrl",
          "/pages/GenericDocuments/PatientGeneralImageAction.do?_method=view&image_id="
              + bean.get("image_id"));
      tmpImageList.add(map);
    }
    return tmpImageList;
  }

  /**
   * Gets the patient general image.
   *
   * @param imageId the image id
   * @return the patient general image
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public BasicDynaBean getPatientGeneralImage(Object imageId) throws SQLException, IOException {
    BasicDynaBean bean = getBean();
    loadByteaRecords(bean, "image_id", imageId);
    return bean;
  }

  /**
   * Exist.
   *
   * @param keycolumn the keycolumn
   * @param identifier the identifier
   * @param keycolumn1 the keycolumn 1
   * @param identifier1 the identifier 1
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean exist(String keycolumn, Object identifier, String keycolumn1, Object identifier1)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append("SELECT " + keycolumn + " FROM ").append(table).append(" WHERE ")
        .append("upper(" + keycolumn + ")").append("=upper(?) and ").append(keycolumn1)
        .append("=?;");


    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query.toString());
      ps.setObject(1, identifier);
      ps.setObject(2, identifier1);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return true;
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return false;
  }

}
