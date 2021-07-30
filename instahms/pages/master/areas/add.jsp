<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Area - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkAreaName = ${ifn:convertListToJson(areaNames)};

	function doClose() {
		window.location.href = "${cpath}/master/areas/list.htm?sortOrder=area_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
		var newAreaName = trimAll(document.forms[0].area_name.value);
		var selectedCity = document.forms[0].city_id.value;
			for(var i=0;i<chkAreaName.length;i++){
				item = chkAreaName[i];
				if(selectedCity == item.city_id){
					if (newAreaName == item.area_name){
						alert(document.forms[0].area_name.value+" already exists pls enter other name...");
				    	document.forms[0].area_name.value='';
				    	document.forms[0].area_name.focus();
				    	return false;
					}
				}

			}
	}
	
	function setDefaults(form) {
		form.area_id.value = '';
		form.city_id.value = '';
		form.area_name.value = '';
		form.status.selectedIndex = 0;
	}

</script>

</head>

	<c:set var="actionUrl" value="${cpath}/master/areas/create.htm"/>
<form action="${actionUrl}" method="POST" name="areaMaster">
	<input type="hidden" name="area_id" value="${bean.area_id}"/>

	<h1>Add Area</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">City:</td>
				<td>
					<insta:selectdb name="city_id" value="${bean.city_id}" table="city" valuecol="city_id" displaycol="city_name"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Area:</td>
				<td>
					<input type="text" name="area_name" value="${bean.area_name}" onblur="capWords(area_name);" class="required validate-length"
					length="70" title="Name is required and max length of name can be 70"  />
				</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		| <a href="javascript:void(0)" onclick="doClose();">Area List</a>
	</div>
</form>

</body>
</html>
