/**
 * 
 */
package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author irshad
 *
 */
public class WorkOrderDAO  extends GenericDAO {
	
	public WorkOrderDAO(String tablename) {
		super(tablename);
	}
	private static final String WO_FIELDS = "SELECT * ";
	private static final String WO_COUNT = "SELECT count(wo_no) ";
	private String WO_TABLES =" FROM  (select wom.wo_no,wom.raised_by,sm.supplier_name,"+
							  " wom.wo_date,wom.status,wom.supplier_id,wom.center_id from " + 
				              " work_order_main wom join  supplier_master sm  on(sm.supplier_code = wom.supplier_id) ) as wolist ";
	
	public PagedList getWOList(Map filter, Map<LISTING, Object> listing) throws ParseException, SQLException{
		Connection con = null;
		SearchQueryBuilder qb = null;
		String FROM = null;
		int centerID = RequestContext.getCenterId();
		try {
		con = DataBaseUtil.getReadOnlyConnection();
		
		qb = new SearchQueryBuilder(con,WO_FIELDS, WO_COUNT, WO_TABLES, listing);	
		
		qb.appendToQuery("(center_id=0 or center_id="+centerID+")");
		qb.addFilterFromParamMap(filter);
		qb.build();
		
		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
		} finally{
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	private  String UPDATE_WO = "update work_order_main set status=?,closed_date=?,closed_by=?  where wo_no in";

	public void updateWo(List<String> list, String str) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		Timestamp timestamp = new java.sql.Timestamp(new Date().getTime());

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder sb = new StringBuilder(UPDATE_WO);
			if (list != null && list.size() > 0) {
				sb.append("(");
				for (String s : list) {
					sb.append("?,");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");

				ps = con.prepareStatement(sb.toString());
				ps.setString(1, str);

				if (str.equals("C")) {
					ps.setTimestamp(2, timestamp);
					ps.setString(3, RequestContext.getUserName());
				} else {
					ps.setTimestamp(2, null);
					ps.setString(3, null);
				}

				int k = 4;
				for (String s : list) {
					ps.setString(k++, s);
				}
				ps.executeUpdate();
			}

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WO_DET =
			" SELECT wom.*, sm.supplier_name " +
			" FROM work_order_main wom JOIN supplier_master sm ON (sm.supplier_code = wom.supplier_id) " +
			" WHERE wo_no = ?";

	public static BasicDynaBean getWODetails (String woNo) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_WO_DET, woNo);
	}
	
	private static final String GET_WO_ITEMS =
			"SELECT wod.*,woim.wo_item_name,'true' as dbValue FROM "
			+ "work_order_details wod JOIN work_order_items_master woim "
			+ "ON wod.wo_item_id = woim.wo_item_id where wod.wo_no = ?";

	public static List<BasicDynaBean> getWOItems (String woNo) throws SQLException{
		return DataBaseUtil.queryToDynaList(GET_WO_ITEMS, woNo);
	}
	
	public static BasicDynaBean getSupplierDetails(String woNo) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("select * from supplier_master where supplier_code= (select supplier_id from work_order_main where wo_no=?)");
			ps.setString(1, woNo);
			List list = DataBaseUtil.queryToDynaList(ps);
	
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
