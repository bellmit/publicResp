package com.insta.hms.core.clinical.order.master;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.URLRoute;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.packages.PackagesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns the list of orderable items based of params passed. Searching is based on item_name,
 * item_code, item_id If using item_id, pass type as well because two items can have same item_id.
 * 
 * @author ritolia
 *
 */

@RestController
@RequestMapping(URLRoute.ORDER_URL)
public class OrderOrderableItemController extends BaseRestController {

  @Autowired
  private OrderService orderService;

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private OrderSetsService orderSetsService;

  @LazyAutowired
  private PackagesService packagesService;

  @LazyAutowired
  MessageUtil messageUtil;

  /**
   * get orderable item.
   * 
   * @param params the params
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getorderableitem")
  public Map<String, Object> getOrderableItem(@RequestParam MultiValueMap<String, String> params) {

    Map<String, Object> getOrderableItemData = new HashMap<>();
    List<BasicDynaBean> orderableItems = orderService
        .getOrderableItem(new InstaLinkedMultiValueMap<String, String>(params));
    getOrderableItemData.put("orderable_items", ConversionUtils.listBeanToListMap(orderableItems));
    return getOrderableItemData;

  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/getbasicorderinfo")
  public Map<String, Object> getBasicOrderInfo(
      @RequestParam(defaultValue = "o", value = "visit_type") String visitType) {
    return orderService.getBasicOrderInfo(visitType);
  }

  /**
   * get consultation types.
   * 
   * @param orgId        the orgId
   * @param visitType    the visitType
   * @param otDocCharges the otDocCharges
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getconsultationtypes")
  public Map<String, Object> getConsultationTypes(
      @RequestParam(defaultValue = "ORG0001") List<String> orgId,
      @RequestParam(defaultValue = "o") String visitType,
      @RequestParam(defaultValue = "N") String otDocCharges) {

    Map<String, Object> map = new HashMap<>();
    map.put("consultation_types",
        orderService.getConsultationTypes(orgId, visitType, otDocCharges));
    return map;
  }

  /**
   * get consultation types.
   * 
   * @param orgIds             the orgIds
   * @param visitType          the visitType
   * @param otDocCharges       the otDocCharges
   * @param practitionerTypeId the practitionerTypeId
   * @param doctorId           the doctorId
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getpractitionerconsultationtypes")
  public Map<String, Object> getConsultationTypes(
      @RequestParam(defaultValue = "ORG0001") List<String> orgIds,
      @RequestParam(defaultValue = "o") String visitType,
      @RequestParam(defaultValue = "N") String otDocCharges,
      @RequestParam(required = false, value = "practitioner_type_id") Integer practitionerTypeId,
      @RequestParam(required = false, value = "doctor_id") String doctorId) {

    if ((practitionerTypeId == null || practitionerTypeId.equals(""))
        && (doctorId == null || doctorId.equals(""))) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    if ((practitionerTypeId == null || practitionerTypeId.equals(""))
        && !doctorId.equals("Doctor")) {
      Map<String, String> filterMap = new HashMap<>();
      filterMap.put("doctor_id", doctorId);
      BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
      if (doctorBean == null) {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
      }
      if (doctorBean.get("practitioner_id") != null) {
        practitionerTypeId = (int) doctorBean.get("practitioner_id");
      }
    }
    List<Map> consultationTypes = new ArrayList<>();
    if (practitionerTypeId != null && !("Doctor").equals(doctorId)) {
      consultationTypes = orderService.getConsultationTypes(orgIds, visitType, otDocCharges,
          practitionerTypeId);
    }
    if (consultationTypes.isEmpty()) {
      consultationTypes = orderService.getConsultationTypes(orgIds, visitType, otDocCharges);
    }
    Map<String, Object> map = new HashMap<>();
    map.put("consultation_types", consultationTypes);
    return map;
  }

  /**
   * get patient package details.
   *
   * @param mrNo the mrNo
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/patientpackagedetails")
  public Map<String, Object> getOrderedPackages(@RequestParam("mr_no") String mrNo) {
    Map<String, Object> orderedPackagesMap = new HashMap<>();
    List<Map<String, Object>> orderedPackagesList = new ArrayList<>();
    List<BasicDynaBean> componentQuantityDetails = orderService.getOrderedPackageItems(mrNo);
    Map<String, List<BasicDynaBean>> patientPackageDetails = orderService
            .getPatientPackageDetails(mrNo);
    orderedPackagesMap.put("componentQuantityDetails",
            ConversionUtils.copyListDynaBeansToMap(componentQuantityDetails));
    orderedPackagesMap.put("patientPackages",
            ConversionUtils.listBeanToListMap(patientPackageDetails.get("patientPackages")));
    orderedPackagesMap.put("packageComponents",
            ConversionUtils.listBeanToListMap(patientPackageDetails.get("packageComponents")));
    return orderedPackagesMap;
  }

  /**
   * get package components.
   * 
   * @param packageId the packageId
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getpackagecontents")
  public Map<String, Object> getPackageComponents(@RequestParam("packageId") Integer packageId,
      @RequestParam(required = false, value = "org_id") String orgId,
      @RequestParam(required = false, value = "bed_type") String bedType) {
    List<BasicDynaBean> packageContentDetails = orderService.getPackageComponentDetails(packageId,
          orgId, bedType);
    BasicDynaBean packageDetails = orderService.getPackageDetails(packageId, orgId, bedType);
    Map<String, Object> packContentsMap = new HashMap<>();
    if (CollectionUtils.isEmpty(packageContentDetails)) {
      packContentsMap
          .put("error", messageUtil.getMessage("ui.error.package.rates.not.defined", new Object[] {
              bedType}));
    }
    if (null != packageDetails) {
      packContentsMap.put("packDetails", packageDetails.getMap());
    }
    packContentsMap.put("packComponentDetails",
        ConversionUtils.listBeanToListMap(packageContentDetails));
    return packContentsMap;
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/getcustompackageitemdetails")
  public Map<String, Object> getItemDetails(@RequestParam("itemType") String type,
      @RequestParam String itemId, @RequestParam String orgId,
      @RequestParam String bedType, @RequestParam(required = false) String chargeHead,
      @RequestParam(required = false, name = "package_content_id") Integer packageContentId,
      @RequestParam(required = false) Integer panelId) {
    return orderService.getItemDetails(type, itemId, orgId, bedType, chargeHead,
        packageContentId, panelId);
  }

  @IgnoreConfidentialFilters
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @GetMapping(value = "/getpackagecontentsforbulkappts")
  public Map<String, Object> getPackageComponentsForBulkAppts(
      @RequestParam("activityId") String activityId,
      @RequestParam("activityType") String activityType,
      @RequestParam(required = false, value = "mr_no") String mrNo,
      @RequestParam(required = false, value = "multi_visit_package") boolean multiVisitPackage) {
    return orderService.getpackagecontentsforbulkappts(activityId, activityType, mrNo,
        multiVisitPackage);
  }

  /**
   * get item charge estimate.
   * 
   * @param requestBody the requestBody
   * @return map
   * @throws ParseException the ParseException
   * @throws SQLException   the SQLException
   */
  @PostMapping(value = "/getitemchargeestimate")
  public Map<String, Object> getItemChargeEstimate(@RequestBody ModelMap requestBody)
      throws ParseException, SQLException {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return orderService.getItemChargeEstimate(requestBody);
  }

  /**
   * This Api returns package contents and operation as a separate item. Since Operation is not
   * saved as separate item in package. So the query unions it to get it. Unable to use
   * getpackagecontents api, as it doesn't give operation as a seprate line item. This api is used
   * for package popup.
   * 
   * @param packageId the packageId
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getorderpackagedetails")
  public Map<String, Object> getOrderPackageDetails(@RequestParam("packageId") Integer packageId) {
    Map<String, Object> map = new HashMap<>();
    map.put("package_details",
        ConversionUtils.listBeanToListMap(packagesService.getPackageComponentDetails(packageId)));
    return map;
  }

}
