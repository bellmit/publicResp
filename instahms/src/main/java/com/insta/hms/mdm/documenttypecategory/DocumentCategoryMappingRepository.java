package com.insta.hms.mdm.documenttypecategory;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Document and category mapping Repository.
 */
@Repository
public class DocumentCategoryMappingRepository extends MasterRepository<String> {

  private static final String GET_DOC_TYPE_CATEGORY_MAPPING_ID = "Select * from "
      + " doc_type_category_mapping where doc_type_id = ? ";

  private static final String DELETE_DOC_TYPE_CATEGORY_MAPPING_BY_DOC_ID = "Delete from "
      + " doc_type_category_mapping where doc_type_id = ? ";

  public DocumentCategoryMappingRepository() {
    super("doc_type_category_mapping", "doc_type_category_mapping_id");
  }

  /**
   * Get document categories mapped to doc type.
   * 
   * @param docTypeId
   *          document type id
   * @return returns list of BasicDynaBean
   */
  public List<BasicDynaBean> getDocTypesByCatMapping(String docTypeId) {
    return DatabaseHelper.queryToDynaList(GET_DOC_TYPE_CATEGORY_MAPPING_ID, docTypeId);
  }

  /**
   * delete a document type and category mapping by document type id.
   * 
   * @param docTypeId
   *          document type id
   * @return returns list of BasicDynaBean
   */
  public int deleteDocTypesMappingByDocId(String docTypeId) {
    return DatabaseHelper.delete(DELETE_DOC_TYPE_CATEGORY_MAPPING_BY_DOC_ID, docTypeId);
  }
}