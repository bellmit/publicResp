package com.insta.hms.stores;

import java.util.List;

public class StoreLevelDTO {


	private String deptId;
	private String medicineId;
	private int itemId;
	private float minmumLevel;
	private float maximumLevel;
	private float reorderLevel;
	private float dangerLevel;
	private String updateDeptId;
	private String deleteDeptId;
	private String deptName;
	private String bin;

	private List storeWiseList;


	public float getDangerLevel() {return dangerLevel;}
	public void setDangerLevel(float dangerLevel) {this.dangerLevel = dangerLevel;}

	public String getDeleteDeptId() {return deleteDeptId;}
	public void setDeleteDeptId(String deleteDeptId) {this.deleteDeptId = deleteDeptId;}

	public String getDeptId() {return deptId;}
	public void setDeptId(String deptId) {this.deptId = deptId;}

	public float getMaximumLevel() {return maximumLevel;}
	public void setMaximumLevel(float maximumLevel) {this.maximumLevel = maximumLevel;}

	public String getMedicineId() {return medicineId;}
	public void setMedicineId(String medicineId) {this.medicineId = medicineId;}

	public float getMinmumLevel() {return minmumLevel;}
	public void setMinmumLevel(float minmumLevel) {this.minmumLevel = minmumLevel;}

	public float getReorderLevel() {return reorderLevel;}
	public void setReorderLevel(float reorderLevel) {this.reorderLevel = reorderLevel;}


	public String getUpdateDeptId() {return updateDeptId;}
	public void setUpdateDeptId(String updateDeptId) {this.updateDeptId = updateDeptId;}

	public String getDeptName() { return deptName; }
	public void setDeptName(String deptName) { this.deptName = deptName; }
	public List getStoreWiseList() {
		return storeWiseList;
	}
	public void setStoreWiseList(List storeWiseList) {
		this.storeWiseList = storeWiseList;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public String getBin() {
		return bin;
	}
	public void setBin(String bin) {
		this.bin = bin;
	}
	

}
