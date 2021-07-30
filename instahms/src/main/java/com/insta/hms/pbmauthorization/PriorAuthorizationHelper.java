/**
 *
 */
package com.insta.hms.pbmauthorization;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author lakshmi
 *
 */
public class PriorAuthorizationHelper {

	static Logger logger = LoggerFactory.getLogger(PriorAuthorizationHelper.class);

	public enum TransactionResults {
		// PBM shafafiya web service return values.
		OPERATION_SUCCESS	 			(0, "operation is successful"),
		VALIDATION_SUCCESS_WITH_WARNINGS(1, "e-claim transaction validation succeeded with warnings"),
		EMPTY_TRANSACTIONS   		  	(2, "no new prior authorization transactions are available for download"),
		NO_APPROVED_DRUGS	 		  	(3, "member has no approved trade drugs, hence Prescription transaction is not returned"),
		DEFAULT_DRG_GROUPING 		  	(4, "DRG grouping is performed using default patient gender and age (female 21 years old)"),
		LOGIN_FAILURE        		  	(-1, "login failed for the user"),
		VALIDATION_FAILED_WITH_ERRORS 	(-2, "e-claim transaction validation is failed with errors"),
		INVALID_INPUTS       			(-3, "required input parameter for the web service is empty, or null, or contains invalid value"),
		UNEXPECTED_ERROR		        (-4, "unexpected error occurred"),
		FROM_TO_DATE_EXCEED_100_DAYS    (-5, "if difference between date from and date to parameters is longer than 100 days"),
		FILE_NOT_FOUND				    (-6, "the specified file is not found"),
		TRANSACTION_NOT_SUPPORTED       (-7, "transaction is not supported"),
		DRG_GROUPER_BUSY 				(-8, "DRG Grouper is busy serving other requests; if you get this error " +
												"code please try to call the web service again in 5-10 minutes"),
		DRG_GROUPER_ERROR				(-9, "error occurred while running DRG Grouper"),
		NO_SEARCH_CRITERIA 				(-10, "if no search criteria is found")
		;

		int txnResult;
		String resultMsg;
		private TransactionResults(int txnResult, String resultMsg) {
			this.txnResult = txnResult;
			this.resultMsg = resultMsg;
		}

		public int getTxnResult() {
			return this.txnResult;
		}

		public String getResultMsg() {
			return this.resultMsg;
		}

		public static TransactionResults getTxnResultMessage(int txnres) {
			for (TransactionResults tx : TransactionResults.values()) {
				if (txnres == tx.txnResult) {
					return tx;
				}
			}
			return null;
		}
	}

	public String getErrorReportbase64String(byte[] errReportBytes) {
        try {
			File errorFile = File.createTempFile("tempPBMErrorReport", "");
			File resultingFile = File.createTempFile("tempPBMErrorReportEncoded", "");
			try (FileOutputStream errFos = new FileOutputStream(errorFile);
				 InputStream is = new FileInputStream(errorFile);
				 OutputStream os = new FileOutputStream(resultingFile);
				 BufferedReader reader = new BufferedReader(new FileReader(resultingFile.getPath()))) {
				errFos.write(errReportBytes, 0, errReportBytes.length);


				int readBytes = 0;
				byte[] byteSize = new byte[10000000];
				while ((readBytes = is.read(byteSize)) > 0) {
					byte[] ba = new byte[readBytes];

					for (int i = 0; i < ba.length; i++) {
						ba[i] = byteSize[i];
					}

					byte[] encStr = Base64.encodeBase64(ba);
					os.write(encStr, 0, encStr.length);
				}

				StringBuffer fileData = new StringBuffer();
				char[] buf = new char[1024];
				int numRead = 0;
				while ((numRead = reader.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
				}
				return fileData.toString();

			}
		} catch (Exception e) {
			logger.error("Exception occured : " + e.getMessage());
		}

        return null;
    }

	public String decodeErrorReportbase64ToFile(String errorReport, File decodedDataFile) {
		try {
			File errorFile = File.createTempFile("tempPBMErrorReport", "");
			try (OutputStream fos = new FileOutputStream(errorFile);
				 OutputStream outputStream = new FileOutputStream(decodedDataFile);
				 InputStream inputStream = new FileInputStream(errorFile)
			) {
				fos.write(errorReport.getBytes());

				int readBytes = 0;
				byte[] byteSize = new byte[10000000];
				while ((readBytes = inputStream.read(byteSize)) > 0) {
					byte[] ba = new byte[readBytes];

					for (int i = 0; i < ba.length; i++) {
						ba[i] = byteSize[i];
					}

					ba = Base64.decodeBase64(ba);
					outputStream.write(ba, 0, ba.length);
				}
			}
		} catch (Exception e) {
			logger.error("Error while read/write of error report data." + e.getMessage());
			return "Error while read/write of error report data." + e.getMessage();

		}
		return null;
	}

	public String unzipErrorReportFile(File errorZipFile, OutputStream outputStream){

        ZipEntry zEntry = null;
		try (FileInputStream fileInputStream = new FileInputStream(errorZipFile);
			 ZipInputStream zipInputStream =
					 new ZipInputStream(new BufferedInputStream(fileInputStream))) {

            while((zEntry = zipInputStream.getNextEntry()) != null){
                try {
                    byte[] tmp = new byte[4*1024];
                    int size = 0;
                    while((size = zipInputStream.read(tmp)) != -1){
                    	outputStream.write(tmp, 0 , size);
                    }
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e){
                	logger.error("Error while reading error report data. Unzip file: " + e.getMessage());
        			return "Error while reading error report data. Unzip file: " + e.getMessage();
                }
            }
            zipInputStream.close();
        } catch (FileNotFoundException e) {
        	logger.error("Error while reading error report data. File Not Found: " + e.getMessage());
			return "Error while reading error report data. File Not Found: " + e.getMessage();

        } catch (IOException e) {
        	logger.error("Error while reading error report data. IOException: " + e.getMessage());
			return "Error while reading error report data. IOException: " + e.getMessage();
        }
        return null;
    }

	public static final String SystemDownTimeError = "System Downtime(Provider, Daman, Shafafiya) <br/> " +
		" Please Stop the PBM and Shift to manual/current process.  <br/> " +
		" Check the communication between the provider and Daman through " +
		" <b> pbmtechnicalqueries@damanhealth.ae </b> ";

	public static final String DHPOSystemDownTimeError = "System Downtime (DHPO, eClaimLink) <br/> " +
		" Please Stop the PBM and Shift to manual/current process.  <br/> ";

	public String isDHPOConnected() throws MalformedURLException, IOException {
		return null;
/*		HttpURLConnection http = null;
		String err = "System Downtime... Cannot connect to http://www.eClaimLink.ae/ ";
		try {
			// Check if server can be connected.
			System.setProperty("http.keepAlive", "false");
			URL url = new URL("http://www.eClaimLink.ae/");
			http = (HttpURLConnection)url.openConnection();
			int responseCode = http.getResponseCode();

			if (responseCode != 200)
				return err;

		}catch (ConnectException e) {
			return err;
		}catch (Exception e) {
			return err;
		}finally {
			if (http != null)
				http.disconnect();
		}
		return null; */
	}
}
