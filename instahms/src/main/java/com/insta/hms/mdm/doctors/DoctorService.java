package com.insta.hms.mdm.doctors;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService extends MasterService {

  @LazyAutowired private GenericPreferencesService genericPreferencesService;

  private DoctorRepository doctorRepository = (DoctorRepository) getRepository();

  public static final String REGISTRATION_PENDING = "REGISTRATION_PENDING";

  public DoctorService(DoctorRepository doctorRepository, DoctorValidator doctorValidator) {
    super(doctorRepository, doctorValidator);
  }

  private static final String GET_DOCTOR_IMAGE = "select encode(photo, 'base64') as photo from "
      + " doctor_images where doctor_id=? ";

  /**
   * Doctors Filter based on centerId.
   *
   * @param centerId Integer
   * @return List of Map objects
   */
  public List<Map<String, Object>> getDoctors(Integer centerId) {
    BasicDynaBean genBean = genericPreferencesService.getPreferences();
    int centersIncDefault = (Integer) genBean.get("max_centers_inc_default");
    return doctorRepository.getDoctors(centerId, centersIncDefault, null);
  }

  public Map<String, Object> getDoctorDetails(String doctorId) {
    BasicDynaBean doctorBean = doctorRepository.findByKey("doctor_id", doctorId);
    return (doctorBean != null) ? doctorBean.getMap() : null;
  }

  public BasicDynaBean getDoctorCenter(String doctorId, int centerId) {
    return doctorRepository.getDoctorCenter(doctorId, centerId);
  }

  /**
   * List of doctors centerIds based on doctorId.
   *
   * @param doctorId String
   * @return List of CenterIds
   */
  public List<Integer> getDoctorCentersList(String doctorId) {
    List<BasicDynaBean> beanList = doctorRepository.getDoctorCenterList(doctorId);
    List<Integer> centerList = new ArrayList<>();
    for (BasicDynaBean bean : beanList) {
      centerList.add((Integer) bean.get("center_id"));
    }
    return centerList;
  }

  public Integer getDoctorOverbookLimit(String doctorId) {
    return doctorRepository.getDoctorOverbookLimit(doctorId);
  }

  /**
   * Update the Doctor status on Practo for a center.
   *
   * @param doctorId String
   * @param centerId int
   * @param status String
   */
  public void updateDoctorStatusOnPracto(String doctorId, int centerId, String status) {
    doctorRepository.updateDoctorStatusOnPracto(doctorId, centerId, status);
  }

  @SuppressWarnings("unused")
  @Override
  public List<BasicDynaBean> autocomplete(String matchField, String likeValue, boolean activeOnly,
      Map<String, String[]> parameters) {
    // check for contains
    boolean contains = false;
    String paramContains = null;
    if (null != parameters.get("contains")) {
      paramContains = parameters.get("contains")[0].trim();
    }
    if (null != paramContains && paramContains.equalsIgnoreCase("true")) {
      contains = true;
    }

    String lookupQuery = null;
    if (parameters.get("is_all_doctors") != null
        && Boolean.valueOf(parameters.get("is_all_doctors")[0])) {
      lookupQuery = doctorRepository.getMasterLookupQuery();
    } else {
      lookupQuery = doctorRepository.getLookupQuery();
    }
    
    String deftFilter = null;
    if (null != parameters.get("deptFilter")) {
      deftFilter = parameters.get("deptFilter")[0].trim();
      lookupQuery = lookupQuery.replace("#deptFilter#",
          deftFilter.equalsIgnoreCase("Laboratory") ? " AND d.dept_id = 'DEP_LAB' "
              : " AND d.dept_id = 'DEP_RAD' ");
    } else {
      lookupQuery = lookupQuery.replace("#deptFilter#", "");
    }
    
    SearchQueryAssembler qb = null;
    // qb = new SearchQueryAssembler(lookupQuery, null, null,
    // listingParams);
    qb = getLookupQueryAssembler(lookupQuery, parameters);
    addFilterForLookUp(qb, likeValue, matchField, contains, parameters);
    if (null != doctorRepository.getStatusField() && activeOnly) {
      qb.addFilter(QueryAssembler.STRING, doctorRepository.getStatusField(), "=",
          doctorRepository.getActiveStatus());
    }
    qb.build();
    PagedList pagedList = qb.getDynaPagedList();
    List<BasicDynaBean> resultList = pagedList.getDtoList();
    return resultList;
  }

  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));
    // Autocomplete is for both center_id and dept_id from the parameters
    String deptId =
        (null != parameters && parameters.containsKey("dept_id"))
            ? parameters.get("dept_id")[0]
            : null;
    if (deptId != null) {
      qb.addFilter(QueryBuilder.STRING, "dept_id", "=", deptId);
    }
    
    // Autocomplete support for surgeon doctors.
    String otDoctorFlag = (null != parameters && parameters.containsKey("ot_doctor_flag"))
        ? parameters.get("ot_doctor_flag")[0]
        : null;
    if (otDoctorFlag != null && !"".equals(otDoctorFlag)) {
      qb.addFilter(QueryBuilder.STRING, "ot_doctor_flag", "=", otDoctorFlag);
    }
    
    // Ignoring center specific filter
    if (parameters.get("is_all_center") != null
        && Boolean.valueOf(parameters.get("is_all_center")[0])) {
      return qb;
    }
    String[] centerIds =
        (null != parameters && parameters.containsKey("center_id"))
            ? parameters.get("center_id")
            : null;
    List<Integer> valueList = new ArrayList<Integer>();
    valueList.add(0);
    if (centerIds != null) {
      for (String centerId : centerIds) {
        valueList.add(Integer.parseInt(centerId));
      }
    }
    qb.addFilter(QueryBuilder.INTEGER, "center_id", "IN", valueList);

    return qb;
  }

  @Override
  public void addFilterForLookUp(
      SearchQueryAssembler qb,
      String likeValue,
      String matchField,
      boolean contains,
      Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      ArrayList<Object> types = new ArrayList<Object>();
      types.add(QueryAssembler.STRING);
      types.add(QueryAssembler.STRING);
      ArrayList<String> values = new ArrayList<String>();
      values.add(filterText);
      values.add(filterText);
      qb.appendExpression(
          " ( " + matchField + " ILIKE ? " + "OR  doctor_license_number ILIKE ? ) ", types, values);
    }
  }

  /**
   * returns Doctors Charges.
   *
   * @param doctorId String
   * @param orgId String
   * @param bedType String
   * @return BasicDynaBean
   */
  public BasicDynaBean getDoctorCharges(String doctorId, String orgId, String bedType) {
    return doctorRepository.getDoctorCharges(doctorId, orgId, bedType);
  }

  /**
   * returns Doctors Charges.
   *
   * @param doctorId String
   * @param orgId    String
   * @return BasicDynaBean
   */
  public List<BasicDynaBean> getAllDoctorCharges(String doctorId, String orgId) {
    return doctorRepository.getAllDoctorCharges(doctorId, orgId);
  }

  public BasicDynaBean getConsultationCharges(int consultationId, String bedType, String ratePlan) {
    return doctorRepository.getConsultationCharges(consultationId, bedType, ratePlan);
  }

  public String getCodeDesc(String code, String codeType) {
    return doctorRepository.getCodeDesc(code, codeType);
  }

  public BasicDynaBean getDoctorPaymentBean(String doctorId) {
    return doctorRepository.getDoctorPaymentBean(doctorId);
  }

  /**
   * Get Doctors prescriptions.
   *
   * @param bedType String
   * @param orgId String
   * @param patientType String
   * @param insPlanId Integer
   * @param searchQuery String
   * @param itemLimit Integer
   * @return List of Map of Object
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getDoctorsForPrescription(
      String bedType,
      String orgId,
      String patientType,
      Integer insPlanId,
      String searchQuery,
      Integer itemLimit) {
    return ConversionUtils.listBeanToListMap(
        ((DoctorRepository) getRepository())
            .getDoctorsForPrescription(
                bedType, orgId, patientType, insPlanId, searchQuery, itemLimit));
  }

  public List<BasicDynaBean> getResourceBelongingCenter(String doctorId) {
    return doctorRepository.getResourceBelongingCenter(doctorId);
  }

  public List<BasicDynaBean> listAll(
      List<String> columns, String filterBy, Object filterValue, String sortColumn) {
    return getRepository().listAll(columns, filterBy, filterValue, sortColumn);
  }

  public BasicDynaBean getDoctorByConsId(Object consId) {
    return doctorRepository.getDoctorByConsId(consId);
  }

  /**
   * Gets the doctor by id.
   *
   * @param doctorId String
   * @return the doctor by id
   */
  public BasicDynaBean getDoctorById(String doctorId) {
    return doctorRepository.findByPk(Collections.singletonMap("doctor_id", doctorId));
  }
  
  /**
   * Gets the OT doctor charges bean.
   *
   * @param doctorId the doctor id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the OT doctor charges bean
   */
  public BasicDynaBean getOtDoctorChargesBean(String doctorId, String bedType, String orgId) {
    return doctorRepository.getOtDoctorChargesBean(doctorId, bedType, orgId);
  }
  
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getAllDoctorsData(boolean sendOnlyActiveData,
      boolean scheduleableAll) {
    return ConversionUtils.listBeanToListMap(doctorRepository.getAllDoctors(sendOnlyActiveData,
        scheduleableAll));
  }

  /**
   * Gets the doctor users.
   *
   * @param centerId the center id
   * @return the doctor users
   */
  public List<Map<String, Object>> getDoctorUsers(Integer centerId) {
    BasicDynaBean genBean = genericPreferencesService.getPreferences();
    int centersIncDefault = (Integer) genBean.get("max_centers_inc_default");
    return ConversionUtils.listBeanToListMap(doctorRepository
        .getDoctorUserNames(centerId,centersIncDefault));
  }
  
  public List<BasicDynaBean> getDepartmentDoctors(String departmentId) {
    return doctorRepository.getDepartmentDoctors(departmentId);
  }
  
  /**
   * Get Doctor and ReferalDoctor list.
   * 
   * @param searchString the search string
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getDoctorAndReferalDoctor(String searchString) {
    return ConversionUtils
        .listBeanToListMap(doctorRepository.getDoctorAndReferalDoctor(searchString));
  }

  /**
   * Get doctor photo by doctor id.
   * @param doctorId doctor id
   * @return return base64 encoded string
   */
  public String getDoctorImageById(String doctorId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_DOCTOR_IMAGE, new String[] {doctorId});
    return bean != null ? (String) bean.get("photo") : "";
  }
}


