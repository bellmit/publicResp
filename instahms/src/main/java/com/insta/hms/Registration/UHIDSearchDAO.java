package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UHIDSearchDAO extends GenericDAO {
	
	public UHIDSearchDAO() {
		super("patient_details");
	}
	
	public List<BasicDynaBean> getPatientList(List<String> columns,Map filterMap,
			String sortColumn) throws SQLException {
		Connection conn = DataBaseUtil.getReadOnlyConnection();
		boolean shouldFilter = filterMap != null && !filterMap.isEmpty();
		StringBuilder query = new StringBuilder("SELECT ");
		if (columns == null || columns.isEmpty()) {
			query.append("*");
		}
		else {
			boolean first = true;
			for (String column : columns) {
				column = DataBaseUtil.quoteIdent(column);
				if (!first) query.append(", ");
				first = false;
				query.append(column);
			}
		}
		query.append(" FROM ").append("patient_details");

		if (shouldFilter) {
			Iterator it = filterMap.entrySet().iterator();
			query.append(" WHERE ");
			int i=1;
				while  ( it.hasNext (  )  )   {
				   Map.Entry e =  ( Map.Entry ) it.next (  ) ;
				   String key = (String)e.getKey ();
				   if(key.equals("patient_phone")){
					   query.append("regexp_replace(patient_phone,'^\\' || COALESCE(patient_phone_country_code,' ') ,'')");
				   }
				   else{
					   query.append(DataBaseUtil.quoteIdent(key));					   
				   }
				   query.append("=?");				  			   
				   if(i != filterMap.size())
					   query.append(" OR ");  					   
				  i++;
			}
		}

		if ((sortColumn != null) && !sortColumn.equals("")) {
			query.append(" ORDER BY " + DataBaseUtil.quoteIdent(sortColumn));
		}
		PreparedStatement ps = conn.prepareStatement(query.toString());

		try {
			if (shouldFilter) {
				Iterator it1 = filterMap.entrySet().iterator();
				int i=1;
				while  ( it1.hasNext (  )  )   {
					 Map.Entry e =  ( Map.Entry ) it1.next (  ) ;
					query.append(" WHERE ");
					ps.setObject(i, e.getValue());
					i++;
				}
			}
			return DataBaseUtil.queryToDynaList(ps);
		}
		finally {
			DataBaseUtil.closeConnections(conn, ps);
		}
	}

}
