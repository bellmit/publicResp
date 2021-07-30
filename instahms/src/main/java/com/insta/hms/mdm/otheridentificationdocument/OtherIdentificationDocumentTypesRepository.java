package com.insta.hms.mdm.otheridentificationdocument;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class OtherIdentificationDocumentTypesRepository.
 */
@Repository
public class OtherIdentificationDocumentTypesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new other identification document types repository.
   */
  public OtherIdentificationDocumentTypesRepository() {
    super("other_identification_document_types", "other_identification_doc_id",
        "other_identification_doc_name",
        new String[] {"other_identification_doc_id", "other_identification_doc_name"});
  }
}
