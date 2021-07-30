package com.insta.hms.core.patient.communication;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientCommunicationService {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @LazyAutowired
  private PatientCommunicationRepository repository;

  @LazyAutowired
  private ContactPreferencesRepository contactPreferencesRepository;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  public Map<String, Object> getMessageDetails(Map<String, String[]> parameterMap) {
    String mrNo = (null != parameterMap && parameterMap.containsKey("mr_no"))
        ? parameterMap.get("mr_no")[0] : null;
    Map<String, Object> dataMap = new HashMap<>();
    List<BasicDynaBean> preferenceList = repository.getPatientMessagePreference(mrNo);
    for (BasicDynaBean preferenceBean : preferenceList) {
      String messageCategory = (String) preferenceBean.get("message_category_name");
      String messageGroupName = (String) preferenceBean.get("message_group_name");
      if (!dataMap.containsKey(messageCategory)) {
        Map<String, String> prefMap = new HashMap<>();
        prefMap.put("message_mode", (String) preferenceBean.get("message_mode"));
        prefMap.put("prefered_mode", (String) preferenceBean.get("prefered_mode"));
        Map<String, Object> messageTypeMap = new HashMap<>();
        messageTypeMap.put(messageGroupName, prefMap);
        dataMap.put(messageCategory, messageTypeMap);
      } else {
        Map<String, Object> messageTypeMapObj = (Map<String, Object>) dataMap.get(messageCategory);
        if (!messageTypeMapObj.containsKey(messageGroupName)) {
          Map<String, String> prefMap = new HashMap<>();
          prefMap.put("message_mode", (String) preferenceBean.get("message_mode"));
          prefMap.put("prefered_mode", (String) preferenceBean.get("prefered_mode"));
          messageTypeMapObj.put(messageGroupName, prefMap);
          dataMap.put(messageCategory, messageTypeMapObj);
        } else if (! messageGroupName.equals("Patient Prescription")) {
          Map<String, Object> x = (Map<String, Object>) messageTypeMapObj.get(messageGroupName);
          x.put("message_mode", "B");
        }
      }
    }
    Map<String, Object> response = new HashMap<>();
    response.put("message_preferences", dataMap);
    BasicDynaBean prefBean= contactPreferencesRepository.findByKey("mr_no", mrNo);
    if (prefBean!=null) {
      String PromotionalConsent =  prefBean.get("promotional_consent")!=null ?  (String) prefBean.get("promotional_consent") : "N";
      response.put("promotional_consent", PromotionalConsent);
      response.put("lang_code", prefBean.get("lang_code"));
      response.put("receive_communication", prefBean.get("receive_communication"));
    } else {
      response.put("promotional_consent", "N");
      response.put("lang_code", genericPreferencesService.getAllPreferences().get("contact_pref_lang_code"));
      response.put("receive_communication", "B");
    }
    return response;
  }

  public void updatePref(String mrNo, String communication, String prefLang) {
    contactPreferencesRepository.updateContactPreference(mrNo, communication, prefLang);
  }

  public Map<String, Object> updateCommunicationPreference(Map<String, Object> params) {
    Map<String, Object> commPrefMap = (Map) params.get("preferences");
    String mrNo = (String) params.get("mr_no");
    String promotionalConsent = (String) params.get("promotional_consent");
    String langCode = (String) params.get("lang_code");
    List<BasicDynaBean> oldPreferences = repository.getPatientCommunicationPrefs(mrNo);
    List<String> list = new ArrayList();
    for (int i = 0; i < oldPreferences.size(); i++) {
      list.add((String) oldPreferences.get(i).get("message_group_name"));
    }
    List<Object[]> updateParamsList = new ArrayList<>();
    List<Object[]> insertParamsList = new ArrayList<>();

    for (String key : commPrefMap.keySet()) {
      String communicationType = (String) commPrefMap.get(key);
      String messageGroupName = key;
      if (list.contains(messageGroupName)) {
        updateParamsList.add(new Object[] { communicationType, messageGroupName, mrNo });
      } else {
        insertParamsList.add(new Object[] { communicationType, messageGroupName, mrNo });
      }
    }
    repository.batchUpdatePrefs(updateParamsList);
    repository.batchInsertPrefs(insertParamsList);
    contactPreferencesRepository.updateLangAndConsent(mrNo, promotionalConsent,langCode);
    return params;
  }

  public void convetAndSavePatientCommPreference(String mrNo, String smsParam, String emailParam,
      String prefLang) {
    if(prefLang != null && !prefLang.equals("")) {
      String sms = smsParam != null ? smsParam : "Y";
      String email = emailParam != null ? emailParam : "Y";
      if (mrNo != null && !mrNo.equals("")) {
        String communicationType = "N";
        if (sms.equals("Y") && email.equals("Y")) {
          communicationType = "B";
        } else if (sms.equals("Y") && email.equals("N")) {
          communicationType = "S";
        } else if (sms.equals("N") && email.equals("Y")) {
          communicationType = "E";
        }
        updatePref(mrNo, communicationType, prefLang);
      }
    }
  }

}
