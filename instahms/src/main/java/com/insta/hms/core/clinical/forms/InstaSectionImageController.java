package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.URLRoute;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InstaSectionImageController.
 *
 * @author krishnat
 */
@Controller
@RequestMapping(value = URLRoute.FORMS_IMAGE_URL)
public class InstaSectionImageController extends BaseController {

  /** The service. */
  @LazyAutowired
  InstaSectionImageService service;

  /**
   * Save image.
   *
   * @param req the req
   * @param resp the resp
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = {"/saveImage"}, method = RequestMethod.POST)
  public Map<String, Object> saveImage(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("image_id", service.insertImage(params, fileMap));
    return map;
  }

  /*
   * if the image is not being used by transaction, then delete the image and reinsert. we choose to
   * delete and insert instead of update, because
   * 
   * 1) The image might have opened in another form by another user but not saved. 2) In the mean
   * time, the first user has updated an new image. 3) After that the second user completed his
   * form, placing markers on the old image and clicked on save. 4) Now, after save of the form,
   * second user starts seeing the new image, which was uploaded by user1. which is wrong.
   */
  /**
   * Update image.
   *
   * @param req the req
   * @param resp the resp
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = {"/updateImage"}, method = RequestMethod.POST)
  public Map<String, Object> updateImage(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    Map<String, String[]> params = req.getParameterMap();
    Map<String, MultipartFile> fileMap = super.getFiles(req);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("image_id", service.updateImage(params, fileMap));
    return map;
  }

  /**
   * Gets the image.
   *
   * @param request the request
   * @param resp the resp
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getImage", method = RequestMethod.GET)
  public void getImage(HttpServletRequest request, HttpServletResponse resp) throws IOException {
    HttpHeaders headers = new HttpHeaders();

    String imageIdStr = request.getParameter("image_id");
    int imageId = 0;
    if (imageIdStr != null && !imageIdStr.equals("")) {
      imageId = Integer.parseInt(imageIdStr);
    }
    String fieldIdStr = request.getParameter("field_id");
    int fieldId = 0;
    if (imageId == 0 && fieldIdStr != null && !fieldIdStr.equals("")) {
      fieldId = Integer.parseInt(fieldIdStr);
    }

    BasicDynaBean bean = service.getImage(imageId, fieldId);
    resp.addHeader("Content-Type", (String) bean.get("content_type"));

    OutputStream responseStream = null;
    try {
      responseStream = resp.getOutputStream();
      StreamUtils.copy((InputStream) bean.get("file_content"), responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }

}
