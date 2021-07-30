package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.AbstractViewObjectMapper;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class ItemSubGroupPackageVOMapperService
    extends AbstractViewObjectMapper<PackageItemSubGroupsModel, PackageItemSubGroupsVO> {

  @Override
  public PackageItemSubGroupsVO convertModelToViewObject(PackageItemSubGroupsModel modelObj)
      throws SQLException {
    GenericDAO itemSubGroupsDAO = new GenericDAO("item_sub_groups");
    BasicDynaBean itemSubGroupObj =
        itemSubGroupsDAO.findByKey("item_subgroup_id", modelObj.getItemSubgroupId());
    if (itemSubGroupObj != null) {
      PackageItemSubGroupsVO packageItemSubGroupsVO = new PackageItemSubGroupsVO();
      packageItemSubGroupsVO.setTaxSubGroupId(modelObj.getItemSubgroupId());
      packageItemSubGroupsVO.setTaxGroupId((Integer) itemSubGroupObj.get("item_group_id"));
      return packageItemSubGroupsVO;
    }
    return null;
  }

  @Override
  public PackageItemSubGroupsModel convertViewObjectToModel(PackageItemSubGroupsVO viewObj) {
    PackageItemSubGroupsModel packageItemSubGroupsModel = new PackageItemSubGroupsModel();
    packageItemSubGroupsModel.setItemSubgroupId(viewObj.getTaxSubGroupId());
    return packageItemSubGroupsModel;
  }

}
