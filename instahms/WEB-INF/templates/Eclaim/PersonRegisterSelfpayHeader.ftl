<?xml version="1.0" encoding="utf-8"?>
<Person.Register>

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
