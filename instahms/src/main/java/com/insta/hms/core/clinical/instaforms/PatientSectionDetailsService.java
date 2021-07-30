package com.insta.hms.core.clinical.instaforms;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientSectionDetailsService.
 *
 * @author anup vishwas
 */

@Service
public class PatientSectionDetailsService {

  /** The Patient section details repo. */
  @LazyAutowired
  private PatientSectionDetailsRepository patientSectionDetailsRepo;

  /**
   * Gets the all section details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param formId
   *          the form id
   * @param itemType
   *          the item type
   * @return the all section details
   */
  public List<BasicDynaBean> getAllSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return patientSectionDetailsRepo.getAllSectionDetails(mrNo, patientId, itemId, genericFormId,
        formId, itemType);
  }

  /**
   * Gets the section form details.
   *
   * @param consultationId
   *          the consultation id
   * @param formType
   *          the form type
   * @return the section form details
   */
  public List<BasicDynaBean> getSectionFormDetails(int consultationId, String formType) {

    return patientSectionDetailsRepo.getSectionFormDetails(consultationId, formType);
  }
}
