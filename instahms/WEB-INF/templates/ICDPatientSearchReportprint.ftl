<html xmlns="http://www.w3.org/1999/xhtml">

<!--[#assign i=0]-->
<div align="left"><hr height="1px" /></div>
<div align="center" style="font-size: 40pt;"><b>ICD Report</b></div>
<div align="left"><hr height="1px" /></div>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
          <td width="2%">#</td>
          <td width="12%">MRNo</td>
          <td width="12%">VisitId</td>
          <td width="12%">Patient Name</td>
          <td width="12%">Doctor</td>
          <td width="25%">Diagnosis Code</td>
          <td width="25%">Treatment Code</td>
      </tr>
      <tr>
      <td colspan="7"><hr height="1px" /></td>
      </tr>
      <!--[#list patientsList as patient]-->
      <!--[#assign i=i+1]-->
       <tr>
          <td width="4%">${(i?string.number)?html}</td>
          <td width="11%">${(patient.mr_no)?html}</td>
          <td width="11%">${(patient.patient_id)?html}</td>
          <td width="18%">${(patient.salutation)?html} ${(patient.patient_name)?html} ${(patient.last_name)?html}</td>
          <td width="10%">${(patient.doctor_name!)?html}</td>
          <td width="23%">${(patient.diagnosis_icd!)?html}</td>
          <td width="25%">${(patient.treatment_icd!)?html}</td>
       </tr>

      <!--[/#list]-->
</table>
</body>
</html>