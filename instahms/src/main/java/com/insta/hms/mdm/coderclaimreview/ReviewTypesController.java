package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * The Class ReviewTypesController.
 *
 * @author allabakash
 */
@Controller
@RequestMapping(URLRoute.REVIEW_TYPES_PATH)
public class ReviewTypesController extends MasterDetailsRestController {

  /**
   * Instantiates a new review types controller.
   *
   * @param service
   *          the service
   */
  public ReviewTypesController(ReviewTypesService service) {
    super(service);
  }

  /**
   * Gets the review types index page.
   *
   * @return the review types index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getReviewTypesIndexPage() {
    return renderMasterUi("Master", "hospitalAdminMasters");
  }

  /**
   * Meta data.
   *
   * @return the response entity
   */
  @GetMapping(value = "/metadata")
  public ResponseEntity<Map<String, Object>> metaData() {
    return new ResponseEntity<>(((ReviewTypesService) getService()).metaData(),
        HttpStatus.OK);
  }

}
