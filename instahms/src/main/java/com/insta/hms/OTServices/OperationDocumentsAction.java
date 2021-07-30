/**
 *
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class OperationDocumentsAction extends DispatchAction {

	static OTReportsDAO otDocsDAO = new OTReportsDAO();
	
	@IgnoreConfidentialFilters
	public ActionForward searchOperationDocuments(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException,ParseException {
		Map params = request.getParameterMap();
		int prescribedId = Integer.parseInt(request.getParameter("prescription_id"));
		BasicDynaBean operationdetails = OTServicesDAO.getOperation(prescribedId);
		request.setAttribute("operationdetails", operationdetails.getMap());

		String documentType = mapping.getProperty("documentType");
		Boolean specialized = new Boolean(mapping.getProperty("specialized"));

		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		AbstractDocumentPersistence persistenceAPI = AbstractDocumentPersistence.getInstance(documentType, specialized);
		Map extraParams = persistenceAPI.populateKeys(params, null);
		request.setAttribute("pagedList", persistenceAPI.searchDocuments(listingParams, extraParams, specialized, documentType));

		List templatesList = GenericDocumentTemplateDAO.getOperationTemplates((String) operationdetails.get("op_id"));
		if (templatesList.isEmpty()) {
			templatesList = GenericDocumentTemplateDAO.getTemplates(true, "SYS_OT", "A");
		}
		request.setAttribute("templatesList", templatesList);
		request.setAttribute("documentType", documentType);
		request.setAttribute("specialized", specialized);
		request.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
		request.setAttribute("docLink", new Boolean(mapping.getProperty("doclink")));
		return mapping.findForward("operationdocuments");
	}

	public ActionForward signOffDocuments(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("documentslist"));
		FlashScope flash = FlashScope.getScope(request);
		Connection con = null;
		String[] doc_ids = request.getParameterValues("signOffList");
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			if (doc_ids != null) {
				boolean success = true;
				for (String docidStr : doc_ids) {
					int docId = Integer.parseInt(docidStr);
					BasicDynaBean bean = otDocsDAO.getBean();

					bean.set("sign_off_datetime", new java.sql.Timestamp(new java.util.Date().getTime()));
					bean.set("signed_off", true);
					if (otDocsDAO.update(con, bean.getMap(), "doc_id", docId) != 1) {
						success = false;
						break;
					}
				}
				if (success) {
					con.commit();
					String msg = ((doc_ids.length > 1)?"Documents":"Document") + " singed off successfully..";
					flash.put("success", msg);
				} else {
					con.rollback();
					String error = "Failed to sign off " + ((doc_ids.length > 1)?"Documents":"Document..");
					flash.put("error", error);
				}
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		flash.put(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("prescription_id", request.getParameter("prescription_id"));
		redirect.addParameter("operation_details_id", request.getParameter("operation_details_id"));
		redirect.addParameter("prescribed_id", request.getParameter("prescribed_id"));
		redirect.addParameter("visitId", request.getParameter("visitId"));
		return redirect;
	}

}
