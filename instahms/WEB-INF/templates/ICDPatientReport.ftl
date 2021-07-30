<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>MRD Codes Report</title>
</head>

<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy "]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
	[#setting number_format="####"]
-->
<body  style=" font: 12px;">

<h1>MRD Codes Report</h1>

[#escape x as x?html]
<div class="patientHeader" style="margin-bottom: 1em; margin-top: 1em;">
<table width="80%" border="0" cellspacing="0" cellpadding="0">

     <tr>
        <td width="10%">Name:</td>
        <td width="30%">${patient.salutation!} ${patient.patient_name!} ${patient.last_name!}</td>
        <td width="10%">MRNo:</td>
        <td width="30%">${patient.mr_no}</td>
    </tr>
     <tr>
        <td width="10%">Doctor:</td>
        <td width="30%">${patient.doctor_name!}</td>
        <td width="10%">VisitId:</td>
        <td width="30%">${patient.patient_id}</td>
    </tr>
     <tr>
        <td width="10%">Referred By:</td>
        <td width="30%">${patient.refdoctorname!}</td>
        <td width="10%">Dept:</td>
        <td width="30%">${patient.dept_name!}</td>
    </tr>
     <tr>
        <td width="10%">Age/Gender:</td>
        <td width="30%">${patient.age} ${patient.agein}/${patient.patient_gender}</td>
        <td width="10%">Ward/Bed:</td>
        <!--[#if (patient.alloc_bed_name)??]-->
        <td width="30%">${patient.alloc_ward_name!}/${patient.alloc_bed_name!}</td>
        <!--[/#if]-->
    </tr>
    <tr>
        <td width="10%">Blood Group:</td>
        <td width="30%">${patient.bloodgroup!}</td>
        <td width="25%">Discharge Type:</td>
        <td width="30%">${dis.discharge_type!}</td>
    </tr>
    <!--[#if ( patient.tpa_id)??] -->
	<tr>
		<td width="10%">TPA/Sponsor:</td>
		<td width="30%">${patient.tpa_name!} </td>
		<td width="10%">Rate Plan:</td>
		<td width="30%">${patient.org_name!}</td>
	</tr>
	<tr>
	  <td colspan="4">${""}</td>
	</tr>
<!--[/#if]-->
</table>
</div>
<!-- [#if patientDrConsultn?has_content] -->
	<table width="100%" class="detailList" id="consulcodes" >
		<tr>
			<th width="20%">Code</th>
			<th width="40%">Code Description</th>
			<th width="15%">Consultation Type</th>
			<th width="25%">Doctor Name</th>
		</tr>
<!--	[#list patientDrConsultn as patconsultation] -->
		<tr >
			<td width="20%">
<!--			[#assign code_type='Non E&M Code']
				[#if patconsultation.code_type?exists && ((patconsultation.code_type!'') != '')]
					[#assign code_type=patconsultation.code_type]
				[/#if] -->
				${patconsultation.item_code!}(${code_type})
			</td>
			<td width="40%" style="white-space: normal">${patconsultation.code_desc!}</td>
			<td width="15%">${patconsultation.consultation_type!}</td>
			<td width="25%">${patconsultation.doctor_name!}</td>
		</tr>
<!--	[/#list] -->
	</table>
<!-- [/#if] -->
                <!--[#if patientDiagnosis ??] -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
         <td colspan="4"><b>Diagnosis Codes:</b></td>
      </tr>
    <!--[#list patientDiagnosis as patdiagnosis]-->
    <!--[#if patdiagnosis.map.diag_type == 'P']-->

     <tr>
        <td width="25%">Primary Diagnosis(Type):</td>
        <td width="25%">${patdiagnosis.map.icd_code}(${patdiagnosis.map.code_type})</td>
        <td width="25%">Code Description:</td>
        <td width="75%" >${patdiagnosis.map.code_desc}</td>
    </tr>

    <!--[/#if]-->
    <!--[/#list]-->
    <!--[#list patientDiagnosis as patdiagnosis]-->
    <!--[#if patdiagnosis.map.diag_type == 'A']-->

     <tr>
        <td width="25%">Admitting Diagnosis(Type):</td>
        <td width="25%">${patdiagnosis.map.icd_code}(${patdiagnosis.map.code_type})</td>
        <td width="25%">Code Description:</td>
        <td width="75%" >${patdiagnosis.map.code_desc}</td>
    </tr>
    <!--[/#if]-->
    <!--[/#list]-->
    
    <!--[#list patientDiagnosis as patdiagnosis]-->
    <!--[#if patdiagnosis.map.diag_type == 'V']-->

     <tr>
        <td width="25%">Reason For Visit Diagnosis(Type):</td>
        <td width="25%">${patdiagnosis.map.icd_code}(${patdiagnosis.map.code_type})</td>
        <td width="25%">Code Description:</td>
        <td width="75%" >${patdiagnosis.map.code_desc}</td>
    </tr>
    <!--[/#if]-->
    <!--[/#list]-->

    <!--[#list patientDiagnosis as diag]-->
    <!--[#if diag.map.diag_type == 'S']-->
     <tr>
        <td width="25%">Secondary Diagnosis(Type):</td>
        <td width="25%">${diag.map.icd_code}(${diag.map.code_type})</td>
       	<td width="25%">Code Description:</td>
        <td width="75%" >${diag.map.code_desc}</td>
    </tr>
    <tr>
	  <td colspan="4">${""}</td>
	</tr>
    <!--[/#if]-->
    <!--[/#list]-->
   </table>
   <!--[/#if]-->
    <!--[#if patientEncCodes??] -->
   <div style="margin-bottom: 1em; margin-top: 1em;">
   <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr style="margin-top: 1em;">
         <td colspan="4"><b>Encounter Codes:</b></td>
      </tr>

     <tr>
        <td width="30%">Encounter Type:</td>
        <td width="30%">${patientEncCodes.encounter_type!} - ${patientEncCodes.enc_type_desc!}</td>
        <td width="10%">Encounter Start:</td>
        <td width="30%">${patientEncCodes.encounter_start_type!} - ${patientEncCodes.enc_start_type_desc! }</td>
    </tr>

    <tr>
        <td width="30%">Encounter End:</td>
        <td width="30%">${patientEncCodes.encounter_end_type! }- ${patientEncCodes.enc_end_type_desc! }</td>
        <td width="10%">${""}</td>
        <td width="30%">${""}</td>
     </tr>

    <tr>
	  <td colspan="4">${""}</td>
	</tr>
    </table>
    </div>
    <!--[/#if]-->
     <!--[#if patientTrtCodes??] -->
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
         <td colspan="7"><b>Treatment Codes:</b></td>
      </tr>

   <tr class="border-above-below">
		<th width="15%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Bill No</th>
		<th width="15%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Treatment Date</th>
		<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Order#</th>
		<th width="20%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Type</th>
		<th width="20%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Item</th>
		<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code Type</th>
		<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code</th>
  </tr>
		<!--[#if patientTrtCodes??] -->
	 <!--[#list patientTrtCodes as trt]-->
	 <tr>
		<td width="15%">${trt.bill_no}</td>
		<td width="15%">${trt.posted_date}</td>
		<td width="10%">${trt.order_number!}</td>
		<td width="20%">${trt.chargehead_name}</td>
		<td width="20%">${trt.act_description!}</td>
		<td width="10%">${trt.code_type!}</td>
		<td width="10%">${trt.act_rate_plan_item_code!}</td>
    </tr>
    <!--[/#list]-->
    <!--[/#if] -->

    </table>
    <!--[/#if]-->

     <!--[#if patientLoincCodes??] -->
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
	     <tr>
	     	<td colspan="7"><b>LOINC Codes:</b></td>
	     </tr>
	  <tr class="border-above-below">
		<th width="15%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Date</th>
		<th width="25%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Item</th>
		<th width="20%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Result Name</th>
		<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code Type</th>
		<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code</th>
  	  </tr>

     <!--[#list patientLoincCodes as loinc]-->
	 <tr>
		<td width="15%">${loinc.conducted_date}</td>
		<td width="25%">${loinc.test_name}</td>
		<td width="20%">${loinc.resultlabel}</td>
		<td width="10%">${loinc.code_type!}</td>
		<td width="10%">${loinc.result_code!}</td>
    </tr>

    <!--[/#list]-->
    </table>
    <!--[/#if]-->

	<!--[#if patientDrgCodes??] -->
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
         <td colspan="7"><b>DRG Codes:</b></td>
      </tr>

	   <tr class="border-above-below">
			<th width="15%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Bill No</th>
			<th width="15%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Date</th>
			<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Type</th>
			<th width="20%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Rule</th>
			<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Item</th>
			<th width="10%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code Type</th>
			<th width="8%"  align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Code</th>
			<th width="20%" align="left" style="border-top: 1px solid black;border-bottom: 1px solid black;">Desc</th>
	  </tr>
	 <!--[#list patientDrgCodes as drg]-->
	 <tr>
		<td width="15%">${drg.bill_no}</td>
		<td width="15%">${drg.posted_date}</td>
		<td width="10%">${drg.chargehead_name!}</td>
		<td width="20%">${drg.act_remarks!}</td>
		<td width="10%">${drg.act_description!}</td>
		<td width="10%">${drg.code_type!}</td>
		<td width="8%">${drg.code!}</td>
		<td width="20%">${drg.description!}</td>
    </tr>
    <!--[/#list]-->
    </table>
    <!--[/#if] -->

	<!--[#if format == "screen"] -->
		<div  style="margin-top: 2em">
			<form method="GET" action="" target="_blank">
				<input type="submit" value="Print">
				<input type="hidden" name="_method" value="print"/>
				<input type="hidden" name="format" value="pdf"/>
				<input type="hidden" name="patient_id" value="${patient.patient_id}"/>
				<input type="hidden" name="code_type" value="${codeType}"/>
				<!-- [#if category ??] -->
					<!-- [#list category as cat]-->
						<input type="hidden" name="category" value="${cat}" />
					<!--[/#list] -->
				<!--[/#if] -->
			</form>
		</div>
	<!--[/#if] -->
[/#escape]
</body>
</html>
