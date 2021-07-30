<?xml version="1.0" encoding="utf-8"?>
<Claim.Submission xmlns:tns="http://www.haad.ae/DataDictionary/CommonTypes"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:noNamespaceSchemaLocation="http://www.haad.ae/DataDictionary/CommonTypes/ClaimSubmission.xsd">

[#setting number_format="#"]
 <!-- Header: Submission identification information -->
	<Header>
		<SenderID>${provider_id!""}</SenderID>
		<ReceiverID>${receiver_id!""}</ReceiverID>
		<TransactionDate>${todays_date}</TransactionDate>
		<RecordCount>${claims_count}</RecordCount>
		<DispositionFlag>${disposition_flag}</DispositionFlag>
	</Header>
