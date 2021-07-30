/**
 *
 */
package com.insta.hms.master.CenterOuthouses;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class CenterOuthousesDAO extends GenericDAO {

	public CenterOuthousesDAO() {
		super("center_outsources");
	}

	private static final String FIELDS = "SELECT * ";
	private static final String TABLES = " FROM (select co.center_id, co.outsource_id, hcm.center_name, " +
			"	case when outsource_dest_type IN('O', 'IO') then om.oh_name  "+
			" 	when  outsource_dest_type='C' then il.center_name end as outsource_name,outsource_dest "+
			"	from center_outsources co "+
			" 	left join diag_outsource_master dom on (co.outsource_id = dom.outsource_dest) "+
			" 	left join outhouse_master om on (om.oh_id = dom.outsource_dest) "+
			" 	left join hospital_center_master il on (il.center_id::text = dom.outsource_dest) "+
			" 	left join hospital_center_master hcm on (hcm.center_id = co.center_id) " +
			") as foo ";
	private static final String COUNT = "SELECT count(*) ";

	public PagedList search(Map filters) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, FIELDS, COUNT, TABLES, ConversionUtils.getListingParameter(filters));
			qb.addFilterFromParamMap(filters);
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String GET_INTERNAL_LAB_DETAILS = "SELECT center_id,center_name " +
			"	from diag_outsource_master dom " +
			"	join hospital_center_master hcm on (hcm.center_id::text=dom.outsource_dest)" +
			"	WHERE outsource_dest_type='C' ";
	public List<BasicDynaBean> getInternalLabDetails()throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INTERNAL_LAB_DETAILS);
			return DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_INTERNAL_LAB_COUNT = "SELECT COUNT(*) FROM diag_outsource_master WHERE outsource_dest_type='C' ";
	public boolean isIntLabExists()throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INTERNAL_LAB_COUNT);
			int count = DataBaseUtil.getIntValueFromDb(ps);
			if(count > 0) return true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}
	
	private static final String IS_TEST_ASSOCIATES_WITH_CENTER = "SELECT source_center_id "
			+ " FROM diag_outsource_detail dod "
			+ " JOIN diag_outsource_master dom ON(dom.outsource_dest_id = dod.outsource_dest_id)"
			+ " WHERE dod.source_center_id = ? AND dom.outsource_dest = ? LIMIT 1";
	
	public boolean isTestAssociatesWithCenterAndOutsource(Integer center_id, String outsourceDestID)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_TEST_ASSOCIATES_WITH_CENTER);
			ps.setInt(1, center_id);
			ps.setString(2, outsourceDestID);			
			return DataBaseUtil.queryToDynaBean(ps) != null;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
