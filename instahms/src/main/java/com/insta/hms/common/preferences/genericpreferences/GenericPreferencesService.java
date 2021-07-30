package com.insta.hms.common.preferences.genericpreferences;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.PreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.core.patient.header.PatientHeaderService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplate;
import com.insta.hms.master.GenericPreferences.GenericPreferencesCache;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.billprinttemplates.BillPrintTemplateService;
import com.insta.hms.mdm.commonprinttemplates.CommonPrintTemplateService;
import com.insta.hms.mdm.countries.CountryService;
import com.insta.hms.mdm.depositreceiptrefundprinttemplates.DepositReceiptRefundPrintTemplateService;
import com.insta.hms.mdm.grnprinttemplates.GrnPrintTemplatesService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.hospitalprintfiles.HospitalPrintFileService;
import com.insta.hms.mdm.hospitalprints.HospitalPrintService;
import com.insta.hms.mdm.packageuom.PackageUomService;
import com.insta.hms.mdm.poprinttemplates.PoPrintTemplateService;
import com.insta.hms.mdm.prescriptionsprinttemplates.PrescriptionsTemplateService;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.insta.hms.mdm.receiptrefundprinttemplates.ReceiptRefundPrintTemplateService;
import com.insta.hms.redis.RedisMessagePublisher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericPreferencesService. TODO: PackageUOMService, HospitalPrintService have composite
 * primary key.
 *
 * @author ritolia
 *
 */
@Service("genericPreferencesService")
public class GenericPreferencesService extends PreferencesService implements ServletContextAware {

  /** The Constant MAXIMUM_SIZE. */
  private static final Long MAXIMUM_SIZE = 10L * 1024L * 1024L;

  /** The bill print template service. */
  @LazyAutowired
  private BillPrintTemplateService billPrintTemplateService;

  /** The receipt refund print template service. */
  @LazyAutowired
  private ReceiptRefundPrintTemplateService receiptRefundPrintTemplateService;

  /** The deposit receipt refund print template service. */
  @LazyAutowired
  private DepositReceiptRefundPrintTemplateService depositReceiptRefundPrintTemplateService;

  /** The prescriptions template service. */
  @LazyAutowired
  private PrescriptionsTemplateService prescriptionsTemplateService;

  /** The po print template service. */
  @LazyAutowired
  private PoPrintTemplateService poPrintTemplateService;

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /** The hospital center service. */
  @LazyAutowired
  private HospitalCenterService hospitalCenterService;

  /** The country service. */
  @LazyAutowired
  private CountryService countryService;

  /** The common print template service. */
  @LazyAutowired
  private CommonPrintTemplateService commonPrintTemplateService;

  /** The package UOM service. */
  @LazyAutowired
  private PackageUomService packageUomService;

  /** The hospital print service. */
  @LazyAutowired
  private HospitalPrintService hospitalPrintService;

  /** The printer definition service. */
  @LazyAutowired
  private PrinterDefinitionService printerDefinitionService;

  /** The hospital print file service. */
  @LazyAutowired
  private HospitalPrintFileService hospitalPrintFileService;

  /** The patient header service. */
  @LazyAutowired
  private PatientHeaderService patientHeaderService;

  /** The grn print template service. */
  @LazyAutowired
  private GrnPrintTemplatesService grnPrintTemplateService;
  
  /** The Session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The repository. */
  private GenericPreferencesRepository repository;

  private ServletContext servletContext;

  /*
   * @LazyAutowired private PushService pushService;
   */

