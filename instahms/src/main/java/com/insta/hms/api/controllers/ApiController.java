package com.insta.hms.api.controllers;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class ApiController extends BaseRestController {

  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * Show version.
   *
   * @param req      the req
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = "/version", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> showVersion(HttpServletRequest req,
      HttpServletResponse response) {
    String version = messageUtil.getMessage("insta.software.version", null);
    String successMsg = messageUtil.getMessage("api.success", null);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("version", version);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

}
