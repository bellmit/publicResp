package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class SectionImageRepository.
 *
 * @author krishnat
 */
@Repository
public class SectionImageRepository extends GenericRepository {

  /**
   * Instantiates a new section image repository.
   */
  public SectionImageRepository() {
    super("patient_section_image_details");
  }

  /**
   * Delete markers.
   *
   * @param sectionDetailId the section detail id
   */
  public void deleteMarkers(int sectionDetailId) {
    String query =
        "delete from patient_section_image_details psid USING patient_section_fields psf "
            + " WHERE (psf.field_detail_id=psid.field_detail_id) AND psf.section_detail_id=? ";
    DatabaseHelper.delete(query, new Object[] {sectionDetailId});
  }
}
