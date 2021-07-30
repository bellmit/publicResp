package com.insta.hms.integration.backload;

import com.insta.hms.common.PagedList;
import com.insta.hms.common.StringUtil;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
* The Class DataBackloadController.
*/
@Controller
@RequestMapping(URLRoute.DATA_BACKLOAD_PATH)
public class DataBackloadController extends MasterRestController  {

  /** The Interface Config service. */
  @Autowired
  private DataBackloadService dataBackloadService;

  /**
   * Instantiates a new Databackload controller.
   *
   * @param dataBackloadService the databackload service
   */
  public DataBackloadController(DataBackloadService dataBackloadService) {
    super(dataBackloadService);
  }

  /**
   * Gets the data backload index page.
   *
   * @return the data backload index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getBackloadIndexPage() {
    return renderMasterUi("Master", "interfaceMaster", true);
  }
  
  /**
   * Initiate backload job.
   *
   * @param req the request
   * @param resp the response
   * @return the response entity
   */
  @GetMapping(value = {"/startbackload", ""})
  public ResponseEntity<?> initiate(HttpServletRequest req, 
      HttpServletResponse resp) {
    Map<String, String[]> paramMap = new HashMap<>(req.getParameterMap());
    String startDate = (String)paramMap.get("record_start_date")[0];
    String endDate = (String)paramMap.get("record_end_date")[0];
    String interfaceId = (String)paramMap.get("interfaceId")[0];
    if (StringUtil.isNullOrEmpty(startDate) || StringUtil.isNullOrEmpty(endDate)) {
      return ResponseEntity.badRequest().body("Date Range Not Provided Correctly");
    }
    if (StringUtil.isNullOrEmpty(interfaceId)) {
      return ResponseEntity.badRequest().body("Interface not selected");
    }
    return dataBackloadService.startBackload(startDate,endDate,
      Integer.parseInt(interfaceId));
  }
  
  /**
   * List backload audit jobs by status.
   * 
   * <p> expecting status="COMPLETED" or status="INITIATED" as query params for 
   * filtering of records on status </p>
   *
   * @param req the request
   * @param resp the response
   * @return the response entity
   */
  @Override
  @GetMapping(value = {"/list", ""})
  public ResponseEntity<Map<String, Object>> list(HttpServletRequest req,
      HttpServletResponse resp) {
    Map<String, Object> responseMap = new HashMap<>();
    Map<String, String[]> paramMap = new HashMap<>(req.getParameterMap());
    PagedList pagedList = dataBackloadService.getBackloadJobDetails(paramMap);
    responseMap.put("pagedList", pagedList);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
}