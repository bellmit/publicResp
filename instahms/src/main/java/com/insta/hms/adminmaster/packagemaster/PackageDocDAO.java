package com.insta.hms.adminmaster.packagemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

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
 * The Class PackageDocDAO.
 *
 * @author krishna
 */
public class PackageDocDAO extends GenericDAO {

  /**
   * Instantiates a new package doc DAO.
   */
  public PackageDocDAO() {
    super("pack_doc_master");
  }

  /**
   * Uploaded all docs.
   *
   * @param visitId the visit id
   * @return the map
   * @throws SQLException the SQL exception
   */
  public static Map uploadedAllDocs(String visitId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean flag = true;
    List docTypeNames = new ArrayList();
    try {
      ps = con.prepareStatement(
          "SELECT pdm.doc_type_id, foo.doc_type, dt.doc_type_name FROM pack_doc_master pdm  "
              + " JOIN doc_type dt ON (dt.doc_type_id=pdm.doc_type_id) "
              + " JOIN package_prescribed pp ON (pp.patient_id=? AND pdm.pack_id=pp.package_id)"
              + " LEFT JOIN (SELECT pd.doc_type "
              + " FROM patient_general_docs pgd JOIN patient_documents pd "
              + " USING (doc_id) where patient_id=? group by doc_type) foo "
              + " ON (foo.doc_type=pdm.doc_type_id) "
              + " WHERE pp.status!='X' "
              + " GROUP BY pdm.doc_type_id, foo.doc_type, dt.doc_type_name");
      ps.setString(1, visitId);
      ps.setString(2, visitId);
      rs = ps.executeQuery();
      while (rs.next()) {
        String docType = rs.getString("doc_type");
        if (docType == null) {
          flag = false;
          docTypeNames.add(rs.getString("doc_type_name")); // required to be uploaded
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    Map map = new HashMap();
    map.put("success", flag);
    map.put("documents", docTypeNames);
    return map;
  }

}
