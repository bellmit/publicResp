package com.insta.hms.master.SponsorProcedureMaster;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

@Repository
public class SponsorProcedureRepository extends MasterRepository<Integer> {

	private static String SPONSOR_PROCEDURE_SEARCH_TABLES = " FROM (SELECT spl.procedure_no, spl.tpa_id, spl.procedure_code, "
			+ "	spl.procedure_name, tpa.tpa_name, spl.status, spl.procedure_limit, "
			+ " spl.remarks  FROM sponsor_procedure_limit spl "
			+ " JOIN tpa_master tpa USING (tpa_id)) AS FOO ";

	public SponsorProcedureRepository() {
		super("sponsor_procedure_limit", "procedure_no", "procedure_code", // table, primary key, name field 
				new String[]{"procedure_code", "procedure_name"}); // lookup fields used in auto-completes and dropdowns
	}

	@Override
	public SearchQuery getSearchQuery() {
		return new SearchQuery(SPONSOR_PROCEDURE_SEARCH_TABLES);
	}
}
