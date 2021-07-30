package com.insta.hms.mdm.phrasesuggestions;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.phrasesuggestionscategories.PhraseSuggestionsCategoryService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phrase Suggestions Service.
 * @author Sonam
 */
@Service 
public class PhraseSuggestionsService extends MasterService {
  @LazyAutowired private DepartmentService deptService;
  @LazyAutowired private PhraseSuggestionsCategoryService phraseSuggCatService;
  @LazyAutowired private PhraseSuggestionsRepository phraseSuggestionsRepository;

  public PhraseSuggestionsService(PhraseSuggestionsRepository repository,
      PhraseSuggestionsValidator validator) {
    super(repository, validator);
  }

  /**
   * @return Map.
   * 
   */
  public Map<String, List<BasicDynaBean>> getListPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("phraseSuggestionsList", lookup(false));
    map.put("phraseSuggestionsDeptList", deptService.lookup(true));
    map.put("phraseSuggCategoryList", phraseSuggCatService.lookup(true));

    return map;
  }

  /**
   * @return map.
   * 
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("phraseSuggestionsDeptList", deptService.lookup(true));
    map.put("phraseSuggCategoryList", phraseSuggCatService.lookup(true));
    return map;
  }

  /**
   * @param deptId department ID.
   * @return list
   */
  public List<BasicDynaBean> getPhraseSuggestionsDeptWise(String deptId) {
    return phraseSuggestionsRepository.getPhraseSuggestionsDeptWise(deptId);
  }
}
