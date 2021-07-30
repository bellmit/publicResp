/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class PatientDocketAction extends DispatchAction{

    static Logger log = LoggerFactory.getLogger(PatientDocketAction.class);

	public ActionForward getPatientDocket(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			Exception {

		String mrNo = request.getParameter("mr_no");

		List<EMRDoc> allDocs = new ArrayList<EMRDoc>();

		for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
			List<EMRDoc> list = provider.getProviderImpl().listDocumentsByMrno(mrNo);
			if (list != null && !list.isEmpty()) {
				allDocs.addAll(list);
			}
		}

		for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
			List<EMRDoc> list = provider.getProviderImpl().listVisitDocumentsForMrNo(mrNo);
			if (list != null && !list.isEmpty()) {
				allDocs.addAll(list);
			}
		}

		List docTypes = DocumentTypeMasterDAO.getDocTypeNames();

		request.setAttribute("docList", allDocs);
		request.setAttribute("docTypes", ConversionUtils.listBeanToMapBean(docTypes, "doc_type_id"));

		return mapping.findForward("list");
	}

	public ActionForward printDocket(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String[] checkboxValues = request.getParameterValues("printDocument");
		response.setContentType("application/pdf");
		OutputStream stream = response.getOutputStream();

		if (checkboxValues == null) {
			return null;
		} else {
			PdfCopyFields copy = new PdfCopyFields(stream);
			boolean added = false;
			for (int i=0; i<checkboxValues.length; i++) {
				for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
					String[] paramValue = checkboxValues[i].split(",");
					if (provider.getProviderName().equals(paramValue[1])) {
						byte[] pdfBytes = provider.getProviderImpl().getPDFBytes(paramValue[0], Integer.parseInt(paramValue[2]));
						log.debug("Adding PDF document: " + provider.getProviderName() + " " + paramValue[0]);
						if (pdfBytes != null) {
							PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
							copy.addDocument(reader);
							added = true;
						} else {
							log.error("Provider " + provider.getProviderName() +
									" returned null for " + paramValue[0]);
						}
					}
				}
			}
			if (added)
				copy.close();

			stream.flush();
			stream.close();
		}
		return null;
	}
}

