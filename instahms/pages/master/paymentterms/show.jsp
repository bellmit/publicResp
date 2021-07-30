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
var paymentTermsList  = ${ifn:convertListToJson(paymentTermsList)};

function autoCompleterPaymentTerms() {
	var datasource = new YAHOO.util.LocalDataSource({result: paymentTermsList});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "template_name"},{key : "template_code"} ]
	};
	var rAutoComp = new YAHOO.widget.AutoComplete('template_name','templatecontainer', datasource);
	rAutoComp.minQueryLength = 0;
 	rAutoComp.maxResultsDisplayed = 20;
 	rAutoComp.forceSelection = true ;
 	rAutoComp.animVert = false;
 	rAutoComp.resultTypeList = false;
 	rAutoComp.typeAhead = false;
 	rAutoComp.allowBroserAutocomplete = false;
 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = true;
	rAutoComp.useShadow = false;
 	if (rAutoComp._elTextbox.value != '') {
			rAutoComp._bItemSelected = true;
			rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
	}
 	rAutoComp.itemSelectEvent.subscribe(setIds);
}

function setIds(oself, elItem) {
	document.searchKeyForm.template_code.value = elItem[2].template_code;
}

function checkEmpty() {
	var id = document.searchKeyForm.template_name;
	id.blur();
	if (id.value == '') {
		alert("Please enter the search value");
		id.focus();
		return false;
	}
	return true;
}

function setDeliveryInstruction(){
	if(document.getElementById("isDelivery_instruction").checked) {
		document.getElementById("isDelivery_instruction").value='Y';
	}
	else {
		document.getElementById("isDelivery_instruction").value='N';
	}
}

function Save(){
	var paymentTermsForm = document.templateForm;
	paymentTermsForm.template_name.value = trimAll(paymentTermsForm.template_name.value);
	paymentTermsForm.terms_conditions.value = trimAll(paymentTermsForm.terms_conditions.value);
	
	if(paymentTermsForm.template_name.value ==""){
		alert('TemplateName is required');
		paymentTermsForm.template_name.focus();
		return false;
	}
	if(paymentTermsForm.terms_conditions.value == ""){
		alert('Terms and Conditions is required');
		paymentTermsForm.terms_conditions.focus();
		return false;
	}
	if(paymentTermsForm.isDelivery_instruction.checked) {
		paymentTermsForm.isDelivery_instruction.value='Y';
		paymentTermsForm.is_delivery_instruction.value='Y';
	}
	else {
		paymentTermsForm.isDelivery_instruction.value='N';
		paymentTermsForm.is_delivery_instruction.value='N';
	}
	if(paymentTermsForm.operation.value  == "update"){
		paymentTermsForm.action = "update.htm";
	}else{
		paymentTermsForm.action = "create.htm";
	}
	paymentTermsForm.submit();
}
function checkDuplicates(){
	var templateName= document.templateForm.template_name.value;
	var templatecode = document.templateForm.template_code.value;
	for(var i =0; i<paymentTermsList.length; i++ ) {
		var templateName1 = paymentTermsList[i].template_name;
		var tcode = paymentTermsList[i].template_code;
		if (tcode != templatecode) {
			if(templateName==templateName1){
				alert("The Template Name Already Exists Please Enter Another Name");
				document.templateForm.template_name.focus();
				document.templateForm.template_name.value = "";
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
<body class="yui-skin-sam" onload="autoCompleterPaymentTerms()">
<h1 style="float:left">Edit Payment Terms</h1>
<form name="searchKeyForm" action="show.htm" method="GET">
	<input type="hidden" name="template_code" id="template_code" value=""/>
	<table style="float:right; padding-top:8px">
		<tr>
			<td>Enter Name:</td>
			<td>
				<div id="autocomplete" style="width: 100px; padding-bottom: 20px">
					<input type="text" id="template_name"  >
					<div id="templatecontainer" style="right: 0px; width: 200px"></div>
				</div>
			</td>
			<td><input type="submit" value="Get Details" onclick="return checkEmpty();"/></td>
		</tr>
	</table>
</form>
<div style="clear: both"></div>
<form method="POST" action="" name="templateForm">
<input type="hidden" id="operation" name="operation" value="${not empty bean.template_code ? 'update' : 'insert'}">
<input type="hidden" name="template_code" value="${bean.template_code}"/>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"> Payment Term Details</legend>
<table   class="formtable" >
  <tr>
     <td class="formlabel">Template Name:</td>
     <td ><input type="text" name="template_name"  maxlength="30"  id="template_name"   onChange="return checkDuplicates();" value="<c:out value='${bean.template_name }' />" /><span class="star">*</span></td>
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
		<c:if test="${not empty bean}">
		<a href="javascript:void(0);" onclick="window.location.href='${cpath }/master/paymentterms/add.htm?'">Add</a>
		|
		</c:if>
		<a href="${cpath }/master/paymentterms.htm?sortOrder=template_name&sortReverse=false&status=A">Back To DashBoard</a>
  </div>
  </form></body></html>
