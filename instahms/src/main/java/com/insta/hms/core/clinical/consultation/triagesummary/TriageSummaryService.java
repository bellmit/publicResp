package com.insta.hms.core.clinical.consultation.triagesummary;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.forms.DynamicSectionService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsService;
import com.insta.hms.core.clinical.forms.SectionFactory;
import com.insta.hms.core.clinical.forms.SystemSectionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TriageSummaryService extends SystemSectionService {

  @LazyAutowired
  private SectionFactory sectionFactory;
  @LazyAutowired
  private SectionDetailsService sectionDetailsService;
  @LazyAutowired
  private SessionService sessionService;

  public TriageSummaryService() {
    this.sectionId = -3;
  }

  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    FormParameter parameterTri = new FormParameter("Form_TRI", parameter.getItemType(),
        parameter.getMrNo(), parameter.getPatientId(), parameter.getId(),
        parameter.getFormFieldName());
    List<BasicDynaBean> sections = sectionDetailsService.getSections(parameterTri,
        (Integer) sessionService.getSessionAttributes().get("roleId"));
    Map<String, Object> response = new HashMap<>();
    List<Map<String, Object>> records = new LinkedList<>();
    List<Map> instaSectionRecords = (List<Map>) ((DynamicSectionService) sectionFactory
        .getDynamicSectionService(1)).getTriageSectionData((Integer) parameterTri.getId())
            .get("records");
    Map<String, Object> instaSectionRecordsMap = instaSectionRecords == null
        ? new HashMap<String, Object>()
        : ConversionUtils.listMapToMapListMap(instaSectionRecords, "section_id");

    Set<Integer> sectionIds = new HashSet<>();
    for (BasicDynaBean section : sections) {

      if (sectionIds.contains(section.get("section_id"))) {
        continue;
      }

      sectionIds.add((Integer) section.get("section_id"));
      Map<String, Object> temp = new HashMap<>();
      temp.put("section_id", section.get("section_id"));
      temp.put("section_title", section.get("section_title"));
      temp.put("display_order", section.get("display_order"));
      Integer sectionId = (Integer) section.get("section_id");
      if (sectionId < 0) {
        temp.putAll(sectionFactory.getSystemServices().get(sectionId.toString())
            .getSectionDetailsFromCurrentForm(parameterTri));
      } else {
        temp.put("records", instaSectionRecordsMap.get(sectionId));
      }
      if (temp.containsKey("section_detail_id")) {
        temp.remove("section_detail_id");
      }
      records.add(temp);
    }
    response.put("records", records);
    return response;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    return getSectionDetailsFromCurrentForm(parameter);
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

}
