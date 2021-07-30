<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="enableDistrict" value='<%=RegistrationPreferencesDAO.getRegistrationPreferences().getEnableDistrict() %>' />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit City Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/cities/list.htm?sortOrder=city_name&sortReverse=false&citystatus=A";
		}

		var chkCityList = ${ifn:convertListToJson(cityList)};
		var hiddenCityId = '${bean.city_id}';

		function checkduplicate(){
			var newCityName = trimAll(document.forms[0].city_name.value);
			for(var i=0;i<chkCityList.length;i++){
				item = chkCityList[i];
				if(hiddenCityId!=item.city_id){
				   var actualCityName = item.city_name;
				    if (newCityName.toLowerCase() == actualCityName.toLowerCase()) {
				    	alert(document.forms[0].city_name.value+" already exists pls enter other name");
				    	document.forms[0].city_name.value='';
				    	document.forms[0].city_name.focus();
				    	return false;
				    }
			     }
			}
			return true;
      }
		
		function restrictInactiveCity() {
			if (!checkduplicate()) return false;
			var cityId = document.forms[0].city_id.value;
			var cityName =  document.forms[0].city_name.value;
			var status = document.forms[0].status.value;
			var areaList = ${ifn:convertListToJson(areaList)};
			
			for (var i=0;i<areaList.length;i++) {
				if(status == 'I'){
					if(areaList[i].city_id == cityId && areaList[i].status == 'A') {
						alert("Active areas are mapped with this city "+cityName+". Hence, it can not be marked as Inactive.");
							return false;
					}
				}
			}
			return true;
		}
		
		function loadDistricts() {
			var stateId = document.forms[0].state_id.value;
			var districtObj = document.forms[0].district_id;
			if(districtObj != null && districtObj != undefined) {
				var districtListJSON = ${ifn:convertListToJson(districtList)};
				var filteredDistricts = filterList(districtListJSON, "state_id", stateId);
				loadSelectBox(districtObj, filteredDistricts, "district_name", "district_id", '-- Select --');
			}
		}
	</script>

</head>
<body>

<c:set var="actionUrl" value="${cpath}/master/cities/update.htm?city_id=${bean.city_id}"/>
<form action="${actionUrl}" method="POST">
		<input type="hidden" name="city_id" value="${bean.city_id}"/>

	<div class="pageHeader">Edit City</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">State:</td>
				<td>
					 <insta:selectdb name="state_id" value="${bean.state_id}" table="state_master" valuecol="state_id" 
					 displaycol="state_name" orderby="state_name" onchange="loadDistricts();"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<c:if test="${enableDistrict=='Y'}">
				<tr>
					<td class="formlabel">District:</td>
					<td>
						 <insta:selectdb name="district_id" value="${bean.district_id}" table="district_master" 
						 	valuecol="district_id" displaycol="district_name" orderby="district_name" 
						 	dummyvalue="-- Select --" dummyvalueId="" filtercol="state_id,status" filtervalue="${bean.state_id },A" />
					</td>
				</tr>
			</c:if>
			<tr>
				<td class="formlabel">City:</td>
				<td>
					<input type="text" name="city_name" value="${bean.city_name}" onblur="capWords(city_name);checkduplicate();" class="required validate-length" length="50" title="Name is required and max length of name can be 50" />
				</td>
			</tr>

			<tr>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

		</table>

	</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return restrictInactiveCity();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/cities/add.htm'">Add</a>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">City List</a></td>
		</tr>
	</table>

</form>

</body>
</html>
