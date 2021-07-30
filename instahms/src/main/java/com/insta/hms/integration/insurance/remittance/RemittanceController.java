package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.URLRoute;
import com.insta.hms.mdm.bulk.CsVModelAndView;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class RemittanceController.
 */
@Controller("remittanceController")
@RequestMapping(URLRoute.REMITTANCE_UPLOAD)
public class RemittanceController extends BaseController {

  /** The remittance service. */
  @Autowired
  private RemittanceService remittanceService;

  /**
   * Add new remittance file page.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @return the model and view
   */
  @RequestMapping(value = "/add", method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public ModelAndView add(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    ModelAndView modelView = new ModelAndView();

    List<BasicDynaBean> insCompList = remittanceService.getInsuCompList();
    modelView.addObject("insCompList", ConversionUtils.listBeanToListMap(insCompList));

    List<BasicDynaBean> insCompTpaList = remittanceService.companyTpaXMLList();
    modelView.addObject("insCompTpaList", ConversionUtils.listBeanToListMap(insCompTpaList));

    List<BasicDynaBean> xmlTpaList = remittanceService.allXmlTpaList();
    modelView.addObject("xmlTpaList", ConversionUtils.listBeanToListMap(xmlTpaList));

    List<BasicDynaBean> tpaList = remittanceService.xmlTpaList();
    modelView.addObject("tpaList", ConversionUtils.listBeanToListMap(tpaList));

    List<BasicDynaBean> tpaCenterList = remittanceService.tpaCenterList();
    modelView.addObject("tpaCenterList", ConversionUtils.listBeanToListMap(tpaCenterList));

    int userCenterId = (Integer) request.getSession(false).getAttribute("centerId");
    List<BasicDynaBean> accGrpAndCenterList =
        remittanceService.accountgrpAndCenterView(userCenterId);
    modelView.addObject("accountGrpAndCenterList", accGrpAndCenterList);

    modelView.setViewName(URLRoute.REMITTANCE_UPLOAD_PAGE);
    return modelView;
  }

  /**
   * Export errors as xml.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @param remittanceId the remittance id who's errors we want to download
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/exporterror"}, method = RequestMethod.GET)
  public ModelAndView export(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, @RequestParam String remittanceId) {

    BasicDynaBean bean = remittanceService.getFileName(Integer.parseInt(remittanceId));
    String filebean = (String) bean.get("file_name");
    String filename = filebean.substring(0, filebean.lastIndexOf('.'));

    List<String[]> rows = remittanceService.processErrors(Integer.parseInt(remittanceId));
    String[] headers = new String[] {"ClaimId", "ActivityId", "Error Message"};
    CsVModelAndView mav = new CsVModelAndView(filename, headers);

    mav.addObject("headers", headers);
    mav.addObject("rows", rows);

    return mav;
  }

  /**
   * Import data.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @param redirect the redirect
   * @param file the file
   * @return the model and view
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  @RequestMapping(value = {"/upload"}, method = RequestMethod.POST)
  public ModelAndView importData(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("remittance_metadata") MultipartFile file)
      throws SAXException, java.io.IOException, java.text.ParseException {

    ModelAndView mav = new ModelAndView();
    HttpSession session = request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    Map<String, String[]> parameters = request.getParameterMap();
    String msg = remittanceService.create(parameters, file, centerId, null);

    redirect.addFlashAttribute("info", msg);
    mav.setViewName("redirect:add");

    return mav;
  }

  /**
   * List.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {

    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> parameters =  new HashMap<>(req.getParameterMap());
    PagedList pagedList = remittanceService.search(parameters);
    modelView.addObject("pagedList", pagedList);
    modelView.setViewName(URLRoute.REMITTANCE_UPLOAD_PAGE_LIST);
    return modelView;
  }

  /**
   * Remittancedownloadlist.
   *
   * @param req the req
   * @param resp the resp
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"", "/remittanceDownloadList"}, method = RequestMethod.GET)
  public ModelAndView remittanceDownloadList(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, SAXException, SQLException {
    ModelAndView modelView = new ModelAndView();
    Map<String, String[]> parameters = req.getParameterMap();
    final String startDate = parameters.get("received_start_date") != null
        ? parameters.get("received_start_date")[0] : null;
    final String endDate = parameters.get("received_end_date") != null
        ? parameters.get("received_end_date")[0] : null;
    PagedList radownloadlist = null;
    try {
      radownloadlist = remittanceService.radownloadlist(parameters, null);
    } catch (ConnectException connectException) {
      String createdMessage = connectException.getMessage();
      modelView.addObject("info", createdMessage);
    } catch (ValidationException validationException) {
      String createdMessage = validationException.getMessage();
      modelView.addObject("info", createdMessage);
    }
    /**
     * Setting in received date, from the date Default date range for last week (current date
     * -7) and To date End date always defaults to current date. IF we will search start and
     * end date's then it should setting in receiving date, from the date to date.
     */
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -7);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    modelView.addObject("pagedList", radownloadlist);
    if (startDate != null) {
      modelView.addObject("received_start_date", startDate);
    } else {
      modelView.addObject("received_start_date", dateFormat.format(cal.getTime()));
    }
    if (endDate != null) {
      modelView.addObject("received_end_date", endDate);
    } else {
      modelView.addObject("received_end_date", dateFormat.format(new Date()));
    }
    modelView.setViewName(URLRoute.REMITTANCE_DOWNLOAD_PAGE_LIST);

    // Adding generic preferences
    modelView.addObject("genericPreferences", remittanceService.getGenericPreferences());
    return modelView;
  }

  /**
   * RemittancedownloadProcess.
   *
   * @param req the req
   * @param res the res
   * @param redirect the redirect
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   * @throws SAXException the SAX exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/raDownloadProcess", method = RequestMethod.GET)
  public ModelAndView radownloadprocess(HttpServletRequest req, HttpServletResponse res,
      RedirectAttributes redirect)
      throws IOException, java.text.ParseException, SQLException, SAXException {
    // getting an unlocked map
    Map<String, String[]> newParameters = new HashMap<>(req.getParameterMap());
    Map<String, String[]> parameters = req.getParameterMap();
    String msg = remittanceService.raDownloadProcessFile(newParameters, null, null);
    redirect.addAllAttributes(parameters);
    req.setAttribute("info", msg);
    return remittanceDownloadList(req, res);
  }

  /**
   * RemittancedownloadProcess.
   *
   * @param req the http req
   * @param res the res
   * @param redirect the redirect
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/raDownload", method = RequestMethod.GET)
  public void raDownload(HttpServletRequest req, HttpServletResponse res,
      RedirectAttributes redirect) throws IOException, SQLException {
    Map<String, String[]> parameters = req.getParameterMap();
    MultipartFile xmlFile = remittanceService.raDownloadFile(parameters);
    String mimeType = "application/octet-stream";
    res.setContentType(mimeType);
    res.setHeader("Content-Disposition",
        String.format("attachment; filename=\"" + xmlFile.getName() + "\""));
    StreamUtils.copy(xmlFile.getInputStream(), res.getOutputStream());
  }
  
}
