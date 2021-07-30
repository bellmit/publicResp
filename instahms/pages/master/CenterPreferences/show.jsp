<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<html>
<head>
	<title><insta:ltext key="patient.centerpreference.addshow.pagetitle"/></title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="modAccumed" value="${preferences.modulesActivatedMap['mod_accumed']}"/>
<script>

</script>
	<style>

		div.helpText{
			width:100px;
		}

		.forminfo {
			white-space:nowrap;
		}

	</style>
	<script>
		var ipDefaultCategory = '${bean.pref_ip_default_category}';
		var opDefaultCategory = '${bean.pref_op_default_category}';
		var ospDefaultCategory = '${bean.pref_osp_default_category}';
		var emergencyPatientDefaultCategory = '${bean.emergency_patient_category_id}'
		var preRegDefaultCategory = '${bean.pref_pre_reg_default_category}';
		var smartCardEnabledFlag = '${bean.pref_smart_card_enabled}';
		
		var pref_default_op_bill_type = '${bean.pref_default_op_bill_type}';
		var pref_default_ip_bill_type = '${bean.pref_default_ip_bill_type}';
		
		var practitionerMappingJSON = ${practitioner_mappings};
		var categoryJSON=${categoryWiseRateplans};
		var pratitionerListJson = ${practitioner_list};
		var consultationTypesJSON = ${consultation_types};
		var practitionerMappingLength = practitionerMappingJSON.length;
		
		function addPractitionerMapping(tableID) {
			var table = document.getElementById(tableID);

			var rowCount = table.rows.length;
			var row = table.insertRow(rowCount);
			row.id = rowCount -1 ;

			var cell1 = row.insertCell(0);
			var optn1 = new Option("--Select--","");
			var element1 = document.createElement("select");
			element1.type = "dropdown";
			element1.name = "practitioner_type"+(rowCount-1);
			element1.id = "practitioner_type"+(rowCount-1);
			element1.onchange =function(){setConsultationTypes(false, (rowCount-1));};
			element1.size = 1;
			element1.className = "dropdown";
			element1.options[0] = optn1;
			cell1.appendChild(element1);

			var cell2 = row.insertCell(1);
			var optn2 = new Option("--Select--","");
			var element2 = document.createElement("select");
			element2.type = "dropdown";
			element2.name = "visit_type"+(rowCount-1);
			element2.id = "visit_type"+(rowCount-1);
			element2.size = 1;
			element2.className = "dropdown";
			element2.options[0] = optn2;
			cell2.appendChild(element2);
			
			var cell3 = row.insertCell(2);
			var optn3 = new Option("--Select--","");
			var element3 = document.createElement("select");
			element3.type = "dropdown";
			element3.name = "consultation_type"+(rowCount-1);
			element3.id = "consultation_type"+(rowCount-1);
			element3.size = 1;
			element3.className = "dropdown";
			element3.options[0] = optn3;
			cell3.appendChild(element3);
			
			var cell4 = row.insertCell(3);
			var element4 = document.createElement("input");
			element4.type = "hidden";
			element4.name = "mapping_id"+(rowCount-1);
			element4.id = "mapping_id"+(rowCount-1);
			element4.value = "";
			cell4.appendChild(element4);
			
			var cell5 = row.insertCell(4);
			cell5.innerHTML = "<button type='button' name='btnDeleteItem' id='btnDeleteItem' title='Delete'"
				+"onclick='deletePractitionerMapping(this)' class='imgButton'> <img src='${cpath}/icons/Delete.png'></button>";
			
			practitionerMappingLength = practitionerMappingLength + 1;
			setPractitionerMappings(false);
			setVisitTypes(false);
		}
		
		function deletePractitionerMapping(deleteButton) {
			var rowNum = deleteButton.parentNode.parentNode.rowIndex - 1;
			if(document.getElementById("mapping_id"+rowNum).value != -1) {
				document.getElementById(rowNum).style.backgroundColor = '#ccc';
				document.getElementById("mapping_id"+rowNum).value = -1;
				document.getElementById("consultation_type"+rowNum).disabled = true;
				document.getElementById("visit_type"+rowNum).disabled = true;
				document.getElementById("practitioner_type"+rowNum).disabled = true;
			} else {
				document.getElementById(rowNum).style.backgroundColor = '#FFF';
				document.getElementById("mapping_id"+rowNum).value = "";
				document.getElementById("consultation_type"+rowNum).disabled = false;
				document.getElementById("visit_type"+rowNum).disabled = false;
				document.getElementById("practitioner_type"+rowNum).disabled = false;
			}
		}
		
		function getPractitionerConsultaionTypes(practitionerTypeId) {
			var response = null;
			var ajaxReqObject = newXMLHttpRequest();
			var url = cpath+"/patients/orders/getpractitionerconsultationtypes.json?"
			url = url + "&practitioner_type_id=" + practitionerTypeId;
			ajaxReqObject.open("GET", url.toString(), false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
					eval("var ajax_response =" + ajaxReqObject.responseText);
					if(!empty(ajax_response))
						response = ajax_response.consultation_types;
				}
			}

			return response;
		}

		function setPractitionerMappings(resetAll) {
			if(resetAll) {
				for (var j=0;j<practitionerMappingJSON.length;j++) {
					var practitionerlist = document.getElementById("practitioner_type"+j);
					var len = 1;
					var optn;
					for (var i=0;i<pratitionerListJson.length;i++) {
					    optn = new Option(pratitionerListJson[i].practitioner_name,pratitionerListJson[i].practitioner_id);
					    practitionerlist.options[len] =  optn;
						len++;
					}
					document.getElementById("practitioner_type"+j).value = practitionerMappingJSON[j].practitioner_id;
					document.getElementById("mapping_id"+j).value = practitionerMappingJSON[j].mapping_id;
				}
			} else {
				var practitionerlist = document.getElementById("practitioner_type"+(practitionerMappingLength-1));
				var len = 1;
				var optn;
				for (var i=0;i<pratitionerListJson.length;i++) {
				    optn = new Option(pratitionerListJson[i].practitioner_name,pratitionerListJson[i].practitioner_id);
				    practitionerlist.options[len] =  optn;
					len++;
				}
			}
			
		}
		
		function setConsultationTypes(resetAll, rowId) {
			if(resetAll) {
				for (var j=0;j<practitionerMappingJSON.length;j++) {
					var consultationlist = document.getElementById("consultation_type"+j);
					var practitionerTypeId = document.getElementById("practitioner_type"+j).value;
					var len = 1;
					var optn;
					if (practitionerTypeId) {
						var practitionerConsultationTypesJSON = getPractitionerConsultaionTypes(practitionerTypeId);
						for (var i=0;i<practitionerConsultationTypesJSON.length;i++) {
						    optn = new Option(practitionerConsultationTypesJSON[i].consultation_type,practitionerConsultationTypesJSON[i].consultation_type_id);
						    consultationlist.options[len] =  optn;
							len++;
						}
					}
					document.getElementById("consultation_type"+j).value = practitionerMappingJSON[j].consultation_type_id;
				}
			} else if(rowId >= 0) {
				document.getElementById("consultation_type"+rowId).options.length = 0;
				var consultationlist = document.getElementById("consultation_type"+rowId);
				var practitionerTypeId = document.getElementById("practitioner_type"+rowId).value;
				var optn1 = new Option("--Select--","");
				consultationlist.options[0] =  optn1;
				var len = 1;
				var optn;
				if (practitionerTypeId) {
					var practitionerConsultationTypesJSON = getPractitionerConsultaionTypes(practitionerTypeId);
					for (var i=0;i<practitionerConsultationTypesJSON.length;i++) {
					    optn = new Option(practitionerConsultationTypesJSON[i].consultation_type,practitionerConsultationTypesJSON[i].consultation_type_id);
					    consultationlist.options[len] =  optn;
						len++;
					}
				}
			} else {
				var consultationlist = document.getElementById("consultation_type"+(practitionerMappingLength-1));
				var practitionerTypeId = document.getElementById("practitioner_type"+(practitionerMappingLength-1)).value;
				var len = 1;
				var optn;
				if (practitionerTypeId) {
					var practitionerConsultationTypesJSON = getPractitionerConsultaionTypes(practitionerTypeId);
					for (var i=0;i<practitionerConsultationTypesJSON.length;i++) {
					    optn = new Option(practitionerConsultationTypesJSON[i].consultation_type,practitionerConsultationTypesJSON[i].consultation_type_id);
					    consultationlist.options[len] =  optn;
						len++;
					}
				}
			}
		}
		
		function setVisitTypes(resetAll) {
			if(resetAll) {
				for (var j=0;j<practitionerMappingJSON.length;j++) {
					var visitlist = document.getElementById("visit_type"+j);
					var optn;
					optn = new Option("Main Visit","M");
					visitlist.options[1] =  optn;
					optn = new Option("Follow Up","F");
					visitlist.options[2] =  optn;
					optn = new Option("Revisit","R");
					visitlist.options[3] =  optn;
					
					document.getElementById("visit_type"+j).value = practitionerMappingJSON[j].visit_type;
				}
			} else {
				var visitlist = document.getElementById("visit_type"+(practitionerMappingLength-1));
				var optn;
				optn = new Option("Main Visit","M");
				visitlist.options[1] =  optn;
				optn = new Option("Follow Up","F");
				visitlist.options[2] =  optn;
				optn = new Option("Revisit","R");
				visitlist.options[3] =  optn;
			}
		}

		function opCategoryList() {
			var categorylist = document.getElementById("pref_op_default_category");
			var len = 1;
			var optn;
			for (var i=0;i<categoryJSON.length;i++) {
			    optn = new Option(categoryJSON[i].category_name,categoryJSON[i].category_id);
				categorylist.options[len] =  optn;
				len++;
			}
			if (opDefaultCategory!="")
				setSelectedIndex(document.forms[0].pref_op_default_category, opDefaultCategory);
		}

		function ipCategoryList() {
			var categorylist = document.getElementById("pref_ip_default_category");
			var len = 1;
			var optn;
			for (var i=0;i<categoryJSON.length;i++) {
			    optn = new Option(categoryJSON[i].category_name,categoryJSON[i].category_id);
				categorylist.options[len] =  optn;
				len++;
			}
			if (ipDefaultCategory!="")
				setSelectedIndex(document.forms[0].pref_ip_default_category, ipDefaultCategory);
		}

		function ospCategoryList() {
			var categorylist = document.getElementById("pref_osp_default_category");
			var len = 1;
			var optn;
			for (var i=0;i<categoryJSON.length;i++) {
			    optn = new Option(categoryJSON[i].category_name,categoryJSON[i].category_id);
				categorylist.options[len] =  optn;
				len++;
			}
			if (ospDefaultCategory!="")
				setSelectedIndex(document.forms[0].pref_osp_default_category, ospDefaultCategory);
		}

		function preRegCategoryList() {
			var categorylist = document.getElementById("pref_pre_reg_default_category");
			var len = 1;
			var optn;
			for (var i=0;i<categoryJSON.length;i++) {
			    optn = new Option(categoryJSON[i].category_name,categoryJSON[i].category_id);
				categorylist.options[len] =  optn;
				len++;
			}
			if (preRegDefaultCategory != "")
				setSelectedIndex(document.forms[0].pref_pre_reg_default_category, preRegDefaultCategory);
		}

		function emergencyPatientCategoryList() {
			var categorylist = document.getElementById("emergency_patient_category_id");
			var len = 1;
			var optn;
			for (var i=0;i<categoryJSON.length;i++) {
			    optn = new Option(categoryJSON[i].category_name,categoryJSON[i].category_id);
				categorylist.options[len] =  optn;
				len++;
			}
			if (emergencyPatientDefaultCategory)
				setSelectedIndex(document.forms[0].emergency_patient_category_id, emergencyPatientDefaultCategory);
		}

		function setSmartCardFlag(){
			var categorylist = document.getElementById("pref_smart_card_enabled");
			var len = 0;
			var optn;
			
			optn = new Option("Yes","Y");
		    categorylist.options[len] =  optn;
			len++;	
			optn = new Option("No","N");
			categorylist.options[len] =  optn;
				
			setSelectedIndex(document.forms[0].pref_smart_card_enabled, smartCardEnabledFlag);			
		}
		
		function setDefaultBillType(){
			document.mainform.pref_default_op_bill_type.value = pref_default_op_bill_type;
			document.mainform.pref_default_ip_bill_type.value = pref_default_ip_bill_type;
		}
		
		function init() {
			opCategoryList();
			ipCategoryList();
			ospCategoryList();
			preRegCategoryList();
			emergencyPatientCategoryList();
			setSmartCardFlag();
			setPractitionerMappings(true);
			setConsultationTypes(true);
			setVisitTypes(true);
			setDefaultBillType();
		}
		
		function validateSave() {
			var rows = document.getElementById("mappingTable").rows;
			var duplicateMap = new Map();
			for (var i=1; i<rows.length; i++) {
				if(document.getElementById("mappingTable").rows[i].style.backgroundColor != "rgb(204, 204, 204)") {		//not checking for deleted row
					for(var j=0;j<3;j++) {
						var cell = rows[i].cells[j];
						if(cell.childNodes[0].value == "") {
							alert("Cannot Save Empty Values");
							return false;
						}
					}
					var key = rows[i].cells[0].childNodes[0].value + "_" + rows[i].cells[1].childNodes[0].value;
					if(duplicateMap.has(key)) {
						alert("Cannot Save Duplicate Values");
						return false;
					} else {
						duplicateMap.set(key,true);
					}
				}
			}
			document.mainform.submit();
		}

	</script>
