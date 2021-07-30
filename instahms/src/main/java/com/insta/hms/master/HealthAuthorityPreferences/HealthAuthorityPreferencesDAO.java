/**
 * mithun.saha
 */
package com.insta.hms.master.HealthAuthorityPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */
public class HealthAuthorityPreferencesDAO extends GenericDAO{

	public HealthAuthorityPreferencesDAO() {
		super("health_authority_preferences");
	}

	private static final String UPDATE_HEALTH_AUTHORITY_PREFS = " UPDATE health_authority_preferences SET health_authority = ?," +
			" diagnosis_code_type=?,prescriptions_by_generics=?, consultation_code_types=?, drug_code_type=?, " +
			" default_gp_first_consultation=?, default_gp_revisit_consultation=?, default_sp_first_consultation=?," + 
			" default_sp_revisit_consultation=?,child_mother_ins_member_validity_days=?," +
			" presc_doctor_as_ordering_clinician=?, base_rate_plan=? "+
			" WHERE health_authority = ?";

	public static boolean saveHealthPrefs(HealthAuthorityDTO hdto) throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		boolean flag = false;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_HEALTH_AUTHORITY_PREFS);
			int i=1;
			ps.setString(i++, hdto.getHealth_authority());
			ps.setString(i++, hdto.getDiagnosis_code_type());
			ps.setString(i++, hdto.getPrescriptions_by_generics());
			String[] consultationCodeTypes = hdto.getConsultation_code_types();
			String consultationCodeType = "";
			if (consultationCodeTypes != null) {
				boolean first = true;
				for (String codeType : consultationCodeTypes) {
					if (!first) {
						consultationCodeType += ",";
					}
					consultationCodeType += codeType;
					first = false;
				}
			}
			String[] drugCodeTypes = hdto.getDrug_code_type();
			String drugCodeType = "";
			if (drugCodeTypes != null) {
				boolean first = true;
				for (String codeType : drugCodeTypes) {
					if (!first) {
						drugCodeType += ",";
					}
					drugCodeType += codeType;
					first = false;
				}
			}
			ps.setString(i++, consultationCodeType);
			ps.setString(i++, drugCodeType);
			ps.setInt(i++, hdto.getDefault_gp_first_consultation());
			ps.setInt(i++, hdto.getDefault_gp_revisit_consultation());
			ps.setInt(i++, hdto.getDefault_sp_first_consultation());
			ps.setInt(i++, hdto.getDefault_sp_revisit_consultation());
			ps.setInt(i++, hdto.getChild_mother_ins_member_validity_days());
			ps.setString(i++, hdto.getPresc_doctor_as_ordering_clinician());
			ps.setString(i++, hdto.getBase_rate_plan());
			ps.setString(i++, hdto.getHealth_authority());

			if (ps.executeUpdate() == 1) {
				flag = true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return flag;
	}

	private static final String GET_HEALTH_AUTHORITY_PREFS = "SELECT health_authority,diagnosis_code_type,prescriptions_by_generics," +
			" consultation_code_types,drug_code_type, default_gp_first_consultation, default_gp_revisit_consultation, " +
			" default_sp_first_consultation, default_sp_revisit_consultation,child_mother_ins_member_validity_days,presc_doctor_as_ordering_clinician " +
			" FROM health_authority_preferences";

	public static HealthAuthorityDTO getHealthAuthorityPreferences() throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		HealthAuthorityDTO dto = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_HEALTH_AUTHORITY_PREFS);
			rs = ps.executeQuery();
			if (rs.next()){
				dto = new HealthAuthorityDTO();
				dto.setHealth_authority(rs.getString("health_authority"));
				dto.setDiagnosis_code_type(rs.getString("diagnosis_code_type "));
				dto.setPrescriptions_by_generics(rs.getString("prescriptions_by_generics"));
				dto.setConsultation_code_types((null != rs.getString("consultation_code_types") && !rs.getString("consultation_code_types").equals(""))
						? rs.getString("consultation_code_types").split(",") : null);
				dto.setDrug_code_type((null != rs.getString("drug_code_type") && !rs.getString("drug_code_type").equals(""))
						? rs.getString("drug_code_type").split(",") : null);
				dto.setDefault_gp_first_consultation(rs.getInt("default_gp_first_consultation"));
				dto.setDefault_gp_revisit_consultation(rs.getInt("default_gp_revisit_consultation"));
				dto.setDefault_sp_first_consultation(rs.getInt("default_sp_first_consultation"));
				dto.setDefault_sp_revisit_consultation(rs.getInt("default_sp_revisit_consultation"));
				dto.setChild_mother_ins_member_validity_days(rs.getInt("child_mother_ins_member_validity_days"));
				dto.setPresc_doctor_as_ordering_clinician(rs.getString("presc_doctor_as_ordering_clinician"));
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return dto;
	}

	private static final String GET_HEALTH_PREFS = "SELECT * FROM health_authority_preferences WHERE health_authority = ?";

	public static HealthAuthorityDTO getHealthAuthorityPreferences(String healthAuth) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		HealthAuthorityDTO dto = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_HEALTH_PREFS);
			ps.setString(1, null == healthAuth ? "" : healthAuth);
			rs = ps.executeQuery();
			dto = new HealthAuthorityDTO();
			if (rs.next()){
				dto.setHealthAuthority(rs.getString("health_authority"));
				dto.setHealth_authority(rs.getString("health_authority"));
				dto.setVisitClassificationReq(rs.getBoolean("is_visit_classification_mandatory"));
				dto.setDiagnosis_code_type(rs.getString("diagnosis_code_type"));
				dto.setPrescriptions_by_generics(rs.getString("prescriptions_by_generics"));
				dto.setConsultation_code_types((null != rs.getString("consultation_code_types") && !rs.getString("consultation_code_types").equals(""))
						? rs.getString("consultation_code_types").split(",") : null);
				dto.setDrug_code_type((null != rs.getString("drug_code_type") && !rs.getString("drug_code_type").equals(""))
						? rs.getString("drug_code_type").split(",") : null);
				dto.setDefault_gp_first_consultation(rs.getInt("default_gp_first_consultation"));
				dto.setDefault_gp_revisit_consultation(rs.getInt("default_gp_revisit_consultation"));
				dto.setDefault_sp_first_consultation(rs.getInt("default_sp_first_consultation"));
				dto.setDefault_sp_revisit_consultation(rs.getInt("default_sp_revisit_consultation"));
				dto.setChild_mother_ins_member_validity_days(rs.getInt("child_mother_ins_member_validity_days"));
				dto.setPresc_doctor_as_ordering_clinician(rs.getString("presc_doctor_as_ordering_clinician"));
				dto.setBase_rate_plan(rs.getString("base_rate_plan"));

			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return dto;
	}

	public static final String GET_ALL_HEALTH_AUTHORITY_PREFS = "SELECT * FROM health_authority_preferences where health_authority = ?";

	public static BasicDynaBean getAllHealthAuthorityPrefs(String healthAuthority) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_HEALTH_AUTHORITY_PREFS);
			ps.setString(1, healthAuthority);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String HEALTH_AUTHORITY_FIELDS = "SELECT * " ;

	private static String HEALTH_AUTHORITY_COUNT = "SELECT count(*)";

	private static String HEALTH_AUTHORITY_TABLES = " FROM (SELECT  * FROM health_authority_preferences JOIN health_authority_master USING(health_authority)) AS foo";

	@MigratedTo(value=HealthAuthorityPreferencesRepository.class,method="getSearchQuery")
	public PagedList getHealthAuthorityDetails(Map map,Map pagingParams)throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb =
				new SearchQueryBuilder(con,HEALTH_AUTHORITY_FIELDS,HEALTH_AUTHORITY_COUNT,
						HEALTH_AUTHORITY_TABLES,pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("health_authority", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static boolean saveHealthAuthorityPrefs(HealthAuthorityDTO dto) throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		boolean flag = false;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_HEALTH_AUTHORITY_PREFS);
			int i=1;
			ps.setString(i++, dto.getHealth_authority());
			ps.setString(i++, dto.getDiagnosis_code_type());
			ps.setString(i++, dto.getPrescriptions_by_generics());
			String[] CodeTypes = dto.getConsultation_code_types();
			String consultationCodeType = "";
			if (CodeTypes != null) {
				boolean first = true;
				for (String codeType : CodeTypes) {
					if (!first) {
						consultationCodeType += ",";
					}
					consultationCodeType += codeType;
					first = false;
				}
			}
			String[] drugCodeTypes = dto.getDrug_code_type();
			String drugCodeType = "";
			if (drugCodeTypes != null) {
				boolean first = true;
				for (String codeType : drugCodeTypes) {
					if (!first) {
						drugCodeType += ",";
					}
					drugCodeType += codeType;
					first = false;
				}
			}
			ps.setString(i++, consultationCodeType);
			ps.setString(i++, drugCodeType);
			ps.setInt(i++, dto.getDefault_gp_first_consultation());
			ps.setInt(i++, dto.getDefault_gp_revisit_consultation());
			ps.setInt(i++, dto.getDefault_sp_first_consultation());
			ps.setInt(i++, dto.getDefault_sp_revisit_consultation());
			ps.setInt(i++, dto.getChild_mother_ins_member_validity_days());
			ps.setString(i++, dto.getPresc_doctor_as_ordering_clinician());
			ps.setString(i++, dto.getBase_rate_plan());
			ps.setString(i++, dto.getHealth_authority());
			
			if (ps.executeUpdate() == 1) {
				flag = true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return flag;
	}

	private static final String GET_HELATH_PREFERENCES= "SELECT * FROM (" +
			"	select regexp_split_to_table(drug_code_type,E',') as drug_code_type,health_authority FROM health_authority_preferences "+
			" 	) as hap " +
    		" JOIN health_authority_master ham ON(ham.health_authority=hap.health_authority) " +
    		" order by drug_code_type";

	public static List<BasicDynaBean> getHealthPrefs() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_HELATH_PREFERENCES);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ITEM_CODE_TYPE_BY_HELATH_AUTHORITY= "SELECT * FROM mrd_supported_codes msc "+
			" JOIN (select regexp_split_to_table(drug_code_type,E',') as drug_code_type,health_authority FROM health_authority_preferences "+
			" 		) as hap ON(msc.code_type = hap.drug_code_type) " +
			" JOIN health_authority_master ham ON(hap.health_authority=ham.health_authority)" +
			" WHERE msc.code_category = ? order by drug_code_type";

	public static List<BasicDynaBean> getItemCodeTypesByHealthAuthority(String codeCategory) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ITEM_CODE_TYPE_BY_HELATH_AUTHORITY);
			ps.setString(1, codeCategory);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_HELATH_AUTHORITY_COUNT = "SELECT count(*) FROM health_authority_master";

	public static Integer getHealthAuthorityCount() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_HELATH_AUTHORITY_COUNT);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
