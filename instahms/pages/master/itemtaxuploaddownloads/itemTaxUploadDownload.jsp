<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.TAX_UPLOAD_DOWNLOAD_PATH %>"/>
<title>Item Tax Upload/Download</title>

<script>
		function doUpload() {
		  
			var importform = document.importTaxGroupForm;
				if(document.getElementById("importItem").value == ""){
					alert("Please select item types import.");
					return false;
				}
				if (importform.uploadFile.value == "") {
					alert("Please browse and select a file to upload");
					return false;
				}
				importform.submit();
			}
			
		function doDownload(){
			if(document.getElementById("exportItem").value == ""){
				alert("Please select item types export.");
				return false;
			}
			if(document.getElementById("exportItem").value == "store_item_tariff_details" && document.getElementById("store_rate_plan_id").value == ""){
				alert("Please select store tariff");
				return false;
			}
			document.exportTaxGroupForm.submit();
		}
		
		function selectedStoreTariff() {
			if(document.getElementById("exportItem").value == "store_item_tariff_details") {
				document.getElementById("storeTariffLabel").style.display = "";
				document.getElementById("storeTariffValue").style.display = "";
			} else {
				document.getElementById("storeTariffLabel").style.display = "none";
				document.getElementById("storeTariffValue").style.display = "none";
			}
		}
		
</script>

</head>
<body>

<h1>Item Tax Upload/Download</h1>
<insta:feedback-panel/>
<fieldset class="fieldsetborder">

<c:set var="actionExportUrl" value="${cpath}${pagePath}/export.htm"/>
<form action="${actionExportUrl}" method="GET" name="exportTaxGroupForm" >

		<table>
			<tr>
				<td>Item Types Export:</td>
				<td>
					<select class="dropdown" name="exportItem"  id="exportItem" onChange="selectedStoreTariff()">
						<option value="" >--- Select ---</option>
							<option value="anesthesia_type_master" >Anesthesia</option>
							<option value="bed_types" >Bed Types</option>
							<option value="consultation_types" >Consultation Types</option>
							<option value="diagnostics">Diagnostics</option>
							<option value="diet_master" >dietary</option>
							<option value="drg_codes_master">DRG Code</option>
							<option value="equipment_master" >Equipment</option>
							<option value="operation_master" >Operation</option>
							<option value="theatre_master" >Operation Theatre</option>
							<option value="common_charges_master" >Other Charges</option>
							<option value="packages" >Package</option>
							<option value="per_diem_codes_master" >Per Diem Code</option>
							<option value="services" >Service</option>
							<option value="store_item_details" >Store Item</option>
							<option value="store_item_tariff_details" >Store Tariff Item</option>						
					</select>
				</td>
				<td id ="storeTariffLabel" style="display:none;">&nbsp; Stores Tariff:</td>
				<td id ="storeTariffValue" style="display:none;"><insta:selectdb name="store_rate_plan_id"
							id="store_rate_plan_id" table="store_rate_plans"
							valuecol="store_rate_plan_id" displaycol="store_rate_plan_name"
							orderby="store_rate_plan_name"
							dummyvalue="Default" dummyvalueId="" /></td>
				<td class="forminfo">
				&nbsp;
					<button type="button" onclick="return doDownload()">Download</button>
				</td>
			</tr>
			
		</table>
</form>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
<c:set var="actionImportUrl" value="${cpath}${pagePath}/import.htm"/>
<form action="${actionImportUrl}" method="POST" name="importTaxGroupForm" enctype="multipart/form-data" >
	<table >
		<tr>
			<td>Item Types Import:</td>
					<td>
						<select class="dropdown" name="importItem"  id="importItem">
							<option value="" >--- Select ---</option>
							<option value="anesthesia_type_master" >Anesthesia</option>
							<option value="bed_types" >Bed Type</option>
							<option value="consultation_types" >Consultation Types</option>
							<option value="diagnostics">Diagnostics</option>
							<option value="diet_master" >dietary</option>
							<option value="drg_codes_master">DRG Code</option>
							<option value="equipment_master" >Equipment</option>
							<option value="operation_master" >Operation</option>
							<option value="theatre_master" >Operation Theatre</option>
							<option value="common_charges_master" >Other Charges</option>
							<option value="packages" >Package</option>
							<option value="per_diem_codes_master" >Per Diem Code</option>
							<option value="services" >Service</option>
							<option value="store_item_details" >Store Item</option>
							<option value="store_item_tariff_details" >Store Tariff Item</option>
							
						</select>
					</td>
					&nbsp; &nbsp; &nbsp;
			<td>
			&nbsp; &nbsp;
				<input type="file" name="uploadFile" id="uploadFile"  accept="<insta:ltext key="upload.accept.master"/>"/>
			</td>
			
			<td>	
				<button type="button" onclick="return doUpload()">Upload</button>
			</td>		
			
		</tr>
	</table>

</form>
</fieldset>
</body>
</html>