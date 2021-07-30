<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>


<html>

	<head>
		<title>Auto Post Payments - Insta HMS</title>
		<insta:link type="js" file="dashboardsearch.js"/>

		<script>
			function submitPost() {
				autoPaymentsForm.submit();
			}
		</script>
	</head>

	<body>
		<form name="autoPaymentsForm" action="AutopostPayments.do" method="POST">
			<input type="hidden" name="_method" value="postPayments"/>
			<div class="pageHeader">Auto Post Payments</div>
			<fieldset class="fieldsetborder"><legend class="fieldsetlabel">Select Transactions</legend>
			<table class="searchFormTable" style="border-top: none">
				<tr>
					<td style="border-right: none;">
						<div class="sfLabel">Bill Status:</div>
						<div class="sfField">
							<insta:checkgroup name="bill_status" opvalues="F,C" optexts="Finalized,Closed"
								selValues="${paramValues.bill_status}"/>
						</div>
					</td>
					<td style="border-right: none;">
						<div class="sfLabel">Patient Type:</div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP"
								selValues="${paramValues.visit_type}"/>
						</div>

					</td>
					<td style="border-right: none;">
						<div class="sfLabel">Insurance:</div>
						<div class="sfField">
							<insta:checkgroup name="insurancestatus" opvalues="Y,N"
								selValues="${paramValues.insurancestatus}"  optexts="Insurance,Non Insurance" />
						</div>

					</td>
					<td style="border-right: none;">
						<div class="sfLabel">Payment Type:</div>
						<div class="sfField">
							<insta:checkgroup name="_paymentType" opvalues="P,C,R"
								optexts="Prescribed,Conducting,Referral" selValues="${paramValues._paymentType}"/>
						</div>

					</td>
				</tr>
				<tr>
					<td style="border-right: none;">
						<div class="sfLabel">Charge Posted Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="bc_posted_date" id="bc_posted_date0"
									value="${paramValues.bc_posted_date[0]}"/>
									<input type="hidden" name="bc_posted_date@op" value="ge,le"/>
									<input type="hidden" name="bc_posted_date@type" value="date"/>
									<input type="hidden" name="bc_posted_date@cast" value="y"/>

						</div>
						<div class="sfField">
						<div class="sfFieldSub">To:</div>
							<insta:datewidget name="bc_posted_date" id="bc_posted_date1"
										value="${paramValues.bc_posted_date[1]}"/>
						</div>
					</td>
					<td style="border-right: none;">
						<div class="sfLabel">Finalized Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="b_finalized_date" id="b_finalized_date0"
									value="${paramValues.b_finalized_date[0]}"/>
									<input type="hidden" name="b_finalized_date@op" value="ge,le"/>
									<input type="hidden" name="b_finalized_date@type" value="date"/>
									<input type="hidden" name="b_finalized_date@cast" value="y"/>

						</div>
						<div class="sfField">
						<div class="sfFieldSub">To:</div>
							<insta:datewidget name="b_finalized_date" id="b_finalized_date1"
										value="${paramValues.b_finalized_date[1]}"/>
						</div>
					</td>
					<td style="border-right: none;">
						<div class="sfLabel">Closed Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="b_closed_date" id="b_closed_date0"
									value="${paramValues.b_closed_date[0]}"/>
									<input type="hidden" name="b_closed_date@op" value="ge,le"/>
									<input type="hidden" name="b_closed_date@type" value="date"/>
									<input type="hidden" name="b_closed_date@cast" value="y"/>

						</div>
						<div class="sfField">
						<div class="sfFieldSub">To:</div>
							<insta:datewidget name="b_closed_date" id="b_closed_date1"
										value="${paramValues.b_closed_date[1]}"/>
						</div>
					</td>
				</tr>
				</table>
				</fieldset>
				<fieldset class="fieldsetborder"><legend class="fieldsetlabel">Select Payment Posting Date</legend>
					<table class="searchFormTable" style="border-top: none">
						<tr>
							<td style="border-right: none;">

								<div class="sfField">
										<insta:radio name="posting_date_by" radioValues="today,finalized" value="${empty param.posting_date_by ? 'today' : param.posting_date_by}" radioText="Today,Finalized Date" radioIds="today,finalized" />
								</div>
							</td>
						</tr>
					</table>
				</fieldset>
				<div class="screenActions">
					<input type="button" name="_postpayment" value="Post Payment" onclick="return submitPost()"/>
				</div>
		</form>
	</body>

</html>