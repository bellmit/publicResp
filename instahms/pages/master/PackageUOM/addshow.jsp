<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@page import="com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Package UOM - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<insta:link type="script" file="ajax.js" />

<style>
.grid{margin:5px 7px 5px 7px;padding:5px 7px 5px 7px;}
input.num {text-align: right; width: 6em;}

</style>
<script>
	var pkgUOMS = ${pkgUOMS};
	var issuePkgList = ${isuuePackageList};
	function validate() {
		if ( trim(document.forms[0].package_uom.value) == '' ) {
			alert('Enter Package UOM description');
			return false;
		}

		if ( trim(document.forms[0].issue_uom.value) == '' ) {
			alert('Enter Unit UOM description');
			return false;
		}

		if ( trim(document.forms[0].package_size.value) == '' ) {
			alert('Enter Package Size');
			return false;
		}

		if ( parseFloat(document.forms[0].package_size.value) == 0 ) {
			alert('Package Size Can not be Zero');
			return false;
		}
		var add = '${ifn:cleanJavaScript(param._method)}' == 'add';
		for ( var i=0; i<issuePkgList.length; i++) {
			if (issuePkgList[i].package_uom == document.forms[0].package_uom.value && issuePkgList[i].issue_uom == document.forms[0].issue_uom.value) {
			    if (add || (document.forms[0].package_uom.value != document.forms[0].originalpkgUOM.value
			    	&& document.forms[0].issue_uom.value != document.forms[0].originalissueUOM.value)) {
					alert("Duplicate Package and Unit Uom");
					document.forms[0].package_uom.focus();
					return false;
				}
			}
			if (issuePkgList[i].integration_uom_id == document.forms[0].integration_uom_id.value) {
				if (add || (document.forms[0].integration_uom_id.value != document.forms[0].originalintgrtID.value)){
					alert("Integration ID already exists");
					document.forms[0].integration_uom_id.focus();
					return false;
				}
			}
		}
	}

</script>
</head>

<body  class="yui-skin-sam">
	<c:choose>
	    <c:when test="${param._method != 'add'}">
	        <h1>View Package UOM</h1>
	    </c:when>
	    <c:otherwise>
	         <h1>Add Package UOM </h1>
	    </c:otherwise>
	</c:choose>

<form action="PackageUOM.do" name="storesratemasterform" method="POST">

	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="originalpkgUOM" value="${bean.map.package_uom}">
	<input type="hidden" name="originalissueUOM" value="${bean.map.issue_uom}">
	<input type="hidden" name="originalintgrtID" value="${bean.map.integration_uom_id}">
	<input type="hidden" name="originalPkgSize" value="${bean.map.package_size}">


	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"> UOM Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Package UOM:</td>
			<td>
				<input type="text" name="package_uom" id="package_uom" maxlength="15" value="<c:out  value='${bean.map.package_uom}'/>"  ${param._method == 'add' ? '' : 'disabled'}/>
				<span class="star">*</span>
			</td>
			<td colspan="4"></td>

		</tr>
		<tr>
			<td class="formlabel">Unit UOM:</td>
			<td class="forminfo">
				<input type="text" name="issue_uom" id="issue_uom" maxlength="15"value="<c:out  value='${bean.map.issue_uom}'/>" ${param._method == 'add' ? '' : 'disabled'}/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Pkg Size:</td>
			<td class="forminfo">
				<input type="text" class="num" name="package_size" id="package_size"
				value="${bean.map.package_size}" onkeypress="return enterNumOnlyzeroToNine(event);" ${param._method == 'add' ? '' : 'disabled'}/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Integration ID:</td>
			<td class="forminfo">
				<input type="text" name="integration_uom_id" id="integration_uom_id" maxlength="100" value="<c:out  value='${bean.map.integration_uom_id}'/>"/>
			</td>
		</tr>

	</table>
</fieldset>


<div class="screenActions">
	<input type="submit" name="save" value="Save" class="button" onclick="return validate();">
	|
	<c:if test="${param._method != 'add'}">
	<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/PackageUOM.do?_method=add'">Add</a>
	|
	</c:if>
	<a href="${cpath }/master/PackageUOM.do?_method=list">Back To DashBoard</a>
</div>

</form>
</body>
</html>
