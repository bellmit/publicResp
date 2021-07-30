<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Consultation Types - Insta HMS</title>
	<insta:link type="js" file="ConsultationTypes/ConsultationTypes.js"/>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js"/>
	
	<script>
	 	var itemGroupList = ${itemGroupListJson};
	    var itemSubGroupList = ${itemSubGroupListJson};
	    function isNumberKey(evt)
        {
          var charCode = (evt.which) ? evt.which : event.keyCode
          if (charCode > 31 && (charCode < 48 || charCode > 57))
               return false;
          return true;
        }
	</script>
	
	
	
</head>

<body onload="init();itemsubgroupinit();">
	<c:if test="${param._method eq 'add'}">
		<h1>Add Consultation Type</h1>
		<c:set var="canInactivate" value=""/>
	</c:if>
	<c:if test="${param._method ne 'add'}">
		<h1>Edit Consultation Type</h1>
		<c:set var="canInactivate" value="${bean.map.consultation_type_id > 0 ?'':'disabled'}"/>
	</c:if>
	<insta:feedback-panel />
	<form name="ConsultationTypesForm">
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'updateTypes'}">
		<c:if test="${param._method == 'editTypes'}">
			<input type="hidden" name="consultation_type_id" value="${bean.map.consultation_type_id}"/>
			<input type="hidden" id="doctorChargeType" value="${bean.map.doctor_charge_type}">
			<input type="hidden" id="chargeHeadId" value="${bean.map.charge_head}">
		</c:if>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Consultation Type Name:</td>
					<td>
						<input type="text" name="consultation_type" id="consultation_type" class="field"
							value="${bean.map.consultation_type }">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Order Code/Alias:</td>
					<td>
						<input type="text" name="consultation_code" id="consultation_code" class="field"
							value="${bean.map.consultation_code}">
					</td>
				</tr>

				<tr>
					<td class="formlabel">Doctor Charge Type:</td>
					<td>
						<select name="doctor_charge_type" id="doctor_charge_type" class="dropdown">
							<option value="">-- Select --</option>
							<option value="doctor_ip_charge">IP Consultation</option>
							<option value="op_charge">OP Consultation</option>
							<option value="op_revisit_charge">OP Revisit Charge</option>
							<option value="night_ip_charge">Night Consultation</option>
							<option value="ward_ip_charge">IP Ward Visit Charge</option>
							<option value="ot_charge">Surgeon Charge</option>
							<option value="assnt_surgeon_charge">Asst Surgeon Charge</option>
							<option value="co_surgeon_charge">Co-op Surgeon Charge</option>
							<option value="private_cons_charge">Private OP Consultation Charge</option>
							<option value="op_oddhr_charge">OP Odd hours Charge</option>
							<option value="private_cons_revisit_charge">Private OP Revisit Charge</option>
						</select>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Charge Head:</td>
					<td>
						<insta:selectdb id="charge_head" name="charge_head" value="" dummyvalue="-- Select --"
							table="chargehead_constants" class="dropdown"   valuecol="chargehead_id"
							displaycol="chargehead_name"  filtercol="chargegroup_id" filtervalue="DOC"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Patient Type:</td>
					<td>
						<insta:selectoptions name="patient_type" id="patient_type" value="${bean.map.patient_type}"
							opvalues=" ,i,o,ot" optexts="-- Select --,InPatient,OutPatient,OT" />
					</td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td>

						<select name="status" ${canInactivate } class="dropdown">
							<option value="A"<c:if test="${bean.map.status eq 'A'}">selected</c:if>>Active</option>
							<option value="I" <c:if test="${bean.map.status eq 'I'}">selected</c:if>>Inactive</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Insurance Category:</td>
					<td>
						<insta:selectdb  name="insurance_category_id" id="insurance_category_id"
							value="${insurance_categories}" table="item_insurance_categories"
							valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Visit Consultation Type:</td>
					<td style="white-space:nowrap;">
						<insta:selectoptions name="visit_consultation_type" value="${bean.map.visit_consultation_type}"
							 opvalues="-1,-2,-3,-4,0"
							 optexts="OP Consultation,Revisit Consultation,IP Doctor Visit,IP Follow Up Consultation,Others" />
						<img class="imgHelpText" title="This is used to define
											the revisit as follow up/ main visit in registration.
											If visit consultation type is Revisit Consultation then according to the doctors op consultations,
											the no. of consultations are counted after the main visit OP Consultation.
											If visit consultation type is Follow Up Consultation then according to the doctors ip consultations,
											the no. of consultations are counted after the IP Discharge."
						 src="${cpath}/images/help.png"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Allow Rate Increase:</td>
					<td>
						<insta:radio name="allow_rate_increase" radioValues="true,false" radioText="Yes,No"
								value="${empty bean.map.allow_rate_increase ? 'true' : bean.map.allow_rate_increase}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Allow Rate Decrease:</td>
					<td>
						<insta:radio name="allow_rate_decrease" radioValues="true,false" radioText="Yes,No"
								value="${empty bean.map.allow_rate_decrease ? 'true' : bean.map.allow_rate_decrease}" />
					</td>

				</tr>
				<tr>
					<td class="formlabel">Allow Zero Claim Amount:</td>
					<td>
						<insta:selectoptions name="allow_zero_claim_amount" value="${empty bean.map.allow_zero_claim_amount ? 'n' : bean.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
					</td>

				</tr>
				<tr>
					<td class="formlabel">Skip Consultation Count:</td>
					<td>
						<input type="checkbox" name="skip_for_followup_count_checkbox" id="skip_for_followup_count_checkbox" ${empty bean.map.skip_for_followup_count ? "" : (bean.map.skip_for_followup_count == 'Y' ? "checked" : "")} onclick="setSkipForFollowUpCount();"/>
						<input type="hidden" value="${empty bean.map.skip_for_followup_count ? 'N' : bean.map.skip_for_followup_count}" id = "skip_for_followup_count" name="skip_for_followup_count"/>
					</td>

				</tr>
				<tr>
				   <td class="formlabel">Consultation Duration:</td>
				   <td>
				     <div style="display:inline-block;white-space:nowrap;">
				     <c:if test="${param._method ne 'add'}">
                   	    <input type="text" style ="display:inline-block;" name="duration" id="duration" class="field"
                              onkeypress="return isNumberKey(event)" value="${bean.map.duration }"/><span class="star">*</span><span> Minutes </span>
                     </c:if>
                     <c:if test="${param._method eq 'add'}">
                         <input type="text" style ="display:inline-block;" name="duration" id="duration" class="field"
                              onkeypress="return isNumberKey(event)" value="${default_duration}"/><span class="star">*</span><span> Minutes </span>
                     </c:if>
                     </div>
                    </td>
				</tr>
				<tr>
				<td class="formlabel">Billing Group:</td>
				<td>
					<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
						displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
				</td>
				</tr>
			</table>
		</fieldset>
<insta:taxations/>
		<c:url var="url" value="consultCharges.do">
			<c:param name="_method" value="list"/>
		</c:url>
		<c:url var="addurl" value="consultCharges.do">
			<c:param name="_method" value="add"/>
			<c:param name="org_id" value="ORG0001"></c:param>
		</c:url>
		<c:url var="editChargesurl" value="consultCharges.do">
			<c:param name="_method" value="edit"/>
			<c:param name="org_id" value="ORG0001"></c:param>
			<c:param name="consultation_type_id" value="${bean.map.consultation_type_id}"></c:param>
		</c:url>
		<table class="screenActions">
			<tr>
				<td >
					<button type="button" accesskey="S" onclick="validate();"><b><u>S</u></b>ave</button> |
					<c:if test="${ifn:cleanHtmlAttribute(param._method == 'editTypes')}">
						<a href="<c:out value='${addurl}' />">Add</a> |
						<a href="<c:out value='${editChargesurl}' />">Edit Charges</a>|
					</c:if>
					<a href="<c:out value='${url}' />">Consultation Types List</a>
				</td>
			</tr>
		</table>

	</form>
</body>

</html>
