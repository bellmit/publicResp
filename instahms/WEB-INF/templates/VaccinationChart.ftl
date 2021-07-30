<html xmlns="http://www.w3.org/1999/xhtml">
<#setting date_format="dd-MM-yyyy">
<head>
 <title>Patients Vaccination</title>
 
</head>
<body>
    <div class="patientHeader">
      <table style="font-size:12px;"  width='100%'>
      <tbody>
       <tr>
        <td>MR No: ${(mr_no)!" "}</td>
        <td>Name: ${(patient_details.full_name)!" "}</td>
        <td>Age: ${(patient_details.age?string("0"))!" "}${(patient_details.agein)!" "}
      <#if patient_details.dateofbirth??>(${(patient_details.dateofbirth)!" "})</#if>
        </td>
       </tr>
       <tr>
        <td>Gender: ${(patient_details.patient_gender)!" "}</td>
        <td>Contact No: ${(patient_details.patient_phone)!" "}</td>
        <td></td>
       </tr>
       </tbody>
      </table>
    </div>
    <h4>Patient Vaccinations</h4>
    <div>
      <table  style="font-size:12px; border: 1px solid #CCCCCC;"  width='100%'>
        <tr>
          <td align="left" style=" border-bottom: 1px solid #000000;" width="15%">Vaccine</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="5%">Dose</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="5%">Recm Age</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="15%">Due Date</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="10%">Status</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="15%">Reason</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="15%">Date Admn</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="10%">Admn by</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="15%">Medicine/Manuf/Batch/Exp Date</td>
          <td align="center" style=" border-bottom: 1px solid #000000;" width="5%">Remarks</td>
        </tr>
            <#list vaccinations as vaccination>
        <tr>
            <td style="border-bottom: 1px solid #CCCCCC;" align="left" width="15%">${((vaccination.vaccine_name)!" ")?html}</td> 
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="5%">${(vaccination.dose_num?string("0"))!" "}</td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="5%">${(vaccination.recommended_age?string("0"))!" "}
             ${(vaccination.age_units)!" "}<#if (vaccination.recommended_age!0) gt 1>s</#if>
             </td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="15%">${(vaccination.due_date)!" "}</td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="10%">${(vaccination.vaccination_status)!" "}</td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="15%">${((vaccination.reason_for_not)!" ")?html}</td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="15%">${(vaccination.vaccination_datetime)!" "}</td>
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="10%">${(vaccination.vacc_doctor_name)!" "}</td>
            
             <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="15%">
              <#if ((vaccination.med_name!'')?html!='')>
               ${((vaccination.med_name)!" ")?html}/
              ${((vaccination.manufacturer)!" ")?html}/
               ${((vaccination.batch)!" ")?html}/
               ${(vaccination.expiry_date)!" "}
             <#else>
             </#if>
             </td>
            
              
             
            <td style="border-bottom: 1px solid #CCCCCC;" align="center" width="5%">${((vaccination.remarks)!" ")?html}</td>
        </tr>
            </#list>
      </table>
    </div>
</body>
</html>
