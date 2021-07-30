<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.TRANSFER_HOSPITALS_PATH %>" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/edit Transfer Hospital - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
var listbean  = ${ifn:convertListToJson(listbean)};

function checkduplicate(){
	var newHospName = trimAll(document.transferHospForm.transfer_hospital_name.value);
		for(var i=0;i<listbean.length;i++){
			item = listbean[i];
		   	var hospName = item.transfer_hospital_name;
		    if (newHospName.toLowerCase() == hospName.toLowerCase()) {
		    	alert(document.transferHospForm.transfer_hospital_name.value+" already exists.");
		    	document.transferHospForm.transfer_hospital_name.value='';
		    	document.transferHospForm.transfer_hospital_name.focus();
		    	return false;
		    }
		}
}
function focus(){
	document.transferHospForm.transfer_hospital_name.focus();
}
function autoCompleterTransferHospitals() {
  		var datasource = new YAHOO.util.LocalDataSource({result: listbean});
  		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
  		datasource.responseSchema = {
  		resultsList : "result",
  		fields : [  {key : "transfer_hospital_name"},{key : "transfer_hospital_id"} ]
  		};
  		  var rAutoComp = new YAHOO.widget.AutoComplete('transfer_hospital_name','transfer_hospital_name_container', datasource);
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
	document.searchKeyForm.transfer_hospital_id.value = elItem[2].transfer_hospital_id;
}

function checkEmpty() {
  		var id = document.searchKeyForm.transfer_hospital_name;
  		id.blur();
  		if (id.value == '') {
  			alert("Please enter the search value");
  			id.focus();
  			return false;
  		}
  		return true;
}

</script>
</head>
<body onload="focus();autoCompleterTransferHospitals();">
<h1 style="float:left">Add Transfer Hospital</h1>
<form name="searchKeyForm" action="show.htm"  >
	<input type="hidden" name="transfer_hospital_id" id="transfer_hospital_id" value=""/>
	<table style="float:right; padding-top:8px">
		<tr>
		<td>Enter Name:</td>
		<td>
	<div id="autocomplete" style="width: 100px; padding-bottom: 20px">
	<input type="text" name="transfer_hospital_name" id="transfer_hospital_name"  >
	<div id="transfer_hospital_name_container" style="right: 0px; width: 200px"></div>
	</div>
	</td>
	<td><input type="submit"  value="Get Details" onclick="return checkEmpty();"></td>
	</tr>
	</table>

</form>
<div style="clear: both"></div>

<form action="create.htm" name="transferHospForm" method="POST">
	

	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
<table class="formtable">
	<tr>
		<td class="formlabel">Hospital Name:</td>
		<td>
			<input type="text" name="transfer_hospital_name" 
		  onblur="capWords(transfer_hospital_name);checkduplicate();" class="required validate-length"
		  maxlength="100" title="Name is required and max length of name can be 100" />
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		</td>
	</tr>

<tr>
	<td class="formlabel">Hospital Service Reg. No.:</td>
	<td>
		<input type="text" name="transfer_hospital_service_regn_no" 
			size="400" maxlength="500"/>
	</td>
	</tr>

<tr>
	<td class="formlabel">Status:</td>
	<td>
		<insta:selectoptions name="status" value="A"
			opvalues="A,I" optexts="Active,Inactive" /></td>
	</tr>


</table>
</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<a href="list.htm?sortOrder=transfer_hospital_name&sortReverse=false&status=A" >Hospital List</a>
	</div>

</form>

</body>
</html>
