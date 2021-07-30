[#assign patientBean = queryToDynaBean("SELECT * FROM patient_details_display_view WHERE mr_no=?", mr_no)]
[#assign treatments = queryToDynaList("SELECT s.service_name, unit_charge, discount, unit_charge-discount as amount,
	CASE WHEN ttd.qty IS NOT NULL THEN ttd.qty ELSE 0 END as qty, ttd.planned_qty FROM tooth_treatment_details ttd JOIN services s USING (service_id)
	LEFT OUTER JOIN service_org_details sod ON sod.org_id=ttd.org_id  AND sod.service_id=s.service_id
	JOIN service_master_charges smc ON (smc.service_id=s.service_id and smc.org_id=sod.org_id and smc.bed_type='GENERAL')
	WHERE ttd.mr_no=? and ttd.treatment_status='P'", mr_no)]
[#assign totalAmount = 0]

<table width="100%">
	<tr>
		<td width="150px">Patient Name: </td>
		<td>${patientBean.full_name}</td>
	</tr>
</table>
<hr/>

<h2>Treatment Plan: </h2>
<table style="margin: top: 10px" width="100%">
	<tr>
		<th width="65%">Service</th>
		<th width="15%">Rate</th>
		<th width="5%">Planned Qty</th>
		<th width="15%">Planned Amt.</th>

	</tr>
<!-- [#list treatments as treatment]
	 [#assign totalAmount = totalAmount + (treatment.amount*treatment.planned_qty)] -->
	<tr>
		<td>${treatment.service_name?html}</td>
		<td >${treatment.amount}</td>
		<td>${treatment.planned_qty}</td>
		<td>${(treatment.amount)*treatment.planned_qty}</td>
	</tr>
<!-- [/#list] -->
<!-- [#if treatments?has_content] -->
	<tr>
		<td width="100%" style="text-align: right; border-top: 1px solid" colspan="3">Total: ${totalAmount}</td>
	</tr>
<!-- [/#if] -->
</table>