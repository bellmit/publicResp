<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Store - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<c:set var="pagePath" value="<%=URLRoute.STORE_PATH%>" />
<script>
	function doClose() {
		window.location.href = "${cpath}${pagePath}/list.htm?sortOrder=dept_name"
				+ "&sortReverse=false&status=A";
	}
	function focus() {
		document.storemasterform.dept_name.focus();
	}
	function init() {
		document.storemasterform.template_name.value = '${bean.template_name}';
		document.storemasterform.presc_template_name.value = '${bean.presc_template_name}';
		document.storemasterform.presc_lbl_template_name.value = '${bean.presc_lbl_template_name}';
		enableAllowRaiseBill();
		enableSellingPriceForBatch();
	}

	function enableAllowRaiseBill() {
		var isSalesStore = document.getElementById('is_sales_store').value;
		var allowRaiseBillObj = document.getElementById('allowed_raise_bill');
		allowRaiseBillObj.disabled = (isSalesStore == 'N');
		if (isSalesStore == 'N')
			allowRaiseBillObj.checked = false;
	}
	function enableSellingPriceForBatch() {
		var storeTariffValue = document.getElementById("store_rate_plan_id").value;
		var sellingPriceForBatchObj = document
				.getElementById("batch_selling_price_id");
		sellingPriceForBatchObj.disabled = (storeTariffValue == '');
	}

	function validate() {
		var valid = true;
		var storeTypeObj = document.storemasterform.store_type_id;
		if (storeTypeObj.value == null || storeTypeObj.value == '') {
			alert('Store type is required');
			storeTypeObj.focus();
			return false;
		}
		var centerId = document.getElementById('center_id').value;
		if (centerId == '') {
			alert("Please select the center.");
			document.getElementById('center_id').focus();
			return false;
		}
		var autoPoGeneration = document
				.getElementById('allow_auto_po_generation_tmp').value;

		if (autoPoGeneration == 'Y') {
			valid = valid
					&& validateRequired(
							document.storemasterform.auto_po_generation_frequency_in_days,
							"Enter Frequency Days");

			if (!valid) {
				document.storemasterform.auto_po_generation_frequency_in_days
						.focus();
				return false;
			}
		} else {
			document.storemasterform.auto_po_generation_frequency_in_days.disabled = false;
			document.storemasterform.auto_po_generation_frequency_in_days.value = null;
		}

		var autoPoCancel = document.getElementById('allow_auto_cancel_po_tmp').value;

		if (autoPoCancel == 'Y') {
			valid = valid
					&& validateRequired(
							document.storemasterform.auto_cancel_po_frequency_in_days,
							"Enter Frequency Days");

			if (!valid) {
				document.storemasterform.auto_cancel_po_frequency_in_days
						.focus();
				return false;
			}
		} else {
			document.storemasterform.auto_cancel_po_frequency_in_days.disabled = false;
			document.storemasterform.auto_cancel_po_frequency_in_days.value = null;
		}

		document.storemasterform.submit();
	}

	function chooseAccountGroup() {
		var counterId = document.getElementById('counter_id').value;
		var accountGroups = document.getElementById('account_group');

		if (counterId != '') {
			if (window.XMLHttpRequest)
				req = new XMLHttpRequest();
			else if (window.ActiveXObject)
				req = new ActiveXObject("MSXML2.XMLHTTP");
			req.open("GET",
					"${cpath}/master/StoreMaster/list.do?_method=getAccountGrpId&counterId="
							+ counterId, true);
			req.setRequestHeader("Content-Type", "text/plain");
			req.send(null);
			req.onreadystatechange = function() {
				if (req.readyState == 4 && req.status == 200) {
					var accountGrpId = req.responseText;
					if (accountGrpId != 0) {
						for (var i = 0; i < accountGroups.length; i++) {
							if (accountGroups.options[i].value == accountGrpId) {
								accountGroups.options[i].selected = true;
								accountGroups.options[i].disabled = false;
							} else
								accountGroups.options[i].disabled = true;
						}
					} else {
						for (var j = 0; j < accountGroups.length; j++) {
							accountGroups.options[j].disabled = false;
						}
					}
				}
			}
		} else {
			for (var j = 0; j < accountGroups.length; j++) {
				accountGroups.options[j].disabled = false;
			}
		}
	}

	function checkAutoPOFields(obj) {
		if (obj.value == 'N') {
			document.storemasterform.allow_auto_po_generation.value = 'N';
			document.storemasterform.allow_auto_po_generation_tmp.value = 'N';
			document.storemasterform.auto_po_generation_frequency_in_days.value = '';

			document.storemasterform.allow_auto_cancel_po.value = 'N';
			document.storemasterform.allow_auto_cancel_po_tmp.value = 'N';
			document.storemasterform.auto_cancel_po_frequency_in_days.value = '';
		}
		document.storemasterform.allow_auto_po_generation_tmp.disabled = obj.value == 'N';
		document.storemasterform.auto_po_generation_frequency_in_days.readOnly = obj.value == 'N'
				|| document.storemasterform.allow_auto_cancel_po.value == 'N';

		document.storemasterform.allow_auto_cancel_po_tmp.disabled = obj.value == 'N';
		document.storemasterform.auto_cancel_po_frequency_in_days.readOnly = obj.value == 'N'
				|| document.storemasterform.allow_auto_cancel_po.value == 'N';
	}

	function checkFrequencyFields(obj, fieldId1, fieldId2) {
		document.getElementById(fieldId2).readOnly = obj.value == 'N';
		//document.storemasterform.auto_po_generation_frequency_in_days.readOnly = obj.value == 'N';
	}

	function setAutoPoValue(obj, fieldId1, fieldId2) {
		document.getElementById(fieldId1).value = obj.value;
		if (obj.value == 'Y') {
			document.getElementById(fieldId2).disabled = false;
			document.getElementById(fieldId2).value = 0;
		} else {
			document.getElementById(fieldId2).disabled = true;
			document.getElementById(fieldId2).value = '';
		}

	}
