[#setting number_format="#"]
[#setting datetime_format = "dd-MM-yyyy HH:MM"]
[#assign issueType={"C":"Consumable","R":"Retail","L":"Reusable","P":"Permanent"}]
[#assign status={"O":"Open","C":"Closed","A":"Approved","R":"Rejected"}]
[#escape x as x?html]

	<table width="100%">
		<tr>
			<td>Indent No :</td>
			<td>${indentdetails.indent_no}</td>
			<td>Raised By:</td>
			<td>${indentdetails. store_user}</td>
			<td>Indent Store :</td>
			<td >${indentdetails.store_name}</td>
		</tr>
		<tr>
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
			<td>Status :</td>
			<td>${status[indentdetails.status]}</td>
			<td>Expected Date :</td>
			<td>${indentdetails.expected_date}</td>
		</tr>
		<tr>
			<td>Reason :</td>
			<td>${indentdetails.remarks ! ""}</td>
		</tr>
	</table>

	<table width="100%">
		<tr>
			<td width="20%">Item</td>
			<td width="10%">Qty Req.</td>
			<td width="10%">Qty Avbl</td>
			<td width="10%">Pkg Size</td>
			<td width="15%">Issue UOM</td>
			<td width="">B.No(qty)</td>

		</tr>
		<!--
			[#list indentlist as i]
				[#list identifierList as il]
		-->
					[#if i.medicine_id?string = il.MEDICINE_ID?string]
						<tr>
							<td width="10%">${i.medicine_name}</td>
							<td width="10%">${i.qty?string('##0.00')}</td>
							<td width="15%">[#if i.availableqty?has_content]${i.availableqty?string('##0.00')}[/#if]</td>
							<td width="10%">${i.issue_base_unit!""}</td>
							<td width="10%">${i.issue_units!""}</td>
							<td${il.BATCH_NO!""}></td>

						</tr>
					[/#if]
			<!--
				[/#list]
			-->
				[#if i.medicine_id?string = '0']
					<tr>
						<td width="10%">${i.medicine_name}</td>
						<td width="10%">${i.qty?string('##0.00')}</td>
						<td width="15%">[#if i.availableqty?has_content]${i.availableqty?string('##0.00')}[/#if]</td>
						<td width="10%">${i.issue_base_unit!""}</td>
						<td width="10%">${i.issue_units!""}</td>
						<td></td>
					</tr>

				[/#if]
		<!--
			[/#list]
		-->
	</table>

[/#escape]
