<?xml version="1.0" encoding="utf-8"?>
[#if eclaim_xml_schema?? && eclaim_xml_schema == "DHA"]
<Claim.Submission xmlns:tns="http://www.eclaimlink.ae/DHD/ValidationSchema"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="http://www.eclaimlink.ae/DHD/ValidationSchema/ClaimSubmission.xsd">
[#else]
<Claim.Submission xmlns:tns="http://www.haad.ae/DataDictionary/CommonTypes"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:noNamespaceSchemaLocation="http://www.haad.ae/DataDictionary/CommonTypes/ClaimSubmission.xsd">
[/#if]

[#setting number_format="#"]
 <!-- Header: Submission identification information -->
	<Header>
		<SenderID>${provider_id!""}</SenderID>
		<ReceiverID>${receiver_id!""}</ReceiverID>
		<TransactionDate>${todays_date}</TransactionDate>
		<RecordCount>${claims_count}</RecordCount>
		[#if testing?? && testing == "N"]
		<DispositionFlag>PRODUCTION</DispositionFlag>
		[#else]
		<DispositionFlag>TEST</DispositionFlag>
		[/#if]
	</Header>
