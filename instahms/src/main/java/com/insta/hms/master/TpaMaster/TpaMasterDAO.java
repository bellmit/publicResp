
package com.insta.hms.master.TpaMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class TpaMasterDAO extends GenericDAO {

	public  TpaMasterDAO() {
		super("tpa_master");
	}

	public String getNextTPAId() throws SQLException {

		String tpa_id = null;
		tpa_id =	AutoIncrementId.getNewIncrId("tpa_id", "tpa_master", "TPA");

		return tpa_id;
	}

    public static List gettpanames() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        List tpaList = null;
        try{
            con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement("select tpa_name,tpa_id,to_char(validity_end_date,'DD-MM-YYYY')as validity_end_date," +
            		" status,sponsor_type,scanned_doc_required,per_day_rate,sponsor_type_id,member_id_pattern,tpa_member_id_validation_type,child_dup_memb_id_validity_days " +
            		" from tpa_master where  status='A'  ",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            tpaList = DataBaseUtil.queryToDynaList(ps);
        }finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return tpaList;
    }

    public static List getTpaNamesForMrno(String mrno) throws SQLException{
        Connection con = null;
        PreparedStatement ps = null;
        List tpaList = null;
        con = DataBaseUtil.getConnection();
        try {
        	ps = con.prepareStatement("SELECT IC.INSURANCE_ID,IC.MR_NO,IC.TPA_ID, IC.POLICY_NO, IC.POLICY_HOLDER_NAME, " +
        			" IC.patient_relationship, IC.policy_validity_start, IC.policy_validity_end, IC.prior_auth_id, IC.prior_auth_mode_id, TA.TPA_NAME ,TA.SPONSOR_TYPE_ID FROM " +
        			" INSURANCE_CASE IC,TPA_MASTER TA " +
        			"WHERE IC.TPA_ID=TA.TPA_ID AND IC.MR_NO=?",
        			ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        	ps.setString(1, mrno);
        	tpaList = DataBaseUtil.queryToDynaList(ps);
        }
        finally {
        	DataBaseUtil.closeConnections(con, ps);
        }
        return tpaList;
    }

    private static final String TPAS_NAMESAND_iDS="select tpa_id,tpa_name,sponsor_type from tpa_master";

	public static List getTpasNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(TPAS_NAMESAND_iDS));
	}

	public static List getActiveTpasNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(TPAS_NAMESAND_iDS+" WHERE status='A' "));
	}

	private static final String GET_TPA_PAYER_ID =
		" SELECT CASE WHEN tpa_code IS NULL OR trim(htc.tpa_code) = '' THEN '@'||tpa_name " +
		" ELSE tpa_code END AS payer_id" +
		" FROM tpa_master tm " +
		" LEFT JOIN ha_tpa_code htc ON(htc.tpa_id=tm.tpa_id AND htc.health_authority = ?)" +
		" WHERE tm.tpa_id = ? ";

	public static BasicDynaBean getTpaPayerID(String tpaId) throws SQLException {
		Integer centerId = RequestContext.getCenterId();
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		return DataBaseUtil.queryToDynaBean(GET_TPA_PAYER_ID,new Object[]{healthAuthority,tpaId});
	}

	public static String getTPACode(String tpaId, Integer centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		if (null == centerId) {
			centerId = RequestContext.getCenterId();
		}
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_TPA_PAYER_ID);
			ps.setString(1, healthAuthority);
			ps.setString(2, tpaId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getTpaDetails(String tpaId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getConnection();
		try {
			ps = con.prepareStatement("SELECT * FROM tpa_master WHERE tpa_id=? ");
			ps.setString(1, tpaId);
			return (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CLAIM_FORMAT ="select claim_format "+
	 " from  tpa_center_master  " +
	 "  where tpa_id=? and center_id=? ";

	public static String getClaimformat(String tpaId,int cenId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CLAIM_FORMAT);
			ps.setString(1, tpaId);
			ps.setInt(2, cenId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String TPA_SELECT = " select *  ";
	private static final String TPA_COUNT = " select count(tpa_id) ";
	private static final String TPA_FORM = " from (select tm.tpa_id,tm.tpa_name,tm.contact_email,tm.contact_mobile,tm.sponsor_type_id as sponsor_type_id,"+
								" st.sponsor_type_name,tm.status from tpa_master tm left join sponsor_type st "+
								" using(sponsor_type_id))as foo  ";
	
	public static PagedList  getTpaMasterList(Map mapParams,Map<LISTING, Object> listingParams)throws SQLException,
	ParseException{
		Connection con = null;
		SearchQueryBuilder qb= null;
		try{
			con =DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, TPA_SELECT, TPA_COUNT, TPA_FORM, listingParams);
			qb.addFilterFromParamMap(mapParams);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
}