  /**
   * Instantiates a new generic preferences service.
   *
   * @param genericPreferencesRepository the generic preferences repository
   * @param genericPreferencesValidator the generic preferences validator
   */
  public GenericPreferencesService(GenericPreferencesRepository genericPreferencesRepository,
      GenericPreferencesValidator genericPreferencesValidator) {
    super(genericPreferencesRepository, genericPreferencesValidator);
    this.repository = genericPreferencesRepository;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.preferences.PreferencesService#getAllPreferences()
   */
  @Override
  public BasicDynaBean getAllPreferences() {
    String schema = RequestContext.getSchema();
    BasicDynaBean bean = GenericPreferencesCache.CACHEDPREFERENCESBEAN.get(schema);
    if (bean == null) {
      bean = repository.getRecord();
      GenericPreferencesCache.CACHEDPREFERENCESBEAN.put(schema, bean);
    }

    return GenericPreferencesCache.CACHEDPREFERENCESBEAN.get(schema);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.preferences.PreferencesService#update(org.apache.commons.beanutils.
   * BasicDynaBean)
   */
  @Override
  public Integer update(BasicDynaBean bean) {
    String schema = RequestContext.getSchema();
    Integer ret = repository.update(bean, null);
    if (ret != 0) {
      BasicDynaBean updatedBean = repository.getRecord(null);

      // since the prefs are changed, we need to get the new one, so clear
      // the cache.
      invalidateGenericPrefCache();
    }
    return ret;
  }

  /**
   * Gets the additional data.
   *
   * @return the additional data
   */
  protected Map<String, Object> getAdditionalData(String userLangCode) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("templateList",
        ConversionUtils.listBeanToListMap(billPrintTemplateService.lookup(false)));
    map.put("receiptRefundTemplateList",
        ConversionUtils.listBeanToListMap(receiptRefundPrintTemplateService.lookup(false)));
    map.put("depositReceiptRefundTemplateList",
        ConversionUtils.listBeanToListMap(depositReceiptRefundPrintTemplateService.lookup(false)));
    map.put("templateNames",
        ConversionUtils.listBeanToListMap(prescriptionsTemplateService.lookup(false)));
    map.put("poTemplates", ConversionUtils.listBeanToListMap(poPrintTemplateService.lookup(false)));
    map.put("bedTypes", ConversionUtils.listBeanToListMap(bedTypeService.lookUpActiveBillingBed()));
    map.put("preferredLanguages", patientHeaderService.getPreferredLanguages(userLangCode));
    BasicDynaBean countryBeanList = getCountryBeanList();
    if (countryBeanList != null) {
      map.put("countryList", getCountryBeanList().getMap());
    }
    map.put("dentalPrintTemplate", ConversionUtils.listBeanToListMap(
        commonPrintTemplateService.getTemplateNames(PrintTemplate.DentalConsultation.getType())));
    map.put("packageUOMList", ConversionUtils.listBeanToListMap(packageUomService.listAll()));
    map.put("issuePackageList",
        ConversionUtils.listBeanToListMap(packageUomService.listDistinct()));
    map.put("issueUOMList",
        ConversionUtils.listBeanToListMap(packageUomService.listDistinctIssueUom()));
    map.put("printerDefination",
        ConversionUtils.listBeanToListMap(printerDefinitionService.lookup(false)));
    BasicDynaBean defaultPrinterStylingBean = hospitalPrintService.getDefaultPrinter("print_type",
        "Bill");
    if (defaultPrinterStylingBean != null) {
      map.put("defaultPrinterStyling",
          hospitalPrintService.getDefaultPrinter("print_type", "Bill").getMap());
    }
    BasicDynaBean fileSizeBean = hospitalPrintFileService.getScreenLogoSize();
    if (fileSizeBean != null) {
      map.put("fileSize", hospitalPrintFileService.getScreenLogoSize().getMap());
    }
    map.put("grnTemplates",
        ConversionUtils.listBeanToListMap(grnPrintTemplateService.lookup(false)));
    return map;
  }

  /**
   * Gets the country bean list.
   *
   * @return the country bean list
   */
  private BasicDynaBean getCountryBeanList() {
    Map<String, Object> hospCenterParams = new HashMap<>();
    hospCenterParams.put("center_id", 0);
    BasicDynaBean hospCenterBean = hospitalCenterService.findByPk(hospCenterParams);
    String countryId = (String) hospCenterBean.get("country_id");
    Map<String, Object> countryBeanParams = new HashMap<>();
    countryBeanParams.put("country_id", countryId);
    return countryService.findByPk(countryBeanParams);
  }

  /**
   * Update screen logo.
   *
   * @param screenLogo
   *          the screen logo
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  protected void updateScreenLogo(MultipartFile screenLogo) throws IOException {
    byte[] imageBytes = screenLogo.getBytes();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String filename = sessionAttributes.get("sesHospitalId") + "Logo.png";
    File saveDest = new File("/etc/hms/hospitalLogo/" + filename);
    File serveDest = new File(servletContext.getRealPath("") + "/images/hospitalLogo/" + filename);
    try (FileOutputStream saveOutputStream = new FileOutputStream(saveDest);
        FileOutputStream serveOutputStream = new FileOutputStream(serveDest);) {
      saveOutputStream.write(imageBytes);
      serveOutputStream.write(imageBytes);
    } catch (FileNotFoundException exception) {
      throw new HMSException("exception.insufficient.privileges");
    }
    hospitalPrintFileService.updateScreenLogo(new ByteArrayInputStream(imageBytes));
  }

  /**
   * Gets the screen logo.
   *
   * @return the screen logo
   */
  protected BasicDynaBean getScreenLogo() {
    return hospitalPrintFileService.getScreenLogo();
  }

  /**
   * Delete screen logo.
   */
  protected void deleteScreenLogo() {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String filename = sessionAttributes.get("sesHospitalId") + "Logo.png";
    File savedFile = new File("/etc/hms/hospitalLogo/" + filename);
    savedFile.delete();
    File serveFile = new File(servletContext.getRealPath("") + "/images/hospitalLogo/" + filename);
    serveFile.delete();
    hospitalPrintFileService.deleteScreenLogo();
  }

