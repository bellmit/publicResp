package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
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

public class EquipmentTestConductedDAO extends GenericDAO {

	public EquipmentTestConductedDAO(){
		super("equipment_test_conducted");
	}

	private static final String EQUIPMENT_CONDUCTED_DATA_QUERY =
		" SELECT eq_id,equipment_name   ";

	private static final String EQUIPMENT_CONDUCTED_COUNT_QUERY =
		" SELECT count(*) ";

	private static final String EQUIPMENT_CONDUCTED_FROM =
		" FROM test_equipment_master tm" +
		" JOIN equipment_test_result etr on(eq_id = etr.equipment_id) " +
		" LEFT JOIN equipment_test_conducted etc on(eq_id = etc.equipment_id)" ;

	private static final String GROUP_BY =
		" eq_id,equipment_name ";
	public PagedList listAll(Map filterMap,Map listingMap ) throws SQLException,ParseException{
		Connection con = null;
		SearchQueryBuilder qb = null;
		try{
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, EQUIPMENT_CONDUCTED_DATA_QUERY, EQUIPMENT_CONDUCTED_COUNT_QUERY
					, EQUIPMENT_CONDUCTED_FROM, " WHERE status = 'A'", GROUP_BY,listingMap);
			qb.addFilterFromParamMap(filterMap);

			qb.build();
			return qb.getDynaPagedList();
		}finally{
			DataBaseUtil.closeConnections(con, null);
			if (qb != null)
				qb.close();
		}
	}

	public static final String EQUIPMENT_CONDUCTION_DETAILS =
		"  SELECT *   FROM test_equipment_master                                        " +
		"    JOIN equipment_test_result etr on(eq_id = etr.equipment_id)                " +
		"    JOIN test_results_master trm on (etr.resultlabel_id = trm.resultlabel_id)  " ;

	public static final String EQUIPMENT_CONDUCTION_JOIN =
		"    JOIN equipment_test_values etv on(etr.resultlabel_id = etv.resultlabel_id) " +
		"    WHERE eq_id = ? ";

	public List<BasicDynaBean> equipmentConductionDetails(String equipmentId,String eqConductedId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(EQUIPMENT_CONDUCTION_DETAILS+EQUIPMENT_CONDUCTION_JOIN+" AND etv.equipment_conducted_id =?");
			ps.setInt(1, Integer.parseInt(equipmentId));
			ps.setInt(2, Integer.parseInt(eqConductedId));
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<BasicDynaBean> newEquipmentConductionDetails(String equipmentId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(EQUIPMENT_CONDUCTION_DETAILS+" WHERE eq_id = ?");
			ps.setInt(1, Integer.parseInt(equipmentId));
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
