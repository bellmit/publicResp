[#if (investigations??) && (investigations?size > 0)]
	<div id="testsDiv">
		<h3>Investigations: </h3>
		<table id="testsTab" width="100%">
			<tbody>

				[#list investigations as report]
					<tr><td>${report.report_data}</td></tr>
				[/#list]
			</tbody>
		</table>
	</div>
[/#if]