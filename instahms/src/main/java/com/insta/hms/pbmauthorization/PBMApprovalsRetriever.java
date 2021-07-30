/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.insta.hms.eservice.EResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.xml.ws.Holder;

/**
 * @author
 *
 */
public class PBMApprovalsRetriever {

	static Logger log = LoggerFactory.getLogger(PBMApprovalsRetriever.class);

	private SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private String serviceUser = null;
	private String servicePassword = null;

	public PBMApprovalsRetriever(String serviceUser, String servicePassword) {
		super();
		this.serviceUser = serviceUser;
		this.servicePassword = servicePassword;
	}

	public String getServiceUser() {
		return serviceUser;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public EResponse getApprovalFileList(Date fromDate, Date toDate)  {

		Holder<Integer> txnResult = new Holder<>();
		Holder<String> errorMessage = new Holder<>();
		Holder<String> xmlTransactions = new Holder<>();

		String user = getServiceUser();
		String password = getServicePassword();

		com.insta.hms.pbmauthorization.generated.WebservicesSoap pbmAuthSoap = getWebService();

		String transactionFromDate = timeStampFormatterSecs.format(fromDate);
		String transactionToDate = timeStampFormatterSecs.format(toDate);

		if (fromDate == null && toDate == null) { // both fromDate and toDate are null
			pbmAuthSoap.getNewPriorAuthorizationTransactions(user,
					password, txnResult, xmlTransactions, errorMessage);

		} else if (fromDate != null && toDate != null) { // both from and to dates are not null
			pbmAuthSoap.searchTransactions(user, password, 2, null, null,
					32, 1, null, transactionFromDate, transactionToDate, -1, -1,
						txnResult, xmlTransactions, errorMessage);
		}

		return new PBMAuthResponse(txnResult.value, errorMessage.value, xmlTransactions.value);
	}

	public EResponse getApprovalFile(String fileId) {
		Holder<Integer> downloadTxnResult = new Holder<Integer>();
		Holder<String> fileName = new Holder<String>();
		Holder<byte[]> file = new Holder<byte[]>();
		Holder<String> errorMessage = new Holder<String>();
		String user = getServiceUser();
		String password = getServicePassword();

		com.insta.hms.pbmauthorization.generated.WebservicesSoap pbmAuthSoap = getWebService();

		pbmAuthSoap.downloadTransactionFile( user, password,
					fileId, downloadTxnResult, fileName, file, errorMessage);

		EResponse authXMLResponse =  new PBMAuthResponse(downloadTxnResult.value, errorMessage.value, file.value, new Object[]{fileName.value});
		return authXMLResponse;
	}

	public EResponse checkNewTransactions() {
		Holder<Integer> txnResult = new Holder<Integer>();
		Holder<String> errorMessage = new Holder<String>();
		String user = getServiceUser();
		String password = getServicePassword();

		com.insta.hms.pbmauthorization.generated.WebservicesSoap pbmAuthSoap = getWebService();

		// Call API to check for new transactions.
		pbmAuthSoap.checkForNewPriorAuthorizationTransactions(user, password, txnResult, errorMessage);

		return new PBMAuthResponse(txnResult.value, errorMessage.value, null);
	}

	public EResponse setTransactionDownloaded(String fileId){
		Holder<Integer> txnResult = new Holder<Integer>();
		Holder<String> errorMessage = new Holder<String>();
		String user = getServiceUser();
		String password = getServicePassword();

		com.insta.hms.pbmauthorization.generated.WebservicesSoap pbmAuthSoap = getWebService();

		pbmAuthSoap.setTransactionDownloaded(user, password, fileId, txnResult, errorMessage);

		return new PBMAuthResponse(txnResult.value, errorMessage.value, null);
	}

	public com.insta.hms.pbmauthorization.generated.WebservicesSoap getWebService() {
		com.insta.hms.pbmauthorization.generated.Webservices pbmauthws = new com.insta.hms.pbmauthorization.generated.Webservices();
		com.insta.hms.pbmauthorization.generated.WebservicesSoap pbmauthwsoap = pbmauthws.getWebservicesSoap();
		return pbmauthwsoap;
	}

	public String getWebServiceUser() {
		return serviceUser;
	}

	public String getWebServicePassword() {
		return servicePassword;
	}
}