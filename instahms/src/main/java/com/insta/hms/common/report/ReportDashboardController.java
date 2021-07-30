package com.insta.hms.common.report;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for report dashboard.
 */
@Controller("reportDashboardController")
@RequestMapping(UrlRoute.REPORT_DASHBOARD)
public class ReportDashboardController extends BaseRestController {

  @LazyAutowired
  private ReportDashboardService dashboardService;

  @LazyAutowired
  private MessageUtil messageUtil;

  private static Logger logger = LoggerFactory.getLogger(ReportDashboardController.class);

  /**
   * Display list of all reports.
   *
   * @param request
   *          the request
   * @return the model and view
   */
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ModelAndView list(HttpServletRequest request) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("reportList", dashboardService.getAllReports());
    mav.addObject("info", messageUtil.getMessage("report.available.for.24.hours"));
    mav.setViewName(UrlRoute.REPORT_DASHBOARD_LIST);
    return mav;
  }

  /**
   * Download.
   *
   * @param request the request
   * @param response the response
   * @param attribs the attribs
   * @return the model and view
   */
  @RequestMapping(value = { "/download" }, method = RequestMethod.GET)
  public ModelAndView download(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes attribs) {
    ModelAndView mav = new ModelAndView();
    mav.setViewName("redirect:list");
    String redisKey;
    try {
      redisKey = URLDecoder.decode(request.getParameter("id"), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      redisKey = null;
      logger.debug("Unable to decode encoded param redisKey");
    }
    if (redisKey == null) {
      throw new EntityNotFoundException("No such report");
    } 
    String status = dashboardService.getStatusForRedisKey(redisKey);
    if (status == null) {
      throw new EntityNotFoundException("ui.message.no.such.report");
    } 
    if (!status.equalsIgnoreCase("completed") && !status.equalsIgnoreCase("failed")) {
      mav.setViewName(UrlRoute.REPORT_LOADER);      
      return mav;
    }
    String filePath = dashboardService.getFilePathForRedisKey(redisKey);
    File file = new File(filePath);
    try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
      String mimeType = null;
      String fileName = file.getName();
      String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
      if (fileExtension.equals("pdf")) {
        mimeType = "application/pdf";
      } else if (fileExtension.equals("csv")) {
        mimeType = "application/csv";
      } else if (fileExtension.equals("txt")) {
        mimeType = "text/plain";
      } else {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setContentLength((int) file.length());
      response.setHeader("Content-disposition", "inline; filename=\"" + fileName + "\"");
      FileCopyUtils.copy(inputStream, response.getOutputStream());

    } catch (IOException exception) {
      attribs.addFlashAttribute("error", messageUtil.getMessage("report.view.failed"));
      logger.error("exception while downloading file: " + file.getName(), exception);
    }
    return mav;

  }
}
