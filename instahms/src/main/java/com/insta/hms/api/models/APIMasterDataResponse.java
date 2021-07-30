package com.insta.hms.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.insta.hms.core.masterdata.MasterdataService.ReturnCode;

@JsonRootName(value = "MasterDataResponse")
public class APIMasterDataResponse {

	@JsonProperty(value = "hospital_departments")
	@JacksonXmlElementWrapper(localName = "hospital_departments")
	@JacksonXmlProperty(localName = "Department")
	public List<Attributes> department_attr_list;

	@JacksonXmlElementWrapper(localName = "hospital_center_masters")
	@JsonProperty(value = "hospital_center_masters")
	@JacksonXmlProperty(localName = "HospitalCenterMaster")
	public List<Attributes> hospital_center_master_attr_list;

	@JacksonXmlElementWrapper(localName = "hospital_doctors")
	@JsonProperty(value = "hospital_doctors")
	@JacksonXmlProperty(localName = "Doctor")
	public List<Attributes> hospital_doctors_attr_list;

	public String return_message;

	public String return_code;

	@JsonIgnore
	Map<String, Object> masterDataMap;

	public APIMasterDataResponse() {
	}

	public APIMasterDataResponse(Map<String, Object> masterDataResponse) {
		masterDataMap = masterDataResponse;
		constructMasterDataMap();
	}

	private void constructMasterDataMap() {
		if (MapUtils.isNotEmpty(masterDataMap)) {
			department_attr_list = new ArrayList<>();
			department_attr_list.addAll(
					constructAttributesMap((List<Map<String, Object>>) masterDataMap.get("hospital_departments")));

			hospital_doctors_attr_list = new ArrayList<>();
			hospital_doctors_attr_list
					.addAll(constructAttributesMap((List<Map<String, Object>>) masterDataMap.get("hospital_doctors")));

			hospital_center_master_attr_list = new ArrayList<>();
			hospital_center_master_attr_list.addAll(
					constructAttributesMap((List<Map<String, Object>>) masterDataMap.get("hospital_center_master")));

			ReturnCode returnCode = (ReturnCode) masterDataMap.get("ReturnCode");
			if (returnCode != null) {
				return_code = returnCode.getReturnCode();
				return_message = returnCode.getReturnMessage();
			}
		}
	}

	private class Attributes {
		public Map<String, Object> properties;
	}

	public List<Attributes> constructAttributesMap(List<Map<String, Object>> propertylist) {
		List<Attributes> attributeList = new ArrayList<Attributes>();
		if (CollectionUtils.isNotEmpty(propertylist)) {
			for (Map<String, Object> attributes : propertylist) {
				if (attributes != null) {
					Attributes attributesObj = this.new Attributes();
					attributesObj.properties = attributes;
					attributeList.add(attributesObj);
				}
			}
		}
		return attributeList;
	}

}