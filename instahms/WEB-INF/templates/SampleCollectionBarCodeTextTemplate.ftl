^Q38,3
^W75
^H15
^P1
^S4
^AT
^C1
^R0
~Q+0
^O0
^D0
^E12
~R200
^L
Dy2-me-dd
Th:m:s
BA,63,146,3,7,100,0,0,${sample_no}
AC,25,38,1,1,0,0,NAME:
AC,130,38,1,1,0,0,[#if patient.visit_type != 'in']${(patient.full_name)!}[#else]${(patient.patient_name)!}[/#if]
AC,25,81,1,1,0,0,AGE:
AC,126,81,1,1,0,0,${patient.age}/[#if patient.visit_type != 'in']${patient.agein}[#else]${patient.age_unit}[/#if]
AC,218,81,1,1,0,0,SEX:
AC,298,81,1,1,0,0,${patient.patient_gender}
AE,168,253,1,1,0,0,${sample_no}
E
