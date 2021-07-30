[#include "TextPrintMacros.ftl"]
[#assign name = "${visitdetails.full_name!}"]
[#assign age = "${visitdetails.age} ${visitdetails.agein} ${visitdetails.patient_gender}"]
[#assign address = "${visitdetails.patient_address!}"]
[#assign location = "${visitdetails.cityname!}, ${visitdetails.statename!}"]
[#assign doctor="${visitdetails.doctor_name!}"]
[#assign department = "${visitdetails.dept_name!}"]
[#assign rateplan]
	 ${visitdetails.org_name}
 [/#assign]
[#assign referredto = "${visitdetails.referred_to_hosp!}"]
[#assign sponsor="${visitdetails.tpa_name!}"]
[#assign mrno="${visitdetails.mr_no}"]
[#assign visitid = "${visitdetails.patient_id}"]
[#assign regdate="${visitdetails.reg_date!} ${visitdetails.reg_time!}"]
[#assign dischargedate= "${visitdetails.discharge_date!} ${visitdetails.discharge_time!}"]
[#assign bedtype]
	[#if (modules_activated.mod_ipservices!'') == 'Y']
		[#if (visitdetails.alloc_bed_name!'')=='' ](Not Allocated)
		[#else]${(visitdetails.alloc_ward_name)!}/${(visitdetails.alloc_bed_name)!}
		[/#if]
	[#else]
		${(visitdetails.reg_ward_name)!}/${(visitdetails.bill_bed_type)!}
	[/#if]
[/#assign]
[#assign refrred="${(visitdetails.refdoctorname)!}"]
[#assign datelabel]
[#if (visitdetails.visit_type)! = 'i']Admission Date:
[#else]Visit Date:
[/#if]
[/#assign]
[#assign disType=(visitdetails.discharge_type)!'']
[#assign dischaergedatelable]
[#if disType == 'Expiry']
	Death Date:
[#else]Discharge Date:
[/#if]
[/#assign]


Name:       ${lf(name,              40)}[#if ((label!'')!='')]${label1!}:${lf(labelvalue1!,   16)}[/#if]
Age/Gender: ${lf(age,               40)}[#if ((label2!'')!='')]${label2!}:${lf(labelvalue2!,     16)}[/#if]
Address:    ${lf(address,           40)}MR No:${lf(mrno,  16)}
Location:   ${lf(location,          40)}Visit ID:${lf(visitid,   16)}
Doctor:     ${lf(doctor,            40)}${datelabel}${lf(regdate,   16)}
Department: ${lf(department,        40)}${dischaergedatelable}${lf(dischargedate,  16)}
[#if visitdetails.visit_type = 'i']Ward/Bed:${lf(bedtype,   16)}[/#if]
Rate Plan:  ${lf(rateplan,          40)}[#if ((visitdetails.refdoctorname!'')!='')]Referred By:${lf(refrred,   16)}[/#if]
Sponsor:    ${lf(sponsor,           40)}
Referred To:${lf(referredto,        40)}
