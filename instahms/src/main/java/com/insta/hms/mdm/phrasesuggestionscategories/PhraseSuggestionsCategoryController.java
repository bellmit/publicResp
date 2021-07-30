/** */

package com.insta.hms.mdm.phrasesuggestionscategories;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** 
 * @author sonam.
 * */
@Controller
@RequestMapping(URLRoute.PHRASE_SUGGESTIONS_CATEGORY_PATH)
public class PhraseSuggestionsCategoryController extends MasterController {
  public PhraseSuggestionsCategoryController(PhraseSuggestionsCategoryService service) {
    super(service, MasterResponseRouter.PHRASE_SUGGESTIONS_CATEGORY_MASTER_ROUTER);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((PhraseSuggestionsCategoryService) getService()).getListPageData();
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((PhraseSuggestionsCategoryService) getService()).getAddEditPageData();
  }
}
