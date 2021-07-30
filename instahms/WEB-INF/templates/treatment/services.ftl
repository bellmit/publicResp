[#if (services??) && (services?size > 0) ]
	<div id="servicesDiv" >
		<h3>Services: </h3>
		<table id="servicesTab" width="100%">
			<tbody>
				[#list services as service]
					<tr>
						<td colspan="2">${(service.service_name)!}</td>
					</tr>
					<tr>
						<td width="30px"></td>
						<td>${(service.doc_content)!}</td>
					</tr>
				[/#list]
			</tbody>
		</table>
	</div>
[/#if]
