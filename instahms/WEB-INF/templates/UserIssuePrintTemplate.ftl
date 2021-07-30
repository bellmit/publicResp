[#escape x as x?html]
<div>
<!-- [#assign total = 0] -->
		<!-- [#if UserIssueList?has_content] -->
			<hr/>
			<h2 style="margin-top: 10px;text-align:center;"><b>Stores Stock User/Department/Ward Issue</b></h2>
			<hr/>
			<br/>
			<table cellspacing='0' cellpadding='1'  style='margin-top: 5px' width="100%">
				<tr>
					<td>From Store:  </td><td valign="top"><b>${UserIssueList[0].from_store!}</b></td>
					<td>User Type: </td><td valign="top"><b>${UserIssueList[0].user_type!}</b></td>
				</tr>
				<tr>
					<td>Issue No: </td><td valign="top"><b>${UserIssueList[0].user_issue_no?string("0.##")}</b></td> 
					<td>Issue Date: </td><td valign="top"><b>${UserIssueList[0].date!}</b></td>
				</tr>
				<tr>
					<td>Issued To: </td><td valign="middle"><b>${UserIssueList[0].issued_to!}</b></td>
					<td>Patient Name: </td><td valign="middle" width="30%"><b>${UserIssueList[0].patient_name!}</b></td>
				</tr>
				<tr>
					<td>User: </td><td valign="top"><b>${UserIssueList[0].username!}</b></td>
					<td>Reason: </td><td valign="top"><b>${UserIssueList[0].reference!}</b></td>
				</tr>
			</table>
			
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr><td colspan="6"><hr/></td></tr>
					<tr>
						<th align="left" width="30%"><b>Item Name</b></th>
						<th align="center" width="20%"><b>Batch/Sl No</b></th>
						<th align="center" width="20%"><b>Expiry Date</b></th>
						<th align="center" width="20%"><b>Stock</b></th>
						<th align="center" width="20%"><b>Issue Type</b></th>
						<th align="center" width="20%"><b>Issue Units</b></th>
						<th align="center" width="20%"><b>Qty</b></th>
						
					</tr>
					<tr><td colspan="6"><hr/></td></tr>
					<!-- [#list UserIssueList as userIssue] -->
					
					<tr>
						<td align="left" width="30%">${userIssue.medicine_name!}</td>
						<td align="center" width="20%">${userIssue.batch_no!}</td>
						<td align="center" width="20%">${userIssue.exp_dt!}</td>
						<td align="center" width="20%">${userIssue.stocktype!}</td>
						<td align="center" width="20%">${userIssue.issue_type!}</td>
						<td align="center" width="20%">${userIssue.issue_units!}</td>
						<td align="center" width="20%">${userIssue.qty?string.number}</td>
					</tr>
					<tr><td colspan="6"><br></br></td></tr>
					<!--	[/#list] -->
					
				</tbody>
			</table>
		<!-- [/#if] -->
</div>
[/#escape ]
