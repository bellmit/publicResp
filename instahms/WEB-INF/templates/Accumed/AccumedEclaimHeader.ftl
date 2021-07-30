<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Claim.Import>

[#setting number_format="#"]
 <!-- Header: Submission identification information -->
	<Header>
	    <Operation>${operation}</Operation>
	    <ProviderID>${provider_id!""}</ProviderID>
	    <TransactionDate>${todays_date}</TransactionDate>
	    <RecordCount>${claims_count}</RecordCount>
	    [#if from_date??]<FromDate>${from_date}</FromDate>[/#if]
	    [#if to_date??]<ToDate>${to_date}</ToDate>[/#if]
	</Header>

