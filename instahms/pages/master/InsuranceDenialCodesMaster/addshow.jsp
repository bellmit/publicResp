<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Denial Codes - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var backupName = '';

	function keepBackUp(){
		if(document.denialCodeForm._method.value == 'update'){
				backupName = document.denialCodeForm.denial_code.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/InsuranceDenialCodes.do?_method=list&sortOrder=denial_code&sortReverse=false" +
							"&status=A";
	}

	function focus(){
		document.denialCodeForm.denial_code.focus();
	}

	function FormatTextAreaValues(vText) {
		vRtnText= vText;
		while(vRtnText.indexOf("\n") > -1) {
			vRtnText = vRtnText.replace("\n"," ");
		}
		while(vRtnText.indexOf("\r") > -1) {
			vRtnText = vRtnText.replace("\r"," ");
		}
		return vRtnText;
	}

	function formatExampleValue() {
		document.denialCodeForm.example.value = FormatTextAreaValues(document.denialCodeForm.example.value);
	}

	<c:if test="${param._method != 'add'}">
      Insta.masterData=${DenialCodesList};
    </c:if>

</script>
</head>

<body onload="focus(); keepBackUp();">

<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit Denial Code</h1>
         <c:url var ="searchUrl" value="InsuranceDenialCodes.do"/>
         <insta:findbykey keys="code_description,denial_code" fieldName="denial_code" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Denial Code</h1>
    </c:otherwise>
</c:choose>

<insta:feedback-panel/>

<form action="InsuranceDenialCodes.do" method="POST" name="denialCodeForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="old_denial_code"  value="${bean.map.denial_code}"/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Denial Code:</td>
			<td>
				<input type="text" name="denial_code"  value="${bean.map.denial_code}" class="required validate-length"
					length="15" title="Denial Code is required and max length of denial code can be 15" ${param._method == 'add' ? '' : 'readonly'}/>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Code Description:</td>
			<td colspan="5">
				<input type="text" name="code_description" style="width: 100%" value="${bean.map.code_description}"
					class="required" size="60" title="Code Description is required and max length of denial code can be 500" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Example:</td>
			<td>
				<textarea name="example" title="Code Example" rows="4" cols="60">${bean.map.example}</textarea>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Code Type:</td>
			<td>
				<insta:selectdb name="type" value="${bean.map.type}" table="insurance_denial_code_types"
					valuecol="type" displaycol="type" dummyvalue="..Select.." dummyvalueId=""/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
		<tr>
			<td class="formlabel">Special Denial Code On Correction:</td>
			<td><insta:selectoptions name="special_denial_code_on_correction"  id="special_denial_code_on_correction"
			value="${bean.map.special_denial_code_on_correction}" opvalues="N,Y" optexts="No,Yes" /></td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="formatExampleValue();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/InsuranceDenialCodes.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">Denial Codes List</a>
	</div>

</form>

</body>
</html>
