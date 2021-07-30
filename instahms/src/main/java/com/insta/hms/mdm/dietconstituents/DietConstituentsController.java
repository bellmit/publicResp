package com.insta.hms.mdm.dietconstituents;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DietConstituentsController.
 */
@RestController
@RequestMapping(URLRoute.DIET_CONSTITUTENTS_PATH)
public class DietConstituentsController extends BaseRestController {

  @LazyAutowired
  private DietConstituentsService service;

  /**
   * Gets the constituents for diet for a diet id.
   *
   * @param dietId
   *          the diet id
   * @return the constituents for diet
   */
  @SuppressWarnings("unchecked")
  @GetMapping(value = "/getconstituentsfordiet")
  public Map<String, Object> getConstituentsForDiet(
      @RequestParam(required = true, name = "diet_id") Integer dietId) {
    List<Map<String, Object>> constitutents = service.getConstituentsForDiet(dietId);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("constituents", constitutents);
    return responseMap;
  }

}
