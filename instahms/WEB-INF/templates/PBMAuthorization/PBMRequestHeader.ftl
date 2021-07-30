<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
[#if health_authority?? && health_authority == "DHA"]
<Prior.Request xmlns:tns="http://www.eclaimlink.ae/DHD/ValidationSchema"
 xsi:noNamespaceSchemaLocation="http://www.eclaimlink.ae/DHD/ValidationSchema/PriorRequest.xsd"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
[#else]
<Prior.Request xsi:schemaLocation="http://www.haad.ae/DataDictionary/CommonTypes.xsd
http://www.w3.org/2001/XMLSchema-instance"
 xsi:noNamespaceSchemaLocation="https://www.haad.ae/DataDictionary/CommonTypes/PriorRequest.xsd"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
[/#if]

[#setting number_format="#"]
 <!-- Header: PBM Request identification information -->
	<Header>
		<SenderID>${provider_id!""}</SenderID>
		<ReceiverID>${receiver_id!""}</ReceiverID>
		<TransactionDate>${transaction_date}</TransactionDate>
		<RecordCount>${pbm_auth_count}</RecordCount>
		[#if testing?? && testing == "N"]
		<DispositionFlag>PRODUCTION</DispositionFlag>
		[#else]
		<DispositionFlag>TEST</DispositionFlag>
		[/#if]
	</Header>
