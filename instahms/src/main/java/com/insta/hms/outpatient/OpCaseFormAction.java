package com.insta.hms.outpatient;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class OpCaseFormAction.
 */
public class OpCaseFormAction extends DispatchAction {

  /** The Constant drConDao. */
  static DoctorConsultationDAO drConDao = new DoctorConsultationDAO();

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, ParseException, IOException {

    Map params = req.getParameterMap();
    String consIdStr = req.getParameter("consultation_id");
    int consId = Integer.parseInt(consIdStr);
    BasicDynaBean consultation = drConDao.findDepartmentId(consId);
    req.setAttribute("consultation", consultation.getMap());

    String documentType = mapping.getProperty("documentType");
    Boolean specialized = new Boolean(mapping.getProperty("specialized"));

    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    AbstractDocumentPersistence persistenceAPI = AbstractDocumentPersistence
        .getInstance(documentType, specialized);
    Map extraParams = persistenceAPI.populateKeys(params, null);
    req.setAttribute("patientDocs",
        persistenceAPI.searchDocuments(listingParams, extraParams, specialized, documentType));

    Map filterParams = new HashMap();
    filterParams.put("doc_type_id", new String[] { "SYS_OP" });
    filterParams.put("status", new String[] { "A" });
    filterParams.put("dept_name", new String[] { (String) consultation.get("dept_id"), "DEP0001" });
    Map<LISTING, Object> listing = ConversionUtils.getListingParameter(filterParams);
    String templatePageNum = req.getParameter("templatePageNum");
    int pageNum = 1;
    if (templatePageNum != null && !templatePageNum.equals("")) {
      pageNum = Integer.parseInt(templatePageNum);
    }
    listing.put(LISTING.PAGENUM, pageNum);
    PagedList list = GenericDocumentTemplateDAO.getGenericDocTemplates(filterParams, specialized,
        listing);
    req.setAttribute("pagedList", list);

    req.setAttribute("documentType", documentType);
    req.setAttribute("specialized", specialized);
    req.setAttribute("uploadFile", new Boolean(mapping.getProperty("uploadFile")));
    return mapping.findForward("OpCaseForm");
  }
}
