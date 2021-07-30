package com.insta.hms.master.DiagCenterResultsApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.Result;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mohammed.r
 *
 */


public class DiagResultsCenterApplicabilityDAO extends GenericDAO {

		public DiagResultsCenterApplicabilityDAO() {
			super("test_results_center");
	}


		public static final String GET_CENTERS =
			" SELECT trm.resultlabel,trc.resultlabel_id ,trc.result_center_id,trc.center_id,trc.status,hcm.center_name," +
			" hcm.city_id,hcm.state_id, c.city_name, s.state_name " +
			" FROM test_results_center trc " +
			" LEFT JOIN hospital_center_master hcm ON (hcm.center_id=trc.center_id) " +
			" LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
			" LEFT JOIN state_master s ON (s.state_id=c.state_id)" +
			" LEFT JOIN test_results_master trm ON(trm.resultlabel_id=trc.resultlabel_id) " +
			" WHERE trm.test_id=?" +
			" ORDER BY s.state_name, c.city_name, hcm.center_name" ;

		public List getCenters(String testId) throws SQLException{
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_CENTERS);
				ps.setString(1, testId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

		public boolean delete(Connection con,Integer resultLabelId) throws SQLException {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = con.prepareStatement("SELECT * FROM test_results_center where resultlabel_id=? and center_id != 0");
				ps.setInt(1, resultLabelId);
				rs = ps.executeQuery();
				if (!rs.next()) return true; // no records to delete.

				ps = con.prepareStatement("DELETE FROM test_results_center where resultlabel_id=? and center_id != 0");
				ps.setInt(1, resultLabelId);
				int rowsDeleted = ps.executeUpdate();
				return (rowsDeleted != 0);
			} finally {
				DataBaseUtil.closeConnections(null, ps);
			}
		}

