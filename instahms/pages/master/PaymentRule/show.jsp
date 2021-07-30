<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<insta:link type="js" file="PaymentRule.js"/>
<insta:link type="script" file="ajax.js"/>
<title>Payment Rule - Insta HMS</title>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">
		function onOptionChange(){
			var doctorOptionValue = document.getElementById("doctorSelect").value ;
			var prescribingOptionValue = document.getElementById("prescribingDoctorSelect").value;
			var referalOptionValue = document.getElementById("referalDoctorSelect").value;
			
			if (doctorOptionValue != "5") {
				document.getElementById('drExpr').style.display = "none";
				document.getElementById('drExprLbl').style.display = "none";
			} else {
				document.getElementById('drExpr').style.display = "";
				document.getElementById('drExprLbl').style.display = "";
			}
			if (referalOptionValue != "5") {
				document.getElementById('refExpr').style.display = "none";
				document.getElementById('refExprLbl').style.display = "none";
			} else {
				document.getElementById('refExpr').style.display = "";
				document.getElementById('refExprLbl').style.display = "";
			}
			if (prescribingOptionValue != "5") {
				document.getElementById('prescExpr').style.display = "none";
				document.getElementById('prescExprLbl').style.display = "none";
			} else {
				document.getElementById('prescExpr').style.display = "";
				document.getElementById('prescExprLbl').style.display = "";
			}
			
			var doctorPaymentValue = getElementByName(document,'dr_payment_value');
			if(doctorOptionValue == "1") {
				doctorPaymentValue.className = 'required validate-percentage';
			} else {
				doctorPaymentValue.className = 'required validate-number';
			}
			
			var refPaymentValue = getElementByName(document,'ref_payment_value');
			if(referalOptionValue == "1") {
				refPaymentValue.className = 'required validate-percentage';
			} else {
				refPaymentValue.className = 'required validate-number';
			}
			
			var prescPaymentValue = getElementByName(document,'presc_payment_value');
			if(prescribingOptionValue == "1") {
				prescPaymentValue.className = 'required validate-percentage';
			} else {
				prescPaymentValue.className = 'required validate-number';
			}
			
		}
</script>
</head>

<body onload="${not empty bean ? 'onOptionChange();loadChargeHeads();loadActivity();' : 'loadActivity()'}">

