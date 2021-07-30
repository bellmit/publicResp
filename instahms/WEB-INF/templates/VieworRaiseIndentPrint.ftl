[#setting number_format="#"]
[#setting datetime_format = "dd-MM-yyyy HH:MM"]
[#assign issueType={"C":"Consumable","R":"Retail","L":"Reusable","P":"Permanent"}]
[#assign status={"O":"Open","C":"Closed","A":"Approved","R":"Rejected", "X":"Cancelled","P":"Processed"}]
[#escape x as x?html]

	<table width="100%">
		<tr>
			<td>Indent No :</td>
			<td>${indentdetails.indent_no}</td>
			<td>Raised By:</td>
			<td>${indentdetails. store_user}</td>
		</tr>
		<tr>
			<td>Indent Store :</td>
			<td >${indentdetails.store_name}</td>
			[#if indentdetails.indent_type = 'U']
				[#if indentdetails.location_type = 'D']
					<td>Dept :</td>
					<td>${indentdetails.dept_name}</td>
					[#else]
						<td>Ward :</td>
						<td>${indentdetails.ward_name}</td>
				[/#if]

				[#elseif indentdetails.indent_type = 'R']
					<td>Dept :</td>
					<td>${indentdetails.dept_name}</td>

				[#else]
					<td>Requesting Store:</td>
					<td>${storeName}</td>
			[/#if]
		</tr>
		<tr>
			<td>Expected Date :</td>
			<td>${indentdetails.expected_date}</td>

			<td>Status :</td>
			<td>${status[indentdetails.status]}</td>
		</tr>
	</table>

	<table width="100%">
		<tr>
			<td width="20%">Item</td>
			<td width="10%">Required</td>
			<td width="10%">Pending</td>
			<td width="10%">Issued</td>
			<td width="15%">Qty</td>
			<td width="20%">PORaised</td>
			<td width="15%">Issuetype</td>

		</tr>
		<!--
			[#list indentlist as i]
				[#list identifierList as il]
		-->
					[#if i.medicine_id?string = il.MEDICINE_ID?string]
						<tr>
							<td width="10%">${i.medicine_name}</td>
							<td width="10%">${i.qty?string('##0.00')}</td>
							<td width="10%">${(i.qty-i.qty_fullfilled)?string('##0.00')}</td>
							<td width="10%">${i.qty_fullfilled?string('##0.00')}</td>
							<td width="15%">[#if i.availableqty?has_content]${i.availableqty?string('##0.00')}[/#if]</td>
							<td width="20%">[#if i.poqty?has_content]${i.poqty?string('##0.00')}[/#if]</td>
							<td width="15%">${issueType[i.issue_type]}</td>

						</tr>
					[/#if]
			<!--
				[/#list]
			-->
				[#if i.medicine_id?string = '0']
					<tr>
						<td width="10%">${i.medicine_name}</td>
						<td width="10%">${i.qty?string('##0.00')}</td>
						<td width="10%">${(i.qty-i.qty_fullfilled)?string('##0.00')}</td>
						<td width="10%">${i.qty_fullfilled?string('##0.00')}</td>
						<td width="15%">[#if i.availableqty?has_content]${i.availableqty?string('##0.00')}[/#if]</td>
						<td width="20%">[#if i.poqty?has_content]${i.poqty?string('##0.00')}[/#if]</td>
						<td width="15%">${i.issue_type ! ""}</td>
						<td></td>
					</tr>

				[/#if]
		<!--
			[/#list]
		-->
	</table>

[/#escape]
