<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="DecimalDigits" value="<%= GenericPreferencesDAO.getGenericPreferences().getDecimalDigits() %>"/>
<insta:js-bundle prefix="stores.suppliercontracts.itemrate"/>
<title><insta:ltext key="suppliercontracts.itemrate.addshow.title"/></title>
<script>
	
	function setValue(num){
		 document.getElementById("_update_supplier_rate"+num).value = 'true';
		 var rateValue = document.getElementById("supplier_rate"+num).value.trim();
		 if(isNaN(rateValue) || rateValue == '' ){
			// showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
			 document.getElementById("supplier_rate"+num).value='';
		 }else{
			document.getElementById("supplier_rate"+num).value = round(parseFloat(document.getElementById("supplier_rate"+num).value), ${DecimalDigits});
		 }
	}
	
	function setDiscountValue(num){
		 document.getElementById("_update_discount"+num).value = 'true';
		 var discountValue = document.getElementById("discount"+num).value.trim();
		 if(isNaN(discountValue) || discountValue == '' ){
			 //showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
			 document.getElementById("discount"+num).value='';
		 }else{
			 if(discountValue > 100){
				 showMessage("js.stores.suppliercontracts.itemrate.discountnot.greaterthan100percent");
			 }else{
				 document.getElementById("discount"+num).value =  round(parseFloat(document.getElementById("discount"+num).value), ${DecimalDigits});
			 }
		 }
	}
	
	function setMrpValue(num){
		document.getElementById("_update_mrp"+num).value = 'true';
		var mrpValue = document.getElementById("mrp"+num).value.trim();
		if(isNaN(mrpValue) || mrpValue == '' ){
		 //showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
		 document.getElementById("mrp"+num).value='';
		}else{
			document.getElementById("mrp"+num).value =  round(parseFloat(document.getElementById("mrp"+num).value), ${DecimalDigits});
		}
	}

	function setMarginValue(num){
		document.getElementById("_update_margin"+num).value = 'true';
		var marginValue = document.getElementById("margin"+num).value.trim();
		if(isNaN(marginValue) || marginValue == '' ){
		 document.getElementById("margin"+num).value='';
		}else{
			document.getElementById("margin"+num).value =  round(parseFloat(document.getElementById("margin"+num).value), ${DecimalDigits});
		}
	}

	function setMarginTypeValue(num){
		document.getElementById("_update_margin_type"+num).value = 'true';
	}

	function validateForm(){
		 var rate_len = document.getElementsByName("supplier_rate").length;
		 var i;
		 for( i=0;i<rate_len;i++){
			var margin = document.getElementById("margin"+i).value.trim();
			var margin_type = document.getElementById("margin_type"+i).value.trim();
			var discountValue = document.getElementById("discount"+i).value.trim();
			var supplier_rate = document.getElementsByName("supplier_rate")[i].value;
			
			if(discountValue > 100){
				showMessage("js.stores.suppliercontracts.itemrate.discountnot.greaterthan100percent");
				document.getElementsByName("discount")[j].focus();
				return false;
			}
			if(margin || margin ===0){
				if(!margin_type){
					showMessage("js.stores.suppliercontracts.itemrate.select.margin.type");
					return false;
				}
			}else if(!supplier_rate || parseFloat(supplier_rate) <= parseFloat(0).toFixed(2)){
				showMessage("js.stores.suppliercontracts.itemrate.enternumber");
				document.getElementsByName("supplier_rate")[i].focus();
				return false;
			}

			if(margin_type == 'P' && margin > 100){
				showMessage("js.stores.suppliercontracts.margin.greater.than.hundred");
				return false;
			}
		 }

		 SupplierItemRatesListSearchForm.submit();
	}
