package com.insta.hms.stores;

public class PharmacyMasterDTO {

	// category master variables

	private String categoryName;

	// Medicine master variabels

	private String searchMedName;
	private String medicineId;
	private String searchManfName;
	private String searchGenName;
	private String searchMedCatName;
	private float issuPerBaseUnit;

	private String medicineName;
	private String medShortName;
	private String manufacturerName;
	private String genericName;
	private String composition;
	private String therapaticUse;
	private String pharmaItem;
	private String packageType;
	private String issueUnits;
	private String hdrugStatus;
	private String drugst;
	private float consumptionCapacity;
	private String consumptionUom;

	private int categoryId;
	private String operation;
	private String status;
	private boolean claimable;

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public String getDrugst() {
		return drugst;
	}

	public void setDrugst(String drugst) {
		this.drugst = drugst;
	}

	public String getGenericName() {
		return genericName;
	}

	public void setGenericName(String genericName) {
		this.genericName = genericName;
	}

	public String getHDrugStatus() {
		return hdrugStatus;
	}

	public void setHDrugStatus(String drugStatus) {
		hdrugStatus = drugStatus;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	public String getMedShortName() {
		return medShortName;
	}

	public void setMedShortName(String medShortName) {
		this.medShortName = medShortName;
	}

	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	public String getPharmaItem() {
		return pharmaItem;
	}

	public void setPharmaItem(String pharmaItem) {
		this.pharmaItem = pharmaItem;
	}

	public String getTherapaticUse() {
		return therapaticUse;
	}

	public void setTherapaticUse(String therapaticUse) {
		this.therapaticUse = therapaticUse;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public float getIssuPerBaseUnit() {
		return issuPerBaseUnit;
	}

	public void setIssuPerBaseUnit(float issuPerBaseUnit) {
		this.issuPerBaseUnit = issuPerBaseUnit;
	}

	public String getSearchGenName() {
		return searchGenName;
	}

	public void setSearchGenName(String searchGenName) {
		this.searchGenName = searchGenName;
	}

	public String getSearchManfName() {
		return searchManfName;
	}

	public void setSearchManfName(String searchManfName) {
		this.searchManfName = searchManfName;
	}

	public String getSearchMedCatName() {
		return searchMedCatName;
	}

	public void setSearchMedCatName(String searchMedCatName) {
		this.searchMedCatName = searchMedCatName;
	}

	public String getSearchMedName() {
		return searchMedName;
	}

	public void setSearchMedName(String searchMedName) {
		this.searchMedName = searchMedName;
	}

	public String getMedicineId() {
		return medicineId;
	}

	public void setMedicineId(String medicineId) {
		this.medicineId = medicineId;
	}

	public boolean getClaimable() {
		return claimable;
	}

	public void setClaimable(boolean claimable) {
		this.claimable = claimable;
	}

	public String getIssueUnits() {
		return issueUnits;
	}

	public void setIssueUnits(String issueUnits) {
		this.issueUnits = issueUnits;
	}

	public float getConsumptionCapacity() {
		return consumptionCapacity;
	}

	public void setConsumptionCapacity(float consumptionCapacity) {
		this.consumptionCapacity = consumptionCapacity;
	}

	public String getConsumptionUom() {
		return consumptionUom;
	}

	public void setConsumptionUom(String consumptionUom) {
		this.consumptionUom = consumptionUom;
	}

}
