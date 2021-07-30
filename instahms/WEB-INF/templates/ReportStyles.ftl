<style type="text/css">
	@page {
		size: 595pt 842pt;
		margin: 36pt 36pt 36pt 36pt;
	}

	body {
		font-family: Arial, sans-serif;
	}

	table.report {
		empty-cells: show;
		font-size: 9pt;
	}

	table.report {
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.report th {
		border: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.report td {
		padding: 2px 4px 2px 4px;
		border: 1px solid black;
	}

	table.report td.number {
		text-align: right;
	}

	table.report td.heading {
		font-weight: bold;
	}

	<#--------tabular report style with no vertical lines--------->

	table.CFDreport {
		empty-cells: show;
		font-size: 9pt;
	}

	table.CFDreport {
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.CFDreport th {
		border-top: 1px solid black;
		border-bottom: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.CFDreport td {
		padding: 2px 4px 2px 4px;
		border-top: 1px solid black;
		border-bottom: 1px solid black;
	}

	table.CFDreport td.number {
		text-align: right;
	}

	table.CFDreport tr.heading td {
		font-weight: bold;
	}

	<#----------------------------------->
	p.noresult {
		font-weight: bold;
	}

	p.heading {
		font-size: 12pt;
		font-weight: bold;
	}
	p {
		margin: 4pt;
	}

	.total {
		font-weight: bold;
	}

</style>

<#if format == "screen">
	<link type="text/css" rel="stylesheet" href="../css/hmsNew.css"/>
</#if>

<style type="text/css">
	a, a:visited {
		text-decoration: none;
	}
	a:hover {
		text-decoration: underline;
	}
</style>
