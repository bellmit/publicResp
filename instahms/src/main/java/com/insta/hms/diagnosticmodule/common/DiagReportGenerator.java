package com.insta.hms.diagnosticmodule.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.laboratory.TestVisitReportSignaturesDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class to generate a HTML report for all the prescribed tests for a given
 * report ID. A "nice" way of doing this is to call the JSP engine and supply
 * it a path to a JSP, but that requires setting up of requestContext, parameters,
 * response and all that ... since our requirement is a simple table we just build
 * the html string using java itself.
 *
 * Note that since this generated html can be edited from TinyMCE, we avoid using
 * css altogether, and use only in-line styles.
 */
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

/*
 * TODO:
 *  - Put the entire PatientDetails result into demography, no need for making Demography class (done)
 *  - Use the Map version of PatientDetails rather than the DTO version (done)
 *  - Initial StringBuilder is no longer required - (done)
 *  - Need other doctors (need changes to queries) - (done)
 *  - Need sample number (query may change) - (done)
 *  - If possible, reorganize the code to use ConversionUtils hierarchy builders.
 */
public class DiagReportGenerator {

    static Logger log = LoggerFactory.getLogger(DiagReportGenerator.class);

	private List<File> tempFiles;
	private ServletContext sc;

	public DiagReportGenerator(ServletContext sc) {
		this.sc = sc;
		tempFiles = new ArrayList();
	}

	public static class DeptTest {
		public String deptName;
		public List<ValueTest> valueTests;
		public List<FormatTest> reportTests;
		public List<HistoTest> histoTests;
		public List<MicroTest > microTests;
		public List<CytoTests > cytoTests;
		public boolean hasRemarks = false;
		public List<MicroTest> getMicroTests() {
			return microTests;
		}
		public void setMicroTests(List<MicroTest> microTests) {
			this.microTests = microTests;
		}
		public DeptTest(String name) {
			deptName = name;
			valueTests = new ArrayList<ValueTest>();
			reportTests = new ArrayList<FormatTest>();
			histoTests = new ArrayList<HistoTest>();
			microTests = new ArrayList<MicroTest>();
			cytoTests = new ArrayList<CytoTests>();
		}
		public String getDeptName() {return deptName;}
		public void setDeptName(String deptName) {this.deptName = deptName;}

		public boolean isHasRemarks() {return hasRemarks;}
		public void setHasRemarks(boolean hasRemarks) {this.hasRemarks = hasRemarks;}

		public List<FormatTest> getReportTests() {return reportTests;}
		public void setReportTests(List<FormatTest> reportTests) {this.reportTests = reportTests;}

		public List<ValueTest> getValueTests() {return valueTests;}
		public void setValueTests(List<ValueTest> valueTests) {this.valueTests = valueTests;}
		public List<HistoTest> getHistoTests() {
			return histoTests;
		}
		public void setHistoTests(List<HistoTest> histoTests) {
			this.histoTests = histoTests;
		}
		public List<CytoTests> getCytoTests() {
			return cytoTests;
		}
		public void setCytoTests(List<CytoTests> cytoTests) {
			this.cytoTests = cytoTests;
		}
	}

	public static class Test {
		public String testName;
		public String testId;
		public String labNumber;
		public String specimenType;
		public Date testDate;
		public Date sampleDate;
		public Timestamp assertionTime;
		public Timestamp prescDateTime;
		public String sampleSource;
		public String reconductionReason;
		public String packageName;
		public String packageDescription;
		public String sampleInstructions;
		public String techSignatureName;
		public String condDoctorId;
		public String condDoctorName;
		public String serviceSubGroupName;
		public int serviceSubGroupID;
		public int displayOrderPackage;
		public String bedName;
		public String wardName;
		public String completed_by;
		public String validated_by;
		
		public String getWardName() {
			return wardName;
		}
		public void setWardName(String wardName) {
			this.wardName = wardName;
		}
		
		public String getTestId() {
			return testId;
		}
		public void setTestId(String testId) {
			this.testId = testId;
		}

		public String getBedName() {
			return bedName;
		}
		public void setBedName(String bedName) {
			this.bedName = bedName;
		}
		public String getSampleInstructions() {
			return sampleInstructions;
		}
		public void setSampleInstructions(String sampleInstructions) {
			this.sampleInstructions = sampleInstructions;
		}
		public String getPackageDescription() {
			return packageDescription;
		}
		public void setPackageDescription(String packageDescription) {
			this.packageDescription = packageDescription;
		}
		public String getPackageName() {
			return packageName;
		}
		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}
		public String getReconductionReason() {
			return reconductionReason;
		}
		public void setReconductionReason(String reconductionReason) {
			this.reconductionReason = reconductionReason;
		}
		public String getSampleSource() {
			return sampleSource;
		}
		public void setSampleSource(String sampleSource) {
			this.sampleSource = sampleSource;
		}
		public String getLabNumber() {return labNumber;}
		public void setLabNumber(String labNumber) {this.labNumber = labNumber;}

		public Date getSampleDate() {return sampleDate;}
		public void setSampleDate(Date sampleDate) {this.sampleDate = sampleDate;}

		public String getSpecimenType() {return specimenType;}
		public void setSpecimenType(String specimenType) {this.specimenType = specimenType;}

		public Date getTestDate() {return testDate;}
		public void setTestDate(Date testDate) {this.testDate = testDate;}

		public String getTestName() {return testName;}
		public void setTestName(String testName) {this.testName = testName;}

		public String getTechSignatureName() {
			return techSignatureName;
		}
		public void setTechSignatureName(String techSignatureName) {
			this.techSignatureName = techSignatureName;
		}
		public String getCondDoctorName() {
			return condDoctorName;
		}
		public void setCondDoctorName(String condDoctorName) {
			this.condDoctorName = condDoctorName;
		}

		public void setCondDoctorId(String condDoctorId) {
			this.condDoctorId = condDoctorId;
		}

