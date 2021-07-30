package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentQABO {

	EquipmentTestConductedDAO eqConductedDAO = new EquipmentTestConductedDAO();

	public PagedList getEquipmentsDetails(Map filterParams)throws SQLException ,ParseException{
		PagedList pagedList = eqConductedDAO.listAll(filterParams, ConversionUtils.getListingParameter(filterParams));
		List<BasicDynaBean> dtoList = pagedList.getDtoList();
		Map eqMap = null;
		List equipemtDetails = null;
		List newDtoList = new ArrayList();

		for( BasicDynaBean equipmentBean : dtoList ){
			eqMap = new HashMap();

			equipemtDetails = eqConductedDAO.findAllByKey("equipment_id", equipmentBean.get("eq_id"));
			eqMap.put("equipmentName", equipmentBean.get("equipment_name"));
			eqMap.put("equipmentId", equipmentBean.get("eq_id"));
			eqMap.put("equipmentDetails", equipemtDetails);

			newDtoList.add(eqMap);
		}

		pagedList.setDtoList(newDtoList);

		return pagedList;
	}
}
