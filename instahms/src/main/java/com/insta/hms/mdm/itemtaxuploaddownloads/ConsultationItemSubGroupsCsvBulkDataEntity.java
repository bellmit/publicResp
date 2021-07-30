package com.insta.hms.mdm.itemtaxuploaddownloads;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

@Component
public class ConsultationItemSubGroupsCsvBulkDataEntity extends CsVBulkDataEntity {

  public ConsultationItemSubGroupsCsvBulkDataEntity() {
    super(KEYS, FIELDS, null, MASTERS);
  }

  private static final String[] KEYS = new String[] { "consultation_type_id" };

  private static final String[] FIELDS = new String[] { "item_group_id", "item_subgroup_id" };

  private static final BulkDataMasterEntity[] MASTERS = new BulkDataMasterEntity[] {
      new BulkDataMasterEntity("item_subgroup_id", "consultation_item_sub_groups",
          "item_subgroup_id", "item_subgroup_id"),
      new BulkDataMasterEntity("item_subgroup_id", "item_sub_groups", "item_subgroup_id",
          "item_subgroup_name"),
      new BulkDataMasterEntity("item_group_id", "item_groups", "item_group_id",
          "item_group_name") };
}
