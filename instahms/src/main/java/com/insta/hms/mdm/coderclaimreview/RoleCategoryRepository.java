package com.insta.hms.mdm.coderclaimreview;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class RoleCategoryRepository extends MasterRepository<Integer> {

  public RoleCategoryRepository() {
    super("review_category_role", "id");
  }

  private static final String LIST_ALL_QUERY = "SELECT  c.id, r.role_id, r.role_name "
      + "FROM review_category_role c  left join u_role r ON (c.role_id = r.role_id)"
      + " where category_id = ?";

  public List getRoles(Integer categoryId) throws SQLException {
    return DataBaseUtil.queryToDynaList(LIST_ALL_QUERY, categoryId);
  }
}