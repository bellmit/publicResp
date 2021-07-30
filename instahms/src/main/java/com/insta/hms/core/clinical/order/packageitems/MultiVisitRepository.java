package com.insta.hms.core.clinical.order.packageitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * This Class returns the item Charge for MutiVisit Package.
 * 
 * @author ritolia
 *
 */
@Repository
public class MultiVisitRepository extends GenericRepository {

  public MultiVisitRepository() {
      super("packages");
  }

  /** The Constant ACTIVITY_QTY. */
  private static final String ACTIVITY_QTY = "activity_qty";

  /** The Constant PACKAGE_OBJECT_ID. */
  private static final String PACKAGE_OBJECT_ID = "pack_ob_id";

  /** The Constant GET_PACKAGE_COMPONENT_DETAIL. */

  private static final String GET_PACKAGE_COMPONENT_DETAIL = "SELECT activity_qty, "
      + " package_content_id as pack_ob_id FROM package_contents WHERE package_id = ?"
      + " AND activity_id= ? ";

  /** The Constant GET_PACKAGE_COMPONENT_DETAIL_DOCTOR. */
  private static final String GET_PACKAGE_COMPONENT_DETAIL_DOCTOR = "SELECT activity_qty, "
      + " package_content_id as pack_ob_id FROM package_contents WHERE package_id = ? "
      + " AND activity_id= ? AND consultation_type_id = ?";

  /** The Constant GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE. */
  private static final String GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE = " SELECT charge FROM "
      + " package_content_charges where org_id = ? AND bed_type = ? "
      + " AND package_content_id = ?";

  /**
   * Gets the multi visit package item charge.
   *
   * @param packageId the package id
   * @param itemBean  the item bean
   * @param itemId    the item id
   * @param bedType   the bed type
   * @param orgId     the rate plan id
   * @param itemType  the item type
   * @param qty       the quantity
   * @return the multi visit package item charge
   */
  public BigDecimal getMultiVisitPackageItemCharge(Integer packageId, BasicDynaBean itemBean,
      String itemId, String bedType, String orgId, String itemType, BigDecimal qty) {

    String activityId;
    BasicDynaBean resultBean;
    BigDecimal charge = BigDecimal.ZERO;
    Integer itemTotalQtyInt = null;
    Integer packObId = -1;

    BasicDynaBean componentDeatilBean = DatabaseHelper.queryToDynaBean(GET_PACKAGE_COMPONENT_DETAIL,
        new Object[] { packageId, itemId });

    if (componentDeatilBean != null) {
      itemTotalQtyInt = (Integer) componentDeatilBean.get(ACTIVITY_QTY);
      packObId = (Integer) componentDeatilBean.get(PACKAGE_OBJECT_ID);
    }

    if (("doctors").equals(itemType) && null != itemBean) {
      activityId = (String) itemBean.get("doctor_name");
      BasicDynaBean doctorComponentDetailBean = DatabaseHelper.queryToDynaBean(
          GET_PACKAGE_COMPONENT_DETAIL_DOCTOR,
          new Object[] { packageId, activityId, Integer.parseInt(itemId) });

      if (doctorComponentDetailBean != null) {
        packObId = (Integer) doctorComponentDetailBean.get(PACKAGE_OBJECT_ID);
        itemTotalQtyInt = (Integer) doctorComponentDetailBean.get(ACTIVITY_QTY);
      }

      if (doctorComponentDetailBean == null) {
        activityId = "Doctor";
        doctorComponentDetailBean = DatabaseHelper.queryToDynaBean(
            GET_PACKAGE_COMPONENT_DETAIL_DOCTOR,
            new Object[] { packageId, activityId, Integer.parseInt(itemId) });
        packObId = (Integer) doctorComponentDetailBean.get(PACKAGE_OBJECT_ID);
        itemTotalQtyInt = (Integer) doctorComponentDetailBean.get(ACTIVITY_QTY);
      }
    }

    BigDecimal itemTotalQtyNumeric = itemTotalQtyInt == null ? BigDecimal.ZERO
        : new BigDecimal(itemTotalQtyInt);
    resultBean = DatabaseHelper.queryToDynaBean(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE,
        new Object[] { orgId, bedType, packObId });

    if (resultBean != null) {
      charge = (BigDecimal) resultBean.get("charge");
    } else {
      resultBean = DatabaseHelper.queryToDynaBean(GET_MULTI_VISIT_PACKAGE_ITEM_CAHRGE,
          new Object[] { "ORG0001", "GENERAL", packObId });
      if (resultBean != null) {
        charge = new BigDecimal(String.valueOf(resultBean.get("charge")));
      }
    }

    charge = charge.compareTo(BigDecimal.ZERO) != 0
        ? charge.divide(itemTotalQtyNumeric, RoundingMode.CEILING).multiply(qty)
        : BigDecimal.ZERO;

    return charge;
  }

