package com.insta.hms.emr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateFilter implements Filter {

	public List applyFilter(List<EMRDoc> allDocs, String criteria) {
		List<Map> filteredDocs = new ArrayList<Map>();

		String globalDocDate = "";
		Map<String, List> filterMapDocs = new HashMap<String, List>();
		List<EMRDoc> test = new ArrayList<EMRDoc>();

		Collections.sort(allDocs, new InstaComparator());

		for (int k = 0; k < allDocs.size(); k++) {
			EMRDoc emrdoc = (EMRDoc) allDocs.get(k);
			test.add(emrdoc);

			if (test.size() > 0)
				filterMapDocs.put(globalDocDate, test);
		}
		if (filterMapDocs.size() > 0)
			filteredDocs.add(filterMapDocs);

		return filteredDocs;
	}

}
