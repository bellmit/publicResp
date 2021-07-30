package com.insta.hms.emr;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrowthChartsProviderImpl implements EMRInterface {

  protected static final Map<String, String> chartTypes = new LinkedHashMap<>();

  static {
    chartTypes.put("L,WA","Length-for-age And Weight-for-age percentiles (Birth to 2 years)");
    chartTypes.put("HC","Head circumference-for-age percentiles (Birth to 2 years)");
    chartTypes.put("WL","Weight-for-length percentiles (Birth to 2 years)");
    chartTypes.put("S,WA","Stature for age And weight for age percentiles (2 to 20 years)");
    chartTypes.put("BMI","BMI for age percentiles (2 to 20 years)");
    chartTypes.put("WS","Weight-for-stature percentiles (2 to 20 years)");
  }
  
	public byte[] getPDFBytes(String docid, int printId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		Map modulesActivatedMap = ((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap();
		new PatientDetailsDAO();
    BasicDynaBean patientDetails = PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo);
		if (null != modulesActivatedMap.get("mod_growth_charts") && modulesActivatedMap.get("mod_growth_charts").equals("Y")
				&& (patientDetails != null && 
				!((Integer)patientDetails.get("age") > 20 && patientDetails.get("agein").equals("Y"))) ) {
			return populateEMRGrowthCharts(mrNo);
		}
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		//we dont have visit level documents.
		return Collections.emptyList();
	}
	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public List<EMRDoc> populateEMRGrowthCharts(String mrNo) throws SQLException {
		List<EMRDoc> docs = new ArrayList<>();
		Map patientDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (Map.Entry<String, String> chartType : chartTypes.entrySet()) {
			EMRDoc doc = new EMRDoc();
			 
	    BasicDynaBean growthChartDescbean = new GenericDAO("growth_chart_details").findByKey("chart_type", chartType.getKey());
			doc.setType("SYS_GROWTH_CHART");
			doc.setDocid(mrNo+chartType.getKey());
			doc.setTitle((String)growthChartDescbean.get("chart_name"));
			doc.setUpdatedBy("");
			doc.setDoctor(null);
			if ( patientDetails != null && patientDetails.get("reg_date") != null ){
				doc.setDate((Date)patientDetails.get("reg_date"));
			}	
			doc.setPrinterId(printerId);
			doc.setPdfSupported(true);
			doc.setAuthorized(true);

			String displayUrl = "/GrowthCharts/GrowthChartsAction.do?method=createCharts&chart_type="+ chartType.getKey() +
					"&mr_no=" + mrNo + "&printerId="+printerId;
			doc.setDisplayUrl(displayUrl);
			doc.setProvider(EMRInterface.Provider.GrowthChartsProvider);

			docs.add(doc);
		}
		return docs;
	}

}
