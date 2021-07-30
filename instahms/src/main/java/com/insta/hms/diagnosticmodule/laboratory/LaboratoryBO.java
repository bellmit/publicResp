package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.common.SampleCollection;
import com.insta.hms.diagnosticmodule.common.TestConducted;
import com.insta.hms.diagnosticmodule.common.TestDetails;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.common.TestReportImages;
import com.insta.hms.diagnosticmodule.common.TestVisitReports;
import com.insta.hms.diagnosticmodule.radiology.RadiologyDAO;
import com.insta.hms.diagnosticsmasters.Reagent;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.diagnosticsmasters.Test;
import com.insta.hms.diagnosticsmasters.TestTemplate;
import com.insta.hms.diagnosticsmasters.TestandResults;
import com.insta.hms.diagnosticsmasters.TestandResults.MicroOrgDetails;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.DiagnosticReagentMaster.DiagnosticReagentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.stores.StoreItemStock;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

public class LaboratoryBO {

	static Logger logger = LoggerFactory.getLogger(LaboratoryBO.class);
	
	private static LaboratoryDAO dao = new LaboratoryDAO();
	private static GenericDAO microAbstAntibioticDAO = new GenericDAO("test_micro_antibiotic_details");
	private static GenericDAO testPrescDAO = new GenericDAO("tests_prescribed");
	private static TestVisitReportSignaturesDAO sigDAO = new TestVisitReportSignaturesDAO();
	private static DiagnosticsDAO diagDao = new DiagnosticsDAO();
	private static OhSampleRegistrationDAO ohSampleRegistrationDao =new OhSampleRegistrationDAO();
	private static GenericDAO testVisitReportsDao = new GenericDAO("test_visit_reports");
	private static GenericDAO diagReagentDAO = new GenericDAO("diagnostic_reagent_usage");


	public List<TestandResults> getTestsandResults(String visitId,
			 String category,String prescriptionList)throws SQLException,Exception{

		List<MicroOrgDetails> microOrgDetails = null;
		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		boolean isIncomingPatient = ( patientDetails == null );
		if(patientDetails == null)
			patientDetails =  PatientDetailsDAO.getIncomingPatientResultDetails(visitId).getMap();
		ArrayList<TestandResults> trlist= new ArrayList<TestandResults>();
	    boolean isTemplatethere=false;

		List<Hashtable<String , String>> l=null;

		l = dao.getVisitTestDetails(visitId, category,prescriptionList);
		logger.debug("{}", l);
		Iterator<Hashtable<String,String>> i = l.iterator();

		while(i.hasNext()){
			Hashtable<String,String>item = i.next();
			Test t = new Test();
			t.setTestId(item.get("TEST_ID"));
			t.setTestName(item.get("TEST_NAME"));
			t.setDdeptId(item.get("DDEPT_ID"));
			t.setDdeptName(item.get("DDEPT_NAME"));
			t.setSampleNeed(item.get("SAMPLE_NEEDED"));
			t.setSampleDate(item.get("SAMPLE_DATE"));
			t.setSampleTime(item.get("SAMPLE_TIME"));
			t.setReportGroup(item.get("CONDUCTION_FORMAT"));
			t.setLabno(item.get("LABNO"));
			t.setRemarks(item.get("REMARKS"));
			t.setReportId(item.get("REPORT_ID"));
			t.setReportName(item.get("REPORT_NAME"));
			t.setConductedDoctor(item.get("CONDUCTED_BY"));
			t.setTestDate(item.get("CONDUCTED_DATE"));
			t.setTestStatus(item.get("HOUSE_STATUS"));
			t.setPrescriptionType(item.get("PRESCRIPTION_TYPE"));
			t.setBillType(item.get("BILL_TYPE"));
			t.setBillStatus(item.get("BILL_STATUS"));
			t.setSampleNo(item.get("SAMPLE_SNO"));
			t.setOrigSampelNo(item.get("ORIG_SAMPLE_NO"));
			t.setConducting_doc_mandatory(item.get("CONDUCTING_DOC_MANDATORY"));
			t.setConductionInstructions(item.get("CONDUCTION_INSTRUCTIONS"));
			t.setTestTime(item.get("CONDUCTED_TIME"));
			if(item.get("RE_CONDUCTION").equals("t"))
				t.setPaymentStatus("P");
			else
				t.setPaymentStatus(item.get("PAYMENT_STATUS"));
			t.setTestTime(item.get("CONDUCTED_TIME"));
			t.setSampleSource(item.get("SOURCE_NAME"));
			t.setSpecimenCondition(item.get("SPECIMEN_CONDITION"));
			t.setOrderRemarks(item.get("ORDER_REMARKS"));
			t.setSampleType(item.get("SAMPLE_TYPE"));
			t.setTechnician(item.get("TECHNICIAN"));
			t.setConductingRoleIds((item.get("CONDUCTING_ROLE_ID") != null && !item.get("CONDUCTING_ROLE_ID").isEmpty()) ? item.get("CONDUCTING_ROLE_ID").split(",") : null);
			t.setOutSourceDestPresId(item.get("OUTSOURCE_DEST_PRESCRIBED_ID"));
			t.setSampleDate(item.get("SAMPLE_DATE"));
			t.setIsconfidential(item.get("ISCONFIDENTIAL").equals("t"));
			t.setIncoming_source_type(item.get("INCOMING_SOURCE_TYPE"));
			//Results
			List<BasicDynaBean>results =
				LaboratoryDAO.getResultsfrommaster(item.get("TEST_ID"),item.get("PRESCRIBED_ID"), visitId);
			Iterator<BasicDynaBean> ri = results.iterator();
			ArrayList<Result> rlist = new ArrayList<Result>();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("sample_date", item.get("SAMPLE_DATE"));
			logger.debug("{}", results);
			while(ri.hasNext()){
				BasicDynaBean ritem =  ri.next();
				resultMap.put("resultlabel_id", ritem.get("resultlabel_id"));
				BasicDynaBean resultRange = null;
				if (null != ritem.get("resultlabel_id")) {
					resultRange = ResultRangesDAO.getResultRange(
							resultMap,patientDetails);
				}
				Result r = new Result((String)ritem.get("test_id"),(String)ritem.get("resultlabel"),(String)ritem.get("units"),
								  	  resultRange == null ? (String)ritem.get("reference_range") : (String)resultRange.get("reference_range_txt")
									  ,ritem.get("display_order") == null ? String.valueOf(ritem.get("test_details_id")) : String.valueOf(ritem.get("display_order")));
				r.setResultvalue((String)ritem.get("value"));
				r.setRemarks((String)ritem.get("remarks"));
				r.setWithinNormal((String)ritem.get("withinnormal"));
				r.setExpression((String)ritem.get("expr_4_calc_result"));
				r.setCode_type((String)ritem.get("code_type"));
				r.setResult_code((String)ritem.get("result_code"));
				r.setResultrange(resultRange);
				r.setMethodName((String)ritem.get("method_name"));
				r.setCalculated((String)ritem.get("calculated"));
				r.setMethodId((Integer)ritem.get("method_id"));
				r.setCountOfRanges(ResultRangesDAO.listAllTestResultReferences(item.get("TEST_ID")).size());
				BeanUtils.copyProperties(r,ritem);

				r.setDataAllowed(ritem.get("data_allowed") == null ? "V" : (String)ritem.get("data_allowed"));
				r.setSourceIfList((String)ritem.get("source_if_list"));
				r.setDefaultValue((String)ritem.get("default_value"));
				rlist.add(r);
			}

			//Templates
			ArrayList<TestTemplate> newTemplates = null;
			TestTemplate template = null;
			TestTemplate amendedTemplate = null;
			List<TestTemplate> amendedTemplates = new ArrayList<TestTemplate>();
			if(item.get("CONDUCTION_FORMAT").equals("T")){
				List<BasicDynaBean> templatesFromPresList =   LaboratoryDAO.
				getTemplatesAtPrescriptionLevel(Integer.parseInt(item.get("PRESCRIBED_ID")),false);

				BasicDynaBean templatesFromPres = templatesFromPresList.size() > 0 ?
						templatesFromPresList.get(0) : null;
				if(templatesFromPres !=null ) {
					template = new TestTemplate();
					String templateName = (String)templatesFromPres.get("format_name");
					String templateId = (String)templatesFromPres.get("testformat_id");
					template.setTestId(item.get("TEST_ID"));
					template.setTemplateId(templateId);
					template.setTemplateName(templateName);
					template.setAmendmentReason((String)templatesFromPres.get("amendment_reason"));
				}

				//A template row is needed for amended report
				List<BasicDynaBean>  amendedDynaBeans = LaboratoryDAO.getAmendedTemplates(
						Integer.parseInt(item.get("PRESCRIBED_ID")));
				for ( BasicDynaBean amendedItem : amendedDynaBeans ){
						amendedTemplate = new TestTemplate();
						amendedTemplate.setTestId(item.get("TEST_ID"));
						amendedTemplate.setTemplateName((String)amendedItem.get("format_name"));
						amendedTemplate.setAmendmentReason((String)amendedItem.get("amendment_reason"));

						amendedTemplates.add(amendedTemplate);
				}


				//This is when the test has more than one template,
				//and need to be conduct in different templet
				newTemplates = new ArrayList<TestTemplate>();
				List<BasicDynaBean> templatesFromMaster =
					LaboratoryDAO.getTemplatesAtMasterLevel(item.get("TEST_ID"), item.get("PRESCRIBED_ID"));
				if(templatesFromMaster !=null) {
					for(int m=0; m<templatesFromMaster.size(); m++) {
						TestTemplate newtemplate = new TestTemplate();
						BasicDynaBean bean = templatesFromMaster.get(m);

						String newTemplateName =(String)bean.get("format_name");
						String newTemplateId = (String)bean.get("testformat_id");

						newtemplate.setTestId(item.get("TEST_ID"));
						newtemplate.setTemplateName(newTemplateName);
						newtemplate.setTemplateId(newTemplateId);
						newTemplates.add(newtemplate);
					}
				}
			}
            // reagents

			List<BasicDynaBean> reagentlist = LaboratoryDAO.getReagentDetails(item.get("TEST_ID"));
			List<Reagent> reagent = new ArrayList<Reagent>();
			Reagent r = null;
			if(reagentlist !=null ) {
				for(int k=0; k<reagentlist.size();k++){
					r = new Reagent();
					r.setActualQty((BigDecimal)reagentlist.get(k).get("qty"));
					r.setItemId((Integer)reagentlist.get(k).get("medicine_id"));
					r.setNeededQty((BigDecimal)reagentlist.get(k).get("quantity_needed"));
					r.setItemName((String)reagentlist.get(k).get("medicine_name"));
					reagent.add(r);
				}
			}

			TestandResults tr = new TestandResults(
					t,rlist,reagent
					,LaboratoryDAO.getTestDetails(Integer.parseInt(item.get("PRESCRIBED_ID")),false));
			tr.setTemplate(template);
			tr.setNewTemplates(newTemplates);
			tr.setAmendedTemplates(amendedTemplates);
			isTemplatethere=dao.getIstemplatethere(item.get("PRESCRIBED_ID"));
			if(isTemplatethere){
			tr.setIsTemplatethere("Y");
			}else{
			tr.setIsTemplatethere("N");
			}
			tr.setHasDocuments(Integer.parseInt(item.get("DOC_COUNT")) != 0);
			tr.setPrescribedId(item.get("PRESCRIBED_ID"));
			tr.setMandate_additional_info(item.get("MANDATE_ADDITIONAL_INFO"));
			tr.setRevisionNumber(item.get("REVISION_NUMBER"));
			tr.setCondctionStatus(item.get("CONDUCTED"));
			tr.setReagent(reagent);
			
			tr.setImpressionDetails(
					dao.getTestImpressionDetails(
						Integer.parseInt(item.get("PRESCRIBED_ID"))));
			List<BasicDynaBean > microTestCondDetailsList =
				dao.getTestMicroBiologyDetails(Integer.parseInt(item.get("PRESCRIBED_ID")));
			BasicDynaBean microDetails = microTestCondDetailsList.size() > 0
				? microTestCondDetailsList.get(0) : null;
			tr.setMicroDetails(microDetails);

			if( microDetails != null ){
				microOrgDetails = new ArrayList<MicroOrgDetails>();
				MicroOrgDetails orgDetails = new MicroOrgDetails();
				List<BasicDynaBean> microOrgDynaList =
					LaboratoryDAO.getMicroOrgGrpDetails((Integer)microDetails.get("test_micro_id"));

				//set antibiotic details
				for(BasicDynaBean orgDetailsListItem : microOrgDynaList){
					orgDetails = new MicroOrgDetails();
					BeanUtils.copyProperties(orgDetailsListItem, orgDetails);

					orgDetails.setAntibioticDetails(
							microAbstAntibioticDAO.findAllByKey("test_org_group_id", orgDetails.getOrg_group_id()));

					microOrgDetails.add(orgDetails);
				}

				tr.setMicroOrgDetails(microOrgDetails);
			}
			List<BasicDynaBean> cytoTestDetailsList =
				dao.getTestCytoDetails(Integer.parseInt(item.get("PRESCRIBED_ID")));
			tr.setCytoDetails(cytoTestDetailsList.size() > 0 ? cytoTestDetailsList.get(0): null);
			tr.setImageUploaded(item.get("IMAGEUPLOADED"));
			trlist.add(tr);
		}

		return trlist;
	}

