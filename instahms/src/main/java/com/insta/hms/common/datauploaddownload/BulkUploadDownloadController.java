package com.insta.hms.common.datauploaddownload;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.FileOperationService.OperationScreenType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for csvUpload dashboard.
 */
@Controller("bulkUploadDownloadController")
@RequestMapping(UrlRoute.BULK_UPLOAD_DOWNLOAD)
public class BulkUploadDownloadController extends BaseRestController {

  /** Bulk Upload Download service. */
  @LazyAutowired
  private BulkUploadDownloadService bulkUploadDownloadService;

  /** message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** Session service. */
  @LazyAutowired
  private SessionService sessionService;

  private static Logger logger = LoggerFactory.getLogger(BulkUploadDownloadController.class);

  /**
   * lists the upload and download files background job.
   *
   * @param request  the request
   * @param response the response
   * @param attribs  the attribs
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = { "/list", "" })
  public ModelAndView list(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes attribs) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("csvUploadList", bulkUploadDownloadService.getAllBulkUploadDownloadJobs());
    mav.addObject("info", messageUtil.getMessage("csvUpload.available.for.72.hours"));
    mav.setViewName(UrlRoute.BULK_UPLOAD_DOWNLOAD_PAGE);
    return mav;
  }

  /**
   * This method moves the job to background and stores the key in redis key.
   * 
   * @param request       the request
   * @param response      the response
   * @param attribs       the attribs
   * @param multiPartFile the file
   * @return redirects to list
   * @throws IOException exception
   */
  @IgnoreConfidentialFilters
  @PostMapping(value = "/uploadDownloadFiles")
  public String uploadDownload(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes attribs,
      @RequestPart(value = "fileUpload", required = false) MultipartFile multiPartFile)
      throws IOException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("master", OperationScreenType.valueOf(request.getParameter("master")));
    map.put("action", request.getParameter("action"));
    map.put("code_system_category_id", request.getParameter("code_system_category_id"));
    map.put("code_systems_id", request.getParameter("code_systems_id"));
    if (request.getParameter("template") != null) {
      map.put("template", request.getParameter("template"));
    } else {
      map.put("template", "N");
    }

    if (request.getParameter("isCharges") != null) {
      map.put("orgId", request.getParameter("organization"));
    }

    if (request.getParameter("action").equalsIgnoreCase("Upload")) {
      Date date = new Date();
      String currentDate = new SimpleDateFormat("yyyyMMdd").format(date);

      String userName = sessionService.getSessionAttributes().get("userId").toString();
      String schema = sessionService.getSessionAttributes().get("sesHospitalId").toString();

      File receivedFile = new File(EnvironmentUtil.getTempDirectory() + File.separator + currentDate
          + File.separator + schema + File.separator + userName + File.separator
          + multiPartFile.getOriginalFilename());
      Path path = Paths.get(receivedFile.getParent());
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
      FileOutputStream fos = new FileOutputStream(receivedFile);
      fos.write(multiPartFile.getBytes());
      fos.close();
      map.put("file", receivedFile);
    }

    bulkUploadDownloadService.uploadDownload(map);
    return "redirect:list";
  }

  /**
   * This method displays the downloads file or displays error message.
   * 
   * @param request  the request
   * @param response the response
   * @throws IOException exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/download")
  public void download(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String redisId = request.getParameter("id");
    File file = bulkUploadDownloadService.getFile(redisId);
    Path filePath = Paths.get(file.getParent(), file.getName());

    if (bulkUploadDownloadService.getStatusForRedisKey(redisId).equalsIgnoreCase("fail")) {
      throw new HMSException(bulkUploadDownloadService.getMessage(redisId));
    } else {
      if (Files.exists(filePath)) {
        response.setContentType(Files.probeContentType(filePath));
        response.addHeader("Content-Disposition", "attachment; filename=" + file.getName());
        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
      } else {
        logger.error(messageUtil.getMessage("exception.file.not.not.found"));
        throw new HMSException(messageUtil.getMessage("exception.file.not.not.found"));
      }
    }
  }
}
