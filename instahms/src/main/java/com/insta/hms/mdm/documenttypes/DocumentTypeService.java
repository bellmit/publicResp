package com.insta.hms.mdm.documenttypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.documenttypecategory.DocumentCategoryMappingRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentTypeService extends MasterService {

  @LazyAutowired
  private DocumentTypeService documentTypeService;

  @LazyAutowired
  private DocumentCategoryMappingRepository documentCategoryMappingRepository;

  public DocumentTypeService(DocumentTypeRepository documentTypeRepository,
      DocumentTypeValidator documentTypeValidator) {
    super(documentTypeRepository, documentTypeValidator);
  }

  public List<BasicDynaBean> listAll(String sortColumn) {
    return getRepository().listAll(sortColumn);
  }

  /**
   * Get document types by category.
   * 
   * @param category
   *          document category
   * @return returns list of document types
   */
  public List getDocTypesByCategory(String category, String specialized) {
    return ((DocumentTypeRepository) getRepository()).getDocTypesByCategory(category, specialized);
  }

  /**
   * Insert a document.
   * 
   * @param documentBean
   *          document bean
   * @param params
   *          request parameters
   * @return returns integer
   */
  @Transactional(rollbackFor = Exception.class)
  public int insertDocumentType(BasicDynaBean documentBean, Map<String, String[]> params) {
    Integer ret = documentTypeService.insert(documentBean);
    List<BasicDynaBean> basicDynaBeans = new ArrayList<>();
    String[] array = params.get("selectedCategories");
    if (array != null && array.length > 0) {
      for (String s : array) {
        BasicDynaBean basicDynaBean = documentCategoryMappingRepository.getBean();
        basicDynaBean.set("doc_type_id", documentBean.get("doc_type_id"));
        basicDynaBean.set("doc_type_category_id", s);
        basicDynaBeans.add(basicDynaBean);
      }
      int[] results = documentCategoryMappingRepository.batchInsert(basicDynaBeans);
    }
    return ret;
  }

  /**
   * update a document bean.
   * 
   * @param documentBean
   *          document bean
   * @param params
   *          request params
   * @return returns integer
   */
  @Transactional(rollbackFor = Exception.class)
  public int updateDocumentType(BasicDynaBean documentBean, Map<String, String[]> params) {
    Integer ret = documentTypeService.update(documentBean);
    int existingDocs = documentCategoryMappingRepository
        .deleteDocTypesMappingByDocId((String.valueOf(documentBean.get("doc_type_id"))));
    List<BasicDynaBean> basicDynaBeans = new ArrayList<>();
    String[] array = params.get("selectedCategories");
    if (array != null && array.length > 0) {
      for (String s : array) {
        BasicDynaBean basicDynaBean = documentCategoryMappingRepository.getBean();
        basicDynaBean.set("doc_type_id", documentBean.get("doc_type_id"));
        basicDynaBean.set("doc_type_category_id", s);
        basicDynaBeans.add(basicDynaBean);
      }
      int[] results = documentCategoryMappingRepository.batchInsert(basicDynaBeans);
    }
    return ret;
  }

}