	public static BasicDynaBean getResultRange(Connection con, HashMap<String, Object> map,Map<String, Object> pd) throws Exception {
		return ResultRangesDAO.getResultRange(con, map, pd);
	}
	
	public List getLabtechnicions(String category, int center_id) throws Exception{

		return dao.getLabtechnicions(category, center_id);
	}

	public static boolean insertLaboratoryTestDetails(Connection con, ArrayList<TestPrescribed> tpList,
			ArrayList<TestConducted> tcList, ArrayList<TestDetails> trList,
			ArrayList<SampleCollection> scList, String category,TestVisitReports tvr
			,String userName,Preferences pref,
			List<BasicDynaBean > impressionsList,List<BasicDynaBean> microList,
			List<BasicDynaBean>cytoList,
			List<BasicDynaBean> microOrgGrpList,List<BasicDynaBean> microAbstAntibioticList,
			int dischardedReport,List<TestDetails> deletedTdList, int center_id,
			String[] deletedNewTestDetailsId, boolean deleteReport, FlashScope flash)
		throws SQLException,Exception{

		boolean status = false;
		//Connection con = DataBaseUtil.getConnection();
		 StringBuilder flashmsg = new StringBuilder();
		con.setAutoCommit(false);
		String modConsumableActive = "Y";
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modConsumableActive = (String)pref.getModulesActivatedMap().get("mod_consumables_flow");
			if(modConsumableActive == null || "".equals(modConsumableActive)){
				modConsumableActive = "N";
			}
		}
		try {
			outer: {
			   Iterator<TestPrescribed> tpIterator = tpList.iterator();

				while(tpIterator.hasNext()) {
					TestPrescribed tp = tpIterator.next();
					status = RadiologyDAO.updateTestPrescription(con,tp);
					if (!status)
						break outer;
				}

				GenericDAO sampleDAO = new GenericDAO("sample_collection");

				BasicDynaBean sampleBean = null;
				Iterator<SampleCollection> scIterator = scList.iterator();
				Map<String, Object> keys = new HashMap<String, Object>();

				while(scIterator.hasNext()){
					//sample collection is needed only for Laboratory
					SampleCollection sc = scIterator.next();
					logger.debug("Inserting sample: " + sc +" " + sc.getSampleNo() + " " + sc.getSampleDate());
					sampleBean = sampleDAO.findByKey("sample_sno", sc.getSampleNo());

					if( sampleBean == null ){//possible when new sample

						int newSampleCollectionId = sampleDAO.getNextSequence();
						sc.setSampleSequence(newSampleCollectionId);
						status = LaboratoryDAO.insertSample(con,sc,center_id);

						//update tests_prescribed with this sample_collection_id
						Map sampleKeys = new HashMap();
						sampleKeys.put("sample_no", sc.getSampleNo());
						sampleKeys.put("sample_collection_id", newSampleCollectionId);

						testPrescDAO.update(con, sampleKeys, "prescribed_id",sc.getPrescribedId());

					}else{
						keys = new HashMap<String, Object>();
						keys.put("specimen_condition", sc.getSpecimenCondition());

						if ( ((String) ((BasicDynaBean)GenericPreferencesDAO.getdiagGenericPref()).get("sampleflow_required")).equals("N") )
							keys.put("sample_date",sc.getSampleDate());

						sampleDAO.update(con, keys, "sample_sno",sc.getSampleNo() );
					}
					if(!status)break outer;
				}

				sampleBean = null;
				scList = null;

				Iterator<TestConducted> tcIterator = tcList.iterator();
				while (tcIterator.hasNext()) {
					TestConducted tc = tcIterator.next();
					status = RadiologyDAO.insertOrUpdateTestConduction(con,tc);
					if (!status) break outer;

					if (tc.getTestConducted().equals("V") || tc.getTestConducted().equals("RV")) {
						status = sigDAO.insertSignatureRecord(con, tc.getReportId(), tc.getPrescribedId(), userName, "T", tc.getTechnician());
					} else {
						LinkedHashMap map = new LinkedHashMap();
						map.put("report_id", tc.getReportId());
						map.put("prescribed_id", tc.getPrescribedId());
						map.put("signed_as", "T");

						if (sigDAO.getReportSignatureRecord(tc.getReportId(), tc.getPrescribedId(), userName, "T") != null)
							status &= sigDAO.delete(con, map);
					}
					if (!status) break outer;
				}

				Iterator<TestDetails> trListIterator= trList.iterator();
				String reportWithNormalResults = "A";
				while(trListIterator.hasNext()){
					TestDetails td = trListIterator.next();

					//test result status shold be same as test status
					for ( TestPrescribed testPresc : tpList) {
						if ( testPresc.getPrescribedId() == td.getPrescribedId()
								&& !td.getTestDetailStatus().equals("A")
								&& !td.getTestDetailStatus().equals("S")) {
							td.setTestDetailStatus( testPresc.getTestconductedFlag() );
							break;
						}
					}
					status = RadiologyDAO.insertTestResults(con,td);

					if ( !reportWithNormalResults.equals("T") && !td.getTestDetailStatus().equals("A") ) {
						
						if ((td.getWithInNormal().equals("*") || td.getWithInNormal().equals("#")) && !reportWithNormalResults.equals("C"))
							reportWithNormalResults = "H";
						else if (td.getWithInNormal().equals("**") || td.getWithInNormal().equals("##"))
							reportWithNormalResults = "C";
						
						BasicDynaBean testDetailsBean  = diagDao.getTestDetailsOfPrescription( con, td.getPrescribedId() );
	
						if ( testDetailsBean.get("conduction_format").equals("T") )
							reportWithNormalResults = "T";//for template based test
					}

					if(!status) break outer;
				}

				if (tvr !=null) {

					tvr.setReportResultsSeverityStatus(reportWithNormalResults);
					status = DiagnosticsDAO.insertOrUpdateReport(con, tvr);
				}

				//discarding a report whose
				if ( dischardedReport != 0  && !deleteReport) {
					diagDao.discardReport(con, dischardedReport,tvr.getReportId());

					//make conducted status back to N and reportid as null for the tests
					//which links to discharged report(for the internal lab tests only)
					
					List<BasicDynaBean> sourcePrescribedList =
							ohSampleRegistrationDao.getSourcePrescribedIDS(con, tvr.getReportId(), tvr.getVisitId());
					Map<String, Object> columnData = new HashMap<String, Object>();
					columnData.put("conducted", "N");
					columnData.put("report_id", null);
					
					status &= LaboratoryBO.copyDataToMultipleChains(con, sourcePrescribedList, columnData, "source_test_prescribed_id", null);

					//update report id to new report id for the tests of the discarded report
					diagDao.updateReportId( con, dischardedReport, tvr.getReportId() );
				}

				/* When all newly added results are deleted then we are deleting newly added report and
					and storing old report id against prescribed tests . */
				if(deleteReport) {
					int newReportId = tvr.getReportId();
					BasicDynaBean rBean = testVisitReportsDao.findByKey("revised_report_id", newReportId);
					int oldReportId = Integer.parseInt(rBean.get("report_id").toString());
					diagDao.updateReportDetails(con, oldReportId, newReportId);
					testVisitReportsDao.delete(con, "report_id", newReportId);
				}

				GenericDAO tdDao = new GenericDAO("test_details");
				for(TestDetails deletedResult : deletedTdList){
					tdDao.delete(con, "test_details_id", deletedResult.getTestDetailsId());
				 }

				//delete expr result if all of results involved init are deleted
				//recalculate expression results
				tpIterator = tpList.iterator();
				List<String> results = null;
				List<String> values = null;
				Map<String,Object> reportValKeys = null;

				while(tpIterator.hasNext()) {
					TestPrescribed tp = tpIterator.next();

					results = new ArrayList<String>();
					values = new ArrayList<String>();

//					list out results and values as seperate lists
					diagDao.listResultsndValues( con,tp.getPrescribedId(),results,values );
					List<BasicDynaBean > exprResults  =
						diagDao.getExpressionResultsForPrescription(con,  tp.getPrescribedId());
					String newValue = null;
					for ( BasicDynaBean exprResult : exprResults ) {
						if (exprResult.get("calculated") != null) {
							if (exprResult.get("calculated").equals("Y")) {
								newValue = ResultExpressionProcessor.processResultExpressionForLAB(
										results, values, (String)exprResult.get("expr_4_calc_result"));
							} else {
								newValue = (String)exprResult.get("report_value");
							}
						} 
						
						reportValKeys = new HashMap<String, Object>();
						reportValKeys.put("report_value",newValue);

						tdDao.update(con, reportValKeys, "test_details_id",(Integer)exprResult.get("test_details_id"));
					}
				}


				//Histopathology results
				GenericDAO impressionDetDAO = new GenericDAO("test_histopathology_results");
				if(impressionsList.size() > 0){
					for(BasicDynaBean impressionBean : impressionsList){
						if(impressionDetDAO.findByKey(con, "prescribed_id", impressionBean.get("prescribed_id")) == null)
							impressionDetDAO.insert(con, impressionBean);
						else
							impressionDetDAO.update(con, impressionBean.getMap(), "prescribed_id", impressionBean.get("prescribed_id"));
					}
				}

				//Microbiology results
				GenericDAO microDAO = new GenericDAO("test_microbiology_results");
				if(microList.size() > 0){
					for(BasicDynaBean microBean : microList){
						if(microDAO.findByKey(con, "prescribed_id", microBean.get("prescribed_id")) == null)
							microDAO.insert(con, microBean);
						else
							microDAO.update(con, microBean.getMap(), "prescribed_id", microBean.get("prescribed_id"));
					}
				}

				//Micro test Organism group details
				GenericDAO microOrgGrpDAO = new GenericDAO("test_micro_org_group_details");
				for(BasicDynaBean microGrp : microOrgGrpList){
					BasicDynaBean microOrgGrpBean  = microOrgGrpDAO.findByKey(con,
							"test_org_group_id",microGrp.get("test_org_group_id"));

					if(microOrgGrpBean == null)
						microOrgGrpDAO.insert(con, microGrp);
					else
						microOrgGrpDAO.update(con, microGrp.getMap(),
								"test_org_group_id",(Integer) microGrp.get("test_org_group_id"));
				}


				//Microbiology Antiboitics
				BasicDynaBean antibioticBean = null;
				for(BasicDynaBean microAntibioticBean : microAbstAntibioticList){
					antibioticBean = microAbstAntibioticDAO.findByKey(con,
							"micr_ant_results_id",microAntibioticBean.get("micr_ant_results_id"));

					if(antibioticBean == null){
						microAntibioticBean.set("micr_ant_results_id", microAbstAntibioticDAO.getNextSequence());
						microAbstAntibioticDAO.insert(con, microAntibioticBean);
					}else
						microAbstAntibioticDAO.update(con, microAntibioticBean.getMap(),
								"micr_ant_results_id",microAntibioticBean.get("micr_ant_results_id"));
				}

//				Cytology
				GenericDAO cytoDAO = new GenericDAO("test_cytology_results");
				if(cytoList.size() > 0){
					for(BasicDynaBean cytoBean : cytoList){
						cytoDAO.delete(con, "prescribed_id",cytoBean.get("prescribed_id"));
					}
					cytoDAO.insertAll(con, cytoList);
				}

				for (int i=0; i<tcList.size(); i++) {
					TestConducted conducted = tcList.get(i);
					BasicDynaBean testPrescribedBean = testPrescDAO.findByKey("prescribed_id", conducted.getPrescribedId());
					int storeId = DiagnosticDepartmentMasterDAO.getStoreOfDiagnosticDepartment(conducted.getTestId(),center_id);
					if ( (conducted.getTestConducted().equals("C") || conducted.getTestConducted().equals("V"))
							&& modConsumableActive.equals("Y")) {
						if(!(Boolean)testPrescribedBean.get("stock_reduced")){
							if(diagReagentDAO.findByKey("prescription_id", conducted.getPrescribedId()) == null){
								status =  StoreItemStock.updateReagents(con, conducted.getTestId(), conducted.getPrescribedId(), userName, storeId,null,0, "diagnostics",flashmsg);
								if(!status){ 
									if(!"".equals(flashmsg.toString()))
										flash.info("Insufficient stock items <br><br>"+flashmsg.toString());
									break;
									}
								if(status){
									BasicDynaBean testBean = testPrescDAO.getBean();
									testBean.set("stock_reduced", true);
									status &= testPrescDAO.update(con, testBean.getMap(), "prescribed_id", conducted.getPrescribedId()) > 0 ;
								}
							}else{
								List reagents = LaboratoryDAO.getDiagReagentsUsed(conducted.getPrescribedId());
								List<DynaBean> reagentsRequired = new ArrayList<DynaBean>();
								DynaBeanBuilder builder = new DynaBeanBuilder();
								builder.add("item_id", Integer.class)
									.add("qty",BigDecimal.class)
									.add("redusing_qty",BigDecimal.class);
								DynaBean reagentsbean = builder.build();
								for(int j = 0;j<reagents.size();j++){
									BasicDynaBean reagentsUsed = (BasicDynaBean)reagents.get(j);
									reagentsbean = builder.build();
									reagentsbean.set("item_id",reagentsUsed.get("reagent_id"));
									reagentsbean.set("redusing_qty", (BigDecimal)reagentsUsed.get("qty"));
									reagentsbean.set("qty", (BigDecimal)reagentsUsed.get("qty"));
									reagentsRequired.add(reagentsbean);
								}
								status =  StoreItemStock.updateReagents(con, conducted.getTestId(), conducted.getPrescribedId(), userName, storeId,reagentsRequired,0, "diagnostics", flashmsg);
								if(status){
									BasicDynaBean testBean = testPrescDAO.getBean();
									testBean.set("stock_reduced", true);
									status &= testPrescDAO.update(con, testBean.getMap(), "prescribed_id", conducted.getPrescribedId()) > 0 ;
								}else{
									if(!"".equals(flashmsg.toString()))
										flash.info("Insufficient stock items <br><br>"+flashmsg.toString());
									break;
								}
							}
						}
					}

					status = (ResourceDAO.updateAppointments(con, conducted.getPrescribedId(),"DIA"));
				}
			}
			if(null != deletedNewTestDetailsId) {
				for(int l=0; l<deletedNewTestDetailsId.length; l++) {
					if(deletedNewTestDetailsId[l]!=null && !"".equals(deletedNewTestDetailsId[l])) {
						new GenericDAO("test_details").delete(con, "test_details_id", Integer.parseInt(deletedNewTestDetailsId[l]));
					}
				}
			}
			// doctor payments
			Iterator<TestConducted> tcItr = tcList.iterator();
			while (tcItr.hasNext()) {
				TestConducted tc = tcItr.next();

				// set the conduction status and doctor Id in the activity/charge
				// If the test conduction status is Not conducted or In-Progress i.e (N/P) then marked the
				// activity as not conducted in bill_activity_charge.
				int prescribedId = tc.getPrescribedId();
				BillActivityChargeDAO.updateActivityDetails(con, "DIA", String.valueOf(prescribedId),
						tc.getConductedBy(),
						(tc.getTestConducted().equals("N") || tc.getTestConducted().equals("P")) ? "N" : "Y",
						new java.sql.Timestamp(tc.getConductedDate().getTime()), userName);

				// re-calculate the payout amounts.
				String chargeId = BillActivityChargeDAO.getChargeId("DIA", prescribedId);
				if (chargeId != null)	// can be null for re-conduct tests
					PaymentEngine.updateAllPayoutAmounts(con, chargeId);
			}
		} finally{
			//DataBaseUtil.commitClose(con, status);
		}
		return status;
	}


	public String getTemplateContent(String templateid,String prescribedid,String testDetailsId)throws SQLException{
		String template = null;
		Connection con = DataBaseUtil.getConnection();
		try{
			template = dao.getTemplateContent(con,templateid,prescribedid,testDetailsId);
		}finally{
			con.close();
		}

		return template;
	}


	public List getTestConfirmList(String mrno, String department, String testid, String sortFeild, String sortOrder)throws SQLException{
		return dao.getTestConfirmList(mrno, department, testid, sortFeild, sortOrder);
	}

	public List getTestNames( String category)throws Exception{
		return dao.getTestNames(category);
	}

	public static final String HOSPITAL_TEST = "h";
	public static final String INCOMING_TEST = "i";
	public static final String OUTHOUST_TEST = "o";


	public static class Report {
		String reportId;
		// To avoid parsing Exceptions lets Keep reportId as String
		String reportName;
		List<TestClass> testList;
		String hasData;//indicates where report has data or null
		String signOff;
		String canEditOption;
		String canSigOffOption;
		String handedOver;
		String addendumSignoff;
		String outSourceDest;
		String outSourceDestType;
		String sampleStatus;
		String sFlag;


		public String getOutSourceDest() {
			return outSourceDest;
		}
		public void setOutSourceDest(String outSourceDest) {
			this.outSourceDest = outSourceDest;
		}
		public String getOutSourceDestType() {
			return outSourceDestType;
		}
		public void setOutSourceDestType(String outSourceDestType) {
			this.outSourceDestType = outSourceDestType;
		}
		public String getAddendumSignoff() {
			return addendumSignoff;
		}
		public void setAddendumSignoff(String addendumSignoff) {
			this.addendumSignoff = addendumSignoff;
		}
		public String getCanSigOffOption(){return canSigOffOption;}
		public void setCanSigOffOption(String canSigOffOption) {this.canSigOffOption = canSigOffOption;}

		public String getCanEditOption() {return canEditOption;}
		public void setCanEditOption(String canEditOption) {this.canEditOption = canEditOption;}

		public String getSignOff() {return signOff;}
		public void setSignOff(String signOff) {this.signOff = signOff;}

		public Report(String reportId,String reportName,String hasData){
			this.reportId = reportId;
			this.reportName = reportName;
			this.hasData=hasData;
			this.testList = new ArrayList<TestClass>();
		}

		public String getReportId() {return reportId;}
		public void setReportId(String reportId) {this.reportId = reportId;}

		public String getReportName() {return reportName;}
		public void setReportName(String reportName) {this.reportName = reportName;}

		public List<TestClass> getTestList() {return testList;}
		public void setTestList(List<TestClass> testList) {this.testList = testList;}

		public String getHasData() {
			return hasData;
		}

		public void setHasData(String hasData) {
			this.hasData = hasData;
		}
		public String getHandedOver() {
			return handedOver;
		}
		public void setHandedOver(String handedOver) {
			this.handedOver = handedOver;
		}
		public String getSampleStatus() {
			return sampleStatus;
		}
		public void setSampleStatus(String sampleStatus) {
			this.sampleStatus = sampleStatus;
		}
		public String getSFlag() {
			return sFlag;
		}
		public void setSFlag(String flag) {
			sFlag = flag;
		}

	}

	public static class TestClass {
		String testId;
		String testName;
		String conducted;
		String sampleCollectionFlag;
		int prescribedId;
		String priority;
		String sampleNeeded;
		String ddeptId;
		String isBillPaid;
		String canEditOption;
		String sampleId;
		String sampleStatus;
		String houseStatus;
		String outHouseName;
		String outHouseId;
		String outhouse_sampleno;
		Timestamp presDateTime;
		String remarks; // remarks entered during prescription.
		boolean reConduct = false;
		boolean collectSample = false;
		boolean assignOuthouse = false;
		String labNO;
		int commonOrderID;
		String sFlag;



		public boolean isReConduct() {return reConduct;}
		public void setReConduct(boolean reConduct) {this.reConduct = reConduct;}

		public String getOuthouse_sampleno() {return outhouse_sampleno;}
		public void setOuthouse_sampleno(String outhouse_sampleno) {this.outhouse_sampleno = outhouse_sampleno;}

		public String getOutHouseId() {return outHouseId;}
		public void setOutHouseId(String outHouseId) {this.outHouseId = outHouseId;}

		public String getOutHouseName() {return outHouseName;}
		public void setOutHouseName(String outHouseName) {this.outHouseName = outHouseName;}

		public String getHouseStatus() {return houseStatus;}
		public void setHouseStatus(String houseStatus) {this.houseStatus = houseStatus;}

		public String getSampleStatus() {return sampleStatus;}
		public void setSampleStatus(String sampleStatus) {this.sampleStatus = sampleStatus;}

		public String getSampleId() {return sampleId;}
		public void setSampleId(String sampleId) {this.sampleId = sampleId;}

		public String getCanEditOption() {return canEditOption;}
		public void setCanEditOption(String canEditOption) {this.canEditOption = canEditOption;}

		public String getIsBillPaid() {return isBillPaid;}
		public void setIsBillPaid(String isBillPaid) {this.isBillPaid = isBillPaid;}

		public String getConducted() {return conducted;}
		public void setConducted(String conducted) {this.conducted = conducted;}

		public String getDdeptId() {return ddeptId;}
		public void setDdeptId(String ddeptId) {this.ddeptId = ddeptId;}

		public int getPrescribedId() {return prescribedId;}
		public void setPrescribedId(int prescribedId) {this.prescribedId = prescribedId;}

		public String getPriority() {return priority;}
		public void setPriority(String priority) {this.priority = priority;}

		public String getSampleCollectionFlag() {return sampleCollectionFlag;}
		public void setSampleCollectionFlag(String sampleCollectionFlag) {this.sampleCollectionFlag = sampleCollectionFlag;}

		public String getSampleNeeded() {return sampleNeeded;}
		public void setSampleNeeded(String sampleNeeded) {this.sampleNeeded = sampleNeeded;}

		public String getTestId() {return testId;}
		public void setTestId(String testId) {this.testId = testId;}

		public String getTestName() {return testName;}
		public void setTestName(String testName) {this.testName = testName;}

		public Timestamp getPresDateTime() {return presDateTime; }
		public void setPresDateTime(Timestamp presDateAndTime) { this.presDateTime = presDateAndTime; }

		public String getRemarks() { return remarks; }
		public void setRemarks(String remarks) { this.remarks = remarks; }

		public boolean getCollectSample() { return collectSample; }
		public void setCollectSample(boolean collectSample) { this.collectSample = collectSample; }

		public boolean getAssignOuthouse() { return assignOuthouse; }
		public void setAssignOuthouse(boolean assignOuthouse) { this.assignOuthouse = assignOuthouse; }

		public String getLabNO() { return labNO; }
		public void setLabNO(String labNo) { this.labNO = labNo; }

		public int getCommonOrderID() { return commonOrderID; }
		public void setCommonOrderID(int commonOrderID) { this.commonOrderID = commonOrderID; }
		public String getSFlag() {
			return sFlag;
		}
		public void setSFlag(String flag) {
			sFlag = flag;
		}
	}


	/*
	 * order by pres_date is slowing down the query.
	 * because of the performance issue with single query, we split this into two queries.
	 *
	 * 1) first query retrieves only the prescribed id's by looking at the tables which are mandatory ignoring
	 * 		the bill_activity_charge, bill, bill_charge, and test_visit_reports.
	 * 2) second query gets the test_details without applying any order by clause and joing with all other tables.
	 *
	 * Refer BUG : 24296
	 */
	public static PagedList unfinishedTestsList(Map filterParams, Map listingParams, BasicDynaBean diagGenericPref,int centerId, String[] origsampleSnosArray)
		throws SQLException, IOException, ParseException {

		Boolean hasSampleFlow = ((String) diagGenericPref.get("sampleflow_required")).equals("Y");
		// prescribed id's retreived using order by clause.
		PagedList pagedListPrescId = LaboratoryDAO.getDiagSchedulesPresIds(filterParams, listingParams, origsampleSnosArray);
		if (pagedListPrescId.getDtoList().size() > 0) {
			ArrayList idList = new ArrayList();
			List dtoList = pagedListPrescId.getDtoList();
			// iterate over all the prescribed ids.
			for (int i=0; i<dtoList.size(); i++) {
				idList.add(((BasicDynaBean) dtoList.get(i)).get("prescribed_id"));
			}
			// pass the prescribed id's and get the individual test details. without order by.
			PagedList pagedListTestDetails = LaboratoryDAO.unfinishedTestsList(filterParams, listingParams, idList, origsampleSnosArray);

			Map<Object, BasicDynaBean> mappedList = ConversionUtils.listBeanToMapBean(pagedListTestDetails.getDtoList(), "prescribed_id");
			List modifiedList = new ArrayList();

			/*
			 * the actual test details exists in the second pagedlist but it is not ordered.
			 * so to retain the same order iterate over the prescribedIds list and get test details for that
			 * particular prescribed id from testdetails dto list. and set it to prescribed ids dto list.
			 */
			for (int i=0; i<idList.size(); i++) {
				int prescId = (Integer) idList.get(i);

				BasicDynaBean bean = (BasicDynaBean) mappedList.get(prescId);
				if (bean != null) {
					Map test = new HashMap(bean.getMap());
					String billType = (String) test.get("bill_type");
					String paymentStatus = (String) test.get("payment_status");
					String patientFrom = (String) test.get("hospital");
					boolean billPaid = true;
					boolean canEdit = false;
					boolean collectSample = false;
					boolean assignOuthouse = false;

					if ( paymentStatus !=null && paymentStatus.equals("U")) {
						if (billType.equals("P"))
							billPaid = false;
					}
					test.put("billPaid", billPaid);
					String sampleNeeded = (String) test.get("sample_needed");
					String houseStatus = (String) test.get("house_status");
					Boolean sampleCollected = ((String) test.get("sflag")).equals("1") ;
					Boolean resultEntryApplicable = !((String)test.get("conducted")).equalsIgnoreCase("NRN") &&
					!((String)test.get("conducted")).equalsIgnoreCase("CRN") ;
					if (hasSampleFlow || houseStatus.equals("O")) {
						if (sampleNeeded.equals("y")) {
							if (sampleCollected) {
								canEdit = true;
							} else {
								collectSample = true;
							}
						} else {
							if (!houseStatus.equals("O")) {
								canEdit = true;
							} else if (houseStatus.equals("O") &&
									((String) test.get("is_outhouse_selected")).equals("Y")) {
								canEdit = true;
							}
						}

					} else {
						canEdit = true;
					}

					if ((sampleNeeded.equals("n") || (sampleNeeded.equals("y") && patientFrom.equals("incoming"))) &&
							houseStatus.equals("O") &&
							((String) test.get("is_outhouse_selected")).equals("N")){
						assignOuthouse = true;
					}

					DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
					ArrayList reagentBean = (ArrayList)dao.findTestBykey(test.get("test_id").toString());
					if(reagentBean.size()==0) {
						test.put("reagentExists", false);
						test.put("storeExist", false);
					} else {
						test.put("reagentExists", true);
						Boolean exist = new DiagnosticDepartmentMasterDAO().isDiagDeptStoreExist(test.get("ddept_id").toString(), centerId);
						test.put("storeExist", exist);
					}

					test.put("billPaid", billPaid);
					test.put("collectSample", collectSample);
					test.put("canEdit", canEdit);
					test.put("assignOuthouse", assignOuthouse);
					test.put("resultEntryApplicable", resultEntryApplicable);
					modifiedList.add(test);
				}
			}
			pagedListPrescId.setDtoList(modifiedList);
		}
		return pagedListPrescId;

	}


	public static ArrayList<Report> gethierarchicalStructer(List l) throws Exception{
		ArrayList<Report> rList = new ArrayList<Report>();
		Iterator<Hashtable<String, String>> it = l.iterator();
		String prevReportId = null;
		Report report = null;
		TestClass test = null;
		while(it.hasNext()){
			Hashtable<String, String> ht = it.next();

			if ( ht.get("REPORT_STATE").equals("D") )
				continue;//discarded reports are not needed in manage reports
			String reportId = ht.get("REPORT_ID");

			if(!reportId.equals(prevReportId)){
				String reportName = ht.get("REPORT_NAME");
				String hasData=ht.get("REPORT_DATA");
				if(reportName.equals("") ){reportName = "No Report";}
				report = new Report(reportId,reportName,hasData);
				report.setSignOff(ht.get("SIGNED_OFF"));
				report.setHandedOver(ht.get("HANDED_OVER"));
				report.setAddendumSignoff(ht.get("ADDENDUM_SIGNED_OFF"));
				report.setOutSourceDestType(ht.get("OUTSOURCE_DEST_TYPE"));
				report.setOutSourceDest(ht.get("OUTSOURCE_DEST"));
				report.setSampleStatus(ht.get("SAMPLE_STATUS"));
				report.setSFlag(ht.get("SFLAG"));
				rList.add(report);
				prevReportId = reportId ;

			}

			test = new TestClass();
			test.testName = ht.get("TEST_NAME");
			String conducted = ht.get("CONDUCTED");
			test.conducted = conducted;
			if (ht.get("COMMON_ORDER_ID") != null && !ht.get("COMMON_ORDER_ID").equals(""))
				test.commonOrderID = Integer.parseInt(ht.get("COMMON_ORDER_ID"));
			test.labNO = ht.get("LABNO");
			if (ht.get("PRES_DATE") != null && !ht.get("PRES_DATE").equals("")) {
				test.presDateTime = new Timestamp(new DateUtil().getSqlTimeStampFormatter().parse(ht.get("PRES_DATE")).getTime());
			}
			report.testList.add(test);
		}
		return rList;
	}


	public static boolean saveReportPrescriptions( ArrayList<TestVisitReports> tvrList,
			ArrayList<TestPrescribed> tpList,String reportIdsForRegeneration, String userName,
			boolean updatePHTemplate, Integer pheader_template_id,String cPath)throws Exception{

		boolean status = false;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		List<Integer> newReportIds = new ArrayList<Integer>();
		String visitId = null;
		int index = 0;

		try {
			do {
				Iterator<TestVisitReports> tvIterator = tvrList.iterator();
				Iterator<TestPrescribed> tpIterator = tpList.iterator();
				String prevReportName = "";
				String reportName = "";
				int reportId = 0;

				while(tvIterator.hasNext() && tpIterator.hasNext()){
					TestVisitReports tv = tvIterator.next();
					TestPrescribed tp = tpIterator.next();
					visitId = tv.getVisitId();
					// For NEW report generation will be allowed if there is test details for the prescribedID, 
					//this is to avoid new report generation from the manage report screen.
					if (!DiagnosticsDAO.isTestDetailsExist(con, tp.getPrescribedId()))
						continue;
					
					if ( tv.getReportName().startsWith("NEW")) {
						//new reports;
						reportName = tv.getReportName();
						if( !prevReportName.equals(reportName) )
							reportId = DiagnosticsDAO.getReportId(tv);
						tv.setReportId(reportId);
						prevReportName = tv.getReportName();
						tp.setReportId(reportId);


					} else if ( tv.getReportName().equals("N") ) {
						tp.setReportId(0);
					} else {
						int reportId1 = DiagnosticsDAO.getReportId(tv);
						tv.setReportId(reportId1);
						tp.setReportId(reportId1);
					}

					status = DiagnosticsDAO.updateReportPrescription(con,tp);
					if(!status)break;

					if ( !tv.getReportName().equals("N") ) {
						//	updating abnormal report status
						status = DiagnosticsDAO.insertOrUpdateReport(con, tv);
					        if(!status)break;
						      newReportIds.add(reportId);
					}

				}
				status = true;
			} while(false);

		} finally {
			 if ( con != null )
				 con.commit();
		}

		//updating report status
		Iterator<TestVisitReports> tvIterator = tvrList.iterator();
		try{
			while( tvIterator.hasNext() ){
				TestVisitReports tv = tvIterator.next();

				if ( tv.getReportId() == 0 )
					continue;//possible for test with "No Report" option
				List<BasicDynaBean> nonValueBasedTestsOfTheReport  =
						diagDao.getNonValueBasedTestDetailsOfReport(con, tv.getReportId());

				Map<String,String> keys = new HashMap<String, String>();
				String severityStatus = "A";
				if (null == nonValueBasedTestsOfTheReport || nonValueBasedTestsOfTheReport.isEmpty())
					severityStatus = getSeverityStatus(con, tv.getReportId());
				keys.put("report_results_severity_status",(nonValueBasedTestsOfTheReport != null 
				    && nonValueBasedTestsOfTheReport.size() > 0) ? "T" : severityStatus);//for value based tests

				//update
				status &= testVisitReportsDao.update(con, keys, "report_id",tv.getReportId()) > 0;

			}
		}finally{
			DataBaseUtil.commitClose(con, status);
		}

		if(reportIdsForRegeneration != null){
			String oldReportIds[] = reportIdsForRegeneration.split(",");
			for(int j=0;j<oldReportIds.length;j++){
				if( !oldReportIds[j].equals("") && !oldReportIds[j].equals("NO") ){
					newReportIds.add(Integer.parseInt(oldReportIds[j]));
				}
			}
		}


		if(status){
			//Regenerate changed reports Only;
			for(int i=0 ; i<newReportIds.size() ; i++ ){
				if(newReportIds.get(i) != 0){
					String html = DiagReportGenerator.getReport(newReportIds.get(i),userName,cPath,false, false);
					status = DiagnosticsDAO.setReportContent(newReportIds.get(i), visitId, userName,
							updatePHTemplate, pheader_template_id);
				}
			}
		}


		return status;
	}
	
	private static String getSeverityStatus(Connection con, int reportID) throws SQLException {
		List<BasicDynaBean> testSeverities = diagDao.reportHasAnyAbnormalResults(con, reportID);
		String reportStatus = "A";
		if (null != testSeverities && testSeverities.size() > 0) {
			for (int i=0; i<testSeverities.size(); i++) {
				BasicDynaBean bean = testSeverities.get(i);
				String severityStatus = (String)bean.get("withinnormal");
				if (severityStatus.equals("**") || severityStatus.equals("##")) {
					reportStatus = "C";
					break;
				} else if (severityStatus.equals("*") || severityStatus.equals("#")) {
					reportStatus = "H";
				}
			}
		}
		return reportStatus;
	}

	public static boolean signofReports(ArrayList<TestVisitReports> signOfList ,Preferences pref, String userName, int center_id, HashMap map)
			throws SQLException, IOException,Exception {

		Connection con = null;
		boolean status = true;
		con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String modConsumableActive = "Y";
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			modConsumableActive = (String)pref.getModulesActivatedMap().get("mod_consumables_flow");
            if(modConsumableActive == null || "".equals(modConsumableActive)){
            	modConsumableActive = "N";
            }
        }
		ArrayList<Integer> notConductedPresIds = new ArrayList<Integer>();
		ArrayList<TestVisitReports> validsignOffList = new ArrayList<TestVisitReports>();
		try {
			for(int i =0;i<signOfList.size();i++){
				String visitID = null;
				TestVisitReports reports = signOfList.get(i);
				String severityVal = LaboratoryDAO.getSeverityResultByReportId(con, reports.getReportId());
				if (severityVal != null) {
					map.put(reports.getReportId(), severityVal);
					continue;
				}
				validsignOffList.add(reports);
				
				List<BasicDynaBean> testlist = LaboratoryDAO.getTestList(reports.getReportId());
				for(int j = 0;j<testlist.size();j++) {
					BasicDynaBean test = testlist.get(j);
					visitID = (String)test.get("pat_id");
					BasicDynaBean testPrescribedBean = testPrescDAO.findByKey("prescribed_id", (Integer)test.get("prescribed_id"));
					int storeId = DiagnosticDepartmentMasterDAO.getStoreOfDiagnosticDepartment( (String) test.get("test_id"), center_id);
					if(!((String)test.get("conducted")).equals("C") && modConsumableActive.equals("Y")){
						if(!(Boolean)testPrescribedBean.get("stock_reduced")){
						if(diagReagentDAO.findByKey("prescription_id", (Integer)test.get("prescribed_id"))== null){
							status =  StoreItemStock.updateReagents(con, (String)test.get("test_id"), ((Integer)test.get("prescribed_id")), userName, storeId,null,0, "diagnostics");
							if(!status)break;
							if(status){
								BasicDynaBean testBean = testPrescDAO.getBean();
								testBean.set("stock_reduced", true);
								testBean.set("user_name", userName);
								status &= testPrescDAO.update(con, testBean.getMap(), "prescribed_id",
										test.get("prescribed_id")) > 0;
							}
						}else{
							 List reagents = LaboratoryDAO.getDiagReagentsUsed(((Integer)(test.get("prescribed_id"))).intValue());
							 List<DynaBean> reagentsRequired = new ArrayList<DynaBean>();
							 DynaBeanBuilder builder = new DynaBeanBuilder();
								builder.add("item_id", Integer.class)
								.add("qty",BigDecimal.class)
								.add("redusing_qty",BigDecimal.class);
							 DynaBean reagentsbean = builder.build();
							 for(int k = 0;k<reagents.size();k++){
								BasicDynaBean reagentsUsed = (BasicDynaBean)reagents.get(k);
								reagentsbean = builder.build();
								reagentsbean.set("item_id",reagentsUsed.get("reagent_id"));
								reagentsbean.set("redusing_qty", (BigDecimal)reagentsUsed.get("qty"));
								reagentsbean.set("qty", (BigDecimal)reagentsUsed.get("qty"));
								reagentsRequired.add(reagentsbean);
							 }
							 status =  StoreItemStock.updateReagents(con, (String)test.get("test_id"),
									 ((Integer)(test.get("prescribed_id"))).intValue(), userName, storeId,reagentsRequired,0, "diagnostics");
							 if(status){
									BasicDynaBean testBean = testPrescDAO.getBean();
									testBean.set("stock_reduced", true);
									testBean.set("user_name", userName);
									status &= testPrescDAO.update(con, testBean.getMap(), "prescribed_id",
											test.get("prescribed_id")) > 0;
								}
						 }
						}
					}

					if (!test.get("conducted").equals("C") && !test.get("conducted").equals("V")) {
						notConductedPresIds.add((Integer)test.get("prescribed_id"));
					}

					// inserting signature row for a test.
					status &= sigDAO.insertSignatureRecord(con, reports.getReportId(),
							(Integer) test.get("prescribed_id"), userName, "D", (String) test.get("conducted_by"));

					String technician = (String) test.get("technician");
					if (technician == null || technician.equals("")) {
						status &= LaboratoryDAO.updateTechnician(userName, (Integer) test.get("prescribed_id"));
						technician = userName;
					}

					status &= sigDAO.insertSignatureRecord(con, reports.getReportId(), (Integer) test.get("prescribed_id"),
							userName, "T", technician);

				}
				List<BasicDynaBean> transferToPatientList =
						ohSampleRegistrationDao.getSourcePrescribedIDS(con, reports.getReportId(), visitID);
				Map<String, Object> columnData = new HashMap<String, Object>();
				columnData.put("conducted", "S");
				columnData.put("report_id", reports.getReportId());
				status &= LaboratoryBO.copyDataToMultipleChains(con, transferToPatientList, columnData, "source_test_prescribed_id", null);
			}
			

			if(status)
				status = DiagnosticsDAO.upDateSignofReports(con, validsignOffList, userName);


			for (Integer prescribedId : notConductedPresIds) {
				// re-calculate the payout amounts since conduction is now set to Y.
				// because of sign-off. No need to worry about updating conducted by, since that
				// can only be done in the conduction UI.
				BillActivityChargeDAO.updateActivityConducted(con, "DIA", String.valueOf(prescribedId), "Y");
				String chargeId = BillActivityChargeDAO.getChargeId("DIA", prescribedId);
				if (chargeId != null)	// can be null for re-conduct tests
					PaymentEngine.updateAllPayoutAmounts(con, chargeId);
			}

		} finally {
			if (status) con.commit();
			else con.rollback();
			con.close();
		}

		return status;
	}


	public static boolean editReport(TestVisitReports tvr)throws Exception{
		boolean status = false;
		Connection con = null;
		try{
			do{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				status = LaboratoryDAO.editReport(con,tvr);
				if(!status)break;

			}while(false);
		}finally{
			if(con!=null){
				if(status)con.commit();
				else con.rollback();

				con.close();
			}
		}
		return status;
	}


	public static boolean uploadImage(TestReportImages trm)throws Exception{
		boolean status = false;
		Connection con = null;

		try{
			do{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				status = LaboratoryDAO.uploadImage(con,trm);
				if(!status)break;

			}while(false);
		}finally{
			if(con!=null){
				if(status)con.commit();
				else con.rollback();

				con.close();
			}
		}
		return status;
	}

	public static boolean deleteImage(TestReportImages trm)throws Exception{
		boolean status = false;
		Connection con = null;
		try{
		do{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			status = LaboratoryDAO.deleteImage(con,trm);

		}while(false);
		}finally{
			if(con!=null){
				if(status)con.commit();
				else con.rollback();
				con.close();
			}
		}
		return status;
	}

	public static boolean saveTemplateReport(Connection con, int presId, String fmtId, String content,
			int testDetailsId,List newTestDetailsId,String testId)
	throws SQLException {
		
		boolean status = false;		
		status = DiagnosticsDAO.updateReportFormat(con, presId, fmtId, content,testDetailsId,newTestDetailsId,testId);
		
		return status;
	}




	public static class Samples{
		public String speciman;
		public List<TestClass> testList;
		public String hasOuthouseOption;

		public Samples(String speciman){
			this.speciman = speciman;
			testList = new ArrayList<TestClass>();
		}

		public String getHasOuthouseOption() {return hasOuthouseOption;}
		public void setHasOuthouseOption(String hasOuthouseOption) {this.hasOuthouseOption = hasOuthouseOption;}

		public String getSpeciman() {return speciman;}
		public void setSpeciman(String speciman) {this.speciman = speciman;}

		public List<TestClass> getTestList() {return testList;}
		public void setTestList(List<TestClass> testList) {this.testList = testList;}

	}

	public static boolean saveOuthouses(ArrayList<SampleCollection> scList,
			ArrayList<OutHouseSampleDetails> ohlist, String category, int centerId)
	throws Exception {

		boolean status = false;
		String chargeHead=null;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		Iterator<SampleCollection> it = null;
		Iterator<OutHouseSampleDetails> ohit = null;
		boolean allSuccess = false;
		
		try {
			if (scList != null)
				it = scList.iterator();
	
			if (ohlist != null)
				ohit = ohlist.iterator();
	
			if (category.equals("DEP_LAB")) {
				chargeHead = ChargeDTO.CH_DIAG_LAB;
			} else {
				chargeHead = ChargeDTO.CH_DIAG_RAD;
			}
	
			if (ohit != null && it != null) {
				while (it.hasNext()) {
					SampleCollection sc = it.next();
					status = LaboratoryDAO.insertSample(con, sc,centerId);
				}
				OutHouseSampleDetails osd = null;
				if(status){
				while (ohit.hasNext()) {
					osd = ohit.next();
					status &= LaboratoryDAO.setSamplesToOuthouse(osd, con);
					if (status) {
						ChargeBO chargeBo = new ChargeBO();
						status &= LaboratoryDAO.updateOuthouseFlag(con, osd.getPrescribedId());
						String chargeId = LaboratoryDAO.getOhTestChargeId(con, osd.getPrescribedId(),chargeHead);
						   status &= chargeBo.updateOhPayment(con,chargeId, centerId);
					  }
					}
				} else {
					status = false;
				}
			}
			
			allSuccess = true;
			allSuccess &= status;
			
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		return allSuccess;
	}
	/**
	 * saves reagents used for a test Temporaryly
	 * which will be saved into inventroy usage tables
	 * on completion of the test
	 * @param con
	 * @param testId
	 * @param prescriptionId
	 * @param usageSeqNo
	 * @param reagentsRequired
	 * @param pref
	 * @return
	 * @throws Exception
	 */
	public boolean saveDiagReagentUsage(Connection con,String testId,int prescriptionId,
			String[] usageSeqNo,List reagentsRequired,Preferences pref)throws Exception{
		String invModAct = null;
		boolean status = true;
		Map<String, Object> keys = new HashMap<String, Object>();
		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			invModAct = (String)pref.getModulesActivatedMap().get("mod_stores");
			if(invModAct == null ||  "".equals(invModAct)){
				invModAct = "N";
			}
		}
		int noOfReagents = 0;
		int[] reagentRequired = new int[reagentsRequired.size()];
		BigDecimal[] qtyRequired = new BigDecimal[reagentsRequired.size()];
		BigDecimal[] actualQty = new BigDecimal[reagentsRequired.size()];
		if(invModAct != null && invModAct.equals("Y")){
			for(int i = 0;i<reagentsRequired.size();i++){
				noOfReagents = reagentsRequired.size();
				DynaBean regaents = (DynaBean)reagentsRequired.get(i);
				reagentRequired[i] = (Integer)regaents.get("item_id");
				qtyRequired[i] = (BigDecimal)regaents.get("redusing_qty");
				actualQty[i] = (BigDecimal)regaents.get("qty");
			}
			if(new GenericDAO("diagnostics_reagents").findByKey("test_id", testId) != null){

					BasicDynaBean diagReagentBean = diagReagentDAO.getBean();
					if(usageSeqNo[0].isEmpty()){
						for ( int reagent=0; reagent < noOfReagents; reagent++ ) {
						diagReagentBean.set("test_id", testId);
						diagReagentBean.set("reagent_id", (Integer)reagentRequired[reagent]);
						diagReagentBean.set("prescription_id", prescriptionId);
						diagReagentBean.set("usage_no", diagReagentDAO.getNextSequence());
						diagReagentBean.set("qty",(BigDecimal)actualQty[reagent]);
						status &= diagReagentDAO.insert(con, diagReagentBean);
						}
					}else{
						for(int i = 0;i<reagentsRequired.size();i++){
							DynaBean regaents = (DynaBean)reagentsRequired.get(i);
							keys.put("usage_no", Integer.parseInt(usageSeqNo[i]));
							keys.put("prescription_id", prescriptionId);
							diagReagentBean.set("qty", (BigDecimal)regaents.get("qty"));
							status &= diagReagentDAO.update(con, diagReagentBean.getMap(), keys) > 0;
						}
					}
				}
			}
		return status;
	}

	private static final String UPDATE_OUTSOURCE_DEST_ID = "UPDATE tests_prescribed set outsource_dest_id = ? " +
			" WHERE prescribed_id = ? and pat_id = ?";

	public static boolean updateOutsourceInTestPres(int outSourceDestId,int prescId, String visitId)
	throws SQLException {

		boolean status = false;
		try (Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(UPDATE_OUTSOURCE_DEST_ID);){
			ps.setInt(1,outSourceDestId);
			ps.setInt(2, prescId);
			ps.setString(3, visitId);
			ps.executeUpdate();		
		}
		return status;
	}
	
	// this method is used from triggering an email when report gets signed off.
	// Web Diag Printer configurations are used commonly for email and web based prints.
	public static byte[] convertReportHtmlToPdf(int reportId, String report, HtmlConverter hc) throws XPathExpressionException, IOException,
	                 SQLException, DocumentException, ParseException {

		BasicDynaBean visitReportBean  = DiagnosticsDAO.getReportDynaBean( reportId );

		String patientId = null;
		String category=null;
		boolean signedOff = false;
		boolean isDuplicate = false;
		FtlReportGenerator ftlGen = null;
		String reportContent = report;
		
		patientId = (String)visitReportBean.get("patient_id");
		category= (String)visitReportBean.get("category");
		signedOff = visitReportBean.get("signed_off").equals("Y");
		isDuplicate = visitReportBean.get("handed_over").equals("Y");
		int center_id = RequestContext.getCenterId();
		
		Map map = LaboratoryBO.getReportParams(reportId);
		if (null != map && null != map.get("patient_id") && !map.get("patient_id").equals("") 
				&& null != map.get("center_id") && !map.get("center_id").equals("")) {
			patientId = (String)map.get("patient_id");
			center_id = (Integer)map.get("center_id");
		}
		
		if (reportContent == null) {
			reportContent = "Report is Not Availble";
		}
		BasicDynaBean printPrefs= null;

		printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG, 0, center_id);
		
		PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
        PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();
        PrintTemplate template = category.equals("DEP_LAB") ? PrintTemplate.WebLab : PrintTemplate.WebRad;
        String patientHeader = phTemplateDAO.getPatientHeader( (Integer) printTemplateDao.getPatientHeaderTemplateId(template),
        		PatientHeaderTemplate.WebBased.getType()); 
        
		ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
		Map ftlParams = new HashMap();
		ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
		StringWriter writer = new StringWriter();
		try {
		//	t.process(ftlParams, writer);
			ftlGen.setReportParams(ftlParams);
			ftlGen.process(writer);
		} catch (TemplateException te) {
			logger.debug("Exception raised while processing the patient header for report Id : "+reportId);
		}
		StringBuilder printContent = new StringBuilder();
		printContent.append(writer.toString());
		printContent.append(reportContent);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

		hc.writePdf(os, printContent.toString(), "Investigation Report", printPrefs, false,
				repeatPatientHeader, true, true, signedOff,isDuplicate, center_id);
		return os.toByteArray();
		
	}
	
	//This function will fetch the patient id and center id for the patient by report id, It is useful in internal lab case, why because
	//center id and patient id we need to get from collection center not from conduction center when they signoff in the conduction center
	
	public static Map getReportParams(int reportID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		BasicDynaBean bean = null;
		BasicDynaBean incomingPatientBean = null;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(DiagnosticsDAO.REPORT);
			pstmt.setInt(1, reportID);
			bean = DataBaseUtil.queryToDynaBean(pstmt);
			if (null != bean && null != bean.get("patient_id") && !bean.get("patient_id").equals("")) {
				pstmt = con.prepareStatement(DiagnosticsDAO.INCOMING_PATIENT_DETAILS);
				pstmt.setInt(1, reportID);
				incomingPatientBean = DataBaseUtil.queryToDynaBean(pstmt);
				if (null != incomingPatientBean) {
					if (incomingPatientBean.get("incoming_source_type").equals("C")) {
						map.put("center_id", incomingPatientBean.get("center_id"));
						map.put("patient_id", incomingPatientBean.get("pat_id"));
					} else {
						map.put("center_id", RequestContext.getCenterId());
						map.put("patient_id", incomingPatientBean.get("pat_id"));
					}
				} else {
					map.put("center_id", RequestContext.getCenterId());
					map.put("patient_id", bean.get("patient_id"));
				}
			}			
			
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
		return map;
	}
	
	
	/*This method will be helpful to set up the status for multiple chains of prescribed id
	 * Parameters:
	 * con: database connection
	 * Lists: dynaBeanlist
	 * columndata: what data you want to set, by passing key value pairs. (here key is column name)
	 * traverseColumn: on which column you want to traverse (ex: by source_prescribed_id OR outsource_prescribed_id).
	*/
	
	public static boolean copyDataToMultipleChains(Connection con, List<BasicDynaBean> lists, 
			Map<String, Object> columndata, String traverseColumn, List<String> columnNames)throws SQLException, IOException {

		boolean success = true;
		boolean continueLoop = true;
		HashMap<String, Integer> keys = new HashMap<String, Integer>();
		Integer traverseColumnValue = null;	
		
		DynaProperty property = null;
		Class classType = null;

		if (columndata == null)
			columndata = new HashMap<String, Object>();
		
		if (lists != null && !lists.isEmpty()) {

 mainloop:	for (BasicDynaBean bean : lists) {
				
				if (null != bean.get(traverseColumn)) {
					continueLoop = true;
					traverseColumnValue = (Integer)bean.get(traverseColumn);
					//if column data is null, we construct the column data by using column Names
					if (columnNames != null) {
						for (String columnName : columnNames) {
							property = bean.getDynaClass().getDynaProperty(columnName);
							classType = property.getType();
							if (classType == java.lang.String.class)
								columndata.put(columnName, (String)bean.get(columnName));
							if (classType == java.lang.Integer.class)
								columndata.put(columnName, (Integer)bean.get(columnName));
						}
					}
					
					do {
						keys.put("prescribed_id", traverseColumnValue);							
						success &= testPrescDAO.update(con, columndata, keys) > 0;
						if (!success)
							break mainloop;
						
						traverseColumnValue = (Integer)testPrescDAO.findByKey(con, "prescribed_id", traverseColumnValue).get(traverseColumn);
						if (null == traverseColumnValue)
							continueLoop = false;
						
					} while (continueLoop);
					
				}
			}
		}

	return success;
	}
	
	public static StringBuilder getImprobableResultValues(Connection con, int reportId) throws SQLException, IOException {
		StringBuilder builder = new StringBuilder(); 
		HashMap<String, String> updateValues = new HashMap<String, String>();
		List<BasicDynaBean> resultsList = LaboratoryDAO.getSeveritiesByReportId(con, reportId);
		Iterator<BasicDynaBean> trListIterator= resultsList.iterator();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update test_details set report_value = ?, withinnormal = ? where test_details_id = ?");
			while(trListIterator.hasNext()){
				BasicDynaBean td = trListIterator.next();
				BasicDynaBean testDetails = LaboratoryDAO.getExistingRecordsByTestdetailId(Integer.parseInt(td.get("test_details_id").toString()));
				if (testDetails != null) {
					updateValues.put("report_value", (String)testDetails.get("report_value"));
					updateValues.put("withinnormal", (String)testDetails.get("withinnormal"));
				} else {
					updateValues.put("report_value", "");
					updateValues.put("withinnormal", "");
				}
				ps.setString(1, updateValues.get("report_value"));
				ps.setString(2, updateValues.get("withinnormal"));
				ps.setInt(3, Integer.parseInt(td.get("test_details_id").toString()));
				ps.addBatch();
				builder.append("The Result label "+(String)td.get("resultlabel"));
				builder.append(" of test "+(String)td.get("test_name"));
				builder.append(" is having improbable value "+(String)td.get("report_value")+"<br/>");
			} 
			ps.executeBatch();
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return builder;
	}
	
	public static Map<String, Object> buildCriticalLabTestEventData(Map.Entry<String, Object> entry) throws SQLException, ParseException {
		List<DynaBeanMapDecorator> criticalReports = new ArrayList<DynaBeanMapDecorator>();
		HashMap<String, Object> criticalLabTokens = new HashMap<String, Object>();
		String pres_doctor = null;
			List resultLabels = new ArrayList();
			List reportValues = new ArrayList();
			List units = new ArrayList();
			List severity = new ArrayList();
			List referenceRanges = new ArrayList();

			String expectedDate = null;
			try {
			    criticalReports =  (List<DynaBeanMapDecorator>) entry.getValue();
			    for (DynaBeanMapDecorator criticalReportBean : criticalReports) {
			    	resultLabels.add((String) criticalReportBean.get("resultlabel"));
			    	reportValues.add((String) criticalReportBean.get("report_value"));
			    	units.add((String) criticalReportBean.get("units"));
			    	severity.add((String) criticalReportBean.get("severity"));
			    	referenceRanges.add((String) criticalReportBean.get("reference_range"));
			    }
			    if(criticalReports.get(0).get("pres_doctor") == null || criticalReports.get(0).get("pres_doctor").equals("")){
			      pres_doctor = (String) criticalReports.get(0).get("doctor") ;
			    } else {
			      pres_doctor = (String) criticalReports.get(0).get("pres_doctor");
			    }
			    
			    criticalLabTokens.put("doctor", pres_doctor);
			    criticalLabTokens.put("mr_no", (String) criticalReports.get(0).get("mr_no"));
			    criticalLabTokens.put("sample_date",criticalReports.get(0).get("sample_date") != null ?
			    	criticalReports.get(0).get("sample_date") : null);
			    criticalLabTokens.put("test_name", (String) criticalReports.get(0).get("test_name"));
			    criticalLabTokens.put("result_name", resultLabels);
			    criticalLabTokens.put("report_value", reportValues);
			    criticalLabTokens.put("units", units);
			    criticalLabTokens.put("severity", severity);
			    criticalLabTokens.put("reference_range", referenceRanges);
			    criticalLabTokens.put("receipient_id__", (String) criticalReports.get(0).get("mr_no"));
			    criticalLabTokens.put("visit_id", (String) criticalReports.get(0).get("pat_id"));
			    criticalLabTokens.put("bed", (String) criticalReports.get(0).get("bed_name"));
			    criticalLabTokens.put("ward", (String) criticalReports.get(0).get("ward_name"));
			    criticalLabTokens.put("visit_date", criticalReports.get(0).get("reg_date").toString());
			    criticalLabTokens.put("patient_name", (String) criticalReports.get(0).get("patient_name"));
			    criticalLabTokens.put("gender", (String) criticalReports.get(0).get("patient_gender"));
			    criticalLabTokens.put("patient_phone", (String) criticalReports.get(0).get("patient_phone"));
			    if (criticalReports.get(0).get("dateofbirth") != null) {
			    	expectedDate = criticalReports.get(0).get("dateofbirth").toString();
			    } else {
			    	expectedDate = criticalReports.get(0).get("expected_dob").toString();
			    }
				Map map = DateUtil.getAgeForDate(expectedDate.toString(), "yyyy-MM-dd");
				Object age = (map.get("age") != null) ? map.get("age").toString() : "";
				criticalLabTokens.put("age", criticalReports.get(0).get("age").toString()+" "+criticalReports.get(0).get("agein").toString());
			    Map<String, String> referralFilterMap = new HashMap<String, String>();
			    BasicDynaBean referralDetails = null;	
			    BasicDynaBean referalDoctorDetails = null;
			    BasicDynaBean referals = null;
				BasicDynaBean doctorDetails = 	new DischargeSummaryBOImpl().getDoctorDetails(pres_doctor);
				String referalDoctor = (String) (String) criticalReports.get(0).get("reference_docto_id");
				if (referalDoctor != null) {
					referralFilterMap.put("referal_no", referalDoctor);
					List<String> referralDetailsColumns = new ArrayList<String>();
					referralDetailsColumns.add("referal_doctor_email");
					referralDetailsColumns.add("referal_mobileno");
					referralDetailsColumns.add("referal_name");
					List<BasicDynaBean> referral_details = new ReferalDoctorDAO().listAll(referralDetailsColumns, referralFilterMap, null);
					if (referral_details.size() != 0) {
						 referralDetails = referral_details.get(0);
						criticalLabTokens.put("referral_mobile", (String) referralDetails.get("referal_mobileno"));
						criticalLabTokens.put("referral_email", (String) referralDetails.get("referal_doctor_email"));
						 criticalLabTokens.put("referal_name", (String) referralDetails.get("referal_name"));
					} else {
						 referalDoctorDetails = new DischargeSummaryBOImpl().getDoctorDetails(referalDoctor);
						if (referalDoctorDetails != null) {
							criticalLabTokens.put("referral_mobile", (String) referalDoctorDetails.get("doctor_mobile"));
							criticalLabTokens.put("referral_email", (String) referalDoctorDetails.get("doctor_mail_id"));
							criticalLabTokens.put("referal_name", (String) referalDoctorDetails.get("doctor_name"));
						}
					}
				}
				referals = referralDetails != null ? referralDetails : referalDoctorDetails;
				if (doctorDetails !=  null) {
					criticalLabTokens.put("doctor_mobile", (String) doctorDetails.get("doctor_mobile"));
					criticalLabTokens.put("doctor_mail", (String) doctorDetails.get("doctor_mail_id"));
					criticalLabTokens.put("doctor_name", (String) doctorDetails.get("doctor_name"));
				}
				
				if (doctorDetails ==  null && referals !=  null) {
					criticalLabTokens.put("doctor_mobile", (String) criticalLabTokens.get("referral_mobile"));
					criticalLabTokens.put("doctor_mail", (String) criticalLabTokens.get("referral_email"));
					 criticalLabTokens.put("doctor_name", criticalLabTokens.get("referal_name"));
				}
					    
			} catch (SQLException e) {
				logger.error("Exception during building critical Lab test data");
			}
		return criticalLabTokens;
	}
	
}
