package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.URLRoute;
import com.insta.hms.mdm.bulk.CsVModelAndView;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.OHSRS_DOHGOVPH_URL)
public class OhsrsdohgovphController extends BaseRestController {

  @LazyAutowired
  private OhsrsdohgovphService service;
    
  /**
   * Get UI for OHSRS Report.
   * @return Model and View for UI.
   */
  @IgnoreConfidentialFilters
  @GetMapping({"/index", ""})
  public ModelAndView getOhsrsIndexPage() {
    ModelAndView mav = new ModelAndView("/pages/Reports/regulatory/ph_ohsrs");
    service.setViewMeta(mav);
    return mav;
  }
    
  @IgnoreConfidentialFilters
  @GetMapping("/report/{year}")
  public Map<String, Object> getReport(@PathVariable int year) {
    return service.getReport(year);
  }
  
  @IgnoreConfidentialFilters
  @PostMapping(value = "/report/{year}/upload/{ohsrsFunction}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> uploadCsvForOhsrsFunction(@PathVariable int year, 
      @PathVariable String ohsrsFunction, @RequestParam("csv_file") MultipartFile csvFile) {
    return service.storeCsv(year, ohsrsFunction, csvFile); 
  }
  
  @IgnoreConfidentialFilters
  @GetMapping(value = "/report/{year}/download/{ohsrsFunction}")
  public CsVModelAndView downloadCsvForOhsrsFunction(@PathVariable int year, 
      @PathVariable String ohsrsFunction) {
    return service.downloadCsv(year, ohsrsFunction); 
  }

  @IgnoreConfidentialFilters
  @GetMapping("/report/{year}/generate")
  public Map<String, Object> generateReport(@PathVariable int year) {
    return service.queueReportGeneration(year);
  }
  
  @IgnoreConfidentialFilters
  @GetMapping("/report/{year}/send")
  public Map<String, Object> sendReport(@PathVariable int year) {
    return service.queueReportSubmission(year);
  }

  @IgnoreConfidentialFilters
  @PostMapping("/report/{year}/signoff")
  public Map<String, Object> signoffReport(@PathVariable int year, 
      @RequestBody Map<String, Object> mapParams) {
    return service.signoffReport(year, mapParams);
  }
}
