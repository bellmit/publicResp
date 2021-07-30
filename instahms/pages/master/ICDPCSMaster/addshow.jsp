<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add PCS ICD Code - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].icd_code.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/ICDPCSMaster.do?_method=list&sortOrder=icd_code" +
					"&sortReverse=false&status=A";
	}

	function focus(){
		document.forms[0].icd_code.focus();
	}
</script>
</head>

<body onload="focus(); keepBackUp();">
<form action="ICDPCSMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="old_icd_code"  value="${bean.map.icd_code}"/>

	<div class="pageHeader">${param._method == 'add' ? 'Add' : 'Edit'} PCS ICD Code (Treatment)</div>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">ICD Code:</td>
			<td>
				<input type="text" name="icd_code"  value="${bean.map.icd_code}" class="required validate-length"
					length="15" title="ICD Code is required and max length of ICD Code can be 15" ${param._method == 'add' ? '' : 'readonly'}/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">ICD Description:</td>
			<td>
				<input type="text" name="icd_description"  value="${bean.map.icd_description}" class="required" size="60" title="ICD Description is required" />
			</td>
		</tr>
		<tr>
				<td class="formlabel">Code Type:</td>
				<td> 
					<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type" 
					displaycol="code_type" dummyvalue="..Select.." value="${bean.map.code_type}"/>
				</td>
		</tr>
		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/ICDPCSMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">ICD Codes List</a>
	</div>

</form>

</body>
</html>
