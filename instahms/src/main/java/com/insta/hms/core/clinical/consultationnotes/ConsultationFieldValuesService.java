package com.insta.hms.core.clinical.consultationnotes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.security.usermanager.UserService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ConsultationFieldValuesService.
 *
 * @author sonam
 */
@Service
public class ConsultationFieldValuesService extends SystemSectionService {

  public ConsultationFieldValuesService() {
    this.sectionId = -5;
  }

  @LazyAutowired
  private ConsultationFieldValuesRepository fieldRepository;
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  @LazyAutowired
  private UserService userService;

  private static final String FIELDS = "fields";
  private static final String DOC_ID = "doc_id";
  private static final String TEMPLATE_ID = "template_id";
  private static final String VALUE_ID = "value_id";
  private static final String FIELD_ID = "field_id";
  private static final String FIELD_VALUE = "field_value";
  private static final String FIELD_NAME = "field_name";

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    Map<String, Object> responseData = new HashMap<>();
    if (requestBody.get(FIELDS) != null
        && !((List<Map<String, Object>>) requestBody.get(FIELDS)).isEmpty()) {

      BasicDynaBean fieldsBean = fieldRepository.getBean();
      Map<String, Object> row = ((List<Map<String, Object>>) requestBody.get(FIELDS)).get(0);
      List<String> conversionErrorList = new ArrayList<>();
      ConversionUtils.copyToDynaBean(row, fieldsBean, conversionErrorList);

      Integer docId = (Integer) fieldsBean.get(DOC_ID);
      Integer templateId = (Integer) requestBody.get(TEMPLATE_ID);

      if (docId == null || docId == 0) {
        BasicDynaBean docConBean =
            doctorConsultationService.getDoctorConsultDetails((int) parameter.getId());
        if (docConBean != null) {
          docId = (Integer) docConBean.get(DOC_ID);
          templateId = (Integer) docConBean.get(TEMPLATE_ID);
        }
      }

      Map<String, Object> record = new HashMap<>();
      if (fieldsBean.get(VALUE_ID) == null) {
        int valueId = fieldRepository.getNextSequence();
        fieldsBean.set(DOC_ID, docId);
        fieldsBean.set(VALUE_ID, valueId);
        fieldRepository.insert(fieldsBean);
        responseData.put(DOC_ID, docId);
        responseData.put(TEMPLATE_ID, templateId);
      } else {
        List<BasicDynaBean> updateBeans = new ArrayList<>();
        Map<String, Object> updateKeysMap = new HashMap<>();
        List<Object> updateKeys = new ArrayList<>();
        updateBeans.add(fieldsBean);
        updateKeys.add(fieldsBean.get(VALUE_ID));
        updateKeysMap.put(VALUE_ID, updateKeys);
        fieldRepository.batchUpdate(updateBeans, updateKeysMap);
      }

      record.put(VALUE_ID, fieldsBean.get(VALUE_ID));
      record.put(FIELD_ID, fieldsBean.get(FIELD_ID));
      record.put(FIELD_VALUE, fieldsBean.get(FIELD_VALUE));
      record.put(DOC_ID, fieldsBean.get(DOC_ID));
      responseData.put(FIELDS, Arrays.asList(record));
    }
    return responseData;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {

    Object[] tableStructure =
        {"section_id", "section_detail_id", "finalized", DOC_ID, TEMPLATE_ID,};
    List<Object> mapStructure = new ArrayList<>(Arrays.asList(tableStructure));
    Object[] recStr = {VALUE_ID, FIELD_ID, FIELD_VALUE, FIELD_NAME, DOC_ID};
    List<Object> recordStructure = new ArrayList<>(Arrays.asList(recStr));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(
        fieldRepository.getConsultationNoteFieldValues(parameter), mapStructure, null);

  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    data.put("records", new ArrayList<Object>());
    return data;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    BasicDynaBean consBean = doctorConsultationService.findByKey((int) parameter.getId());
    return fieldRepository.delete(DOC_ID, consBean.get(DOC_ID)) > 0;
  }

}