  /**
   * Gets the logo size.
   *
   * @return the logo size
   */
  protected BasicDynaBean getLogoSize() {
    return hospitalPrintFileService.getScreenLogoSize();
  }

  /**
   * Update form image data.
   *
   * @param image
   *          the image
   * @param bean
   *          the bean
   * @return the integer
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Transactional(rollbackFor = Exception.class)
  protected Integer updateFormImageData(MultipartFile image, BasicDynaBean bean)
      throws IOException {
    if (image != null && image.getSize() > MAXIMUM_SIZE) {
      throw new ValidationException("exception.maximum.size.violation");
    } else if (image != null) {
      updateScreenLogo(image);
    }
    return update(bean);
  }

  /**
   * Hack for react. Redux-form not supporting Boolean value. Need to pass it as String.
   *
   * @param bean
   *          the bean
   */
  protected void showBean(Map<String, Object> bean) {
    if ((Boolean) bean.get("is_return_against_grnno") == false) {
      bean.put("is_return_against_grnno", 'f');
    } else {
      bean.put("is_return_against_grnno", 't');
    }
    if ((Boolean) bean.get("auto_mail_po_to_sup") == false) {
      bean.put("auto_mail_po_to_sup", 'f');
    } else {
      bean.put("auto_mail_po_to_sup", 't');
    }
    if ((Boolean) bean.get("apply_supplier_tax_rules") == false) {
      bean.put("apply_supplier_tax_rules", 'f');
    } else {
      bean.put("apply_supplier_tax_rules", 't');
    }
  }

  /**
   * Hack for react. Redux-form not supporting Boolean value. Need to pass it as String.
   *
   * @param params
   *          the params
   * @param mergeBean
   *          the merge bean
   */
  protected void updateBean(Map<String, String[]> params, BasicDynaBean mergeBean) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();

    String username = (String) getAllPreferences().get("username");
    if (username != null && !username.equals("")) {
      mergeBean.set("username", username);
    }

    if (params.get("is_return_against_grnno")[0] != null
        && params.get("is_return_against_grnno")[0].equals("t")) {
      mergeBean.set("is_return_against_grnno", true);
    } else {
      mergeBean.set("is_return_against_grnno", false);
    }
    if (params.get("auto_mail_po_to_sup")[0] != null
        && params.get("auto_mail_po_to_sup")[0].equals("t")) {
      mergeBean.set("auto_mail_po_to_sup", true);
    } else {
      mergeBean.set("auto_mail_po_to_sup", false);
    }
    if (params.get("apply_supplier_tax_rules")[0] != null
        && params.get("apply_supplier_tax_rules")[0].equals("t")) {
      mergeBean.set("apply_supplier_tax_rules", true);
    } else {
      mergeBean.set("apply_supplier_tax_rules", false);
    }
  }

  /**
   * Invalidate generic pref cache.
   */
  public void invalidateGenericPrefCache() {
    // this.pushService.push("/topic/messages", this.getAllPreferences().getMap());
    String schema = RequestContext.getSchema();
    clearCache(RequestContext.getSchema());
    if (EnvironmentUtil.isDistributed()) {
      ApplicationContextProvider.getApplicationContext().getBean(RedisMessagePublisher.class)
          .notifyCacheInvalidation(schema + "@generic_preferences");
    }
  }

  /**
   * Clear cache.
   *
   * @param schema
   *          the schema
   */
  public void clearCache(String schema) {
    GenericPreferencesCache.CACHEDPREFERENCESDTO.remove(schema);
    GenericPreferencesCache.CACHEDPREFERENCESBEAN.remove(schema);
  }

  /**
   * Gets the e rx header fields.
   *
   * @param pbmPrescId
   *          the pbm presc id
   * @param doctorId
   *          the doctor id
   * @param tpaId
   *          the tpa id
   * @param insuCompId
   *          the insu comp id
   * @param healthAuthority
   *          the health authority
   * @return the e rx header fields
   */
  public BasicDynaBean getERxHeaderFields(int pbmPrescId, String doctorId, String tpaId,
      String insuCompId, String healthAuthority) {
    return repository.getERxHeaderFields(pbmPrescId, doctorId, tpaId, insuCompId, healthAuthority);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * Query to get inbound SMS config values.
   */
  private static final String GET_INBOUND_SMS_CONF = "SELECT received_sms_appointment_confirm,"
      + "received_sms_appointment_cancel FROM generic_preferences";

  /**
   * Gets received sms values for confirm and cancel.
   */
  public BasicDynaBean getInboundSMSConfValues() {
    return DatabaseHelper.queryToDynaBean(GET_INBOUND_SMS_CONF);
  }
}