		public String getCondDoctorId(){
			return condDoctorId;
		}
		public Timestamp getAssertionTime() {
			return assertionTime;
		}
		public void setAssertionTime(Timestamp assertionTime) {
			this.assertionTime = assertionTime;
		}
		public Timestamp getPrescDateTime() {
			return prescDateTime;
		}
		public void setPrescDateTime(Timestamp prescDateTime) {
			this.prescDateTime = prescDateTime;
		}
		public String getServiceSubGroupName() {
			return serviceSubGroupName;
		}
		public void setServiceSubGroupName(String serviceSubGroupName) {
			this.serviceSubGroupName = serviceSubGroupName;
		}
		public int getServiceSubGroupID() {
			return serviceSubGroupID;
		}
		public void setServiceSubGroupID(int serviceSubGroupID) {
			this.serviceSubGroupID = serviceSubGroupID;
		}
		public int getDisplayOrderPackage() {
			return displayOrderPackage;
		}
		public void setDisplayOrderPackage(int displayOrderPackage) {
			this.displayOrderPackage = displayOrderPackage;
		}
		public String getCompleted_by() {
			return completed_by;
		}
		public void setCompleted_by(String completed_by) {
			this.completed_by = completed_by;
		}
		public String getValidated_by() {
			return validated_by;
		}
		public void setValidated_by(String validated_by) {
			this.validated_by = validated_by;
		}

	}

	public static class ValueTest extends Test {
		public String testRemarks;
		public List<BasicDynaBean> results;
		public String testConduction;
		public String orderedDate;
		public String sampleId;

		public String getOrderedDate() {
			return orderedDate;
		}

		public void setOrderedDate(String orderedDate) {
			this.orderedDate = orderedDate;
		}

		public String getSampleId() {
			return sampleId;
		}

		public void setSampleId(String sampleId) {
			this.sampleId = sampleId;
		}

		public ValueTest(String name) {
			testName = name;
			results = new ArrayList<BasicDynaBean>();
		}

		public String getTestConduction() { return testConduction;	}
		public void setTestConduction(String testConduction) {	this.testConduction = testConduction; }

		public List<BasicDynaBean> getResults() {return results;}
		public void setResults(List<BasicDynaBean> results) {this.results = results;}

		public String getTestRemarks() {return testRemarks;}
		public void setTestRemarks(String testRemarks) {this.testRemarks = testRemarks;}

	}

	public static class FormatTest extends Test {
		public BasicDynaBean result;
		public String orderedDate;
		public String sampleId;
		public String amendmentReason;
		public String testRemarks;

		public String testConduction;
		public FormatTest(String name) {
			testName = name;
		}

		public String getTestConduction() { return testConduction;	}
		public void setTestConduction(String testConduction) {	this.testConduction = testConduction; }

		public BasicDynaBean getResult() {return result;}
		public void setResult(BasicDynaBean result) {this.result = result;}

		public String getOrderedDate() {
			return orderedDate;
		}

		public void setOrderedDate(String orderedDate) {
			this.orderedDate = orderedDate;
		}

		public String getSampleId() {
			return sampleId;
		}

		public void setSampleId(String sampleId) {
			this.sampleId = sampleId;
		}

		public String getAmendmentReason() {
			return amendmentReason;
		}

		public void setAmendmentReason(String amendmentReason) {
			this.amendmentReason = amendmentReason;
		}

		public String getTestRemarks() {
			return testRemarks;
		}

		public void setTestRemarks(String testRemarks) {
			this.testRemarks = testRemarks;
		}

	}

	public static class HistoTest extends Test{

		public HistoTest(String testName){
			this.testName = testName;
		}
		public String short_impression;
		public String impression_details;
		public int no_of_blocks;
		public String block_description;
		public int no_of_slides;
		public String slide_description;
		public String clinical_details;
		public String micro_gross_details;
		public String testConduction;
		public String testRemarks;
		public int histo_id;
		public String getTestConduction() {
			return testConduction;
		}
		public void setTestConduction(String testConduction) {
			this.testConduction = testConduction;
		}
		public String getTestRemarks() {
			return testRemarks;
		}
		public void setTestRemarks(String testRemarks) {
			this.testRemarks = testRemarks;
		}
		public String getBlock_description() {
			return block_description;
		}
		public void setBlock_description(String block_description) {
			this.block_description = block_description;
		}
		public String getClinical_details() {
			return clinical_details;
		}
		public void setClinical_details(String clinical_details) {
			this.clinical_details = clinical_details;
		}
		public String getImpression_details() {
			return impression_details;
		}
		public void setImpression_details(String impression_details) {
			this.impression_details = impression_details;
		}
		public String getMicro_gross_details() {
			return micro_gross_details;
		}
		public void setMicro_gross_details(String micro_gross_details) {
			this.micro_gross_details = micro_gross_details;
		}
		public int getNo_of_blocks() {
			return no_of_blocks;
		}
		public void setNo_of_blocks(int no_of_blocks) {
			this.no_of_blocks = no_of_blocks;
		}
		public int getNo_of_slides() {
			return no_of_slides;
		}
		public void setNo_of_slides(int no_of_slides) {
			this.no_of_slides = no_of_slides;
		}
		public String getShort_impression() {
			return short_impression;
		}
		public void setShort_impression(String short_impression) {
			this.short_impression = short_impression;
		}
		public String getSlide_description() {
			return slide_description;
		}
		public void setSlide_description(String slide_description) {
			this.slide_description = slide_description;
		}
		public int getHisto_id() {
			return histo_id;
		}
		public void setHisto_id(int histo_id) {
			this.histo_id = histo_id;
		}

	}

	public static class MicroTest extends Test{
		public MicroTest(String testName){
			this.testName = testName;
		}

		public boolean growth_exists;
		public String nogrowth_template_name;
		public int colony_count;
		public String nogrowth_report_comment;
		public String testConduction;
		public String testRemarks;
		public List<MicroOrgDetails > orgGrpDetails;
		public String microscopic_details;
		public String growth_report_comment;

		public String getGrowth_report_comment() {
			return growth_report_comment;
		}
		public void setGrowth_report_comment(String growth_report_comment) {
			this.growth_report_comment = growth_report_comment;
		}
		public String getMicroscopic_details() {
			return microscopic_details;
		}
		public void setMicroscopic_details(String microscopic_details) {
			this.microscopic_details = microscopic_details;
		}
		public String getTestConduction() {
			return testConduction;
		}
		public void setTestConduction(String testConduction) {
			this.testConduction = testConduction;
		}
		public String getTestRemarks() {
			return testRemarks;
		}
		public void setTestRemarks(String testRemarks) {
			this.testRemarks = testRemarks;
		}
		public int getColony_count() {
			return colony_count;
		}
		public void setColony_count(int colony_count) {
			this.colony_count = colony_count;
		}
		public boolean isGrowth_exists() {
			return growth_exists;
		}
		public void setGrowth_exists(boolean growth_exists) {
			this.growth_exists = growth_exists;
		}
		public String getNogrowth_report_comment() {
			return nogrowth_report_comment;
		}
		public void setNogrowth_report_comment(String nogrowth_report_comment) {
			this.nogrowth_report_comment = nogrowth_report_comment;
		}
		public String getNogrowth_template_name() {
			return nogrowth_template_name;
		}
		public void setNogrowth_template_name(String nogrowth_template_name) {
			this.nogrowth_template_name = nogrowth_template_name;
		}
		public List<MicroOrgDetails> getOrgGrpDetails() {
			return orgGrpDetails;
		}
		public void setOrgGrpDetails(List<MicroOrgDetails> orgGrpDetails) {
			this.orgGrpDetails = orgGrpDetails;
		}

	}

	public static class MicroOrgDetails{
		public String org_group_name;
		public String organism_name;
		public String abst_panel_name;
		public String resistance_marker;
		public String comments;

		public List<BasicDynaBean> antibitics;

		public List<BasicDynaBean> getAntibitics() {
			return antibitics;
		}
		public void setAntibitics(List<BasicDynaBean> antibitics) {
			this.antibitics = antibitics;
		}
		public String getAbst_panel_name() {
			return abst_panel_name;
		}
		public void setAbst_panel_name(String abst_panel_name) {
			this.abst_panel_name = abst_panel_name;
		}
		public String getComments() {
			return comments;
		}
		public void setComments(String comments) {
			this.comments = comments;
		}
		public String getOrg_group_name() {
			return org_group_name;
		}
		public void setOrg_group_name(String org_group_name) {
			this.org_group_name = org_group_name;
		}
		public String getOrganism_name() {
			return organism_name;
		}
		public void setOrganism_name(String organism_name) {
			this.organism_name = organism_name;
		}
		public String getResistance_marker() {
			return resistance_marker;
		}
		public void setResistance_marker(String resistance_marker) {
			this.resistance_marker = resistance_marker;
		}
	}

	public static class AntiBiotics {
		public String antibiotic_name;
		public String anti_results;
		public String suspectability;
		public int prescribed_id;
		public String getAnti_results() {
			return anti_results;
		}
		public void setAnti_results(String anti_results) {
			this.anti_results = anti_results;
		}
		public String getAntibiotic_name() {
			return antibiotic_name;
		}
		public void setAntibiotic_name(String antibiotic_name) {
			this.antibiotic_name = antibiotic_name;
		}
		public int getPrescribed_id() {
			return prescribed_id;
		}
		public void setPrescribed_id(int prescribed_id) {
			this.prescribed_id = prescribed_id;
		}
		public String getSuspectability() {
			return suspectability;
		}
		public void setSuspectability(String suspectability) {
			this.suspectability = suspectability;
		}


	}


	public static class CytoTests extends Test{
		public CytoTests(String testName){
			this.testName = testName;
		}
		public String test_type;
		public String speciman_adequecy;
		public String smear_received;
		public String cyto_short_impression;
		public String cyto_microscopic_details;
		public String cyto_impression_details;
		public String cyto_clinical_details;

		public String getCyto_microscopic_details() {
			return cyto_microscopic_details;
		}
		public void setCyto_microscopic_details(String cyto_microscopic_details) {
			this.cyto_microscopic_details = cyto_microscopic_details;
		}
		public String getSmear_received() {
			return smear_received;
		}
		public void setSmear_received(String smear_received) {
			this.smear_received = smear_received;
		}
		public String getSpeciman_adequecy() {
			return speciman_adequecy;
		}
		public void setSpeciman_adequecy(String speciman_adequecy) {
			this.speciman_adequecy = speciman_adequecy;
		}
		public String getTest_type() {
			return test_type;
		}
		public void setTest_type(String test_type) {
			this.test_type = test_type;
		}
		public String getCyto_impression_details() {
			return cyto_impression_details;
		}
		public void setCyto_impression_details(String cyto_impression_details) {
			this.cyto_impression_details = cyto_impression_details;
		}
		public String getCyto_short_impression() {
			return cyto_short_impression;
		}
		public void setCyto_short_impression(String cyto_short_impression) {
			this.cyto_short_impression = cyto_short_impression;
		}
		public String getCyto_clinical_details() {
			return cyto_clinical_details;
		}
		public void setCyto_clinical_details(String cyto_clinical_details) {
			this.cyto_clinical_details = cyto_clinical_details;
		}


	}

	public static String getReport(int reportId,String userName,String cPath, boolean WebBasedReport,
			boolean apiBased) throws SQLException, ParseException,IOException,Exception {
		return getReport(reportId, userName, cPath, WebBasedReport, apiBased, null);
	}

	/*
	 *  wraps only the report content(i.e., patientdetails and test details without header and footer).
	 *  header and footer now will not be a part of actual content.
	 *
	 *  they are appended while taking print(according to printer settings).
	 */
	public static String getReport(int reportId,String userName,String cPath, boolean WebBasedReport,
			boolean apiBased, Map data_map) throws SQLException, ParseException,IOException,Exception {

		/*
		 *  the following map ftl params are for Legacy support.
		 *
		 *  	Legacy				Renamed to
		 *  -------------			------------
		 *   1) fullName 		patient_full_name
		 *   2) ageAndSex       sex- patient_gender, age- age, age - ageIn(Y/M/D)
		 *   3) mrNo			mr_no
		 *   4) visitNo			patient_id
		 *   5) diagPrintPrefs  not using in modified version
		 *   6) reportDoctor	prescribing_doctor
		 *   7) doctors			conducting_doctors(list)
		 *   8) imageTag 		not using in modified version
		 *
		 *   once after migration done successfully for user customized print templates, we can
		 *   remove these fields.
		 *
		 *   along with this patient visit details map is available for customization and
		 *   conducting_doctors, doctor name is renamed to prescribing_doctor and consulting_doctor,
		 *   reportSampleNo variables are also available for customization.
		 *
		 */

		/*
		 * get relevant data from the DAOs
		 */
		List values = DiagnosticsDAO.getReportTestValues(reportId,false);
		log.debug("Number of results: " + values.size()+ " report Id: "+reportId);
		List designations = DiagnosticsDAO.getReportDesignations(reportId);
		List conductingDoctors = DiagnosticsDAO.getConductingDoctors(reportId);
		BasicDynaBean report = null;
		String patientId = null;
		Integer centerID = RequestContext.getCenterId();
		Map visit_details_map = null;
		
		if (data_map != null && data_map.get("visit_report_bean") != null)
			report = (BasicDynaBean)data_map.get("visit_report_bean");
		else
			report = DiagnosticsDAO.getReportDynaBean(reportId);
		
		if (data_map != null && data_map.get("hospital_patient_id") != null) {
			patientId = (String)data_map.get("hospital_patient_id");
		} else {
		  patientId = DiagnosticsDAO.getReportPatientId(reportId);
		}
			if ( !apiBased && !WebBasedReport && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1
					&& report != null && report.get("signoff_center") != null) {
				if (report.get("signoff_center").equals(centerID)) {
					//No need to change the patientId
				} else {
					patientId = DiagnosticsDAO.getHospitalPatientID(reportId);				
				}
			}
			//In case of webBasedReport, patient id need to get from collection center.
			if (WebBasedReport || apiBased) {
				patientId = DiagnosticsDAO.getHospitalPatientID(reportId);			
			}
		
		if (data_map != null && data_map.get("visit_details") != null)
			visit_details_map = (Map)data_map.get("visit_details");
		else
			visit_details_map = getPatientDetailsMap(patientId, reportId);
		
		
		BasicDynaBean diagPrintPrefs = null;
		if(WebBasedReport == true || apiBased == true) {
			diagPrintPrefs = PrintConfigurationsDAO.getWebDiagPrintPrefs();
		} else {
		    diagPrintPrefs = PrintConfigurationsDAO.getDiagDefaultPrintPrefs();
		}
		BasicDynaBean originalReportBean = LaboratoryDAO.getOriginalReport(reportId);
		boolean isRadiology = !((String)report.get("category")).equals("DEP_LAB");

		String category = null;
		String idNoLabel = null;
		HashMap<String, Object> root = new HashMap<String,Object>();
		GenericDAO tpDAO = new GenericDAO("tests_prescribed");
		
		/*
		 *Section 2: Patient Details
		 *moved to DiagTemplateFormat.ftl and passing values to placeholders from java
		 */
		if (isRadiology){
			category = "DEP_RAD";
			idNoLabel="Radiology ID: ";
		}else{
			category = "DEP_LAB";
			idNoLabel = "Lab ID No:";
		}

		String addendum = "";
		if ( report.get("report_addendum") != null )
			addendum = (String)report.get("report_addendum");//addendum of the report

		root.put("report_addendum", addendum);
		root.put("idNoLabel", idNoLabel);
		root.put("isRadiology", isRadiology);

		root.put("visitDetails", visit_details_map);
		root.put("currentDate", new Date());
		root.put("diagPrintPrefs", diagPrintPrefs);
		root.put("imageTag", "");
		root.put("report", report);
		root.put("cpath", cPath);

		/*
		 * Get some report values based on the first result that has a non-null value for the
		 * corresponding item. Useful to print on the header when there is only one test/value
		 * in the report, or if there is only a single common value for all tests in the report.
		 */
		String prescribing_doctor = null; boolean singleDoctor = true;String referal = null;
		String reportTestDate = null; boolean singleTestDate = true;String incoming_hosp = null;
		String reportLabNo = null; boolean singleLabNo = true;
		String reportSampleDate = null; boolean singleSampleDate = true;boolean incoming = false;
		String reportSpecimenType = null; boolean singleSpecimenType = true;
		String reportSampleNo = null; boolean singleSampleNo = true;
		boolean singleTechnician = false; String technician = null;
		boolean singleCondDoc = false; String conducting_doctor = null;
		boolean singleAssertionTime = false; String assertionTime = null;
		boolean singlePrescDateTime = false; String prescDateTime = null;
		boolean singleServiceSubGroup = true; String reportServiceSubGroup = null;
		String conducting_doc_id = null;

		List<BasicDynaBean> signatureList = new TestVisitReportSignaturesDAO().findAllByKey("report_id", reportId);
		root.put("signature_params", signatureList);

		/*
		 * Iterate through the result and form a hierarchical structure:
		 * depts: list of DeptTest
		 *  DeptTest: name + List<ValueTest> valueTests + List<BasicDynaBean> reportTests
		 *  ValueTest: name + List<BasicDynaBean> (results)
		 */
		String prevDeptName = "";
		int prevPrescribedId = 0;
		boolean hasAbnormalResults = false;
		ArrayList<DeptTest> depts = new ArrayList<DeptTest>();
		ArrayList<DeptTest> amendedDepts = new ArrayList<DeptTest>();
		ArrayList<DeptTest> reconductedDepts = new ArrayList<DeptTest>();
		DeptTest dept = null;
		ValueTest vt = null;
		boolean allAreValidated = true;
		boolean allAreSignedOff = true;
		DateUtil dateUtil = new DateUtil();

		String labNo=null;
		List<Integer> amendedPrescribedIds = new ArrayList<Integer>();
		List<Integer> reconductedPrescribedIds = new ArrayList<Integer>();
		BasicDynaBean reconductedTest = null;

		Iterator it = values.iterator();
		while (it.hasNext()) {
			BasicDynaBean testResult = (BasicDynaBean) it.next();

			String deptName = (String) testResult.get("ddept_name");
			if (!deptName.equals(prevDeptName)) {
				// add a new department to the dept list
				log.debug("new department: " + deptName);
				dept = new DeptTest(deptName);
				depts.add(dept);
				prevDeptName = deptName;
			}
			int prescribedId = ((Integer) testResult.get("prescribed_id")).intValue();

			//
		
			String technician_db = (String) testResult.get("technician");
			if (technician_db != null && !technician_db.equalsIgnoreCase("")) {
				if (technician == null) {
					technician = technician_db;
					singleTechnician = true;
				} else
					singleTechnician = singleTechnician && technician.equals(technician_db);
			}

			String service_sub_group = (String) testResult.get("service_sub_group_name");
			if(service_sub_group != null && !service_sub_group.equalsIgnoreCase("")) {
				if(reportServiceSubGroup == null) {
					reportServiceSubGroup = service_sub_group;
					singleServiceSubGroup = true;
				} else
					singleServiceSubGroup = singleServiceSubGroup && reportServiceSubGroup.equals(service_sub_group);
			}

			String cond_doc_db = (String) testResult.get("cond_doctor_name");
			if (cond_doc_db != null && !cond_doc_db.equalsIgnoreCase("")) {
				if (conducting_doctor == null) {
					conducting_doctor = cond_doc_db;
					conducting_doc_id = (String) testResult.get("conducted_by");
					singleCondDoc = true;
				} else {
					singleCondDoc = singleCondDoc && conducting_doctor.equals(cond_doc_db);
				}
			}

			String doctor = (String)testResult.get("doctor_name");
			if ((doctor!=null) && !doctor.equalsIgnoreCase("")) {
				if (prescribing_doctor == null)
					prescribing_doctor = doctor;
				else
					singleDoctor = singleDoctor && prescribing_doctor.equals(doctor);
			}

			java.sql.Timestamp testDate = (java.sql.Timestamp)testResult.get("conducted_date");
			if (testDate != null) {
				String testDateStr = DataBaseUtil.timeStampFormatter.format(testDate);
				if (reportTestDate == null)
					reportTestDate = testDateStr;
				else
					singleTestDate = singleTestDate && reportTestDate.equals(testDateStr);
			}

			java.sql.Timestamp sampleDate = (java.sql.Timestamp)testResult.get("sample_date");
			if (sampleDate != null) {
				String sampleDateStr = DataBaseUtil.timeStampFormatter.format(sampleDate);
				if (reportSampleDate == null)
					reportSampleDate = sampleDateStr;
				else
					singleSampleDate = singleSampleDate && reportSampleDate.equals(sampleDateStr);
			}

			String llabNo = (String) testResult.get("labno");
			if (llabNo != null) {
				if (reportLabNo == null)
					reportLabNo = llabNo;
				else
					singleLabNo = singleLabNo && reportLabNo.equals(llabNo);
			}

			String specimenType = (String) testResult.get("sample_type");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSpecimenType == null)
					reportSpecimenType = specimenType;
				else
					singleSpecimenType = singleSpecimenType && reportSpecimenType.equals(specimenType);
			}

			String sampleNo = (String) testResult.get("sample_sno");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSampleNo == null)
					reportSampleNo = sampleNo;
				else
					singleSampleNo = singleSampleNo && reportSampleNo.equals(sampleNo);
			}

			java.sql.Timestamp assertionDateTime = (java.sql.Timestamp)testResult.get("assertion_time");
			if (assertionDateTime != null) {
				String assertionTimeStr = DataBaseUtil.timeStampFormatter.format(assertionDateTime);
				if (assertionTime == null) {
					assertionTime = assertionTimeStr;
					singleAssertionTime = true;
				} else {
					singleAssertionTime = singleAssertionTime && assertionTime.equals(assertionTimeStr);
				}
			}

			java.sql.Timestamp presDateTime = (java.sql.Timestamp)testResult.get("pres_date");
			if (presDateTime != null) {
				String presTimeStr = DataBaseUtil.timeStampFormatter.format(presDateTime);
				if (prescDateTime == null) {
					prescDateTime = presTimeStr;
					singlePrescDateTime = true;
				} else {
					singlePrescDateTime = singlePrescDateTime && prescDateTime.equals(presTimeStr);
				}
			}

			referal = (String)testResult.get("referal_name");
			incoming_hosp = (String)testResult.get("hospital_name");			
			
			String techSignatureName = null;
			String condDoctorId = null;
			for (BasicDynaBean bean : signatureList) {
				if ((Integer) bean.get("prescribed_id") == prescribedId) {
					String techn = (String) testResult.get("technician");
					techn = techn == null ? "" : techn;
					if (techn.equals(bean.get("signatory_username")))
						techSignatureName = (String) bean.get("signatory_username");

					String cond_by = (String) testResult.get("conducted_by");
					cond_by = cond_by == null ? "" : cond_by;
					if (cond_by.equals(bean.get("doctor_id")))
						condDoctorId = (String) bean.get("doctor_id");
				}
			}

			allAreValidated = allAreValidated && techSignatureName != null;
			allAreSignedOff = allAreSignedOff && condDoctorId != null;

			String format = (String) testResult.get("conducted_in_reportformat");
			if (format.equals("N")) {

				if(testResult.get("original_test_details_id") != null
						&& (Integer)testResult.get("original_test_details_id") != 0)
					amendedPrescribedIds.add(prescribedId);//to get amended results

				if((Boolean)testResult.get("re_conduction"))
						reconductedPrescribedIds.add(((BigDecimal)testResult.get("reference_pres")).intValue());

				if (prescribedId != prevPrescribedId) {
					log.debug("new value test: " + prescribedId);
					vt = new ValueTest((String) testResult.get("test_name"));
					vt.setOrderedDate(dateUtil.getTimeStampFormatter().format(testResult.get("pres_date")));
					vt.setSampleId((String)testResult.get("sample_sno"));
					dept.valueTests.add(vt);
					prevPrescribedId = prescribedId;
					labNo=(String) testResult.get("labno");
					if(labNo==null){
						vt.labNumber="-";
					}else{
						vt.labNumber = (String) testResult.get("labno");
					}
					vt.testRemarks = (String) testResult.get("remarks");
					vt.testDate = (Date) testResult.get("conducted_date");
					vt.sampleDate = (Date) testResult.get("sample_date");
					vt.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
					vt.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
					vt.assertionTime = (Timestamp) testResult.get("assertion_time");
					vt.prescDateTime = (Timestamp) testResult.get("pres_date");
					vt.specimenType = (String) testResult.get("sample_type");
					vt.sampleSource = (String)testResult.get("source_name");
					vt.testConduction = (String)testResult.get("conduction_instructions");
					vt.packageName = (String)testResult.get("package_name");
					vt.packageDescription = (String)testResult.get("description");
					vt.sampleInstructions = (String)testResult.get("sample_collection_instructions");
					vt.setCondDoctorName((String) testResult.get("cond_doctor_name"));
					vt.setCondDoctorId(condDoctorId);
					vt.setTechSignatureName(techSignatureName);
					vt.setDisplayOrderPackage(testResult.get("display_order_in_package") != null ? (Integer)testResult.get("display_order_in_package") : 0);
					vt.setBedName((String)testResult.get("bed_name"));
					vt.setWardName((String)testResult.get("ward_name"));
					vt.setCompleted_by((String)testResult.get("completed_by"));
					vt.setValidated_by((String)testResult.get("validated_by"));
					vt.testId = (String)testResult.get("test_id");
				}
				vt.results.add(testResult);
				String remarks = (String) testResult.get("comments");
				if ( (remarks != null) && (!remarks.isEmpty()) ) {
					dept.hasRemarks = true;
				}
				String withinNormal = (String) testResult.get("withinnormal");
				if (!withinNormal.equals("Y")) {
					hasAbnormalResults = true;
				}
			} else if(format.equals("Y")){
				log.debug("new report test: " + testResult.get("test_name"));

				if(testResult.get("original_test_details_id") != null
						&& (Integer)testResult.get("original_test_details_id") != 0)
					amendedPrescribedIds.add((Integer) testResult.get("prescribed_id"));//to get amended results

				if((Boolean)testResult.get("re_conduction")){
					reconductedTest = tpDAO.findByKey("prescribed_id", testResult.get("reference_pres"));

						//test which are reconducted after signoff need to be shown seperately
					if(reconductedTest.get("conducted").equals("RAS"))
						reconductedPrescribedIds.add(((BigDecimal)testResult.get("reference_pres")).intValue());

				}

				FormatTest ft = new FormatTest((String) testResult.get("test_name"));
				ft.setOrderedDate(dateUtil.getTimeStampFormatter().format(testResult.get("pres_date")));
				ft.setSampleId((String)testResult.get("sample_sno"));
				dept.reportTests.add(ft);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					ft.labNumber = "-";
				} else {
					ft.labNumber = (String) testResult.get("labno");
				}
				ft.testDate = (Date) testResult.get("conducted_date");
				ft.sampleDate = (Date) testResult.get("sample_date");
				ft.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
				ft.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
				ft.assertionTime = (Timestamp) testResult.get("assertion_time");
				ft.prescDateTime = (Timestamp) testResult.get("pres_date");
				ft.specimenType = (String) testResult.get("sample_type");
				ft.result = testResult;
				ft.testConduction = (String)testResult.get("conduction_instructions");
				ft.packageName = (String)testResult.get("package_name");
				ft.packageDescription = (String)testResult.get("description");
				ft.sampleInstructions = (String)testResult.get("sample_collection_instructions");
				ft.testRemarks = (String) testResult.get("remarks");
				ft.setCondDoctorName((String) testResult.get("cond_doctor_name"));
				ft.setCondDoctorId(condDoctorId);
				ft.setTechSignatureName(techSignatureName);
				ft.setDisplayOrderPackage(testResult.get("display_order_in_package") != null ? (Integer)testResult.get("display_order_in_package") : 0);
				ft.setBedName((String)testResult.get("bed_name"));
				ft.setWardName((String)testResult.get("ward_name"));
				ft.setCompleted_by((String)testResult.get("completed_by"));
				ft.setValidated_by((String)testResult.get("validated_by"));
				ft.testId = (String)testResult.get("test_id");
			}else if(format.equals("H")){
				HistoTest ht = new HistoTest((String) testResult.get("test_name"));
				dept.histoTests.add(ht);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					ht.labNumber = "-";
				} else {
					ht.labNumber = (String) testResult.get("labno");
				}
				ht.testDate = (Date) testResult.get("conducted_date");
				ht.sampleDate = (Date) testResult.get("sample_date");
				ht.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
				ht.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
				ht.assertionTime = (Timestamp) testResult.get("assertion_time");
				ht.prescDateTime = (Timestamp) testResult.get("pres_date");
				ht.specimenType = (String) testResult.get("sample_type");
				ht.testConduction = (String)testResult.get("conduction_instructions");
				ht.testRemarks = (String) testResult.get("remarks");
				ht.setCondDoctorName((String) testResult.get("cond_doctor_name"));
				ht.setCondDoctorId(condDoctorId);
				ht.setTechSignatureName(techSignatureName);
				ht.setBedName((String)testResult.get("bed_name"));
				ht.setWardName((String)testResult.get("ward_name"));
				ht.setCompleted_by((String)testResult.get("completed_by"));
				ht.setValidated_by((String)testResult.get("validated_by"));
				ht.testId = (String)testResult.get("test_id");
				BeanUtils.copyProperties(ht, testResult);


			}else if(format.equals("M")){
				MicroTest mt = new MicroTest((String) testResult.get("test_name"));
				dept.microTests.add(mt);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					mt.labNumber = "-";
				} else {
					mt.labNumber = (String) testResult.get("labno");
				}
				mt.testDate = (Date) testResult.get("conducted_date");
				mt.sampleDate = (Date) testResult.get("sample_date");
				mt.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
				mt.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
				mt.assertionTime = (Timestamp) testResult.get("assertion_time");
				mt.prescDateTime = (Timestamp) testResult.get("pres_date");
				mt.specimenType = (String) testResult.get("sample_type");
				mt.testConduction = (String)testResult.get("conduction_instructions");
				mt.testRemarks = (String) testResult.get("remarks");
				mt.setBedName((String)testResult.get("bed_name"));
				mt.setWardName((String)testResult.get("ward_name"));
				mt.setCompleted_by((String)testResult.get("completed_by"));
				mt.setValidated_by((String)testResult.get("validated_by"));
				mt.testId = (String)testResult.get("test_id");
				BeanUtils.copyProperties(mt, testResult);
				List<BasicDynaBean> microOrgDetails =
					DiagnosticsDAO.getMicroOrgGroupDetails((Integer)testResult.get("test_micro_id"));
				MicroOrgDetails repOrgDet = null;
				List<MicroOrgDetails> micoOrgForTest = new ArrayList<MicroOrgDetails>();

				for(BasicDynaBean microBean : microOrgDetails){
					repOrgDet = new MicroOrgDetails();
					BeanUtils.copyProperties(repOrgDet, microBean);

					List<BasicDynaBean> antiList = null;
					if(microBean.get("abst_panel_id") != null)
						antiList = DiagnosticsDAO.getAntibioticDetails(
								(Integer) microBean.get("test_org_group_id"),(Integer)microBean.get("abst_panel_id"));
					repOrgDet.setAntibitics(antiList);

					micoOrgForTest.add(repOrgDet);
				}
				mt.setOrgGrpDetails(micoOrgForTest);
				mt.setCondDoctorName((String) testResult.get("cond_doctor_name"));
				mt.setCondDoctorId(condDoctorId);
				mt.setTechSignatureName(techSignatureName);

			} else if (format.equals("C")) {
				CytoTests ct = new CytoTests((String)testResult.get("test_name"));
				dept.cytoTests.add(ct);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					ct.labNumber = "-";
				} else {
					ct.labNumber = (String) testResult.get("labno");
				}
				ct.testDate = (Date) testResult.get("conducted_date");
				ct.sampleDate = (Date) testResult.get("sample_date");
				ct.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
				ct.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
				ct.assertionTime = (Timestamp) testResult.get("assertion_time");
				ct.prescDateTime = (Timestamp) testResult.get("pres_date");
				ct.specimenType = (String) testResult.get("sample_type");
				ct.test_type = (String) testResult.get("test_type");
				ct.speciman_adequecy = (String) testResult.get("specimen_adequacy");
				ct.cyto_microscopic_details = (String) testResult.get("cyto_microscopic_details");
				ct.smear_received = (String) testResult.get("smear_received");
				ct.cyto_short_impression = (String) testResult.get("cyto_short_impression");
				ct.cyto_impression_details = (String)testResult.get("cyto_impression_details");
				ct.cyto_clinical_details = (String)testResult.get("cyto_clinical_details");
				ct.setCondDoctorName((String) testResult.get("cond_doctor_name"));
				ct.setCondDoctorId(condDoctorId);
				ct.setTechSignatureName(techSignatureName);
				ct.setBedName((String)testResult.get("bed_name"));
				ct.setWardName((String)testResult.get("ward_name"));
				ct.setCompleted_by((String)testResult.get("completed_by"));
				ct.setValidated_by((String)testResult.get("validated_by"));
				ct.testId = (String)testResult.get("test_id");
				BeanUtils.copyProperties(ct, testResult);
			}

		}
		
		String mrNo= (String)((Map)root.get("visitDetails")).get("mrNo");
		String rawObr = (String) report.get("hl7_obr_segment");
		incoming = mrNo == null || mrNo.isEmpty();
		if (reportSampleNo == null) reportSampleNo = "";
		if(prescribing_doctor == null) prescribing_doctor = ""; if(reportTestDate == null)reportTestDate="";
		if(reportLabNo == null) reportLabNo = ""; if(reportSampleDate == null)reportSampleDate="";
		if(reportSpecimenType ==null)reportSpecimenType = "";if(reportServiceSubGroup == null)reportServiceSubGroup="";
		if(referal == null)referal = "";
        root.put("reportSampleNo", reportSampleNo); root.put("singleSmapleNo", singleSampleNo);
		root.put("reportDoctor", prescribing_doctor); root.put("singleDoctor", singleDoctor);
        root.put("prescribing_doctor", prescribing_doctor);
        if(!StringUtils.isEmpty(rawObr)) {
          root.put("rawObrArray", rawObr.split("\\|"));
        }
        root.put("reviewRemarks", report.get("review_remarks"));
		root.put("reportTestDate",reportTestDate); root.put("singleTestDate", singleTestDate);
		root.put("reportServiceSubGroup", reportServiceSubGroup); root.put("singleServiceSubGroup", singleServiceSubGroup);
		root.put("reportLabNo", reportLabNo); root.put("singleLabNo", singleLabNo);
		root.put("reportSampleDate", reportSampleDate); root.put("singleSampleDate", singleSampleDate);
		root.put("reportSpecimenType", reportSpecimenType); root.put("singleSpecimenType", singleSpecimenType);
		root.put("incoming",incoming);
		root.put("incoming_hosp", incoming_hosp);
		root.put("referal", referal);
		root.put("user_name", report.get("user_name"));
		root.put("singleAssertionTime", singleAssertionTime);
		root.put("assertionTime", assertionTime);
		root.put("singlePrescDateTime", singlePrescDateTime);
		root.put("prescDateTime", prescDateTime);
		root.put("technician", technician);
		root.put("singleTechnician", singleTechnician);
		root.put("conducting_doctor", conducting_doctor);
		root.put("singleCondDoctor", singleCondDoc);
		root.put("conducting_doc_id", conducting_doc_id);
		root.put("allAreValidated", allAreValidated);
		root.put("allAreSignedOff", allAreSignedOff);

		//amendments
		if(originalReportBean != null)
			amendedReportData(amendedPrescribedIds,root,(Integer)originalReportBean.get("report_id"),amendedDepts);

		//reconductions
		if( reconductedPrescribedIds.size() > 0 )
			reconductedReportData(reconductedPrescribedIds, root,isRadiology,reconductedDepts);

		String userDiaplayName = userName;
		User userDetails = new UserDAO().getUserDetails(userName);

		if( userDetails != null )
			userDiaplayName = userDetails.getFullname();
		root.put("values", values);
		root.put("depts",depts);
		root.put("reconductDepts", reconductedDepts);
		root.put("amendedDepts", amendedDepts);
		root.put("conducting_doctors", conductingDoctors);
		root.put("doctors", conductingDoctors);
		root.put("designations",designations);
		root.put("userDetails", userDetails);
		root.put("userDisplayName", userDiaplayName);
		root.put("seviarityColorCodes", GenericPreferencesDAO.getAllPrefs());

		PrintTemplate template;
		if (WebBasedReport)
			template = category.equals("DEP_LAB") ? PrintTemplate.WebLab : PrintTemplate.WebRad;
		else if (apiBased)
			template = category.equals("DEP_LAB") ? PrintTemplate.APILAB : PrintTemplate.APIRAD;
		else
			template = category.equals("DEP_LAB") ? PrintTemplate.Lab : PrintTemplate.Rad;

        String templateContent = new PrintTemplatesDAO().getCustomizedTemplate(template);
        FtlReportGenerator ftlGen = null;
        if(templateContent == null || templateContent.equals("")){
       		ftlGen = new FtlReportGenerator(template.getFtlName());
        }else{
        	StringReader reader = new StringReader(templateContent);
        	ftlGen = new FtlReportGenerator(template.getFtlName(),reader);
        }
        StringWriter writer = new StringWriter();
        ftlGen.setReportParams(root);
        ftlGen.process(writer);
        StringBuilder html = new StringBuilder(writer.toString());
		writer.close();

		String outString = html.toString();
		log.debug(outString);
		return outString;
	}


	public static Map getPatientDetailsMap(String patientId, int reportId)
		throws SQLException, ParseException {
		
		BasicDynaBean patient = null;
		BasicDynaBean customer = OhSampleRegistrationDAO.getIncomingCustomer(patientId);
		if (customer == null)
			patient = VisitDetailsDAO.getPatientVisitBean(patientId);
		
		Map patientDetails = new HashMap();
		String fullName = "";
		String ageAndSex = "";
		String mrno = "";
		String incomingPhoneNo = "";
		String incomingOtherInfo = "";
		if (patient == null && customer !=null) {
			fullName = (String)customer.get("patient_name");
			ageAndSex =  (Integer)customer.get("patient_age") +" "+(String)customer.get("age_unit")+"/"+ (String)customer.get("patient_gender");
			patientId = (String)customer.get("incoming_visit_id");
			incomingPhoneNo = (String)customer.get("phone_no");
			incomingOtherInfo = (String)customer.get("patient_other_info");

		} else {
			Map ageMap = PatientDetailsDAO.getAgeAsPerRegistraionDate(patientId,reportId);

			fullName = patient.get("full_name").toString();
			if (ageMap != null) {
				if (ageMap.get("age") != null && ageMap.get("ageIn") != null) {
					ageAndSex = ageMap.get("age").toString() +" "+ ageMap.get("ageIn").toString();
				} else if (ageMap.get("age") == null || ageMap.get("ageIn") == null) {
					ageAndSex = "-";
				}
				if (patient.get("patient_gender") != null) {
					ageAndSex = ageAndSex + "/" + patient.get("patient_gender").toString();
				} else if (patient.get("patient_gender") == null) {
					ageAndSex = ageAndSex + "/-";
				}				
			}
			mrno = patient.get("mr_no").toString();
		}

		GenericDocumentsFields.copyStandardFields(patientDetails, false);
		if (patient != null)
			GenericDocumentsFields.copyPatientDetails(patient.getMap(), patientDetails, false);

		patientDetails.put("fullName", fullName);
		patientDetails.put("ageAndSex", ageAndSex);
		patientDetails.put("mrNo", mrno);
		patientDetails.put("visitNo", patientId);
		/* For web based report will have more than one report id so that time we are passing  0 as report id.
		 * If report id is 0  then else part will run..*/
		if(reportId > 0) {
			ArrayList reportList = new DiagnosticsDAO().getReport(reportId);
			Map report = (Map) reportList.get(0);
			patientDetails.put("reportDate", report.get("REPORT_DATE"));
			patientDetails.put("reportTime", report.get("REPORT_TIME"));
			patientDetails.put("reportName", report.get("REPORT_NAME"));
			patientDetails.put("userName", report.get("USER_NAME"));
			patientDetails.put("signedOff", report.get("SIGNED_OFF"));
		} else {
			// just for legacy purpose we added this..
			patientDetails.put("reportDate", null);
			patientDetails.put("reportTime", null);
			patientDetails.put("reportName", "");
			patientDetails.put("userName", "");
		}
		patientDetails.put("consulting_doctor", patientDetails.get("doctor_name"));
		patientDetails.put("incomingPhoneNo", incomingPhoneNo);
		patientDetails.put("incomingOtherInfo", incomingOtherInfo);

		return patientDetails;
	}

	/**
	 * Amended data of the report
	 * @param amendedPrescribedIds
	 * @param root
	 * @throws SQLException
	 */
	private static void amendedReportData(List amendedPrescribedIds,HashMap<String, Object> root
			,int originalReportId,List amendedDepts)
	throws SQLException{

		DeptTest amenddept = null;
		ValueTest amendvt = null ;
		String amendPrvDept = "",category = "",idNoLabel = "";

		BasicDynaBean report = DiagnosticsDAO.getReportDynaBean(originalReportId);
		boolean isRadiology = !((String)report.get("category")).equals("DEP_LAB");

		if (isRadiology){
			category = "DEP_RAD";
			idNoLabel="Radiology ID: ";
		}else{
			category = "DEP_LAB";
			idNoLabel = "Lab ID No:";
		}

		root.put("idNoLabel_amended", idNoLabel);
		root.put("isRadiology_amended", isRadiology);


		String prescribing_doctor_amended = null;boolean singleDoctor_amended = true;
		String reportTestDate_amended = null;boolean singleTestDate_amended = true;
		String reportSampleDate_amended = null;boolean singleSampleDate_amended = true;
		String reportLabNo_amended = null;boolean singleLabNo_amended = true;
		String reportSpecimenType_amended = null;boolean singleSpecimenType_amended = true;
		String reportSampleNo_amended = null;boolean singleSampleNo_amended = true;
		String referal_amended = null;String incoming_hosp_amended = null;
		String reportServiceSubGroup_amended = null;boolean singleServiceSubGroup_amended = true;
		boolean incoming_amended = true;

		List amendedTestResults = new ArrayList();
		Set<Integer> amendedPrescribedIdsSet = new HashSet<Integer>(amendedPrescribedIds);//removes duplicate prescribed ids
		for(Integer amendedPrescId : amendedPrescribedIdsSet){
			amendedTestResults.addAll(DiagnosticsDAO.getReportTestValues(amendedPrescId,true));
		}

		Iterator amendit = amendedTestResults.iterator();
		int prevPrescribedId = 0;
		while (amendit.hasNext()) {
			BasicDynaBean testResult = (BasicDynaBean) amendit.next();

			String doctor = (String)testResult.get("doctor_name");
			if ((doctor!=null) && !doctor.equalsIgnoreCase("")) {
				if (prescribing_doctor_amended == null)
					prescribing_doctor_amended = doctor;
				else
					singleDoctor_amended = singleDoctor_amended && prescribing_doctor_amended.equals(doctor);
			}

			java.sql.Timestamp testDate = (java.sql.Timestamp)testResult.get("conducted_date");
			if (testDate != null) {
				String testDateStr = DataBaseUtil.timeStampFormatter.format(testDate);
				if (reportTestDate_amended == null)
					reportTestDate_amended = testDateStr;
				else
					singleTestDate_amended = singleTestDate_amended && reportTestDate_amended.equals(testDateStr);
			}

			java.sql.Timestamp sampleDate = (java.sql.Timestamp)testResult.get("sample_date");
			if (sampleDate != null) {
				String sampleDateStr = DataBaseUtil.timeStampFormatter.format(sampleDate);
				if (reportSampleDate_amended == null)
					reportSampleDate_amended = sampleDateStr;
				else
					singleSampleDate_amended = singleSampleDate_amended && reportSampleDate_amended.equals(sampleDateStr);
			}

			String serviceSubGroup = (String) testResult.get("service_sub_group_name");
			if(serviceSubGroup != null) {
				if (reportServiceSubGroup_amended == null)
					reportServiceSubGroup_amended = serviceSubGroup;
				else
					singleServiceSubGroup_amended = singleServiceSubGroup_amended && reportServiceSubGroup_amended.equals(serviceSubGroup);
			}

			String labNo_amended = (String) testResult.get("labno");
			if (labNo_amended != null) {
				if (reportLabNo_amended == null)
					reportLabNo_amended = labNo_amended;
				else
					singleLabNo_amended = singleLabNo_amended && reportLabNo_amended.equals(labNo_amended);
			}

			String specimenType = (String) testResult.get("sample_type");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSpecimenType_amended == null)
					reportSpecimenType_amended = specimenType;
				else
					singleSpecimenType_amended = singleSpecimenType_amended && reportSpecimenType_amended.equals(specimenType);
			}

			String sampleNo = (String) testResult.get("sample_sno");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSampleNo_amended == null)
					reportSampleNo_amended = sampleNo;
				else
					singleSampleNo_amended = singleSampleNo_amended && reportSampleNo_amended.equals(sampleNo);
			}

			referal_amended = (String)testResult.get("referal_name");
			incoming_hosp_amended = (String)testResult.get("hospital_name");
			incoming_amended = incoming_hosp_amended != null;



			String deptName = (String) testResult.get("ddept_name");
			if (!deptName.equals(amendPrvDept)) {
				// add a new department to the dept list
				amenddept = new DeptTest(deptName);
				amendedDepts.add(amenddept);
				amendPrvDept = deptName;
			}

			String format = (String) testResult.get("conducted_in_reportformat");
			String labNo = null;
			boolean hasAbnormalResults = false;

			if (format.equals("N")) {
				int prescribedId = ((Integer) testResult.get("prescribed_id")).intValue();
				if (prescribedId != prevPrescribedId) {
					log.debug("Amended value test: " + prescribedId);
					amendvt = new ValueTest((String) testResult.get("test_name"));
					amenddept.valueTests.add(amendvt);
					prevPrescribedId = prescribedId;
					labNo=(String) testResult.get("labno");
					if(labNo==null){
						amendvt.labNumber="-";
					}else{
						amendvt.labNumber = (String) testResult.get("labno");
					}
					amendvt.testRemarks = (String) testResult.get("remarks");
					amendvt.testDate = (Date) testResult.get("conducted_date");
					amendvt.sampleDate = (Date) testResult.get("sample_date");
					amendvt.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
					amendvt.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
					amendvt.specimenType = (String) testResult.get("sample_type");
					amendvt.sampleSource = (String)testResult.get("source_name");
					amendvt.testConduction = (String)testResult.get("conduction_instructions");
				}
				amendvt.results.add(testResult);
				String remarks = (String) testResult.get("comments");
				if ( (remarks != null) && (!remarks.isEmpty()) ) {
					amenddept.hasRemarks = true;
				}
				String withinNormal = (String) testResult.get("withinnormal");
				if (!withinNormal.equals("Y")) {
					hasAbnormalResults = true;
				}
			} else if(format.equals("Y")){
				log.debug("amended report test: " + testResult.get("test_name"));
				FormatTest ft = new FormatTest((String) testResult.get("test_name"));
				amenddept.reportTests.add(ft);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					ft.labNumber = "-";
				} else {
					ft.labNumber = (String) testResult.get("labno");
				}
				ft.testDate = (Date) testResult.get("conducted_date");
				ft.sampleDate = (Date) testResult.get("sample_date");
				ft.serviceSubGroupName = (String) testResult.get("service_sub_group_name");
				ft.serviceSubGroupID = (Integer) testResult.get("service_sub_group_id");
				ft.specimenType = (String) testResult.get("sample_type");
				ft.result = testResult;
				ft.testConduction = (String)testResult.get("conduction_instructions");
				ft.amendmentReason = (String)testResult.get("amendment_reason");
				ft.testRemarks = (String) testResult.get("remarks");
			}
		}

		if (reportSampleNo_amended == null) reportSampleNo_amended = "";
		if(prescribing_doctor_amended == null) prescribing_doctor_amended = "";
		if(reportTestDate_amended == null)reportTestDate_amended="";
		if(reportLabNo_amended == null) reportLabNo_amended = "";
		if(reportSampleDate_amended == null)reportSampleDate_amended="";
		if(reportSpecimenType_amended ==null)reportSpecimenType_amended = "";
		if(referal_amended == null)referal_amended = "";
		if(reportServiceSubGroup_amended == null)reportServiceSubGroup_amended = "";

		root.put("reportSampleNo_amended", reportSampleNo_amended);
		root.put("singleSmapleNo_amended", singleSampleNo_amended);
		root.put("reportDoctor_amended", prescribing_doctor_amended);
		root.put("singleDoctor_amended", singleDoctor_amended);
		root.put("prescribing_doctor_amended", prescribing_doctor_amended);
		root.put("reportTestDate_amended",reportTestDate_amended);
		root.put("singleTestDate_amended", singleTestDate_amended);
		root.put("reportLabNo_amended", reportLabNo_amended);
		root.put("singleLabNo_amended", singleLabNo_amended);
		root.put("reportSampleDate_amended", reportSampleDate_amended);
		root.put("singleSampleDate_amended", singleSampleDate_amended);
		root.put("reportSpecimenType_amended", reportSpecimenType_amended);
		root.put("singleSpecimenType_amended", singleSpecimenType_amended);
		root.put("incoming_amended",incoming_amended);
		root.put("incoming_hosp_amended", incoming_hosp_amended);
		root.put("referal_amended", referal_amended);
		root.put("reportServiceSubGroup_amended", reportServiceSubGroup_amended);
		root.put("singleServiceSubGroup_amended", singleServiceSubGroup_amended);
	}

	private static void reconductedReportData(List reconductedPrescIds,HashMap<String,Object> root
			,boolean isRadiology,List reconductedDepts)
	throws SQLException{

		DeptTest reconDept = null;
		ValueTest recodVt = null ;
		String recondPrvDept = "",category = "",idNoLabel = "";

		String prescribing_doctor_reconducted = null;boolean singleDoctor_reconducted = true;
		String reportTestDate_reconducted = null;boolean singleTestDate_reconducted = true;
		String reportSampleDate_reconducted = null;boolean singleSampleDate_reconducted = true;
		String reportLabNo_reconducted = null;boolean singleLabNo_reconducted = true;
		String reportSpecimenType_reconducted = null;boolean singleSpecimenType_reconducted = true;
		String reportSampleNo_reconducted = null;boolean singleSampleNo_reconducted = true;
		String referal_reconducted = null;String incoming_hosp_reconducted = null;
		String reportServiceSubGroup_reconducted = null;boolean singleServiceSubGroup_reconducted = true;
		boolean incoming_reconducted = true;

		GenericDAO tpDAO = new GenericDAO("tests_prescribed");

		if (isRadiology){
			category = "DEP_RAD";
			idNoLabel="Radiology ID: ";
		}else{
			category = "DEP_LAB";
			idNoLabel = "Lab ID No:";
		}

		root.put("idNoLabel_reconducted", idNoLabel);
		root.put("isRadiology_reconducted", isRadiology);

		List reconductedTestResults = new ArrayList();
		Set<Integer> reconductedPrescribedIdsSet = new HashSet<Integer>(reconductedPrescIds);//removes duplicate prescribed ids
		BasicDynaBean reconductedTest = null;
		for(Integer recPrescId : reconductedPrescribedIdsSet){
			reconductedTest = tpDAO.findByKey("prescribed_id", recPrescId);
			if ( reconductedTest != null && reconductedTest.get("conducted").equals("RAS") )//reconducted after signoff
				reconductedTestResults.addAll(DiagnosticsDAO.getReportTestValues(recPrescId,null));
		}

		Iterator reconductIT = reconductedTestResults.iterator();
		int prevPrescribedId = 0;
		while (reconductIT.hasNext()) {
			BasicDynaBean testResult = (BasicDynaBean) reconductIT.next();

			String doctor = (String)testResult.get("doctor_name");
			if ((doctor!=null) && !doctor.equalsIgnoreCase("")) {
				if (prescribing_doctor_reconducted == null)
					prescribing_doctor_reconducted = doctor;
				else
					singleDoctor_reconducted = singleDoctor_reconducted && prescribing_doctor_reconducted.equals(doctor);
			}

			java.sql.Timestamp testDate = (java.sql.Timestamp)testResult.get("conducted_date");
			if (testDate != null) {
				String testDateStr = DataBaseUtil.timeStampFormatter.format(testDate);
				if (reportTestDate_reconducted == null)
					reportTestDate_reconducted = testDateStr;
				else
					singleTestDate_reconducted = singleTestDate_reconducted && reportTestDate_reconducted.equals(testDateStr);
			}

			java.sql.Timestamp sampleDate = (java.sql.Timestamp)testResult.get("sample_date");
			if (sampleDate != null) {
				String sampleDateStr = DataBaseUtil.timeStampFormatter.format(sampleDate);
				if (reportSampleDate_reconducted == null)
					reportSampleDate_reconducted = sampleDateStr;
				else
					singleSampleDate_reconducted = singleSampleDate_reconducted && reportSampleDate_reconducted.equals(sampleDateStr);
			}

			String serviceSubGroup = (String) testResult.get("service_sub_group_name");
			if(serviceSubGroup != null) {
				if(reportServiceSubGroup_reconducted == null)
					reportServiceSubGroup_reconducted = serviceSubGroup;
				else
					singleServiceSubGroup_reconducted = singleServiceSubGroup_reconducted && reportServiceSubGroup_reconducted.equals(serviceSubGroup);
			}

			String labNo_amended = (String) testResult.get("labno");
			if (labNo_amended != null) {
				if (reportLabNo_reconducted == null)
					reportLabNo_reconducted = labNo_amended;
				else
					singleLabNo_reconducted= singleLabNo_reconducted && reportLabNo_reconducted.equals(labNo_amended);
			}

			String specimenType = (String) testResult.get("sample_type");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSpecimenType_reconducted == null)
					reportSpecimenType_reconducted = specimenType;
				else
					singleSpecimenType_reconducted = singleSpecimenType_reconducted && reportSpecimenType_reconducted.equals(specimenType);
			}

			String sampleNo = (String) testResult.get("sample_sno");
			if ((specimenType != null) && !specimenType.equals("")) {
				if (reportSampleNo_reconducted == null)
					reportSampleNo_reconducted = sampleNo;
				else
					singleSampleNo_reconducted = singleSampleNo_reconducted && reportSampleNo_reconducted.equals(sampleNo);
			}

			referal_reconducted = (String)testResult.get("referal_name");
			incoming_hosp_reconducted = (String)testResult.get("hospital_name");
			incoming_reconducted = incoming_hosp_reconducted != null;

			String deptName = (String) testResult.get("ddept_name");
			if (!deptName.equals(recondPrvDept)) {
				// add a new department to the dept list
				reconDept = new DeptTest(deptName);
				reconductedDepts.add(reconDept);
				recondPrvDept = deptName;
			}

			String labNo = null;
			boolean hasAbnormalResults = false;
			String format = (String) testResult.get("conducted_in_reportformat");
			if (format.equals("N")) {
				int prescribedId = ((Integer) testResult.get("prescribed_id")).intValue();
				if (prescribedId != prevPrescribedId) {
					log.debug("Amended value test: " + prescribedId);
					recodVt = new ValueTest((String) testResult.get("test_name"));
					reconDept.valueTests.add(recodVt);
					prevPrescribedId = prescribedId;
					labNo=(String) testResult.get("labno");
					if(labNo==null){
						recodVt.labNumber="-";
					}else{
						recodVt.labNumber = (String) testResult.get("labno");
					}
					recodVt.testRemarks = (String) testResult.get("remarks");
					recodVt.testDate = (Date) testResult.get("conducted_date");
					recodVt.sampleDate = (Date) testResult.get("sample_date");
					recodVt.specimenType = (String) testResult.get("sample_type");
					recodVt.sampleSource = (String)testResult.get("source_name");
					recodVt.testConduction = (String)testResult.get("conduction_instructions");
					recodVt.reconductionReason = (String)testResult.get("reconducted_reason");
					recodVt.serviceSubGroupName = (String)testResult.get("service_sub_group_name");
					recodVt.serviceSubGroupID = (Integer)testResult.get("service_sub_group_id");
				}
				recodVt.results.add(testResult);
				String remarks = (String) testResult.get("comments");
				if ( (remarks != null) && (!remarks.isEmpty()) ) {
					reconDept.hasRemarks = true;
				}
				String withinNormal = (String) testResult.get("withinnormal");
				if (!withinNormal.equals("Y")) {
					hasAbnormalResults = true;
				}
			} else if(format.equals("Y")){
				log.debug("new report test: " + testResult.get("test_name"));
				FormatTest ft = new FormatTest((String) testResult.get("test_name"));
				reconDept.reportTests.add(ft);
				labNo=(String) testResult.get("labno");
				if (labNo==null) {
					ft.labNumber = "-";
				} else {
					ft.labNumber = (String) testResult.get("labno");
				}
				ft.testDate = (Date) testResult.get("conducted_date");
				ft.sampleDate = (Date) testResult.get("sample_date");
				ft.specimenType = (String) testResult.get("sample_type");
				ft.result = testResult;
				ft.testConduction = (String)testResult.get("conduction_instructions");
				ft.reconductionReason = (String)testResult.get("reconducted_reason");
				ft.serviceSubGroupName = (String)testResult.get("service_sub_group_name");
				ft.serviceSubGroupID = (Integer)testResult.get("service_sub_group_id");
			}
		}

		if (reportSampleNo_reconducted == null) reportSampleNo_reconducted = "";
		if(prescribing_doctor_reconducted == null) prescribing_doctor_reconducted = "";
		if(reportTestDate_reconducted == null)reportTestDate_reconducted="";
		if(reportLabNo_reconducted == null) reportLabNo_reconducted = "";
		if(reportSampleDate_reconducted == null)reportSampleDate_reconducted="";
		if(reportSpecimenType_reconducted ==null)reportSpecimenType_reconducted = "";
		if(referal_reconducted == null)referal_reconducted = "";
		if(reportServiceSubGroup_reconducted == null)reportServiceSubGroup_reconducted = "";

		root.put("reportSampleNo_reconducted", reportSampleNo_reconducted);
		root.put("singleSmapleNo_reconducted", singleSampleNo_reconducted);
		root.put("reportDoctor_reconducted", prescribing_doctor_reconducted);
		root.put("singleDoctor_reconducted", singleDoctor_reconducted);
		root.put("prescribing_doctor_reconducted", prescribing_doctor_reconducted);
		root.put("reportTestDate_reconducted",reportTestDate_reconducted);
		root.put("singleTestDate_reconducted", singleTestDate_reconducted);
		root.put("reportLabNo_reconducted", reportLabNo_reconducted);
		root.put("singleLabNo_reconducted", singleLabNo_reconducted);
		root.put("reportSampleDate_reconducted", reportSampleDate_reconducted);
		root.put("singleSampleDate_reconducted", singleSampleDate_reconducted);
		root.put("reportSpecimenType_reconducted", reportSpecimenType_reconducted);
		root.put("singleSpecimenType_reconducted", singleSpecimenType_reconducted);
		root.put("incoming_reconducted",incoming_reconducted);
		root.put("incoming_hosp_reconducted", incoming_hosp_reconducted);
		root.put("referal_reconducted", referal_reconducted);
		root.put("reportServiceSubGroup_reconducted", reportServiceSubGroup_reconducted);
		root.put("singleServiceSubGroup_reconducted", singleServiceSubGroup_reconducted);
	}
	
	public static int getReportPrintCenter(int reportId, String pref) throws SQLException {
		if(pref == null) {
		    pref = GenericPreferencesDAO.getGenericPreferences().getDiag_report_print_center();
		}
		int centerId = 0;
		if(pref.equalsIgnoreCase("col")) {
			centerId = (Integer)((BasicDynaBean)LaboratoryDAO.getReportCollectionCenter(reportId)).get("center_id");
		} else {
			centerId = (Integer)((BasicDynaBean)LaboratoryDAO.getReportConductionCenter(reportId)).get("center_id");
		}
		return centerId;
	}

}