</head>

<body onload="init();">
<%-- <body onload="return loadMailClientConfigParams()"> --%>
	<c:set var="dummyvalue">
		<insta:ltext key="selectdb.dummy.value"/>
	</c:set>
	<form action="${cpath}/master/centerpreferences/update.htm" method="POST" name="mainform">
	<!-- <input type="hidden" name="method" id="method" value="saveCenterPreferences"/> -->
	<input type="hidden" name="center_id" id="center_id" value="${centerId}"/>

	<div class="pageHeader"><insta:ltext key="patient.centerpreference.addshow.pageheader"/></div>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Billing</legend>
		<table class="formtable" width="100%" align="left">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.centerpreference.addshow.rateplan"/>: </td>
				<td>
					<insta:selectdb name="pref_rate_plan_for_non_insured_bill"
							value="${bean.pref_rate_plan_for_non_insured_bill}" class="dropdown"
							style="width: 140px;" table="organization_details" 	valuecol="org_id" orderby="org_name"
							displaycol="org_name" dummyvalue="${dummyvalue}"
							dummyvalueId=""/>
				</td>
				<td class="formlabel"><insta:ltext key="ui.label.op.default.billtype"/>: </td>
				<td>	
					<select id="pref_default_op_bill_type"  name="pref_default_op_bill_type" class="dropdown" >
						<option value="P">Bill Now</option>
						<option value="C">Bill Later</option>
					</select>
				</td>
				<td class="formlabel"><insta:ltext key="ui.label.ip.default.billtype"/>: </td>
				<td>
					<select id="pref_default_ip_bill_type"  name="pref_default_ip_bill_type" class="dropdown">
						<option value="P">Bill Now</option>
						<option value="C">Bill Later</option>
					</select>
				</td>
			</tr>
			<tr>
			  <td class="formlabel"><insta:ltext key="ui.label.tax.selection.mandatory"/>: </td>
				<td>
					<insta:selectoptions name="tax_selection_mandatory" value="${bean.tax_selection_mandatory}"
					opvalues="Y,N" optexts="Yes,No"/>
				</td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Registration</legend>
		<table class="formtable" width="100%" >
			<tr>
				<td class="formlabel">OP Default Patient Category: </td>
				<td>
					<select id="pref_op_default_category"  name="pref_op_default_category" size="1" class="dropdown" >
						<option value="">-- Select --</option>
					</select>
				</td>
				<td class="formlabel">IP Default Patient Category: </td>
				<td>
					<select id="pref_ip_default_category"  name="pref_ip_default_category" size="1" class="dropdown" >
						<option value="">-- Select --</option>
					</select>
				</td>
				<td class="formlabel">OSP Default Patient Category: </td>
				<td>
					<select id="pref_osp_default_category"  name="pref_osp_default_category" size="1" class="dropdown" >
						<option value="">-- Select --</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Pre. Reg. Default Patient Category: </td>
				<td>
					<select id="pref_pre_reg_default_category"  name="pref_pre_reg_default_category" size="1" class="dropdown" >
						<option value="">-- Select --</option>
					</select>
				</td>
				<td class="formlabel">Smart Card Enabled: </td>
				<td>
					<select id="pref_smart_card_enabled"  name="pref_smart_card_enabled" size="1" class="dropdown" >
					</select>
				</td>
				<c:if test="${bean.pref_smart_card_enabled == 'Y'}">
 					<td class="formlabel">SmartCard ID Pattern:</td>
 					<td>
 						<input type="text" name="smart_card_id_pattern" id="smart_card_id_pattern" value="${bean.smart_card_id_pattern}" />
 						<img style="text-align: right" class="imgHelpText" title="SmartCard Identifier Pattern: is for validating smart card ID. The pattern will be in the combination of x and 9. Here x is for any alphabetical character and 9 is for any numeric digit. Other than x and 9 represents constant values.
