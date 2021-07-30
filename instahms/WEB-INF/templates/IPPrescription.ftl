<html>

<head>
	<title>Hospital Services - Insta HMS</title>
</head>
<!--
[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->
<body>
<!-- [#escape x as x?html] -->
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
	<!-- [#if (orderId)??] -->
	<table cellspacing='0' cellpadding='0' width='100%' >
		<tbody>
			<tr>
				<td align='left' valign='top' width="118px">Order #:</td>
				<td align='left' valign='top'>${orderId?string("#")}</td>
			</tr>
		</tbody>
	</table>
	<!-- [/#if] -->
</div>

<!-- [#if orders?size > 0] -->
	<div align="center"><b>New Orders</b></div>

	<table cellspacing='0' cellpadding='0' width='100%'>
		<tr class="border-above-below">
			<th>Date/Time</th>
			<th>Prescribed&nbsp;By</th>
			<th>Type</th>
			<th>Item</th>
			<th>Details</th>
			<th>Quantity</th>
			<th>Amount</th>
			<th>Remarks</th>
		</tr>

		<!-- [#assign first = "Y"] -->
		<!-- [#list orders as comp] -->
			<tr>
				<td>${comp.pres_timestamp!}</td>
				<td>${comp.pres_doctor_name!}</td>
				<td>${comp.type!}
					<!-- [#if comp.sub_type_name! != ''] -->
						(${comp.sub_type_name!})
					<!-- [/#if] -->
				</td>
				<td>${comp.item_name!}</td>
				<td>${comp.details!}</td>
				<td>${comp.quantity!}</td>
				<td>${comp.amount!}</td>
				<td>${comp.remarks!}</td>
			</tr>
		<!-- [/#list] -->
	</table>
<!-- [/#if] -->

<!-- [#if operations?size > 0] -->
<!-- [#assign statusDisplay = {"X":"Cancelled", "C":"Completed", "U":"N/A", "N": ""}] -->
	<div align="center" style="border-top: 1px solid black; margin-top: 12pt"><b>New Surgery Orders</b></div>
	<!-- [#list operations as operation] -->
		<div style="border-top: 1px solid black; border-bottom: 1px solid black; padding: 5pt 0pt">
			<p>Surgery: ${operation.name}</p>
			<table cellspacing='0' cellpadding='0' width='100%'>
				<tr>
					<td>Operation Theatre: </td>
					<td>${operation.theatre!}</td>
					<td>Start:</td>
					<td>${operation.start_datetime!}</td>
					<td>End:</td>
					<td>${operation.end_datetime!}</td>
				</tr>

				<tr>
					<td>Surgeon:</td>
					<td>${operation.surgeon_name!}</td>
					<td>Status:</td>
					<td>${statusDisplay[operation.status]}</td>
					<td>Remarks:</td>
					<td>${operation.remarks!}</td>
					<td></td>
				</tr>
			</table>

			<table cellspacing='0' cellpadding='0' width='100%'>
				<tr class="border-above-below">
					<th>Date/Time</th>
					<th>Prescribed By</th>
					<th>Type</th>
					<th>Item</th>
					<th>Details</th>
					<th>Quantity</th>
					<th>Amount</th>
					<th>Remarks</th>
				</tr>

				<!-- [#list operationSubOrders as comp] -->
					<!-- [#if comp.operation_ref == operation.id] -->
						<tr>
							<td>${comp.pres_timestamp!""}</td>
							<td>${comp.pres_doctor_name!""}</td>
							<td>
								${comp.type!}
								<!-- [#if comp.sub_type_name! != ''] -->
									(${comp.sub_type_name!})
								<!-- [/#if] -->
							</td>
							<td>${comp.item_name!""}</td>
							<td>${comp.details!""}</td>
							<td>${comp.quantity!}</td>
							<td>${comp.amount!}</td>
							<td>${comp.remarks!""}</td>
						</tr>
					<!-- [/#if] -->
				<!-- [/#list] -->
			</table>
		</div>
	<!-- [/#list] -->
<!-- [/#if] -->

<!-- there could be other operation ref orders which are'nt part of any new surgery orders. -->
<!-- [#if orphanSubOrders?size > 0] -->
	<div align="center" style="border-top: 1px solid black; margin-top: 12pt"><b>New Orders under Surgeries</b></div>
	<div style="border-top: 1px solid black; border-bottom: 1px solid black; padding: 5pt 0pt">
		<table cellspacing='0' cellpadding='0' width='100%'>
			<tr class="border-above-below">
				<th>Date/Time</th>
				<th>Prescribed By</th>
				<th>Type</th>
				<th>Item</th>
				<th>Details</th>
				<th>Quantity</th>
				<th>Amount</th>
				<th>Remarks</th>
			</tr>

			<!-- [#list orphanSubOrders as comp] -->
				<tr>
					<td>${comp.pres_timestamp!""}</td>
					<td>${comp.pres_doctor_name!""}</td>
					<td>
						${comp.type!}
						<!-- [#if comp.sub_type_name! != ''] -->
							(${comp.sub_type_name!})
						<!-- [/#if] -->
					</td>
					<td>${comp.item_name!""}</td>
					<td>${comp.details!""}</td>
					<td>${comp.quantity!}</td>
					<td>${comp.amount!}</td>
					<td>${comp.remarks!""}</td>
				</tr>
			<!-- [/#list] -->
		</table>
	</div>
<!-- [/#if] -->

<table width="100%" style="margin-top: 1em">
	<tr align="right" valign="bottom">
		<td>Signature</td>
	</tr>
	<tr align="right" valign="bottom">
		<td>(${username})</td>
	</tr>
</table>

<!-- [/#escape] -->
</body>
</html>

