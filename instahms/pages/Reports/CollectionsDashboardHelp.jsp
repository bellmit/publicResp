<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
	<title>CFD Help - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<style type="text/css">
		li {margin-top: 3px; list-style-type: square;}
		h1 {text-align: center;}
		h4 {margin: 0px;}
		p {margin: 5px 0px 5px 0px}
	</style>
</head>

<body>
<div style="width: 740px">
	<h1>Understanding the Consolidated Financial Dashboard </h1>
	The Consolidated Financial Dashboard consists of the following sections:
	<ul>
		<li><b>Cash Flow by Type</b>: Money that has been received/paid for.</li>
		<li><b>Cash Flow by Mode</b>: In what form the money has been received, like Cash, Credit card etc.</li>
		<li><b>Counts</b>: Various counts indicating activity.</li>
		<li><b>Revenue</b>: Financially recognized revenue under various heads.</li>
	</ul>

	<h3>Cash Flow by Type</h3>
	Cash Flow is a sum total of money received, (after deducting payments made) from all the
	counters that is managed in Insta HMS. This is not necessarily equal to the Revenue, since
	we may receive money as advance, deposits etc., which are not recognized as Revenue on the
	same day as the money is received.

	<p>The Cash Flow Types are organized as follows:</p>

	<h4>Bill Later</h4>

	<p>Bill Later bills have two kinds of payments: <b>Advances</b> (when the
	patient is admitted and/or deposits more money during the course of treatment, in anticipation
	of charges to be incurred), and <b>Settlements</b>, money paid at the time of discharge.
	This distinction is not strictly enforced, the billing-person collecting the money
	is expected to assign the collection as one or the other.</p>

	<p>This amount includes Bill Now with insurance</p>

	<p>Note that this money does not necessarily reflect in the Revenue section, since revenue
	is recognized only at the time, a bill has been finalized, which typically is not on the
	same day as the one on which, money has been received.</p>

	<h4>Bill Now</h4>

	<p>This excludes Bill Now with insurance. Since Bill Now bills are typically settled immediately,
	the collected money can be apportioned towards various heads into which it was received.</p>

	<p>This section shows the <i>billed amounts</i> in all Bill Now bills that were
	closed during the course of the period, assuming that the billed amount was actually collected.
	Note that this is the Revenue that has been reported in the corresponding section, and not really the
	collection. Although its expected that the <i>Bill Now billed amounts total</i>, will actually
	be the amount collected, there can be exceptions, as noted below. </p>

	<h4>Bill Now Extra Receipts</h4>
	<p>This section accounts for the difference between Bill Now
	revenues and Cash Flow, as the receipts beyond the billed amount in the previous section.</p>

	<p>Normally, the billed amounts will match the collections for Bill Now bills, so this section
	will normally be empty, except for the following cases:</p>

	<ul>
		<li>A Bill Now bill has been reopened and left open. 
		The original receipt of money will appear in Cash Flows, but the billed amount will not appear 
		as revenue, since the bill is still open.</li>
		<li>Billed amounts have been set off against deposits.</li>
		<li>A Bill Now bill has been reopened on a different date, 
		changes made, and closed (eg, refunds given). Either the receipt or the refund will not be on 
		the same date as the bill date.</li>
	</ul>

	<p>The extra receipts are shown under the following categories:</p>
	<ul>
		<li>Receipts for bills excluded: This is the amount of money 
		collected within the given period, for which the bill has been excluded from the period 
		(only those bills, that are closed within the period, are considered for revenue calculations). 
		This amount can be negative, if so, it means that refunds have been made for those bills, that 
		have not been accounted for, in the Bill Now revenues.</li>
		<li>Excess in bills included: This is the superfluous amount 
		in receipts that were accounted of the billed amount for the period. If this amount is negative,
		it means that there is a shortfall, i.e., the receipts are not within the
		same period as the bill closing date.</li>
		<li>Less Deposit Set Offs: This is the total amount of set offs
		using deposits. This amount is always negative, since deposit set offs can only
		indicate money not collected, but is part of the revenue.</li>
	</ul>

	<h4>Deposits</h4>
	<p>This section reports the amount collected as deposits, which are not accounted for
	against any bill. It includes both, deposits and deposit returns.</p>

	<h4>Payments</h4>
	<p>This section shows all the money that has been paid-out from
	the counters towards various heads like Doctor Payments, Supplier payments etc.
	This amount is net of payment reversals. </p>

	<p>Payments appear as negative, as they correspond to the money going outwards
	from the Hospital.</p>

	<h4>Total</h4>
	<p>The total of all collections, net of payments appears as the <b>Grand Total</b> of Cash Flow.
	This is the total money that has come in within the given period. Note that this is not
	necessarily the revenue as reported in the finances of the company.</p>

	<h3>Cash Flow by Mode</h3>
	<p>This section lists the total collections, which includes money-in as well as money-out,
	in terms of the different modes of payment. Money coming in includes receipts, deposit receipts,
	and payment reversals. Money going out includes refunds, deposit returns and payments.</p>

	<p>The total amount in the Cash Flow by Mode is expected to match the total amount in
	Cash Flow by Type, as well as the total amount in the Day Book report for the same period.</p>

	<h3>Patient Counts</h3>
	<p>The following patient counts are reported:</p>
	<ul>
		<li>IP Admissions: Number of IP patients that were admitted within the given period.</li>
		<li> IP Discharges: Number of IP patients that were discharged within the given period.</li>
		<li>Active In Patients: Number of IP patients that are in the hospital. Note that (Current)
		indicates the number "as of now", which will show the same number regardless of the time period
		chosen for the report.</li>
		<li>OP Registrations: number of out patient registrations within the period.</li>
	</ul>
	<h3>Bill Counts</h3>
	<p>The following Bill counts are reported:</p>
	<ul>
		<li>Bill Now Open: As of the report time, the number of Bill Now bills that are open.</li>
		<li>Bill Later Open: As of the report time, the number of Bill Later bills that are open.</li>
		<li>Bills Cancelled: Number of bills cancelled within the period.</li>
		<li>Bills Closed: Number of bills closed within the period.</li>
	</ul>

	<h3>Revenue</h3>
	<p>Revenue represents the billed amount under various charge groups/heads.
	Under each group, the top few items, which contributed most to the revenue are shown .
	Due to the "top N" reporting, the report only shows a fixed number of items under each group.
	Thus, it may not show the same items for different periods. </p>

	<p>The list is sorted (descending order of amounts) so that individual items
	that contributed more to revenue are listed on the top, and the balance is listed
	as "Others". The Grand Total is available as a hyperlink, which upon clicking, will list
	all the bills that contributed to the revenue within the period. </p>

	<p>Note that the revenue is not expected to match the Cash Flow, although over
	a period of time, the two will converge. </p>

	<table align="center">
		<tr>
			<td align="center">
				<form name="helpForm" method="GET">
					<input type="button" value="Close" onclick="window.close();"/>
				</form>
			</td>
		</tr>
	</table>

</div>
</body>
</html>

