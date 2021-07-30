/**
 *
 */
package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import flexjson.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class PatientPortalEmrAction extends DispatchAction {
	static Logger log = LoggerFactory.getLogger(PatientPortalEmrAction.class);
	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer();

		String mrno = (String) req.getSession(false).getAttribute("userid");

		String filterType= req.getParameter("filterType")!=null?(String)req.getParameter("filterType"):"docType";
		String portalLink = req.getParameter("mrNoLink");

		List <EMRDoc> allDocs = new ArrayList<EMRDoc>();
		EMRDocFilter docFilter = new EMRDocFilter();
		boolean duplicateMrNoExists = false;

		if (mrno != null && !mrno.equals("")) {
			for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
					List<EMRDoc> list = provider.getProviderImpl().listDocumentsByMrno(mrno);
//					allDocs = applyFilterToTheDocs(allDocs, list, req);
					allDocs = docFilter.applyFilter(allDocs, list, req, false);
			}
			String[] duplicateMrnos = PatientDetailsDAO.getDuplicateMrNos(mrno);
			for(int i=0; i<duplicateMrnos.length; i++) {
				for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
					List<EMRDoc> list = provider.getProviderImpl().listDocumentsByMrno(duplicateMrnos[i]);
//					allDocs = applyFilterToTheDocs(allDocs, list, req);
					allDocs = docFilter.applyFilter(allDocs, list, req, false);
				}
			}
			if(duplicateMrnos.length > 0) duplicateMrNoExists = true;
		}

		if (mrno != null && !mrno.equals("")) {
			for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
				List<EMRDoc> list = provider.getProviderImpl().listVisitDocumentsForMrNo(mrno);
//				allDocs =  applyFilterToTheDocs(allDocs, list, req);
				allDocs = docFilter.applyFilter(allDocs, list, req, false);
			}
			String[] duplicateMrnos = PatientDetailsDAO.getDuplicateMrNos(mrno);
			for(int i=0; i<duplicateMrnos.length; i++) {
				for (EMRInterface.Provider provider: EMRInterface.Provider.values()) {
					List<EMRDoc> list = provider.getProviderImpl().listVisitDocumentsForMrNo(duplicateMrnos[i]);
//					allDocs = applyFilterToTheDocs(allDocs, list, req);
					allDocs = docFilter.applyFilter(allDocs, list, req, false);
				}
			}
			if(duplicateMrnos.length > 0) duplicateMrNoExists = true;
		}

		Filter filter = FilterFactory.getFilter(filterType);
		List filteredDocs = Collections.EMPTY_LIST;
		if (!allDocs.isEmpty())
			filteredDocs= filter.applyFilter(allDocs, "");

		req.setAttribute("filteredDocs", js.exclude("class").deepSerialize(filteredDocs));
		req.setAttribute("docTypeValues", js.exclude("class").serialize(new DocumentTypeMasterDAO().getdocTypeDetails()));
		req.setAttribute("allDocs", filteredDocs);
		req.setAttribute("filterTypeFromAction", filterType);
		req.setAttribute("portalLink", portalLink);
		req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
		req.setAttribute("mr_no", mrno);
		req.setAttribute("duplicateMrNoExists", duplicateMrNoExists);

		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward getEmptyScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		return m.findForward("getEmptyScreen");
	}

	public List<EMRDoc> applyFilterToTheDocs(List<EMRDoc>allDocs, List list, HttpServletRequest req)throws ParseException {

		String indocType = null;
		String exdocType = null;
		String fromDate = null;
		String toDate = null;

		String filterType= req.getParameter("filterType")!=null?(String)req.getParameter("filterType"):"visits";

		if (req.getParameter("indocType")!=null){
			indocType = req.getParameter("indocType");
			if (indocType.equals("*")) indocType = null;
		}
		if (req.getParameter("exdocType")!=null){
			exdocType = req.getParameter("exdocType");
			if (exdocType.equals("*")) indocType = null;
		}
		if (req.getParameter("fromDate")!=null)
			fromDate = req.getParameter("fromDate");
		if (req.getParameter("toDate")!=null)
			toDate = req.getParameter("toDate");


			if (list != null && !list.isEmpty()) {
				Iterator<EMRDoc> it = list.iterator();
				while (it.hasNext()) {
					EMRDoc p = it.next();
					if (filterType.equals("visits")) {
						if ((indocType!=null && exdocType!=null) || (indocType!=null && exdocType==null) || (indocType==null && exdocType!=null)) {
							if (p.getType().equals(indocType) && exdocType==null) {
								allDocs.add(p);
							}
							if (!p.getType().equals(exdocType) && indocType==null) {
								allDocs.add(p);
							}
						} else {
							allDocs.add(p);
						}
					} else {
						SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
						if ((!fromDate.equals("") && !toDate.equals("")) || (fromDate.equals("") && !toDate.equals("")) || (!fromDate.equals("") && toDate.equals(""))) {
							if (p.getDate() == null) {
								// skip the document. when the document date is null, but searching within a date range.
								continue;
							}
							if (!toDate.equals("") && fromDate.equals("")) {
								java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
								int j = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valtoDate);
								if (j <= 0) {
									allDocs.add(p);
								}
							}
							if (!fromDate.equals("") && toDate.equals("")) {
								java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
								int k = p.getDate().compareTo(valfromDate);
								if (k >= 0) {
									allDocs.add(p);
								}
							}
							if (!fromDate.equals("") && !toDate.equals("") ) {
								java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
								java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
								if( p.getDate() == null) {
									allDocs.add(p);
								}else{
									int j = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valtoDate);
									int k = new Date(dateformat.parse(p.getDate().toString()).getTime()).compareTo(valfromDate);
									if ((j <= 0) && (k >= 0)) {
										allDocs.add(p);
									}
								}
							}
						} else {
							allDocs.add(p);
						}
					}
				}
			}

		return allDocs;
	}


}
