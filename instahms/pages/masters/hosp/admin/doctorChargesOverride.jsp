<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
	<head>
		<insta:link type="script" file="hmsvalidation.js" />
		<insta:link type="js" file="masters/Adddoctor.js" />
		<insta:link type="js" file="masters/charges_common.js" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Rate Plan Overrides - Insta HMS</title>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<script>
			var cpth  =  '${cpath}';
			function funSaveValues(){
				if(!validateAllDiscounts()) return false;
				document.forms[0].org_id.value = document.forms[0].org_id.value;
				document.forms[0].organization.value = document.forms[0].org_id.value;
				document.forms[0].action=cpth+"/pages/masters/ratePlan.do?method=overRideDoctorCharges";
				document.forms[0].submit();
			}

			function validateAllDiscounts() {
				var len = document.forms[0].ids.value;
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

		</script>

	</head>
	<body class="setMargin yui-skin-sam">

		<form action="${cpath}/pages/masters/ratePlan.do" method="post">
			<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>
			<input type="hidden" name="_method" value="overRideDoctorCharges" />
			<input type="hidden" name="org_id" value="${bean.map.org_id}"/>
			<input type="hidden" name="organization" value="${bean.map.org_id}"/>
			<input type="hidden" name="doctor_id" value="${bean.map.doctor_id}"/>
			<input type="hidden" name="doctor_name" value="${bean.map.doctor_id}"/>
			<input type="hidden" name="chargeCategory" value="doctor"/>

			<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
			<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Doctor Charges Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Doctor Name : </td>
					<td class="forminfo">${bean.map.doctor_name}</td>
				</tr>

				<tr>
					<td class="formlabel">OP Consultation Charge</td>
					<td><input type="text" name="op_charge" id="op_charge" Class="number" value="${ifn:afmt(bean.map.op_charge)}"
							onblur="validateDiscount('op_charge','op_charge_discount','')"/>
					</td>
					<td class="formlabel">OP Consultation Charge Discount</td>
					<td><input type="text" name="op_charge_discount" id="op_charge_discount" Class="number" value="${ifn:afmt(bean.map.op_charge_discount)}"
							onblur="validateDiscount('op_charge','op_charge_discount','')"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">OP Revisit Charge</td>
					<td><input type="text" name="op_revisit_charge" id="op_revisit_charge" Class="number" value="${ifn:afmt(bean.map.op_revisit_charge)}"
							onblur="validateDiscount('op_revisit_charge','op_revisit_discount','')"/>
					</td>
					<td class="formlabel">OP Revisit Charge Discount</td>
					<td><input type="text" name="op_revisit_discount" id="op_revisit_discount" Class="number" value="${ifn:afmt(bean.map.op_revisit_discount)}"
							onblur="validateDiscount('op_revisit_charge','op_revisit_discount','')"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Private OP Consultation Charge</td>
					<td><input type="text" name="private_cons_charge" id="private_cons_charge" Class="number" value="${ifn:afmt(bean.map.private_cons_charge)}"
							onblur="validateDiscount('private_cons_charge','private_cons_discount','')"/>
					</td>
					<td class="formlabel">Private OP Consultation Charge Discount</td>
					<td><input type="text" name="private_cons_discount" id="private_cons_discount" Class="number" value="${ifn:afmt(bean.map.private_cons_discount)}"
							onblur="validateDiscount('private_cons_charge','private_cons_discount','')"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Private OP Revisit Charge</td>
					<td><input type="text" name="private_cons_revisit_charge" id="private_cons_revisit_charge" Class="number" value="${ifn:afmt(bean.map.private_cons_revisit_charge)}"
							onblur="validateDiscount('private_cons_revisit_charge','private_revisit_discount','')"/>
					</td>
					<td class="formlabel">Private OP Revisit Charge Discount</td>
					<td><input type="text" name="private_revisit_discount" id="private_revisit_discount" Class="number" value="${ifn:afmt(bean.map.private_revisit_discount)}"
							onblur="validateDiscount('private_cons_revisit_charge','private_revisit_discount','')"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">OP Odd hours Charge </td>
					<td><input type="text" name="op_oddhr_charge" id="op_oddhr_charge" Class="number" value="${ifn:afmt(bean.map.op_oddhr_charge)}"
							onblur="validateDiscount('op_oddhr_charge','op_oddhr_charge_discount','')"/>
					</td>
					<td class="formlabel">OP Odd hours Charge Discount</td>
					<td><input type="text" name="op_oddhr_charge_discount" id="op_oddhr_charge_discount" Class="number" value="${ifn:afmt(bean.map.op_oddhr_charge_discount)}"
							onblur="validateDiscount('op_oddhr_charge','op_oddhr_charge_discount','')"/>
					</td>
				</tr>
			</table>
		</fieldset>

		<div class="resultList">
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

		<div class="infoPanel">
			<div class="img"><img src="${cpath}/images/information.png"/></div>
			<div class="txt">Note: all doctor charges will be applied <b>in addition</b> to Consultation Charges and Operation Charges as defined in the respective rate masters.
			</div>
			<div style="clear: both"></div>
		</div>

		<table class="screenActions">
			<tr>
				<td>
					<button type="button" accesskey="S" name="Save" onclick="funSaveValues();"><b><u>S</u></b>ave</button>
					<c:choose>
						<c:when test="${fromItemMaster eq 'false'}">
							| <a href="${cpath}/pages/masters/ratePlan.do?_method=getDoctorChargesList&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Doctor Charges List</a>
						</c:when>
						<c:otherwise>
							<c:set var="url" value="${cpath}/master/DoctorMasterCharges.do?_method=getDoctorChargesScreen"/>
							| <a href="<c:out value='${url}&doctor_id=${bean.map.doctor_id}&org_id=${ifn:cleanURL(baseRateSheet)}&mode=update'/>">Doctor Charges</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>

	</form>
	</body>
</html>
