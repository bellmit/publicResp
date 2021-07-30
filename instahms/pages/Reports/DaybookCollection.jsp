<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<html>
<head>
<title><insta:ltext key="billing.daybook.details.collectionsdaybook.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="/reports/daybook.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<script type="text/javascript">
var fromDate, toDate;
var roleId = '${roleId}';
var UsrCounterDayBookAccess = '${actionRightsMap.usr_or_counter_day_book_access}';
var counter_name = '${counter_name}';

</script>
<style>
input.number{text-align:left;}
</style>
</head>
<c:set var="textValues">
	<insta:ltext key="billing.daybook.details.pdf"/>,
	<insta:ltext key="billing.daybook.details.text"/>
</c:set>

<body onload="getPatientType();onInit();">
	<form method="POST">
		<div class="pageHeader">${requestScope.names} </div>
		<input type="hidden" name="_actionId" value="${actionId}"/>
		<div class="infoPanel">
			<div class="img"><img src="${cpath}/images/information.png"/></div>
			<div class="txt"><insta:ltext key="billing.daybook.details.longtemplate"/></div>
			<div style="clear: both"></div>
		</div>
		<insta:feedback-panel />
			<fieldset class="fieldSetBorder">
			<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="billing.daybook.details.daterange"/>:</td>
						<td>
							<select onchange="onChangeDateRange(this)" class="dropdown">
								<option>-- Select --</option>
								<option value="pd"><insta:ltext key="billing.daybook.details.yesterday"/></option>
								<option value="td"><insta:ltext key="billing.daybook.details.today"/></option>
								<option value="pm"><insta:ltext key="billing.daybook.details.previousmonth"/></option>
								<option value="tm"><insta:ltext key="billing.daybook.details.thismonth"/></option>
								<option value="py"><insta:ltext key="billing.daybook.details.previousfin.year"/></option>
								<option value="ty"><insta:ltext key="billing.daybook.details.thisfin.year"/></option>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="billing.daybook.details.from"/>:</td>
						<td><insta:datewidget name="fromDate"/>
							<input type="text" name="fromTime" size="4"
								value='<fmt:formatDate value="${serverNow}" pattern="HH:mm:ss"/>' class="number"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="billing.daybook.details.to"/>:</td>
						<td><insta:datewidget name="toDate"/>
							<input type="text" name="toTime" size="4"
								value='<fmt:formatDate value="${serverNow}" pattern="HH:mm:ss"/>' class="number"/>
						</td>

					</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.daybook.details.patienttype"/> :</td>
					<td>
						<input type="checkbox" name="patienttype" id="ip" value="ip" checked="checked" onclick="getPatientType()">
						<label><insta:ltext key="billing.daybook.details.ip"/></label>
						<input type="checkbox" name="patienttype" id="op" value="op" checked="checked" onclick="getPatientType()">
						<label><insta:ltext key="billing.daybook.details.op"/></label>
						<input type="checkbox" name="patienttype" id="others" value="others" checked="checked" onclick="getPatientType()">
						<label><insta:ltext key="billing.daybook.details.others"/></label>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><label><insta:ltext key="billing.daybook.details.billfrom"/>:<label></td>
					<td>	<input type="text" name="billFrom" id="billFrom" maxlength="15" style="width:10em" value="">
					<td class="formlabel"><label><insta:ltext key="billing.daybook.details.billto"/>:</label></td>
					<td><input type="text" name="billTo" id="billTo" maxlength="15" style="width:10em" value=""></td>
				</tr>
				<tr>
					<td class="formlabel"><label><insta:ltext key="billing.daybook.details.receiptfrom"/>:<label></td>
					<td>
							<input type="text" name="receiptFrom" id="receiptFrom" maxlength="15" style="width:10em" value="">
					</td>
					<td class="formlabel">
							<label><insta:ltext key="billing.daybook.details.receiptto"/>:</label>
					</td>
					<td>
							<input type="text" name="receiptTo" id="receiptTo" maxlength="15" style="width:10em" value="">
					</td>
			</tr>
			<c:if test="${counter_name!='' || roleId==1 || roleId==2 || actionRightsMap.usr_or_counter_day_book_access == 'A'}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.daybook.details.countername"/> :</td>
				<td>
				<c:choose>
					<c:when test="${roleId == 1 || roleId == 2 || actionRightsMap.usr_or_counter_day_book_access=='A'}">
						<select id='counterid' name="counterid" onchange="" class="dropdown">
							<option value="all"><insta:ltext key="billing.daybook.details.allcounters"/></option>
							<c:set var="billCounter" value="${sessionScope.billingcounterId}"></c:set>
							<c:set var="pharmacyCounter" value="${sessionScope.pharmacyCounterId}"></c:set>
							<c:forEach items="${counterList}" var="counter">
							<option value="${counter.map.counter_id}"
								<c:if test="${pharmacyCounter eq counter.map.counter_id}"><insta:ltext key="billing.daybook.details.selected"/></c:if>
								<c:if test="${billCounter eq counter.map.counter_id }"> <insta:ltext key="billing.daybook.details.selected"/></c:if> > ${counter.map.counter_no}
							</option>
							</c:forEach>

						</select>
					</c:when>
					<c:otherwise>
						<input type="hidden" name="counterid" id="counterid" value="${counter_id}"/>
						${counter_name}
					</c:otherwise>
					</c:choose>
				</td>
			</tr>
			</c:if>
					<tr>
						<td class="formlabel"><insta:ltext key="billing.daybook.details.username"/> :</td>
						<td>
						<c:choose>
							<c:when test="${roleId == 1 || roleId == 2 || actionRightsMap.usr_or_counter_day_book_access=='A'}">
								<select name="counterUserName" id="counterUserName" class="dropdown">
									<option value="all"><insta:ltext key="billing.daybook.details.allusers"/></option>
									<c:forEach items="${users}" var="counterUser">
									<option value="${counterUser.EMP_USERNAME }"
									<c:if test="${counterUser.EMP_USERNAME eq sessionScope.userid }"><insta:ltext key="billing.daybook.details.selected"/></c:if>
									>${counterUser.EMP_USERNAME }</option>
									</c:forEach>
								</select>
							</c:when>
							<c:otherwise>
								<input type="hidden" name="counterUserName" id="counterUserName" value="${ifn:cleanHtmlAttribute(user_name)}"/>
								${ifn:cleanHtml(user_name)}
							</c:otherwise>
						</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="billing.daybook.details.paymentmode"/> :</td>
						<td>
							<insta:selectdb name="payMode" id="payMode" table="payment_mode_master"
								displaycol="payment_mode" valuecol="payment_mode"
								dummyvalue="All Payment Modes" dummyvalueId="all" orderby="displayorder" filtered="false"/>
						</td>
					</tr>
					<c:choose>
						<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
							<tr>
								<td align="right"><insta:ltext key="billing.daybook.details.center"/>:</td>
								<td>
									<insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master" valuecol="center_id" 
									   displaycol="center_name" value="${centerId}" filtered="false"/>
								</td>
							</tr>
						</c:when>
						<c:otherwise>
								<input type="hidden"  name="centerFilter"  id="centerFilter" value="${centerId}"/>
						</c:otherwise>
					</c:choose>
				</table>
			</fieldset>
		<table class="screenActions">
			<tr>
				<td>
					<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="${textValues}" style="width: 5em" />
					<button type="button" accesskey="G" id="print"  name="print" onclick="getReport()" class="button">
					<u><b><insta:ltext key="billing.daybook.details.g"/></b></u><insta:ltext key="billing.daybook.details.eneratereport"/></button>&nbsp;
				</td>
				<td style="width: 5em;">
				</td>
			</tr>
		</table>
		<table>
			<tr>
				<td align="center" colspan="4" height="25"><font face="Arial" style="font-size: 11"><jsp:include page="/pages/frame/footer.jsp"/></font></td>
			</tr>
		</table>

</form>
</body>
</html>
