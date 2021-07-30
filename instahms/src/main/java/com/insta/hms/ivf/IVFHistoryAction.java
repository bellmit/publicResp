package com.insta.hms.ivf;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class IVFHistoryAction.
 */
public class IVFHistoryAction extends DispatchAction {
  
  private static final GenericDAO ivfCompleEmbryoInf = new GenericDAO("ivf_comple_embryo_inf");

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {

    HashMap<String, Object> filter = new HashMap<String, Object>();
    String mrNo = request.getParameter("mr_no");
    filter.put("mr_no", mrNo);
    PagedList list = null;
    List etList = null;
    List efList = null;

    if (null != mrNo) {
      list = IVFSessionDAO.getIVFHistory(filter,
          ConversionUtils.getListingParameter(request.getParameterMap()));
      etList = ConversionUtils.listBeanToListMap(ivfCompleEmbryoInf.listAll(
          null, "embryo_op", "T", "ivf_cycle_id"));
      efList = ConversionUtils.listBeanToListMap(ivfCompleEmbryoInf.listAll(
          null, "embryo_op", "F", "ivf_cycle_id"));
      String ivfCycleID = new IVFSessionDAO().findByKey("mr_no", mrNo).get("ivf_cycle_id")
          .toString();
    }

    request.setAttribute("etList", etList);
    request.setAttribute("efList", efList);
    request.setAttribute("PagedList", list);
    return mapping.findForward("list");
  }

  /**
   * Gets the patient visit details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the patient visit details
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward getPatientVisitDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {

    ActionRedirect redirect = null;
    String visitId = request.getParameter("patient_id");
    Map visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);

    if (visitbean == null) {
      FlashScope flash = FlashScope.getScope(request);
      flash.put("error", visitId + " doesn't exists.");
      redirect = new ActionRedirect(mapping.findForward("listRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    redirect.addParameter("mr_no", visitbean.get("mr_no"));
    return redirect;
  }

  /**
   * Gets the IVF history print.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the IVF history print
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward getIVFHistoryPrint(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      TemplateException, DocumentException, XPathExpressionException, ParseException {

    String mrNo = request.getParameter("mr_no");

    Map map = new HashMap();
    map.put("patient", PatientDetailsDAO.getPatientGeneralDetailsMap(
        request.getParameter("mr_no")));
    map.put("hospitalName", request.getSession(false).getAttribute("sesHospitalId"));
    map.put("ivfhistorydetails", IVFSessionDAO.getIVFHistoryPrint(mrNo));
    map.put("etList", ivfCompleEmbryoInf.listAll(null, "embryo_op", "T", "ivf_cycle_id"));
    map.put("efList", ivfCompleEmbryoInf.listAll(null, "embryo_op", "F", "ivf_cycle_id"));

    Template temp = AppInit.getFmConfig().getTemplate("IVFHistoryPrint.ftl");
    HtmlConverter htmlConverter = new HtmlConverter();

    StringWriter writer = new StringWriter();
    temp.process(map, writer);
    String textContent = writer.toString();
    OutputStream os = response.getOutputStream();
    response.setContentType("application/pdf");

    try {
      htmlConverter.writePdf(os, textContent);
    } finally {
      os.close();
    }
    return null;
  }

}
