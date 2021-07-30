package com.insta.hms.core.clinical.order.department;

import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class DepartmentOrderItemRepository extends OrderItemRepository {

  public DepartmentOrderItemRepository() {
    super("department");
  }

  private static final String GET_DEPARTMENT_DETAIL = " SELECT distinct 'Department' as type,"
      + " d.dept_id AS id, d.dept_name AS name, null as code, d.dept_name AS department, "
      + "  -1 as subGrpId, -1 as groupid,'N' as prior_auth_required, "
      + "  0 AS insurance_category_id ,false as conduction_applicable,"
      + " false as conducting_doc_mandatory, false as results_entry_applicable,'N' as "
      + " tooth_num_required, false as multi_visit_package, -1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info,'' as additional_info_reqts " + "  FROM department d ";

  /**
   * get Department Details.
   * @param entityIdList the entityIdList
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getDepartmentDetails(List<Object> entityIdList) {
    List<Object> otherParams = new ArrayList<Object>();
    return super.getItemDetails(GET_DEPARTMENT_DETAIL, entityIdList, "d.dept_id", otherParams);
  }

  @Override
  public String getPackageRefQuery() {
    return null;
  }
  
  @Override
  public String getOperationRefQuery() {
    return null;
  }

  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return Collections.emptyList();
  }

}
