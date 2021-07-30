<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
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
	if(document.getElementById("operation").value == "update"){
		document.templateForm.action = "update.htm";
	}else{
		document.templateForm.action = "create.htm";
	}
	document.templateForm.submit();
}
function checkDuplicates(){
	var templateName=document.getElementById("template_name").value;
	var paymentTermsList  = ${ifn:convertListToJson(paymentTermsList)};
	var templatecode = document.templateForm.template_code.value;
	for(var i =0; i<paymentTermsList.length; i++ ) {
		var templateName1=paymentTermsList[i].template_name;
		var tcode = paymentTermsList[i].template_code;
		var templatecode = document.templateForm.template_code.value;
		if (tcode != templatecode) {
			if(templateName==templateName1){
				alert("The Template Name Already Exists Please Enter Another Name");
				document.getElementById("template_name").focus();
				document.getElementById("template_name").value = "";
				return false;
			}
		}
	}
}
/* <c:if test="${!empty tempdto}">
    Insta.masterData=${templateList};
  </c:if>  */
</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body class="yui-skin-sam" >

<h1>Add Payment Terms</h1>
     
<form method="POST" action="" name="templateForm">
<input type="hidden" id="operation" name="operation" value="${not empty bean.template_code ? 'update' : 'insert'}">
<input type="hidden" name="template_code" value="${bean.template_code}"/>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"> Payment Term Details</legend>
<table   class="formtable" >
  <tr>
     <td class="formlabel">Template Name:</td>
     <td ><input type="text" name="template_name"  maxlength="30"  id="template_name"   onChange="return checkDuplicates();" value="${bean.template_name }"><span class="star">*</span></td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
     <td>&nbsp;</td>
 </tr>
  <tr>
     <td class="formlabel">Status:</td>
	 <td><insta:radio name="status" radioValues="A,I" value="${not empty bean.status ? bean.status : 'A'}"
		 radioText="Active,Inactive" radioIds="active,inactive" /></td>
  </tr>
  <tr>
  	<td class="formlabel">PO Delivery Instructions:</td>
  	<td><input type="checkbox" name="isDelivery_instruction" id="isDelivery_instruction" value="${bean.is_delivery_instruction}"
					<c:if test="${bean.is_delivery_instruction == 'Y'}">checked</c:if> onChange="setDeliveryInstruction();"/>
		<input type="hidden" name="is_delivery_instruction" id="is_delivery_instruction" value="${bean.is_delivery_instruction}" />
  	</td>
  </tr>
  <tr>
  	<td class="formlabel">Terms & Conditions:</td>
  	<td><textarea name="terms_conditions" rows="5"  id="terms_conditions"
  		cols="39" ><c:out value="${bean.terms_conditions}"/></textarea><span class="star">*</span></td>
  </tr>
  </table></fieldset>

   <div class="screenActions">
		<button type="button" accesskey="S" name="save" class="button" onclick="return Save()" >
		<b><u>S</u></b>ave</button>
		|
		<a href="${cpath }/master/paymentterms.htm?sortOrder=template_name&sortReverse=false&status=A">Back To DashBoard</a>
  </div>
  </form></body></html>
