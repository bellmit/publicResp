[#if (medicines??) && (medicines?size > 0)]
	<div id="medicinesDiv">
		<h3>Medicines: </h3>
		<table id="medicinesTab" width="100%">
			<tbody>
			[#list medicines as medicine]
				<tr>
					<td width="30px"></td>
					<td>${(medicine.medicine_name)!}</td>
				</tr>
			[/#list]
			</tbody>
		</table>
	</div>
[/#if]