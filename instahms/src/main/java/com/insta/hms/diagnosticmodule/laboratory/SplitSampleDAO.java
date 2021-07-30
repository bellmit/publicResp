package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitSampleDAO extends GenericDAO {
	public SplitSampleDAO() {
		super("sample_collection");
	}
	
	private static Logger logger = LoggerFactory.getLogger(ReceiveSamplesDAO.class);
	private static final GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
	
	public void performSplitSample(Map<String, Integer> paramsMap, String[] splitTestPrescribedIDs, Boolean isSplitDone) throws Exception {
		Integer sampleCollectionID = paramsMap.get("sample_collection_id"),
				centerID = paramsMap.get("center_id"),
				childOutsourceDestinationID = paramsMap.get("child_outsource_dest_id"),
				parentOutsourceDestinationID = paramsMap.get("parent_outsource_dest_id");
		Boolean success = false;
		Connection con = DataBaseUtil.getConnection();
		try {
			con.setAutoCommit(false);
			BasicDynaBean childSampleBean = generateChildSample(con, sampleCollectionID, centerID);
			putTestsInChildSample(con, childSampleBean, splitTestPrescribedIDs, childOutsourceDestinationID);
			if (isSplitDone) {
				updateSplitStatus(con, sampleCollectionID);
				if (parentOutsourceDestinationID != null && parentOutsourceDestinationID >= 0) {
					updateOutSourceDestinationForTests(con, sampleCollectionID, parentOutsourceDestinationID);
				}
			}
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	private final String UPDATE_SPLITTED_TESTS_QUERY = "UPDATE tests_prescribed "
			+ "SET sample_collection_id = ?, sample_no = ? ";
	private void putTestsInChildSample(Connection con, BasicDynaBean childSampleCollectionBean, String[] splitTestPrescribedIDs, 
			int outsourceDestinationID) throws Exception {
		List<String> splitTestPrescribedIDList = Arrays.asList(splitTestPrescribedIDs);
		StringBuilder query = new StringBuilder();
		query.append(UPDATE_SPLITTED_TESTS_QUERY);
		if (outsourceDestinationID >= 0) {
			query.append(", outsource_dest_id = ? ");
		}
		DataBaseUtil.addWhereFieldInList(query, "prescribed_id", splitTestPrescribedIDList, false);
		String putTestsInChildSampleQuery = query.toString();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(putTestsInChildSampleQuery);
			ps.setInt(1, (Integer) childSampleCollectionBean.get("sample_collection_id"));
			ps.setString(2, (String) childSampleCollectionBean.get("sample_sno"));
			int parameterOffset = 3;
			if (outsourceDestinationID >= 0) {
				ps.setInt(3, outsourceDestinationID);
				parameterOffset++;
			}
			for (int i = 0; i < splitTestPrescribedIDs.length; i++) {
				ps.setInt(i + parameterOffset, Integer.parseInt(splitTestPrescribedIDs[i]));
			}
			ps.executeUpdate();
			logger.info("Associated all the tests with child sample with sample "
					+ "number: " + childSampleCollectionBean.get("sample_sno").toString());
		} catch (SQLException sqlException) {
			logger.error("SQLException in SplitSampleDAO while putting tests "
					+ "in newly generated sample." + sqlException.getMessage());
			throw sqlException;
		} catch (Exception e) {
			logger.error("Exception in SplitSampleDAO while putting tests in newly "
					+ "generated sample. Exception: " + e.getMessage());
			throw e;
		}
	}

	private BasicDynaBean generateChildSample(Connection con, int sampleCollectionID, int centerID) throws Exception {
		BasicDynaBean sampleCollectionBean = null;
		try {
			sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id", sampleCollectionID);
			int sampleTypeID = (Integer) sampleCollectionBean.get("sample_type_id");
			String childSampleNumber = SampleTypeDAO.getNextSampleNumber(sampleTypeID, centerID);
			sampleCollectionBean.set("aliquot_parent_sample_no", (String)sampleCollectionBean.get("sample_sno"));
			sampleCollectionBean.set("sample_sno", childSampleNumber);
			sampleCollectionBean.set("sample_collection_id", sampleCollectionDAO.getNextSequence());
			sampleCollectionBean.set("coll_sample_no", null);
			sampleCollectionBean.set("orig_sample_no", null);
			sampleCollectionBean.set("receipt_time", null);
			sampleCollectionBean.set("receipt_user", null);
			sampleCollectionBean.set("receipt_other_details", null);
			sampleCollectionBean.set("sample_receive_status", "R");
			sampleCollectionBean.set("sample_transfer_status", "P");
			sampleCollectionBean.set("sample_split_status", "N");
			sampleCollectionDAO.insert(con, sampleCollectionBean);
		} catch (SQLException sqlException) {
			logger.error("SQLException while generating child sample in SplitSampleDAO. "
					+ "Exception: " + sqlException.getMessage());
			throw sqlException;
		} catch (IOException ioException) {
			logger.error("IOException while generating child sample in SplitSampleDAO. "
					+ "Exception: " + ioException.getMessage());
			throw ioException;
		} catch (Exception e) {
			logger.error("Error while generating child sample in SplitSampleDAO. "
					+ "Exception: " + e.getMessage());
			throw e;
		}
		
		return sampleCollectionBean;
	}

	private void updateSplitStatus(Connection con, int sampleCollectionID) throws Exception {
		BasicDynaBean sampleCollectionBean = null;
		try{
			sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id", sampleCollectionID);
			sampleCollectionBean.set("sample_split_status", "D");
			Map<String, Integer> keys = new HashMap<String, Integer> ();
			keys.put("sample_collection_id", sampleCollectionID);
			sampleCollectionDAO.update(con, sampleCollectionBean.getMap(), keys);
		} catch (SQLException sqlException) {
			logger.error("SQLException while updating split status in SplitSampleDAO. "
					+ "Exception: " + sqlException.getMessage());
			throw sqlException;
		} catch (IOException ioException) {
			logger.error("IOException while updating split status in SplitSampleDAO. "
					+ "Exception: " + ioException.getMessage());
			throw ioException;
		} catch (Exception e) {
			logger.error("Error while updating split status in SplitSampleDAO. "
					+ "Exception: " + e.getMessage());
			throw e;
		}
	}

	private void updateOutSourceDestinationForTests(Connection con, int sampleCollectionID, int parentOutsourceDestinationID) throws Exception {
		try {
			GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");
			BasicDynaBean testsPrescribedBean = testsPrescribedDAO.getBean();
			testsPrescribedBean.set("outsource_dest_id", parentOutsourceDestinationID);
			Map<String, Integer> keys = new HashMap<String, Integer> ();
			keys.put("sample_collection_id", sampleCollectionID);
			testsPrescribedDAO.update(con, testsPrescribedBean.getMap(), keys);
		} catch (IOException e) {
			logger.error("IOException while updating outsource destination");
			throw e;
		} catch (SQLException e) {
			logger.error("IOException while updating outsource destination");
			throw e;
		}
	}
}
