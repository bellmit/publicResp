package com.insta.hms.ScoreCard;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class ScoreCardDAO{

	private static final String GET_KTV =
		" SELECT ROUND(ktv,2) AS ktv "+
		" FROM "+
		" clinical_dial_adeq_values "+
		" WHERE mr_no = ? "+
		" AND values_as_of_date::DATE BETWEEN ? AND ?  "+
		" ORDER BY values_as_of_date::DATE DESC "+
		" LIMIT 1 ";

	public static BigDecimal getKtv(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean ktvBean = DataBaseUtil.queryToDynaBean(GET_KTV, new Object[]{mrNo,fd,td});
		BigDecimal ktv = ktvBean== null? null: (BigDecimal)ktvBean.get("ktv");
		return ktv;
	}

	private static final String GET_URR =
		" SELECT ROUND(urr,2) AS urr "+
		" FROM  "+
		" clinical_dial_adeq_values "+
		" WHERE mr_no = ?  "+
		" AND values_as_of_date::DATE BETWEEN ? AND ?  "+
		" ORDER BY values_as_of_date::DATE DESC   "+
		" LIMIT 1  ";

	public static BigDecimal getUrr(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean urrBean = DataBaseUtil.queryToDynaBean(GET_URR, new Object[]{mrNo,fd,td});
		BigDecimal urr = urrBean== null? null:(BigDecimal)urrBean.get("urr");
		return urr;
	}

	private static final String GET_BLOOD_FLOW =
		" SELECT ROUND(blood_flow,2) as blood " +
		" FROM dialysis_prescriptions " +
		" WHERE mr_no = ? " +
		"  AND presc_date::DATE BETWEEN ? AND ?  " +
		" ORDER BY presc_date DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getBloodFlow(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean bloodBean = DataBaseUtil.queryToDynaBean(GET_BLOOD_FLOW, new Object[]{mrNo,fd,td});
		BigDecimal blood = bloodBean== null? null:(BigDecimal)bloodBean.get("blood");
		return blood;
	}

	private static final String GET_HB =
		" SELECT ROUND(convert_to_numeric(test_value),2) AS hb " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND ( resultlabel_short ilike 'hb' OR resultlabel ilike 'hb' )" +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getHB(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean hbBean =  DataBaseUtil.queryToDynaBean(GET_HB, new Object[]{fd,td,mrNo});
		BigDecimal hb = hbBean== null? null:(BigDecimal)hbBean.get("hb");
		return hb;
	}


	private static final String GET_WEIGHT_LOSS =
		" SELECT ROUND(AVG(total_wt_loss),2) AS weight FROM ( " +
		" SELECT total_wt_loss AS tw,* FROM dialysis_session ds " +
		" LEFT JOIN dialysis_prescriptions dp ON (ds.prescription_id = dp.dialysis_presc_id) " +
		" WHERE mr_no = ? " +
		" ORDER BY start_date DESC " +
		" LIMIT 6) foo WHERE foo.start_time::DATE BETWEEN ? AND ? " ;
	public static  BigDecimal getWeightLoss(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean weightBean =  DataBaseUtil.queryToDynaBean(GET_WEIGHT_LOSS, new Object[]{mrNo,fd,td});
		BigDecimal weight = weightBean== null? null:(BigDecimal)weightBean.get("weight");
		return weight;
	}


	private static final String GET_POST_BP =
		" SELECT fin_bp_high_sit||'/'||fin_bp_low_sit AS bp " +
		" FROM dialysis_session ds " +
		" LEFT JOIN dialysis_prescriptions dp ON (ds.prescription_id = dp.dialysis_presc_id) " +
		" WHERE  " +
		" mr_no = ? AND  " +
		" start_time::DATE BETWEEN ? AND ? " +
		" ORDER BY start_time DESC " +
		" LIMIT 1 " ;

	public static String getPostBP(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean bpBean =  DataBaseUtil.queryToDynaBean(GET_POST_BP, new Object[]{mrNo,fd,td});
		String bp = bpBean== null? null:(String)bpBean.get("bp");
		return bp;
	}


	private static final String GET_ALBUMIN =
		" SELECT ROUND(convert_to_numeric(test_value),2) AS albumin " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'albumin' OR resultlabel ilike 'albumin') " +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getAlbumin(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean albuminBean =  DataBaseUtil.queryToDynaBean(GET_ALBUMIN, new Object[]{fd,td,mrNo});
		BigDecimal albumin = albuminBean== null? null:(BigDecimal)albuminBean.get("albumin");
		return albumin;
	}

	private static final String GET_PTH =
		" SELECT ROUND(convert_to_numeric(test_value),2) AS pth " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'pth' OR resultlabel ilike 'pth' )" +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getPth(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean pthBean =  DataBaseUtil.queryToDynaBean(GET_PTH, new Object[]{fd,td,mrNo});
		BigDecimal pth = pthBean== null? null:(BigDecimal)pthBean.get("pth");
		return pth;
	}

	private static final String GET_CAXPO4 =
		" SELECT ROUND((SUM(calcium)*SUM(phosphorus)),2) AS caxpo4 FROM ( " +
		"  (  SELECT COALESCE(ROUND(convert_to_numeric(test_value),2),0) AS calcium,  null AS phosphorus " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'ca' OR resultlabel ilike 'calcium') " +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 ) " +
		" UNION " +
		"  ( SELECT null AS calicum, ROUND(convert_to_numeric(test_value),2) AS phosphorus " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'phos' OR resultlabel ilike 'phosphorus') " +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1) ) AS foo " ;

	public static BigDecimal getCaxpo4(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean caxpo4Bean =  DataBaseUtil.queryToDynaBean(GET_CAXPO4, new Object[]{fd,td,mrNo, fd,td,mrNo});
		BigDecimal caxpo4 = caxpo4Bean== null? null:(BigDecimal)caxpo4Bean.get("caxpo4");
		return caxpo4;
	}


	private static final String GET_TOTAL_PROTEIN =
		" SELECT ROUND(convert_to_numeric(test_value),2) AS total_protein " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'total protein' OR resultlabel ilike 'total protein')" +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getTotalProtein(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean totalproteinBean =  DataBaseUtil.queryToDynaBean(GET_TOTAL_PROTEIN, new Object[]{fd,td,mrNo});
		BigDecimal total_protein = totalproteinBean== null? null:(BigDecimal)totalproteinBean.get("total_protein");
		return total_protein;
	}

	private static final String GET_POTASSIUM =
		" SELECT ROUND(convert_to_numeric(test_value),2) AS potassium " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'k' OR resultlabel_short ilike 'potassium' OR resultlabel ilike 'potassium')" +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 " ;
	public static BigDecimal getPotassium(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean potassiumBean =  DataBaseUtil.queryToDynaBean(GET_POTASSIUM, new Object[]{fd,td,mrNo});
		BigDecimal potassium = potassiumBean== null? null:(BigDecimal)potassiumBean.get("potassium");
		return potassium;
	}

	// formula taken from internet: corrected calcium = calcium + 0.01 - albumin
	private static final String GET_CALCIUM =
		" SELECT (SUM(calcium)+0.01-SUM(albumin)) AS calcium FROM ( " +
		"  ( SELECT COALESCE(ROUND(convert_to_numeric(test_value),2),0) AS calcium, null AS albumin " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'ca' OR resultlabel ilike 'calcium') " +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1 ) " +
		" UNION " +
		"  ( SELECT null AS calicum, ROUND(convert_to_numeric(test_value),2) AS albumin " +
		" FROM test_results_master trm " +
		" JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id) " +
		" LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id) " +
		" WHERE mod_time::date BETWEEN ? AND ? " +
		" AND (resultlabel_short ilike 'albumin' OR resultlabel ilike 'albumin') " +
		" AND mrno = ? " +
		" ORDER BY mod_time DESC " +
		" LIMIT 1) ) AS foo " ;
	public static BigDecimal getCorrectedCalcium(String mrNo, Date fd, Date td) throws SQLException {
		BasicDynaBean ccaBean =  DataBaseUtil.queryToDynaBean(GET_CALCIUM, new Object[]{fd,td,mrNo,fd,td,mrNo});
		BigDecimal cca = ccaBean== null? null:(BigDecimal)ccaBean.get("calcium");
		return cca;
	}


	/**
	 * Score card List logic begins here...
	 */

	public static final String CARD_QUERY_SELECT = " SELECT * ";
	public static final String CARD_QUERY_COUNT = " SELECT count(*) ";
	public static final String CARD_QUERY_TABLE = " FROM score_card_main  ";

	public static PagedList searchScoreCardMain(Map filter, Map listing)
	throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				CARD_QUERY_SELECT, CARD_QUERY_COUNT, CARD_QUERY_TABLE, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("save_date");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	public int getCurrentCardSequence() throws SQLException {
		String query = "SELECT currval('score_card_main_score_card_id_seq')";
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement(query);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}

