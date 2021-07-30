package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class BatchSampleIdGenerator {

	public static Map<String, String> generateBatchBasedSampleId(Connection con, String[] testId, String[] isPackage, String[] sampleId, 
			String[] sampleTypeId, String[] batchBased, StringBuilder batchBasedNo, String[] orig_sample_no,
			String[] outhouseId, Map<String, String> sampNotoTypemap, Map<String, String> sampleNoToOrigSmplNoMap, 
			Map<String, String> outSourceDestIdMap) throws SQLException {

		BasicDynaBean testBean =null;
		GenericDAO diagDAO = new  GenericDAO("diagnostics");
		boolean isNogenerated = false;
		String sampleID = null;
		
		for(int i=0; i<testId.length; i++) {
			if(isPackage[i].equalsIgnoreCase("n")) {
				testBean = diagDAO.findByKey("test_id", testId[i]);
				if ( testBean.get("sample_needed").equals("n") ) {
					batchBased[i] = ""; //To avoid null pointer exception
					continue;//we allow sample not needed tests for incoming sample reg.Such tests doesn't need sample no.
				}
				if (!isNogenerated) {
					 sampleID = new SampleTypeDAO().getBatchBasedSampleNo(con);
					batchBasedNo.append(sampleID);
					 isNogenerated = true;
				 }
				 sampleId[i] = sampleID;
				 batchBased[i] = sampleTypeId[i]+""+sampleId[i];
				 sampNotoTypemap.put(batchBased[i], sampleTypeId[i]);
				 sampleNoToOrigSmplNoMap.put(batchBased[i], orig_sample_no[i]);
				 outSourceDestIdMap.put(batchBased[i], outhouseId[i]);
			} else {
				batchBased[i] = "";
			}
		}
		return sampNotoTypemap;
	}
}
