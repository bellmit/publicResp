package com.insta.hms.mdm.otheridentificationdocument;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class OtherIdentificationDocumentTypesService extends MasterService {

  @LazyAutowired
  OtherIdentificationDocumentTypesRepository otherIdentiDocTypRepository;

  public OtherIdentificationDocumentTypesService(
      OtherIdentificationDocumentTypesRepository repository,
      OtherIdentificationDocumentTypesValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("otherIdentifierTypeDetails", lookup(false));
    return map;
  }
  

}
