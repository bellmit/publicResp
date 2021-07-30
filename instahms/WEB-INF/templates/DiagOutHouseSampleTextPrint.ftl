[#include "TextPrintMacros.ftl"]
[#setting number_format="##"]
[#setting date_format="dd-MM-yyyy"]
[#escape x as x?html]
[@cfill "OutSource Sample Details" 80/]

[#assign from = "${hospName} "]
[#assign to = "${details.outsource_name} "]
[#assign name = "${details.patient_full_name}"]
[#assign age = "${details.age?string('#')} ${details.agein}"]
[#assign sex = "${details.patient_gender}"]


From:        ${lf(from!,               46)} To:                 ${lf(to,                    16)}
Patient Name:${lf(name,                46)} Age:                ${lf(age,                   16)}
Gender:      ${lf(sex,                 46)} MR No:              ${lf(details.mr_no,         16)}
Sample No:   ${lf(details.sample_sno,  46)} Prescribed Doctor:  ${lf(details.doctor_name!,  16)}
_______________________________________________________________________________________________
Please carry out the following investigation.
[#list sampleDetails as sample]

Test Name:${sample.test_name}

[#assign presid = "${sample.pprescribed_id}"]
[#list labels[presid] as label]
${label}:-----------------------------------------------------------------------------------------
[/#list]
[/#list]

Laboratory use only                                                             Signature
[/#escape]
