package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderSetsService extends GenericPackagesService {

  @LazyAutowired
  private ServiceGroupService serviceGroupService;

  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private CenterService centerService;

  @LazyAutowired
  private DepartmentService departmentService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadService;

  public OrderSetsService(PackagesRepository repo, OrderSetsValidator validator,
                          PackageContentsRepository d1, CenterPackageApplicabilityRepository d2,
                          DeptPackageApplicabilityRepository d3,
                          TpaPackageApplicabilityRepository d4) {
    super(repo, validator, "O", d1, d2, d3, d4);
  }

  /**
   * Gets the consultation types.
   *
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes() {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();

    Integer centerId = (Integer) sessionAttributes.get("centerId");
    Map<String, Object> params = new HashMap<>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);
    String healthAuthority = (String) centerBean.get("health_authority");
    healthAuthority = healthAuthority == null ? "" : healthAuthority;

    return consultationTypesService.getConsultationTypes("o", "i", "ORG0001", healthAuthority);
  }

  /**
   * Gets the order sets for prescription.
   *
   * @param visitType the visit type
   * @param gender the gender
   * @param centerId the center id
   * @param deptId the dept id
   * @param fromDate the from date
   * @param toDate the to date
   * @param searchQuery the search query
   * @return the order sets for prescription
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getOrderSetsForPrescription(String visitType, String gender,
      Integer centerId, String deptId, Date fromDate, Date toDate, String searchQuery) {
    return ConversionUtils.listBeanToListMap(
        ((PackagesRepository) getRepository()).getOrdersetsForPrescription(visitType, gender,
            centerId, deptId, fromDate, toDate, searchQuery));
  }

}
