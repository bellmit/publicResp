package com.insta.hms.mdm.systemgeneratedsections;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.phrasesuggestionscategories.PhraseSuggestionsCategoryService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SystemGeneratedSectionsService.
 */
@Service
public class SystemGeneratedSectionsService extends MasterService {
  
  /** The system generated sections repository. */
  @LazyAutowired
  private SystemGeneratedSectionsRepository systemGeneratedSectionsRepository;
  
  /** The phrase suggestions category service. */
  @LazyAutowired
  private PhraseSuggestionsCategoryService phraseSuggestionsCategoryService;

  /**
   * Instantiates a new system generated sections service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public SystemGeneratedSectionsService(SystemGeneratedSectionsRepository repository,
      SystemGeneratedSectionsValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the sectionsdata.
   *
   * @param params the params
   * @return the sectionsdata
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getSectionsdata(Map params) {

    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    List<BasicDynaBean> sections = getSectionsdata();
    map.put("sections", sections);
    return map;
  }

  /**
   * Gets the sectionsdata.
   *
   * @return the sectionsdata
   */
  private List<BasicDynaBean> getSectionsdata() {
    return systemGeneratedSectionsRepository.getSectionsdata();
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    List<BasicDynaBean> phraseSuggestionsCategoryList = phraseSuggestionsCategoryService
        .lookup(false);
    map.put("phraseSuggestionsCategoryList", phraseSuggestionsCategoryList);
    return map;
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }

  /**
   * Find by key.
   *
   * @param sectionId the section id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int sectionId) {
    return systemGeneratedSectionsRepository.findByKey("section_id", sectionId);
  }
}