		public boolean delete(Connection con, Integer centerId, Integer resultLabelId) throws SQLException {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = con.prepareStatement("SELECT * FROM test_results_center where resultlabel_id=? and center_id = ?");
				ps.setInt(1, resultLabelId);
				ps.setInt(2, centerId);
				rs = ps.executeQuery();
				if (!rs.next()) return true; // no records to delete.

				ps = con.prepareStatement("DELETE FROM test_results_center where resultlabel_id=? and center_id = ?");
				ps.setInt(1, resultLabelId);
				ps.setInt(2, centerId);
				int rowsDeleted = ps.executeUpdate();
				return (rowsDeleted != 0);
			} finally {
				DataBaseUtil.closeConnections(null, ps);
			}
		}


		public boolean insert(Connection con, String copyFromResultlabel_id, String copyToResultlabel_id) throws SQLException {
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement("INSERT INTO test_results_center(result_center_id, resultlabel_id, center_id, status)" +
					"(SELECT nextval('test_results_center_seq'), ?, 0, status FROM test_results_master WHERE resultlabel_id=?");
				ps.setString(1, copyToResultlabel_id);
				ps.setString(2, copyFromResultlabel_id);
				return ps.executeUpdate() > 0;
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

		public boolean insertResultsCenter(ArrayList<Result> results,
				boolean success, Connection con) throws SQLException,IOException,Exception {
			int centerId = RequestContext.getCenterId();
			if (success) {
				for(int i=0; i<results.size(); i++) {
					if (!"".equals(results.get(i).getExpression())) {
						BasicDynaBean diagbean = this.getBean();
			        	diagbean.set("result_center_id", this.getNextSequence());
			        	diagbean.set("resultlabel_id",Integer.parseInt(results.get(i).getResultlabel_id()));
			        	diagbean.set("center_id", centerId);
			        	diagbean.set("status", "A");
						success &= this.insert(con, diagbean);
					} else {
						BasicDynaBean diagbean = this.getBean();
			        	diagbean.set("result_center_id", this.getNextSequence());
			        	diagbean.set("resultlabel_id",Integer.parseInt(results.get(i).getResultlabel_id()));
			        	diagbean.set("center_id", centerId);
			        	diagbean.set("status", "A");
						success &= this.insert(con, diagbean);
					}
				}
				return success;
			} else {
			return false;
			}
		}
		
		public boolean deleteResultsCenter(ArrayList<Result> deletedResults,boolean success,Connection con)
		    	throws SQLException,IOException {
					if (success) {
					BasicDynaBean diagbean = this.getBean();
			    	for(Result modifedResultRange : deletedResults){
			    		diagbean = this.findByKey("resultlabel_id", new Integer(modifedResultRange.getResultlabel_id()));
			    		if (diagbean != null)
			    			success &= this.delete(con, "resultlabel_id",new Integer(modifedResultRange.getResultlabel_id()));
			    	}
			    	return success;
				} else {
					return false;
				}
		}
		
		 private static final String GET_RESULTS_LIST = " select test_id,units,display_order,trm.resultlabel_id," +
			" expr_4_calc_result, case when trm.method_id is not null then resultlabel|| '.' ||method_name " +
			" else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list," +
			" resultlabel_short, hl7_export_code,trm.method_id "+
			" FROM test_results_master trm "+
			" LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "+
			" LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "+
			" WHERE trm.test_id=? and  trc.center_id = 0" ;
		
		 public List<BasicDynaBean> getResultsList(Connection con,String testId) throws SQLException,Exception {
			 PreparedStatement ps = null;
			 try {
				 ps = con.prepareStatement(GET_RESULTS_LIST);
				 ps.setString(1, testId);
				 return DataBaseUtil.queryToDynaList(ps);
			 }finally{
				 if(ps!=null) ps.close();
			 }
		 }
		 
		 private static final String CHECKCENTERS = " SELECT center_id from test_results_center" +
				 	" LEFT JOIN test_results_master USING (resultlabel_id)" +
				 	" WHERE resultlabel =? and test_id=?";
		 
		 public boolean checkCenters(String resultLabel, String test_id) throws SQLException{
			 PreparedStatement ps = null;
			 Connection con = null;
			 try{
				 con = DataBaseUtil.getReadOnlyConnection();
				 ps = con.prepareStatement(CHECKCENTERS);
				 ps.setString(1, resultLabel);
				 ps.setString(2, test_id);
				 return true;
			 }finally{
				 DataBaseUtil.closeConnections(con, ps);
			 }
		 }
		 
		 private static final String GET_RESULTS_LIST_FOR_TEST = " select test_id,units,display_order,trm.resultlabel_id," +
			 		" expr_4_calc_result, case when trm.method_id is not null then resultlabel|| '.' ||method_name " +
			 		" else resultlabel end as resultlabel, code_type,result_code,data_allowed,source_if_list," +
			 		" resultlabel_short, hl7_export_code,trm.method_id "+
			 		" FROM test_results_master trm "+
			 		" LEFT JOIN diag_methodology_master dm ON (dm.method_id = trm.method_id) "+
			 		" LEFT JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "+
			 		" WHERE trm.test_id=?";
	
		 public static List getResultsListForJson(String testId) throws SQLException{
			 PreparedStatement ps = null;
			 Connection con = null;
			 try {
				 con = DataBaseUtil.getReadOnlyConnection();
				 ps = con.prepareStatement(GET_RESULTS_LIST_FOR_TEST);
				 ps.setString(1, testId);
				 return DataBaseUtil.queryToDynaList(ps);
			 }finally{
				 DataBaseUtil.closeConnections(con, ps);
			 }
		 }
		 
		 public static final String GET_CENTERS_JSON =
					" SELECT trm.resultlabel,trc.resultlabel_id ,trc.center_id " +
					" FROM test_results_center trc " +
					" LEFT JOIN hospital_center_master hcm ON (hcm.center_id=trc.center_id) " +
					" LEFT JOIN test_results_master trm ON(trm.resultlabel_id=trc.resultlabel_id) " +
					" WHERE trm.test_id=?" +
					" ORDER BY trm.resultlabel" ;

		public List getCentersJson(String testId) throws SQLException{
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_CENTERS_JSON);
				ps.setString(1, testId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
		
		public static final String GET_CENTERS_REQUEST_METHODLOGY =
			"	SELECT trc.center_id from test_results_center trc " +
			"	LEFT JOIN test_results_master trm on trm.resultlabel_id = trc.resultlabel_id " +
			"	LEFT JOIN diag_methodology_master dmm on dmm.method_id = trm.method_id " +
			"	WHERE trm.resultlabel =? and dmm.method_name =? and trm.test_id =?"; 
		
		public static final String GET_CENTERS_REQUEST_RESULTLABELS = 
			"	SELECT trc.center_id from test_results_center trc " +	
			"	LEFT JOIN test_results_master trm on trm.resultlabel_id = trc.resultlabel_id " +
			"	LEFT JOIN diag_methodology_master dmm on dmm.method_id = trm.method_id " +
			"	WHERE trm.resultlabel =? and dmm.method_name is null and  trm.test_id =?";
		

			public List<BasicDynaBean> getCentersRequest(Connection con, String resultlabel ,String methodname, String testId) throws SQLException{
				PreparedStatement ps = null;
				try {
					if (!methodname.equals("")) {
						ps = con.prepareStatement(GET_CENTERS_REQUEST_METHODLOGY);
						ps.setString(1, resultlabel);
						ps.setString(2, methodname);
						ps.setString(3, testId);
					} else {
						ps = con.prepareStatement(GET_CENTERS_REQUEST_RESULTLABELS);
						ps.setString(1, resultlabel);
						ps.setString(2, testId);
					}
					return DataBaseUtil.queryToDynaList(ps);
				} finally {
					DataBaseUtil.closeConnections(con, ps);
				}
			}
			
			public static final String GET_ALL_EXPRESSION_FOR_TEST =
					"   SELECT expr_4_calc_result,resultlabel,method_name FROM test_results_master trm " +
					"	LEFT JOIN diag_methodology_master dmm ON (dmm.method_id = trm.method_id) " +
					"	WHERE test_id =? AND expr_4_calc_result != ''";
			
			public List getExpressionForTest(String testId) throws SQLException{
				Connection con = DataBaseUtil.getConnection();
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(GET_ALL_EXPRESSION_FOR_TEST);
					ps.setString(1, testId);
					return DataBaseUtil.queryToDynaList(ps);
				} finally {
					DataBaseUtil.closeConnections(con, ps);
				}
				
			}
			
		
}
