package com.insta.hms.mdm.codetype;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
@RequestMapping(URLRoute.CODE_TYPE_MASTER)
public class CodeTypeController extends MasterRestController {

  public CodeTypeController(CodeTypeService service) {
    super(service);
  }
  
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getCodeTypeIndexPage() {
    return renderMasterUi("Master", "hospitalBillingMasters");
  }

  /**
   * Get list of Code Type.
   *
   * @return response map
   */
  @GetMapping(value = "/codeTypeList")
  public ResponseEntity<Map<String, Object>> getCodeTypeList() {
    Map<String, Object> responseMap = ((CodeTypeService) getService()).getCodeTypeList();
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * Get code details by code type.
   *
   * @param searchInput search text
   * @param codeType code type
   * @return response map
   */
  @GetMapping(value = "/detailList")
  public ResponseEntity<Map<String, Object>> getCodeDetailsByCodeType(
      @RequestParam String searchInput, @RequestParam String codeType)
      throws Exception {
    Map<String, Object> responseMap =
        ((CodeTypeService) getService()).getCodeDetailsByCodeType(searchInput, codeType);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
}
