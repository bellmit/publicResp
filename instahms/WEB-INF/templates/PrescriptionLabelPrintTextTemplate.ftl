[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]
[#setting number_format="#"]

[#list items as item]
[#assign doctor = "${item.doctor_name!}"]
[#assign prescribedate = "${item.sale_date!}"]

[#if item?has_content]
				Name 		  :  ${lf(item.patient_full_name!,    30)}
				Doctor Name   :  ${lf(doctor,       30)}
				Date 		  :  ${lf(prescribedate,       30)}

				Medicine Name :  ${lf(item.medicine_name!,    20)}
				Dosage        :  ${lf(item.dosage!,  20)}
				Duration      :  ${lf(item.duration?string("0"),	2)}${lf(item.duration_units!,	1)}
				Frequency     :  ${lf(item.frequency!,  20)}
				Route         :  ${lf(item.route_name!,  20)}
				Doctor Remarks:  ${lf(item.doctor_remarks!,  50)}
				Other Remarks :  ${lf(item.sales_remarks!,  50)}
				Exp Date      :  ${lf(item.expiry_date!,  20)}
				Bill No.      :  ${lf(item.sale_id!,  20)}




				 [#if ((item.label_msg!'')!='')]
										${lf(item.label_msg!,  50)}
				 [/#if]

[/#if]
[/#list]
