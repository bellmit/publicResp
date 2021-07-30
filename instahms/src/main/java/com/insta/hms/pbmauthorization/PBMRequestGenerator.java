/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PBMRequestGenerator {

	static Logger logger = LoggerFactory.getLogger(PBMRequestGenerator.class);
	private PBMPrescriptionsDAO pbmdao = new PBMPrescriptionsDAO();
	private PBMPrescriptionHelper pbmhelper = new PBMPrescriptionHelper();

	public String generatePBMRequestXML(String pbmPrescId, String userId,
			String requestType, String testing, String activeMode,
			boolean updateRequest) throws IOException, TemplateException, SQLException, Exception {

	String testingProviderId = null; // "PF1506"

    Integer userCenterId = RequestContext.getCenterId();
	userCenterId = userCenterId == null ? 0 : userCenterId;
	BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id", userCenterId);
	if (centerBean != null) {
		String shafafiya_pbm_test_provider_id = centerBean.get("shafafiya_pbm_test_provider_id") != null
					? ((String)centerBean.get("shafafiya_pbm_test_provider_id")).trim() : "";
		testingProviderId =  shafafiya_pbm_test_provider_id;
	}

	int pbm_pres_id = Integer.parseInt(pbmPrescId);

	String path = RequestContext.getHttpRequest().getContextPath();
	Map<String, StringBuilder> errorsMap = new HashMap<String, StringBuilder>();

	List<String> pbmPrescList = new ArrayList<String>();
	pbmPrescList.add(pbmPrescId);

	StringBuilder errStr = new StringBuilder("Error(s) while XML data check. PBM Request XML could not be generated.<br/>" +
							"Please correct (or) update the following and generate pbm request again.<br/>");

	StringBuilder headerErr = new StringBuilder("<br/> HEADER ERROR: PBM Request header does not contain sender. <br/>" +
							"Please check Service Reg No. for account group : <br/> ");

	StringBuilder testingProviderErr = new StringBuilder("<br/> TESTING PROVIDER ERROR: Shafafiya PBM Web service is not set to active mode, cannot use live data. <br/>" +
							"For testing, the Service Reg No. for account group needs to be: <b> "+testingProviderId+" </b> <br/> ");

	if (activeMode.equals("N") && testingProviderId.equals("")) {
		StringBuilder errorString = new StringBuilder("<br/> PBM FACILITY ID ERROR: " +
				"PBM Request cannot be sent. <br/>" +
				"Test Provider No. cannot be null. </b>");
		errStr.append("<br/>"+errorString);

		return errStr.toString();
    }

	Integer centerId = RequestContext.getCenterId();
	int accountGroup = 0;

	String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
	String eclaimXMLSchema = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getHealth_authority();

	BasicDynaBean centerbean = null;

	if (centerId != null) {
		centerbean = new CenterMasterDAO().findByKey("center_id", centerId);
		healthAuthority = centerbean != null ? (String)centerbean.get("health_authority") : healthAuthority;
	}

	if (!requestType.equals("Cancellation")) {

		// Get user store account group and user center.
		// If user center is null then store account group is considered for PBM Request.
		Integer pbmStoreId = null;

		List<String> columns = new ArrayList<String>();
		columns.add("pbm_presc_id");
		columns.add("pbm_store_id");

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("pbm_presc_id", pbm_pres_id);
		BasicDynaBean pbmPresBean = pbmdao.findByKey(columns, field);

		pbmStoreId = pbmPresBean.get("pbm_store_id") != null ? (Integer)pbmPresBean.get("pbm_store_id") : null;

		if (pbmStoreId == null) {

			StringBuilder errorString = new StringBuilder("<br/> PBM STORE ERROR: " +
					"There is no store for this prescription. PBM Request cannot be sent. <br/>" +
					"Please assign store for the prescription and generate PBM Request again. <br/> ");
			errStr.append("<br/>"+errorString);

			return errStr.toString();
		}

		BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", pbmStoreId);
		accountGroup = (Integer) store.get("account_group");

		String service_reg_no = null;
		String msgTxt = "";

		if (accountGroup != 0) {
			BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", accountGroup);
			service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
			msgTxt = "Account Group: "+(pbmhelper.urlString(path, "account-group", new Integer(accountGroup).toString(), (String)accbean.get("account_group_name")));
		}else if (centerbean != null) {
			service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
			msgTxt = "Center: "+(pbmhelper.urlString(path, "center-name", centerId.toString(), (String)centerbean.get("center_name")));
		}

		if (service_reg_no == null || service_reg_no.trim().equals("")) {

			StringBuilder errorString = new StringBuilder("<br/> PBM FACILITY ID ERROR: " +
					"There is no Service Reg No. for "+msgTxt+". PBM Request cannot be sent. <br/>" +
					"Please enter Service Reg No. for "+msgTxt+" needs to be: <b> "+testingProviderId+" </b>");
			errStr.append("<br/>"+errorString);

			return errStr.toString();
		}

		if (service_reg_no != null && activeMode.equals("N")
				&& !service_reg_no.trim().equals(testingProviderId)) {

			StringBuilder errorString = new StringBuilder("<br/> PBM FACILITY ID ERROR: " +
					"PBM Request cannot be sent. <br/>" +
					"For testing, the Service Reg No. for "+msgTxt+". needs to be: <b> "+testingProviderId+" </b>");
			errStr.append("<br/>"+errorString);

			return errStr.toString();
		}
	}

		// Generate PBM Request Id and save request_id, account_group and center_id before generating XML.
		boolean result = pbmdao.savePBMRequestDetails(pbm_pres_id, userId, requestType, accountGroup, centerId, updateRequest);
		if (!result) {
			StringBuilder errorString = new StringBuilder("<br/> PBM REQUEST ID ERROR: " +
					"Error while generating PBM Request Id. <br/>");
			errStr.append("<br/>"+errorString);

			return errStr.toString();
		}

		// Get PBM details.
		BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbm_pres_id);
		String pbm_request_id = pbmbean.get("pbm_request_id") != null ? (String)pbmbean.get("pbm_request_id") : null;
		BasicDynaBean headerbean = pbmdao.getPBMHeaderFields(pbm_request_id);

		if (headerbean.get("provider_id") == null || headerbean.get("provider_id").equals("")) {
			errorsMap.put("HEADER ERROR:", headerErr.append(
			pbmhelper.urlString(path, "account-group", ((Integer)headerbean.get("account_group_id")).toString(), (String)headerbean.get("account_group_name"))));
		}

		if (headerbean.get("provider_id") != null && activeMode.equals("N")
				&& !headerbean.get("provider_id").equals(testingProviderId)) {
			errorsMap.put("TESTING PROVIDER ERROR:", testingProviderErr.append(
			pbmhelper.urlString(path, "account-group", ((Integer)headerbean.get("account_group_id")).toString(), (String)headerbean.get("account_group_name"))));
		}

		testing = (testing == null || testing.trim().equals("")) ? "N" : testing;
		Map headerMap = new HashMap();
		headerbean.set("testing", testing);
		headerbean.set("health_authority", healthAuthority);

		List<PBMRequest> pbmRequestList = new ArrayList<PBMRequest>();
		pbmdao.validatePBMPrescriptions(errorsMap, path, activeMode, pbmPrescList, pbmRequestList);

		if (errorsMap != null && !errorsMap.isEmpty()) {
			Iterator keys = errorsMap.keySet().iterator();

			while (keys.hasNext()) {
				String key = (String)keys.next();
				StringBuilder errorString = (StringBuilder)errorsMap.get(key);
				errStr.append("<br/>"+errorString);
			}

			return errStr.toString();

		}else {

			File requestFile = File.createTempFile("tempPBMRequestFile", "");
			OutputStream fos = new FileOutputStream(requestFile);

			headerMap = headerbean.getMap();

			PBMRequest pbmrequest = pbmRequestList.get(0);
			Map<String, PBMRequest> bodyMap = new HashMap<String, PBMRequest>();
			bodyMap.put("pbmrequest", pbmrequest);

			addPBMRequestHeader(fos, headerMap);
			addPBMRequestBody(fos, bodyMap);
			addPBMRequestFooter(fos, new HashMap());

			fos.flush();
			fos.close();

			String xmlStr = FileUtils.readFileToString(requestFile);
			logger.debug("PBM Request XML Content for PBM Presc Id: "+pbmPrescId+" is ... : " +xmlStr);

			//InputStream fis = new FileInputStream(requestFile);
			//String requestXMLStr = new PBMPrescriptionHelper().convertToBase64Binary(fis);

			return xmlStr;
		}
	}

	public void addPBMRequestHeader(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/PBMAuthorization/PBMRequestHeader.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void addPBMRequestBody(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/PBMAuthorization/PBMRequestBody.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void addPBMRequestFooter(OutputStream stream, Map ftlMap)
			throws IOException, TemplateException {
		Template t = null;
		t = AppInit.getFmConfig().getTemplate("/PBMAuthorization/PBMRequestFooter.ftl");
		writeToStream(t, stream, ftlMap);
	}

	public void writeToStream(Template t, OutputStream stream, Map ftlMap)
			throws IOException, TemplateException{
		if (t == null)
			return;
		StringWriter sWriter = new StringWriter();
		t.process(ftlMap, sWriter);
		stream.write(sWriter.toString().getBytes());
		stream.flush();
	}
}
