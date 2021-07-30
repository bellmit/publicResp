package com.insta.hms.mdm.notetypes;

import com.insta.hms.common.PagedList;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class NoteTypesController.
 */
@Controller
@RequestMapping(URLRoute.NOTE_TYPES_PATH)
public class NoteTypesController extends MasterRestController {

  /** The note types service. */
  @Autowired
  private NoteTypesService noteTypesService;

  /**
   * Instantiates a new note types controller.
   *
   * @param noteTypesService
   *          the note types service
   */
  public NoteTypesController(NoteTypesService noteTypesService) {
    super(noteTypesService);
  }

  /**
   * Gets the note types index page.
   *
   * @return the note types index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getNoteTypesIndexPage() {
    return renderMasterUi("Master", "clinicalMaster", true);
  }

  /**
   * Gets the hospital roles.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the hospital roles
   */
  @GetMapping(value = "/gethospitalroles")
  public ResponseEntity<Map<String, Object>> getHospitalRoles(HttpServletRequest request,
      HttpServletResponse response) {
    return new ResponseEntity<>(noteTypesService.getHospitalRolesList(),
        HttpStatus.OK);
  }
  
  @Override
  @PostMapping(value = "/create", consumes = "application/json")
  protected ResponseEntity<Map<String, Object>> create(HttpServletRequest req, 
      HttpServletResponse resp, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return new ResponseEntity<>(noteTypesService.saveNoteType(requestBody), 
        HttpStatus.CREATED);
  }
  
  /**
   * List.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @Override
  @GetMapping(value = { "/list", "" })
  public ResponseEntity<Map<String, Object>> list(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
    PagedList pagedList = noteTypesService.getNoteTypesDetails(paramMap);
    responseMap.put("pagedList", pagedList);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);

  }
  
  /**
   * Gets the templatedetails.
   *
   * @param request the request
   * @param response the response
   * @return the templatedetails
   */
  @GetMapping(value = "/gettemplatedetails")
  public ResponseEntity<Map<String, Object>> gettemplatedetails(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = noteTypesService
        .getTemplateDetails(request.getParameterMap());
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
  
  @SuppressWarnings("rawtypes")
  @PostMapping(value = "/deletetemplate")
  protected ResponseEntity deletetemplate(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    noteTypesService.deleteTemplate(requestBody);
    return new ResponseEntity(HttpStatus.OK);
  }
  
  /**
   * Template auto complete.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @GetMapping(value = "/template/autocomplete")
  public ResponseEntity<Map<String, Object>> templateAutoComplete(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = noteTypesService
        .gettemplateAutoComplete(request.getParameterMap());
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
}
