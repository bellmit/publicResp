package com.insta.hms.master.ServiceMaster;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

public class ServiceMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ServiceMasterDAO.class);

	public ServiceMasterDAO() {
		super("services");
	}

	public String getNextId() throws SQLException {
		return AutoIncrementId.getNewIncrId("service_id", "SERVICES", "Services");
	}

	/*
	 * Search: returns a PagedList suitable for a dashboard type list
	 */
	private static final String SEARCH_FIELDS = "select *";
		//" SELECT sod.service_id, sod.applicable, sod.item_code, " +
		//"  s.service_name, s.status, s.dept_name ";

	private static final String SEARCH_COUNT =  " SELECT count(*) ";

	private static final String SEARCH_TABLES =
		" FROM (SELECT sod.service_id, sod.org_id, sod.applicable, sod.item_code," +
		" s.service_name, s.status, sd.department AS dept_name, sod.code_type, " +
		" s.service_sub_group_id, od.org_name, 'services'::text as chargeCategory,sod.is_override  " +
		" FROM service_org_details sod " +
		" JOIN services s ON (s.service_id = sod.service_id) " +
		" JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id)" +
		" JOIN organization_details od on(od.org_id = sod.org_id))  AS foo ";

//	private static final String INIT_WHERE = " WHERE sod.org_id=? ";

	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("service_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public boolean insert(Connection con, BasicDynaBean b) throws SQLException, java.io.IOException {
		try {
			super.insert(con, b);
			return true;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return false;
	}

	public int update(Connection con, Map columnData, String keyName, Object keyValue)
		throws SQLException, IOException {
		try {
			int rows = super.update(con, columnData, keyName, keyValue);
			return rows;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return 0;
	}

	public List<String> getAllNames() throws SQLException {
		return getColumnList("service_name");
	}

	private static final String SERVICE_DETAILS =
		" SELECT s.*, sod.org_id, sod.applicable, sod.item_code, o.org_name, sd.serv_dept_id, sod.code_type, qty_split_in_pending_presc, "+
		" sod.special_service_code,sod.special_service_contract_name " +
		" FROM services s " +
		"  JOIN service_org_details sod ON (sod.service_id = s.service_id) " +
		"  JOIN organization_details o ON (o.org_id = sod.org_id) " +
		"  JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id)"+
		" WHERE s.service_id=? AND sod.org_id=? ";

	public BasicDynaBean getServiceDetails(String serviceId, String orgId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(SERVICE_DETAILS, serviceId, orgId);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}


  private static final String SERVICES_NAMESAND_iDS="SELECT service_name,service_id FROM services";

    public List getServicesNamesAndIds() throws SQLException{

    	return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(SERVICES_NAMESAND_iDS));
    }



   private static final String GET_SERVICE_DETAILS="" +
			   		"SELECT s.service_id,s.service_name,s.units,s.service_tax,"+
			   			"s.status,s.conduction_applicable,s.activity_timing_eclaim,ssg.service_sub_group_name," +
			   			"sg.service_group_name, " +
			   			"CASE WHEN s.specialization='D' THEN 'Dialysis' ELSE '' END AS specialization_name, " +
			   			"sd.department,s.service_code, CASE WHEN service_duration IS NULL THEN (select default_duration from scheduler_master where " +
                        " res_sch_category='SNP' AND dept='*' AND res_sch_name='*' AND res_sch_type='SRID' AND status='A') ELSE service_duration END as service_duration " +
                        " FROM services s " +
			   			"LEFT JOIN service_item_sub_groups sisg using (service_id)" +
			   		"JOIN  services_departments sd  using(serv_dept_id) " +
			   		" JOIN service_sub_groups ssg using(service_sub_group_id) " +
			   		"JOIN service_groups sg using(service_group_id) " +
			   		"LEFT JOIN item_sub_groups_tax_details sgtd ON sgtd.item_subgroup_id = sisg.item_subgroup_id " +
			   		"ORDER BY service_id";

    public static List<BasicDynaBean> getServiceDetails() throws SQLException{

    	List serviceList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_SERVICE_DETAILS);
		serviceList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return serviceList;
    }

    public List<BasicDynaBean> getActiveInsuranceCategories(String serviceId){
      return DatabaseHelper.queryToDynaList(SELECT_INSURANCE_CATEGORY_IDS,serviceId);
    }

    private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
        + "FROM service_insurance_category_mapping "
        + "WHERE service_id =?";
  
    
    private static final String SELECT_MAPPED_SERVICE_RESOURCES = " SELECT srm.serv_res_id, srm.serv_resource_name "
    	+ " FROM service_resource_master srm"
        + " JOIN service_service_resources_mapping ssrm ON (ssrm.serv_res_id = srm.serv_res_id)"
        + " WHERE service_id = ? AND srm.schedule='t' #CENTERFILTER#";
    
    public List<BasicDynaBean> getMappedServiceResources(String serviceId, int centerId) {
      
    	String queryForCenter = SELECT_MAPPED_SERVICE_RESOURCES;
    	if (centerId != 0) {
    		queryForCenter = queryForCenter.replace("#CENTERFILTER#", "AND srm.center_id = ?");
    		return DatabaseHelper.queryToDynaList(queryForCenter, serviceId, centerId);
    	} else {
    		queryForCenter = queryForCenter.replace("#CENTERFILTER#", "");
    		return DatabaseHelper.queryToDynaList(queryForCenter, new Object[]{serviceId});
    	}
    }

    private String SERVICE_RESOURCES_BY_CENTER = "SELECT serv_res_id,serv_resource_name FROM "
      + " service_resource_master srm "
      + " where srm.status='A' and srm.schedule='t' ##CENTERFILTER## ";

    public List<BasicDynaBean> getServiceResourcesByCenter(int centerId) {
        String query = SERVICE_RESOURCES_BY_CENTER;
        if (centerId != 0) {
          query = query.replace("##CENTERFILTER##", "and srm.center_id = ?");
          return DatabaseHelper.queryToDynaList(query, new Object[]{centerId});
        } else {
          query = query.replace("##CENTERFILTER##", "");
          return DatabaseHelper.queryToDynaList(query);
        }
      }

    private static final String GET_SERVICE_SUBTASK_DETAILS="" +
	   		"SELECT s.service_id,st.sub_task_id,st.desc_long,st.desc_short,st.status,st.display_order FROM service_sub_tasks st " +
    		"LEFT JOIN services s using(service_id) " +
	   		"WHERE s.service_id =? ORDER BY st.display_order";

    public static List getServiceSubTasksDetails(String serviceId) throws SQLException {
    	
    	PreparedStatement ps=null;
    	Connection con=null;
    	try {    	
    	con=DataBaseUtil.getConnection();
       	ps=con.prepareStatement(GET_SERVICE_SUBTASK_DETAILS);
       	ps.setString(1, serviceId);
    	return DataBaseUtil.queryToDynaList(ps);
    	} catch(Exception ex) {
    		throw new ExceptionInInitializerError(ex);
    	}
    	finally {
    	DataBaseUtil.closeConnections(con, ps);
    	}

    }

    private static final String GET_SERVICE_NAME = "SELECT service_name, service_id  FROM services" +
    		" WHERE service_name = ?";


    public boolean isDuplicateName(String serviceName) throws SQLException {

    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	boolean result = false;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		pstmt = con.prepareStatement(GET_SERVICE_NAME);
    		pstmt.setString(1, serviceName);
    		rs = pstmt.executeQuery();
    		if (rs.next())
    			result = true;
    	} finally {
    		DataBaseUtil.closeConnections(con, pstmt);
    	}
    	return result;
    }

    private static final String SERVICES_FOR_ORG =
    	" SELECT s.service_name, s.service_id, unit_charge as charge, discount, " +
    	"	service_code::text as order_code, s.prior_auth_required, sg.service_group_name, tooth_num_required, s.doc_speciality_id  " +
		" FROM services s " +
		"	JOIN service_sub_groups ssg USING (service_sub_group_id) " +
		"	JOIN service_groups sg USING (service_group_id) " +
		"	JOIN services_departments dep ON (dep.serv_dept_id=s.serv_dept_id) "+
		"	JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id)" +
		" 	LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.applicable AND sod.service_id=s.service_id " +
		"	JOIN service_master_charges smc ON (smc.service_id=s.service_id and smc.org_id=sod.org_id and smc.bed_type=?)" +
		" WHERE s.status='A' AND dt.dept_type_id='DENT' AND (service_name ilike ? OR service_code ilike ? OR service_name ilike ?) limit 100";
    public static List getServices(String bedType, String orgId, String findItem) throws SQLException {
    	Connection con = DataBaseUtil.getConnection();
    	PreparedStatement ps = null;
    	try {
    		ps = con.prepareStatement(SERVICES_FOR_ORG);
    		ps.setString(1, orgId);
    		ps.setString(2, bedType);
    		ps.setString(3, findItem + "%");
			ps.setString(4, findItem + "%");
			ps.setString(5, "% " +findItem+ "%");
			return DataBaseUtil.queryToDynaList(ps);
    	} finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }
    
    
    

    private String INSERT_HL7_INTERFACE = "INSERT INTO services_export_interface " +
       		"( service_id, interface_name) " +
       		"VALUES(?,?)";

       /**
        * Insert multiple interface corresponding one testId
        *
        * Example :
        *
        * test_id  interface_name
        * -------   ------------
        *    1      TMT
        *    2      TMT
        *
        *
        * @param serviceId
        * @param interfaceNames
        * @return
        * @throws SQLException
        */
    public boolean insertHl7Interfaces(Connection con, String serviceId,String[] interfaceNames)
       throws SQLException {

    		if(interfaceNames == null || interfaceNames.equals("")){
    			return false;
    		}
	       	boolean isSuccess = true;
	       	PreparedStatement ps = null;
	       	 try{
		       	 ps = con.prepareStatement(INSERT_HL7_INTERFACE);
		
		       	for (String interfaceName : interfaceNames) {
		       		ps.setString(1, serviceId);
		       		ps.setString(2, interfaceName);
		       		ps.addBatch();
		   		}
		
		       	int count[] = ps.executeBatch();
		       	isSuccess = count!=null;
	       	 }finally {
	       		DataBaseUtil.closeConnections(null, ps);
	       	 }
	       	return isSuccess;
      }

    

    /**
     *For updating interface name corresponding each test its need to be delete the interface rows and reinsert the interface rows
     *
     * Cause :
     *
     * test_id  interface_name
     * ------   ------------
     *    1     TMT
     *    1     PowerScribe
     *    1     iPlatina
     *
     *  Now Updated one requred only iSite and PowerScribe so we need to detlete and re-insert in this table.
     *
     *
     * @param serviceId
     * @param interfaceNames
     * @return boolean
     */

    public boolean updateHl7Interface(Connection con, String serviceId, String[] interfaceNames) throws SQLException{
    	boolean success = true;

    	if(deleteHl7Interface(con,serviceId) >= 0) {
    		success = insertHl7Interfaces(con, serviceId, interfaceNames);
    	}
    	return success;

    }


    private String DELETE_INTERFACE = "DELETE FROM services_export_interface WHERE service_id = ?";

    /**
     * Deleting All interface name rows corresponding testId.
     * @param servicesId
     * @return
     */

   public int deleteHl7Interface(Connection con, String servicesId) throws SQLException{
	   int updatedRow = 0;
	   PreparedStatement ps = null;
	   try{
		   ps = con.prepareStatement(DELETE_INTERFACE);
		   ps.setString(1, servicesId);
		   updatedRow =  ps.executeUpdate();
	   }finally {
		   DataBaseUtil.closeConnections(null, ps);
	   }
		   return updatedRow;
		   
   }
   
   
   private static final String GET_SERVICE_ITEM_SUBGROUP_DETAILS = "select sisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from service_item_sub_groups sisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = sisg.item_subgroup_id) "+
			" left join services s on (s.service_id = sisg.service_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where sisg.service_id = ? "; 

	public static List<BasicDynaBean> getServiceItemSubGroupDetails(String serviceId) throws SQLException {
		List list = null;
		Connection con = null;
	    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_SERVICE_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, serviceId);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
		}
   
	private static final String GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name "+
			" FROM services_item_sub_groups sisg "+
			" JOIN item_sub_groups isg ON(sisg.item_subgroup_id = isg.item_subgroup_id) "+
			" WHERE sisg.service_id = ? ";
	
	public List<BasicDynaBean> getServiceItemSubGroupTaxDetails(String itemId)throws SQLException{
		return DataBaseUtil.queryToDynaList(GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS, itemId);
	}

}

