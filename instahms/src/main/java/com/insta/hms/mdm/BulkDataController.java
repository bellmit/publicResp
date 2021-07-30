package com.insta.hms.mdm;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVModelAndView;

import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class BulkDataController.
 */
public class BulkDataController extends MasterController {

  /** The file name. */
  private final String fileName;

  /** The Constant RESPONSE_FORMAT_CSV. */
  protected static final String RESPONSE_FORMAT_CSV = "CSV";

  /** The Constant RESPONSE_FORMAT_STRING. */
  protected static final String RESPONSE_FORMAT_STRING = "STRING";

  /** The Constant ERROR_FILE_NAME. */
  private static final String ERROR_FILE_NAME = "importerrors";

  /**
   * Instantiates a new bulk data controller.
   *
   * @param service
   *          the service
   * @param router
   *          the router
   * @param fileName
   *          the file name
   */
  public BulkDataController(MasterService service, ResponseRouter router, String fileName) {
    super(service, router);
    this.fileName = fileName;
  }

  /**
   * Export data.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/export", method = RequestMethod.GET)
  public ModelAndView exportData(HttpServletRequest request, HttpServletResponse response) {
    Map<String, List<String[]>> csvData = ((BulkDataService) getService()).exportData();

    CsVModelAndView mav = new CsVModelAndView(this.fileName);
    mav.addHeader(csvData.get("headers").get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Import data.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/import", method = RequestMethod.POST)
  public ModelAndView importData(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    MessageUtil messageUtil = ApplicationContextProvider.getBean(MessageUtil.class);
    Map<String, MultiValueMap<Object, Object>> feedback = 
        new HashMap<String, MultiValueMap<Object, Object>>();
    String error = ((BulkDataService) getService()).importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute("error", messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      String responseFormat = getImportResponseFormat(feedback);
      // If the errors have to be sent as a CSV
      if (RESPONSE_FORMAT_CSV.equalsIgnoreCase(responseFormat)) {
        List<String[]> errors = getErrorList(feedback);
        CsVModelAndView mav = new CsVModelAndView(ERROR_FILE_NAME);
        mav.addHeader(new String[] { "Line Number", "Error" });
        ;
        mav.addData(errors);
        mav.addObject("result", feedback.get("result").toSingleValueMap());
        mav.addObject("info", getFormattedResult(feedback));
        return mav;
        // If errors have to be sent as strings for display on screen
      } else {
        ModelAndView mav = new ModelAndView();
        mav.addObject("warnings", feedback.get("warnings"));
        mav.addObject("result", feedback.get("result").toSingleValueMap());
        mav.addObject("info", getFormattedResult(feedback));
        mav.setViewName(router.route("import"));
        return mav;
      }
    }
  }

  /**
   * Export template.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/template", method = RequestMethod.GET)
  public ModelAndView exportTemplate(HttpServletRequest request, HttpServletResponse response) {
    String[] header = ((BulkDataService) getService()).getDynamicHeaderAliases().values()
        .toArray(new String[0]);
    CsVModelAndView mav = new CsVModelAndView(this.fileName);
    mav.addHeader(header);
    return mav;
  }

  /**
   * Gets the import response format.
   *
   * @param feedback
   *          the feedback
   * @return the import response format
   */
  protected String getImportResponseFormat(Map<String, MultiValueMap<Object, Object>> feedback) {
    return RESPONSE_FORMAT_STRING;
  }

  /**
   * Gets the error list.
   *
   * @param feedback
   *          the feedback
   * @return the error list
   */
  private List<String[]> getErrorList(Map<String, MultiValueMap<Object, Object>> feedback) {
    List<String[]> exportErrorList = new ArrayList<String[]>();
    MultiValueMap<Object, Object> mvm = feedback.get("warnings");
    for (Object lineNumber : mvm.keySet()) {
      List<Object> errorList = mvm.get(lineNumber);
      for (Object list : errorList) {
        exportErrorList.add(new String[] { lineNumber.toString(), list.toString() });
      }
    }
    return exportErrorList;
  }

  /**
   * Gets the formatted result.
   *
   * @param feedback
   *          the feedback
   * @return the formatted result
   * 
   *         This method format 'result' to show process count, insertion count and update count
   */
  public String getFormattedResult(Map<String, MultiValueMap<Object, Object>> feedback) {
    StringBuilder info = new StringBuilder();
    Integer processedCount = (Integer) feedback.get("result").get("processed_count").get(0);
    Integer insertionCount = (Integer) feedback.get("result").get("insertion_count").get(0);
    Integer updateCount = (Integer) feedback.get("result").get("updation_count").get(0);

    info.append("Processed lines: ").append(processedCount).append("<br>");
    if (insertionCount > 0) {
      info.append("New rows inserted: ").append(insertionCount).append("<br>");
    }
    info.append("Existing records updated: ").append(updateCount).append("<br>");
    info.append("<br>");
    info.append("<hr>");
    return info.toString();
  }
}
