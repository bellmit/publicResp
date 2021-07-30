[#include "TextPrintMacros.ftl"]
[#setting datetime_format="dd-MM-yyyy"]

[#escape x as x?html]

	[#assign title ]
		${depositPrintName}
	[/#assign]

[@cfill title 80/]

[#list depositsList as dl]

MR No: ${lf(dl.mr_no, 18)}  				                         Name: ${lf(dl.patient_name,  26)}
		[#if dl.patient_age_in == 'Y']

Age/Gender:${dl.patient_age} Years / ${lf(dl.patient_gender, 35)}  Contact No: ${lf(patient_phone!, 16)}
		[#elseif dl.patient_age_in == 'M']
			${dl.patient_age} Months / ${lf(dl.patient_gender, 35)}
		[#else]
			${dl.patient_age} Days / ${lf(dl.patient_gender, 35)}
		[/#if]


Receipt No: ${lf(dl.deposit_no, 36)}								Deposit Date: ${lf(dl.deposit_date, 26)}

Received Amount: ${currencySymbol}${dl.amount} Towards: Deposit

Mode of Payment: ${lf(dl.payment_mode_name, 14)}                         Aganist: ${lf(dl.mr_no, 13)}

${lf(dl.card_type!,8)}  ${lf(dl.bank_name!,16)} ${lf(dl.reference_no!,12)}

Deposit Payee Name: ${lf(dl.deposit_payer_name, 13)}

Net Amount received against this Receipt / Refund :${currencySymbol}${dl.amount}
[#assign pkgName = dl.package_name!""]
[#assign depositFor = dl.deposit_avalibility!""]

[#if pkgName != "" && depositFor != ""]
[#assign depositFor = " (" + depositFor + ")"]
[/#if]

Deposit for: ${lf(pkgName + depositFor, 37)}

Received with thanks : ${AmountinWords}

Signature
( ${dl.username} )

[/#list]
[/#escape]
