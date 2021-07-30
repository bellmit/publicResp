package com.insta.hms.documents;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import com.insta.hms.forms.genericforms.GenericFormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/genericdocument")
public class GenericDocumentsController extends DocumentsController {

  public GenericDocumentsController(GenericDocumentsService service) {
    super(service);
  }

  @LazyAutowired
  private GenericDocumentsService genericDocumentsService;

  /**
   * List generic docs and forms for a patient.
   *
   * @param visitId the visit id
   * @param docCategory the doc category
   * @return the map
   */
  @GetMapping(value = "/visit/{visitId}/list")
  public ResponseEntity<Map<String, Object>> list(
      @PathVariable(value = "visitId", required = true) String visitId,
      @RequestParam(value = "doc_category", required = true) String docCategory) {
    return new ResponseEntity<>(
        genericDocumentsService.listPatientDocumentation(visitId, docCategory), HttpStatus.OK);
  }

  /**
   * @param searchQuery search query key
   * @param docTypeId doc type
   * @param docCategory doc category (e.g.: CLN)
   * @param docItemType doc item type (Doc or Form)
   * @param deptId department id
   * @return response mapom
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/templatenames") public ResponseEntity<Map<String, Object>> templateNames(
      @RequestParam(value = "search_query", required = false) String searchQuery,
      @RequestParam(value = "doc_type_id", required = false) String docTypeId,
      @RequestParam(value = "category") String docCategory,
      @RequestParam(value = "item_type") String docItemType,
      @RequestParam(value = "dept_id") String deptId) {
    return new ResponseEntity<>(genericDocumentsService
        .lookUpTemplateNames(docTypeId, docCategory, docItemType, deptId, searchQuery),
        HttpStatus.OK);
  }

  @Override
  public String getURL(String contextPath, String docId) {
    return null;
  }
}