Ex: pattern: xx-99-99 then allowed values are SA-45-67, TI-95-27 
pattern: IND99-99-9x then allowed values are ind12-20-3g,IND07-43-4U."
 					 		src="${cpath}/images/help.png"/>
 					</td>	
 				</c:if>	
 				<tr>
 				<td class="formlabel">Patient Identification: </td>
				<td>
					<insta:selectoptions name="patient_identification" value="${bean.patient_identification}"
						opvalues="N,G,GP"  optexts="None,Govt. Identifier Type,Govt. Identifier Type or Passport"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Emergency Patient Default Patient Category: </td>
				<td>
					<select id="emergency_patient_category_id"  name="emergency_patient_category_id" size="1" class="dropdown" >
						<option value="">-- Select --</option>
					</select>
				</td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder"> <legend class="fieldSetLabel">OP Consultation Data Access</legend>
		<table class="formtable">
			<tr>
				<td>
					<input type="radio" value="V" name="op_consultation_data_access" ${ bean.op_consultation_data_access == 'V' ? 'checked' : '' }/>
					     <label>Access Across All Visits</label>
			     </td>
			     <td>
					<input type="radio" value="D" name="op_consultation_data_access" ${ bean.op_consultation_data_access == 'D' ? 'checked' : '' }/>
					<label>Access Across Doctor Department Visit</label>
			     </td>
			     <td>
					<input type="radio" value="S" name="op_consultation_data_access" ${ bean.op_consultation_data_access == 'S' || empty bean.op_consultation_data_access ? 'checked' : '' }/>
					     <label>Self</label>
			     </td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Practitioner Type Mapping</legend>
		<div class="resultList" id="practitionerMappingDiv"
			style="margin: 10px 0px 5px 0px; width: 100%;">
			<table class="resultList"  id = "mappingTable" cellspacing="0" cellpadding="0" border="0"
				style="width: 100%;">
				<tr bgcolor="#8FBC8F">
					<th style="width: 50px">Practitioner Type</th>
					<th style="width: 30px">Visit Type</th>
					<th style="width: 50px">Consultation Type</th>
					<th style="width: 30px"></th>
					<th style="width: 30px"></th>
				</tr>
				<c:forEach begin="1" end='${practitioner_mappings_length}' var="i">
					<tr id='${i-1}'>
						<td><select id="practitioner_type${i-1}"
							name="practitioner_type${i-1}" size="1" onchange="setConsultationTypes(false, ${i-1})" class="dropdown">
							<option value="">-- Select --</option>
						</select></td>
						<td><select id="visit_type${i-1}" name="visit_type${i-1}"
							size="1" class="dropdown">
							<option value="">-- Select --</option>
						</select></td>
						<td><select id="consultation_type${i-1}"
							name="consultation_type${i-1}" size="1" class="dropdown">
							<option value="">-- Select --</option>
						</select></td>
						<td><input type="hidden" name="mapping_id${i-1}" id="mapping_id${i-1}" value=""/></td>
						<td><button type="button" name="btnDeleteItem" id="btnDeleteItem" title="Delete"
								onclick="deletePractitionerMapping(this)" accesskey="" class="imgButton">
								<img src="${cpath}/icons/Delete.png">
							</button></td>
					</tr>
				</c:forEach>
			</table>
			<button type="button" name="btnAddItem" id="btnAddItem" title="Add"
					onclick="addPractitionerMapping('mappingTable')" accesskey="" class="imgButton">
					<img src="${cpath}/icons/Add.png">
			</button>
		</div>
	</fieldset>
	<c:if test="${modAccumed == 'Y'}">
	<fieldset class="fieldSetBorder"> <legend class="fieldSetLabel">Accumed FTP Details</legend>
		<table class="formtable">
				<tr>
					<td class="formlabel">FTP URL:</td>
					<td><input type="text" name="accumed_ftp_url"
						value="${bean.accumed_ftp_url}" /></td>
					<td class="formlabel">Username</td>
					<td><input type="text" name="accumed_ftp_username"
						value="${bean.accumed_ftp_username}" /></td>
					<td class="formlabel">Password</td>
					<td><input type="password" name="accumed_ftp_password"
						value="${bean.accumed_ftp_password}" /></td>
				</tr>
			</table>
	</fieldset>
	</c:if>


	<div style="float: left; margin-top: 10px" >
		<button type="submit" property="Submit" accesskey="S" onclick="return validateSave()"><b><u><insta:ltext key="patient.centerpreference.addshow.s"/></u></b><insta:ltext key="patient.centerpreference.addshow.ave"/></button>
	</div>
	</form>
</body>
</html>
