<html>
<h2 style='text-align: center;'>Pending Test(s) as of ${todaysDate?string("dd-MM-yyyy HH:mm")}</h2>
<body>
<table valign='top' cellpadding='2' cellspacing='0' border='0' width='100%'>
	<tbody>
		<tr style='border-bottom: 1px solid black;'>
			<th>#</th>
			<th>Test Name </th>
			<th>Status</th>
		</tr>
		<!--[#list pendingTests as  test] -->
		<tr>
			<td>${test.map.slno?string.number}</td>
			<td>${test.map.name?html}</td>
			<td>${test.map.test_status}</td>
		</tr>
		<!-- [/#list] -->
	</tbody>
</table>
</body>
</html>