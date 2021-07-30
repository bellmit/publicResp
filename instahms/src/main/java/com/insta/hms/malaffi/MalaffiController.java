package com.insta.hms.malaffi;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(URLRoute.MALAFFI)
public class MalaffiController extends BaseController {

  private static final Logger logger = LoggerFactory.getLogger(MalaffiController.class);

  @LazyAutowired
  private MalaffiService malaffiService;

  @GetMapping(value = "/patient/{mrNo}")
  public ModelAndView get(@PathVariable("mrNo") String mrNo) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("ssoUrl", malaffiService.getEndpointUrl());
    List<String> errors = new ArrayList<>();
    mav.addObject("xml", malaffiService.getBase64EncodedXml(mrNo, errors));
    mav.addObject("errors", errors);
    mav.setViewName(URLRoute.MALAFFI_INDEX_PAGE);
    return mav;
  }
}
