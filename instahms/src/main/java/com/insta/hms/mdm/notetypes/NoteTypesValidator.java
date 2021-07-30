package com.insta.hms.mdm.notetypes;

import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NoteTypesValidator extends MasterValidator {

  protected static final String TEMPLATE_ID = "template_id";
  /**
   * Validate template id.
   *
   * @param parameterMap the parameter map
   * @param errorMap the error map
   * @return true, if successful
   */
  
  public boolean validateTemplateId(Map<String, String[]> parameterMap, 
      ValidationErrorMap errorMap) {
    String[] templateId = parameterMap.get(TEMPLATE_ID);
    if (templateId == null || templateId[0] == null || templateId[0].equals("")) {
      errorMap.addError(TEMPLATE_ID, "exception.notetype.templateid");
      return false;
    } 
    try {
      Integer.parseInt(templateId[0]);
    } catch (NumberFormatException exe) {
      errorMap.addError(TEMPLATE_ID, "exception.notetype.integer.templateid");
      return false;
    }
    return true;
  }

}