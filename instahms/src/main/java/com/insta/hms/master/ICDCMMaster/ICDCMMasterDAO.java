/**
 *
 */
package com.insta.hms.master.ICDCMMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class ICDCMMasterDAO extends GenericDAO{

	public ICDCMMasterDAO() {
		super("mrd_icdcodes_cm");
	}

	private static final String ICD_FIELDS = "SELECT icd_code, icd_description, status ";
	private static final String ICD_COUNT = "SELECT count(icd_code) ";
	private static final String ICD_TABLES = " FROM mrd_icdcodes_cm ";

	public static PagedList searchICDList(Map filter, Map listing) throws SQLException, ParseException {
		Connection con = null;
		PagedList l = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String sortField = (String)listing.get(LISTING.SORTCOL);
			boolean sortReverse = (Boolean)listing.get(LISTING.SORTASC);
			int pageSize = (Integer)listing.get(LISTING.PAGESIZE);
			int pageNum = (Integer)listing.get(LISTING.PAGENUM);

			SearchQueryBuilder qb = new SearchQueryBuilder(con, ICD_FIELDS,
					ICD_COUNT, ICD_TABLES, null, sortField, sortReverse, pageSize, pageNum);
			qb.addFilter(qb.STRING, "icd_code",	"ilike", filter.get("icd_code"));
			qb.addFilter(qb.STRING, "icd_description","ilike", filter.get("icd_description"));
			qb.addFilter(qb.STRING, "status", "IN", filter.get("status"));
			qb.build();
			l = qb.getMappedPagedList();
			qb.close();
		} finally {
			con.close();
		}
		return l;
	}
}
