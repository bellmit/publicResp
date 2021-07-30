package com.insta.hms.emr;

import java.util.List;

/**
 * Interface to a document filter. The implementor of this interface need to
 * provide a filtered listing of documents as specified in the filter.
 *
 */
public interface Filter {

	/**
	 * Filters the list of documents provided using the filter criteria.
	 *
	 * @param allDocs
	 * @param criteria
	 * @return
	 */
	public List applyFilter(List<EMRDoc> allDocs, String criteria);
}
