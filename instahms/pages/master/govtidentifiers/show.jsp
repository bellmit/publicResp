<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Govt ID Type Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
			if(document.govtidtypemasterform._method.value == 'update'){
				backupName = document.govtidtypemasterform.identifier_type.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/govtidentifiers/list.htm?_method=list&status=A&sortOrder=identifier_type" +
						"&sortReverse=false";
		}
		function focus() {
			document.govtidtypemasterform.identifier_type.focus();
		}

		<c:if test="${param._method != 'add'}">
    	   Insta.masterData=${ifn:convertListToJson(identifierTypeDetails)};
       </c:if>

	</script>
</head>
<body onload= "keepBackUp();" >   
        <h1 style="float:left">Edit Govt ID Type</h1>
        <c:url var="searchUrl" value="show.htm"/>
        <insta:findbykey keys="identifier_type,identifier_id" fieldName="identifier_id" method="show" url="${searchUrl}"/>
 
<form action="update.htm"  name="govtidtypemasterform" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="identifier_id" value="${bean.identifier_id}"/>
	
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Default Value:</td>
			<td>
				<input type="text" maxlength="100" name="identifier_type" value="${bean.identifier_type}" />
			</td>
			<td class="formlabel">Unique ID:</td>
			<td>
				<insta:selectoptions name="unique_id" value="${bean.unique_id}" opvalues="N,Y" optexts="No,Yes"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Description:</td>
			<td>
				<input type="text" maxlength="200" name="remarks" value="${bean.remarks}"/>
			</td>
			<td class="formlabel">Default:</td>
			<td>
				<insta:selectoptions name="default_option" value="${bean.default_option}" opvalues="N,Y" optexts="No,Yes"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
			<td class="formlabel">Govt ID Pattern:</td>
			<td>
				<input type="text" maxlength="100" name="govt_id_pattern" id="govt_id_pattern" value="${bean.govt_id_pattern}" />
				<img style="text-align: right" class="imgHelpText" title="Govt. Identifier Pattern: is for validating ${beanMap.government_identifier_label}. The pattern will be in the
					combination of x and 9. Here x is for any alphabetical character and 9 is for any numeric digit. Other than x and 9 represents constant values.
					Ex: pattern: xx-99-99 then allowed values are SA-45-67, TI-95-27
					pattern: IND99-99-9x then allowed values are ind12-20-3g,IND07-43-4U."
					 src="${cpath}/images/help.png"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Mandatory:</td>
			<td>
				<insta:selectoptions name="value_mandatory" value="${bean.value_mandatory}" opvalues="N,Y" optexts="No,Yes"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		 <c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/govtidentifiers/add.htm?_method=add">Add</a>
		|
		</c:if>
			<a href="javascript:void(0)" onclick="doClose();">Govt ID Type List</a>
	</div>

</form>
</body>
</html>
