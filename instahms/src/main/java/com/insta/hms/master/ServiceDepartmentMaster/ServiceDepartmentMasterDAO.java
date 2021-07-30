package com.insta.hms.master.ServiceDepartmentMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class ServiceDepartmentMasterDAO extends GenericDAO {

	public ServiceDepartmentMasterDAO() {
		super("services_departments");
	}

	private static final String SELECT_FIELDS =	"SELECT dep.*, dt.dept_type_desc ";
	private static final String SELECT_TABLES =
		" FROM services_departments dep " +
		"	LEFT JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id) ";
	private static final String COUNT = "SELECT count(*) ";
	public static PagedList searchDeptList(Map params, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, SELECT_FIELDS, COUNT, SELECT_TABLES, listingParams);
			qb.addFilterFromParamMap(params);
			qb.addSecondarySort("serv_dept_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String SERVICES_NAMESAND_iDS = "select department,serv_dept_id from services_departments";

	public static List getServicesNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(SERVICES_NAMESAND_iDS));
	}

	public static String getServiceDepartmentStoreStr(String serviceId) throws SQLException {
        BasicDynaBean sBean = new GenericDAO("services").findByKey("service_id", serviceId);
        Integer servDeptId = (sBean != null && sBean.get("serv_dept_id") != null) ? (Integer) sBean.get("serv_dept_id") : null;
        Integer storeId = null;
        if (servDeptId != null) {
        	BasicDynaBean dDeptBean = new GenericDAO("services_departments").findByKey("serv_dept_id", servDeptId);
        	storeId = (dDeptBean != null && dDeptBean.get("store_id") != null) ? (Integer) dDeptBean.get("store_id") : null;
        }
        if (storeId != null) {
        	return new String(storeId+"");
        }
        return null;
	}
}