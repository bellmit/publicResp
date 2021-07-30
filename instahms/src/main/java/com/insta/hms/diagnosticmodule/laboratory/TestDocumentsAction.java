package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestDocumentsAction extends DispatchAction {

	public ActionForward searchTestDocuments(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException,ParseException {
		Map params = request.getParameterMap();
		int prescribedId = Integer.parseInt(request.getParameter("prescribed_id"));
		BasicDynaBean testdetails = LaboratoryDAO.getPrescribedDetails(prescribedId);
		request.setAttribute("testdetails", testdetails.getMap());

		String documentType = mapping.getProperty("documentType");
		Boolean specialized = new Boolean(mapping.getProperty("specialized"));

		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		AbstractDocumentPersistence persistenceAPI = AbstractDocumentPersistence.getInstance(documentType, specialized);
		Map extraParams = persistenceAPI.populateKeys(params, null);
		request.setAttribute("pagedList", persistenceAPI.searchDocuments(listingParams, extraParams, specialized, documentType));

		request.setAttribute("documentType", documentType);
		request.setAttribute("specialized", specialized);
		request.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
		request.setAttribute("docLink", new Boolean(mapping.getProperty("doclink")));
		request.setAttribute("category", mapping.getProperty("category"));
		String hospital = (String) testdetails.get("hospital");
		request.setAttribute("isIncomingPatient", hospital.equals("incoming"));
		return mapping.findForward("testdocuments");
	}
}
