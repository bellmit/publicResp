package com.insta.hms.common.preferences.genericpreferences;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.URLRoute;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MultipartController;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class GenericPreferencesController.
 */
@RestController
@RequestMapping(URLRoute.GENERIC_PREFERENCES)
public class GenericPreferencesController extends BaseRestController
    implements MultipartController {

  /** The gen service. */
  @LazyAutowired
  private GenericPreferencesService genService;

  /**
   * This end points updates the generic Preference.
   *
   * @param request the request
   * @param response the response
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @IgnoreConfidentialFilters
  @PostMapping(value = "/update")
  public Map<String, Object> update(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Map<String, Object> genPrefData = new HashMap<String, Object>();
    Map<String, MultipartFile> fileMap = super.getFiles(request);
    Map<String, String[]> params = request.getParameterMap();
    BasicDynaBean bean = genService.toBean(params, fileMap);
    genService.updateBean(params, bean);
    Integer ret = genService.updateFormImageData(fileMap.get("screenLogo"), bean);
    if (ret != 0) {
      Map<String, Object> beanMap = new HashMap(genService.getAllPreferences().getMap());
      genService.showBean(beanMap);
      genPrefData.put("bean", beanMap);
      genPrefData.put("fileSize", genService.getLogoSize().getMap());
    }
    return genPrefData;
  }

  /**
   *  This end Point returns screenLogo.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @GetMapping("viewScreenLogo")
  public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {

    BasicDynaBean bean = genService.getScreenLogo();
    response.setContentType("image/gif");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    OutputStream responseStream = null;
    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy((InputStream) bean.get("screen_logo"), responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }

  /**
   * This end Point sends only the generic preferences data.
   *
   * @return the generic preference data
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings({ "unchecked" })
  @GetMapping(value = "")
  public Map<String, Object> getGenericPreferenceData() {
    Map<String, Object> genericPrefData = new HashMap<String, Object>();
    genericPrefData.putAll(genService.getAllPreferences().getMap());
    return genericPrefData;
  }

  /**
   * This end Point sends generic Preference data along with other details.
   *
   * @return the map
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GetMapping(value = "/show")
  public Map<String, Object> show(HttpServletRequest request) {

    Map<String, Object> genricPrefData = new HashMap<String, Object>();
    BasicDynaBean bean = genService.getAllPreferences();
    Map<String, Object> beanMap = new HashMap(bean.getMap());
    genService.showBean(beanMap);
    genricPrefData.put("bean", beanMap);
    genricPrefData.putAll(genService.getAdditionalData((String) request.getAttribute("language")));
    return genricPrefData;
  }

  /** This end point deletes the screenLogo. */
  @IgnoreConfidentialFilters
  @DeleteMapping("deleteScreenLogo")
  public void delete() {

    genService.deleteScreenLogo();
  }
}
