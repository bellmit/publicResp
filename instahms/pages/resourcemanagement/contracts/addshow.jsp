<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Contracts - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js" />
<style>
.scrolForContainer .yui-ac-content{
	 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
    _height:11em; /* ie6 */
}

.yui-ac {
	padding-bottom: 2em;
}
</style>
<script>
	var contractors = ${contractors};
	var contr_name = '${contractor_name}'
	function viewContactsForm(){
		window.open("?method=getcontractForm&contractId=${bean.contract_id}");
	}
	function validate(){
		if(document.forms[0].contract_type_id.selectedIndex==0){
			alert("Contract Type is required");
			document.forms[0].contract_type_id.focus();
			return false;
		}
		if(document.forms[0].contract_company.value==""){
			alert("Name is required");
			document.forms[0].contract_company.focus();
			return false;
		}
		if(document.forms[0].contract_start_date.value==""){
			alert("StartDate is required");
			document.forms[0].contract_start_date.focus();
			return false;
		}

		if(empty(document.forms[0].contractor_name.value)){
			alert("contractor name is required");
			document.forms[0].contractor_name.focus();
			return false;
		}

		if(document.forms[0].contract_end_date.value==""){
			alert("EndDate is required");
			document.forms[0].contract_end_date.focus();
			return false;
		}

		if ( !validateDates())
			return false;



		document.forms[0].submit();
	}

	function validateDates() {

		var startDate = getDateFromField(document.forms[0].contract_start_date);
		var endDate = getDateFromField(document.forms[0].contract_end_date);
		var renDate = getDateFromField(document.forms[0].contract_renewal_date);
		if ( !doValidateDateField(document.forms[0].contract_start_date, 'past'))
			return false;

		if (startDate > endDate) {
			alert("Start date can not be after End date.");
			document.forms[0].contract_end_date.focus();
			return false;
		}
		if (document.forms[0].contract_renewal_date.value != ""){
			if (startDate > renDate || endDate < renDate ) {
				alert("Renewal date should be between start date and end date.");
				document.forms[0].contract_renewal_date.focus();
				return false;
			}
		}
		return true;
	}
	function doClose() {

		window.location.href = "${cpath}/resourcemanagement/contracts.do?method=list";

	}

var itAutoComplete = null;

function initContractorNames() {
document.forms[0].contractor_name.value =contr_name;
	if (itAutoComplete != undefined) {
		itAutoComplete.destroy();
	}

  YAHOO.example.itemArray = [];
	var i=0;
	for(var j=0; j<contractors.length; j++)
		{
		   YAHOO.example.itemArray.length = i+1;
			YAHOO.example.itemArray[i] = contractors[j];
			i++;
		}

   YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'contractor_name'},
						{key : 'contractor_id'}
					]
		};

		itAutoComplete = new YAHOO.widget.AutoComplete('contractor_name','contractor_name_dropdown', datasource);
		itAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		itAutoComplete.useShadow = true;
		itAutoComplete.minQueryLength = 0;
		itAutoComplete.allowBrowserAutocomplete = false;
		itAutoComplete.resultTypeList = false;
		itAutoComplete.forceSelection = true;
		itAutoComplete.maxResultsDisplayed = 20;

		if (itAutoComplete._elTextbox.value != '') {
			itAutoComplete._bItemSelected = true;
			itAutoComplete._sInitInputValue = itAutoComplete._elTextbox.value;
		}


		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			document.forms[0].contractor_name.value = elItem[2].contractor_name;
			document.forms[0].contractor_id.value = elItem[2].contractor_id;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			document.forms[0].contractor_name.value = '';
		});
}
}
</script>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
</head>
<body onload="initContractorNames();">

<form action="contracts.do?method=${param.method == 'add' ? 'create' : 'update'}" method="POST" enctype="multipart/form-data" >
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="contract_id" value="${bean.contract_id}"/>
		<input type="hidden" name="contractName" value="${bean.contract_company}"/>
	</c:if>

	<div class="pageHeader">${param.method == 'add' ? 'Add' : 'Edit'} Contract</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Contract Details</legend>
			<table class="formtable" width="100%">

				<tr>
					<td class="formlabel">Contract Type:</td>
					<td>
						<insta:selectdb name="contract_type_id" value="${bean.contract_type_id}" table="contract_type_master"
							valuecol="contract_type_id" displaycol="contract_type"  dummyvalue="--Select--"/><span class="star">*</span>
					</td>
					<td class="formlabel">Name:</td>
					<td ><input type="text" name="contract_company" id="contract_company" value="${bean.contract_company}" style="width: 12em"
						class="required validate-length" length="50" maxlength="50" onblur="capWords(contract_company);"  title="Contract Name is required"/><span class="star">*</span>
					</td>
					<td class="formlabel">Contract Value:</td>
					<td><input type="text" name="contract_value" id="contract_value" value="${bean.contract_value}" style="width: 12em"
								 class="required validate-number"  title="contract value is required and should be a number" onkeypress="return enterNumOnly(event);"/><span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Start Date:</td>
					<td ><insta:datewidget name="contract_start_date" id="contract_start_date" valid="past"  value="${bean.contract_start_date}"/><span class="star">*</span></td>
					<td class="formlabel">End Date:</td>
					<td><insta:datewidget name="contract_end_date" id="contract_end_date"  value="${bean.contract_end_date}"/><span class="star">*</span></td>
					<td class="formlabel">Renewal Date:</td>
					<td><insta:datewidget name="contract_renewal_date" id="contract_renewal_date"  value="${bean.contract_renewal_date}"/></td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="contract_status" value="${bean.contract_status}" opvalues="A,I" optexts="Active,InActive"/></td>
					<td class="formlabel">Contract Form:</td>
					<td colspan="4">
						<input type="file" name="file_upload" id="file_upload" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
						<c:if test="${param.method == 'show'}">
							<a href="#" onclick="viewContactsForm();"><c:out value="${bean.contract_file_name}"/> </a>
						</c:if>
					</td>
				</tr>

			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Contract Organization</legend>
				<table class="formtable">
					<tr>
						<td>
							<table class="formtable">
								<tr>
									<td class="formlabel">Contractor Name: </td>
									<td class="forminfo">
									<div id="contractor_name_wrapper" class="autoComplete" style="padding-bottom: 1em;width:160px;">
												<input type="text" name="contractor_name" id="contractor_name"  style="width: 13em;" />
												<input type="hidden" name="contractor_id" id="contractor_id" value="${bean.contractor_id}" />
												<div id="contractor_name_dropdown" style="width:30em;"></div>
									</div>
									<span class="star">*</span>
									</td>
									<td class="formlabel" >Note:</td>
									<td><textarea name="contract_note" id="contract_note" rows="2" cols="30" >${bean.contract_note}</textarea></td>
									<td>&nbsp;</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</fieldset>

	<table>
		<tr>
			<td><input type="button" value="Save" onclick="validate();"/>
			|<a href = "${cpath}/resourcemanagement/contracts.do?method=list"> | Back To Dashboard</a></td>
		</tr>
	</table>

</form>

</body>
</html>
