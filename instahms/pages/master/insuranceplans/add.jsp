<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.INSURANCE_PLANS_PATH %>"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Plan Master- Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="planMaster/policy.js" />
	<insta:js-bundle prefix="masters.insurance.common"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<style>
		#myDialog_mask.mask {
			    z-index: 1;
			    display: none;
			    position: absolute;
			    top: 0;
			    left: 0;
			    -moz-opacity: 0.0001;
			    opacity: 0.0001;
			    filter: alpha(opacity=50);
			    background-color: #CCC;
		}

		#myDialog1_mask.mask {
			    z-index: 1;
			    display: none;
			    position: absolute;
			    top: 0;
			    left: 0;
			    -moz-opacity: 0.0001;
			    opacity: 0.0001;
			    filter: alpha(opacity=50);
			    background-color: #CCC;
		}
	</style>
	<script>
		<%-- var jChargeHeads = <%= request.getAttribute("itemCatMapJSON") %>; --%>
		var numberOfSystemCategory = 0;
		var jChargeHeads = ${ifn:convertListToJson(insuPayableList)}
		var subTotAmtPaise = 0;
		var mapPlanCat = '${bean.category_id}';
		//var id = <%= request.getAttribute("tpa_id") %>;
		
		var tpaLists = ${ifn:convertListToJson(tpaMasterLists)};
		var insuNameTpaList = ${ifn:convertListToJson(insuranceCompaniesTpaLists)};
		var catNameList = ${ifn:convertListToJson(categoryLists)};

		var corporateInsu = '${corpInsurance}';
		
		function populateInsuranceCategory() {
			var i=0;
			var catSel = document.createElement('select');
			var prevCatSel = document.getElementById('category_id');
			prevCatSel.parentNode.replaceChild(catSel,prevCatSel);
			catSel.id = "category_id";
			catSel.name = "category_id";
			catSel.setAttribute ('class','dropdown');
			catSel.disabled = true;

			for(var j=0; j<catNameList.length; j++) {
				var catEle = catNameList[j];
				if(catEle.insurance_co_id == document.policyMasterForm.insurance_co_id.value) {
					catSel.removeAttribute("disabled");
					catSel.length = i;
					var catOpt = document.createElement('option');
					catOpt.text = catEle.category_name;
					catOpt.value = catEle.category_id;
					catSel.appendChild(catOpt);
					i++
				}
			}
			if(catSel.length< 1) {
			 	catSel.title = 'No plan type present for this insurance company...';
			 	var catEmptOpt = document.createElement('option');
				catEmptOpt.text = 'No Plan Type Avlb.';
				catEmptOpt.value = '';
				catSel.appendChild(catEmptOpt);
			} else {
				catSel.title = '';
			}
			sortDropDown(catSel);
		}

		function imposeMaxLength(Event, Object, MaxLen) {
       		 return (Object.value.length <= MaxLen)||(Event.keyCode == 8 ||Event.keyCode==46||(Event.keyCode>=35&&Event.keyCode<=40))
        }

        function markApplicable(Object){
			if(Object.checked){
				if(Object.id == 'ipApplicablez') {
					document.getElementById('ip_applicable').value = 'Y';

				}else if(Object.id == 'opApplicablez') {
					document.getElementById('op_applicable').value = 'Y';

				}else if(Object.id == 'requirePbmAuthz') {
					document.getElementById('require_pbm_authorization').value = 'Y';
				
				}else if(Object.id == 'op_episode_visit') {
					document.getElementById('limits_include_followup').value = 'Y';
				}
			}else{
				if(Object.id == 'ipApplicablez'){
					document.getElementById('ip_applicable').value = 'N';

				}else if(Object.id == 'opApplicablez') {
					document.getElementById('op_applicable').value = 'N';

				}else if(Object.id == 'requirePbmAuthz') {
					document.getElementById('require_pbm_authorization').value = 'N';
				
				}else if(Object.id == 'op_episode_visit') {
					document.getElementById('limits_include_followup').value = 'N';
				}
			 }
        }

        function setPrePostDiscountFlag(obj){
        	if(obj.checked == true){
        		document.getElementById("is_copay_pc_on_post_discnt_amt").value = 'Y';
        	} else {
        		document.getElementById("is_copay_pc_on_post_discnt_amt").value = 'N';
        	}
        }
        
        function enableAndDisabledPlanItemCategory(){
        	//var itemCatHidden = document.getElementById("dlg_itemCatId");
        	//var pat_typ=document.getElementById('_dlg_patient_type');
        	var e = document.getElementById("_category_payable_fld");
        	var _cpVal = e.options[e.selectedIndex].value;
        	//document.getElementById(itemCatHidden.value+"_category_payable_"+pat_typ.value).value=_cpVal;
        	if(_cpVal=='N'){
	   			document.getElementById("_dlg_cat_amt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_amt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_perc_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_trmnt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_cap_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_category_prior_auth_required_fld").disabled=true;
	   		}else{
	   			document.getElementById("_dlg_cat_amt_fld").removeAttribute("readOnly");
	   			document.getElementById("_dlg_amt_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_perc_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_trmnt_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_cap_fld").removeAttribute("readonly");
	   		 	document.getElementById("_category_prior_auth_required_fld").disabled=false;
	   		}
        	
        }
        function enableAndDisabledPlanItemCategory1(){
        	//var itemCatHidden = document.getElementById("dlg_itemCatId");
        	//var pat_typ=document.getElementById('_dlg_patient_type');
        	var e = document.getElementById("_category_payable_fld1");
        	var _cpVal = e.options[e.selectedIndex].value;
        	//document.getElementById("_category_payable_"+pat_typ.value).value=_cpVal;
        	if(_cpVal=='N'){
	   			document.getElementById("_dlg_cat_amt_fld1").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_amt_fld1").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_perc_fld1").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_trmnt_fld1").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_cap_fld1").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_category_prior_auth_required_fld1").disabled=true;
	   		}else{
	   			document.getElementById("_dlg_cat_amt_fld1").removeAttribute("readOnly");
	   			document.getElementById("_dlg_amt_fld1").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_perc_fld1").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_trmnt_fld1").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_cap_fld1").removeAttribute("readonly");
	   		 	document.getElementById("_category_prior_auth_required_fld1").disabled=false;
	   		}
        	
        }
        
        function limitTypeChange() {
        	var limitType = document.getElementById("limit_type");
        	var selectedLimitType = limitType.options[limitType.selectedIndex].value;
        	if('R' == selectedLimitType) {
        		if (!confirm("Do you want to create case rate based plan")) {
        			limitType.selectedIndex = 0;
        			return false;
        		} else {      			
        			hideDrgPerdiemSection();
        			hideCopayAndLimitSection();
        		}
        	} else {
        		showDrgPerdiemSection();
    			showCopayAndLimitSection();
        	}
        	categoriesMapped();
        }
        
        function hideDrgPerdiemSection () { 	    	
  	    	document.getElementById("base_rate").value ='';
  	    	document.getElementById("gap_amount").value ='';
  	    	document.getElementById("marginal_percent").value ='';
  	    	document.getElementById("add_on_payment_factor").value ='';
  	    	
  	    	document.getElementById("perdiem_copay_per").value ='';
  	    	document.getElementById("perdiem_copay_amount").value ='';
  	    	 	
  	    	var caseRateCnt = document.getElementById("case_rate_count");
  	    	caseRateCnt.disabled = false;
  	    	caseRateCnt.innerText = null;
  	    	var option0 = document.createElement("option");
  	      	option0.text = '-- Select --';
  	    	option0.value = '';
  	    	caseRateCnt.add(option0);
  	      	var option1 = document.createElement("option");
  	      	option1.text = "1";
  	      	option1.value = '1';
  	      	caseRateCnt.add(option1);
  	      	var option2 = document.createElement("option");
	      	option2.text = "2";
	      	option2.value = '2';
	      	caseRateCnt.add(option2);
  	    	
  	    	document.getElementById("corporateInsu2").style.display='none';
  	    	document.getElementById("corporateInsu3").style.display='none';
        }
        
        function hideCopayAndLimitSection() {
        	document.getElementById("opCopayLimit").style.display='none';
  	    	document.getElementById("ipCopayLimit").style.display='none';
  	    	document.getElementById("selectAllOpCharges").style.display='none';
  	    	document.getElementById("selectAllIpCharges").style.display='none';
  	    	document.getElementById('op_episode_visit').checked =false;
  	    	
  	    	document.getElementById("op_planlimit").value ='0.00';
  	    	document.getElementById("op_visit_limit").value ='0.00';
  	    	document.getElementById("op_episode_limit").value ='0.00';
  	    	document.getElementById("op_visit_deductible").value ='0.00';
  	    	document.getElementById("op_visit_copay").value ='0.00';
  	    	document.getElementById("op_visit_copay_limit").value ='0.00';
  	    	
  	    	document.getElementById("ip_planlimit").value ='0.00';
  	    	document.getElementById("episode_visit_ip").value ='0.00';
  	    	document.getElementById("ip_per_day_limit").value ='0.00';
  	    	document.getElementById("ip_visit_deductible").value ='0.00';
  	    	document.getElementById("ip_visit_copay").value ='0.00';
  	    	document.getElementById("ip_visit_copay_limit").value ='0.00';
        }

		function showDrgPerdiemSection () {
			document.getElementById("corporateInsu2").style.display='';
			document.getElementById("corporateInsu3").style.display='';
			
			var caseRateCnt = document.getElementById("case_rate_count");
			caseRateCnt.innerText = null;
			var option0 = document.createElement("option");
			option0.text = '-- Select --';
			option0.value = '';
			caseRateCnt.add(option0);
			caseRateCnt.disabled = true;
		}
        
        function showCopayAndLimitSection() {
        	document.getElementById("opCopayLimit").style.display='';
  	    	document.getElementById("ipCopayLimit").style.display='';
  	    	document.getElementById("selectAllOpCharges").style.display='';
  	    	document.getElementById("selectAllIpCharges").style.display='';
        }
        
        function limitTypeOnPageLoad() {
        	var limitType = document.getElementById("limit_type");
        	var selectedLimitType = limitType.options[limitType.selectedIndex].value;
        	if('R' == selectedLimitType) {
        		hideDrgPerdiemSection();
    			hideCopayAndLimitSection();
        	}
        }
        
	</script>
