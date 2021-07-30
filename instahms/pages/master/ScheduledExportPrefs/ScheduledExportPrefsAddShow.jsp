<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Scheduled Export Preferences - Insta HMS</title>
	<script>
		function toggleControls() {
			var period = document.getElementById('period').value;
			if (period == '') {
				alert("Please select the Period");
				document.getElementById('period').focus();
				return false;
			} else if (period == 'H') {
				document.getElementById('daily_or_weekly_time').value = '';
				document.getElementById('daily_or_weekly_time').disabled = true;
				document.getElementById('hourly_time').disabled = false;
				document.getElementById('weekly_on').value = '';
				document.getElementById('weekly_on').disabled = true;

			} else if (period == 'D') {
				document.getElementById('daily_or_weekly_time').value = '';
				document.getElementById('daily_or_weekly_time').disabled = false;
				document.getElementById('hourly_time').disabled = true;
				document.getElementById('weekly_on').value = '';
				document.getElementById('weekly_on').disabled = true;

			} else if (period == 'W') {
				document.getElementById('daily_or_weekly_time').value = '';
				document.getElementById('daily_or_weekly_time').disabled = false;
				document.getElementById('hourly_time').disabled = true;
				document.getElementById('weekly_on').value = '';
				document.getElementById('weekly_on').disabled = false;
			}

		}
		function validateForm() {
			var period = document.getElementById('period').value;
			var hourlyTime = document.getElementById('hourly_time');
			var dailyOrWeeklyTime = document.getElementById('daily_or_weekly_time');
			if (document.getElementById('schedule_name').value == '') {
				alert('Please enter the Schedule Name');
				document.getElementById('schedule_name').focus();
				return false;
			}
			var exportFor = document.getElementById('exportFor').value;
			if (exportFor == '') {
				alert("Please select the export for");
				document.getElementById('exportFor').focus();
				return false;
			}
			var exportType = document.getElementById('export_type').value;
			if (exportType == '') {
				alert("Please select the Export Type");
				document.getElementById('export_type').focus();
				return false;
			}
			var targetUrl = document.getElementById('target_url').value;
			if (targetUrl == '') {
				alert("Please enter the Target URL");
				document.getElementById('target_url').focus();
				return false;
			}
			if (period == '') {
				alert("Please select the period");
				document.getElementById('period').focus();
				return false;
			}
			if (period == 'H') {
				if (hourlyTime.value == '') {
					alert("Please enter the minutes 0-59");
					hourlyTime.focus();
					return false;
				}
				if (isNaN(hourlyTime.value)) {
					alert("Please enter the minutes 0-59");
					hourlyTime.focus();
					return false;
				}

			}
			if (period == 'D') {
				if (dailyOrWeeklyTime.value == '') {
					alert("Please enter the Time");
					dailyOrWeeklyTime.focus();
					return false;
				}
				if (!validateTime(dailyOrWeeklyTime)) {
					return false;
				}
			}
			if (period == 'W') {
				if (document.getElementById('weekly_on').value == '') {
					alert("Please select the Weekly on which day to export");
					document.getElementById('weekly_on').focus();
					return false;
				}
				if (dailyOrWeeklyTime.value == '') {
					alert("Please enter the Time.");
					dailyOrWeeklyTime.focus();
					return false;
				}
				if (!validateTime(dailyOrWeeklyTime)) {
					return false;
				}
			}
			if (document.getElementById('directory').value == '') {
				alert("Please enter Export base directory");
				document.getElementById('directory').focus();
				return false;
			}
			if (document.getElementById('voucher_date').value != '') {
				if (!doValidateDateField(document.getElementById('voucher_date'))) {
					return false;
				}
			}
		}
		function init() {
			// if there only one option in the exportFor dropdown, then set that as the default.
			if (document.getElementById('exportFor').options.length == 2) {
				document.getElementById('exportFor').options.selectedIndex = 1;
			}
		}
	</script>
