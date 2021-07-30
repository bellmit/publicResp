/**
 *
 */
package com.insta.hms.master.PrescriptionsMaster;

/**
 * @author krishna.t
 *
 */
public enum Master {

	Medicine ("prescribed_medicines_master", "Medicine", "medicine_name"),
	Test ("prescribed_tests_master", "Test", "test_name"),
	Service ("prescribed_services_master", "Service", "service_name"),
	;

	String table = null;
	String type = null;
	String secondarySort = null;
	private Master(String table, String type, String secondarySort) {
		this.table = table;
		this.type = type;
		this.secondarySort = secondarySort;
	}

	public String getTable() {
		return table;
	}

	public String getType() {
		return type;
	}
	public String getSecondarySort() {
		return secondarySort;
	}
}
