/**
 *
 */
package com.insta.hms.master.DRGCodesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi
 *
 */
public class DRGCodesMasterDAO extends GenericDAO {

	public DRGCodesMasterDAO() {
		super("drg_codes_master");
	}

	public static final String DRG_CODE_DETAILS = "SELECT drg_code::text as drg_code_dup_id, drg_code::text, " +
			"drg_description, patient_type, " +
			"relative_weight, status, code_type, hcpcs_portion_per FROM drg_codes_master " ;

	public static List<BasicDynaBean> getDRGCodeDetails() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(DRG_CODE_DETAILS);
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
		if (ps != null) ps.close();
		if (con != null) con.close();
		return l;
	}
	

	private static final String GET_DRG_ITEM_SUBGROUP_DETAILS = "select disg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from drg_code_item_sub_groups disg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = disg.item_subgroup_id) "+
			" left join drg_codes_master dcm on (dcm.drg_code = disg.drg_code) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where disg.drg_code = ? "; 

		public static List<BasicDynaBean> getDrgItemSubGroupDetails(String drgCode) throws SQLException {
			List list = null;
			Connection con = null;
		    PreparedStatement ps = null;
			 try{
				 con=DataBaseUtil.getReadOnlyConnection();
				 ps=con.prepareStatement(GET_DRG_ITEM_SUBGROUP_DETAILS);
				 ps.setString(1, drgCode);
				 list = DataBaseUtil.queryToDynaList(ps);
			 }finally {
				 DataBaseUtil.closeConnections(con, ps);
			 }
			return list;
		}
		
		private static final String GET_DRG_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
				" FROM drg_code_item_sub_groups drgisg "+
				" JOIN item_sub_groups isg ON(drgisg.item_subgroup_id = isg.item_subgroup_id) "+
				" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "+
				" WHERE drgisg.drg_code = ? ";
		
		public List<BasicDynaBean> getDrgItemSubGroupTaxDetails(String itemId) throws SQLException{
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_DRG_SUB_GROUP_TAX_DETAILS);
				ps.setString(1,itemId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

}
