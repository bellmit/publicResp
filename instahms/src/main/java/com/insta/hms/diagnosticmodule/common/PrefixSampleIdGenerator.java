package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.util.Map;

public class PrefixSampleIdGenerator {

	@SuppressWarnings("static-access")
	public static Map<String, String> generatePrefixBasedSampleId(Connection con, String[] testId,String[] isPackage, String[] orig_sample_no, 
			String[] sampleId, String[] sampleTypeId, Integer centerId, String[] outSourceChain, String[] outhouseId, 
			Map<String, String> sampNotoTypemap, Map<String, String> sampleNoToOrigSmplNoMap, 
			Map<String, String> outSourceDestIdMap) throws Exception {
		
		BasicDynaBean testBean =null;
		GenericDAO diagDAO = new  GenericDAO("diagnostics");
		SampleTypeDAO sampleDAO = new SampleTypeDAO();
		GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
		
		for(int i=0; i<testId.length; i++) {
			if(isPackage[i].equalsIgnoreCase("n")) {
				testBean = diagDAO.findByKey("test_id", testId[i]);
				 if ( testBean.get("sample_needed").equals("n") )
					 continue;//we allow sample not needed tests for incoming sample reg.Such tests doesn't need sample no.
				 boolean isOriginalSampleNoExists = false;
				 String existingSampleId = null;
				 boolean check = (i==0) || (i==1 && isPackage[0].equalsIgnoreCase("y"));
				 if(!check) {
					 for(int j=0; j<i;j++){
						 boolean isSameDestChain = false;
						 if(isPackage[j].equalsIgnoreCase("n")){
							 // These conditions for checking outsourcechain for inhouse tests(since there will not be any 
							 // outsource chain for the inhouse tests) 
							 if (outSourceChain[i] == null && outSourceChain[j] == null) {
								 isSameDestChain = true;											 
							 } else if (outSourceChain[i] != null && outSourceChain[j] != null 
									 && outSourceChain[j].equals(outSourceChain[i])) {
								 isSameDestChain = true;
							 }							 
							 if(orig_sample_no[j].equals(orig_sample_no[i]) && sampleTypeId[j].equals(sampleTypeId[i])
									 && isSameDestChain){
								 isOriginalSampleNoExists = true;
								 existingSampleId = sampleId[j];
								 break;
							 }
						 }
					 }
				 }
				String sampleTypeID = sampleTypeId[i];
				if(!isOriginalSampleNoExists && isPackage[i].equalsIgnoreCase("n")) {
					sampleId[i] = sampleDAO.getNextSampleNumber(Integer.parseInt(sampleTypeID),centerId);
					BasicDynaBean existingSampleBean = sampleCollectionDAO.findByKey("sample_sno",sampleId[i]);
			   		if(existingSampleBean != null) {
			   		//	TODO return errorResponse(request, response, "Duplicate Sample Id "+sampleId[i]);
			   			throw new Exception("Duplicate Sample Id "+sampleId[i]);
			   		}
			   		sampNotoTypemap.put(sampleId[i], sampleTypeID);
			   		sampleNoToOrigSmplNoMap.put(sampleId[i], orig_sample_no[i]);
			   		outSourceDestIdMap.put(sampleId[i], outhouseId[i]);
				} else {
					sampleId[i] = existingSampleId;
				}
			}
		}
		return sampNotoTypemap;
	}
	
}
