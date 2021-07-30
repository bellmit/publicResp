<ENVELOPE>
	<HEADER>
		<TALLYREQUEST>Import Data</TALLYREQUEST>
	</HEADER>
	<BODY>
	<IMPORTDATA>
		<REQUESTDESC>
			<REPORTNAME>Vouchers</REPORTNAME>
			[#if accounting_company_name?has_content]
				<STATICVARIABLES>
			     	<SVCURRENTCOMPANY>${accounting_company_name!}</SVCURRENTCOMPANY>
			    </STATICVARIABLES>
			[/#if]
		</REQUESTDESC>
		<REQUESTDATA>
