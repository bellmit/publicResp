<!-- [#escape x as x?html] -->
	<table style="margin-top: 10px; width:100%">
		<tr>
			<th >Due Date</th>
			<th >Time</th>
			<th >Type</th>
			<th >Item</th>
			<th >Remarks</th>
			<th >Compl. Time</th>
		</tr>
		[#list patient_activities as activity]
	<!--	[#assign item_name=""]
			[#assign prescription_type_name=""]
			[#assign prescription_type='']
			[#if activity.prescription_type?has_content]
				[#assign prescription_type=activity.prescription_type!'']
			[/#if]

			[#if prescription_type == 'M' || prescription_type == 'O']
				[#assign prescription_type_name = "Medicine"]
			[#elseif prescription_type == 'I']
				[#assign prescription_type_name = "Inv."]
			[#elseif prescription_type == 'S']
				[#assign prescription_type_name = "Service"]
			[#elseif prescription_type == 'D']
				[#assign prescription_type_name = "Consultation"]
			[#elseif prescription_type == 'O']
				[#assign prescription_type_name = "Others"]
			[#elseif activity.activity_type == 'G']
				[#assign prescription_type_name = "General Activity"]
			[/#if]
			 -->
			<tr>
				<td>${activity.due_date?string("dd-MM-yyyy")}</td>
				<td>${activity.due_date?string("HH:mm")}</td>
				<td>${prescription_type_name!}</td>
				<td>${activity.item_name!}</td>
				<!-- [#if activity.activity_remarks?has_content] -->
					<td>${activity.activity_remarks!}</td>
				<!-- [#else] -->
					<td>${activity.remarks!}</td>
				<!--[/#if] -->
				<td>
				<!-- [#if activity.completed_date?has_content] -->
						${activity.completed_date?string("dd-MM-yyyy HH:mm")}
				 <!--[/#if] -->
				</td>
			</tr>
		[/#list]
	</table>
[/#escape]