package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.diagnosticmodule.laboratory.ExternalReportAction;
import com.insta.hms.emr.listdocs.EmrListDocuments;
import com.insta.hms.emr.listdocs.EmrListDocumentsMrNo;
import com.insta.hms.emr.listdocs.EmrListDocumentsVisit;
import com.insta.hms.emr.listdocs.EmrListDocumentsVisitsForMrNo;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.security.usermanager.UserService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.sun.mail.iap.ConnectionException;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EMRMainDisplayAction extends DispatchAction {
	static Logger log = LoggerFactory.getLogger(HtmlConverter.class);

	private static UserService userService = ApplicationContextProvider
		      .getBean(UserService.class);

	private static PatientDetailsRepository patientDetailsRepository = ApplicationContextProvider
        .getBean(PatientDetailsRepository.class);
	
	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer();
		String mrno= (String) req.getParameter("mr_no");
		boolean duplicateMrNoExists = false;
		int centerId = RequestContext.getCenterId();
        Map patmap = null;
        if (mrno != null && !mrno.equals("")) {
          BasicDynaBean patientDetailsBean = patientDetailsRepository.findByKey("mr_no", mrno);
          if (patientDetailsBean != null
              && !StringUtils.isEmpty((String) patientDetailsBean.get("original_mr_no"))) {
            mrno = (String) patientDetailsBean.get("original_mr_no");
          }
          patmap = PatientDetailsDAO.getPatientDetailsWithEmrAccess(mrno);
          if (patmap == null) {
            FlashScope flash = FlashScope.getScope(req);
            flash.put("error", mrno + " does not exist.");
            ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
			}
		}

		String screenId = (String) req.getAttribute("screenId");
		BasicDynaBean visitBean = null;
		String searchVisitID = screenId.equals("visit_emr_screen") ? (String) req.getParameter("visit_id") : "";
		if (searchVisitID != null && !searchVisitID.equals("")) {
			visitBean = new VisitDetailsDAO().getVisitDetailsWithEmrAccess(searchVisitID);
			if (visitBean == null) {
				FlashScope flash = FlashScope.getScope(req);
				flash.put("error", "Visit: "+searchVisitID +" does not exist.");
				ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}


		String visitid = null;
		if(screenId.equals("visit_emr_screen") &&
				(req.getParameter("VisitId") != null && !req.getParameter("VisitId").equals(""))) {
			visitid = req.getParameter("VisitId");
		}

		if ((mrno == null || mrno.equals("")) && (visitid == null || visitid.equals(""))) {
			mrno = req.getParameter("userId");
		}

		String filterType = "docType"; // for visit emr it will always filters by document type.
		if ((screenId.equals("emr_screen") || screenId.equals("emr_screen_without_mrno_search")) && req.getParameter("filterType") != null)
			filterType = req.getParameter("filterType");

		String portalLink = req.getParameter("mrNoLink");

		List <EMRDoc> allDocs = new ArrayList<>();
		
		ExecutorService exec = Executors.newFixedThreadPool(2);
		int noOfProviders = EMRInterface.Provider.values().length;
		/*Currently splitting the providers array in 2.
		 * to be done: make array split configurable to gauge multiple thread(s) performance.
		 */
		int firstHalf = noOfProviders/2;
		int secHalf = noOfProviders-firstHalf;
		EmrListDocuments listDocsFirst= null;
		EmrListDocuments listDocsSecond = null;
		
		if (mrno != null && !mrno.equals("")) {
		  /*First half of providers processed by thread 1 and other by thread 2*/
		  listDocsFirst = new EmrListDocumentsMrNo(0, firstHalf, req, mrno); 
		  listDocsSecond = new EmrListDocumentsMrNo(secHalf-1, noOfProviders, req, mrno);
		  updateAllDocs(allDocs, listDocsFirst, listDocsSecond, exec);
		 
          String[] duplicateMrnos = PatientDetailsDAO.getDuplicateMrNos(mrno);
          for(int i=0; i<duplicateMrnos.length; i++) {
            listDocsFirst = new EmrListDocumentsMrNo(0, firstHalf, req, duplicateMrnos[i]);
            listDocsSecond = new EmrListDocumentsMrNo(secHalf-1, noOfProviders, req, duplicateMrnos[i]);
            updateAllDocs(allDocs, listDocsFirst, listDocsSecond, exec);
          }
        if (duplicateMrnos.length > 0)
          duplicateMrNoExists = true;
		}
		String visitNotEmpty = (visitid != null && !visitid.equals(""))? visitid : searchVisitID;
		if ((visitid != null && !visitid.equals(""))|| (searchVisitID != null && !searchVisitID.equals(""))) {
		  listDocsFirst = new EmrListDocumentsVisit(0, firstHalf, req, visitNotEmpty);
		  listDocsSecond = new EmrListDocumentsVisit(secHalf-1, noOfProviders, req, visitNotEmpty);
		  updateAllDocs(allDocs, listDocsFirst, listDocsSecond, exec);
        } else if (mrno != null && !mrno.equals("")) {
          listDocsFirst = new EmrListDocumentsVisitsForMrNo(0, firstHalf, req, mrno);
          listDocsSecond = new EmrListDocumentsVisitsForMrNo(secHalf-1, noOfProviders, req, mrno);
          updateAllDocs(allDocs, listDocsFirst, listDocsSecond, exec);
      
          String[] duplicateMrnos = PatientDetailsDAO.getDuplicateMrNos(mrno);
          for (int i = 0; i < duplicateMrnos.length; i++) {
            listDocsFirst = new EmrListDocumentsVisitsForMrNo(0, firstHalf, req, duplicateMrnos[i]);
            listDocsSecond =
                new EmrListDocumentsVisitsForMrNo(secHalf-1, noOfProviders, req, duplicateMrnos[i]);
            updateAllDocs(allDocs, listDocsFirst, listDocsSecond, exec);
          }
          if (duplicateMrnos.length > 0)
            duplicateMrNoExists = true;
        }
		/*Shut down executor service of thread*/
		exec.shutdown();

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

		BasicDynaBean centerPreferencesUrls = CenterPreferencesDAO.getCentersPreferencesUrls(centerId);
		String mrnoUrl = (String)centerPreferencesUrls.get("pacs_mrno_url");
		String orderUrl = (String)centerPreferencesUrls.get("pacs_order_url");
		if(mrnoUrl != null)
			req.setAttribute("mrnoUrl", mrnoUrl.replace("$M", ""));
		if(orderUrl != null)
			req.setAttribute("orderUrl", orderUrl);

		if(visitBean == null && (visitNotEmpty != null && !visitNotEmpty.equals(""))){
			visitBean = new VisitDetailsDAO().getVisitDetailsWithEmrAccess(visitNotEmpty);
		}
		req.setAttribute("mr_no", visitBean!= null?  (String)visitBean.get("mr_no"): mrno);
		req.setAttribute("visit_id", visitBean!= null? (String)visitBean.get("patient_id"):visitNotEmpty);
		req.setAttribute("duplicateMrNoExists", duplicateMrNoExists);
		req.setAttribute("mandate_emr_comments", patmap != null ? patmap.get("mandate_emr_comments") 
				: (visitBean != null ? visitBean.get("mandate_emr_comments") : ""));
		Boolean hasMalaffiRole = false;
		if (userService.getUserMalaffiRole(RequestContext.getUserName()) != null) {
			hasMalaffiRole = true;
		}
		req.setAttribute("hasMalaffiRole", hasMalaffiRole);

		return m.findForward("list");
	}

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

	public ActionForward getDuplicatePatientDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		String mrNo = request.getParameter("mrNo");
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		PagedList dupPatientList = PatientDetailsDAO.getDuplicatePatientsDetails(mrNo,listingParams);

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");

		js.deepSerialize(dupPatientList, response.getWriter());
		return null;
	}

	public ActionForward getTestDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		int reportId = Integer.parseInt(request.getParameter("reportId"));
		List l = DIAGProviderBOImpl.getPrescribedTestsList(reportId);

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write(js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(l)));

		return null;
	}

	public ActionForward getTestDocumentDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		int docId = Integer.parseInt(request.getParameter("docId"));
		List l = DIAGProviderBOImpl.getTestsDocumentList(docId);

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write(js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(l)));

		return null;
	}
	
	@IgnoreConfidentialFilters
	public ActionForward getexternalreport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws ConnectionException, Exception{
		
		String error = "";
		String VisitNo = request.getParameter("_external_visit_id");						
		ExternalReportAction  er = new ExternalReportAction();
		
		error = er.getExternalReportData(response,VisitNo,error);
				
		if (!error.equals("")) {
			Document document = new Document();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph(error));
            document.close();
			response.setHeader("Expires", "0");
	        response.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0");
	        response.setHeader("Pragma", "public");
    	    response.setContentType("application/pdf");
    	    response.setContentLength(baos.size());
    	    OutputStream out = response.getOutputStream();
            baos.writeTo(out);
		    out.flush();
		    out.close();
		}		
	    return null;		
	}
	
    private void updateAllDocs(List<EMRDoc> allDocs, EmrListDocuments list1, EmrListDocuments list2,
        ExecutorService exec) throws Exception {
        /*Future objects which are computed asynchronously by the threads */
        Future<List <EMRDoc>> allDocsFutureFirst = null;
        Future<List <EMRDoc>> allDocsFutureSecond = null;
        /*submitting the callable task to threads for computation */
        allDocsFutureFirst = exec.submit(list1);
        allDocsFutureSecond = exec.submit(list2);
        /* adds the computed results from both threads in allDocs*/
        allDocs.addAll(allDocsFutureFirst.get());
        allDocs.addAll(allDocsFutureSecond.get());
      }
	
}
