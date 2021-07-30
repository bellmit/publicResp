package com.insta.hms.mdm.indicationforcaesareansection;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.INDICATION_FOR_CAESAREAN_SECTION_MASTER)
public class IndicationForCaesareanSectionController extends MasterRestController {

  public IndicationForCaesareanSectionController(
      IndicationForCaesareanSectionService indicationForCaesareanSectionService) {
    super(indicationForCaesareanSectionService);
  }

  /**
   * Find indication for caesarean section based on filters.
   *
   * @param params
   *          filter map
   * @return map of indication for caesarean sections list
   */
  @GetMapping(value = URLRoute.FIND_BY_FILTER)
  public ResponseEntity<Map<String, Object>> findByFilters(
      @RequestParam Map<String, String> params) {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.putAll((
        (IndicationForCaesareanSectionService) getService()).findByFilters(params));

    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Gets the index page.
   *
   * @return the index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getIndexPage() {
    //return new ModelAndView(URLRoute.HOSPITAL_ADMIN_INDEX_PAGE);
    return renderMasterUi("Master","hospitalAdminMasters");
  }

}