<form action="PaymentRule.do" name="paymentForm" method="POST">
	<input type="hidden" name="_method" value="${not empty bean ? 'update' : 'create'}">
	<input type="hidden" name="rate_plans" value="${bean.map.rate_plan }">
	<input type="hidden" name="doctor_categorys" value="${bean.map.doctor_category }">
	<input type="hidden" name="referrer_categorys" value="${bean.map.referrer_category }">
	<input type="hidden" name="prescribed_categorys" value="${bean.map.prescribed_category }">
	<input type="hidden" name="charge_heads" value="${bean.map.charge_head }">
	<input type="hidden" name="precedenceValue"  value="${bean.map.precedance }">
	<input type="hidden" name="payment_id" value="${bean.map.payment_id}">
	<input type="hidden" name="firstPayee" value="dr_payment"/>
	<input type="hidden" name="secondPayee" value="ref_payment"/>
	<input type="hidden" name="thirdPayee" value="presc_payment"/>


	<h1>${not empty bean ? 'Edit' : 'Add'} Payment Rule</h1>

	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
		<table class="formtable" style="white-space:nowrap">
			<tr>
				<td class="formlabel">Priority:</td>
				<td><input type="text" name="precedance" id="precedance" value="${bean.map.precedance}" size="6"
					class="required validate-number" title="Precedence values is required" ></td>
				<td class="formlabel">Rate Plan:</td>
				<td>
					<select name="rate_plan" id="rate_plan" class="dropdown">
							<option value="*">All</option>
						<c:forEach var="rateplan" items="${organizationDetails}">
							<option value="${rateplan.map.org_id}" ${rateplan.map.org_id == bean.map.rate_plan ? 'selected' : ''}>${rateplan.map.org_name}</option>
						</c:forEach>
					</select>
				</td>
				<td class="formlabel">
					<c:if test="${max_centers_inc_default > 1}">
						Center Name:
					</c:if>
				</td>
				<td>
					<c:if test="${max_centers_inc_default > 1}">
						<insta:selectdb  name="center_id"  table="hospital_center_master"
							valuecol="center_id" displaycol="center_name" orderby="center_name" dummyvalue="All"
							 value="${bean.map.center_id}" dummyvalueId="*"/>
					 </c:if>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Doctor Category:</td>
				<td>
					<select name="doctor_category" id="doctor_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data); })" class="dropdown">
							<option value="*">All</option>
						<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}" ${fn:contains(bean.map.doctor_category, details.map.cat_id) ? 'selected' : ''}>${details.map.cat_name}</option>
						</c:forEach>
					</select>
				</td>

				<td class="formlabel">Referral Category:</td>
				<td>
					<select name="referrer_category" id="referrer_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data); })"class="dropdown">
						<option value="*">All</option>
						<option value="" ${bean.map.referrer_category eq 'R' ? 'selected' : ''}>No Referrer</option>
						<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}" ${fn:contains (bean.map.referrer_category, details.map.cat_id) ? 'selected' : ''}>${details.map.cat_name}</option>
						</c:forEach>
				</select>
				</td>
				<td class="formlabel">Prescribed Category:</td>
				<td>
					<select name="prescribed_category" id="prescribed_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data);})" class="dropdown">
							<option value="*">All</option>
							<option value="" ${bean.map.prescribed_category eq 'P' ? 'selected' : ''}>No Prescribing Doctor</option>
						<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}"${fn:contains(bean.map.prescribed_category, details.map.cat_id) ? 'selected' : ''}>${details.map.cat_name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Charge Group:</td>
				<td>
					<select name="charge_group" onchange="loadChargeHeads();loadActivity();" class="dropdown" id="charge_group">
						<option value="">-- Select --</option>
						<c:forEach var="group" items="${chargeGroups}">
							<option value="${group.map.chargegroup_id}" ${fn:contains(bean.map.chargegroup_id, group.map.chargegroup_id) ? 'selected' : ''}>${group.map.chargegroup_name}</option>
						</c:forEach>
					</select>
				</td>
				<td class="formlabel">Charge Head:</td>
				<td>
					<select name="charge_head" onchange="loadActivity();" class="dropdown" id="charge_head">
						<option value="">-- Select --</option>
					</select><span class="star">*</span>
				</td>
				<td class="formlabel">Activity:</td>
				<td>
					<select name="activity_id" class="dropdown" value="${bean.map.test_name}${bean.map.service_name}${bean.map.package_name}"></select>
				</td>
			</tr>
			<tr>

			</tr>
			<tr>
				<td class="formlabel">Doctor Payment Type: </td>
				<td>
					<select name="dr_payment_option" style="width:15em" onchange="onOptionChange()" id="doctorSelect" class="dropdown">
						<option value="1"  ${bean.map.dr_payment_option==1?'selected':''}>Percentage</option>
						<option value="3"  ${bean.map.dr_payment_option==3?'selected':''}>Actual</option>
						<option value="4"  ${bean.map.dr_payment_option == 4? 'Selected': ''}>Less than bill amount</option>
						<option value="5" ${bean.map.dr_payment_option == 5? 'Selected': ''}>Expression Builder</option>
					</select>
				</td>
				<td class="formlabel">Value: </td>
				<td>
					<input type="text" name="dr_payment_value" size="6" class="required validate-number"
					value="${bean.map.dr_payment_value}" title="Doctor Payment Value is required/Invalid">
				</td>
				<td></td>
			</tr>
			<tr>
				<td style="display:none;" id="drExprLbl" class="formlabel">Expression: </td>
				<td colspan="3" style="display:none;" id="drExpr">
					<textarea name="dr_payment_expr" rows="4" cols="60" id="dr_payment_expr"
						<c:if test="${(roleId ne 1) && (roleId ne 2)}">readonly</c:if>
					><c:out value="${bean.map.dr_payment_expr}"/></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Referral Payment Type: </td>
				<td><select name="ref_payment_option" style="width:15em" onchange="onOptionChange()" id="referalDoctorSelect" class="dropdown">
						<option value="1" ${bean.map.ref_payment_option ==1?'Selected':''}>Percentage</option>
						<option value="3"  ${bean.map.ref_payment_option == 3? 'Selected': ''}>Actual</option>
						<option value="4"  ${bean.map.ref_payment_option == 4? 'Selected': ''}>Less than bill amount</option>
						<option value="5" ${bean.map.ref_payment_option == 5? 'Selected': ''}>Expression Builder</option>
					</select>
				</td>
				<td class="formlabel">Value: </td>
				<td><input type="text" name="ref_payment_value" size="6" class="required validate-number"
					value="${bean.map.ref_payment_value}" title="Referral Payment Value is required/Invalid">
				</td>
				<td></td>
			</tr>
			<tr>
				<td style="display:none;" id="refExprLbl" class="formlabel">Expression: </td>
				<td colspan="3" style="display:none;" id="refExpr">
					<textarea name="ref_payment_expr" rows="4" cols="60" id="ref_payment_expr"
						<c:if test="${(roleId ne 1) && (roleId ne 2)}">readonly</c:if>
					><c:out value="${bean.map.ref_payment_expr}"/></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Prescribed Payment Type: </td>
				<td><select name="presc_payment_option" style="width:15em" onchange="onOptionChange()"  id="prescribingDoctorSelect" class="dropdown">
						<option value="1" ${bean.map.presc_payment_option==1 ? 'Selected':''}>Percentage</option>
						<option value="3"  ${bean.map.presc_payment_option==3? 'Selected':''} >Actual</option>
						<option value="4"  ${bean.map.presc_payment_option == 4? 'Selected': ''}>Less than bill amount</option>
						<option value="5"   ${bean.map.presc_payment_option == 5? 'Selected': ''}>Expression Builder</option>
					</select>
				</td>
				<td class="formlabel">Value: </td>
				<td><input type="text" name="presc_payment_value" size="6" class="required validate-number"
					value="${bean.map.presc_payment_value}" title="Prescribed Payment Value is required/Invalid">
				</td>
				<td></td>
			</tr>
			<tr>
				<td style="display:none;" id="prescExprLbl" class="formlabel">Expression: </td>
				<td colspan="3" style="display:none;" id="prescExpr">
					<textarea name="presc_payment_expr" rows="4" cols="60" id="presc_payment_expr"
						<c:if test="${(roleId ne 1) && (roleId ne 2)}">readonly</c:if>
					><c:out value="${bean.map.presc_payment_expr}"/></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Package Payment Value: </td>
				<td>
					<input type="text" name="dr_pkg_amt" size="6" class="required validate-number"
					 value="${bean.map.dr_pkg_amt}">
				</td>
				<td class="formlabel">Use Discount Amount:</td>
				<td colspan="2">
					<insta:selectoptions name="use_discounted_amount" value="${bean.map.use_discounted_amount}" opvalues="1,0" optexts="Yes,No"/>
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Specify whether the discounted amount or the pre discounted charge amount should be used to calculate the payout."/>
				</td>
			</tr>
		</table>
	</fieldset>

		<dl class="accordion" style="margin-bottom: 10px;">
			<dt>
				<span>Expression Help</span>
				<div class="clrboth"></div>
			</dt>
			<dd id="expr_tokens">
				<div class="bd">
					<table class="resultList">
						<tr>
							<td colspan="5"><b>Simple expression:</b> \${amount - discount - 10}</td>
						</tr>
						<tr>
							<td colspan="5"><b>Complex expression:</b> &lt;#if bill_type == 'C'&gt;\${amount}&lt;#else&gt;\${amount - 20}&lt;/#if&gt;</td>
						</tr>
						<tr>
							<td colspan="5"><b>Available Tokens:</b></td>
						</tr>

						<tr>
							<c:forEach items="${exprTokens}" var="token" varStatus="it">
							<c:if test="${(it.index % 5) == 0}">
							</tr><tr>
							</c:if>
							<td>${token}</td>
							</c:forEach>
						</tr>
					</table>
				</div>
			</dd>
		</dl>
	<div class="screenActions"><button type="button"
		onclick="return validateScreen();"  accesskey="S"><b><u>S</u></b>ave</button>&nbsp;|
	<c:if test="${param._method=='edit'}">
		<a href="#" onclick="window.location.href='${cpath}/master/PaymentRule.do?_method=add'">Add</a>&nbsp;|
	</c:if>
	<a href="${cpath}/master/PaymentRule.do?_method=list&sortOrder=precedance&sortReverse=false">Payment Rule List</a></div>
	<script>
		var laboratoryDetails=${laboratoryTestDetails};
		var radiologyTestDetails=${radiologyTestDetails};
		var serviceDetails=${serviceDetails};
		var chargeHeads = ${chargeHeads};
		var packageDetails = ${packageDetails};
		var chargeHeadId = '${bean.map.charge_head}';
		var activityId = '${bean.map.activity_id}';
		var cpath = '${cpath}';
	</script>
</form>

</body>
</html>
