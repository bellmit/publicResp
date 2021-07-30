<html xmlns="http://www.w3.org/1999/xhtml">
<head>

</head>
<!--
<#setting number_format="##">
<#setting date_format="dd-MM-yyyy"> -->
<body>
<!--<#escape x as x?html>-->
	<div align="center">
   		<b>OutSource Sample Details</b>
	</div>
 <br/>
	<table width="100%" border="0">
		<tr>
			<td class="label">From: ${hospName!""}</td>
    	    <td class="label">To: ${details.outsource_name!""}</td>
    	</tr>
    	<tr>
			<td class="label">Patient Name: ${details.patient_full_name!""}</td>
    	    <td class="label">Age: ${details.age!""}/${details.agein!""}</td>
    	</tr>
    	<tr>
			<td class="label">Gender: ${details.patient_gender!""}</td>
    	    <td class="label">MRNo: ${details.mr_no!""}</td>
    	</tr>
    	<tr>
			<td class="label">Sample No: ${details.sample_sno!""}</td>
    	    <td class="label">Prescribed Doctor: ${details.doctor_name!""}</td>
    	</tr>
    	<tr>
    	    <td colspan="4"><hr style="margin-down: 10px" /></td>
    	 </tr>
	</table>
	<table width="100%" border="0">
	 <tr>
	     <td><b> Please carry out the following investigation.</b></td>
	 </tr>
	   <!-- <#list sampleDetails as sample> -->
     <tr>
	     <td><b>Test Name:</b>${sample.test_name}</td>
	 </tr>
	 <tr>
    	  <td></td>
     </tr>
   <!-- <#assign presid = "${sample.prescribed_id![]}"> -->
   <!-- <#if labels?has_content>   -->
		<!--   <#list labels[presid] as label>  -->
			 <tr>
			    <td><li>${label}:</li></td>
			</tr>
 			<!--    </#list>  -->
 	<!--  </#if>  -->
<!-- </#list> -->
   <tr>
       <td><b>Laboratory use only</b></td>
   </tr>
   <tr align="right"><td><b>Signature</b></td></tr>
	</table>
<!--</#escape>-->
  </body>
</html>