</head>

<body onload="init();categoriesMapped();limitTypeOnPageLoad();">
         <h1 >Add Plan Details</h1>
          <br />
    

	<insta:feedback-panel/>

	<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
	<form onsubmit="return validateForm();" name="policyMasterForm" action="${actionUrl}" method="POST" >
	
		<input type="hidden" name="_method" value="create"/>
		<%-- <input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/> --%>
		<input type="hidden" name="plan_id" id="plan_id" value="${bean.plan_id}"/>
		<input type="hidden" name="updtdChargeHeadList" id="updtdChargeHeadList" value=""/>
		<input type="hidden" name="ip_applicable" id="ip_applicable" value="${bean.ip_applicable==null?'N':bean.ip_applicable}"/>
		<input type="hidden" name="op_applicable" id="op_applicable" value="${bean.op_applicable==null?'N':bean.op_applicable}"/>
		<input type="hidden" name="require_pbm_authorization" id="require_pbm_authorization" value="${bean.require_pbm_authorization==null?'N':bean.require_pbm_authorization}"/>
		<input type="hidden" name="limits_include_followup" id="limits_include_followup" value="${bean.limits_include_followup==null?'N':bean.limits_include_followup}"/>
		
		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Plan Details</legend>

			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" align="left">
				<tr>
					<td class="formlabel">Plan Name :</td>
					<td class="forminfo">
						<input type="text" name="plan_name" id="plan_name" maxlength="100" value="${bean.plan_name}" />
					</td>
					
					 <td class="formlabel">Insurance Company Name:</td> 
					<td class="forminfo">
						<insta:selectdb name="insurance_co_id"  table="insurance_company_master" valuecol="insurance_co_id"
								displaycol="insurance_co_name"  filtered="false" 
									value="${bean.insurance_co_id}" orderby="insurance_co_name" onchange="populateInsuranceCategory();populateInsuranceTpaname();categoriesMapped();" onblur="populateInsuranceCategory();populateInsuranceTpaname();"/>
					</td>
					
					<td class="formlabel">Status :</td>
					<td>
						<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I"
						optexts="Active,Inactive" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Valid From:</td>
					<td>
						<fmt:formatDate value="${bean.insurance_validity_start_date}" pattern="dd-MM-yyyy" var="from_date"/>
						<insta:datewidget name="insurance_validity_start_date" value="${from_date}" id="insurance_validity_start_date"
							btnPos="left" title="Valid from start" />
					</td>
					<td class="formlabel">Valid Upto:</td>
					<td>
						<fmt:formatDate value="${bean.insurance_validity_end_date}" pattern="dd-MM-yyyy" var="to_date"/>
						<insta:datewidget name="insurance_validity_end_date" value="${to_date}" id="insurance_validity_end_date"
							 btnPos="left" title="Valid Upto" />
					</td>
					<td class="formlabel"><insta:ltext key="masters.insurance.common.interfacecode" />:</td>
					<td><input type="text" name="interface_code" maxlength="100" value="${ifn:cleanHtmlAttribute(bean.interface_code)}"/></td>
				</tr>
				<tr>
					<td class="formlabel">Plan Notes :</td>
					<td class="forminfo" colspan="5"><TEXTAREA COLS=95 ROWS=3 name="plan_notes"   id="plan_notes" >${bean.plan_notes}</TEXTAREA></td>
				</tr>
				<tr>
					<td class="formlabel">Plan Exclusions :</td>
					<td class="forminfo" colspan="5"><TEXTAREA COLS=95 ROWS=3 name="plan_exclusions"  id="plan_exclusions" >${bean.plan_exclusions}</TEXTAREA></td>
				</tr>
				<tr>
				
					<c:if test="${mod_eclaim_pbm}">
					<td class="formlabel" title="Require PBM Authorization">Require PBM Auth.:</td>
					<td class= "forminfo">
						<input type="checkbox" name="requirePbmAuthz" id="requirePbmAuthz" value="Y" onclick="markApplicable(this);"
							<c:if test="${bean.require_pbm_authorization == 'Y' }"> checked </c:if> />
					</td>
					</c:if>
				
					
					 <td class="formlabel">Last Modified User :</td>
					<td class="forminfo"><b></b>--</td> 
					<td class="formlabel">Last Modified Date :</td>
					<td class="forminfo"><b></b>--</td>
					<td class="formlabel">Plan Code:</td>
					<td><input type="text" name="plan_code" id="plan_code" maxlength="30" value="${ifn:cleanHtmlAttribute(bean.plan_code)}"/></td>
				</tr>
				<%-- <tr>
					<td class="formlabel">Over-All Treatment Limit :</td>
					<td class="forminfo"><input type="text" name="overall_treatment_limit" maxlength="8" class="numeric" onkeypress="return enterNumAndDotAndMinus(event);" value="${bean.overall_treatment_limit}"/></td>
					
				</tr> --%>
				<%-- Ins30: Corporate Insurance Begin
				<c:if test="${corpInsurance eq 'Y'}">
				<tr>
					<td class="formlabel">Plan Valid From:</td>
					<td>
						<fmt:formatDate value="${bean.insurance_validity_start_date}" pattern="dd-MM-yyyy" var="start_date"/>
						<insta:datewidget name="insurance_validity_start_date" value="${start_date}" id="insurance_validity_start_date"
							btnPos="left" title="Plan validity start" />
						<span class="star" id="sd1" style="visibility:hidden">*</span>
					</td>
					<td class="formlabel">Plan Valid Till:</td>
					<td>
						<fmt:formatDate value="${bean.insurance_validity_end_date}" pattern="dd-MM-yyyy" var="end_date"/>
						<insta:datewidget name="insurance_validity_end_date" value="${end_date}" id="insurance_validity_end_date"
							 btnPos="left" title="Plan validity end" />
						<span class="star" id="se1" style="visibility:hidden">*</span>
					</td>
				</tr>
				</c:if>
			 	Ins30: Corporate Insurance End--%>
			</table>
		</fieldset>
		
		<fieldset class="fieldSetBorder" id=""><legend class="fieldSetLabel"> Contract Details  : </legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" align="left">
			<tr>
				<td class="formlabel"> Sponsor  : </td>
						<input type="hidden" name="tpaid" id="tpaid" value="${bean.sponsor_id}"/>
					
					<td class="forminfo"  style="" >  
								<select name="sponsor_id" id="sponsor_id" class="dropDown">
							       <option value=""></option>
						        </select>
					</td>
				
				<td class="formlabel">Plan Type :</td>
					<td class="forminfo" id="catStyle" style="" >
						<select name="category_id" id="category_id" class="dropDown">
							<option value=""></option>
						</select>
					</td>
				<td class="formlabel">  </td>
				<td></td>		
			</tr>
			<tr>
				<td class="formlabel">Default Rate Plan :</td>
					<td class="forminfo">
					<insta:selectdb name="default_rate_plan" table="organization_details" valuecol="org_id"
							displaycol="org_name"
							value="${bean.default_rate_plan}"  orderby="org_name" dummyvalue="-- Select --" dummyvalueId="" />
					</td>
				<td class="formlabel">  Default Discount Plan : </td>
				<td class= "forminfo">
					<select class= "dropdown"  name="discount_plan_id" >
					 	<option value=''>-- Select --</option>
					 	<c:forEach var="defaultDiscountPlan" items="${defaultDiscountPlanList}"  varStatus="st" >
					 		 <option value='${defaultDiscountPlanList[st.index].discount_plan_id}'  
					 		 	${defaultDiscountPlanList[st.index].discount_plan_id == bean.discount_plan_id ? 'selected' : ''} >
					 		 ${defaultDiscountPlanList[st.index].discount_plan_name}</option> 
					 	</c:forEach>
					</select>		 
				</td>		
			</tr>
			<tr>
				<td class="formlabel" title="if unchecked, adds co-pay percent on the pre-discounted amount">
						Co-pay % applicable on post-discounted amount :
					</td>
					<td class= "forminfo" title="if unchecked, adds co-pay percent on the pre-discounted amount">
						<input type="hidden" name="is_copay_pc_on_post_discnt_amt" id="is_copay_pc_on_post_discnt_amt"
							value="${not empty bean.is_copay_pc_on_post_discnt_amt? bean.is_copay_pc_on_post_discnt_amt : 'N'}" />
						<input type="checkbox" name="is_copay_pc_on_post_discnt_amt_chk"
							id="is_copay_pc_on_post_discnt_amt_chk" onclick="setPrePostDiscountFlag(this);"
							<c:if test="${ bean.is_copay_pc_on_post_discnt_amt eq 'Y' }"> checked </c:if> />
					</td>
				
				<td class="formlabel"> IP-Applicable :</td>
					<td class= "forminfo">
						<input type="checkbox" name="ipApplicablez" id="ipApplicablez" value="Y" onclick="markApplicable(this);"
							<c:if test="${bean.ip_applicable eq 'Y' }"> checked </c:if> />
					</td>
				
				<td class="formlabel"> OP-Applicable :</td>
					<td class= "forminfo">
						<input type="checkbox" name="opApplicablez" id="opApplicablez" value="Y" onclick="markApplicable(this);"
							<c:if test="${bean.op_applicable == 'Y' }"> checked </c:if> />
					</td>
			</tr>
			<tr>
				<td class="formlabel">Limit Type :</td>
				<td class= "forminfo">
					<select class= "dropdown" id="limit_type" name="limit_type" onchange="limitTypeChange();">
					 	<option value='C' selected>Category Based</option>
					 	<option value='R'>Case rate Based</option>
					 </select>
				</td>
				<td class="formlabel">No. of Case Rates Allowed:</td>
				<td class= "forminfo">
					<select class= "dropdown"  id="case_rate_count" name="case_rate_count" disabled>
					 	<option value='' selected>-- Select --</option>
					 	<option value='1'>1</option>
					 	<option value='2'>2</option>
					 </select>
				</td>
			</tr>
		</table>
		</fieldset>

		<c:if test="${corpInsurance ne 'Y'}">
		<fieldset class="fieldSetBorder" id="corporateInsu3"><legend class="fieldSetLabel"> DRG Payment Rules: </legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" align="left">
			<tr>
				<td class="formlabel"> Base Rate: </td>
				<td class= "forminfo">
					<input type="text" id="base_rate" name="base_rate" class="numeric"
						 	value="${bean.base_rate}"/>
						 <img class="imgHelpText"
 title="Base Rate is the established reimbursement rate that is used to multiply against the DRG Relative Weight to determine the reimbursement amount on a per case basis."
						 src="${cpath}/images/help.png"/>
				</td>
				<td class="formlabel"> Gap Amount: </td>
				<td class= "forminfo">
					<input type="text" id="gap_amount" name="gap_amount" class="numeric"
						 	value="${bean.gap_amount}"/>
						 <img class="imgHelpText"
 title="Gap Amount is the segment of cost to be borne by the provider that is above the calculated DRG reimbursement amount before the account becomes eligible for an outlier payment.
