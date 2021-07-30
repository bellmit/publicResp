[#escape x as x?html]
<div>
<!-- [#assign total = 0] -->
		<!-- [#if patientIssueList?has_content] -->
			<hr/>
			<h2 style="margin-top: 10px;text-align:center;"><b>Stores Stock Patient Issue</b></h2>
			<hr/>
			<br/>
			<table cellspacing='0' cellpadding='1'  style='margin-top: 5px' width="100%">
				<tr>
					<td>From Store:  </td><td valign="top"><b>${patientIssueList[0].from_store}</b></td>
					<td>User Type: </td><td valign="top"><b>${patientIssueList[0].user_type}</b></td>
				</tr>
				<tr>
					<td>Issue No: </td><td valign="top"><b>${patientIssueList[0].user_issue_no}</b></td>
					<td>Issue Date: </td><td valign="top"><b>${patientIssueList[0].date}</b></td>
				</tr>
				<tr>
					<td>Issued To: </td><td valign="middle"><b>${patientIssueList[0].issued_to}</b></td>
					<td>Patient Name: </td><td valign="middle" width="30%"><b>${patientIssueList[0].patient_name}</b></td>
				</tr>
				<tr>
					<td>Mr No: </td><td valign="top"><b>${patientIssueList[0].mr_no}</b></td>
					<td>Bill No: </td><td valign="top"><b>${patientIssueList[0].bill_no!}</b></td>
				</tr>
				<tr>
					<td>User: </td><td valign="top"><b>${patientIssueList[0].username!}</b></td>
					<td>Reason: </td><td valign="top"><b>${patientIssueList[0].reference!}</b></td>
				</tr>
			</table>
			
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr><td colspan="6"><hr/></td></tr>
					<tr>
						<th align="left" width="50%"><b>Item Name</b></th>
						<th align="center" width="30%"><b>Batch/Sl No</b></th>
						<th align="center" width="70%"><b>Issue Type</b></th>
						<th align="center" width="30%"><b>Issue Units</b></th>
						<th align="center" width="30%"><b>Qty</b></th>
						<th align="center" width="30%"><b>Value</b></th>
					</tr>
					<tr><td colspan="6"><hr/></td></tr>
					<!-- [#list patientIssueList as patientIssue] -->
					<!-- [#assign value = patientIssue.amount] -->
					<!-- [#assign total = total + value] -->
					<tr>
						<td align="left" width="50%">${patientIssue.medicine_name}</td>
						<td align="center" width="30%">${patientIssue.batch_no}</td>
						<td align="center" width="70%">${patientIssue.issue_type}</td>
						<td align="center" width="30%">${patientIssue.issue_units}</td>
						<td align="center" width="30%">${patientIssue.qty}</td>
						<td align="center" width="30%">${patientIssue.amount}</td>
					</tr>
					<tr><td colspan="6"><br></br></td></tr>
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
