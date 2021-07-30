package com.insta.hms.core.help;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.practo.allspark.AllsparkClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Help Controller.
 */
@RestController
@RequestMapping("/help")
public class HelpController extends BaseController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(HelpController.class);

  @LazyAutowired
  private AllsparkClient allsparkClient;
  
  /**
   * Index.
   *
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/index" }, method = RequestMethod.GET)
  public ModelAndView index() {
    return new ModelAndView("/pages/help");
  }

  /**
   * Downloads.
   *
   * @param request
   *          the request
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/downloads" }, method = RequestMethod.GET)
  public ModelAndView downloads(HttpServletRequest request) {
    String nexusBase = "/instanexus";
    Yaml yaml = new Yaml();
    StringBuilder yamlFile = new StringBuilder();
    try {
      String realPath = request.getServletContext().getRealPath(request.getServletPath());
      logger.info(realPath);
      String instahmsPath = realPath.substring(0, realPath.indexOf("/help"));
      logger.info(instahmsPath);
      String nexusLatestYmlFile = instahmsPath.substring(0, instahmsPath.lastIndexOf("/"))
          + "/instanexus/latest.yml";
      logger.info(nexusLatestYmlFile);
      InputStream ios = new FileInputStream(new File(nexusLatestYmlFile));
      BufferedReader in = new BufferedReader(new InputStreamReader(ios));

      String inputLine;

      while ((inputLine = in.readLine()) != null) {
        yamlFile.append(inputLine).append("\n");
      }

      in.close();
    } catch (IOException ex) {
      logger.error("failed to access latest.yml");
    }
    logger.error(yamlFile.toString());
    Map<String, Object> yamlData = (Map<String, Object>) yaml.load(yamlFile.toString());
    String nexusVersion = (String) yamlData.get("version");
    String nexusDownloadFile = nexusBase + "/" + ((String) yamlData.get("path"));
    String nexusLinuxVersion = (String) yamlData.get("linuxVersion");
    String nexusLinux64DownloadFile = nexusBase + "/" + ((String) yamlData.get("linuxPath64"));
    String nexusLinux32DownloadFile = nexusBase + "/" + ((String) yamlData.get("linuxPath32"));
    ModelAndView mav = new ModelAndView("/pages/downloads");
    mav.addObject("nexus_base", nexusBase);
    mav.addObject("nexus_version", nexusVersion);
    mav.addObject("nexus_download_url", nexusDownloadFile);
    mav.addObject("nexus_linux_version", nexusLinuxVersion);
    mav.addObject("nexus_linux64_download_url", nexusLinux64DownloadFile);
    mav.addObject("nexus_linux32_download_url", nexusLinux32DownloadFile);

    return mav;
  }

  /**
   * Queue Callback request for support.
   *
   * @param request
   *          the request
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = {"/requestcallback"} , method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> requestCallbackProxy(
      HttpServletRequest request) throws URISyntaxException, IOException {
    Map<String, Object> requestParameters = new HashMap<>();
    requestParameters.put("requestee", request.getParameter("requestee"));
    requestParameters.put("callback_number", request.getParameter("callback_number"));
    requestParameters.put("current_url", request.getParameter("current_url"));
    allsparkClient.attachSessionInfo(requestParameters);
    return allsparkClient.callSupportPostAPI("/_requestcallback", requestParameters);
  }
}
