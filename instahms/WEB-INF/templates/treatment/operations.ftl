[#if ((operations??) && (operations?size > 0))]
	<div id="operationsDiv" >
		<h3>Operations: </h3>
		[#list operations as operation]
		  	<table width="100%">
				<tr>
					<td align="left">Operation Theatre:</td>
					<td align="left"><b> ${operation.theatre!}</b></td>
					<td align="left">Operation:</td>
					<td align="left"><b>${operation.name!}</b></td>
					<td></td>
				</tr>
				<tr>
					<td align="left">Operation Start Date:</td>
					<td align="left"><b>${operation.operation_date!}</b></td>
					<td align="left">Start Time:</td>
					<td align="left"><b> ${operation.starttime!}</b></td>
					<td align="left">Operation End Date:</td>
					<td align="left"><b> ${operation.operation_end_date!}</b></td>
					<td align="center">End Time:</td>
					<td><b> ${operation.endtime!}</b></td>
				</tr>
				[#assign teamSize = 0]
				[#assign prescSize = 0]
					[#list operation_presc as presc]
						[#if (presc.operation_ref == operation.prescribed_id) && (presc.role != "1")]
							[#assign teamSize = teamSize+1]
						[/#if]
						[#if (presc.operation_ref == operation.prescribed_id) && (presc.role == "1")]
							[#assign prescSize = prescSize+1]
						[/#if]
					[/#list]
					[#if (teamSize > 0)]
						<tr>
							<th>Surgery&nbsp;Team</th>
						</tr>
					[/#if]
						<tr>
							<td></td>
							<td align="left">${operation.primarysurgeon!}</td>
							<td align="left">Primary Surgeon</td>
							<td align="left"></td>
							<td align="left">1</td>
							<td align="center"></td>
						</tr>
						<tr>
							<td align="left"></td>
							<td align="left">${operation.primaryanaesthetist!}</td>

							<td align="left">Primary Anaesthetist</td>
							<td align="left"></td>
							<td align="left">1</td>
							<td align="center"></td>
						</tr>
					[#list operation_presc as presc]
						[#if (presc.operation_ref == operation.prescribed_id) && (presc.role != "1")]
							<tr>
								<td align="left"></td>
								<td align="left">${presc.doctor!}</td>
								<td align="left">${presc.pgroup!}</td>
								<td align="left"></td>
								<td align="left">${presc.qty!}</td>
							</tr>
						[/#if]
					[/#list]
			</table>
			<table  width="100%">
				[#if (prescSize > 0)]
					<tr>
						<th>OT&nbsp;Prescriptions</th>
					</tr>
				[/#if]
				[#list operation_presc as presc]
					[#if (presc.operation_ref == operation.prescribed_id) && (presc.role == "1") ]
						<tr>
							<td align="left"></td>
							<td align="left">${presc.doctor!}</td>
							<td align="left">${presc.pgroup!}</td>
							<td align="left">${presc.details!}</td>
							<td align="left">${presc.qty!}</td>
						</tr>
					[/#if]
				[/#list]
			</table>
		[/#list]
	</div>
[/#if]