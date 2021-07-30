package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(URLRoute.REVIEW_CATEGORY_PATH)
public class ReviewCategoryController extends MasterDetailsRestController {

  public ReviewCategoryController(ReviewCategoryService service) {
    super(service);
  }

  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getReviewTypesIndexPage() {
    return renderMasterUi("Master", "hospitalAdminMasters");
  }
}
