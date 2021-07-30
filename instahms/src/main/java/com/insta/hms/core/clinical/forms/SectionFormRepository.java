package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class SectionFormRepository.
 *
 * @author krishnat
 */
@Repository
public class SectionFormRepository extends GenericRepository {

  /**
   * Instantiates a new section form repository.
   */
  public SectionFormRepository() {
    super("patient_section_forms");
  }

  private static final String UPDATE_FORM_TYPE_ON_CONSULTATION_TYPE_CHANGE =
      "UPDATE patient_section_forms SET form_type=? WHERE section_detail_id IN "
          + " (SELECT section_detail_id from patient_section_details WHERE section_item_id=?)"
          + " AND form_type=?";

  public boolean updateFormTypeOnConsultationTypeChange(int consultationId, String newFormType,
      String existingFormType) {
    return DatabaseHelper.update(UPDATE_FORM_TYPE_ON_CONSULTATION_TYPE_CHANGE,
        new Object[] {newFormType, consultationId, existingFormType}) > 0;
  }
}
