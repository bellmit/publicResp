package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.SearchQueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypeFilter implements Filter {
	   static Logger log = LoggerFactory.getLogger(TypeFilter.class);
	public List applyFilter(List<EMRDoc> allDocs, String criteria) {

		List filteredDocs = new ArrayList();
		Collections.sort(allDocs, Collections.reverseOrder(new InstaComparator()));
		List<DocHolder> filterListDocs = new ArrayList<DocHolder>();
		List<EMRDoc> list = null;

		for (EMRDoc doc : allDocs) {
			String type = doc.getType();
			Object[] filterListDocArray = filterListDocs.toArray();
			DocHolder docHolderObj = null;

			if(!exists(filterListDocs,type)) {
				list = new ArrayList<EMRDoc>();
				docHolderObj = new DocHolder();
				docHolderObj.setFilterId(type);
				docHolderObj.setViewDocs(list);
				list.add(doc);
				filterListDocs.add(docHolderObj);
			}else {
				for(Object fld : filterListDocArray) {
					DocHolder dd =	(DocHolder)fld;
					if(dd.getFilterId().equals(type)) dd.getViewDocs().add(doc);
				}
			}
		}

		List typeList = new ArrayList();
		for (DocHolder doc : filterListDocs) typeList.add(doc.getFilterId());
		List<DocHolder> displayDocs = new ArrayList<DocHolder>();

		try {
			String docType = null;
			String docTypeName = null;
			Connection con = DataBaseUtil.getConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					"select doc_type_name, doc_type_id " , "SELECT count(doc_type_name)", " from doc_type ", null , null, false, 0, 0);
			qb.addFilter(qb.STRING, "doc_type_id", "IN", typeList);
			qb.build();

			PreparedStatement ps = qb.getDataStatement();
			try (ResultSet rs = ps.executeQuery()) {
  			while(rs.next()) {
  				docType = rs.getString("doc_type_id");
  				docTypeName = rs.getString("doc_type_name");
  				for (DocHolder doc : filterListDocs) {
  					if (doc.getFilterId() != null && doc.getFilterId().equalsIgnoreCase(docType)) {
  						String formatted = doc.getFilterId();
  						if (docType != null && !docType.isEmpty()) {
  							formatted = docTypeName;
  						}
  						doc.setLabel(formatted);
  						displayDocs.add(doc);
  					}
  				}
  			}
			}

			if ( displayDocs.size() > 0 ) filteredDocs.add(displayDocs);
			qb.close(); ps.close();con.close();
		} catch (SQLException e) { //ignored
			throw new RuntimeException(e);
		}
		return filteredDocs;
	}

	private boolean exists(List<DocHolder> filterListDocs, String type) {
		for (DocHolder fld : filterListDocs)
			if(fld.getFilterId().equalsIgnoreCase(type))
				return true;
		return false;
	}
}
