<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Cache-Control" content="no-cache"/>
<title>Registration Charges - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="masters/charges_common.js" />
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/registrationcharges.js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

	<script>
		var cpth  =  '${cpath}';
		function funSaveValues(){
			if(!validateAllDiscounts()) return false;
			document.regChargeForm.action=cpth+"/master/RegistrationCharges.do?method=update";
			document.regChargeForm.submit();
		}

		function validateAllDiscounts() {
			var charges = 5;
			var len = document.regChargeForm.ids.value;
			var valid = true;
			for(var j=0;j<charges;j++) {
				for(var i=0;i<len;i++) {
					valid = valid && validateDiscount('ip_reg_charge','ip_reg_charge_discount',i);
					valid = valid && validateDiscount('op_reg_charge','op_reg_charge_discount',i);
					valid = valid && validateDiscount('gen_reg_charge','gen_reg_charge_discount',i);
					valid = valid && validateDiscount('reg_renewal_charge','reg_renewal_charge_discount',i);
					valid = valid && validateDiscount('mrcharge','mrcharge_discount',i);
					valid = valid && validateDiscount('ip_mlccharge','ip_mlccharge_discount',i);
					valid = valid && validateDiscount('op_mlccharge','op_mlccharge_discount',i);
				}
			}
			if(!valid) return false;
			else return true;
		}

		function ratePlanChange() {
			document.regChargeForm.submit();
		}

		function doExport() {
			document.exportForm.orgId.value = document.regChargeForm.orgId.value;
			document.exportForm.submit();
		}

		function doUpload() {

			if (document.chargesImportForm.xlsRegistrationFile.value == "") {
				alert('Please browse and select a file to upload');
				return false;
			}
			document.chargesImportForm.orgId.value = document.regChargeForm.orgId.value;
		}
	</script>
</head>