</head>
<body onload="init();">
	<h1>Scheduled Export Preferences</h1>
	<insta:feedback-panel/>
	<form action="ScheduledExportPrefs.do">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
		<input type="hidden" name="schedule_id" value="${bean.map.schedule_id}"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Schedule Name: </td>
					<td><input type="text" name="schedule_name" id="schedule_name" value="${bean.map.schedule_name}"/>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Export Type: </td>
					<td>
						<select class="dropdown" name="export_type" id="export_type">
							<option value="">-- Select --</option>
							<option value="focus" ${bean.map.export_type == 'focus' ? 'selected' : ''}>Focus</option>
							<option value="tally" ${bean.map.export_type == 'tally' ? 'selected' : ''}>Tally</option>
						</select>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Target Url: </td>
					<td><input type="text" name="target_url" id="target_url" value="${bean.map.target_url}"/>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Period: </td>
					<td><select name="period" id="period" onchange="toggleControls();" class="dropdown">
							<option value="">-- Select --</option>
							<option value="H" ${bean.map.period == 'H' ? 'selected' : ''}>Hourly</option>
							<option value="D" ${bean.map.period == 'D' ? 'selected' : ''}>Daily</option>
							<option value="W" ${bean.map.period == 'W' ? 'selected' : ''}>Weekly</option>
						</select>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Minutes(0-59): </td>
					<td><input type="text" name="hourly_time" id="hourly_time" value="${bean.map.hourly_time}" class="timefield"/></td>
				</tr>
				<tr>
					<td class="formlabel">Time(HH:mm): </td>
					<td><input type="text" name="daily_or_weekly_time" id="daily_or_weekly_time"
							value="<fmt:formatDate pattern='HH:mm' value='${bean.map.daily_or_weekly_time}'/>" class="timefield"/> (24 Hour format)</td>
				</tr>
				<tr>
					<td class="formlabel">Day of Week : </td>
					<td>
						<select name="weekly_on" id="weekly_on" class="dropdown">
							<option value="">-- Select --</option>
							<option value="Sun" ${bean.map.weekly_on == 'Sun' ? 'selected' : ''}>Sunday</option>
							<option value="Mon" ${bean.map.weekly_on == 'Mon' ? 'selected' : ''}>Monday</option>
							<option value="Tue" ${bean.map.weekly_on == 'Tue' ? 'selected' : ''}>Tuesday</option>
							<option value="Wed" ${bean.map.weekly_on == 'Wed' ? 'selected' : ''}>Wednesday</option>
							<option value="Thu" ${bean.map.weekly_on == 'Thu' ? 'selected' : ''}>Thursday</option>
							<option value="Fri" ${bean.map.weekly_on == 'Fri' ? 'selected' : ''}>Friday</option>
							<option value="Sat" ${bean.map.weekly_on == 'Sat' ? 'selected' : ''}>Saturday</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Export For: </td>
					<td>
						<c:set var="bCenterId" value="C${bean.map.center_id}"/>
						<c:set var="bAccountGroup" value="A${bean.map.account_group}"/>
						<select name="exportFor" id="exportFor" class="dropdown">
							<option value="">--Select--</option>
							<c:if test="${acc_prefs.map.all_centers_same_comp_name != 'Y'}">
								<c:forEach items="${centers}" var="center">
									<c:set var="mCenterId" value="C${center.map.center_id}"/>
									<option value="C${center.map.center_id}" ${bCenterId == mCenterId ? 'selected' : ''}>
										<c:set var="CCompanyName" value=""/>
										<c:if test="${not empty center.map.accounting_company_name}">
											<c:set var="CCompanyName" value="-(${center.map.accounting_company_name})"/>
										</c:if>
										${center.map.center_name}${CCompanyName}
									</option>
								</c:forEach>
							</c:if>
							<c:forEach items="${accGroups}" var="accGroup">
								<c:choose>
									<c:when test="${acc_prefs.map.all_centers_same_comp_name != 'Y' && accGroup.map.account_group_id == 1}">
										<!-- do not display the hospital account group -->
									</c:when>
									<c:otherwise>
										<c:set var="mAccountGroup" value="A${accGroup.map.account_group_id}"/>
										<option value="A${accGroup.map.account_group_id}" ${bAccountGroup == mAccountGroup ? 'selected' : ''}>
											<c:set var="ACompanyName" value=""/>
											<c:if test="${not empty accGroup.map.accounting_company_name}">
												<c:set var="ACompanyName" value="-(${accGroup.map.accounting_company_name})"/>
											</c:if>
											${accGroup.map.account_group_name}${ACompanyName}
										</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select></td>
				</tr>
				<tr>
					<td class="formlabel">Export base directory : </td>
					<td><input type="text" name="directory" id="directory" value="${bean.map.directory}"></td>
				</tr>
				<tr>
					<td class="formlabel">Voucher Date</td>
					<td>
						<fmt:formatDate value="${bean.map.voucher_date}" var="voucher_date" pattern="dd-MM-yyyy"/>
						<insta:datewidget name="voucher_date" id="voucher_date" value='${voucher_date}'/>
					</td>
				</tr>
			</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLable">Export Items</legend>
				<c:set var="exportItems" value="${fn:split(bean.map.export_items, ',')}"/>
				<div style="float: left">
					<input type="checkbox" name="exportItems" id="exportItems"
						${fn:length(exportItems) == 0 or fn:length(exportItems) == 1 ? 'checked' : ''}
						value="" onclick="enableCheckGroupAll(this)">All <br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Bills"
						${ifn:arrayFind(exportItems, 'Bills') != -1 ? 'checked' : ''}>Bills<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Receipts"
						${ifn:arrayFind(exportItems, 'Receipts') != -1 ? 'checked' : ''}>Receipts<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Deposits"
						${ifn:arrayFind(exportItems, 'Deposits') != -1 ? 'checked' : ''}>Deposits<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Payment Vouchers"
						${ifn:arrayFind(exportItems, 'Payment Vouchers') != -1 ? 'checked' : ''}>Payment Vouchers<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Payments Due"
						${ifn:arrayFind(exportItems, 'Payments Due') != -1 ? 'checked' : ''}>Payments Due<br/>
				</div>
				<div class="float: left">
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Invoices"
						${ifn:arrayFind(exportItems, 'Stores Invoices') != -1 ? 'checked' : ''}>Stores Invoices<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Returns with Debit"
						${ifn:arrayFind(exportItems, 'Stores Returns with Debit') != -1 ? 'checked' : ''}>Stores Returns with Debit<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Consignment Stock Issued"
						${ifn:arrayFind(exportItems, 'Stores Consignment Stock Issued') != -1 ? 'checked' : ''}>Stores Consignment Stock Issued<br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Consignment Stock Returns"
						${ifn:arrayFind(exportItems, 'Stores Consignment Stock Returns') != -1 ? 'checked' : ''}>Stores Consignment Stock Returns
				</div>
				<div style="clear: both"/>

			</fieldset>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save" onclick="return validateForm();"/>
						<c:if test="${param._method == 'show'}">
							| <a href="ScheduledExportPrefs.do?_method=add">Add</a>
						</c:if>
						| <a href="ScheduledExportPrefs.do?_method=list&sortOrder=schedule_name&sortReverse=false">List</a>
					</td>
				</tr>
			</table>
	</form>
</body>
</html>