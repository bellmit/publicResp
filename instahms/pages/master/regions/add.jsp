<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Region Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkRegionName = ${ifn:convertListToJson(referenceData)};

	function validate() {

		var regionName = document.getElementById('region_name').value.trim();
		if (empty(regionName)) {
			alert('Please enter region name.');
			document.getElementById('region_name').focus();
			return false;
		}

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newRegionName = trimAll(document.regionMaster.region_name.value);

			for(var i=0;i<chkRegionName.length;i++){
				item = chkRegionName[i].map;
				if (newRegionName == item.region_name){
					alert(document.regionMaster.region_name.value+" already exists pls enter other name...");
			    	document.regionMaster.region_name.value='';
			    	document.regionMaster.region_name.focus();
			    	return false;
			}
		}
		return true;
	}
</script>

</head>
			
<c:set var="actionUrl" value="${cpath}/master/regions/create.htm"/>

<form action="${actionUrl}" method="POST" name="regionMaster">
	<input type="hidden" name="region_id" id="region_id" value="${bean.region_id}"/>

	<h1>Add Region </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Region Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Region Name:</td>
				<td>
					<input type="text" name="region_name" id="region_name" value="<c:out value="${bean.region_name}"/>" maxlength="100" title="Region Name is mandatory."><span class="star">*</span>
				</td>
				<td/>
				<td/>
				<td/>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td/>
				<td/>
				<td/>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		<insta:screenlink addPipe="true" screenId="mas_region" label="Region List"  extraParam="?sortOrder=region_id&sortReverse=false&status=A"/>
	</div>
</form>

</body>
</html>
