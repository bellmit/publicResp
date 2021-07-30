[#assign name="${visitdetails.full_name!}"]
[#assign age="${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}"]

Name:  ${lf(name,    40)}Age/Gender: ${lf(age,           16)}
MR No: ${lf(visitdetails.mr_no,     40)}Contact No:${lf(visitdetails.patient_phone!,  16)}

