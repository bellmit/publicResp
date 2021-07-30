package com.insta.hms.forms.genericforms;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.forms.FormController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/genericform")
public class GenericFormController extends FormController<Integer> {

  @LazyAutowired
  private GenericFormService genericFormService;

  public GenericFormController(GenericFormService genFormService) {
    super(genFormService);
  }

  @RequestMapping(value = UrlRoute.SHOW_DETAILS)
  public Map<String, Object> showDetails(Integer id, @RequestParam String patientId) {
    return super.showDetails(id);
  }

  @PostMapping(value = UrlRoute.ADD_FORM)
  public Map<String, Object> add(@RequestBody ModelMap requestBody,
      @PathVariable(value = "visitId") String visitId) {
    Integer instaFormId = (Integer) requestBody.get("insta_form_id");
    return genericFormService.add(instaFormId, visitId);
  }

  @DeleteMapping(value = UrlRoute.DISCARD_FORM)
  public ResponseEntity<Map<String, Object>> discard(
      @PathVariable(value = "id") Integer genFormId) {
    if (genericFormService.discard(genFormId)) {
      return new ResponseEntity<Map<String, Object>>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  @PutMapping(value = UrlRoute.EDIT_FORM)
  public ResponseEntity<Map<String, Object>> edit(@RequestBody ModelMap requestBody,
      @PathVariable(value = "id") Integer genFormId) {
    return new ResponseEntity<Map<String, Object>>(genericFormService.edit(genFormId, requestBody),
        HttpStatus.OK);
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/allergieslookup")
  public Map<String, Object> getAllergies(HttpServletRequest request,
      HttpServletResponse response) {
    return ((GenericFormService) service).getAllergies(request.getParameterMap());
  }
}
