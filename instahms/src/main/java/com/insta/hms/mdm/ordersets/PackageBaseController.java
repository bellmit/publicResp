package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageBaseController.
 */
public class PackageBaseController extends MasterDetailsRestController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PackageBaseController.class);

  /** The master type. */
  private String masterType;

  /**
   * Instantiates a new package base controller.
   *
   * @param service the service
   * @param masterType the master type
   */
  public PackageBaseController(GenericPackagesService service, String masterType) {
    super(service);
    this.masterType = masterType;
  }

  /**
   * Gets the order sets index page.
   *
   * @return the order sets index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getOrderSetsIndexPage() {
    Map<String, String> globalVar = new HashMap<>();
    globalVar.put("masterType", this.masterType);
    //Sending flow type due to master requirement.
    globalVar.put("flowType", "opFlow");
    return renderMasterUi("Master", "hospitalBillingMasters", globalVar);
  }

  /**
   * Meta data.
   *
   * @return the response entity
   * @throws Exception the exception
   */
  @RequestMapping(value = "/metadata", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> metaData() throws Exception {
    return new ResponseEntity<>(((GenericPackagesService) getService()).metaData(), HttpStatus.OK);
  }

  /**
   * Advance list.
   *
   * @param params the params
   * @return the response entity
   * @throws ParseException the parse exception
   */
  @RequestMapping(value = "/advanceList", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> advanceList(
      @RequestParam MultiValueMap<String, Object> params) throws ParseException {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.putAll(((GenericPackagesService) getService())
        .advanceList(new InstaLinkedMultiValueMap<>(params)));
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

}
