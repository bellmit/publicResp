package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVModelAndView;
import com.insta.hms.mdm.storeitemrates.taxsubgroup.StoreTariffItemSubgroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Item tax upload dowonload controller.
 * 
 * @author prasanna
 *
 */

@Controller
@RequestMapping(URLRoute.TAX_UPLOAD_DOWNLOAD_PATH)
public class ItemTaxUploadDownloadController extends BaseController {

  @Autowired
  private MessageUtil messageUtil;
  @Autowired
  private OperationItemSubGroupService operationItemSubGroupService;
  @Autowired
  private DiagnosticsItemSubGroupService diagnosticsItemSubGroupService;
  @Autowired
  private BedItemSubGroupService bedItemSubGroupService;
  @Autowired
  private AnesthesiaItemSubGroupService anesthesiaItemSubGroupService;
  @Autowired
  private CommonItemSubGroupService commonItemSubGroupService;
  @Autowired
  private DietaryItemSubGroupService dietaryItemSubGroupService;
  @Autowired
  private DrgCodeItemSubGroupService drgCodeItemSubGroupService;
  @Autowired
  private EquipmentItemSubGroupService equipmentItemSubGroupService;
  @Autowired
  private PackageItemSubGroupService packageItemSubGroupService;
  @Autowired
  private PerdiemCodeItemSubGroupService perdiemCodeItemSubGroupService;
  @Autowired
  private ServiceItemSubGroupService serviceItemSubGroupService;
  @Autowired
  private OpTheatreItemSubgroupService opTheatreItemSubgroupService;
  @Autowired
  private ConsultationItemSubGroupsService consultationItemSubGroupsService;
  @Autowired
  private StoreItemSubGroupService storeItemSubGroupService;
  @Autowired
  private StoreTariffItemSubgroupService storeTariffItemSubgroupService;

  /**
   * Upload download.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @RequestMapping(value = { "/UploadDownload", "" }, method = RequestMethod.GET)
  public ModelAndView uploadDownload(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView modelView = new ModelAndView();
    modelView.setViewName(URLRoute.TAX_UPLOAD_DOWNLOAD_PAGE);
    return modelView;
  }

  /**
   * Export.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the model and view
   */
  @RequestMapping(value = "/export", method = RequestMethod.GET)
  public ModelAndView export(HttpServletRequest req, HttpServletResponse res) {

    Map<String, BulkDataService> taxExport = new HashMap<String, BulkDataService>();
    taxExport.put("consultation_types", consultationItemSubGroupsService);
    taxExport.put("operation_master", operationItemSubGroupService);
    taxExport.put("diagnostics", diagnosticsItemSubGroupService);
    taxExport.put("bed_types", bedItemSubGroupService);
    taxExport.put("anesthesia_type_master", anesthesiaItemSubGroupService);
    taxExport.put("common_charges_master", commonItemSubGroupService);
    taxExport.put("diet_master", dietaryItemSubGroupService);
    taxExport.put("drg_codes_master", drgCodeItemSubGroupService);
    taxExport.put("equipment_master", equipmentItemSubGroupService);
    taxExport.put("packages", packageItemSubGroupService);
    taxExport.put("per_diem_codes_master", perdiemCodeItemSubGroupService);
    taxExport.put("services", serviceItemSubGroupService);
    taxExport.put("theatre_master", opTheatreItemSubgroupService);
    taxExport.put("store_item_details", storeItemSubGroupService);
    taxExport.put("store_item_tariff_details", storeTariffItemSubgroupService);

    Map<String, String[]> parameters = req.getParameterMap();
    String exportItemName = parameters.get("exportItem")[0];
    Map<String, List<String[]>> csvData  = null;
    if (exportItemName.equals("store_item_tariff_details")) {
      String tariffId = parameters.get("store_rate_plan_id")[0];
      csvData = taxExport.get(exportItemName)
          .exportData(new Object[] { Integer.valueOf(tariffId) });
    } else {
      csvData = taxExport.get(exportItemName).exportData();
    }
    // TODO - Method for dynamically setting file name
    CsVModelAndView mav = new CsVModelAndView(parameters.get("exportItem")[0] + " Tax Group");
    mav.addHeader(csvData.get("headers").get(0));
    mav.addData(csvData.get("rows"));
    return mav;

  }

  /**
   * Import master.
   *
   * @param req the req
   * @param mmap the mmap
   * @param res the res
   * @param redirect the redirect
   * @param file the file
   * @return the model and view
   */
  @RequestMapping(value = "/import", method = RequestMethod.POST)
  public ModelAndView importMaster(HttpServletRequest req, ModelMap mmap, HttpServletResponse res,
      RedirectAttributes redirect, @RequestPart("uploadFile") MultipartFile file) {

    Map<String, String[]> parameters = req.getParameterMap();
    Map<String, MultiValueMap<Object, Object>> feedback = 
        new HashMap<String, MultiValueMap<Object, Object>>();

    String importItemName = parameters.get("importItem")[0];
    Map<String, BulkDataService> taxImport = new HashMap<String, BulkDataService>();
    taxImport.put("consultation_types", consultationItemSubGroupsService);
    taxImport.put("operation_master", operationItemSubGroupService);
    taxImport.put("diagnostics", diagnosticsItemSubGroupService);
    taxImport.put("bed_types", bedItemSubGroupService);
    taxImport.put("anesthesia_type_master", anesthesiaItemSubGroupService);
    taxImport.put("common_charges_master", commonItemSubGroupService);
    taxImport.put("diet_master", dietaryItemSubGroupService);
    taxImport.put("drg_codes_master", drgCodeItemSubGroupService);
    taxImport.put("equipment_master", equipmentItemSubGroupService);
    taxImport.put("packages", packageItemSubGroupService);
    taxImport.put("per_diem_codes_master", perdiemCodeItemSubGroupService);
    taxImport.put("services", serviceItemSubGroupService);
    taxImport.put("theatre_master", opTheatreItemSubgroupService);
    taxImport.put("store_item_details", storeItemSubGroupService);
    taxImport.put("store_item_tariff_details", storeTariffItemSubgroupService);

    String error = taxImport.get(importItemName).importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute("error", messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("");
      redirect.addFlashAttribute("info", feedback.get("result").toSingleValueMap());
      redirect.addFlashAttribute("error", feedback.get("warnings").toSingleValueMap());
    }
    ModelAndView mav = new ModelAndView();

    mav.setViewName(URLRoute.TAX_UPLOAD_DOWNLOAD_REDIRECT);

    return mav;
  }

}
