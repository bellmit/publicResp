/** */

package com.insta.hms.mdm.phrasesuggestions;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** @author sonam.
 *  */
@Controller
@RequestMapping(URLRoute.PHRASE_SUGGESTIONS_PATH)
public class PhraseSuggestionsController extends MasterController {
  public PhraseSuggestionsController(PhraseSuggestionsService service) {
    super(service, MasterResponseRouter.PHRASE_SUGGESTIONS_ROUTER);
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {
    return ((PhraseSuggestionsService) getService()).getListPageData();
  }

  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((PhraseSuggestionsService) getService()).getAddEditPageData();
  }
}
