[#include "TextPrintMacros.ftl"]
[#if (patient_id!'')!='']
	[#include "VisitDetailsTextHeader.ftl"]
	[#else][#include "PatientDetailsTextHeader.ftl"]
[/#if]
[#list fieldvalues as docdetails]
	[#assign formTitle=docdetails.title!]
	[#break]
[/#list]
[#assign documentSeq = patientDocDetails.doc_number!]
[@cfill formTitle 80/]
[#if  patientDocDetails.doc_number??]
Document Number: ${documentSeq}
[/#if]
[#if  visitdetails.doc_date??]
Document Date: ${visitdetails.doc_date?string('dd-MM-yyyy')}
[/#if]

[#list fieldvalues as values]
[#if ((values.field_value)!'') != '']
	${(values.field_name)!}${((values.field_value))}
[/#if]
[/#list]