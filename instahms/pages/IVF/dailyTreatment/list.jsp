<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Daily Treatment Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/ivfsessions.js" />
	<insta:link type="script" file="ivf/dailytreatment.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="css">

	</style>
	<script>
	var toolbar = {
		Edit: {
			title: "Edit Daily Treatment",
			imageSrc: "icons/Edit.png",
			href: '/IVF/IVFDailyTreatment.do?_method=show',
			onclick: null,
			description: "Edit Daily Treatment"
		}
	};
	function init() {
		createToolbar(toolbar);
	}
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="init();" class="yui-skin-sam">
<div class="pageHeader">Daily Treatment Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" />
<form name="DailyTreatment" method="post" action="${cpath}/IVF/IVFDailyTreatment.do" autocomplete="off">
<input type="hidden" name="_method" value="updateCycleStatus"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Daily Treatment</legend>
		<table border="0" class="resultList"" cellpadding="0" cellspacing="0" width="100%" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();" class="header">
				<th width="10%">Date</th>
				<th width="8%">Day</th>
				<th width="8%">Doctor</th>
				<th colspan="2" width="8%">Left Ovary</th>
				<th colspan="2" width="8%">Right Ovary</th>
				<th width="8%">Endometrium</th>
				<th width="8%">FSH</th>
				<th width="8%">P4</th>
				<th width="8%">E2</th>
				<th width="8%">LH</th>
				<th width="8%">TSH</th>
				<th width="10%">Medication</th>
			</tr>
			<tr class="header">
				<th colspan="3" width="10%"></th>
				<th width="7%">Size</th>
				<th width="7%">Number</th>
				<th width="7%">Size</th>
				<th width="7%">Number</th>
				<th colspan="6" width="50%"></th>
				<th width="10%">Medicine-(Dosage)-[Frequency]</th>
			</tr>
			</tr>
			<c:forEach var="dt" items="${dailyTreatmentlist}" varStatus="st">
				<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', patient_id: '${ifn:cleanJavaScript(param.patient_id)}',ivf_cycle_id: '${ifn:cleanJavaScript(param.ivf_cycle_id)}',
						ivf_cycle_daily_id: '${dt.map.ivf_cycle_daily_id}',start_date:'${ifn:cleanJavaScript(param.start_date)}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td width="10%">${dt.map.treatment_date}</td>
					<td>${dt.map.treatment_days_from_start}</td>
					<td>${dt.map.doctor_name}</td>
					<td colspan="2" width="8%">
						<table class="formtable" width="100%">
							<c:forEach var="lft" items="${follicleList}">
								<c:if test = "${lft.map.ivf_cycle_daily_id == dt.map.ivf_cycle_daily_id}">
									<c:if test="${lft.map.ovary_position == 'L'}">
									<tr>
										<td width="35%">${lft.map.follicles_size}</td>
										<td width="65%">${lft.map.follicles_count}</td>
									</tr>
									</c:if>
								</c:if>
							</c:forEach>
						</table>
					</td>
					<td colspan="2" width="8%">
						<table class="formtable" width="100%">
							<c:forEach  var="rht" items="${follicleList}">
								<c:if test="${rht.map.ivf_cycle_daily_id==dt.map.ivf_cycle_daily_id}">
									<c:if test="${rht.map.ovary_position == 'R'}">
										<tr>
											<td width="35%">${rht.map.follicles_size}</td>
											<td width="65%">${rht.map.follicles_count}</td>
										</tr>
									</c:if>
								</c:if>
							</c:forEach>
						</table>
					</td>
					<td width="8%">${dt.map.endometrium_thickness}</td>
					<td width="8%">${dt.map.fsh_value}</td>
					<td width="8%">${dt.map.p4_value}</td>
					<td width="8%">${dt.map.e2_value}</td>
					<td width="8%">${dt.map.lh_value}</td>
					<td width="8%">${dt.map.tsh_value }</td>
					<td width="8%">
						<table class="formtable" width="100%">
							<c:forEach var="med" items="${prescriptionList}">
								<c:if test="${med.map.ivf_cycle_daily_id==dt.map.ivf_cycle_daily_id}">
									<tr>
										<td>
											<insta:truncLabel value="${med.map.item_name}-(${med.map.medicine_dosage})-[${med.map.display_name}]" length="40"/>
										</td>
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div class="screenActions" align="left">
		<input type="checkbox" name="dailytreatCompleted" id="dailytreatCompleted"
			${dailyTreatCompleted=='D'||dailyTreatCompleted=='C'?'checked':''}
			${dailyTreatCompleted=='D'||dailyTreatCompleted=='C'?'disabled':''}/> Daily Treatment Completed
		| <input type="submit" name="save" id="save" value="Save"/>
		| <a href="${cpath}/IVF/IVFDailyTreatment.do?_method=add&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}&start_date=${ifn:cleanURL(param.start_date)}">Add Daily Treatment Details</a>
		| <a href="${cpath}/IVF/IVFPreCycle.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}&start_date=${ifn:cleanURL(param.start_date)}">Pre Cycle</a>
	</div>
</form>
</body>
</html>