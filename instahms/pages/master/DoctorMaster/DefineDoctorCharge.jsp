<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/Adddoctor.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/editCharges.js"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Doctor Charges</title>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var cpth  =  '${cpath}';
	function funSaveValues(){
		if(!validateAllDiscounts()) return false;
		document.forms[1].org_id.value = document.forms[1].orgId.value;
		document.forms[1].organization.value = document.forms[1].orgId.value;
		document.forms[1].action=cpth+"/master/DoctorMasterCharges.do?method=addOrUpdateCharges";
		document.forms[1].submit();
	}

	function changeRatePlan(){
		document.showform.org_id.value = document.forms[1].orgId.value
		document.showform.submit();
	}

	function validateAllDiscounts() {
		var len = document.forms[1].ids.value;
		var valid = true;
		valid = valid && validateDiscount('op_charge','op_charge_discount','');
		valid = valid && validateDiscount('private_cons_charge','private_cons_discount','');
		valid = valid && validateDiscount('op_revisit_charge','op_revisit_discount','');
		valid = valid && validateDiscount('private_cons_revisit_charge','private_revisit_discount','');

		for(var i=0;i<len;i++) {
			valid = valid && validateDiscount('doctor_ip_charge','doctor_ip_charge_discount',i);
			valid = valid && validateDiscount('night_ip_charge','night_ip_charge_discount',i);
			valid = valid && validateDiscount('ward_ip_charge','ward_ip_charge_discount',i);
			valid = valid && validateDiscount('ot_charge','ot_charge_discount',i);
			valid = valid && validateDiscount('assnt_surgeon_charge','assnt_surgeon_charge_discount',i);
			valid = valid && validateDiscount('co_surgeon_charge','co_surgeon_charge_discount',i);
		}
		if(!valid) return false;
		else return true;
	}
	Insta.masterData = ${doctorsList};

	function fillRatePlanDetails(doctorId){
		if(derivedRatePlanDetails.length>0) {
			document.getElementById("ratePlanDiv").style.display = 'block' ;
			for (var i =0; i<derivedRatePlanDetails.length; i++) {
				var ratePlanTbl = document.getElementById("ratePlanTbl");
				var len = ratePlanTbl.rows.length;
				var templateRow = ratePlanTbl.rows[len-1];
			   	var row = '';
			   		row = templateRow.cloneNode(true);
			   		row.style.display = '';
			   		row.id = len-2;
			   		len = row.id;
			   	YAHOO.util.Dom.insertBefore(row, templateRow);

				var cell1 = row.insertCell(-1);
			    cell1.setAttribute("style", "width: 70px");
			    if(derivedRatePlanDetails[i].is_override=='Y')
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/blue_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
		    	else
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/empty_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
			    var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "hidden");
			    inp2.setAttribute("name", "ratePlanId");
			    inp2.setAttribute("id", "ratePlanId"+len);
			    inp2.setAttribute("value", derivedRatePlanDetails[i].org_id);
			    cell1.appendChild(inp2);

				var cell2 = row.insertCell(-1);
			    cell2.setAttribute("style", "width: 70px");
			    cell2.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].discormarkup;

			    var cell3 = row.insertCell(-1);
			    cell3.setAttribute("style", "width: 40px");
			    cell3.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].rate_variation_percent;

				var orgId = derivedRatePlanDetails[i].org_id;
				var doctorId = derivedRatePlanDetails[i].doctor_id;
				var cell4 = row.insertCell(-1);
				var baseRateSheet = derivedRatePlanDetails[i].base_rate_sheet_id;
				var orgName = derivedRatePlanDetails[i].org_name;
				var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
						'&doctor_id='+doctorId+'&chargeCategory=doctor&fromItemMaster=true&baseRateSheet='+baseRateSheet+
						'&org_name='+orgName;
					cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
			}
		}
	}

</script>

</head>