</script>
</head>
<body onload="">
	<form  name="SupplierItemRatesListSearchForm" action="SupplierContractItemRates.do" method="get"> 
		<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
		<input type="hidden" name="supplier_code" id="supplier_code" value="${param.supplier_code}"/>
        <input type="hidden" name="medicine_id" id="medicine_id" value="${param.medicine_id}"/>
        <%-- <input type="hidden" name="sortOrder"  value="validity_end"/>
        <input type="hidden" name="sortReverse"  value="true"/>
        <input type="hidden" name="_supplier_status"  value="${param._supplier_status}"/>
        <input type="hidden" name="_item_status"  value="${param._item_status}"/> --%>
 		<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
		<input type="hidden" name="_method"  value="update"/>
		<input type="hidden" name="supplier_rate_contract_id"  value="${param.supplier_rate_contract_id}"/>
		
	
		<table width="100%">
			<tr> 
				<td width="100%"><h1><insta:ltext key="suppliercontracts.itemrate.addshow.title"/> </h1></td>
				<td>&nbsp; &nbsp;</td>
			</tr>
	    </table>		
		   
		<insta:feedback-panel/>
			
		<fieldset class="fieldSetBorder" id="SupplierItemRates"><legend class="fieldSetLabel"><insta:ltext key="suppliercontracts.itemrate.addshow.itemdatails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="80%" > 
				<tr>
					<td class="formlabel" ><insta:ltext key="suppliercontracts.itemrate.addshow.itemname"/>: </td>
					<td class="forminfo" >
						<%-- <input type="hidden" name="_medicine_name" id="_medicine_name" maxlength="100" value="${param._medicine_name}" /> --%>
 					   <label>${pagedList.dtoList[0].medicine_name}</label>
 					</td>
 					<td class="formlabel" ><insta:ltext key="suppliercontracts.itemrate.addshow.pkg.size"/>: </td>
					<td class="forminfo" >
 					   <label>${pagedList.dtoList[0].issue_base_unit}</label>
 					</td>
					<td class="formlabel" width="100px"><insta:ltext key="suppliercontracts.itemrate.addshow.suppliercontractitem.itemstatus"/>:</td>
					<td class="forminfo" width="100px">
						<c:if test="${pagedList.dtoList[0].item_status eq 'A'}"><insta:ltext key="master.supplierratecontract.raisecontract.status.active"/></c:if>
						<c:if test="${pagedList.dtoList[0].item_status eq 'I'}"><insta:ltext key="master.supplierratecontract.raisecontract.status.inactive"/></c:if>
					</td>
					<td class="formlabel"></td>
					<td class="forminfo"></td>
				</tr>
			 </table> 
		 </fieldset>
		
		<fieldset class="fieldSetBorder" id="SupplierItemRates"><legend class="fieldSetLabel"><insta:ltext key="suppliercontracts.itemrate.addshow.itemrates"/></legend>
			<table class="dashboard" id="baseItemTbl" cellpadding="0" cellspacing="0" width="100%">
				<tr class="header">
				    <th style="width: 180px"><insta:ltext key="suppliercontracts.itemrate.list.suppliercontractname"/></th> 
					<th style="width: 180px"><insta:ltext key="suppliercontracts.itemrate.addshow.suppliername"/></th> 
					<th style="width: 180px"><insta:ltext key="suppliercontracts.itemrate.addshow.contratenddate"/></th>
					<th style="width: 100px"><insta:ltext key="suppliercontracts.itemrate.addshow.rate"/></th> 
					<th style="width: 100px"><insta:ltext key="suppliercontracts.itemrate.addshow.discount"/></th>
					<th style="width: 100px"><insta:ltext key="suppliercontracts.itemrate.addshow.mrp"/></th>  
					<th style="width: 100px"><insta:ltext key="suppliercontracts.itemrate.addshow.margin"/></th>  
					<th style="width: 100px"><insta:ltext key="suppliercontracts.itemrate.addshow.margin_type"/></th>  
				</tr>
				
				<c:forEach var="itemList" items="${pagedList.dtoList}" varStatus="st">
					<tr>
						<%-- <input type="hidden" name="supplier_rate_contract_id"  value="${itemList.supplier_rate_contract_id}" /> --%>
						<td>${itemList.supplier_rate_contract_name}</td>
						<td>
							<c:if test="${itemList.sup_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${itemList.sup_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${itemList.supplier_name}
						</td>
						<fmt:parseDate value="${itemList.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
						<td>${to}</td>
						<td>
							<input type="text" name="supplier_rate" id="supplier_rate${st.index}" maxlength="13" onchange="setValue(${st.index})" value="${itemList.supplier_rate}"  ${itemList.status == 'I' ? 'disabled' : '' }/>
							<input type="hidden" name="_update_supplier_rate"  id="_update_supplier_rate${st.index}" value="false" />
						</td>
						<td>
							<input type="text" name="discount" id="discount${st.index}" maxlength="13" onchange="setDiscountValue(${st.index})" value="${itemList.discount}"  ${itemList.status == 'I' ? 'disabled' : '' }/>
							<input type="hidden" name="_update_discount"  id="_update_discount${st.index}" value="false" />
						</td>
						<td>
							<input type="text" name="mrp" id="mrp${st.index}" maxlength="13" onchange="setMrpValue(${st.index})" value="${itemList.mrp}"  ${itemList.status == 'I' ? 'disabled' : '' }/>
							<input type="hidden" name="_update_mrp"  id="_update_mrp${st.index}" value="false" />
						</td>
						<td>
							<input type="text" name="margin" id="margin${st.index}" maxlength="13" onchange="setMarginValue(${st.index})" value="${itemList.margin}"  ${itemList.status == 'I' ? 'disabled' : '' }/>
							<input type="hidden" name="_update_margin"  id="_update_margin${st.index}" value="false" />
						</td>
						<td>
							<select name="margin_type" id="margin_type${st.index}" onchange="setMarginTypeValue(${st.index})"  ${itemList.status == 'I' ? 'disabled' : '' }>
								<option value="">--Select--</option>
								<option value="P" ${itemList.margin_type == 'P' ? 'selected' : '' }>Percent</option>
								<option value="A" ${itemList.margin_type == 'A' ? 'selected' : '' }>Amount</option>
							</select>
							<input type="hidden" name="_update_margin_type"  id="_update_margin_type${st.index}" value="false" />
						</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</fieldSet>
		
		<div class="screenActions">
			<c:if test="${(pagedList.dtoList[0].sup_status == 'A' && pagedList.dtoList[0].item_status == 'A') || (pagedList.dtoList[0].supplier_code == '-1')}" > 
				<button type="button" accesskey="S" onclick="return validateForm();"><b><u><insta:ltext key="suppliercontracts.itemrate.addshow.s"/></u></b><insta:ltext key="suppliercontracts.itemrate.addshow.ave"/></button>  	
			</c:if> 
			<insta:screenlink screenId="mas_supplier_contract_item_rates" addPipe="${pagedList.dtoList[0].sup_status == 'A' && pagedList.dtoList[0].item_status == 'A' ? 'true' : 'false'}" label="Supplier Contract Item Rates Lists"
				extraParam="?_method=list&status=A" title="${contractitemlist}"/>				
			<insta:screenlink screenId="mas_supplier_rate_contracts" addPipe="${pagedList.dtoList[0].sup_status == 'A' && pagedList.dtoList[0].item_status == 'A' ? 'true' : 'false'}" label="Supplier Rate Contracts List"
				extraParam="?_method=list&status=A"/>			
			<insta:screenlink screenId="mas_supplier_rate_contracts" addPipe="${pagedList.dtoList[0].sup_status == 'A' && pagedList.dtoList[0].item_status == 'A' ? 'true' : 'false'}" label="Add Supplier Item Rates"
				extraParam="?_method=addScreen&status=A&supplier_rate_contract_id=${param.supplier_rate_contract_id}&supplier_code=${param.supplier_code}"/>				
			<div  class="legend" style="float: right;display: ${hasResults? 'block' : 'none'}" >
		  		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		  		<div class="flagText">Active</div>
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive</div>
			</div>
		</div>		
	</form>
</body>
</html>