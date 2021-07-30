<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Payment Terms - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<script>
function setDeliveryInstruction(){
	if(document.getElementById("isDelivery_instruction").checked) {
		document.getElementById("isDelivery_instruction").value='Y';
	}
	else {
		document.getElementById("isDelivery_instruction").value='N';
	}
}

function Save(){
	document.getElementById("template_name").value=trimAll(document.getElementById("template_name").value);
	document.getElementById("terms_conditions").value=trimAll(document.getElementById("terms_conditions").value);
	if(document.getElementById("template_name").value==""){
		alert('TemplateName is required');
		document.getElementById("template_name").focus();
		return false;
	}
	if(document.getElementById("terms_conditions").value == ""){
		alert('Terms and Conditions is required');
		document.getElementById("terms_conditions").focus();
		return false;
	}
	if(document.getElementById("isDelivery_instruction").checked) {
		document.getElementById("isDelivery_instruction").value='Y';
		document.getElementById("is_delivery_instruction").value='Y';
	}
	else {
		document.getElementById("isDelivery_instruction").value='N';
		document.getElementById("is_delivery_instruction").value='N';
	}
	document.templateForm.action = "tempdetails.do?_method=saveTemplateDetails";
	document.templateForm.submit();
}
function checkDuplicates(){
	var templateName=document.getElementById("template_name").value;
	'<c:forEach items="${templateCode}" var="templateNames">'
		var templateName1='${templateNames.TEMPLATE_NAME}';
		var tcode = '${templateNames.TEMPLATE_CODE}';
		var templatecode = document.templateForm.template_code.value;
		if (tcode != templatecode) {
			if(templateName==templateName1){
				alert("The Template Name Already Exists Please Enter Another Name");
				document.getElementById("template_name").focus();
				document.getElementById("template_name").value = "";
				return false;
			}
		}
	'</c:forEach>'
}
<c:if test="${!empty tempdto}">
    Insta.masterData=${templateList};
  </c:if>
</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body class="yui-skin-sam" >
<c:choose>
     <c:when test="${not empty tempdto}">
     <h1 style="float:left">Edit Department</h1>
	    <c:url var="searchUrl" value="/pages/masters/insta/stores/tempdetails.do"/>
	    <insta:findbykey keys="template_name,template_code" method="getTemplateDetailsScreen" fieldName="tempName" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
      <h1>Payment Terms</h1>
     </c:otherwise>
</c:choose>
<form method="GET" action="" name="templateForm">
<input type="hidden" name="operation" value="${not empty tempdto.map.template_code ? 'update' : 'insert'}">
<input type="hidden" name="template_code" value="${tempdto.map.template_code}"/>
<input type="hidden" name="_method" value="saveTemplateDetails"/>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"> Payment Term Details</legend>
<table   class="formtable" >
  <tr>
     <td class="formlabel">Template Name:</td>
     <td ><input type="text" name="template_name"  maxlength="30"  id="template_name"   onChange="return checkDuplicates();" value="${tempdto.map.template_name }"><span class="star">*</span></td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
 </tr>
  <tr>
     <td class="formlabel">Status:</td>
	 <td><insta:radio name="status" radioValues="A,I" value="${not empty tempdto.map.status ? tempdto.map.status : 'A'}"
		 radioText="Active,Inactive" radioIds="active,inactive" /></td>
  </tr>
  <tr>
  	<td class="formlabel">PO Delivery Instructions:</td>
  	<td><input type="checkbox" name="isDelivery_instruction" id="isDelivery_instruction" value="${tempdto.map.is_delivery_instruction}"
					<c:if test="${tempdto.map.is_delivery_instruction == 'Y'}">checked</c:if> onChange="setDeliveryInstruction();"/>
		<input type="hidden" name="is_delivery_instruction" id="is_delivery_instruction" value="${tempdto.map.is_delivery_instruction}" />
  	</td>
  </tr>
  <tr>
  	<td class="formlabel">Terms & Conditions:</td>
  	<td><textarea name="terms_conditions" rows="5"  id="terms_conditions"
  		cols="39" ><c:out value="${tempdto.map.terms_conditions}"/></textarea><span class="star">*</span></td>
  </tr>
  </table></fieldset>

   <div class="screenActions">
		<button type="button" accesskey="S" name="save" class="button" onclick="return Save()" >
		<b><u>S</u></b>ave</button>
		|
		<c:if test="${not empty tempdto}">
		<a href="javascript:void(0);" onclick="window.location.href='${cpath }/pages/masters/insta/stores/tempdetails.do?_method=getTemplateDetailsScreen'">Add</a>
		|
		</c:if>
		<a href="${cpath }/pages/masters/insta/stores/tempdetails.do?_method=getTemplateDashBoard&sortOrder=template_name&sortReverse=false&status=A">Back To DashBoard</a>
  </div>
  </form></body></html>
