<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers"
    value='<%=GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default()%>'
    scope="request" />
<html>
<head>
<title>Philhealth Monthly Mandatory Hospital Report- Insta HMS</title>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="valueDate">
	<fmt:formatDate pattern="dd-MM-yyyy  HH:mm" value="${currentDate}"/>
</c:set>
  <c:set var="mandatoryFieldsMissing" value="${(empty reportingMeta.accredition_no) or (empty reportingMeta.philhealth_tpaid_ohsrsdohgovph) or (empty reportingMeta.region) or (empty reportingMeta.hospital_name) or (empty reportingMeta.category) or (empty reportingMeta.address_street) or (empty reportingMeta.address_zip) or (empty reportingMeta.address_municipality) or (empty reportingMeta.address_province) or (empty reportingMeta.phic_bed_count) or (empty reportingMeta.doh_bed_count)}"/>

<script>
	function onInit() {
		document.getElementById('pd').checked = true;
		setDateRangeYesterday(document.inputform.fromDate,document.inputform.toDate);
	}

	function validateForm(format){
		document.inputform.format.value=format;
		if (validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) {
				return true;
		}
		return false;
	}
</script>
</head>
<body onload="onInit();">
	<div class="pageHeader">Philhealth Monthly Mandatory Hospital Report</div>

	<c:if test="${max_centers > 1 and centerId eq 0}">
		<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR" id="msgDiv">
			<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
			<div class="clrboth"></div>
			<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"><img src="${cpath}/images/error.png" /></div>
			<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;"><fmt:message key="ui.notification.consultation.select.center"/></div>
			<div class="clrboth"></div>
		</div>
	</c:if>
	<c:if test="${mandatoryFieldsMissing}">
		<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR" id="msgDiv">
			<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
			<div class="clrboth"></div>
			<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"><img src="${cpath}/images/error.png" /></div>
			<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;">
				Some of the mandatory fields are missing in center's reporting meta configuration. Ensure all the following fields are available in reporting meta:<br/><br/>
				philhealth_tpaid_ohsrsdohgovph<br/>
				accredition_no<br/>
				region<br/>
				hospital_name<br/>
				category<br/>
				address_street<br/>
				address_zip<br/>
				address_municipality<br/>
				address_province<br/>
				phic_bed_count<br/>
				doh_bed_count
			</div>
			<div class="clrboth"></div>
		</div>
	</c:if>
	<c:if test="${not (max_centers > 1 and centerId eq 0) and not mandatoryFieldsMissing }">
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="_method" value="getReport">
			<input type="hidden" name="format" value="pdf">
			<input type="hidden"  name="currDateTime"  id="currDateTime" value="${valueDate}"/>

			<div class="helpPanel">
				This is a mandatory hospital report to be submitted within the first ten (10) days of he following month.
			</div>
			<br/>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipDay" value="Y"/>
				<jsp:param name="skipWeek" value="Y"/>
				<jsp:param name="skipYear" value="Y"/>
			</jsp:include>
			<br/>
			<br/>
			<br/>
			<div style="margin-top: 10px">
				<button type="submit" onclick="return validateForm('pdf')">Generate</button>
			</div>
		</form>
	</c:if>
</body>
</html>

