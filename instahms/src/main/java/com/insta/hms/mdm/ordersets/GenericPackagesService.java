package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.ui.ModelMap;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericPackagesService extends MasterDetailsService {

  // Context = 'O' for Order Sets, 'P' for Packages.
  private String context;

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

  /**
   * Meta data.
   *
   * @return the map
   */
  public Map<String, Object> metaData() throws Exception {
    Map<String, Object> info = new HashMap<>();
    info.put("service_groups", ConversionUtils.listBeanToListMap(serviceGroupService.lookup(true)));
    info.put("service_sub_groups",
        ConversionUtils.listBeanToListMap(serviceSubGroupService.lookup(true)));
    info.put("consultation_types", ConversionUtils.listBeanToListMap(getConsultationTypes()));
    info.put("hospital_centers_list",
        ConversionUtils.listBeanToListMap(centerService.getCentersList()));
    info.put("departments_list", ConversionUtils.listBeanToListMap(departmentService
        .listAll(Arrays.asList("dept_id", "dept_name"), "status", "A", "dept_name")));
    info.put("charge_heads", ConversionUtils.listBeanToListMap(chargeHeadService.lookup(false)));
    return info;
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

  public GenericPackagesService(MasterRepository<?> repo, MasterValidator validator, String context,
      MasterRepository<?>... detailRepos) {
    super(repo, validator, detailRepos);
    this.context = context;
  }

  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qb = super.getSearchQueryAssembler(params, listingParams);
    qb.addFilter(SearchQueryAssembler.STRING, "type", "=", context);
    return qb;
  }

  @Override
  public Map<String, Map<String, Map<String, BasicDynaBean>>> toBeansMap(ModelMap requestBody,
      BasicDynaBean parentBean) {
    parentBean.set("type", context);
    return super.toBeansMap(requestBody, parentBean);
  }

  public List<BasicDynaBean> getPackageComponents(Integer packageId) {
    return ((PackagesRepository) this.getRepository()).getPackageComponents(packageId);
  }

  public BasicDynaBean getPackageContentDetail(Integer packageId, Integer packageContentId) {
    return ((PackagesRepository) this.getRepository()).getPackageContentDetail(packageId,
        packageContentId);
  }

  public Map<String, Object> advanceList(InstaLinkedMultiValueMap<String, Object> params)
      throws ParseException {
    return ((PackagesRepository) getRepository()).getPackages(params, this.context);
  }

}