</script>

<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}"
	scope="request" />
</head>
<body onload="init();">
	<h1>Add Store</h1>

	<c:set var="actionUrl" value="${cpath}${pagePath}/create.htm" />

<form action="${actionUrl}"  name="storemasterform" method="POST">
		<insta:feedback-panel />
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Store:</td>
					<td><input type="text" name="dept_name"
						value="${bean.dept_name}" onblur="capWords(dept_name)"
						class="required validate-length" length="100"
						title="Name is required and max length of name can be 100" /></td>
					<c:choose>
						<c:when test="${genPrefs.max_centers_inc_default == 1}">
							<input type="hidden" name="center_id" id="center_id" value="0" />
						</c:when>
						<c:otherwise>
							<td class="formlabel">Center:</td>
							<td class="forminfo"><select class="dropdown"
								name="center_id" id="center_id">
									<option value="">-- Select --</option>
									<c:forEach items="${centers}" var="center">
										<c:if test="${center.center_id != 0}">
											<option value="${center.center_id}">${center.center_name}</option>
										</c:if>
									</c:forEach>
							</select></td>
						</c:otherwise>
					</c:choose>
					<td class="formlabel">Counter:</td>
					<td><select name="counter_id" id="counter_id" class="dropdown"
						onchange="chooseAccountGroup();">
							<option value="">--select counter--</option>
							<c:forEach items="${pharmacy_counters}" var="pCounter">
								<option value="${pCounter.counter_id}"
									${bean.counter_id == pCounter.counter_id?'selected':''}>${pCounter.counter_no}</option>
							</c:forEach>
					</select></td>

				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="status" value="${bean.status}"
							opvalues="A,I" optexts="Active,Inactive" /></td>
					<td class="formlabel">TIN Number:</td>
					<td><input type="text" name="pharmacy_tin_no"
						value="${bean.pharmacy_tin_no}" /></td>
					<td class="formlabel">Drug License Number:</td>
					<td><input type="text" name="pharmacy_drug_license_no"
						value="${bean.pharmacy_drug_license_no}" /></td>
				</tr>
				<tr>
					<td class="formlabel">Auto fill prescriptions:</td>
					<td><insta:selectoptions name="auto_fill_prescriptions"
							value="${bean.auto_fill_prescriptions}" opvalues="false,true"
							optexts="No,Yes" /></td>
					<td class="formlabel">Account Group:</td>
					<td><insta:selectdb name="account_group" id="account_group"
							value="${bean.account_group}" table="account_group_master"
							valuecol="account_group_id" displaycol="account_group_name" /></td>
					<c:if test="${taxLabel eq 'V'}">
						<td>Sales Account Prefix(VAT):</td>
					</c:if>
					<c:if test="${taxLabel eq 'G'}">
						<td>Sales Account Prefix(GST):</td>
					</c:if>
					<td><input type="text" name="sales_store_vat_account_prefix"
						id="sales_store_vat_account_prefix"
						value="${bean.sales_store_vat_account_prefix}" /></td>

				</tr>
				<tr>
					<c:if test="${taxLabel eq 'V'}">
						<td class="formlabel">Purchase Account Prefix(VAT):</td>
					</c:if>
					<c:if test="${taxLabel eq 'G'}">
						<td class="formlabel">Purchase Account Prefix(GST):</td>
					</c:if>
					<td><input type="text"
						name="purchases_store_vat_account_prefix"
						id="purchases_store_vat_account_prefix"
						value="${bean.purchases_store_vat_account_prefix}" /></td>
					<td class="formlabel">Purchase Account Prefix(CST):</td>
					<td><input type="text"
						name="purchases_store_cst_account_prefix"
						id="purchases_store_cst_account_prefix"
						value="${bean.purchases_store_cst_account_prefix}" /></td>
					<td class="formlabel">Store Type:</td>
					<td><insta:selectdb name="store_type_id"
							value="${bean.store_type_id}" table="store_type_master"
							valuecol="store_type_id" displaycol="store_type_name"
							filtered="false" dummyvalue="..select.." /></td>
				</tr>
				<tr>
					<td class="formlabel">Is Super Store:</td>
					<td><insta:selectoptions name="is_super_store"
							value="${bean.is_super_store}" opvalues="Y,N" optexts="Yes,No"
							onchange="checkAutoPOFields(this)" /></td>
					<td class="formlabel">Sale Units:</td>
					<td><insta:selectoptions name="sale_unit"
							value="${bean.sale_unit}" opvalues="I,P" optexts="Issue,Package" />
					</td>
					<td class="formlabel">Allowed to raise Bill:</td>
					<td><insta:selectoptions name="allowed_raise_bill"
							value="${bean.allowed_raise_bill}" opvalues="N,Y"
							optexts="No,Yes" /></td>
				</tr>
				<tr>
					<td class="formlabel">Prescription Print Template:</td>
					<td><select name="presc_template_name"
						id="presc_template_name" class="dropdown">
							<option value="BUILTIN_HTML">Built-in Default Template</option>
							<option value="BUILTIN_TEXT">Built-in Text Template</option>
							<c:forEach var="t" items="${presc_templates}">
								<option value="${t.template_name}">${t.template_name}</option>
							</c:forEach>
					</select></td>
					<td class="formlabel">Prescription Label Print Template:</td>
					<td><select name="presc_lbl_template_name"
						id="presc_lbl_template_name" class="dropdown">
							<option value="BUILTIN_HTML">Built-in Default Template</option>
							<option value="BUILTIN_TEXT">Built-in Text Template</option>
							<c:forEach var="t" items="${presc_lbl_templates}">
								<option value="${t.template_name}">${t.template_name}</option>
							</c:forEach>
					</select></td>
					<td class="formlabel">Sales Print Template:</td>
					<td><select name="template_name" id="template_name"
						class="dropdown">
							<option value="BUILTIN_HTML">Built-in Default Template</option>
							<option value="BUILTIN_TEXT">Built-in Text Template</option>
							<c:forEach var="t" items="${templates}">
								<option value="${t.template_name}">${t.template_name}</option>
							</c:forEach>
					</select></td>
				</tr>
				<tr>
					<td class="formlabel">Is Sales Store:</td>
					<td><insta:selectoptions name="is_sales_store"
							id="is_sales_store" value="${bean.is_sales_store}" opvalues="Y,N"
							optexts="Yes,No" onchange="enableAllowRaiseBill()" /></td>
					<td class="formlabel">Auto Fill Indents:</td>
					<td><insta:selectoptions name="auto_fill_indents"
							value="${bean.auto_fill_indents}" opvalues="false,true"
							optexts="No,Yes" /></td>
					<td class="formlabel">Is Sterile Store:</td>
					<td><insta:selectoptions name="is_sterile_store"
							value="${bean.is_sterile_store}" opvalues="N,Y"
							dummyvalue="--None--" dummyvalueId="" optexts="No,Yes" /></td>
				</tr>
				<tr>
					<td class="formlabel">Stores Tariff:</td>
					<td class="forminfo"><insta:selectdb name="store_rate_plan_id"
							id="store_rate_plan_id" table="store_rate_plans"
							valuecol="store_rate_plan_id" displaycol="store_rate_plan_name"
							orderby="store_rate_plan_name" value="${bean.store_rate_plan_id}"
							dummyvalue="Default" dummyvalueId=""
							onchange="enableSellingPriceForBatch()" /></td>
					<td class="formlabel">Use Selling Prices from Item Batch:</td>
					<td class="forminfo"><insta:selectoptions name="use_batch_mrp"
							value="${bean.use_batch_mrp}" opvalues="N,Y" optexts="No,Yes" />
						<img class="imgHelpText" src="${cpath}/images/help.png"
						title='<insta:ltext key="master.stores.addstore.batchsellingpricepreference"/>' />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Allow auto PO generation:</td>
					<td class="forminfo"><insta:selectoptions
							name="allow_auto_po_generation_tmp"
							id="allow_auto_po_generation_tmp"
							value="${bean.allow_auto_po_generation}" opvalues="N,Y"
							optexts="No,Yes" disabled="${bean.is_super_store == 'N' }"
							onchange="setAutoPoValue(this,'allow_auto_po_generation','auto_po_generation_frequency_in_days');checkFrequencyFields(this,'allow_auto_po_generation','auto_po_generation_frequency_in_days')" />
						<input type="hidden" name="allow_auto_po_generation"
						id="allow_auto_po_generation"
						value="${bean.allow_auto_po_generation}" /></td>
					<td class="formlabel">PO generation Frequency(days):</td>
					<td class="forminfo"><input type="text"
						id="auto_po_generation_frequency_in_days"
						name="auto_po_generation_frequency_in_days"
						onkeypress="return enterNumOnlyzeroToNine(event)"
						value="${bean.auto_po_generation_frequency_in_days}"
						${bean.is_super_store == 'N' || bean.allow_auto_po_generation == 'N' ? 'readonly' : ''}
						autocomplete="off"
						<c:if test="${bean.allow_auto_po_generation != 'Y' }"> disabled </c:if> />
					</td>
					<td class="formlabel">Auto Cancel PO :</td>
					<td class="forminfo"><insta:selectoptions
							name="allow_auto_cancel_po_tmp" id="allow_auto_cancel_po_tmp"
							value="${bean.allow_auto_cancel_po}" opvalues="N,Y"
							optexts="No,Yes" disabled="${bean.is_super_store == 'N' }"
							onchange="setAutoPoValue(this,'allow_auto_cancel_po','auto_cancel_po_frequency_in_days');checkFrequencyFields(this,'allow_auto_cancel_po','auto_cancel_po_frequency_in_days')" />
						<input type="hidden" name="allow_auto_cancel_po"
						id="allow_auto_cancel_po" value="${bean.allow_auto_cancel_po}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Auto Cancel PO Frequency(days):</td>
					<td class="forminfo"><input type="text"
						id="auto_cancel_po_frequency_in_days"
						name="auto_cancel_po_frequency_in_days"
						onkeypress="return enterNumOnlyzeroToNine(event)"
						value="${bean.auto_cancel_po_frequency_in_days}"
						${bean.is_super_store == 'N' || bean.allow_auto_cancel_po == 'N' ? 'readonly' : ''}
						autocomplete="off"
						<c:if test="${bean.allow_auto_cancel_po != 'Y' }"> disabled </c:if> />
					</td>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions">
			<button type="button" accesskey="S" onclick="validate();">
				<b><u>S</u></b>ave
			</button>
			| <a href="javascript:void(0)" onclick="doClose();">Store List</a>
		</div>

	</form>
</body>
</html>
