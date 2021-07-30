package com.insta.hms.mdm.role;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class RoleMasterRepository.
 */
@Repository
public class RoleMasterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new role master repository.
   */
  public RoleMasterRepository() {
    super("u_role", "role_id", "role_name");
    setStatusField("role_status");
  }

  /** The Constant ROLES. */
  private static final String ROLES = "SELECT role_id, role_name FROM u_role WHERE role_status='A'";

  /** The Constant ORDER_BY_ROLE. */
  private static final String ORDER_BY_ROLE = "order by role_name asc";

  /** The Constant EXCLUDE_INSTA_ADMIN. */
  private static final String EXCLUDE_INSTA_ADMIN = " AND role_id != 1 ";


  /**
   * Roles.
   *
   * @param excludeInstaAdmin the exclude insta admin
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> roles(boolean excludeInstaAdmin) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps =
          con.prepareStatement(ROLES
              + (excludeInstaAdmin ? EXCLUDE_INSTA_ADMIN + ORDER_BY_ROLE : ORDER_BY_ROLE));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
