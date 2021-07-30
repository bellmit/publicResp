package com.insta.hms.mdm.documenttypecategory;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import org.springframework.stereotype.Service;

@Service
public class DocumentCategoryService extends MasterService {

  @LazyAutowired
  private DocumentCategoryRepository documentCategoryRepository;

  /**
   * Instantiates a new master service.
   * @param documentCategoryRepository the repository
   * @param documentTypeCategoryValidator the validator
   */
  public DocumentCategoryService(DocumentCategoryRepository documentCategoryRepository,
      DocumentTypeCategoryValidator documentTypeCategoryValidator) {
    super(documentCategoryRepository, documentTypeCategoryValidator);
  }
}