It is a set amount that should be reviewed periodically in conjunction with updates to the base rate by the facility."
						 src="${cpath}/images/help.png"/>
				</td>
				<td class="formlabel"> Marginal Percent(%): </td>
				<td class= "forminfo">
					<input type="text"  id="marginal_percent" name="marginal_percent" maxlength="6" class="numeric"
						 	value="${bean.marginal_percent}"/>
						 <img class="imgHelpText"
 title="Marginal Percent is the established percentage applied to the Costs above the Base Rate plus the Gap that will be reimbursed to a provider as the outlier payment.
If the sum of the Base Rate plus the Gap equal more than the total Costs,
there is no provision for an outlier payment."
						 src="${cpath}/images/help.png"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"> Add On Payment Factor(%): </td>
				<td class= "forminfo">
					<input type="text" id="add_on_payment_factor" name="add_on_payment_factor" maxlength="6" class="numeric"
						 	value="${bean.add_on_payment_factor == null ? 75 : bean.add_on_payment_factor}"/>
						 <img class="imgHelpText" title="Enter a percentage value between 0 and 100. Define 75% for HAAD"
						 src="${cpath}/images/help.png"/>
				</td>
			</tr>
		</table>
		</fieldset>

		<fieldset class="fieldSetBorder" id="corporateInsu2"><legend class="fieldSetLabel"> Perdiem Copay: </legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" align="left">
			<tr>
				<td class="formlabel"> Perdiem Co-pay(%): </td>
				<td class= "forminfo">
					<input type="text" id="perdiem_copay_per" name="perdiem_copay_per" class="numeric"
						 	value="${bean.perdiem_copay_per}"/>
						 <img class="imgHelpText"
 title="Perdiem Co-pay(%) is the percentage of total perdiem amount to be collected as co-pay for a patient."
						 src="${cpath}/images/help.png"/>
				</td>
				<td class="formlabel"> Perdiem Co-pay Amount: </td>
				<td class= "forminfo">
					<input type="text" id="perdiem_copay_amount" name="perdiem_copay_amount" class="numeric"
						 	value="${bean.perdiem_copay_amount}"/>
						 <img class="imgHelpText"
 title="Perdiem Co-pay Amount is the amount of copay to be collected from total perdiem amount of a patient."
						 src="${cpath}/images/help.png"/>
				</td>
				<td class="formlabel"> </td>
				<td></td>
			</tr>
		</table>
		</fieldset>
	    </c:if>

		<fieldset class="fieldSetBorder" id="corporateInsu1"><legend class="fieldSetLabel"> Copay & Limits - OP: </legend>
		<table id="opCopayLimit" class="formtable" cellpadding="0" cellspacing="0" width="100%">
			 <tr>
			    <td class="formlabel"><input type="checkbox" name="op_episode_visit" id="op_episode_visit" value="" onclick="updatevalues(this);markApplicable(this);"
			   							 <c:if test="${bean.limits_include_followup  == 'Y' }"> checked </c:if> />
			     </td>
			     <td> Limits Include Follow-up Visits</td>	
			    <%--  <td><input type="hidden" name="episode_visit_opvalue" id="episode_visit_opvalue" value="${bean.limits_include_followup  == 'Y' ? 'episode':'visit'}" /> --%>
			     		
			     </td>
			</tr>
			 <tr>
				<td class="formlabel"  >Plan Limit : </td>
					<td >
						<input type="text" name="op_plan_limit" id="op_planlimit" class="numeric"
						 	onkeypress="return enterNumOnly(event)" onchange="onPlanLimit(this,'op')"  value="${bean.op_plan_limit}"   maxlength="13"  />
					</td>
					<td class="formlabel">Visit Sponsor Limit :
					</td>
				    <td class="forminfo" >
						<input type="text" name="op_visit_limit" id="op_visit_limit"  class="numeric" value=""  
							onkeypress="return enterNumOnly(event)" onchange="onVisitSponsorLimit(this,'op')" maxlength="13" />
					</td>
					<td class="formlabel">Episode Sponsor Limit :
					</td>
				    <td class="forminfo" >
						<input type="text" name="op_episode_limit" id="op_episode_limit"  class="numeric" value="" 
							onkeypress="return enterNumOnly(event)" onchange="onVisitEpisodeSponsorLimit(this,'op')" maxlength="13" />
					</td>
			</tr> 
			<tr>
				<td class="formlabel" >Visit Deductible : </td>
					<td class="forminfo" >
						<input type="text" name="op_visit_deductible" id="op_visit_deductible" maxlength="13" value="${bean.op_visit_deductible}"
							onkeypress="return enterNumOnly(event)" onchange="onVisitdeductible(this,'op')" />
					</td>
				    <td class="formlabel" width="100px" >Visit Copay :</td>
					<td class="forminfo" >
						<input type="text" name="op_copay_percent" id="op_visit_copay" maxlength="13" value="${bean.op_copay_percent}"
							onkeypress="return enterNumOnly(event)" onchange="onVisitCopay(this,'op')" />%
					</td>
					<td class="formlabel">Visit Max Copay : 
					</td>
				    <td class= "forminfo">
					<input type="text" name="op_visit_copay_limit"  id="op_visit_copay_limit" class="numeric" maxlength="13"
						 	onkeypress="return enterNumOnly(event)" onchange="onMaxCopay(this,'op')" value="${bean.op_visit_copay_limit}"/>
						 <img class="imgHelpText"
 title="OP Co-pay Limit is the amount of copay to be collected from a patient for a OP visit."
						 src="${cpath}/images/help.png"/>
				</td>	    
			</tr>
			<c:if test="${mod_eclaim_preauth}">
				<tr>
					<input type="hidden" name="enable_pre_authorized_limit" id="enable_pre_authorized_limit" value="N"/>
				    <td class="formlabel"><input type="checkbox" name="enable_pre_authorized_limitz" value="Y" id="enable_pre_authorized_limitz" onclick="onEnablePreAuthAmtLimit(this)"
				   							 <c:if test="${bean.enable_pre_authorized_limit  == 'Y' }"> checked </c:if> />
				     </td>
				     <td> Enable Prior Authorization Visit Limit </td>
				     <td class="formlabel">Prior Authorization Visit Limit : </td>
				     <td class="forminfo" >
						<input type="text" name="op_pre_authorized_amount" id="op_pre_authorized_amount" maxlength="13" value="${bean.op_pre_authorized_amount}"
							onkeypress="return enterNumOnly(event)" onchange="" disabled/>
					</td>
					<td class="formlabel">Visit Limit Charge Group Exclusions : </td>
					<td>
						<insta:selectdb multiple="true" name="excluded_charge_groups_list" id="excluded_charge_groups_list" valuecol="chargegroup_id" table="chargegroup_constants" displaycol="chargegroup_name"
							filtercol="op_applicable"  filtervalue="Y" value="${bean.excluded_charge_groups}" usecache="true" onChange="onExcludeChargeGroupChange(this)" disabled="true"/>
						<input type="hidden" name="excluded_charge_groups" id="excluded_charge_groups" value="${bean.excluded_charge_groups}" />
					</td>
				</tr>
			</c:if>
		 </table> 
		
						<table class="detailList"  id="chargesTable_o" cellpadding="0" cellspacing="0" width="100%">
							<tr>
								<th><input type="checkbox" name="selAllChkbxo" id="selAllChkbxo" value="" onclick="selectAllForDiscounts('o');"/></th>
								<th>Plan Item-Category</th>
								<th align="right" style="text-align:right;">Sponsor Limit</i></th>
								<th align="right" style="text-align:right;">Deductible<i> (Cat)</i></th>
								<th align="right" style="text-align:right;">Deductible<i> (Item)</i></th>
								<th align="right" style="text-align:right;">Copay %</th>
								<th align="right" style="text-align:right;">Max Copay</th>
								<th>&nbsp;&nbsp;</th>
								<th >&nbsp;&nbsp;</th> 
							</tr>
							<tr style="display: none">
						</table>
						<table class="addButton">
							<tr style="text-align:right;">
								<td width="1000">Add New Insurance Item Categories</td>
								<td>
									<button type="button" name="addCategories_o" Class="imgButton" id="addCategories_o" onclick="addInruranceItemCategories(this,'o')" >
										<img name="addButton" src="${cpath}/icons/Add.png" />
									</button>
								</td>
							</tr>
						</table>
						<table id="selectAllOpCharges" style="padding-top:0;border-top:0 solid #E0E0E0" class="detailList" align="right" cellpadding="0" cellspacing="0" width="100%" >
							<tr align="right">
								<td style="padding-top:0;border-top:0 solid #E0E0E0" colspan="20">&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td style="padding-top:0;border-top:0 solid #E0E0E0; text-align:right;">Update All Selected Out-patient Charges &nbsp;&nbsp;<img src="${cpath}/icons/Edit.png" id="sel_img_o" onclick="return showDialogExt('o');" style="padding-right:0;"/></td>
							</tr>
						</table>
								
		 </fieldset> 
		 
		 <fieldset class="fieldSetBorder" id="corporateInsu1"><legend class="fieldSetLabel"> Copay & Limits - IP: </legend>
		<table id="ipCopayLimit" class="formtable" cellpadding="0" cellspacing="0" width="100%">
			
			 <tr>
				<td class="formlabel"  >Plan Limit : </td>
					<td >
						<input type="text" name="ip_plan_limit" id="ip_planlimit" class="numeric" maxlength="13"
						 	onkeypress="return enterNumOnly(event)" onchange="onPlanLimit(this,'ip')" value="${bean.ip_plan_limit}"   />
					</td>
					 <td class="formlabel">Visit Sponsor Limit :
					</td>
				    <td class="forminfo" >
						<input type="text" name="ip_visit_limit" id="episode_visit_ip"  class="numeric" value="${bean.ip_visit_limit}" 
						onkeypress="return enterNumOnly(event)" onchange="onVisitSponsorLimit(this,'ip')" maxlength="13" />
					</td>
					<td class="formlabel">Per Day Limit :
					</td>
				    <td class="forminfo" >
						<input type="text" name="ip_per_day_limit" id="ip_per_day_limit"  class="numeric"
						 	onkeypress="return enterNumOnly(event)" onchange="onPerDayLimit(this)" maxlength="13" value="${bean.ip_per_day_limit}"   />
					</td>
					
			</tr> 
			<tr>
				<td class="formlabel" >Visit Deductible : </td>
					<td class="forminfo" >
						<input type="text" name="ip_visit_deductible" id="ip_visit_deductible" maxlength="13" value="${bean.ip_visit_deductible}"
						 onkeypress="return enterNumOnly(event)" onchange="onVisitdeductible(this,'ip')" />
					</td>
				    <td class="formlabel" width="100px" >Visit Copay :</td>
					<td class="forminfo" >
						<input type="text" name="ip_copay_percent" id="ip_visit_copay" maxlength="13" value="${bean.ip_copay_percent}" 
							onkeypress="return enterNumOnly(event)" onchange="onVisitCopay(this,'ip')" />%
					</td>
					<td class="formlabel">Visit Max Copay : 
					</td>
				    <td class= "forminfo">
					<input type="text" name="ip_visit_copay_limit"  id="ip_visit_copay_limit" class="numeric" maxlength="13"
						 	onkeypress="return enterNumOnly(event)" onchange="onMaxCopay(this,'ip')" value="${bean.ip_visit_copay_limit}"/>
						 <img class="imgHelpText"
 								title="IP Co-pay Limit is the amount of copay to be collected from a patient for a IP visit."
						 src="${cpath}/images/help.png"/>
				</td>
				    
			</tr>
		 </table> 
		
			<table class="detailList"  id="chargesTable_i" name="chargesTable_i" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<th> <input type="checkbox" name="selAllChkbxi" id="selAllChkbxi" value="" onclick="selectAllForDiscounts('i');"/></th>
					<th>Plan Item-Category</th>
					<th align="right" style="text-align:right;">Sponsor Limit</i></th>
					<th align="right" style="text-align:right;">Deductible<i> (Cat)</i></th>
					<th align="right" style="text-align:right;">Deductible<i> (Item)</i></th>
					<th align="right" style="text-align:right;">Copay %</th>
					<th align="right" style="text-align:right;">Max Copay</th>
				    <th >&nbsp;&nbsp;</th>
				    <th >&nbsp;&nbsp;</th>  
				</tr>
				<tr style="display: none">
			</table>
			<table class="addButton">
					<tr style="text-align:right;">
						<td width="1000">Add New Insurance Item Categories</td>
						<td>
							<button type="button" name="addCategories_i" Class="imgButton" id="addCategories_i" onclick="addInruranceItemCategories(this, 'i')" >
								<img name="addButton" src="${cpath}/icons/Add.png" />
							</button>
						</td>
					</tr>
			</table>
			<table id="selectAllIpCharges" style="padding-top:0;border-top:0 solid #E0E0E0" class="detailList" align="right"  cellpadding="0" cellspacing="0" width="100%" >
				<tr align="right">
					<td style="padding-top:0;border-top:0 solid #E0E0E0" colspan="20">&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td style="padding-top:0;border-top:0 solid #E0E0E0; text-align:right;">Update All Selected In-Patient Charges &nbsp;&nbsp;<img src="${cpath}/icons/Edit.png" id="sel_img_i" onclick="return showDialogExt('i');" style="padding-right:0;"/></td>
				</tr>
			</table>
						
		 </fieldset>   
	
		<div id="myDialog" style="display:block;visibility:hidden;">
			<div class="bd" id="bd1">
				<table class="formTable" align="center">
					<tr>
						<td>
							<fieldset class="fieldSetBorder" style="width:460px;"><legend class="fieldSetLabel">Edit Item-Category Charge Details</legend>
							<br/>
							<table class="formTable" align="center">
								<tr>
									<td class="formlabel">Plan Item-Category:</td>
									<td class="forminfo">&nbsp;
									<label id="dlg_itemCatName">--</label>
									<input type="hidden" id="editRowId" value=""/>
									<input type="hidden" name="dlg_itemCatId" id="dlg_itemCatId"/>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Insurance Payable:</td>
									<td class="forminfo">
										<select class= "dropdown" id="_category_payable_fld" name="_category_payable_fld" onchange="enableAndDisabledPlanItemCategory();">
					 						<option value='Y'>Yes</option>
					 						<option value='N'>No</option>
					 					</select>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Pre Auth Required:</td>
							 	 	<td class="forminfo">
							 	 		<select name="category_prior_auth_required" class="dropdown" id="_category_prior_auth_required_fld">
							 	 			<option value="">Not Defined</option>
							 	 			<option value="N">Never</option>
											<option value="S">Sometimes</option>
											<option value="A">Always</option>
										</select>
							 	 	</td>
								</tr>
								<tr>
									<td class="formlabel">Sponsor Limit:</td>
									<td class="forminfo"><input type="text" id="_dlg_trmnt_fld"  class="numeric" maxlength="13"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
								</tr>
								<tr>
									<td class="formlabel">Deductible (Cat):</td>
									<td class="forminfo"><input type="text"  id="_dlg_cat_amt_fld" class="numeric" maxlength="13" onkeypress="return enterNumAndDotAndMinus(event);" /></td>
								</tr>
								<tr>
									<td class="formlabel">Deductible (Item):</td>
									<td class="forminfo"><input type="text"  id="_dlg_amt_fld" class="numeric" maxlength="13" onkeypress="return enterNumAndDotAndMinus(event);" /></td>
								</tr>
								<tr>
									<td class="formlabel">Copay %:</td>
									<td class="forminfo"><input type="text" id="_dlg_perc_fld"  class="numeric" maxlength="6"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
								</tr>
								<tr>
									<td class="formlabel">Max Copay:</td>
									<td class="forminfo"><input type="text" id="_dlg_cap_fld"  class="numeric" maxlength="13"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
								</tr>
								<!-- <tr>
									<td class="formlabel">Treatment Limit:</td>
									<td class="forminfo"><input type="text" id="_dlg_trmnt_fld"  class="numeric" maxlength="8"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
								</tr> -->
							</table>
								<input type="hidden" id="_dlg_patient_type" name="_dlg_patient_type" value="" />
							</fieldset>
						</td>
					</tr>
					<tr>
						<td align="left">
							<input type="button" id="_ok_button" value="OK" onclick="handleOk();"/>
							<input type="button" id="_cancel_button" value="Cancel" onclick="handleCancel();"/>
							<input type="button" onclick="showNextOrPrevCharge('prev')" name="prevBtn" value="<<Prev"/>
							<input type="button" onclick="showNextOrPrevCharge('next')" name="nextBtn" value="Next>>"/>
						</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
	<div id="myDialog1" style="display:block;visibility:hidden;">
			<div class="bd" id="bd2">
				<table class="formTable" align="center">
					<tr>
						<td>
							<fieldset class="fieldSetBorder" style="width:460px;"><legend class="fieldSetLabel">Edit Item-Category Charge Details</legend>
									<br/>
									<table class="formTable" align="center">
										<tr>
											<td class="formlabel">Insurance Payable:</td>
											<td class="forminfo">
												<select class= "dropdown" id="_category_payable_fld1" name="_category_payable_fld1" onchange="enableAndDisabledPlanItemCategory1();">
					 								<option	 value='Y'>Yes</option>
					 								<option value='N'>No</option>
					 							</select>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Pre Auth Required:</td>
										 	 	<td class="forminfo">
										 	 		<select name="category_prior_auth_required" class="dropdown" id="_category_prior_auth_required_fld1">
										 	 			<option value="">Not Defined</option>
										 	 			<option value="N">Never</option>
														<option value="S">Sometimes</option>
														<option value="A">Always</option>
													</select>
										 	 	</td>
										</tr>
										<tr>
											<td class="formlabel">Sponsor Limit:</td>
											<td class="forminfo"><input type="text" id="_dlg_trmnt_fld1"  class="numeric" maxlength="13"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
										</tr>
										<tr>
											<td class="formlabel">Deductible (Cat):</td>
											<td class="forminfo"><input type="text"  id="_dlg_cat_amt_fld1" class="numeric" maxlength="13" onkeypress="return enterNumAndDotAndMinus(event);" /></td>
										</tr>
										<tr>
											<td class="formlabel">Deductible (Item):</td>
											<td class="forminfo"><input type="text"  id="_dlg_amt_fld1" class="numeric" maxlength="13" onkeypress="return enterNumAndDotAndMinus(event);" /></td>
										</tr>
										<tr>
											<td class="formlabel">Copay %:</td>
											<td class="forminfo"><input type="text" id="_dlg_perc_fld1"  class="numeric" maxlength="6"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
										</tr>
										<tr>
											<td class="formlabel">Max Copay:</td>
											<td class="forminfo"><input type="text" id="_dlg_cap_fld1"  class="numeric" maxlength="13"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
										</tr>
										<!-- <tr>
											<td class="formlabel">Treatment Limit:</td>
											<td class="forminfo"><input type="text" id="_dlg_trmnt_fld1"  class="numeric" maxlength="8"  onkeypress="return enterNumAndDotAndMinus(event);"/></td>
										</tr> -->
									</table>
							 </fieldset>
						</td>
					</tr>
					<tr>
						<td align="left">
							<input type="button" id="_ok_button1" value="OK" onclick="handleOk1();"/>
							<input type="button" id="_cancel_button1" value="Cancel" onclick="handleCancel1();"/>
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<div id="addCategoryDialog" style="display:block;visibility:hidden;">
			<div class="bd" id="bd3">
				<table class="formTable" align="center">
					<tr>
						<td>
							<fieldset class="fieldSetBorder" style="width:460px;"><legend class="fieldSetLabel">Add Insurance Item Category</legend>
									<br/>
									<table class="formTable" align="center">
										<tr>
											<td class="formlabel">Select Insurance Item Category:</td>
										 	 	<td class="forminfo">
										 	 		<select name="insurance_item_category" class="dropdown" id="insurance_item_category">
										 	 			<option value="" >Select</option>
										 	 			<c:forEach items="${insuPayableList}" var="insuCategory">
										 	 				<c:if test="${insuCategory.get('system_category') =='N'}">
																<option value="${insuCategory.get('insurance_category_id')}">${insuCategory.get('insurance_category_name')}</option>
															</c:if>
														</c:forEach>
													</select>
										 	 	</td>
										</tr>
									</table>
							 </fieldset>
						</td>
					</tr>
					<tr>
						<td align="left">
							<input type="button" id="_ok_button2" value="Add" onclick="addToTable();"/>
							<input type="button" id="_cancel_button2" value="Close" onclick="handleDialogCancel();"/>
						</td>
					</tr>
				</table>
			</div>
		</div>

		<div class="screenActions">
			<button type="button" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
			
			|<a href="${cpath}/${pagePath}/list.htm?">Plan List</a>


			<insta:screenlink screenId="insurance_plan_main_audit_log" label="Plan Audit Log" addPipe="true"
                              extraParam="?_method=getAuditLogDetails&plan_id=${param.plan_id}&al_table=insurance_plan_main_audit_log"/>

           <insta:screenlink screenId="insurance_plan_details_audit_log" label="Plan Item Category Details Audit Log" addPipe="true"
                              extraParam="?_method=getAuditLogDetails&plan_id=${param.plan_id}&al_table=insurance_plan_details_audit_log"/>


		</div>
	</form>
</body>
</html>

