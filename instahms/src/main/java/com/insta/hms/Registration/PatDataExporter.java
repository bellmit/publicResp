package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.jobs.GenericJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PatDataExporter extends GenericJob {
	private static final Logger log = LoggerFactory.getLogger(PatDataExporter.class);

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {

		Date fromDate = null;
		Date toDate = null;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -5);
		toDate = new java.sql.Date(cal.getTimeInMillis());
		cal.add(Calendar.DATE, -1);
		fromDate = new java.sql.Date(cal.getTimeInMillis());
		setJobConnectionDetails();
		Connection con = DataBaseUtil.getConnection();
		try {
			File exportDir = new File("/tmp/EncounterData/in");
			exportDir.mkdirs();
			String filename = "encounter_data_" +
				new DateUtil().getFormatWithoutSpace().format(new java.util.Date()) + ".xml";
			FileOutputStream fos = new FileOutputStream(exportDir + "/" + filename);

			ExportRegData ereg = new ExportRegData();
			Map headerMap = new HashMap();
			headerMap.put("todays_date", DateUtil.getCurrentTimestamp());
			headerMap.put("user_name", "auto");

			PatDataExportUtility.addClaimHeader(fos, headerMap);
			ExportRegDataDAO.getXmlFieldsArray(fromDate, toDate, fos);
			PatDataExportUtility.addClaimFooter(fos, new HashMap());
			fos.flush();
			fos.close();
			log.info("Export patient data done, filename: " + filename);
		} catch(Exception e) {
			log.error("Failed to export encounter data");
			throw new JobExecutionException(e.getMessage());
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
