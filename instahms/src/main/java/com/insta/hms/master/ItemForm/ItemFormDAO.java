/**
 *
 */
package com.insta.hms.master.ItemForm;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author krishna
 *
 */
public class ItemFormDAO extends GenericDAO {

	public ItemFormDAO() {
		super("item_form_master");
	}


	public boolean exists(String formName, int formId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT item_form_name FROM item_form_master " +
					" WHERE upper(item_form_name)=upper(?) and item_form_id!=?");
			ps.setString(1, formName);
			ps.setInt(2, formId);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
			else return false;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

}