<body class="setMargin yui-skin-sam" onload="fillRatePlanDetails('${DocDetails.doctor_id}');">
<h1 style="float:left;">Doctor Charges</h1>
<c:url var="searchUrl" value="/master/DoctorMasterCharges.do"/>
<insta:findbykey keys="doctor_name,doctor_id" fieldName="doctor_id" method="getDoctorChargesScreen" url="${searchUrl}"
extraParamKeys="mode,org_id" extraParamValues="update,ORG0001"/>
<form action="${cpath}/master/DoctorMasterCharges.do" method="post">
	<input type="hidden" name="_method" value="addOrUpdateCharges" />
	<input type="hidden" name="org_id" value="${DocDetails.org_id}"/>
	<input type="hidden" name="organization" value="${DocDetails.org_id}"/>
	<input type="hidden" name="doctor_id" value="${DocDetails.doctor_id}"/>
	<input type="hidden" name="doctor_name" value="${DocDetails.doctor_id}"/>
	<input type="hidden" name="mode" value="${ifn:cleanHtmlAttribute(mode)}"/>

<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Doctor Name</td>
			<td><input type="text" name="doctorName"
				onblur="upperCase(doctorName)" id="doctorName" value="${DocDetails.doctor_name}" readonly /></td>
			<td class="formlabel">Department</td>
			<td><input type="text" name="deptName" value="${DocDetails.dept_name}" readonly />
			</td>

		</tr>

		<tr>
			<td class="formlabel">Rates for Rate Sheet:</td>
			<td>
				<c:choose>
					<c:when test="${mode eq 'insert'}">
						<label class="forminfo">GENERAL</label>
							<input type="hidden" name="orgId" value="ORG0001">
					</c:when>
					<c:otherwise>
						<insta:selectdb name="orgId" value="${DocDetails.org_id}"
								table="organization_details" valuecol="org_id" orderby="org_name"
								displaycol="org_name" onchange="changeRatePlan();"
								filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>

					</c:otherwise>
				</c:choose>
			</td>
			<td class="formlabel">OP Validity Days</td>
			<td><input type="text" name="validity"
				onkeypress='return enterNumOnlyzeroToNine(event)'
				Class="number" value="${DocDetails.op_consultation_validity}" readonly/></td>
		</tr>

		<tr>
			<td class="formlabel">OP Consultation Charge</td>
			<td><input type="text" name="op_charge" id="op_charge" Class="number" value="${ifn:afmt(DocDetails.op_charge)}"
					onblur="validateDiscount('op_charge','op_charge_discount','')"/>
			</td>
			<td class="formlabel">OP Consultation Charge Discount</td>
			<td><input type="text" name="op_charge_discount" id="op_charge_discount" Class="number" value="${ifn:afmt(DocDetails.op_charge_discount)}"
					onblur="validateDiscount('op_charge','op_charge_discount','')"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">OP Revisit Charge</td>
			<td><input type="text" name="op_revisit_charge" id="op_revisit_charge" Class="number" value="${ifn:afmt(DocDetails.op_revisit_charge)}"
					onblur="validateDiscount('op_revisit_charge','op_revisit_discount','')"/>
			</td>
			<td class="formlabel">OP Revisit Charge Discount</td>
			<td><input type="text" name="op_revisit_discount" id="op_revisit_discount" Class="number" value="${ifn:afmt(DocDetails.op_revisit_discount)}"
					onblur="validateDiscount('op_revisit_charge','op_revisit_discount','')"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Private OP Consultation Charge</td>
			<td><input type="text" name="private_cons_charge" id="private_cons_charge" Class="number" value="${ifn:afmt(DocDetails.private_cons_charge)}"
					onblur="validateDiscount('private_cons_charge','private_cons_discount','')"/>
			</td>
			<td class="formlabel">Private OP Consultation Charge Discount</td>
			<td><input type="text" name="private_cons_discount" id="private_cons_discount" Class="number" value="${ifn:afmt(DocDetails.private_cons_discount)}"
					onblur="validateDiscount('private_cons_charge','private_cons_discount','')"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Private OP Revisit Charge</td>
			<td><input type="text" name="private_cons_revisit_charge" id="private_cons_revisit_charge" Class="number" value="${ifn:afmt(DocDetails.private_cons_revisit_charge)}"
					onblur="validateDiscount('private_cons_revisit_charge','private_revisit_discount','')"/>
			</td>
			<td class="formlabel">Private OP Revisit Charge Discount</td>
			<td><input type="text" name="private_revisit_discount" id="private_revisit_discount" Class="number" value="${ifn:afmt(DocDetails.private_revisit_discount)}"
					onblur="validateDiscount('private_cons_revisit_charge','private_revisit_discount','')"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">OP Odd hours Charge </td>
			<td><input type="text" name="op_oddhr_charge" id="op_oddhr_charge" Class="number" value="${ifn:afmt(DocDetails.op_oddhr_charge)}"
					onblur="validateDiscount('op_oddhr_charge','op_oddhr_charge_discount','')"/>
			</td>
			<td class="formlabel">OP Odd hours Charge Discount</td>
			<td><input type="text" name="op_oddhr_charge_discount" id="op_oddhr_charge_discount" Class="number" value="${ifn:afmt(DocDetails.op_oddhr_charge_discount)}"
					onblur="validateDiscount('op_oddhr_charge','op_oddhr_charge_discount','')"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="resultList">
	<div style="padding:5px 0 5px 0"></div>
		<c:set var="rowCount" value="1" />
		<c:set var="colCount" value="1" />

		<fieldset class="fieldSetBorder">
		<table id="doctorCharges" class="dataTable" cellspacing="0" cellpadding="0">
		<tr>
			<th>Bed Types</th>
			<c:forEach var="bed" items="${bedTypes}">
				<th style="width: 2em; overflow: hidden">${bed}</th>
				<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
			</c:forEach>
		</tr>

		<tr>
			<td style="text-align: right">IP Consultation</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="doctor_ip_charge" class="number validate-decimal"
					id="doctor_ip_charge${i}" value="${ifn:afmt(charges[bed].doctor_ip_charge)}"
					onblur="validateDiscount('doctor_ip_charge','doctor_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
			<input type="hidden" name="ids" value="${i+1}">
		</tr>
		<tr>
			<td style="text-align: right">IP Consultation Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="doctor_ip_charge_discount" class="number validate-decimal"
					id="doctor_ip_charge_discount${i}" value="${ifn:afmt(charges[bed].doctor_ip_charge_discount)}"
					onblur="validateDiscount('doctor_ip_charge','doctor_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>

		<tr>
			<td style="text-align: right">Night Consultation</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="night_ip_charge" class="number validate-decimal"
					id="night_ip_charge${i}" value="${ifn:afmt(charges[bed].night_ip_charge)}"
					onblur="validateDiscount('night_ip_charge','night_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">Night Consultation Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="night_ip_charge_discount" class="number validate-decimal"
					id="night_ip_charge_discount${i}" value="${ifn:afmt(charges[bed].night_ip_charge_discount)}"
					onblur="validateDiscount('night_ip_charge','night_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">IP Ward Visit Charge</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="ward_ip_charge" class="number validate-decimal"
					id="ward_ip_charge${i}" value="${ifn:afmt(charges[bed].ward_ip_charge)}"
					onblur="validateDiscount('ward_ip_charge','ward_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">IP Ward Visit Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="ward_ip_charge_discount" class="number validate-decimal"
					id="ward_ip_charge_discount${i}" value="${ifn:afmt(charges[bed].ward_ip_charge_discount)}"
					onblur="validateDiscount('ward_ip_charge','ward_ip_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>

		<tr>
			<td style="text-align: right">Surgeon/Anaesthetist Charge</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="ot_charge" class="number validate-decimal"
					id="ot_charge${i}" value="${ifn:afmt(charges[bed].ot_charge)}"
					onblur="validateDiscount('ot_charge','ot_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">Surgeon/Anaesthetist Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td>
					<input type="text" name="ot_charge_discount" class="number validate-decimal"
					id="ot_charge_discount${i}" value="${ifn:afmt(charges[bed].ot_charge_discount)}"
					onblur="validateDiscount('ot_charge','ot_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>

		<tr>
			<td style="text-align: right">Asst. Surgeon/Anaesthetist Charge</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td><input type="text" name="assnt_surgeon_charge" class="number validate-decimal"
					id="assnt_surgeon_charge${i}" value="${ifn:afmt(charges[bed].assnt_surgeon_charge)}"
					onblur="validateDiscount('assnt_surgeon_charge','assnt_surgeon_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">Asst Surgeon/Anaesthetist Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td><input type="text" name="assnt_surgeon_charge_discount" class="number validate-decimal"
					id="assnt_surgeon_charge_discount${i}" value="${ifn:afmt(charges[bed].assnt_surgeon_charge_discount)}"
					onblur="validateDiscount('assnt_surgeon_charge','assnt_surgeon_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>

		<tr>
			<td style="text-align: right">Co-op Surgeon Charge</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td><input type="text" name="co_surgeon_charge" class="number validate-decimal"
					id="co_surgeon_charge${i}" value="${ifn:afmt(charges[bed].co_surgeon_charge)}"
					onblur="validateDiscount('co_surgeon_charge','co_surgeon_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<tr>
			<td style="text-align: right">Co-op Surgeon Discount</td>
			<c:forEach var="bed" items="${bedTypes}" varStatus="k">
				<c:set var="i" value="${k.index}"/>
				<td><input type="text" name="co_surgeon_charge_discount" class="number validate-decimal"
					id="co_surgeon_charge_discount${i}" value="${ifn:afmt(charges[bed].co_surgeon_charge_discount)}"
					onblur="validateDiscount('co_surgeon_charge','co_surgeon_charge_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'doctorCharges');">
				</td>
			</c:forEach>
		</tr>
		<c:if test="${not empty bedTypes}">
		<tr>
			<td style="text-align: right">Apply Charges To All</td>
			<td><input type="checkbox" name="checkbox" onclick="fillValues('doctorCharges', this);"/></td>
			<c:forEach begin="2" end="${fn:length (bedTypes)}" >
				<td>&nbsp;</td>
			</c:forEach>
		</tr>
		</c:if>
	</table>
	</fieldset>
	</div>

	<div id="ratePlanDiv" style="display:none">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
			<table class="dashBoard" id="ratePlanTbl">
				<tr class="header">
					<td>Rate Plan</td>
					<td>Discount / Markup</td>
					<td>Variation %</td>
					<td>&nbsp;</td>
				</tr>
				<tr id="" style="display: none">
			</table>
			<table class="screenActions" width="100%">
				<tr>
					<td align="right">
						<img src='${cpath}/images/blue_flag.gif'>Overridden
					</td>
				</tr>
			</table>
		</fieldset>
	</div>

	<div class="infoPanel">
		<div class="img"><img src="${cpath}/images/information.png"/></div>
		<div class="txt">Note: all doctor charges will be applied <b>in addition</b> to Consultation Charges and Operation Charges as defined in the respective rate masters.
		</div>
		<div style="clear: both"></div>
	</div>

	<table class="screenActions">
		<tr>
			<td><button type="button" accesskey="S" name="Save" onclick="funSaveValues();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/DoctorMaster.do?_method=list&status=A&sortOrder=doctor_name&sortReverse=false&org_id=${ifn:cleanURL(org_id)}">Doctors List</a></td>
		</tr>
	</table>

</form>

<form action="DoctorMasterCharges.do" name="showform" method="GET">		<%-- for rate plan change --%>
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}" />
	<input type="hidden" name="doctor_id" value="${DocDetails.doctor_id}"/>
	<input type="hidden" name="org_id" value=""/>
	<input type="hidden" name="mode" value="update"/>
</form>

<script>
	var derivedRatePlanDetails = ${derivedRatePlanDetails};
</script>

</body>
</html>
