package com.insta.hms.core.billing;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping(URLRoute.GET_NEW_BILL)
@RestController("newBillController")

public class NewBillController extends BaseRestController {

static Logger log = LoggerFactory.getLogger(NewBillController.class);

  @LazyAutowired
  private NewBillService newbillService;

  @RequestMapping(value = { "", "/get" }, method = RequestMethod.GET)
  public ModelAndView getNewBillcreation() {

    ModelAndView model = new ModelAndView();
    model.setViewName(URLRoute.GET_NEW_BILL_CREATION_PAGE);
    return model;
  }

  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> createBill(@RequestBody ModelMap requestBody) {
    boolean isInsurance = (boolean) requestBody.get("is_insurance");
    String visitId = (String) requestBody.get("visit_id");
    String billType = (String) requestBody.get("bill_type");
    return newbillService.createBill(isInsurance, visitId, billType);

  }

  @RequestMapping(value = "getallvisit", method = RequestMethod.GET)
  public Map<String, Object> getAllVisitDetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, @RequestParam(value = "mr_no") String mrNo,
      @RequestParam(value = "visit_type") String visitType) {
    return newbillService.getAllVisitDetails(mrNo, visitType);
  }

}
