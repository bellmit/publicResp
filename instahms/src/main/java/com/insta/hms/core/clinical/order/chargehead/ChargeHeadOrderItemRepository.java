package com.insta.hms.core.clinical.order.chargehead;

import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ChargeHeadOrderItemRepository extends OrderItemRepository {

  public ChargeHeadOrderItemRepository() {
    super("chargehead_constants");
  }

  private static final String DIRECT_CHARGES_QUERY = " SELECT 'Direct Charge' AS type, "
      + "  chargehead_id as id, chargehead_name as name, "
      + "  null AS code, '' AS department, s.service_sub_group_id as subGrpId, "
      + "  s.service_group_id as groupid,'N' as prior_auth_required, "
      + "  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, "
      + "  false as results_entry_applicable,'N' as tooth_num_required, "
      + "  false as multi_visit_package, -1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info, '' as additional_info_reqts "
      + "  FROM chargehead_constants " 
      + "  JOIN service_sub_groups s using(service_sub_group_id)";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(DIRECT_CHARGES_QUERY, entityIdList, "chargehead_id", null);
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