  private static final String GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS =
      "SELECT pp.mr_no,ppc.activity_id as item_id,ppc.package_id, SUM(ppcc.quantity) "
      + " as consumed_qty, pp.pat_package_id, ppcc.patient_package_content_id, "
      + " pp.deposit_balance "
      + " FROM patient_package_contents ppc JOIN patient_package_content_consumed ppcc "
      + " ON ppcc.patient_package_content_id = ppc.patient_package_content_id  "
      + " JOIN patient_packages pp ON pp.pat_package_id=ppc.patient_package_id "
      + " WHERE pp.mr_no = ? AND pp.status NOT IN('X','C') "
      + " GROUP by ppcc.patient_package_content_id, pp.mr_no,"
      + " ppc.activity_id,ppc.package_id,pp.pat_package_id";
  
  private static final String GET_MULTIVISIT_PACK_COMP_ORDERED_QUANTITY_DETAILS =
           " SELECT * FROM "
           + " (SELECT pmov.mr_no,item_id,ppr.package_id,sum(quantity) as consumed_qty, "
           + " ppr.pat_package_id,pc.package_content_id AS patient_package_content_id"
           + "  FROM patient_multivisit_orders_view pmov "
           + " JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
           + " JOIN package_contents pc ON pc.package_id = ppr.package_id "
           + " AND pc.activity_id=pmov.item_id"
           + " WHERE  pmov.mr_no = ? AND pmov.status != 'X' "
           + " GROUP BY pmov.mr_no,item_id,ppr.package_id,ppr.pat_package_id,"
           + " pc.package_content_id) as foo "
           + " JOIN patient_packages pp USING(pat_package_id) WHERE pp.status NOT IN('X','C')";

  /**
   * It returns the already ordered multiVisit Package items.
   *
   * @param mrNo      the mrNo
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOrderedPackageItems(String mrNo) {
    List<BasicDynaBean> orderedPackageItems = DatabaseHelper
        .queryToDynaList(GET_MULTIVISIT_PACKAGE_COMPONENT_ORDERED_QUANTITY_DETAILS,
        new Object[] { mrNo });
    /* Get the MVP Details of patients which have multi visit package 
    * partially consumed in 12.3 and upgraded to 12.4
    * Fall back to old method ,since migration has not done into 
    * new package transaction tables for mvp. 
    */
    if (CollectionUtils.isEmpty(orderedPackageItems)) {
      orderedPackageItems =  DatabaseHelper
          .queryToDynaList(GET_MULTIVISIT_PACK_COMP_ORDERED_QUANTITY_DETAILS,
          new Object[] { mrNo });
    }
    return orderedPackageItems;
  }

  private static final String GET_MULTIVISIT_PATIENT_PACKAGE_ORDERED_QUANTITY_DETAILS =
      "SELECT * FROM "
          + " (SELECT pmov.mr_no,item_id,sum(quantity) as consumed_qty, "
          + " ppr.package_id,ppr.pat_package_id "
          + " FROM patient_multivisit_orders_view pmov "
          + " LEFT JOIN package_prescribed ppr ON(ppr.prescription_id=pmov.package_ref) "
          + " AND pmov.status != 'X'"
          + " GROUP BY pmov.mr_no,item_id,ppr.package_id,ppr.pat_package_id) as foo"
          + " JOIN patient_packages pp USING(pat_package_id) WHERE pp.pat_package_id = ?";

  public List<BasicDynaBean> getOrderedPatientPackageItems(Integer patientPackageId) {
    return DatabaseHelper.queryToDynaList(GET_MULTIVISIT_PATIENT_PACKAGE_ORDERED_QUANTITY_DETAILS,
            new Object[] { patientPackageId });
  }

}
