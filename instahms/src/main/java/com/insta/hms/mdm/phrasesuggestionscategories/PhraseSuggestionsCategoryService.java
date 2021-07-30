/** */

package com.insta.hms.mdm.phrasesuggestionscategories;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * @author sonam.
 * */
@Service
public class PhraseSuggestionsCategoryService extends MasterService {
  public PhraseSuggestionsCategoryService(
      PhraseSuggestionsCategoryRepository repository, 
      PhraseSuggestionsCategoryValidator validator) {
    super(repository, validator);
  }

  /**
   * @return map of phrase Suggestions Category List.
   */
  public Map<String, List<BasicDynaBean>> getListPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("phraseSuggestionsCategoryList", lookup(false));
    return map;
  }

  /**
   * @return map containing phrase Suggestions Category List and phrase Suggestions List.
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("phraseSuggestionsCategoryList", lookup(false));
    map.put(
        "phraseSuggestinList",
        null); //need to replace null by lookup method of PhraseSuggestionsService once it done
    return map;
  }
}
