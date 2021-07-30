package com.insta.hms.master.InComingHospitals;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.incominghospitals.IncomingHospitalsController;
import com.insta.hms.mdm.incominghospitals.IncomingHospitalsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InComingHospitalDAO  extends GenericDAO{

	public InComingHospitalDAO() {
		super("incoming_hospitals");
	}
	@MigratedTo(IncomingHospitalsRepository.class)
	public String getNextHospitalId() throws SQLException {

		String hospitalId = null;

		hospitalId = getNextFormattedId();

		return hospitalId;
	}

	private static String GET_ALL_HOSPITAL_NAME = "select hospital_id,hospital_name from incoming_hospitals ";

	public static List getAvalHospitalNames() {
		PreparedStatement ps = null;
		ArrayList hospitalnames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_HOSPITAL_NAME);
			hospitalnames = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return hospitalnames;
	}


	private static final String HOSPITALS_NAMESAND_iDS="select hospital_id,hospital_name from incoming_hospitals";
	   @MigratedTo(IncomingHospitalsController.class)
	   public static List getHospitalsNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(HOSPITALS_NAMESAND_iDS));
	}
	   
	   
       /**
        * Save Lab Name or Incoming Hospital
        * @param originalLabName
        * @param con
        * @return true if insert successfully or if it exists in database else false
        * 
        */

       public BasicDynaBean checkOrCreateIncomingHospital(Connection con, String originalLabName) throws SQLException, IOException {

    	   boolean status = false;
           BasicDynaBean exists = findByKey("hospital_name", originalLabName);
           if(exists !=null) return exists;
           BasicDynaBean bean = getBean();
           String orginalLabId = getNextHospitalId();
           bean.set("hospital_id",orginalLabId);
           bean.set("hospital_name", originalLabName);
           bean.set("status","A");
           status = insert(con, bean);
           if(status){
        	   return bean;
           }else {
        	   return null;
           }

       }
              
   	private static String GET_CENTER_OUTSOURCE_AGAINST_TEST = " SELECT d.test_id, hcm.center_id, dod.outsource_dest_id "+
			" FROM diag_outsource_detail dod "+
			" JOIN diagnostics d  ON (d.test_id = dod.test_id) "+
			" JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "+
			" JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) AND hcm.status='A' "+
			" WHERE dod.status = 'A' AND dod.source_center_id = ? AND dom.outsource_dest_type = 'C' ";
   	
	public static List<BasicDynaBean> getCenterOutSourceAgainstTest(int centerId, String testIds[]) throws SQLException {

    StringBuilder sb  = new StringBuilder(GET_CENTER_OUTSOURCE_AGAINST_TEST);
    if(testIds != null) {
      String[] placeHolderArr = new String[testIds.length];
      Arrays.fill(placeHolderArr, "?");
      String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
      sb.append("AND dod.test_id in ( " + placeHolders + ")");
    }
    int idx = 1;
    try (Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(sb.toString())) {
			ps.setInt(idx++, centerId);
			if(testIds != null) {
  			for (String testId: testIds) {
  			  ps.setString(idx++, testId);
  			}
			}
			return DataBaseUtil.queryToDynaList(ps);
		}
	}		

}
