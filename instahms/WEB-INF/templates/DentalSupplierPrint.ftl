[#assign supplies_order_id=supplies_order_id?number]
[#assign supplierBean = queryToDynaBean("SELECT *, hcm.center_name FROM dental_supplies_order dso
	JOIN dental_supplier_master dsm ON (dso.supplier_id=dsm.supplier_id)
	JOIN doctors d ON (d.doctor_id=dso.ordered_by)
	JOIN hospital_center_master hcm ON (hcm.center_id=dso.center_id)
	WHERE supplies_order_id=?", supplies_order_id)]
[#assign patientBean = queryToDynaBean("select * from patient_details_display_view where mr_no=?", supplierBean.mr_no)]

<h1>Dental Supplies Order for ${supplierBean.supplier_name!?html}</h1>
<hr/>
<table width="100%">
	<tr>
		<td width="100px">Patient Name: </td>
		<td>${patientBean.full_name?html}</td>
		<td width="100px">Center Name: </td>
		<td>${supplierBean.center_name}</td>
	</tr>
	<tr>
		<td width="100px">Order Date : </td>
		<td>${supplierBean.ordered_date}</td>
		<td width="100px">Order by : </td>
		<td>${supplierBean.doctor_name?html}</td>
	</tr>
</table>
<hr/>
[#assign items = queryToDynaList(
	"SELECT * FROM dental_supplies_items dsi
		JOIN dental_supplies_master dsm	ON (dsi.item_id=dsm.item_id)
		LEFT JOIN dental_shades_master shades ON (dsi.shade_id=shades.shade_id)
		WHERE supplies_order_id=?", supplies_order_id)]
<table width="100%" style="margin-top: 10px;" cellspacing="0">
	<tr>
		<th width="30%" >Item</th>
		<th width="20%" >Shade</th>
		<th width="10%" >Order Qty</th>
		<th width="10%" >Rec'd Qty</th>
		<th width="30%" >Remarks</th>
	</tr>
<!-- [#list items as item] -->
	<tr>
		<td>${item.item_name?html}</td>
		<td>${item.shade_name!?html}</td>
		<td>${(item.item_qty!0)?string('#')}</td>
		<td>${(item.received_qty!0)?string('#')}</td>
		<td>${item.item_remarks!?html}</td>
	</tr>
<!-- [/#list] -->
</table>
