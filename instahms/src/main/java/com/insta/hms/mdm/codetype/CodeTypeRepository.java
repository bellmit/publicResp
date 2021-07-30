package com.insta.hms.mdm.codetype;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository("codeTypeRepository")
public class CodeTypeRepository extends MasterRepository<String> {
  public CodeTypeRepository() {
    super("mrd_supported_code_types", "code_type");
  }

  private static final String GET_CODE_TYPE_LIST = "SELECT DISTINCT code_type"
      + " FROM mrd_supported_code_types";

  public List<BasicDynaBean> getCodeTypeList() {
    return DatabaseHelper.queryToDynaList(GET_CODE_TYPE_LIST);
  }

  private static final String GET_CODE_DETAILS_BY_CODE_TYPE = "SELECT code_type, code, code_desc,"
      + " status FROM getItemCodesForCodeType(?)"
      + " WHERE status='A' AND code IS NOT NULL AND code_desc IS NOT NULL";

  private static final String GET_CODE_TYPE_BY_CODE_CATEGORY_LIST = "SELECT DISTINCT code_type"
      + " FROM mrd_supported_codes WHERE code_category = ?";

  /**
   * Get code details by code type.
   *
   * @param searchInput search text
   * @param codeType    code type
   * @return response
   */
  public List<BasicDynaBean> getCodeDetailsByCodeType(String searchInput, String codeType)
      throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {
      int index;
      String[] searchWord = searchInput.split(" ");
      StringBuilder query = new StringBuilder(GET_CODE_DETAILS_BY_CODE_TYPE);

      for (index = 0; index < searchWord.length; index++) {
        query.append(" AND (code ILIKE ? OR code_desc ILIKE ?)");
      }
      query.append(" LIMIT 100");
      ps = con.prepareStatement(query.toString());
      int psIndex = 1;
      ps.setString(psIndex++, codeType);

      for (index = 0; index < searchWord.length; index++) {
        ps.setString(psIndex++, searchWord[index] + "%");
        ps.setString(psIndex++, "%" + searchWord[index] + "%");
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Get Get code types by code category.
   *
   * @param codeCategory category of code
   * @return code type list
   */
  public List<BasicDynaBean> getCodeTypesByCodeCategory(String codeCategory) throws SQLException {
    StringBuilder query = new StringBuilder(GET_CODE_TYPE_BY_CODE_CATEGORY_LIST);
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    try {

      ps = con.prepareStatement(query.toString());
      int psIndex = 1;
      ps.setString(psIndex++, codeCategory);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
