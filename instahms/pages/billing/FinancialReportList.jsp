<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="revUrl" value="${cpath}/billing/RevenueReport.do"/>
<c:set var="colUrl" value="${cpath}/billing/CollectionReport.do"/>
<c:set var="paymentUrl" value="${cpath}/billing/PaymentsReport.do"/>
<c:set var="rtVarUrl" value="${cpath}/billing/RateVariationReport.do"/>

<head>
	<title>Financial Reports - Insta HMS</title>
	<insta:link type="style" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>

	<script>
		var fromDate, toDate;

		var groupNames = { dept_name : "Department", chargehead_name: "Charge Head", ac_head: "Account Head",
			chargegroup_name: "Charge Group", doctor_name: "Doctor", referer: "Referer", tpa_name: "TPA/Sponsor",
			ward_name: "Ward", org_name: "Rate Plan", visit_type_name: "Patient Type",
			payment_mode_name: "Payment Mode", combo_type: "Payment Type",
			patient_name_id: "Patient", payee_name: "Referral Doctor"
		};

		function doReport(url, method, groupBy,filterBy, reportType,whichReport) {
			var valid = validateFromToDate(fromDate, toDate);
			if (!valid) return false;
            document.inputform.method.value = method;
			if(whichReport=='billing'){
			document.inputform.dataOfview.value = groupBy;
			document.inputform.groupBy.value="dept_name";
			}else{
			document.inputform.groupBy.value = groupBy;
			document.inputform.filterBy.value = filterBy;
			document.inputform.docName.value = filterBy; //docName is needed for tabular reports
			}
			document.inputform.groupByName.value = groupNames[groupBy];
			if (reportType != null) {
				document.inputform.reportType.value= reportType;
				document.inputform.trendPeriod.value = reportType;
			}
			document.inputform.action = url;
			if(document.inputform.filterBy.value=='undefined')
				document.inputform.filterBy.value='';
			if(document.inputform.docName.value=='undefined')
				document.inputform.docName.value='';

			document.inputform.submit();
			return false;
		}

		function doRevenueReport(reportType, groupBy, trendPeriod) {
			var valid = validateFromToDate(fromDate, toDate);
			if (!valid) return false;

			document.inputform.method.value = 'showReport';
			document.inputform.action = '${revUrl}';
			document.inputform.groupBy.value = groupBy;
			document.inputform.reportType.value= reportType;
			document.inputform.trendPeriod.value = trendPeriod;
			if(document.inputform.filterBy.value=='undefined')
				document.inputform.filterBy.value='';
			if(document.inputform.docName.value=='undefined')
				document.inputform.docName.value='';

			document.inputform.submit();
			return false;
		}

		function doCollectionReport(reportType, groupBy, trendPeriod) {
			var valid = validateFromToDate(fromDate, toDate);
			if (!valid) return false;

			document.inputform.method.value = 'showReport';
			document.inputform.action = '${colUrl}';
			document.inputform.groupBy.value = groupBy;
			document.inputform.reportType.value= reportType;
			document.inputform.trendPeriod.value = trendPeriod;
			document.inputform.filterBy.value = '';
			if(document.inputform.filterBy.value=='undefined')
				document.inputform.filterBy.value='';
			if(document.inputform.docName.value=='undefined')
				document.inputform.docName.value='';

			document.inputform.submit();
			return false;
		}

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			setDateRangeYesterday(fromDate, toDate);
		}

		function onChangeDateRange(sel) {
			var type = sel.value;
			if (type == "pd") { setDateRangeYesterday(fromDate, toDate); }
			else if (type == "td") { setDateRangeToday(fromDate, toDate); }
			else if (type == "pm") { setDateRangePreviousMonth(fromDate, toDate); }
			else if (type == "tm") { setDateRangeMonth(fromDate, toDate); }
			else if (type == "py") { setDateRangePreviousFinancialYear(fromDate, toDate); }
			else if (type == "ty") { setDateRangeFinancialYear(fromDate, toDate); }
			sel.selectedIndex = 0;
		}
	</script>

	<style type="text/css">
		table.dashboard td { text-align: center; }
		table#dates td {padding-left: 8px}
	</style>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Financial Reports</div>

		<form name="inputform" method="GET" action="" target="_blank">
			<input type="hidden" name="method" value="summaryReport">
			<input type="hidden" name="groupBy">
			<input type="hidden" name="filterBy">
			<input type="hidden" name="docName">
			<input type="hidden" name="dataOfview">
			<input type="hidden" name="groupByName">
			<input type="hidden" name="reportType">
			<input type="hidden" name="trendPeriod">

			<table id="dates" align="center">
				<tr>
					<td>
						<select onchange="onChangeDateRange(this)">
							<option>-- Select Dates --</option>
							<option value="pd">Yesterday</option>
							<option value="td">Today</option>
							<option value="pm">Previous Month</option>
							<option value="tm">This Month</option>
							<option value="py">Previous Fin. Year</option>
							<option value="ty">This Fin. Year</option>
						</select>
					</td>
					<td>
						<table>
							<tr>
								<td align="right">From:</td>
								<td><insta:datewidget name="fromDate"/></td>
								<td align="right">To:</td>
								<td><insta:datewidget name="toDate"/></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

			<div class="stwMain">
				<div class="stwHeader stwClosed" id="revenue"
					onclick="stwToggle(this);"><label>Revenue Reports</label></div>
				<div class="stwContent stwHidden" id="revenue_content">
					<table class="dashboard">
						<tr>
							<th>About This Report</th>
							<th>Tabular Summary</th>
							<th>Bill-wise Details</th>
							<th>Item-wise Details</th>
							<th>Daily Trend</th>
							<th>Weekly Trend</th>
							<th>Monthly Trend</th>
						</tr>

						<c:forEach var="groupBy" items="dept_name,chargehead_name,chargegroup_name,ac_head,doctor_name,referer,tpa_name,ward_name,org_name,visit_type_name,patient_name_id">
							<tr>
								<td style="text-align:left">
									<c:choose>
										<c:when test="${groupBy == 'dept_name'}">
											<label title="This report shows department-wise grouping of revenue">
												Department wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'chargehead_name'}">
											<label title="This report shows Charge Head wise grouping of revenue">
												Charge Head wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'chargegroup_name'}">
											<label title="This report shows Charge Group wise grouping of revenue">
												Charge Group wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'ac_head'}">
											<label title="This report shows Account Head wise grouping of revenue">
												Account Head wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'doctor_name'}">
											<label title="This report shows Doctor wise grouping of revenue">
												Doctor wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'referer'}">
											<label title="This report shows Referer wise grouping of revenue">
												Referral Wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'tpa_name'}">
											<label title="This report shows TPA/Sponsor wise grouping of revenue">
												TPA/Sponsor wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'ward_name'}">
											<label title="This report shows Ward wise grouping of revenue">
												Ward wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'org_name'}">
											<label title="This report shows Rate Plan wise grouping of revenue">
												Rate Plan wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'visit_type_name'}">
											<label title="This report shows Patient Type Type wise grouping of revenue">
												Patient Type wise Revenue Report</label>
										</c:when>
										<c:when test="${groupBy == 'patient_name_id'}">
											<label title="This report shows Patient Type wise grouping of revenue">
												Patient wise Revenue Report</label>
										</c:when>
									</c:choose>
								</td>
								<td>
									<c:choose><c:when test="${groupBy == 'visit_type_name'}">
										NA
									</c:when><c:otherwise>
										<a href="" onclick="return doRevenueReport('summary','${groupBy}')">Print</a>
									</c:otherwise></c:choose>
								</td>
								<td><a href="" onclick="return doRevenueReport('billWise', '${groupBy}')">Print</a></td>
								<td><a href="" onclick="return doRevenueReport('itemWise', '${groupBy}')">Print</a></td>
								<td><a href="" onclick="return doRevenueReport('trend','${groupBy}','day')">Print</a></td>
								<td><a href="" onclick="return doRevenueReport('trend','${groupBy}','week')">Print</a></td>
								<td><a href="" onclick="return doRevenueReport('trend','${groupBy}','month')">Print</a></td>
							</tr>
						</c:forEach>
					</table>
				</div>
			</div>

			<div class="stwMain">
				<div class="stwHeader stwClosed" id="collection"
					onclick="stwToggle(this);"><label>Collection Reports</label></div>
				<div class="stwContent stwHidden" id="collection_content">
					<table class="dashboard">
						<tr>
							<th>About This Report</th>
							<th>Tabular Summary</th>
							<th>Receipt-wise Details</th>
							<th>Daily Trend</th>
							<th>Weekly Trend</th>
							<th>Monthly Trend</th>
						</tr>

						<c:forEach var="groupBy" items="payment_mode_name,visit_type_name,combo_type">
							<tr>
								<td style="text-align:left">
									<c:choose>
										<c:when test="${groupBy == 'payment_mode_name'}">
											<label title="This report shows payment mode wise grouping of collection">
												Payment Mode wise Collection Report
											</label>
										</c:when>
										<c:when test="${groupBy == 'visit_type_name'}">
											<label title="This report shows patient type wise grouping of collection">
												Patient Type wise Collection Report
											</label>
										</c:when>
										<c:when test="${groupBy == 'combo_type'}">
											<label title="This report shows Payment Detail Type wise grouping of collection">
												Payment Detail Type wise Collection Report
											</label>
										</c:when>
									</c:choose>
								</td>
								<td><a href="" onclick="return doCollectionReport('summary', '${groupBy}')">Print</a></td>
								<td><a href="" onclick="return doCollectionReport('detail', '${groupBy}')">Print</a></td>
								<td><a href="" onclick="return doCollectionReport('trend', '${groupBy}', 'day')">Print</a>
								<td><a href="" onclick="return doCollectionReport('trend', '${groupBy}', 'week')">Print</a>
								<td><a href="" onclick="return doCollectionReport('trend', '${groupBy}', 'month')">Print</a>
							</tr>
						</c:forEach>
					</table>
				</div>
			</div>

			<div class="stwMain">
				<div class="stwHeader stwClosed" id="payments"
					onclick="stwToggle(this);"><label>Payment  Reports</label></div>
				<div class="stwContent stwHidden" id="payments_content">
					<table class="dashboard">
						<tr>
							<th>About This Report</th>
							<th>Tabular Summary</th>
							<th>Charge-wise Details</th>
							<th>Daily Trend</th>
							<th>Weekly Trend</th>
							<th>Monthly Trend</th>
						</tr>

						<tr>
							<td style="text-align:left">
								<label title="This report shows doctor wise grouping of payments">
									Conducting Doctor - wise Payments Report
								</label>
							</td>
							<td>
								<a href="" onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport',
									'All', 'doctor_id', 'dashboard','payments')">
									Print</a>
							</td>
							<td>
								<a href="" onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport',
									'All', 'doctor_id', 'summary','payments')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">
								<label title="This report shows doctor wise grouping of payments">
									Prescribing Doctor - wise Payments Report
								</label>
							</td>
							<td>
								<a href="" onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport',
									'All', 'prescribing_dr_id', 'dashboard','payments')">
									Print</a>
							</td>
							<td>
								<a href="" onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport',
									'All','prescribing_dr_id', 'summary','payments')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">Referral Doctor wise Payments  Report</td>
							<td>
								<a href=""
									onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport', 'All','reference_docto_id',
									 'dashboard','payments')">
									Print</a>
							</td>
							<td>
								<a href=""
									onclick="return doReport('${paymentUrl}', 'paymentsDashboardReport', 'All','reference_docto_id',
									'summary','payments')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
					</table>
				</div>
			</div>

			<div class="stwMain">
				<div class="stwHeader stwClosed" id="ratevariation"
					onclick="stwToggle(this);"><label>Billing Exception Reports</label></div>
				<div class="stwContent stwHidden" id="ratevariation_content">
					<table class="dashboard">
						<tr>
							<th>About This Report</th>
							<th>Tabular Summary</th>
							<th>Item-wise Details</th>
							<th>Bill-wise Details</th>
							<th>Daily Trend</th>
							<th>Weekly Trend</th>
							<th>Monthly Trend</th>
						</tr>
						<tr>
							<td style="text-align:left">
								<label title="This report shows rate variation between actual rate and billed rate">
									Discount Report
								</label>
							</td>
							<td>
								<a href="" onclick="return doReport('${rtVarUrl}', 'rateVariationDashboard',
									'chargegroup_name','', 'dashboard','billing')">
									Print</a>
							</td>
							<td>
								<a href="" onclick="return doReport('${rtVarUrl}', 'rateVariationDashboard',
									'chargegroup_name','', 'detail','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">Rate Variation Report</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'rateVariationDashboard', 'org_name','',
									 'dashboard','billing')">
									Print</a>
							</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'rateVariationDashboard', 'org_name','',
									 'detail','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">TPA Dues Report</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'tpaDuesReport', 'tpa_name','',
									 'dashboard','billing')">
									Print</a>
							</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'tpaDuesBillWiseReport','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">TPA Write off Report</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'tpaWriteOffReport', 'tpa_name','',
									 'dashboard','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">Patient Dues Report</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'patientExceptionsReport', 'patient_dues','',
									 'dashboard','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'patientExceptionsReport', 'patient_dues','', 'detail','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
						<tr>
							<td style="text-align:left">Patient Write off Report</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'patientExceptionsReport', 'patient_write_off','',
									 'dashboard','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>
								<a href=""
									onclick="return doReport('${rtVarUrl}', 'patientExceptionsReport', 'patient_write_off','', 'detail','billing')">
									Print</a>
							</td>
							<td>NA</td>
							<td>NA</td>
							<td>NA</td>
						</tr>
					</table>
				</div>
			</div>

		</form>

	</body>
</html>


