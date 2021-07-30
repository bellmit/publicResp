package com.insta.hms.core.clinical.consultationnotes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class ConsultationFieldValuesRepository.
 *
 * @author sonam
 */
@Repository
public class ConsultationFieldValuesRepository extends GenericRepository {
  public ConsultationFieldValuesRepository() {
    super("patient_consultation_field_values");
  }

  private static final String CONSULT_FIELD_VALUES = "SELECT dc.doc_id, dc.template_id, "
      + "pcfv.value_id, pcfv.field_id, pcfv.field_value, 'Conultation Note' as field_name, "
      + "psd.section_detail_id, psd.section_id, psd.finalized  " + "From doctor_consultation dc "
      + "JOIN patient_consultation_field_values pcfv ON (dc.doc_id=pcfv.doc_id) "
      + "JOIN patient_section_details psd ON(psd.section_item_id = dc.consultation_id) "
      + "JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + "WHERE psd.mr_no= ? AND psd.patient_id= ? AND #filter#=? "
      + "AND psd.item_type= ? AND psf.form_type= ? AND section_id= -5 AND pcfv.field_id=-1 ";

  /**
   * Gets the consultation note field values.
   *
   * @param parameter the parameter
   * @return the consultation note field values
   */
  public List<BasicDynaBean> getConsultationNoteFieldValues(FormParameter parameter) {

    return DatabaseHelper.queryToDynaList(
        CONSULT_FIELD_VALUES.replace("#filter#", parameter.getFormFieldName()),
        new Object[] {parameter.getMrNo(), parameter.getPatientId(), parameter.getId(),
            parameter.getItemType(), parameter.getFormType()});
  }

}
