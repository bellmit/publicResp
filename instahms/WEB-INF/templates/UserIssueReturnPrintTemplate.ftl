[#escape x as x?html]
<div>
<!-- [#assign total = 0] -->
		<!-- [#if UserIssueReturnList?has_content] -->
			<hr/>
			<h2 style="margin-top: 10px;text-align:center;"><b>Stock User/Department/Ward Return</b></h2>
			<hr/>
			<br/>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">

				<tr>
					<td>Return No: </td><td valign="top"><b>${UserIssueReturnList[0].user_return_no?string.number}</b></td>
					<td>Store: </td><td valign="top"><b>${UserIssueReturnList[0].to_store!}</b></td>
					<td>Returned Date: </td><td valign="top"><b>${UserIssueReturnList[0].date!}</b></td>
				</tr>
				<tr>
					<td>Returned By: </td><td valign="top"><b>${UserIssueReturnList[0].returned_by!}</b></td>
					<td>Issued Type: </td><td valign="middle"><b>${UserIssueReturnList[0].issue_type!}</b></td>
					<td>Patient Name: </td><td valign="middle" width="30%"><b>${UserIssueReturnList[0].patient_name!}</b></td>
				</tr>
				
				<tr>
					<td>User: </td><td valign="top"><b>${UserIssueReturnList[0].username!}</b></td>
					<td>Reason: </td><td valign="top"><b>${UserIssueReturnList[0].reference!}</b></td>
				</tr>
			</table>
			
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr><td colspan="6"><hr/></td></tr>
					<tr>
						<th align="left" width="50%"><b>Item Name</b></th>
						<th align="center" width="20%"><b>Batch/Sl No</b></th>
						<th align="center" width="20%"><b>Expiry Date</b></th>
						<th align="center" width="20%"><b>Qty</b></th>
						<th align="center" width="20%"><b>Stock</b></th>
						<th align="center" width="20%"><b>Issue Type</b></th>	
					</tr>
					<tr><td colspan="6"><hr/></td></tr>
					<!-- 	[#list UserIssueReturnList as userIssueReturn] -->
					<tr>
						<td align="left" width="50%">${userIssueReturn.medicine_name!}</td>
						<td align="center" width="20%">${userIssueReturn.batch_no!}</td>
						<td align="center" width="20%">${userIssueReturn.exp_dt!}</td>
						<td align="center" width="20%">${userIssueReturn.qty?string.number}</td>
						<td align="center" width="20%">${userIssueReturn.stocktype!}</td>
						<td align="center" width="20%">${userIssueReturn.issue_type!}</td>
					</tr>
					<br></br>
					<!--	[/#list] -->
			
				</tbody>
			</table>
		<!-- [/#if] -->
</div>
[/#escape ]