<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="registration.categorychange.details.patientcategorychange"/></title>
	<insta:link type="css" file="hmsNew.css"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="registration/editPatientVisit.js"/>
	<script>
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var categoryExpirydateText = '${ifn:cleanJavaScript(regPrefFields.categoryExpiryDate)}';

		function categoryChange() {
			document.mainform.method.value = "editCategoryDetails";
			document.mainform.submit();
		}

		function save() {

			if (document.mainform.org_id.value == "") {
				showMessage("js.registration.editpatientcategory.select.rateplan");
				document.mainform.org_id.focus();
				return false;
			}

			/**
			if (categoryExpirydateText != null && categoryExpirydateText != '') {
				var date =  getDate("category_expiry_date");
				var d = new Date(gServerNow);
				d.setHours(0);
				d.setMinutes(0);
				d.setSeconds(0);
				d.setMilliseconds(0);

				var diff = d - date;

				if (diff > 0) {
					alert(categoryExpirydateText + " should be more than Currentdate");
					document.mainform.category_expiry_date.focus();
					return false;
				}
			}*/
			if (!checkRatePlanValidity()) return false;

			document.mainform.submit();
		}
	</script>
	<insta:js-bundle prefix="registration.editvisitdetails"/>
	<insta:js-bundle prefix="registration.editpatientcategory"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body>
<div class="pageHeader"><insta:ltext key="registration.categorychange.details.patientcategorychange"/></div>
<c:set var="changeRateplan">
		<insta:ltext key="billing.patientbill.details.changerateplan.bedtype"/>
		</c:set>
<insta:feedback-panel/>
<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
						<insta:ltext key="registration.categorychange.details.template1"/><br/>
						<insta:ltext key="registration.categorychange.details.template2"/><br/>
				</td>
			</tr>
		</table>
	</div>
<insta:patientdetails  visitid="${param.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.categorychange.details.otherdetails"/></legend>
	<table class="patientdetails" style="width:100%">
		 <fmt:formatDate var="rdate" value="${patient.reg_date}" pattern="dd-MM-yyyy"/>
		 <fmt:formatDate var="rtime" value="${patient.reg_time}" pattern="HH:mm"/>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.categorychange.details.reg"/>&nbsp;<insta:ltext key="registration.categorychange.details.date.and.time"/></td>
			<td class="forminfo"> ${rdate} ${rtime}</td>
			<c:choose>
		        <c:when test="${not empty regPrefFields.oldRegNumField}">
		 	       	<td class="formlabel">${ifn:cleanHtml(regPrefFields.oldRegNumField)}:</td>
		           	<td class="forminfo"><label id="oldmrnoLabel"></label></td>
		           	<td class="formlabel"></td>
					<td></td>
		       </c:when>
		       <c:otherwise>
		       		<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
		       </c:otherwise>
	       </c:choose>
       </tr>
	</table>
</fieldset>
<form name="mainform" method="post" action="PatientCategoryChange.do">
<input type="hidden" name="reg_date" value="${rdate}">
<input type="hidden" name="reg_time" value="${rtime}">
<input type="hidden" name="method" value="saveCategory">
<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />

	<table class="formtable">
		<tr>
			<td class="formlabel">${ifn:cleanHtml(regPrefFields.patientCategory)}: </td>
			<td>
				<select name="patient_category_id" id="patient_category_id" class="dropdown">
					<c:forEach items="${categories_list}" var="category">
						<option value="${category.map.category_id}" ${category.map.category_id == patient_category ? 'selected' : ''}>
							${category.map.category_name}
						</option>
					</c:forEach>
				</select>
			</td>
			<%--
			<c:choose>
				<c:when test="${not empty regPrefFields.categoryExpiryDate}">
					<td class="formlabel">${regPrefFields.categoryExpiryDate}:</td>
						<fmt:formatDate value="${visitbean.category_expiry_date}" var="expDate" pattern="dd-MM-yyyy"/>
						<c:choose>
							<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
								<td>
									<insta:datewidget name="category_expiry_date" id="category_expiry_date"
										valid="future" btnPos="left" value="${expDate}"/>
								</td>
							</c:when>
							<c:otherwise>
								<td class="forminfo">${expDate}
									<input type="hidden" name="category_expiry_date" id="category_expiry_date" value="${expDate}">
								</td>
							</c:otherwise>
						</c:choose>
					</td>
				</c:when>
				<c:otherwise>
					<td></td>
					<td></td>
				</c:otherwise>
			</c:choose>
			--%>

			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>

		<tr>
			<td class="formlabel"><insta:ltext key="registration.categorychange.details.rateplan"/>:</td>
			<c:set var="ratePlan" value="${not empty visitbean.org_id ? visitbean.org_id : defaultRatePlanId}"/>
			<c:set var="allowedRatePlans" value="${visitbean.visit_type == 'i'? ip_allowedRatePlans : op_allowedRatePlans }"/>
			<td>
				<c:choose>
					<c:when test="${not empty allowedRatePlans}">
						<select name="org_id" class="dropdown">
							<option value="">--Select--</option>
							<c:forEach items="${allowedRatePlans}" var="rp">
								<option value="${rp.map.org_id}" ${rp.map.org_id == ratePlan?'selected':''}>${rp.map.org_name}</option>
							</c:forEach>
						</select>
					</c:when>
					<c:otherwise>
						<insta:selectdb name="org_id" displaycol="org_name" value="${ratePlan}"
						table="organization_details" valuecol="org_id" dummyvalue="--Select--"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</table>

	<div class="screenActions">
		<button type="button" name="Save" accesskey="S" onclick="save()" ${(patient.op_type == 'M' || patient.op_type == 'O')?'':'disabled'}><b><u><insta:ltext key="registration.categorychange.details.s"/></u></b><insta:ltext key="registration.categorychange.details.ave"/></button>
		| <a href="${pageContext.request.contextPath}/pages/registration/ManageVisits/PatientCategoryChange.do?method=editCategoryDetails&patient_id=${ifn:cleanURL(param.patient_id)}"><insta:ltext key="registration.categorychange.details.reset"/></a>
		| <a href="${pageContext.request.contextPath}/pages/registration/editvisitdetails.do?_method=getPatientVisitDetails&patient_id=${ifn:cleanURL(param.patient_id)}&ps_status=active"><insta:ltext key="registration.categorychange.details.editvisitdetails"/></a>
		<c:if test="${patient.visit_type == 'o' && patient.use_perdiem == 'N'}">
			|<a href="${cpath}/insurance/showInsuranceDetails.htm?visitId=${ifn:cleanURL(patient.patient_id)}"><insta:ltext key="registration.patient.label.add.or.editinsurance"/></a>
		</c:if>
		<c:if test="${patient.visit_type == 'i' || patient.use_perdiem == 'Y'}">
			|<a href="${pageContext.request.contextPath}/editVisit/changeTPA.do?_method=changeTpa&visitId=${ifn:cleanURL(patient.patient_id)}"><insta:ltext key="registration.categorychange.details.add.or.editinsurance"/></a>
		</c:if>
		<insta:screenlink screenId="change_visit_org" addPipe="true" label="${changeRateplan}"
			extraParam="?_method=updateRatePlan&visitId=${param.patient_id}&billNo=" title="Add/Edit Rate Plan."/>
	</div>
</form>
</body>
</html>
