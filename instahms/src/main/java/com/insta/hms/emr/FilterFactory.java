package com.insta.hms.emr;

public class FilterFactory {

	private static Filter filter = null;
			public static Filter getFilter(String filterType){

			if (filterType.equals("visits")) {
				filter = new VisitFilter();
			}else if (filterType.equals("date")) {
				filter = new DateFilter();
			}else if (filterType.equals("docType")) {
				filter = new TypeFilter();
			}else if(filterType.equals("patientDocType")) {
				filter = new PatientDocTypeFilter();
			}
			return filter;
		}
	 }