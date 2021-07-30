package com.insta.hms.diagnosticsmasters;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestandResults {
	private Test test;
	private List <Result> result;
	private TestTemplate template;
	private String prescribedId;
	private String prescribedDoctor;
	private String condctionStatus;
	private String conductingDoctor;
	private String isTemplatethere;
	private List <Reagent> reagent;
	private BasicDynaBean impressionDetails;
	private BasicDynaBean microDetails;
	private BasicDynaBean cytoDetails;
	private List<BasicDynaBean > antibioticDetails;
	private boolean hasDocuments;
	private List<MicroOrgDetails> microOrgDetails;
	private BasicDynaBean reportFormatDetails;
	private List<TestTemplate> amendedTemplates;
	private String imageUploaded;
	private String revisionNumber;
	private String mandate_additional_info;

	public String getMandate_additional_info() {
		return mandate_additional_info;
	}
	public void setMandate_additional_info(String mandate_additional_info) {
		this.mandate_additional_info = mandate_additional_info;
	}
	public String getRevisionNumber() {
		return revisionNumber;
	}
	public void setRevisionNumber(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
	private List <TestTemplate> newTemplates;
	private boolean reagentsexist;

	public TestTemplate getTemplate() {
		return template;
	}
	public void setTemplate(TestTemplate template) {
		this.template = template;
	}
	public List<TestTemplate> getNewTemplates() {
		return newTemplates;
	}
	public void setNewTemplates(List<TestTemplate> newTemplates) {
		this.newTemplates = newTemplates;
	}
	public String getConductingDoctor() {
		return conductingDoctor;
	}
	public void setConductingDoctor(String conductingDoctor) {
		this.conductingDoctor = conductingDoctor;
	}
	public List<Result> getResult() {
		return result;
	}
	public void setResult(List<Result> result) {
		this.result = result;
	}
	public Test getTest() {
		return test;
	}
	public void setTest(Test test) {
		this.test = test;
	}
	public TestandResults(Test test, List<Result> result,
			List <Reagent> reagent,BasicDynaBean reportDetails) {
		super();
		this.test = test;
		this.result = result;
		this.reagent = reagent;
		this.reportFormatDetails = reportDetails;
	}
	public String getCondctionStatus() {
		return condctionStatus;
	}
	public void setCondctionStatus(String condctionStatus) {
		this.condctionStatus = condctionStatus;
	}

	public String getPrescribedDoctor() {
		return prescribedDoctor;
	}
	public void setPrescribedDoctor(String prescribedDoctor) {
		this.prescribedDoctor = prescribedDoctor;
	}
	public String getPrescribedId() {
		return prescribedId;
	}
	public void setPrescribedId(String prescribedId) {
		this.prescribedId = prescribedId;
	}
	public List<Reagent> getReagent() {
		return reagent;
	}
	public void setReagent(List<Reagent> reagent) {
		this.reagent = reagent;
	}
	public String getIsTemplatethere() {
		return isTemplatethere;
	}
	public void setIsTemplatethere(String isTemplatethere) {
		this.isTemplatethere = isTemplatethere;
	}

	public boolean getreagentsexist() {
		return reagentsexist;
	}

	public void setreagentsexist(boolean reagent_exist ) {
		reagentsexist = reagent_exist;
	}
	public BasicDynaBean getImpressionDetails() {
		return impressionDetails;
	}
	public void setImpressionDetails(BasicDynaBean impressionDetails) {
		this.impressionDetails = impressionDetails;
	}
	public List<BasicDynaBean> getAntibioticDetails() {
		return antibioticDetails;
	}
	public void setAntibioticDetails(List<BasicDynaBean> antibioticDetails) {
		this.antibioticDetails = antibioticDetails;
	}
	public BasicDynaBean getMicroDetails() {
		return microDetails;
	}
	public void setMicroDetails(BasicDynaBean microDetails) {
		this.microDetails = microDetails;
	}
	public BasicDynaBean getCytoDetails() {
		return cytoDetails;
	}
	public void setCytoDetails(BasicDynaBean cytoDetails) {
		this.cytoDetails = cytoDetails;
	}
	public boolean isHasDocuments() {
		return hasDocuments;
	}
	public void setHasDocuments(boolean hasDocuments) {
		this.hasDocuments = hasDocuments;
	}

	public static class MicroOrgDetails{
		private int organism_id;
		private String organism_name;
		private int org_group_id;
		private String org_group_name;
		private int abst_panel_id;
		private String abst_panel_name;
		private List<BasicDynaBean> antibioticDetails;
		public int getAbst_panel_id() {
			return abst_panel_id;
		}
		public void setAbst_panel_id(int abst_panel_id) {
			this.abst_panel_id = abst_panel_id;
		}
		public String getAbst_panel_name() {
			return abst_panel_name;
		}
		public void setAbst_panel_name(String abst_panel_name) {
			this.abst_panel_name = abst_panel_name;
		}
		public List<BasicDynaBean> getAntibioticDetails() {
			return antibioticDetails;
		}
		public void setAntibioticDetails(List<BasicDynaBean> antibioticDetails) {
			this.antibioticDetails = antibioticDetails;
		}
		public int getOrg_group_id() {
			return org_group_id;
		}
		public void setOrg_group_id(int org_group_id) {
			this.org_group_id = org_group_id;
		}
		public String getOrg_group_name() {
			return org_group_name;
		}
		public void setOrg_group_name(String org_group_name) {
			this.org_group_name = org_group_name;
		}
		public int getOrganism_id() {
			return organism_id;
		}
		public void setOrganism_id(int organism_id) {
			this.organism_id = organism_id;
		}
		public String getOrganism_name() {
			return organism_name;
		}
		public void setOrganism_name(String organism_name) {
			this.organism_name = organism_name;
		}
	}

	public List<MicroOrgDetails> getMicroOrgDetails() {
		return microOrgDetails;
	}
	public void setMicroOrgDetails(List<MicroOrgDetails> microOrgDetails) {
		this.microOrgDetails = microOrgDetails;
	}
	public BasicDynaBean getReportFormatDetails() {
		return reportFormatDetails;
	}
	public void setReportFormatDetails(BasicDynaBean reportFormatDetails) {
		this.reportFormatDetails = reportFormatDetails;
	}
	public List<TestTemplate> getAmendedTemplates() {
		return amendedTemplates;
	}
	public void setAmendedTemplates(List<TestTemplate> amendedTemplates) {
		this.amendedTemplates = amendedTemplates;
	}
	public String getImageUploaded() {
		return imageUploaded;
	}
	public void setImageUploaded(String imageUploaded) {
		this.imageUploaded = imageUploaded;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("prescribedId", this.prescribedId);
		map.put("prescribedDoctor", this.prescribedDoctor);
		map.put("condctionStatus", this.condctionStatus);
		map.put("conductingDoctor", this.conductingDoctor);
		map.put("isTemplatethere", this.isTemplatethere);
		map.put("hasDocuments", this.hasDocuments);
		map.put("imageUploaded", this.imageUploaded);
		map.put("revisionNumber", this.revisionNumber);
		map.put("mandate_additional_info", this.mandate_additional_info);
		map.put("test", this.test.toMap());
		return map;
	}

}
