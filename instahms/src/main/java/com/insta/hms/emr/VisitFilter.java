package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisitFilter implements Filter {
    static Logger log = LoggerFactory.getLogger(VisitFilter.class);

    private static String VISIT_DETAILS_TABLES = " FROM patient_registration pr "
        + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor) ";

	public List<DocHolder> applyFilter(List<EMRDoc> allDocs, String criteria) {

		List filteredDocs = new ArrayList();
		Collections.sort(allDocs, Collections.reverseOrder(new InstaComparator()));
		List<DocHolder> filterListDocs = new ArrayList<DocHolder>();
		List<EMRDoc> list = null;

		for (EMRDoc doc : allDocs) {
			String visitId = doc.getVisitid();
			if ((visitId == null) || visitId.equals("")) visitId = "GEN";
			Object[] filterListDocArray = filterListDocs.toArray();
			DocHolder docHolderObj = null;

			if(!exists(filterListDocs,visitId)) {
				list = new ArrayList<EMRDoc>();
				docHolderObj = new DocHolder();
				docHolderObj.setFilterId(visitId);
				docHolderObj.setViewDocs(list);
				list.add(doc);
				filterListDocs.add(docHolderObj);
			}else {
				for(Object fld : filterListDocArray) {
					DocHolder dd =	(DocHolder)fld;
					if(dd.getFilterId().equals(visitId)) dd.getViewDocs().add(doc);
				}
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		List visitList = new ArrayList();
		List genDocs = null;
		boolean genDocsPlaced = false;

		for (DocHolder doc : filterListDocs){
		 if(!doc.getFilterId().equalsIgnoreCase("GEN"))
				visitList.add(doc.getFilterId());
		 else {
			 if(!genDocsPlaced){
				 genDocs = new ArrayList();
				 genDocs.add(doc);
				 genDocsPlaced = true;
			  }
		 }}
		try {
			Connection con = DataBaseUtil.getConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					"select reg_date, patient_id, d.doctor_name " , "SELECT count(patient_id)", VISIT_DETAILS_TABLES, null , "reg_date", true, 0, 0);
			qb.addFilter(qb.STRING, "patient_id", "IN", visitList);
			qb.build();
			PreparedStatement ps = qb.getDataStatement();
			try (ResultSet rs = ps.executeQuery()) {
  			List<DocHolder> displayDocs = new ArrayList<DocHolder>();
  			String patientId = null;
  			String doctorName = null;
  			java.sql.Date date = null;
  			while(rs.next()) {
  				date = rs.getDate("reg_date");
  				patientId = rs.getString("patient_id");
  				doctorName = rs.getString("doctor_name");
  				for (DocHolder doc : filterListDocs) {
  					if (doc.getFilterId().equalsIgnoreCase(patientId)) {
  						String formatted = patientId;
  						String doctorNameStr = doctorName != null ? " - "+doctorName : "";
  						formatted =  formatter.format(date) + " ("+ patientId + ")"+doctorNameStr;
  						doc.setLabel(formatted);
  						displayDocs.add(doc);
  					}
  				}
  			}
  			if ( null != genDocs ) filteredDocs.add(genDocs);
  			if ( displayDocs.size() > 0 ) filteredDocs.add(displayDocs);
  			qb.close();
  			ps.close();
  			con.close();
			}
		}catch (Exception e) {}
		return filteredDocs;
	}

	private boolean exists(List<DocHolder> filterListDocs, String visitId) {
		for (DocHolder fld : filterListDocs)
			if(fld.getFilterId().equalsIgnoreCase(visitId))
				return true;
		return false;
	}
}