package com.insta.hms.core.clinical.order.packageitems;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.otheritmes.OtherOrderItemRepository;
import com.insta.hms.core.clinical.order.otheritmes.OtherOrderItemServices;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemRepository;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemService;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemRepository;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.clinical.prescriptions.PatientPrescriptionsRepository;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.anesthesiatypecharges.AnesthesiaTypeChargesService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.ordersets.PackageChargesService;
import com.insta.hms.mdm.packageitemcharges.PackageItemChargesService;
import com.insta.hms.mdm.packages.PackagesRepository;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.packages.PatientCustomisedPackageDetailsRepository;
import com.insta.hms.mdm.packages.PatientPackageContentChargesRepository;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedRepository;
import com.insta.hms.mdm.packages.PatientPackageContentsRepository;
import com.insta.hms.mdm.packages.PatientPackagesRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class contains all package order related functions. Creation of charge bean, posting
 * components of packages, creating package order bean, Setting header properties to be inserted
 * along with package. Getting master charges for packages. Insert patient packages details, getting
 * next sequence.
 * 
 * @author ritolia
 *
 */
@Service
@Order(key = "Package", value = { "Package", "MultiVisitPackage",
    "DiagPackage" }, prefix = "packages")
public class PackageOrderItemService extends OrderItemService {

  private static Logger log = LoggerFactory.getLogger(PackageOrderItemService.class);

  private static final String PATIENT_ID = "patient_id";
  private static final String PRIOR_AUTH_ID = "_prior_auth_id";
  private static final String MR_NO = "mr_no";
  private static final String VISIT_ID = "visit_id";
  private static final String USER_NAME = "user_name";
  private static final String PACKAGE_ID = "package_id";
  private static final String PRESCRIPTION_ID = "prescription_id";
  private static final String DOCTOR_ID = "doctor_id";
  private static final String PRESCRIBED_DATE = "presc_date";
  private static final String PATIENT_PACKAGE_ID = "pat_package_id";
  private static final String ITEM_CODE = "item_code";
  private static final String ITEM_TYPE = "item_type";
  private static final String CODE_TYPE = "code_type";
  private static final String REMARKS = "remarks";
  private static final String COMMON_ORDER_ID = "common_order_id";
  private static final String PACKAGE_REFERENCE = "package_ref";
  private static final String ACTIVITY_ID = "activity_id";
  private static final String ACTIVITY_CODE = "PKG";
  private static final String IS_CUSTOMIZED_PACKAGE = "is_customized_package";


  private String prefix;
  private PackageOrderItemRepository packageOrderItemRepository;

  @LazyAutowired
  private PackagesService packagesService;

  @LazyAutowired
  private PackagesRepository packagesRepository;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private TestOrderItemService testOrderItemService;

  @LazyAutowired
  private TestOrderItemRepository testOrderItemRepository;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private ServiceOrderItemService serviceOrderItemService;

  @LazyAutowired
  private ServiceOrderItemRepository serviceOrderItemRepository;

  @LazyAutowired
  private OtherOrderItemServices otherOrderItemServices;

  @LazyAutowired
  private OtherOrderItemRepository otherOrderItemRepository;

  @LazyAutowired
  private PatientPackagesRepository patientPackagesRepository;

  @LazyAutowired
  private PatientPackageContentsRepository patientPackageContentsRepository;

  @LazyAutowired
  private PatientPackageContentChargesRepository patientPackageContentChargesRepository;

  @LazyAutowired
  private PatientPackageContentConsumedRepository patientPackageContentConsumedRepository;

  @LazyAutowired
  private PatientCustomisedPackageDetailsRepository patientCustomisedPackageDetailsRepository;

  @LazyAutowired
  private DoctorOrderItemService doctorOrderItemService;

  @LazyAutowired
  private DoctorConsultationRepository doctorOrderItemRepository;

  @LazyAutowired
  private PackageItemChargesService packageItemChargesService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private OrderService orderService;

  @LazyAutowired
  private OperationOrderItemService operationOrderItemService;

  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private PatientPrescriptionsRepository patientPrescriptionsRepository;

  @LazyAutowired
  private IpPreferencesService ipPreferencesService;

  @LazyAutowired
  private AnesthesiaTypeChargesService anesthesiaTypeChargesService;

  @LazyAutowired
  private DoctorService doctorService;

  /** The package charges service. */
  @LazyAutowired
  private PackageChargesService pkgChargesService;


  /**
   * constructor.
   *
   * @param repository the repository
   */
  public PackageOrderItemService(PackageOrderItemRepository repository) {
    super(repository, "packages", PACKAGE_ID);
    this.prefix = "packages";
    this.packageOrderItemRepository = repository;
  }

  /**
   * It returns the  multiVisit Package Details.
   *
   * @param mrNo      the mrNo
   * @return list of multiVisit Package Details
  */
  public List<BasicDynaBean> getMultiVisitPackageBeans(String mrNo) {
    List<BasicDynaBean>  multiVisitPackageBeans = patientPackagesRepository
        .getMultiVisitPackageIds(mrNo);
    /* Get the MVP Details of patients which have multi visit package 
     * partially consumed in 12.3 and upgraded to 12.4
     * Fall back to old method ,since migration has not done into 
     * new package transaction tables for mvp.
    */
    if (CollectionUtils.isEmpty(multiVisitPackageBeans)) {
      multiVisitPackageBeans = patientPackagesRepository.getMultiVisitPackageId(mrNo);
    }
    return multiVisitPackageBeans;
  }

  /**
   * Set the data required to be inserted along with each order. Normalizing the fields name based
   * on parameters getting from front-end. Trying to keep field name same across all itemTypes.
   * 
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, PATIENT_ID, patientId);
    setBeanValue(orderBean, PRESCRIPTION_ID, getNextSequence());
    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE, ConvertUtils
        .convert(orderedItemList.get("packages_prescribed_date"), dynaProperties.getType()));
    setBeanValue(orderBean, DOCTOR_ID, orderedItemList.get("packages_prescribed_doctor_id"));
    Integer packageId = 0;
    if (null != orderedItemList.get("packages_item_id")) {
      if (orderedItemList.get("packages_item_id") instanceof String) {
        packageId = Integer.parseInt(orderedItemList.get("packages_item_id").toString());
      } else {
        packageId = (Integer) orderedItemList.get("packages_item_id");
      }
    } 
    setBeanValue(orderBean, PACKAGE_ID, packageId);
    setBeanValue(orderBean, PATIENT_PACKAGE_ID, DatabaseHelper.getNextSequence("patient_package"));
  }

  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) throws ParseException {

    super.setItemBeanProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList, operationBean);

    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, PATIENT_ID, patientId);
    setBeanValue(orderBean, PRESCRIPTION_ID, getNextSequence());

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE,
        ConvertUtils.convert(orderedItemList.get("prescribed_date"), dynaProperties.getType()));

    setBeanValue(orderBean, DOCTOR_ID, orderedItemList.get("prescribed_doctor_id"));
    setBeanValue(orderBean, PACKAGE_ID, Integer.parseInt((String) orderedItemList.get("item_id")));
    setBeanValue(orderBean, PATIENT_PACKAGE_ID, DatabaseHelper.getNextSequence("patient_package"));
    setBeanValue(orderBean, "doc_presc_id",
        !"".equals(orderedItemList.get("doc_presc_id")) ? orderedItemList.get("doc_presc_id")
            : null);
    return orderBean;
  }

  /**
   * Getting master charges, for calculation of amounts, discount etc.
   * 
   */
  @Override
  public BasicDynaBean getMasterChargesBean(Object packageId, String bedType, String ratePlanId,
      Integer centerId) {
    
    if (packageId instanceof String) {
      packageId = Integer.parseInt((String)packageId);
    }
    
    return packagesService.getPackageDetails((Integer) packageId, ratePlanId, bedType);
  }