<body onload="fillRatePlanDetails()">
	<h1>Registration Charges</h1>
	<insta:feedback-panel/>
	<form action="RegistrationCharges.do" method="POST" name="regChargeForm">
		<input type="hidden" name="method" value="show"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr style="float:left">
					<td class="formlabel">Rate Sheet:</td>
					<td>
						<insta:selectdb name="orgId" id="orgId" value="${orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" dummyvalue="-- Select --" onchange="ratePlanChange();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
					</td>
				</tr>

			</table>
		</fieldset>

		<div class="resultList">
			<table id="regCharges" cellpadding="0" cellspacing="0" class="dataTable">
				<tr>
					<th>BedType</th>
					<c:forEach items="${bedTypes}" var="bed" varStatus="k">
					<c:set var="j" value="${k.index}"/>
						<th>${bed}</th>
						<input type="hidden" name="beds" value="<c:out value='${bed}'/>"/>
					</c:forEach>
					<input type="hidden" name="ids" value="${j+1}">
				</tr>
				<tr>
					<td style="text-align: right">Ip Visit Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_reg_charge" class="number validate-decimal"
								id="ip_reg_charge${i.index}" value="${bean.map.ip_reg_charge}"
								onblur="validateDiscount('ip_reg_charge','ip_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip Visit Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_reg_charge_discount" class="number validate-decimal"
								id="ip_reg_charge_discount${i.index}" value="${bean.map.ip_reg_charge_discount}"
								onblur="validateDiscount('ip_reg_charge','ip_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op Visit Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_reg_charge" class="number validate-decimal"
								id="op_reg_charge${i.index}" value="${bean.map.op_reg_charge}"
								onblur="validateDiscount('op_reg_charge','op_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op Visit Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_reg_charge_discount" class="number validate-decimal"
								id="op_reg_charge_discount${i.index}" value="${bean.map.op_reg_charge_discount}"
								onblur="validateDiscount('op_reg_charge','op_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="gen_reg_charge" class="number validate-decimal"
								id="gen_reg_charge${i.index}" value="${bean.map.gen_reg_charge}"
								onblur="validateDiscount('gen_reg_charge','gen_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="gen_reg_charge_discount" class="number validate-decimal"
								id="gen_reg_charge_discount${i.index}" value="${bean.map.gen_reg_charge_discount}"
								onblur="validateDiscount('gen_reg_charge','gen_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Renewal Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="reg_renewal_charge" class="number validate-decimal"
								id="reg_renewal_charge${i.index}" value="${bean.map.reg_renewal_charge}"
								onblur="validateDiscount('reg_renewal_charge','reg_renewal_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Renewal Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="reg_renewal_charge_discount" class="number validate-decimal"
								id="reg_renewal_charge_discount${i.index}" value="${bean.map.reg_renewal_charge_discount}"
								onblur="validateDiscount('reg_renewal_charge','reg_renewal_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Medical Record Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="mrcharge" class="number validate-decimal"
								id="mrcharge${i.index}" value="${bean.map.mrcharge}"
								onblur="validateDiscount('mrcharge','mrcharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Medical Record Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="mrcharge_discount" class="number validate-decimal"
								id="mrcharge_discount${i.index}" value="${bean.map.mrcharge_discount}"
								onblur="validateDiscount('mrcharge','mrcharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip MLC Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_mlccharge" class="number validate-decimal"
								id="ip_mlccharge${i.index}" value="${bean.map.ip_mlccharge}"
								onblur="validateDiscount('ip_mlccharge','ip_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip MLC Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_mlccharge_discount" class="number validate-decimal"
								id="ip_mlccharge_discount${i.index}" value="${bean.map.ip_mlccharge_discount}"
								onblur="validateDiscount('ip_mlccharge','ip_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op MLC Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_mlccharge" class="number validate-decimal"
								id="op_mlccharge${i.index}" value="${bean.map.op_mlccharge}"
								onblur="validateDiscount('op_mlccharge','op_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op MLC Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_mlccharge_discount" class="number validate-decimal"
								id="op_mlccharge_discount${i.index}" value="${bean.map.op_mlccharge_discount}"
								onblur="validateDiscount('op_mlccharge','op_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<c:if test="${not empty bedTypes}">
				<tr>
					<td style="text-align: right">Apply Charges To All</td>
					<td><input type="checkbox" name="checkbox" onclick="fillValues('regCharges', this);"/></td>
					<c:forEach begin="2" end="${fn:length (bedTypes)}" >
						<td>&nbsp;</td>
					</c:forEach>
				</tr>
				</c:if>
			</table>
		</div>

		<div id="ratePlanDiv" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
				<table class="dashBoard" id="ratePlanTbl">
					<tr class="header">
						<td>Rate Plan</td>
						<td>Discount / Markup</td>
						<td>Variation %</td>
						<td>&nbsp;</td>
					</tr>
					<tr id="" style="display: none">
				</table>
				<table class="screenActions" width="100%">
					<tr>
						<td align="right">
							<img src='${cpath}/images/blue_flag.gif'>Overridden
						</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<table class="screenActions">
		<tr>
			<td><button type="button" accesskey="S" name="Save" onclick="funSaveValues();"><b><u>S</u></b>ave</button></td>
		</tr>
		</table>
	</form>
	<div style="padding-top: 14px" id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Export/Import</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
	</div>
	<fieldset class="fieldSetBorder">
	<table style="padding-top: 3px">
		<tr>
			<th colspan="2" align="left" style="padding-bottom: 9px;"><u>Export/Import Charges</u></th>
		</tr>
		<tr>
		<td>Export:	</td>
		<td>
			<form name="exportForm" action="RegistrationCharges.do">
				<div style="float: left">
					<input type="hidden" name="method" value="exportRegChargesToXls" />
					<input type="hidden" name="orgId" value="" />
					<button name="button" accesskey="D" type="submit" onclick="doExport();"><b><u>D</u></b>ownload</button>
				</div>
				<div style="white-space: normal">
					<img class="imgHelpText"
						 src="${cpath}/images/help.png"
						 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new charges will be updated.Note that this must be done for one Rate Sheet at a time.
						 "/>
				</div>
			</form>
		</td>
		</tr>
		<tr>
		<td style="padding-top: 13px;">Import: </td>
		<td style="padding-top: 13px;">
			<form name="chargesImportForm" action="RegChargeUpload.do" enctype="multipart/form-data" method="POST">
				<input type="hidden" name="method" value="importRegistrationCharges" />
				<input type="hidden" name="org_id" value="" />
					<input type="file" name="xlsRegistrationFile" id="xlsRegistrationFile" accept="<insta:ltext key="upload.accept.master"/>"/>
					<button type="button" accesskey="U"
						onclick="return validateImportChargesFile(this, 'xlsRegistrationFile', 'orgId');"><b><u>U</u></b>pload</button>
			</form>
		</td>
		</tr>
	</table>
	</fieldset>

	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		var cpath = '${cpath}';
	</script>
</body>
</html>
