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
<title>License - Insta HMS</title>
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
	function viewLicense(){
		window.open("?method=getlicenseForm&licenseId=${bean.license_id}");
	}
	function validate(){
		if(document.forms[0].license_type_id.selectedIndex==0){
			alert("License Type is required");
			document.forms[0].license_type_id.focus();
			return false;
		}
		if(document.forms[0].license_desc.value==""){
			alert("Name is required");
			document.forms[0].license_desc.focus();
			return false;
		}
		if(document.forms[0].license_start_date.value==""){
			alert("StartDate is required");
			document.forms[0].license_start_date.focus();
			return false;
		}
		if(document.forms[0].license_end_date.value==""){
			alert("EndDate is required");
			document.forms[0].license_end_date.focus();
			return false;
		}
		if(document.forms[0].license_renewal_date.value==""){
			alert("RenewalDate is required");
			document.forms[0].license_renewal_date.focus();
			return false;
		}
		if(document.forms[0].license_value.value==""){
			alert("License value is required");
			document.forms[0].license_value.focus();
			return false;
		}
		if(document.forms[0].method.value=="create"){
			if(document.forms[0].licenseFile.value==""){
				alert("License File is required");
				document.forms[0].licenseFile.focus();
				return false;
			}
		}
		if(document.forms[0].contractor_name.value=="") {
			alert("contractor name is required");
			document.forms[0].contractor_name.focus();
			return false;
		}
		document.forms[0].submit();
	}

var itAutoComplete = null;

function initContractorNames() {
	document.forms[0].contractor_name.value=contr_name;
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

<form action="license.do" method="POST" enctype="multipart/form-data" >
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="license_id" value="${bean.license_id}"/>
		<input type="hidden" name="licenseName" value="${bean.license_desc}"/>
	</c:if>

	<div class="pageHeader">${param.method == 'add' ? 'Add' : 'Edit'} License</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">License Details</legend>
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">License Type:</td>
					<td>
						<insta:selectdb name="license_type_id" value="${bean.license_type_id}" table="license_type_master"
							valuecol="license_type_id" displaycol="license_type"  dummyvalue="--Select--"/>
					</td>
					<td class="formlabel">Name:</td>
					<td><input type="text" name="license_desc" id="license_desc" value="${bean.license_desc}"
							onblur="capWords(license_desc);" style="width: 12em" maxlength="50"/>
					</td>
					<td class="formlabel">License Value:</td>
					<td><input type="text" name="license_value" id="license_value" value="${bean.license_value}" onkeypress="return enterNumOnlyANDdot(event)"/></td>
				</tr>

				<tr>
					<td class="formlabel">Start Date:</td>
					<td><insta:datewidget name="license_start_date" id="license_start_date"  value="${bean.license_start_date}"/></td>
					<td class="formlabel">End Date:</td>
					<td><insta:datewidget name="license_end_date" id="license_end_date"  value="${bean.license_end_date}"/></td>
					<td class="formlabel">Renewal Date:</td>
					<td><insta:datewidget name="license_renewal_date" id="license_renewal_date"  value="${bean.license_renewal_date}"/></td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="license_status" value="${bean.license_status}" opvalues="A,I" optexts="Active,InActive"/></td>
					<td class="formlabel">License Form:</td>
					<td colspan="4">
						<input type="file" name="licenseFile" id="licenseFile" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
						<c:if test="${param.method == 'show'}">
							<input type="button" name="view"  value="View" onclick="viewLicense();"/>
						</c:if>
					</td>
				</tr>
			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">License Organization</legend>
			<table class="formtable">
			 	<tr>
			 		<td>
						<table class="formtable">
								<tr>
									<td class="formlabel">Contractor Name:</td>
									<td class="forminput">
										<div id="contractor_name_wrapper" class="autoComplete" style="padding-bottom: 1em;width:160px;">
													<input type="text" name="contractor_name" id="contractor_name" style="width: 13em;" />
													<input type="hidden" name="contractor_id" id="contractor_id" value="${bean.contractor_id}" />
													<div id="contractor_name_dropdown" style="width:30em;"></div>
										</div>
										<span class="star">*</span>
									</td>
									<td class="formlabel">Note:</td>
									<td><textarea name="license_note" id="license_note" rows="2" cols="30"/>${bean.license_note}</textarea>
									</td>
									<td>&nbsp;</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</fieldset>

	<table>
		<tr>
			<td colspan="2" align="center">
				<input type="button" value="Save" onclick="validate();"/>
				|<a href = "${cpath}/resourcemanagement/license.do?method=list"> | Back To Dashboard</a></td>
			</td>
		</tr>

	</table>


</form>

</body>
</html>