  /**
   * Getting master charges, for calculation of amounts, discount etc.
   */
  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object packageId, String ratePlanId) {
    if (packageId instanceof String) {
      packageId = Integer.parseInt((String) packageId);
    }

    return packagesService.getAllBedTypePackageDetails((Integer) packageId, ratePlanId);
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    Integer packageId = (Integer) paramMap.get("package_id");
    Integer packObId = (Integer) paramMap.get("pack_ob_id");
    String id = (String) paramMap.get("id");
    BigDecimal quantity = (BigDecimal) paramMap.get("quantity");
    Map<String, Object> otherCharges = new HashMap<String,Object>();
    otherCharges.put("bed_type", bedType);
    otherCharges.put("org_id", ratePlanId);
    otherCharges.put("charge_id", paramMap.get("charge_id"));
    if (paramMap.get("package_contents") != null) {
      otherCharges.put("package_contents", paramMap.get("package_contents"));
    }
    otherCharges.put("act_rate_plan_item_code", paramMap.get("act_rate_plan_item_code"));
    otherCharges.put("code_type", paramMap.get("code_type"));
    if (paramMap.get("submission_batch_type") != null) {
      otherCharges.put("submission_batch_type", paramMap.get("submission_batch_type"));
    }
    if (paramMap.get("is_customized") != null) {
      otherCharges.put("is_customized", paramMap.get("is_customized"));
    }
    otherCharges.put("visit_type", (String) paramMap.get("visit_type"));
    Boolean isMultiVisitPackage = (Boolean) paramMap.get("multi_visit_package");
    if (Boolean.TRUE.equals(isMultiVisitPackage)) {
      Map<String, Object> chargeMap =
          (Map<String, Object>) paramMap.get("mvp_package_content");
      chargeMap.put("submission_batch_type", paramMap.get("submission_batch_type"));
      return getMultiVisitPackItemCharges(packageId, packObId, bedType, ratePlanId, id, quantity,
          isInsurance, chargeMap);
    } else {
      BasicDynaBean masterCharge = getMasterChargesBean(Integer.valueOf(id), bedType, ratePlanId,
          null);
      return getChargesList(masterCharge, quantity, isInsurance, null, otherCharges);
    }
  }

  /**
   * Gets the multi visit pack item charges.
   *
   * @param packageId the package id
   * @param packObId the pack ob id
   * @param bedType the bed type
   * @param orgId the org id
   * @param id the id
   * @param quantity the quantity
   * @param isInsurance the is insurance
   * @param paramMap the param map
   * @return the multi visit pack item charges
   */
  private List<BasicDynaBean> getMultiVisitPackItemCharges(Integer packageId, Integer packObId,
      String bedType, String orgId, String id, BigDecimal quantity,
      boolean isInsurance, Map<String, Object> paramMap) {
    String chargeGroup = null;
    String chargeHead = (String) paramMap.get("charge_head");

    if (chargeHead != null) {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("chargehead_id", chargeHead);
      BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(keys);
      if (chargeHeadBean != null) {
        chargeGroup = (String) chargeHeadBean.get("chargegroup_id");
      }
    }
    
    BigDecimal charge = BigDecimal.ZERO;
    /* MVP which is partially consumed in 12.3 and upgraded to 12.4
    * migration has not done into new package transaction tables for mvp.
    */
    boolean isOldMvp = paramMap.get("is_old_mvp") != null 
        ? (boolean) paramMap.get("is_old_mvp") : false;
    if (isOldMvp) {
      Map<String, Object> filterMap = new HashMap<String, Object>();
      filterMap.put("package_content_id", packObId);
      filterMap.put("org_id", orgId);
      filterMap.put("bed_type", bedType);
      BasicDynaBean packContentCharge = packageItemChargesService.findBykey(filterMap);
      charge = new BigDecimal(String.valueOf(packContentCharge.get("charge")));
    } else if (paramMap.get("activity_charge") != null) {
      charge = new BigDecimal(String.valueOf(paramMap.get("activity_charge")));
    }

    Integer totalItemQty = 0; 
    if (paramMap.get("activity_qty") != null
        && StringUtils.isNumeric(paramMap.get("activity_qty").toString())) {
      totalItemQty = (Integer) paramMap.get("activity_qty");
    }
    if (charge.compareTo(BigDecimal.ZERO) != 0) {
      charge = charge.divide(new BigDecimal(totalItemQty), 4, RoundingMode.HALF_UP);
    }
    BigDecimal discount = BigDecimal.ZERO;
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal discPer = BigDecimal.ZERO;
    if (isOldMvp) {
      BasicDynaBean packageCharges = pkgChargesService.getPackageCharges(packageId,
            orgId, bedType);
      discount = new BigDecimal(String.valueOf(packageCharges.get("discount")));
      amount = new BigDecimal(String.valueOf(packageCharges.get("charge")));
    }
    if (paramMap.get("package_discount") != null && !isOldMvp) {
      discount = new BigDecimal(String.valueOf(paramMap.get("package_discount")));
    }
    if (paramMap.get("package_charge") != null && !isOldMvp) {
      amount = new BigDecimal(String.valueOf(paramMap.get("package_charge")));
    }
    if (amount.compareTo(BigDecimal.ZERO) != 0) {
      discPer = discount.divide(amount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }
    Integer serviceSubGroupId = 0;
    if (paramMap.get("service_sub_group_id") != null) {
      serviceSubGroupId = Integer.parseInt(paramMap.get("service_sub_group_id").toString());
    }
    Integer insuranceCategoryId = 0;
    if (paramMap.get("insurance_category_id") != null) {
      insuranceCategoryId = Integer.parseInt(paramMap.get("insurance_category_id").toString());
    }
    String activityId = (String) paramMap.get("activity_id");
    if ("ANATOPE".equals(chargeHead) && paramMap.get("anaesthesiaDetails") != null) {
      List<Map<String, Object>> anaesthesiaDetails = (List<Map<String, Object>>)
          paramMap.get("anaesthesiaDetails");
      for (Map<String, Object> anaesthesiaDetail : anaesthesiaDetails) {
        activityId = (String) anaesthesiaDetail.get("anaesthesia_type");
      }
    }
    BasicDynaBean chargeBean = null;
    BigDecimal itemDiscount = charge.multiply(discPer).multiply(quantity)
            .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
    chargeBean = billChargeService.setBillChargeBean(chargeGroup,
        (String) paramMap.get("charge_head"), charge, quantity, itemDiscount,
        activityId,
        (String) paramMap.get("activity_description"), null, serviceSubGroupId,
        insuranceCategoryId, isInsurance);
    if (null != paramMap.get("consultation_type_id")) {
      chargeBean.set("consultation_type_id",
          paramMap.get("consultation_type_id"));
    }
    if (null != paramMap.get("billing_group_id")) {
      chargeBean.set("billing_group_id", paramMap.get("billing_group_id"));
    }

    if (paramMap.get("submission_batch_type") != null) {
      chargeBean.set("submission_batch_type", paramMap.get("submission_batch_type"));
    } else {
      String submissionBatchType = (String)packagesService.findByKey("package_id", packageId)
          .get("submission_batch_type");
      chargeBean.set("submission_batch_type", submissionBatchType);
    }
    chargeBean.set("package_id", packageId);
    List<BasicDynaBean> chargesList = new ArrayList<>();
    chargesList.add(chargeBean);
    return chargesList;
  }

  /**
   * Creating Bill charge Bean List to be posted to Bill charge Table. Returning a List because it
   * can have taxes as in case of service item Type.
   */
  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {

    List<BasicDynaBean> packagesChargesList = new ArrayList<BasicDynaBean>();
    if (itemType == null) {
      return packagesChargesList;
    }
    String visitType = (String) otherParams.get("visit_type");
    int insuranceCategoryId;
    if (itemType.get("insurance_category_id") == null) {
      insuranceCategoryId = 0;
    } else {
      insuranceCategoryId = (Integer) itemType.get("insurance_category_id");
    }
    String chargeGroup = "PKG";
    Integer billingGroupId = null;
    if (itemType != null && itemType.get("billing_group_id") != null) {
      billingGroupId = (Integer) itemType.get("billing_group_id");
    }
    Integer packageId = (Integer) itemType.get(PACKAGE_ID);
    BigDecimal totalCharge = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    String mainChargeId = (String) otherParams.get("charge_id");
    BigDecimal chargeQty = BigDecimal.ONE;
    BigDecimal chargeValue = BigDecimal.ZERO;
    BigDecimal chargeDiscount = BigDecimal.ZERO;
    Boolean isCustomizedPackage = (Boolean) otherParams.get("is_customized");
    String bedType = (otherParams != null && otherParams.get("bed_type") != null)
            ? (String) otherParams.get("bed_type") : "GENERAL";
    String orgId = (otherParams != null && otherParams.get("org_id") != null)
            ? (String) otherParams.get("org_id") : "ORG0001";

    String subBatchType = (String)otherParams.get("submission_batch_type");

    if (otherParams.get("package_contents") != null && isCustomizedPackage) {
      Integer chargeInc = 0;
      List<Map<String, Object>> packageContents =
          (List<Map<String, Object>>) otherParams.get("package_contents");
      for (Map<String, Object> packageContent : packageContents) {
        BigDecimal amt = new
             BigDecimal(String.valueOf(packageContent.get("activity_charge")));

        int actQty = (int) (packageContent.get("act_quantity") != null
            ? packageContent.get("act_quantity") : packageContent.get("activity_qty"));
        BigDecimal totalItemQty = new BigDecimal(actQty);
        BigDecimal charge = (amt).divide(totalItemQty, 2);
        totalCharge = totalCharge.add(charge);
        BigDecimal packageCharge = new
            BigDecimal(String.valueOf(packageContent.get("package_charge")));
        //discount will be package discount
        BigDecimal packageDiscount = new BigDecimal(
            String.valueOf(packageContent.get("package_discount")));
        BigDecimal discount = discountSplit(charge, packageCharge, packageDiscount);
        //Adding discount and charge for inventory
        if ("INVITE".equals(packageContent.get("charge_head"))) {
          chargeValue = chargeValue.add(charge.multiply(totalItemQty));
          chargeDiscount = chargeDiscount.add(discount.multiply(totalItemQty));
          if (otherParams.get("exclude_invite") != null
              && (Boolean) otherParams.get("exclude_invite")) {
            continue;
          }
        }
        totalDiscount = totalDiscount.add(discount);
        String packageContentId = packageContent.get("pack_ob_id") != null
            ? String.valueOf(packageContent.get("pack_ob_id"))
            : null;
            
        Integer serviceSubGroupId = (Integer)(!isCustomizedPackage
            ? packageContent.get("service_sub_group_id")
            : packagesService.findByKey("package_id", packageId).get("service_sub_group_id"));
        String actDepartmentId = (String) packageContent.get("act_department_id"); 
        if ("SUOPE".equals(packageContent.get("charge_head")) 
            && packageContent.get("primarySurgeon") != null) {
          Map<String, Object> primarySurgeonDetails = (Map<String, Object>) packageContent
              .get("primarySurgeon");
          String surgeonId = (String) primarySurgeonDetails.get("surgeon_id");
          Map<String, Object> doctorDetails = doctorService.getDoctorDetails(surgeonId);
          actDepartmentId = (String) doctorDetails.get("dept_id");
        } else if ("ANAOPE".equals(packageContent.get("charge_head"))
              && packageContent.get("primaryAnaesthetist") != null) {
          Map<String, Object> primaryAnaesthetistDetails = (Map<String, Object>) packageContent
              .get("primaryAnaesthetist");
          String anaesthetistId = (String) primaryAnaesthetistDetails.get("anaesthetist_id");
          Map<String, Object> doctorDetails = doctorService.getDoctorDetails(anaesthetistId);
          actDepartmentId = (String) doctorDetails.get("dept_id");
        }
        
        BasicDynaBean chargeBean = billChargeService.setBillChargeBean(chargeGroup,
            (String) packageContent.get("charge_head"), charge, totalItemQty,
            discount.multiply(totalItemQty), packageContentId,
            (String) packageContent.get("activity_description"), 
            actDepartmentId, 
            serviceSubGroupId, insuranceCategoryId, isInsurance);
        String packContChargeId = "_" + mainChargeId + "_" + String.valueOf(chargeInc);
        if (otherParams.get("insert_charges") != null
            && (Boolean) otherParams.get("insert_charges")) {
          packContChargeId = billChargeService.getNextPrefixedId();
        }
        chargeBean.set("charge_id", packContChargeId);
        if (null != packageContent.get("consultation_type_id")) {
          chargeBean.set("consultation_type_id",
              packageContent.get("consultation_type_id"));
        } else {
          chargeBean.set("consultation_type_id",0);
        }
        if (billingGroupId != null) {
          chargeBean.set("billing_group_id", billingGroupId);
        }
        if (null != packageContent.get("operation_id")) {
          chargeBean.set("op_id", (String) packageContent.get("operation_id"));
        } else {
          chargeBean.set("op_id", null);
        }
        if (null != packageContent.get("panel_id")) {
          chargeBean.set("panel_id", packageContent.get("panel_id"));
        } else {
          chargeBean.set("panel_id", null);
        }

        if (subBatchType != null && !subBatchType.isEmpty()) {
          chargeBean.set("submission_batch_type", subBatchType);
        } else {
          String submissionBatchType = (String)packagesService.findByKey("package_id", packageId)
              .get("submission_batch_type");
          chargeBean.set("submission_batch_type", submissionBatchType);
        }
        if ("TCOPE".equals(packageContent.get("charge_head"))) {
          BasicDynaBean prefs = ipPreferencesService.getPreferences();
          chargeBean.set("code_type", prefs.get("theatre_charge_code_type"));
          chargeBean.set("act_rate_plan_item_code", prefs.get("theatre_daily_charge_code"));
        } else if ("ANATOPE".equals(packageContent.get("charge_head")) 
            && packageContent.get("anaesthesiaDetails") != null) {
          List<Map<String, Object>> anaesthesiaDetails = (List<Map<String, Object>>) 
              packageContent.get("anaesthesiaDetails");
          for (Map<String, Object> anaesthesiaDetail : anaesthesiaDetails) {
            String anaesthesiaTypeId = (String) anaesthesiaDetail.get("anaesthesia_type");
            BasicDynaBean anaesthesiaTypeBean = anesthesiaTypeChargesService
                .getAnesthesiaTypeCharge(anaesthesiaTypeId, bedType, orgId);
            chargeBean.set("code_type", anaesthesiaTypeBean.get("code_type"));
            chargeBean.set("act_rate_plan_item_code", anaesthesiaTypeBean.get("item_code"));
          }
        } else {
          chargeBean.set("code_type", packageContent.get("code_type"));
          chargeBean.set("act_rate_plan_item_code", packageContent.get("ct_code"));
        }
        if (null != packageContent.get("bed_id")) {
          chargeBean.set("act_description_id",
              String.valueOf(packageContent.get("bed_id")));
        }
        chargeBean.set("allow_rate_increase", (Boolean) itemType.get("allow_rate_increase"));
        chargeBean.set("allow_rate_decrease", (Boolean) itemType.get("allow_rate_decrease"));
        chargeBean.set("allow_discount", (Boolean) itemType.get("allow_discount"));
        String allowZeroClaimfor = (String) otherParams.get("allow_zero_claim_amount");
        if ("I".equals(otherParams.get("submission_batch_type"))
            && (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor))) {
          chargeBean.set("allow_zero_claim", true);
        }

        if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
          if (otherParams.get("item_excluded_from_doctor").equals("Y")
              || otherParams.get("item_excluded_from_doctor").equals(true)) {
            chargeBean.set("item_excluded_from_doctor", true);
            chargeBean.set("item_excluded_from_doctor_remarks",
                otherParams.get("item_excluded_from_doctor_remarks"));
          } else if (otherParams.get("item_excluded_from_doctor").equals("N")
              || otherParams.get("item_excluded_from_doctor").equals(false)) {
            chargeBean.set("item_excluded_from_doctor", false);
            chargeBean.set("item_excluded_from_doctor_remarks",
                otherParams.get("item_excluded_from_doctor_remarks"));
          }
        }
        chargeBean.set("package_id", packageId);
        packagesChargesList.add(chargeBean);
        chargeInc++;
      }
    }
    List<String> anesDetails = new ArrayList<>();
    List<String> priSurgeon = new ArrayList<>();
    List<String> priAnaesthetist = new ArrayList<>();
    if (otherParams.get("package_contents") != null && !isCustomizedPackage) {
      List<Map<String, Object>> packContents =
                 (List<Map<String, Object>>) otherParams.get("package_contents");
      for (Map<String, Object> packContent : packContents) {
        if ("ANATOPE".equals(packContent.get("charge_head")) 
            && packContent.get("anaesthesiaDetails") != null) {
          List<Map<String, Object>> anaesthesiaDetails = (List<Map<String, Object>>) 
              packContent.get("anaesthesiaDetails");
          for (Map<String, Object> anaesthesiaDetail : anaesthesiaDetails) {
            anesDetails.add((String) anaesthesiaDetail.get("anaesthesia_type"));
          }
        } else if ("SUOPE".equals(packContent.get("charge_head")) 
                && packContent.get("primarySurgeon") != null) {
          Map<String, Object> primarySurgeonDetails = (Map<String, Object>) 
                    packContent.get("primarySurgeon");
          priSurgeon.add((String) primarySurgeonDetails.get("surgeon_id"));
        } else if ("ANAOPE".equals(packContent.get("charge_head")) 
                && packContent.get("primaryAnaesthetist") != null) {
          Map<String, Object> primaryAnaesthetistDetails = (Map<String, Object>) 
                    packContent.get("primaryAnaesthetist");
          priAnaesthetist.add((String) primaryAnaesthetistDetails.get("anaesthetist_id"));
        }
      }
      List<BasicDynaBean> packageComponentsBeanList = packagesService
          .getPackageComponents(packageId, orgId, bedType);
      if (packageComponentsBeanList != null
          && !packageComponentsBeanList.isEmpty()) {
        int packageComponentSize = packageComponentsBeanList.size();
        int chargeInc = 0;
        for (int intVal = 0; intVal < packageComponentSize; intVal++) {
          BasicDynaBean packageComponentBean = packageComponentsBeanList.get(intVal);
          BigDecimal totalItemQty = new BigDecimal((int) packageComponentBean.get("activity_qty"));
          BigDecimal charge =
              ((BigDecimal) packageComponentBean.get("activity_charge")).divide(totalItemQty, 2);
          BigDecimal packageCharge = (BigDecimal) packageComponentBean.get("package_charge");
          //discount will be package discount
          BigDecimal packageDiscount = (BigDecimal) packageComponentBean.get("package_discount");
          BigDecimal discount = BigDecimal.ZERO;
          //discount split
          if ((packageCharge.compareTo(BigDecimal.ZERO) != 0)
              && (packageDiscount.compareTo(BigDecimal.ZERO) != 0)) {
            BigDecimal newCharg = charge.divide(packageCharge, 10, RoundingMode.CEILING);
            discount =
                (BigDecimal) packageDiscount.multiply(newCharg);
          }
          totalDiscount = totalDiscount.add(discount);
          if ("INVITE".equals(packageComponentBean.get("charge_head"))) {
            chargeValue = chargeValue.add(charge.multiply(totalItemQty));
            chargeDiscount = chargeDiscount.add(discount.multiply(totalItemQty));
            if (otherParams.get("exclude_invite") != null
                && (Boolean) otherParams.get("exclude_invite")) {
              continue;
            }
          }
          String actDepartmentId = (String) packageComponentBean.get("act_department_id");
          if ("SUOPE".equals(packageComponentBean.get("charge_head"))
              && CollectionUtils.isNotEmpty(priSurgeon)) {
            String surgeonId = priSurgeon.get(0).toString();
            Map<String, Object> doctorDetails = doctorService.getDoctorDetails(surgeonId);
            actDepartmentId = (String) doctorDetails.get("dept_id");
          } else if ("ANAOPE".equals(packageComponentBean.get("charge_head"))
              && CollectionUtils.isNotEmpty(priAnaesthetist)) {
            String anaesthetistId = priAnaesthetist.get(0).toString();
            Map<String, Object> doctorDetails = doctorService.getDoctorDetails(anaesthetistId);
            actDepartmentId = (String) doctorDetails.get("dept_id");
          }
          BasicDynaBean chargeBean = billChargeService.setBillChargeBean(chargeGroup,
              (String) packageComponentBean.get("charge_head"), charge, totalItemQty,
              discount.multiply(totalItemQty),
              String.valueOf(packageComponentBean.get("pack_ob_id")),
              (String) packageComponentBean.get("activity_description"), 
              actDepartmentId,
              (Integer) packageComponentBean.get("service_sub_group_id"), insuranceCategoryId,
              isInsurance);
          String packContChargeId = "_" + mainChargeId + "_" + String.valueOf(chargeInc);
          if (otherParams.get("insert_charges") != null
              && (Boolean) otherParams.get("insert_charges")) {
            packContChargeId = billChargeService.getNextPrefixedId();
          }
          chargeBean.set("charge_id", packContChargeId);
          if (null != packageComponentBean.get("consultation_type_id")) {
            chargeBean.set("consultation_type_id",
                packageComponentBean.get("consultation_type_id"));
          } else {
            chargeBean.set("consultation_type_id",0);
          }
          if (billingGroupId != null) {
            chargeBean.set("billing_group_id", billingGroupId);
          }
          if (null != packageComponentBean.get("operation_id")) {
            chargeBean.set("op_id", (String) packageComponentBean.get("operation_id"));
          } else {
            chargeBean.set("op_id", null);
          }
          if (null != packageComponentBean.get("panel_id")) {
            chargeBean.set("panel_id", packageComponentBean.get("panel_id"));
          } else {
            chargeBean.set("panel_id", null);
          }
          if (null != packageComponentBean.get("bed_id")) {
            chargeBean.set("act_description_id",
                String.valueOf(packageComponentBean.get("bed_id")));
          }
          chargeBean.set("allow_rate_increase", (Boolean) itemType.get("allow_rate_increase"));
          chargeBean.set("allow_rate_decrease", (Boolean) itemType.get("allow_rate_decrease"));
          chargeBean.set("allow_discount", (Boolean) itemType.get("allow_discount"));
          String allowZeroClaimfor = (String) packageComponentBean.get("allow_zero_claim_amount");
          if ("PKGPKG".equals(packageComponentBean.get("charge_head"))) {
            chargeBean.set("act_description",itemType.get("package_name"));
          }
          if ("I".equals(otherParams.get("submission_batch_type")) 
              && (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor))) {
            chargeBean.set("allow_zero_claim", true);
          }

          if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
            if (otherParams.get("item_excluded_from_doctor").equals("Y")
                || otherParams.get("item_excluded_from_doctor").equals(true)) {
              chargeBean.set("item_excluded_from_doctor", true);
              chargeBean.set("item_excluded_from_doctor_remarks",
                  otherParams.get("item_excluded_from_doctor_remarks"));
            } else if (otherParams.get("item_excluded_from_doctor").equals("N")
                || otherParams.get("item_excluded_from_doctor").equals(false)) {
              chargeBean.set("item_excluded_from_doctor", false);
              chargeBean.set("item_excluded_from_doctor_remarks",
                  otherParams.get("item_excluded_from_doctor_remarks"));
            }
          }
          chargeBean.set("package_id", packageId);
          
          if (subBatchType != null && !subBatchType.isEmpty()) {
            chargeBean.set("submission_batch_type", subBatchType);
          } else {
            String submissionBatchType = (String)packagesService.findByKey("package_id", packageId)
                .get("submission_batch_type");
            chargeBean.set("submission_batch_type", submissionBatchType);
          }
          if ("TCOPE".equals(packageComponentBean.get("charge_head"))) {
            BasicDynaBean prefs = ipPreferencesService.getPreferences();
            chargeBean.set("code_type", prefs.get("theatre_charge_code_type"));
            chargeBean.set("act_rate_plan_item_code", prefs.get("theatre_daily_charge_code"));
          } else if ("ANATOPE".equals(packageComponentBean.get("charge_head"))
              && CollectionUtils.isNotEmpty(anesDetails)) {
            String anaesthesiaTypeId = anesDetails.get(0).toString();
            BasicDynaBean anaesthesiaTypeBean = anesthesiaTypeChargesService
                .getAnesthesiaTypeCharge(anaesthesiaTypeId, bedType, orgId);
            chargeBean.set("code_type", anaesthesiaTypeBean.get("code_type"));
            chargeBean.set("act_rate_plan_item_code", anaesthesiaTypeBean.get("item_code"));
            anesDetails.remove(0);
          } else {
            if (packageComponentBean.get("code_type") != null) {
              chargeBean.set("code_type", packageComponentBean.get("code_type"));
            }
            if (packageComponentBean.get("ct_code") != null) {
              chargeBean.set("act_rate_plan_item_code", packageComponentBean.get("ct_code"));
            }
          }
          packagesChargesList.add(chargeBean);
          chargeInc++;
        }
      }
    }
    BasicDynaBean billChargeBean = billChargeService.setBillChargeBean(chargeGroup, "PKGPKG",
        chargeValue, chargeQty,chargeDiscount,
        itemType.get(PACKAGE_ID).toString(), (String) itemType.get("package_name"), null,
        (Integer) itemType.get("service_sub_group_id"), insuranceCategoryId, isInsurance);

    if (itemType.getDynaClass().getDynaProperty(CODE_TYPE) != null
        && itemType.get(CODE_TYPE) != null && !itemType.get(CODE_TYPE).equals("")) {
      billChargeBean.set(CODE_TYPE, (String) itemType.get(CODE_TYPE));
    }

    billChargeBean.set("allow_rate_increase", (Boolean) itemType.get("allow_rate_increase"));
    billChargeBean.set("allow_rate_decrease", (Boolean) itemType.get("allow_rate_decrease"));
    billChargeBean.set("allow_discount", (Boolean) itemType.get("allow_discount"));
    if (billingGroupId != null) {
      billChargeBean.set("billing_group_id", billingGroupId);
    }
    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (otherParams.get("item_excluded_from_doctor").equals("Y")
          || otherParams.get("item_excluded_from_doctor").equals(true)) {
        billChargeBean.set("item_excluded_from_doctor", true);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else if (otherParams.get("item_excluded_from_doctor").equals("N")
          || otherParams.get("item_excluded_from_doctor").equals(false)) {
        billChargeBean.set("item_excluded_from_doctor", false);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }
    billChargeBean.set("package_id", packageId);

    if (null != otherParams.get("consultation_type_id")) {
      billChargeBean.set("consultation_type_id",
          otherParams.get("consultation_type_id"));
    } else {
      billChargeBean.set("consultation_type_id",0);
    }
    
    
    if (subBatchType != null && !subBatchType.isEmpty()) {
      billChargeBean.set("submission_batch_type", subBatchType);
    } else {
      String submissionBatchType = (String)packagesService.findByKey("package_id", packageId)
          .get("submission_batch_type");
      billChargeBean.set("submission_batch_type", submissionBatchType);
    }
    
    billChargeBean.set("panel_id", null);
    billChargeBean.set("op_id", null);
    billChargeBean.set("charge_id", mainChargeId);
    
    if (otherParams != null && otherParams.get("preauth_act_id") != null) {
      billChargeBean.set("preauth_act_id", otherParams.get("preauth_act_id"));
    }

    billChargeBean.set("code_type", otherParams.get("code_type"));
    billChargeBean.set("act_rate_plan_item_code", otherParams.get("act_rate_plan_item_code"));
    packagesChargesList.add(billChargeBean);
    return packagesChargesList;
  }

  /**
   * Getting Package Order Bean that need to be ordered.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<BasicDynaBean> getOrderBean(Map<String, List<Object>> requestParams,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds) {

    String priAuthIdKey = prefix + PRIOR_AUTH_ID;
    List<BasicDynaBean> orderBeanList = new ArrayList<BasicDynaBean>();
    Object[] orderedItemList = requestParams.get(prefix).toArray();
    List errorList = new ArrayList();

    if (orderedItemList != null) {
      for (int i = 0; i < orderedItemList.length; i++) {
        BasicDynaBean orderedItemBean = packageOrderItemRepository.getBean();

        Object priAuthId = ((Map<String, Object>) orderedItemList[i]).get(priAuthIdKey);
        orderedItemAuths.get("newPreAuths").add((String) (priAuthId));
        ConversionUtils.copyJsonToDynaBeanPrefixed((Map) orderedItemList[i], orderedItemBean,
            errorList, prefix + "_");
        setHeaderProperties(orderedItemBean, headerInformation, username, false,
            (Map) orderedItemList[i]);
        orderBeanList.add(orderedItemBean);
      }
    }

    return orderBeanList;
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) throws ParseException {

    BasicDynaBean orderedItemBean = packageOrderItemRepository.getBean();

    Object priAuthId = item.get("prior_auth_id");
    orderedItemAuths.get("newPreAuths").add((String) (priAuthId));

    List errorList = new ArrayList();
    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, true);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }
    setItemBeanProperties(orderedItemBean, headerInformation, username, false, item, null);

    return orderedItemBean;
  }

  /**
   * Order the component inside the packages without entering charges(Services /Tests/ Other
   * Charges). Inserts vDoctor into Doctor Consultation. Not posting charges as charge are posted
   * against packages.
   * 
   * @param packageBean        the packageBean
   * @param headerInformation  the headerInformation
   * @param username           the username
   * @param centerId           the centerId
   * @param chargeList         the chargeList
   * @param packageItemDetails the packageItemDetails
   * @throws Exception the Exception
   */
  @SuppressWarnings("unchecked")
  public void insertPackageComponent(BasicDynaBean packageBean, BasicDynaBean headerInformation,
      String username, Integer centerId, List<BasicDynaBean> chargeList,
      Map<String, Object> packageItemDetails) {

    /* While customizing the package ,if any new item added ,it will not be 
     * available in package_contents table 
     * hence fetching the details from packageItemDetails List Map 
     * instead of package components master */
    List<Map<String, Object>> packageComponent = (List<Map<String, Object>>) packageItemDetails
         .get(prefix + "_package_contents");
    int index = 0;
    int chargeIndex = 0;
    List<BasicDynaBean>  docChargeIds = new ArrayList<BasicDynaBean>();
    for (Map<String, Object> packItem : packageComponent) {
      if (packageComponent.size() == 1 && packItem.get(ITEM_TYPE) == null) {
        break;
      }

      if (packItem.get(ITEM_TYPE) == null) {
        log.warn("Unsupported item in package master, package ID: " + packageBean.get(PACKAGE_ID)
            + " head: " + packItem.get("charge_head"));
        break;
      }

      ArrayList<Object> packageConductingDoctorList = (ArrayList<Object>) packageItemDetails
          .get(prefix + "_package_conducting_doctor");

      String bedType = (String) headerInformation.get("bed_type");
      String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
      String patientId = (String) headerInformation.get(PATIENT_ID);
      BasicDynaBean genericPrefBean = genericPreferencesService.getAllPreferences();

      if (packItem.get(ITEM_TYPE).equals("Laboratory")
          || packItem.get(ITEM_TYPE).equals("Radiology")) {
        int prescId = testOrderItemRepository.getNextSequence();
        BasicDynaBean testBean = testOrderItemService.getBean();
        setBeanValue(testBean, "test_id", packItem.get(ACTIVITY_ID));
        BasicDynaBean masterTestCharges = testOrderItemService
            .getMasterChargesBean(testBean.get("test_id"), bedType, ratePlanId, centerId);
        if (null != masterTestCharges && (Boolean) masterTestCharges.get("applicable")) {

          setBeanValue(testBean, "pres_date", packageBean.get(PRESCRIBED_DATE));
          setBeanValue(testBean, "pres_doctor", packageBean.get(DOCTOR_ID));
          setBeanValue(testBean, "conducted", "N");
          setBeanValue(testBean, "priority", "R");
          setBeanValue(testBean, REMARKS, packageBean.get(REMARKS));
          setBeanValue(testBean, PACKAGE_REFERENCE, packageBean.get(PRESCRIPTION_ID));
          setBeanValue(testBean, "prescription_type",
              ((String) masterTestCharges.get("house_status")).equals("I") ? "h" : "o");
          setBeanValue(testBean, MR_NO, (String) headerInformation.get(MR_NO));
          setBeanValue(testBean, COMMON_ORDER_ID, headerInformation.get("commonorderid"));
          setBeanValue(testBean, USER_NAME, username);
          setBeanValue(testBean, "pat_id", patientId);
          setBeanValue(testBean, "prescribed_id", prescId);
          setBeanValue(testBean, "curr_location_presc_id", prescId);
          setPackageRef(testBean, false, (Integer) headerInformation.get("packageref"));

          testOrderItemService.setLabNo(genericPrefBean, masterTestCharges, testBean,
              headerInformation);
          testOrderItemService.setTokenNo(masterTestCharges, genericPrefBean, testBean, centerId);
          testOrderItemService.setReadyTime(testBean, centerId);
          testOrderItemService.setConducted(masterTestCharges, testBean);
          testOrderItemService.setConductionType(testBean, centerId);

          testOrderItemRepository.insert(testBean);

          String conductingDoctorId = null;
          for (Object packageConductingDoctor : packageConductingDoctorList) {
            Map<String, Object> packageConductingDoctorMap =
                (Map<String, Object>) packageConductingDoctor;
            int actIndex = (Integer) packageConductingDoctorMap.get("package_activity_index");
            if (actIndex == index) {
              conductingDoctorId = (String) packageConductingDoctorMap.get("package_doctor_id");
              break;
            }
          }
          BasicDynaBean charge = chargeList.get(chargeIndex);
          chargeIndex++;
          String chargeId = (String) charge.get("charge_id");
          if (chargeId != null) {
            testOrderItemService.insertPackageBillActivityCharge(testBean, headerInformation,
                 centerId, chargeId, conductingDoctorId);
          }
        }
      } else if (packItem.get(ITEM_TYPE).equals("Service")) {
        BasicDynaBean serviceBean = serviceOrderItemService.getBean();
        setBeanValue(serviceBean, "service_id", packItem.get(ACTIVITY_ID));
        setBeanValue(serviceBean, "quantity", BigDecimal.ONE);
        setBeanValue(serviceBean, PRESCRIBED_DATE, packageBean.get(PRESCRIBED_DATE));
        setBeanValue(serviceBean, DOCTOR_ID, packageBean.get(DOCTOR_ID));
        setBeanValue(serviceBean, "conducted", "N");
        setBeanValue(serviceBean, REMARKS, packageBean.get(REMARKS));
        setBeanValue(serviceBean, PACKAGE_REFERENCE, packageBean.get(PRESCRIPTION_ID));
        BasicDynaBean masterServiceBean = serviceOrderItemService
            .getMasterChargesBean(serviceBean.get("service_id"), bedType, ratePlanId, centerId);
        setBeanValue(serviceBean, "specialization", masterServiceBean.get("specialization"));

        if ((Boolean) masterServiceBean.get("applicable")) {
          setBeanValue(serviceBean, MR_NO, (String) headerInformation.get(MR_NO));
          setBeanValue(serviceBean, COMMON_ORDER_ID, headerInformation.get("commonorderid"));
          setBeanValue(serviceBean, USER_NAME, username);
          setBeanValue(serviceBean, PATIENT_ID, patientId);
          setBeanValue(serviceBean, PRESCRIPTION_ID, serviceOrderItemRepository.getNextSequence());
          serviceOrderItemService.setConduction(masterServiceBean, serviceBean);
          serviceOrderItemRepository.insert(serviceBean);

          String conductingDoctorId = null;
          for (Object packageConductingDoctor : packageConductingDoctorList) {
            Map<String, Object> packageConductingDoctorMap =
                (Map<String, Object>) packageConductingDoctor;
            int actIndex = (Integer) packageConductingDoctorMap.get("package_activity_index");
            if (actIndex == index) {
              conductingDoctorId = (String) packageConductingDoctorMap.get("package_doctor_id");
              break;
            }
          }
          BasicDynaBean charge = chargeList.get(chargeIndex);
          chargeIndex++;
          String chargeId = (String) charge.get("charge_id");
          if (chargeId != null) {
            serviceOrderItemService.insertPackageBillActivityCharge(serviceBean, 
                    headerInformation, centerId, chargeId, conductingDoctorId);
          }
        }
      } else if ("Implant".equals(packItem.get(ITEM_TYPE))
          || "Consumable".equals(packItem.get(ITEM_TYPE))
          || "Other Charge".equals(packItem.get(ITEM_TYPE))) {

        BasicDynaBean otherOrderItemBean = otherOrderItemServices.getBean();
        setBeanValue(otherOrderItemBean, "service_name", packItem.get(ACTIVITY_ID));
        setBeanValue(otherOrderItemBean, "pres_time", packageBean.get(PRESCRIBED_DATE));
        setBeanValue(otherOrderItemBean, DOCTOR_ID, packageBean.get(DOCTOR_ID));
        setBeanValue(otherOrderItemBean, REMARKS, packageBean.get(REMARKS));
        setBeanValue(otherOrderItemBean, "service_group", packItem.get("charge_head"));
        setBeanValue(otherOrderItemBean, PACKAGE_REFERENCE, packageBean.get(PRESCRIPTION_ID));
        setBeanValue(otherOrderItemBean, MR_NO, (String) headerInformation.get(MR_NO));
        setBeanValue(otherOrderItemBean, COMMON_ORDER_ID, headerInformation.get("commonorderid"));
        setBeanValue(otherOrderItemBean, USER_NAME, username);
        setBeanValue(otherOrderItemBean, PATIENT_ID, patientId);
        setBeanValue(otherOrderItemBean, "prescribed_id",
            otherOrderItemRepository.getNextSequence());

        otherOrderItemRepository.insert(otherOrderItemBean);
        BasicDynaBean charge = chargeList.get(chargeIndex);
        String chargeId = (String) charge.get("charge_id");
        if (chargeId != null) {
          otherOrderItemServices.insertPackageBillActivityCharge(otherOrderItemBean,
                  headerInformation, centerId, chargeId, null);
        }
        chargeIndex++;
      } else if ("OPDOC".equals(packItem.get("charge_head"))) {
        BasicDynaBean charge = chargeList.get(chargeIndex);
        docChargeIds.add(charge);
        chargeIndex++;
      } else {
        BasicDynaBean charge = chargeList.get(chargeIndex);
        int packObjId = (int) (packItem.get("pack_ob_id") != null 
            ? (packItem.get("pack_ob_id")) : 0) ;
        int actDescId = charge.get("act_description_id") != null 
            ? Integer.valueOf((String) charge.get("act_description_id")) : 0 ;
        if (charge.get("charge_head").equals(packItem.get("charge_head")) 
             && (packObjId == actDescId)) {
          chargeIndex++;
        }
      }
    }

    ArrayList<Object> packageIdDoctorList = (ArrayList<Object>) packageItemDetails
        .get(prefix + "_package_id_for_doc");
    int docChargeIndex = 0;
    for (Object packageIdDoctor : packageIdDoctorList) {
      Map<String, Object> packageIdDoctormap = (Map<String, Object>) packageIdDoctor;
      BasicDynaBean itemBean = doctorOrderItemRepository.getBean();
      setBeanValue(itemBean, "doctor_name", packageIdDoctormap.get("package_visited_doctor"));

      DynaProperty dynaProperties = itemBean.getDynaClass().getDynaProperty("visited_date");
      setBeanValue(itemBean, "visited_date", ConvertUtils.convert(
          packageIdDoctormap.get("package_doc_visit_date_time"), dynaProperties.getType()));

      setBeanValue(itemBean, PRESCRIBED_DATE, packageBean.get(PRESCRIBED_DATE));
      setBeanValue(itemBean, REMARKS, packageBean.get(REMARKS));
      setBeanValue(itemBean, "head", packageIdDoctormap.get("package_doctor_head"));
      setBeanValue(itemBean, PACKAGE_REFERENCE, packageBean.get(PRESCRIPTION_ID));
      setBeanValue(itemBean, MR_NO, (String) headerInformation.get(MR_NO));
      setBeanValue(itemBean, COMMON_ORDER_ID, headerInformation.get("commonorderid"));
      setBeanValue(itemBean, "username", username);
      setBeanValue(itemBean, PATIENT_ID, headerInformation.get(PATIENT_ID));
      setBeanValue(itemBean, "consultation_id", doctorOrderItemRepository.getNextSequence());

      doctorOrderItemService.setConsultationToken(itemBean, headerInformation);
      doctorOrderItemService.setStatus(itemBean);

      doctorOrderItemRepository.insert(itemBean);
      if (docChargeIds.size() > 0) {
        BasicDynaBean docChargeId = docChargeIds.get(docChargeIndex);
        String chargeId = (String) docChargeId.get("charge_id");
        if (chargeId != null) {
          doctorOrderItemService.insertPackageBillActivityCharge(itemBean, headerInformation,
              centerId, chargeId, null);
          docChargeIndex ++;
        }
      }
    }
  }

  /**
   * Returns the next sequence for package_prescribed_sequence.
   * 
   * @return integer
   */
  public Integer getNextSequence() {
    return packageOrderItemRepository.getNextSequence();
  }

  /**
   * Insert the Patient Package details.
   * 
   * @param packageOrderItemService the packageOrderItemService
   */
  public void insertPatientPackages(BasicDynaBean packageOrderItemService) {
    BasicDynaBean patientPackage = patientPackagesRepository.getBean();
    patientPackage.set(MR_NO, packageOrderItemService.get(MR_NO));
    patientPackage.set(PACKAGE_ID, packageOrderItemService.get(PACKAGE_ID));
    patientPackage.set(PATIENT_PACKAGE_ID, packageOrderItemService.get(PATIENT_PACKAGE_ID));
    patientPackagesRepository.insert(patientPackage);
  }

  /**
   * Insert the Patient Package contents.
   * 
   * @param packageDetails Package Details
   * @param packageOrderItemService the packageOrderItemService
   * @param chargesList the chargesList
   * @param userName user name
   * @param bedType BedType
   * @param orgId OrgId
   *
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> insertPatientPackageContents(Map<String, Object> packageDetails,
      BasicDynaBean packageOrderItemService, List<BasicDynaBean> chargesList,
      String userName, String bedType, String orgId) {
    List<BasicDynaBean> packageContents = new ArrayList<>();

    //removing the prefix packages from packageDetails map
    Map<String, Object> newMap = new HashMap<>();
    Iterator<String> it = packageDetails.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      if (key.startsWith("packages_")) {
        newMap.put(key.replaceFirst("packages_", ""), packageDetails.get(key));
        it.remove();
      }
    }
    packageDetails.putAll(newMap);
    Boolean isCustomizedCharge = packageDetails.get("is_customized") != null
        ? (Boolean) packageDetails.get("is_customized")
        : (Boolean) packageDetails.get("allow_customization") ;
    if (packageDetails.get("package_contents") != null) {
      List<Map<String, Object>> packContents =
          (List<Map<String, Object>>) packageDetails.get("package_contents");

      Integer chargeIndex = 0;
      List<BasicDynaBean> patPackContentCharges = new ArrayList<>();
      List<BasicDynaBean> patPackContentContConsumed = new ArrayList<>();
      Map<Integer, String> contentRefMap = new HashMap<>();
      Map<String, Integer> contentBaseMap = new HashMap<>();
      String submissionType = "P";
      if (null != packageDetails.get("submission_batch_type")) {
        submissionType = (String) packageDetails.get("submission_batch_type");
      }
      for (Map<String, Object> packCont : packContents) {
        //patient package content insertion
        Integer patientPackageContentId = getPatientPackageContentCharges(packCont, userName,
            bedType, orgId, packageContents, patPackContentCharges,
            packageOrderItemService.get(PATIENT_ID),
            packageOrderItemService.get(PACKAGE_ID),
            packageOrderItemService.get(PATIENT_PACKAGE_ID), submissionType);
        if (packCont.get("content_id_ref") != null) {
          contentRefMap.put(patientPackageContentId, packCont.get("content_id_ref").toString());
          if (!contentBaseMap.containsKey(packCont.get("content_id_ref"))) {
            contentBaseMap.put(packCont.get("content_id_ref").toString(),
                patientPackageContentId);
          }
        }
        //patient package consumed insertion
        BasicDynaBean patPackContConsumed =
            patientPackageContentConsumedRepository.getBean();
        patPackContConsumed.set("patient_package_content_id", patientPackageContentId);
        Integer quantity = (null != packCont.get("activity_qty")) 
            ? (Integer) packCont.get("activity_qty") : 0;
        if (quantity == 0 && null != packCont.get("act_quantity")) {
          quantity = (Integer) packCont.get("act_quantity");
        }
        patPackContConsumed.set("quantity", quantity);
        String chargeId = null;
        if (!"INVITE".equals(packCont.get("charge_head"))) {
          BasicDynaBean charge = chargesList.get(chargeIndex);
          chargeId = (String) charge.get("charge_id");
          chargeIndex++;
        } else {
          continue;
        }
        patPackContConsumed.set("bill_charge_id", chargeId);
        patPackContConsumed.set(PRESCRIPTION_ID,
            packageOrderItemService.get(PRESCRIPTION_ID));
        patPackContConsumed.set("item_type", packCont.get("item_type"));
        patPackContentContConsumed.add(patPackContConsumed);
      }
      List<BasicDynaBean> patPackContents = new ArrayList<>();
      for (BasicDynaBean patPackContent : packageContents) {
        if (contentRefMap.containsKey(patPackContent.get("patient_package_content_id"))) {
          patPackContent.set("content_id_ref",
              contentBaseMap.get(
                  contentRefMap.get(patPackContent.get("patient_package_content_id"))));
        } else {
          patPackContent.set("content_id_ref", null);
        }
        patPackContents.add(patPackContent);
      }
      patientPackageContentsRepository.batchInsert(patPackContents);
      Boolean multiVisitPackage = (Boolean) packageDetails.get("multi_visit_package") != null
          ? (Boolean) packageDetails.get("multi_visit_package") : false;
      if (isCustomizedCharge || multiVisitPackage) {
        patientPackageContentChargesRepository.batchInsert(patPackContentCharges);
        insertPatientPackageCustomized(packageOrderItemService.get(PATIENT_PACKAGE_ID),
                packageDetails, userName);
      }
      patientPackageContentConsumedRepository.batchInsert(patPackContentContConsumed);
      updatePatientPackageStatus((Integer) packageOrderItemService.get(PATIENT_PACKAGE_ID));
    }
    return packageContents;
  }

  /**
   * Get patient package content charges.
   * 
   * @param packCont the Package Content
   * @param userName the user name
   * @param bedType the  bedType
   * @param orgId the  orgId
   * @param packageContents Package Contents
   * @param patPackContentCharges Package Content charges
   * @param visitId the  visitId
   * @param packageId the  packageId
   * @param patientPackageId the  patPackageId
   * @param submissionType the  submissionType
   * 
   * @return Integer
   */
  public Integer getPatientPackageContentCharges(Map<String, Object> packCont,
      String userName, String bedType, String orgId,
      List<BasicDynaBean> packageContents, List<BasicDynaBean> patPackContentCharges,
      Object visitId, Object packageId, Object patientPackageId, String submissionType) {
    Integer patientPackageContentId = patientPackageContentsRepository.getNextSequence();
    BasicDynaBean patientPackageContent = patientPackageContentsRepository.getBean();
    patientPackageContent.set("patient_package_content_id", patientPackageContentId);
    patientPackageContent.set(VISIT_ID, visitId);
    patientPackageContent.set(PACKAGE_ID, packageId);
    patientPackageContent.set("patient_package_id", patientPackageId);
    patientPackageContent.set(ACTIVITY_ID, packCont.get(ACTIVITY_ID));
    patientPackageContent.set("activity_type", packCont.get("item_type"));
    patientPackageContent.set("activity_units", packCont.get("activity_units"));
    patientPackageContent.set("panel_id", packCont.get("panel_id"));
    patientPackageContent.set("bed_id", packCont.get("bed_id"));
    patientPackageContent.set("operation_id", packCont.get("operation_id"));
    patientPackageContent.set("activity_remarks", packCont.get("remarks"));
    patientPackageContent.set("package_content_id", packCont.get("pack_ob_id"));
    patientPackageContent.set("created_by", userName);
    patientPackageContent.set("activity_qty", packCont.get("activity_qty"));
    patientPackageContent.set("display_order", packCont.get("display_order"));
    patientPackageContent.set("charge_head", packCont.get("charge_head"));
    patientPackageContent.set("submission_type", submissionType);
    patientPackageContent.set("consultation_type_id", packCont.get("consultation_type_id"));
    packageContents.add(patientPackageContent);

    //patient package content charges insertion
    BasicDynaBean patientPackContChargeRepo =
        patientPackageContentChargesRepository.getBean();
    patientPackContChargeRepo.set("patient_package_content_id", patientPackageContentId);
    patientPackContChargeRepo.set("org_id", orgId);
    patientPackContChargeRepo.set("bed_type", bedType);
    BigDecimal charge = BigDecimal.ZERO;
    if (null != packCont.get("activity_charge")) {
      charge = new BigDecimal(String.valueOf(packCont.get("activity_charge")));      
    } else if (null != packCont.get("package_charge")) {
      charge = new BigDecimal(String.valueOf(packCont.get("package_charge")));
    }
    patientPackContChargeRepo.set("charge",charge);
    patientPackContChargeRepo.set("discount", BigDecimal.ZERO);
    patientPackContChargeRepo.set("created_by", userName);
    patPackContentCharges.add(patientPackContChargeRepo);

    return patientPackageContentId;
  }

  /**
   * Insert patient package content charges.
   * 
   * @param packageContents Package Contents
   * @param patPackContentCharges Package Content charges
   */
  public void insertPatientPackageContentCharges(List<BasicDynaBean> packageContents,
      List<BasicDynaBean> patPackContentCharges) {
    patientPackageContentsRepository.batchInsert(packageContents);
    patientPackageContentChargesRepository.batchInsert(patPackContentCharges);
  }

  /**
   * get patient's package contents for operation component in mvp.
   * @param patientPackageId the patientPackageId
   * @param opId the operationID
   * @return list of patientPackageContentsBean
   */
  public List<BasicDynaBean> getPatientPackageContentsForOperation(Object patientPackageId,
      Object opId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("patient_package_id", patientPackageId);
    filterMap.put("operation_id", opId);
    filterMap.put("activity_type", "Operation");
    return patientPackageContentsRepository.findByCriteria(filterMap);
  }

  /**
   * Find the Patient Package Content and update its details.
   *
   * @param packageContentId the patient package content id
   * @param patientPackageId the patient package id
   * @param qty the qty
   * @param chargeId the charge id
   * @param prescId the presc id
   * @param itemType the item type
   */
  public void insertPatientPackageConsumed(Object patpackageContentId, Object packageContentId, 
      Object patientPackageId, Object qty, Object chargeId, Object prescId, Object itemType) {
    Map<String, Object> filterMap = new HashMap<>();
    if (patpackageContentId != null && patpackageContentId != "") {
      filterMap.put("patient_package_content_id", patpackageContentId);
    }
    filterMap.put("patient_package_id", patientPackageId);
    filterMap.put("package_content_id", packageContentId);
    BasicDynaBean patPackageContent = patientPackageContentsRepository.findByKey(filterMap);
    if (patPackageContent != null) {
      BasicDynaBean patPackContConsumed =
          patientPackageContentConsumedRepository.getBean();
      patPackContConsumed.set("patient_package_content_id",
          patPackageContent.get("patient_package_content_id"));
      patPackContConsumed.set("quantity", qty);
      patPackContConsumed.set("bill_charge_id", chargeId);
      patPackContConsumed.set("prescription_id", prescId);
      patPackContConsumed.set("item_type", itemType);
      patientPackageContentConsumedRepository.insert(patPackContConsumed);
    }
  }
 

  /**
   * Insert patient package customized.
   *
   * @param patientPackageId the pat pack id
   * @param packDetails package Details
   * @param userName User Name
   */
  public void insertPatientPackageCustomized(Object patientPackageId,
      Map<String, Object> packDetails, String userName) {
    BigDecimal discount =
        new BigDecimal(String.valueOf(packDetails.get("discount")));
    BigDecimal charge =
        new BigDecimal(String.valueOf(packDetails.get("charge")));
    BasicDynaBean patConsPackDetail =
        patientCustomisedPackageDetailsRepository.getBean();
    patConsPackDetail.set("patient_package_id", patientPackageId);
    patConsPackDetail.set("amount",charge);
    patConsPackDetail.set("discount",discount);
    patConsPackDetail.set("package_name",
        packDetails.get("package_name"));
    patConsPackDetail.set("multi_visit_package",
        packDetails.get("multi_visit_package"));
    patConsPackDetail.set("package_code",
        packDetails.get("package_code"));
    patConsPackDetail.set("package_terms_conditions",
        packDetails.get("conditions"));
    patConsPackDetail.set("pre_requisites",
        packDetails.get("pre_requisites"));
    patConsPackDetail.set("remarks",
        packDetails.get("approval_remarks"));
    patConsPackDetail.set("created_by", userName);
    patConsPackDetail.set("is_customized_package", 
        packDetails.get("is_customized") != null ? packDetails.get("is_customized") : false);
    patConsPackDetail.set("consumption_validity_value",
            packDetails.get("consumption_validity_value"));
    patConsPackDetail.set("consumption_validity_unit",
            packDetails.get("consumption_validity_unit"));
    patientCustomisedPackageDetailsRepository.insert(patConsPackDetail);
  }

  /**
   * Split discount.
   * 
   * @param charge Main charge
   * @param packageCharge Package charge
   * @param packageDiscount Package discount
   * 
   * @return BigDecimal
   */
  public BigDecimal discountSplit(BigDecimal charge, BigDecimal packageCharge,
      BigDecimal packageDiscount) {
    BigDecimal discount = BigDecimal.ZERO;
    if ((packageCharge.compareTo(BigDecimal.ZERO) != 0)
        && (packageDiscount.compareTo(BigDecimal.ZERO) != 0)) {
      BigDecimal newCharg = charge.divide(packageCharge, 10, RoundingMode.CEILING);
      discount = (BigDecimal) packageDiscount.multiply(newCharg);
    }
    return discount;
  }
  
  /**
   * Update Patient Package Status.
   * 
   * @param patientPackageId the Pateint Package Id
   */
  public void updatePatientPackageStatus(Integer patientPackageId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(PATIENT_PACKAGE_ID, patientPackageId);
    BasicDynaBean patPackage = patientPackagesRepository.findByPk(filterMap);
    if (patPackage == null) {
      return;
    }
    Map<String, Object> filterContentMap = new HashMap<>();
    filterContentMap.put("patient_package_id", patientPackageId);
    List<BasicDynaBean> patientPackageContents =
        patientPackageContentsRepository.findByCriteria(filterContentMap);
    //Fall back to old method ,since migration has not done into 
    //new package transaction tables for mvp which has partially consumed in 12.3.
    if (patientPackageContents == null || patientPackageContents.isEmpty()) {
      multiVisitPackageService.updateMultivisitPackageStatus((int) patPackage.get("package_id"),
          patientPackageId, (String) patPackage.get("mr_no"));
    } else {
      List<BasicDynaBean> patientPackageContentsConsumed =
          patientPackageContentConsumedRepository
          .getPatientPackageContentsConsumed(new Object[] { patientPackageId });
      String status = "C";
      Integer packageId = (patientPackageContents.size() > 0)
          ? (Integer) patientPackageContents.get(0).get("package_id") : 0;
      Map<String, Object> packageFilterMap = new HashMap<>();
      packageFilterMap.put("package_id", packageId);
      BasicDynaBean packageBean = packagesRepository.findByPk(packageFilterMap);
      if (null != packageBean) {
        if (patientPackageContents.size() != patientPackageContentsConsumed.size()) {
          status = "P";
        } else {
          for (BasicDynaBean patientPackageContentConsumed : patientPackageContentsConsumed) {
            Integer itemQty = (null != patientPackageContentConsumed.get("content_qty")) 
                ? (Integer) patientPackageContentConsumed.get("content_qty") : 0;
            Integer consumedQty = (null != patientPackageContentConsumed.get("consumed_qty"))
                ? ((Long) patientPackageContentConsumed.get("consumed_qty")).intValue()
                : 0;
            if (itemQty.compareTo(consumedQty) > 0) {
              status = "P";
            }
          }
        }
      }
      if (patPackage != null) {
        patPackage.set("status", status);
        patientPackagesRepository.update(patPackage, filterMap);
      }
    }
  }
  
  /**
   * Returns the list of package item details by passing there id.
   * 
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return packageOrderItemRepository.getItemDetails(entityIdList,
        (List<Object>) paramMap.get("tpa_id"), (List<Object>) paramMap.get("plan_id"),
        (Integer) paramMap.get("center_id"),
        (String) paramMap.get("dept_id"));
  }

  /**
   * returns true if the package is applicable for tpa and plan.
   */
  public boolean getPackageApplicablity(Integer packId, String tpaId, Integer planId) {
    return packageOrderItemRepository.getPackageApplicability(packId, tpaId, planId);
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    if (parameters.get("operation_id") != null || (parameters.get(PACKAGE_REFERENCE) != null
        && ((Boolean) parameters.get(PACKAGE_REFERENCE)))) {
      return Collections.emptyList();
    }
    List<BasicDynaBean> packageOrders = packageOrderItemRepository
        .getOrderedItems((String) parameters.get("visit_id"));
    for (BasicDynaBean packCharge : packageOrders) {
      BigDecimal amount = (BigDecimal) packCharge.get("amount");
      BigDecimal taxAmt = (BigDecimal) packCharge.get("tax_amt");
      BigDecimal discount = (BigDecimal) packCharge.get("discount");
      BigDecimal insuranceClaim = (BigDecimal) packCharge.get("insurance_claim_amount");
      BigDecimal sponsorTax = (BigDecimal) packCharge.get("sponsor_tax_amt");
      List<BasicDynaBean> refPackCharges = billChargeService
          .getChargeReferences((String) packCharge.get("charge_id"));
      for (BasicDynaBean refCharge : refPackCharges) {
        amount = amount.add((BigDecimal)refCharge.get("amount"));
        taxAmt = taxAmt.add((BigDecimal)refCharge.get("tax_amt"));
        discount = discount.add((BigDecimal)refCharge.get("discount"));
        insuranceClaim = insuranceClaim
            .add((BigDecimal)refCharge.get("insurance_claim_amount"));
        sponsorTax = sponsorTax.add((BigDecimal)refCharge
            .get("sponsor_tax_amt"));
        List<BasicDynaBean> refrefPackCharges = billChargeService
            .getChargeReferences((String) refCharge.get("charge_id"));
        for (BasicDynaBean refrefCharge : refrefPackCharges) {
          amount = amount.add((BigDecimal)refrefCharge.get("amount"));
          taxAmt = taxAmt.add((BigDecimal)refrefCharge.get("tax_amt"));
          discount = discount.add((BigDecimal)refrefCharge.get("discount"));
          insuranceClaim = insuranceClaim
              .add((BigDecimal)refrefCharge.get("insurance_claim_amount"));
          sponsorTax = sponsorTax.add((BigDecimal)refrefCharge
              .get("sponsor_tax_amt"));
        }
      }
      packCharge.set("amount", amount);
      packCharge.set("tax_amt", taxAmt);
      packCharge.set("discount", discount);
      packCharge.set("insurance_claim_amount", insuranceClaim);
      packCharge.set("sponsor_tax_amt", sponsorTax);
    }
    return packageOrders;
  }

  /**
   * get multi visit package items.
   *
   * @param visitId the visitId
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getMultivisitPackageItems(String visitId) {
    List<BasicDynaBean> orderedItems = new ArrayList<BasicDynaBean>();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("visit_id", visitId);
    parameters.put(PACKAGE_REFERENCE, true);
    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderService
        .getOrderItemsTypes().iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null) {
        OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
        List<BasicDynaBean> orderedItem = orderItem.getOrderedItems(parameters);
        if (orderedItem != null) {
          orderedItems.addAll(orderedItem);
        }
      }
    }
    return orderedItems;
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIPTION_ID, item.get("order_id"));
    bean.set(REMARKS, item.get(REMARKS));
    bean.set(DOCTOR_ID, item.get("prescribed_doctor_id"));
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    return getCancelBean(item.get("order_id"));
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIPTION_ID, orderId);
    bean.set("status", "X");
    bean.set("cancelled_by", sessionService.getSessionAttributes().get("userId"));
    return bean;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> prescriptionIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescriptionIdsList.add(item.get(PRESCRIPTION_ID));
    }
    keys.put(PRESCRIPTION_ID, prescriptionIdsList);
    return packageOrderItemRepository.batchUpdate(items, keys);
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException,
      ParseException {
    if (cancel) {
      String visitId = (String) itemInfoMap.get("visit_id");
      cancelPackageRefOrders(visitId, items, editOrCancelOrderBills);
    }
  }

  /**
   * Cancel package ref orders.
   *
   * @param visitId                the visit id
   * @param items                  the items
   * @param editOrCancelOrderBills the edit or cancel order bills
   * @throws NoSuchMethodException     the no such method exception
   * @throws IllegalAccessException    the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException               Signals that an I/O exception has occurred.
   * @throws ParseException            the ParseException
   */
  private void cancelPackageRefOrders(String visitId, List<BasicDynaBean> items,
      List<String> editOrCancelOrderBills) throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, IOException, ParseException {
    List<Integer> prescriptionIdList = new ArrayList<>();
    for (BasicDynaBean itemBean : items) {
      Integer prescriptionId = (Integer) itemBean.get(PRESCRIPTION_ID);
      prescriptionIdList.add(prescriptionId);
    }

    List<Object> params = new ArrayList<>();
    params.add(visitId);
    params.add(prescriptionIdList);

    List<Class<?>> paramTypes = new ArrayList<>();
    paramTypes.add(String.class);
    paramTypes.add(List.class);
    List<BasicDynaBean> orders = orderService.callOrderItemServiceMethod("getPackageRefOrders",
        paramTypes, params);

    paramTypes = new ArrayList<>();
    paramTypes.add(Object.class);
    Map<String, List<BasicDynaBean>> cancelItemsTypeMap = new HashMap<>();
    List<BasicDynaBean> cancelBeans;
    for (BasicDynaBean order : orders) {
      params.clear();
      params.add(order.get("order_id"));
      cancelBeans = orderService.callParticularOrderItemServiceMethod("getCancelBean", paramTypes,
          params, (String) order.get("type"));
      if (cancelItemsTypeMap.get(order.get("type")) == null) {
        cancelItemsTypeMap.put((String) order.get("type"), new ArrayList<>(cancelBeans));
      } else {
        cancelItemsTypeMap.get(order.get("type")).addAll(cancelBeans);
      }
    }

    updatePrescriptions(prescriptionIdList);

    // cancel the sub-orders, but without updating related charge or its hasactivity status
    // since this item has no related charge of its own.
    Map<String, Object> itemInfo = new HashMap<>();
    itemInfo.put("visit_id", visitId);
    orderService.updateOrders(cancelItemsTypeMap, true, true, false, editOrCancelOrderBills,
        itemInfo);
  }

  private void updatePrescriptions(List<Integer> prescriptionIdList) {
    List<Integer> patientPrescIds = new ArrayList<>();
    for (Integer prescId : prescriptionIdList) {
      BasicDynaBean bean = packageOrderItemRepository.findByKey("prescription_id", prescId);
      Integer docPrescId = bean == null ? null : (Integer) bean.get("doc_presc_id");
      if (docPrescId != null) {
        patientPrescIds.add(docPrescId);
      }
    }
    patientPrescriptionsRepository.updatePresc(patientPrescIds, "P");
  }


  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // There is no update for packages.
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIPTION_ID;
  }

  @Override
  public String getOrderItemActivityCode() {
    return ACTIVITY_CODE;
  }

  @Override
  public String getPrescriptionDocKey() {
    return DOCTOR_ID;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException {
    if (itemsMapsList.isEmpty()) {
      return Collections.emptyList();
    }
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    String username = (String) billInfoMap.get(USER_NAME);
    String[] preAuthIds = (String[]) billInfoMap.get("pre_auth_ids");
    Integer[] preAuthModeIds = (Integer[]) billInfoMap.get("pre_auth_mode_ids");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    packageOrderItemRepository.batchInsert(itemList);
    String bedType = (String) headerInformation.get("bed_type");
    String orgId = (String) headerInformation.get("bill_rate_plan_id");
    List<Object> newPreAuths = billItemAuthMap.get("newPreAuths");
    for (int index = 0; index < itemList.size(); index++) {
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> packageItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean packageBean = itemList.get(index);

      if (!newPreAuths.isEmpty() && index < newPreAuths.size()) {
        finalPreAuthIds[0] = (String) newPreAuths.get(index);
      }
      if (packageBean.get("doc_presc_id") != null) {
        updatePrescription("O", (Integer) packageBean.get("doc_presc_id"));
      }

      insertPatientPackages(packageBean);
      List<BasicDynaBean> chargesList = new ArrayList<>();
      if (null != bill) {
        chargesList = insertOrderItemCharges(chargeable, headerInformation,
            packageBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, "", ACTIVITY_CODE,
            centerId, false, packageItemDetails);
      }

      List<BasicDynaBean> packageContents =
          insertPatientPackageContents(packageItemDetails, packageBean,
              chargesList, username, bedType, orgId);
      insertPackageContents(packageBean, headerInformation, username, centerId,
          packageItemDetails, packageContents, chargesList);
    }

    return itemList;
  }

  /**
   * Insert package contents.
   *
   * @param packageBean        the package bean
   * @param headerInformation  the header information
   * @param username           the username
   * @param centerId           the center id
   * @param packageItemDetails the package item details
   * @param packageContents the package contents
   * @throws ParseException the ParseException
   */
  @SuppressWarnings("unchecked")
  private void insertPackageContents(BasicDynaBean packageBean, BasicDynaBean headerInformation,
      String username, Integer centerId, Map<String, Object> packageItemDetails,
      List<BasicDynaBean> packageContents, List<BasicDynaBean> chargesList) throws ParseException {
    if (packageContents.isEmpty()) {
      return;
    }

    orderService.insertPackageContents(packageBean, packageContents, headerInformation, username,
            centerId, chargesList, packageItemDetails);

    
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    Boolean isInsurance = (Boolean) bill.get("is_tpa");

    BasicDynaBean masterCharge = getMasterChargesBean((Object) orderBean.get(PACKAGE_ID), bedType,
        ratePlanId, centerId);

    boolean condApplicable;
    if (masterCharge.getDynaClass().getDynaProperty("conduction_applicable") != null) {
      condApplicable = (Boolean) masterCharge.get("conduction_applicable");
    } else {
      condApplicable = false;
    }

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BigDecimal quantity = BigDecimal.ONE;
      if (null != orderBean.getDynaClass().getDynaProperty("quantity")
          && !((orderBean.get("quantity")).equals(""))) {
        quantity = (BigDecimal) orderBean.get("quantity");
      }
      Map<String, Object> otherParams = new HashMap<>();
      otherParams.put("item_excluded_from_doctor",
          orderItemDetails.get("item_excluded_from_doctor"));
      otherParams.put("item_excluded_from_doctor_remarks",
          orderItemDetails.get("item_excluded_from_doctor_remarks"));
      otherParams.put("bed_type", bedType);
      otherParams.put("org_id", ratePlanId);
      otherParams.put("exclude_invite", true);
      otherParams.put("insert_charges", true);
      otherParams.put("consultation_type_id",
          orderItemDetails.get("consultation_type_id"));
      otherParams.put("package_contents", orderItemDetails.get("package_contents"));
      otherParams.put("submission_batch_type", orderItemDetails.get("submission_batch_type"));
      otherParams.put("code_type", orderItemDetails.get("code_type"));
      otherParams.put("act_rate_plan_item_code", orderItemDetails.get("ct_code"));
      otherParams.put("is_customized", orderItemDetails.get("is_customized"));
      otherParams.put("visit_type", (String) headerInformation.get("visit_type"));
      masterCharge.set("package_name", orderItemDetails.get("package_name"));
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId, otherParams);
      int prescId = (Integer) orderBean.get(PRESCRIPTION_ID);
      String presDrId = (String) orderBean.get(DOCTOR_ID);
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId,
            new Timestamp(postedDate.getTime()), bedType);
      } else {
        Timestamp postedDate = (Timestamp) orderBean.get(PRESCRIBED_DATE);
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate);
      }
    }

    return chargesList;
  }

  @Override
  public BigDecimal getCashCharge(Map<String, Object> paramMap) {
    return null;
  }

  /**
   * Marks patient package as discontinued while updating the remark.
   *
   * @param patientPackageId the patient package id
   * @param discontinueRemark the discontinue remark
   */
  public void discontinuePackage(Integer patientPackageId, String discontinueRemark) {
    Map<String, Object> key = new HashMap<>();
    key.put("pat_package_id", patientPackageId);
    BasicDynaBean patientPackageDetailsBean = this.patientPackagesRepository
        .findByKey(key);
    Map<String, Object> updatedKey = new HashMap<>();
    patientPackageDetailsBean.set("is_discontinued", true);
    patientPackageDetailsBean.set("discontinue_remark", discontinueRemark);
    this.patientPackagesRepository.update(patientPackageDetailsBean, key);
  }
}
