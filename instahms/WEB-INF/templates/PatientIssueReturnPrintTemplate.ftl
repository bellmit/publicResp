[#escape x as x?html]
<div>
<!-- [#assign total = 0] -->
		<!-- [#if patientIssueReturnList?has_content] -->
			<hr/>
			<h2 style="margin-top: 10px;text-align:center;"><b>Stores Stock Patient Issue Return</b></h2>
			<hr/>
			<br/>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">

				<tr>
				<td>Return No: </td><td valign="top"><b>${patientIssueReturnList[0].user_return_no?string.number}</b></td>
					<td>Issue Date: </td><td valign="top"><b>${patientIssueReturnList[0].date!}</b></td>
				</tr>
				<tr>
					<td>Issued Type: </td><td valign="middle"><b>${patientIssueReturnList[0].issue_type!}</b></td>
					<td>Patient Name: </td><td valign="middle" width="30%"><b>${patientIssueReturnList[0].patient_name!}</b></td>
				</tr>
				
				<tr>
					<td>User: </td><td valign="top"><b>${patientIssueReturnList[0].username!}</b></td>
					<td>Reason: </td><td valign="top"><b>${patientIssueReturnList[0].reference!}</b></td>
				</tr>
			</table>
			
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr><td colspan="6"><hr/></td></tr>
					<tr>
						<th align="center" width="15%"><b>Item Name</b></th>
						<th align="center" width="15%"><b>Batch/Sl No</b></th>
						<th align="center" width="70%"><b>Issue Type</b></th>
						<th align="center" width="70%"><b>Qty</b></th>
						<th align="center" width="70%"><b>Value</b></th>
					</tr>
					<tr><td colspan="6"><hr/></td></tr>
					<!-- 	[#list patientIssueReturnList as patientIssueReturn] -->
					<!-- [#assign value = patientIssueReturn.amount] -->
					<!-- [#assign total = total + value] -->
					<tr>
						<td align="left" width="15%">${patientIssueReturn.medicine_name!}</td>
						<td align="center" width="15%">${patientIssueReturn.batch_no!}</td>
						<td align="center" width="70%">${patientIssueReturn.issue_type!}</td>
						<td align="center" width="70%">${patientIssueReturn.qty?string.number}</td>
						<td align="center" width="70%">${patientIssueReturn.amount!}</td>
					</tr>
					<!--	[/#list] -->
					<tr>
						<td colspan="4"></td>
						<td colspan="2"><hr/></td>
					</tr>
					<tr>
						<td colspan="4"></td>
						<td align="center"><b>Total:</b></td>
						<td align="center"><b>${total}</b></td>
					</tr>
					<tr>
						<td colspan="4"></td>
						<td colspan="2"><hr/></td>
					</tr>
				</tbody>
			</table>
		<!-- [/#if] -->
</div>
[/#escape ]