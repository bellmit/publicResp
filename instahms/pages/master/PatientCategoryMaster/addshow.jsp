<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add Patient Category- Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>' />

	<script>
		var ip_allowedRatePlans = '${bean.map.ip_allowed_rate_plans}';
		var op_allowedRatePlans = '${bean.map.op_allowed_rate_plans}';
		var ip_allowedTpas = '${bean.map.ip_allowed_sponsors}';
		var op_allowedTpas = '${bean.map.op_allowed_sponsors}';
		var ip_allowedCompanies = '${bean.map.ip_allowed_insurance_co_ids}';
		var op_allowedCompanies = '${bean.map.op_allowed_insurance_co_ids}';
		var ip_allTpas = ${tpaJSON};
		var op_allTpas = ${tpaJSON};
		var ip_allRatePlans = ${orgJSON};
		var op_allRatePlans = ${orgJSON};
		var ip_allInsCompanies = ${companyJSON};
		var op_allInsCompanies = ${companyJSON};
		var ip_defltRatePlan = '${bean.map.ip_rate_plan_id}';
		var op_defltRatePlan = '${bean.map.op_rate_plan_id}';

		var ip_defltPrimarySponsor = '${bean.map.primary_ip_sponsor_id}';
		var op_defltPrimarySponsor = '${bean.map.primary_op_sponsor_id}';
		var ip_defltPrimaryCompany = '${bean.map.primary_ip_insurance_co_id}';
		var op_defltPrimaryCompany = '${bean.map.primary_op_insurance_co_id}';

		var ip_defltSecondarySponsor = '${bean.map.secondary_ip_sponsor_id}';
		var op_defltSecondarySponsor = '${bean.map.secondary_op_sponsor_id}';
		var ip_defltSecondaryCompany = '${bean.map.secondary_ip_insurance_co_id}';
		var op_defltSecondaryCompany = '${bean.map.secondary_op_insurance_co_id}';

		function validateForm() {
			if (document.PatientCategoryMasterForm.category_name.value=="") {
				alert("Patient Category name is required");
				document.PatientCategoryMasterForm.category_name.focus();
				return false;
			}
			var patternID=document.PatientCategoryMasterForm.code.value;
			if (document.PatientCategoryMasterForm.seperate_num_seq.value=="Y" && (patternID!=undefined && patternID.trim()=='')) {
				alert("MR NO Pattern is required");
				document.PatientCategoryMasterForm.code.focus();
				return false;
			}
			var ip_txtBox4Rate = document.getElementById('ip_allRateplans');
			var op_txtBox4Rate = document.getElementById('op_allRateplans');
			var ip_txtBox4Spncrs = document.getElementById('ip_allSponsors');
			var op_txtBox4Spncrs = document.getElementById('op_allSponsors');
			var ip_txtBox4InsComps = document.getElementById('ip_allCompanies');
			var op_txtBox4InsComps = document.getElementById('op_allCompanies');

			var ip_isAtLeastOneRplnSelected = false;
			var ip_isAtLeastOneSpcrSelected = false;
			var op_isAtLeastOneRplnSelected = false;
			var op_isAtLeastOneSpcrSelected = false;
			var ip_isAtLeastOneCompSelected = false;
			var op_isAtLeastOneCompSelected = false;
			//ip
			if (!ip_txtBox4Rate.checked) {
				var selRate = document.getElementById('ip_alwd_rate_plan');
				for (var i=0; i<selRate.options.length; i++) {
					if (selRate.options[i].selected) {
						ip_isAtLeastOneRplnSelected = true;
					}
				}
			} else {
				ip_isAtLeastOneRplnSelected = true;
			}
			//op
			if (!op_txtBox4Rate.checked) {
				var selRate = document.getElementById('op_alwd_rate_plan');
				for (var i=0; i<selRate.options.length; i++) {
					if (selRate.options[i].selected) {
						op_isAtLeastOneRplnSelected = true;
					}
				}
			} else {
				op_isAtLeastOneRplnSelected = true;
			}

			//ip
			if (!ip_txtBox4Spncrs.checked) {
				var selSpncr = document.getElementById('ip_alwd_sponsor');
				for (var i=0; i<selSpncr.options.length; i++) {
					if (selSpncr.options[i].selected) {
						ip_isAtLeastOneSpcrSelected = true;
					}
				}
			} else {
				ip_isAtLeastOneSpcrSelected = true;
			}
			//op
			if (!op_txtBox4Spncrs.checked) {
				var selSpncr = document.getElementById('op_alwd_sponsor');
				for (var i=0; i<selSpncr.options.length; i++) {
					if (selSpncr.options[i].selected) {
						op_isAtLeastOneSpcrSelected = true;
					}
				}
			} else {
				op_isAtLeastOneSpcrSelected = true;
			}

			var opInsuranceSpnsrExists = isOpInsuranceSpnsrSelected();
			if (!opInsuranceSpnsrExists) {
				op_txtBox4InsComps.checked = true;
				selectAllAndSetInHidden('op_alwd_company','op_allowed_insurance_co_ids','op_deflt_primary_company','op_deflt_secondary_company','op_allCompanies');
			}

			var ipInsuranceSpnsrExists = isIpInsuranceSpnsrSelected();
			if (!ipInsuranceSpnsrExists) {
				ip_txtBox4InsComps.checked = true;
				selectAllAndSetInHidden('ip_alwd_company','ip_allowed_insurance_co_ids','ip_deflt_primary_company','ip_deflt_secondary_company','ip_allCompanies');
			}

			//ip
			if (!ip_txtBox4InsComps.checked) {
				var selComp = document.getElementById('ip_alwd_company');
				for (var i=0; i<selComp.options.length; i++) {
					if (selComp.options[i].selected) {
						ip_isAtLeastOneCompSelected = true;
					}
				}
			} else {
				ip_isAtLeastOneCompSelected = true;
			}
			//op
			if (!op_txtBox4InsComps.checked) {
				var selComp = document.getElementById('op_alwd_company');
				for (var i=0; i<selComp.options.length; i++) {
					if (selComp.options[i].selected) {
						op_isAtLeastOneCompSelected = true;
					}
				}
			} else {
				op_isAtLeastOneCompSelected = true;
			}

			if (ip_isAtLeastOneRplnSelected && op_isAtLeastOneRplnSelected
				&& ip_isAtLeastOneSpcrSelected && op_isAtLeastOneSpcrSelected
				&& ip_isAtLeastOneCompSelected && op_isAtLeastOneCompSelected) {
				return true;
			} else {
				alert('Please Select All or atleast one Rate Plan/Sponsor/Insu. Company');
				return false;
			}

			setDefaults();
		}

		function isOpInsuranceSpnsrSelected() {
			var selSpncr = document.getElementById('op_alwd_sponsor');
			for (var i=0; i<selSpncr.options.length; i++) {
				if (selSpncr.options[i].selected) {
					var tpa = findInList(op_allTpas, "tpa_id", selSpncr.options[i].value);
					if (!empty(tpa) && tpa.sponsor_type == 'I')
						return true;
				}
			}
			return false;
		}

		function isIpInsuranceSpnsrSelected() {
			var selSpncr = document.getElementById('ip_alwd_sponsor');
			for (var i=0; i<selSpncr.options.length; i++) {
				if (selSpncr.options[i].selected) {
					var tpa = findInList(ip_allTpas, "tpa_id", selSpncr.options[i].value);
					if (!empty(tpa) && tpa.sponsor_type == 'I')
						return true;
				}
			}
			return false;
		}


		function setDefaults() {
			var ipRatePlanHidden = document.getElementById('ip_rate_plan_id');
			var opRatePlanHidden = document.getElementById('op_rate_plan_id');

			var ipPrimarySponsorHidden = document.getElementById('primary_ip_sponsor_id');
			var opPrimarySponsorHidden = document.getElementById('primary_op_sponsor_id');

			var ipSecondarySponsorHidden = document.getElementById('secondary_ip_sponsor_id');
			var opSecondarySponsorHidden = document.getElementById('secondary_op_sponsor_id');

			var ipPrimaryInsCompHidden = document.getElementById('primary_ip_insurance_co_id');
			var opPrimaryInsCompHidden = document.getElementById('primary_op_insurance_co_id');

			var ipSecondaryInsCompHidden = document.getElementById('secondary_ip_insurance_co_id');
			var opSecondaryInsCompHidden = document.getElementById('secondary_op_insurance_co_id');

			var ipPrimarySponsorSel =  document.getElementById('ip_deflt_primary_sponsor');
			var opPrimarySponsorSel =  document.getElementById('op_deflt_primary_sponsor');

			var ipSecondarySponsorSel =  document.getElementById('ip_deflt_secondary_sponsor');
			var opSecondarySponsorSel =  document.getElementById('op_deflt_secondary_sponsor');

			var ipRatePlanSel =  document.getElementById('ip_deflt_rate_plan');
			var opRatePlanSel =  document.getElementById('op_deflt_rate_plan');

			var ipPrimaryInsCompSel =  document.getElementById('ip_deflt_primary_company');
			var opPrimaryInsCompSel =  document.getElementById('op_deflt_primary_company');

			var ipSecondaryInsCompSel =  document.getElementById('ip_deflt_secondary_company');
			var opSecondaryInsCompSel =  document.getElementById('op_deflt_secondary_company');

			ipRatePlanHidden.value = ipRatePlanSel.value;
			opRatePlanHidden.value = opRatePlanSel.value;

			ipPrimarySponsorHidden.value = ipPrimarySponsorSel.value;
			opPrimarySponsorHidden.value = opPrimarySponsorSel.value;

			ipPrimaryInsCompHidden.value = ipPrimaryInsCompSel.value;
			opPrimaryInsCompHidden.value = opPrimaryInsCompSel.value;

			ipSecondarySponsorHidden.value = ipSecondarySponsorSel.value;
			opSecondarySponsorHidden.value = opSecondarySponsorSel.value;

			ipSecondaryInsCompHidden.value = ipSecondaryInsCompSel.value;
			opSecondaryInsCompHidden.value = opSecondaryInsCompSel.value;

		}

		function setSelectedInHidden(selName, hidName, defltNamePrimary, defltNameSecondary) {
			var itemList = [];
			var sel = document.getElementById(selName);
			var txtBx = document.getElementById(hidName);
			var tempSelected = new Array();
			for (var i = 0; i < sel.options.length; i++){
				if (sel.options[ i ].selected && sel.options[ i ].value!=''){
					tempSelected.push(sel.options[ i ].value);
					itemList.push({"id":sel.options[ i ].value,"name":sel.options[ i ].text});
				}
			}
			if(itemList == []){ itemList.push({});}
			if(defltNamePrimary!= null)
				loadSelectBox(document.getElementById(defltNamePrimary), itemList, "name", "id", "--Select--", "");
			if(defltNameSecondary!= null)
				loadSelectBox(document.getElementById(defltNameSecondary), itemList, "name", "id", "--Select--", "");
			txtBx.value = tempSelected;
			if(!empty(defltNamePrimary))
				document.getElementById(defltNamePrimary).options[0].selected = true;
			if(!empty(defltNameSecondary))
				document.getElementById(defltNameSecondary).options[0].selected = true;
		}

		function selectAllAndSetInHidden(selName, hidName, defltNamePrimary, defltNameSecondary, checkName){
			var itemList = [];
			var checkbx = document.getElementById(checkName);
			var sel = document.getElementById(selName);
			var txtBx = document.getElementById(hidName);
			var selected = checkbx.checked;

			if (selected)
				txtBx.value = '*';
			else
				txtBx.value = '';
			for (var i = 0; i < sel.options.length; i++){
				sel.options[ i ].selected = selected;
			}
			for (var i = 0; i < sel.options.length; i++){
				if (sel.options[ i ].selected && sel.options[ i ].value!=''){
					itemList.push({"id":sel.options[ i ].value,"name":sel.options[ i ].text});
				}
			}
			if(itemList == []){ itemList.push({});}
			if(!empty(defltNamePrimary))
				loadSelectBox(document.getElementById(defltNamePrimary), itemList, "name", "id", "--Select--", "");
			if(!empty(defltNameSecondary))
				loadSelectBox(document.getElementById(defltNameSecondary), itemList, "name", "id", "--Select--", "");
				sel.disabled = selected;
		}


		function init() {
			var ratePlanArray = new Array();
			//ip
			if(ip_allowedRatePlans == '*') {
				document.getElementById('ip_allRateplans').checked = true;
				selectAllAndSetInHidden('ip_alwd_rate_plan','ip_allowed_rate_plans','ip_deflt_rate_plan','','ip_allRateplans');
				if(ip_defltRatePlan!=null && ip_defltRatePlan!='')
					setSelectedIndex(document.getElementById('ip_deflt_rate_plan'), ip_defltRatePlan);

			} else {
				ratePlanArray = ip_allowedRatePlans.split(',');
				for(var j=0; j<ratePlanArray.length; j++){
					for(var i=0; i<document.getElementById('ip_alwd_rate_plan').options.length; i++) {
						if(document.getElementById('ip_alwd_rate_plan').options[i].value == ratePlanArray[j])
							document.getElementById('ip_alwd_rate_plan').options[i].selected = true;
					}
				}
				setSelectedInHidden('ip_alwd_rate_plan', 'ip_allowed_rate_plans', 'ip_deflt_rate_plan');
				if(ip_defltRatePlan!=null && ip_defltRatePlan!='')
					setSelectedIndex(document.getElementById('ip_deflt_rate_plan'), ip_defltRatePlan);
			}
			//op
			if(op_allowedRatePlans == '*') {
				document.getElementById('op_allRateplans').checked = true;
				selectAllAndSetInHidden('op_alwd_rate_plan','op_allowed_rate_plans','op_deflt_rate_plan','','op_allRateplans');
				if(op_defltRatePlan!=null && op_defltRatePlan!='')
					setSelectedIndex(document.getElementById('op_deflt_rate_plan'), op_defltRatePlan);

			} else {
				ratePlanArray = op_allowedRatePlans.split(',');
				for(var j=0; j<ratePlanArray.length; j++){
					for(var i=0; i<document.getElementById('op_alwd_rate_plan').options.length; i++) {
						if(document.getElementById('op_alwd_rate_plan').options[i].value == ratePlanArray[j])
							document.getElementById('op_alwd_rate_plan').options[i].selected = true;
					}
				}
				setSelectedInHidden('op_alwd_rate_plan', 'op_allowed_rate_plans', 'op_deflt_rate_plan');
				if(op_defltRatePlan!=null && op_defltRatePlan!='')
					setSelectedIndex(document.getElementById('op_deflt_rate_plan'), op_defltRatePlan);
			}
			// ip
			var tpaArray = new Array();
			if (ip_allowedTpas == '*') {
				document.getElementById('ip_allSponsors').checked = true;
				selectAllAndSetInHidden('ip_alwd_sponsor','ip_allowed_sponsors','ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor','ip_allSponsors');
				if(ip_defltPrimarySponsor!=null && ip_defltPrimarySponsor!='')
					setSelectedIndex(document.getElementById('ip_deflt_primary_sponsor'), ip_defltPrimarySponsor);
				if(ip_defltSecondarySponsor!=null && ip_defltSecondarySponsor!='')
					setSelectedIndex(document.getElementById('ip_deflt_secondary_sponsor'), ip_defltSecondarySponsor);
			} else {
				tpaArray = ip_allowedTpas.split(',');
				for(var j=0; j<tpaArray.length; j++){
					for(var i=0; i<document.getElementById('ip_alwd_sponsor').options.length; i++) {
						if(document.getElementById('ip_alwd_sponsor').options[i].value == tpaArray[j])
							document.getElementById('ip_alwd_sponsor').options[i].selected = true;
					}
				}
				setSelectedInHidden('ip_alwd_sponsor', 'ip_allowed_sponsors', 'ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor');
				if(ip_defltPrimarySponsor!=null && ip_defltPrimarySponsor!='')
					setSelectedIndex(document.getElementById('ip_deflt_primary_sponsor'), ip_defltPrimarySponsor);
				if(ip_defltSecondarySponsor!=null && ip_defltSecondarySponsor!='')
					setSelectedIndex(document.getElementById('ip_deflt_secondary_sponsor'), ip_defltSecondarySponsor);
			}

			// op
			var tpaArray = new Array();
			if (op_allowedTpas == '*') {
				document.getElementById('op_allSponsors').checked = true;
				selectAllAndSetInHidden('op_alwd_sponsor','op_allowed_sponsors','op_deflt_primary_sponsor','op_deflt_secondary_sponsor','op_allSponsors');
				if(op_defltPrimarySponsor!=null && op_defltPrimarySponsor!='')
					setSelectedIndex(document.getElementById('op_deflt_primary_sponsor'), op_defltPrimarySponsor);
				if(op_defltSecondarySponsor!=null && op_defltSecondarySponsor!='')
					setSelectedIndex(document.getElementById('op_deflt_secondary_sponsor'), op_defltSecondarySponsor);
			} else {
				tpaArray = op_allowedTpas.split(',');
				for(var j=0; j<tpaArray.length; j++){
					for(var i=0; i<document.getElementById('op_alwd_sponsor').options.length; i++) {
						if(document.getElementById('op_alwd_sponsor').options[i].value == tpaArray[j])
							document.getElementById('op_alwd_sponsor').options[i].selected = true;
					}
				}
				setSelectedInHidden('op_alwd_sponsor', 'op_allowed_sponsors', 'op_deflt_primary_sponsor','op_deflt_secondary_sponsor');
				if(op_defltPrimarySponsor!=null && op_defltPrimarySponsor!='')
					setSelectedIndex(document.getElementById('op_deflt_primary_sponsor'), op_defltPrimarySponsor);
				if(op_defltSecondarySponsor!=null && op_defltSecondarySponsor!='')
					setSelectedIndex(document.getElementById('op_deflt_secondary_sponsor'), op_defltSecondarySponsor);
			}

			// ip
			var compArray = new Array();
			if (ip_allowedCompanies == '*') {
				document.getElementById('ip_allCompanies').checked = true;
				selectAllAndSetInHidden('ip_alwd_company','ip_allowed_insurance_co_ids','ip_deflt_primary_company','ip_deflt_secondary_company','ip_allCompanies');
				if(ip_defltPrimaryCompany!=null && ip_defltPrimaryCompany!='')
					setSelectedIndex(document.getElementById('ip_deflt_primary_company'), ip_defltPrimaryCompany);
				if(ip_defltSecondaryCompany!=null && ip_defltSecondaryCompany!='')
					setSelectedIndex(document.getElementById('ip_deflt_secondary_company'), ip_defltSecondaryCompany);
			} else {
				compArray = ip_allowedCompanies.split(',');
				for(var j=0; j<compArray.length; j++){
					for(var i=0; i<document.getElementById('ip_alwd_company').options.length; i++) {
						if(document.getElementById('ip_alwd_company').options[i].value == compArray[j])
							document.getElementById('ip_alwd_company').options[i].selected = true;
					}
				}
				setSelectedInHidden('ip_alwd_company', 'ip_allowed_insurance_co_ids', 'ip_deflt_primary_company','ip_deflt_secondary_company');
				if(ip_defltPrimaryCompany!=null && ip_defltPrimaryCompany!='')
					setSelectedIndex(document.getElementById('ip_deflt_primary_company'), ip_defltPrimaryCompany);
				if(ip_defltSecondaryCompany!=null && ip_defltSecondaryCompany!='')
					setSelectedIndex(document.getElementById('ip_deflt_secondary_company'), ip_defltSecondaryCompany);
			}

			// op
			var compArray = new Array();
			if (op_allowedCompanies == '*') {
				document.getElementById('op_allCompanies').checked = true;
				selectAllAndSetInHidden('op_alwd_company','op_allowed_insurance_co_ids','op_deflt_primary_company','op_deflt_secondary_company','op_allCompanies');
				if(op_defltPrimaryCompany!=null && op_defltPrimaryCompany!='')
					setSelectedIndex(document.getElementById('op_deflt_primary_company'), op_defltPrimaryCompany);
				if(op_defltSecondaryCompany!=null && op_defltSecondaryCompany!='')
					setSelectedIndex(document.getElementById('op_deflt_secondary_company'), op_defltSecondaryCompany);
			} else {
				compArray = op_allowedCompanies.split(',');
				for(var j=0; j<compArray.length; j++){
					for(var i=0; i<document.getElementById('op_alwd_company').options.length; i++) {
						if(document.getElementById('op_alwd_company').options[i].value == compArray[j])
							document.getElementById('op_alwd_company').options[i].selected = true;
					}
				}
				setSelectedInHidden('op_alwd_company', 'op_allowed_insurance_co_ids', 'op_deflt_primary_company','op_deflt_secondary_company');
				if(op_defltPrimaryCompany!=null && op_defltPrimaryCompany!='')
					setSelectedIndex(document.getElementById('op_deflt_primary_company'), op_defltPrimaryCompany);
				if(op_defltSecondaryCompany!=null && op_defltSecondaryCompany!='')
					setSelectedIndex(document.getElementById('op_deflt_secondary_company'), op_defltSecondaryCompany);
			}

			validateSecondary('op_deflt_primary_company', 'op_deflt_secondary_company');
			validateSecondary('ip_deflt_primary_company', 'ip_deflt_secondary_company');
			validateSecondary('op_deflt_primary_sponsor', 'op_deflt_secondary_sponsor');
			validateSecondary('ip_deflt_primary_sponsor', 'ip_deflt_secondary_sponsor');
		}

		function validatePrimary(priSelName, secSelName) {
			var prisel = document.getElementById(priSelName);
			var secsel = document.getElementById(secSelName);
			if (secsel.value != '' && prisel.value == '') {
				var label = '';
				if (priSelName.startsWith('op_deflt_primary_company')) label = 'OP Default Primary Insurance Company';
				else if (priSelName.startsWith('ip_deflt_primary_company')) label = 'IP Default Primary Insurance Company';
				else if (priSelName.startsWith('op_deflt_primary_sponsor')) label = 'OP Default Primary Sponsor';
				else if (priSelName.startsWith('ip_deflt_primary_sponsor')) label = 'IP Default Primary Sponsor';
				alert("Please Select "+label);
				secsel.value = '';
				prisel.focus();
				return false;
			}
		}

		function validateSecondary(priSelName, secSelName) {
			var prisel = document.getElementById(priSelName);
			var secsel = document.getElementById(secSelName);
			if (secsel.value != '' && prisel.value == '') {
				secsel.value = '';
			}
		}

		String.prototype.startsWith = function (str){
		   	return this.slice(0, str.length) == str;
		};

		<c:if test="${param._method != 'add'}">
     		 Insta.masterData=${categoryLists};
		</c:if>
	</script>
</head>

<body onload="${ifn:cleanJavaScript(param._method !='add' ? 'init()' : '' )}">
<c:choose>
	<c:when test="${param._method !='add'}">
		<h1 style="float:left">Edit Patient Category Details</h1>
		<c:url var ="searchUrl" value="/master/PatientCategoryMaster.do"/>
		<insta:findbykey keys="category_name,category_id" fieldName="category_id" method="show" url="${searchUrl}"/>
	</c:when>
	<c:otherwise>
		<h1>Add Patient Category Details</h1>
	</c:otherwise>
</c:choose>


<insta:feedback-panel/>

<form onsubmit="return validateForm();" name="PatientCategoryMasterForm" action="PatientCategoryMaster.do" method="POST" >

<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
<input type="hidden" name="category_id" value="${bean.map.category_id}" />
<input type="hidden" name="ip_allowed_rate_plans" id="ip_allowed_rate_plans" value="${bean.map.ip_allowed_rate_plans}" />
<input type="hidden" name="op_allowed_rate_plans" id="op_allowed_rate_plans" value="${bean.map.op_allowed_rate_plans}" />

<input type="hidden" name="ip_allowed_sponsors"  id="ip_allowed_sponsors" value="${bean.map.ip_allowed_sponsors}"/>
<input type="hidden" name="op_allowed_sponsors"  id="op_allowed_sponsors" value="${bean.map.op_allowed_sponsors}"/>

<input type="hidden" name="ip_allowed_insurance_co_ids"  id="ip_allowed_insurance_co_ids" value="${bean.map.ip_allowed_insurance_co_ids}"/>
<input type="hidden" name="op_allowed_insurance_co_ids"  id="op_allowed_insurance_co_ids" value="${bean.map.op_allowed_insurance_co_ids}"/>

<input type="hidden" name="ip_rate_plan_id" id="ip_rate_plan_id" value="${bean.map.ip_rate_plan_id}" />
<input type="hidden" name="op_rate_plan_id" id="op_rate_plan_id" value="${bean.map.op_rate_plan_id}" />

<input type="hidden" name="primary_ip_sponsor_id"  id="primary_ip_sponsor_id" value="${bean.map.primary_ip_sponsor_id}"/>
<input type="hidden" name="primary_op_sponsor_id"  id="primary_op_sponsor_id" value="${bean.map.primary_op_sponsor_id}"/>

<input type="hidden" name="primary_ip_insurance_co_id"  id="primary_ip_insurance_co_id" value="${bean.map.primary_ip_insurance_co_id}"/>
<input type="hidden" name="primary_op_insurance_co_id"  id="primary_op_insurance_co_id" value="${bean.map.primary_op_insurance_co_id}"/>

<input type="hidden" name="secondary_ip_sponsor_id"  id="secondary_ip_sponsor_id" value="${bean.map.secondary_ip_sponsor_id}"/>
<input type="hidden" name="secondary_op_sponsor_id"  id="secondary_op_sponsor_id" value="${bean.map.secondary_op_sponsor_id}"/>

<input type="hidden" name="secondary_ip_insurance_co_id"  id="secondary_ip_insurance_co_id" value="${bean.map.secondary_ip_insurance_co_id}"/>
<input type="hidden" name="secondary_op_insurance_co_id"  id="secondary_op_insurance_co_id" value="${bean.map.secondary_op_insurance_co_id}"/>


<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Patient Category Details</legend>

	<table class="formtable">
		<c:if test="${max_centers_inc_default > 1}">
			<tr>
				<td class="formlabel">Available to Center</td>
				<td>
					<c:set var="catCenterId" value="${param._method == 'add' ? centerId : bean.map.center_id}"/>
					<select class="dropdown" name="center_id" id="center_id">
						<option value="0">All Centers</option>
						<c:forEach items="${centers}" var="center">
							<option value="${center.map.center_id}" ${catCenterId == center.map.center_id ? 'selected' : ''}>
								${center.map.center_name}
							</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</c:if>
		<tr>
			<td class="formlabel">Patient Category Name:</td>
			<td><input type="text" name="category_name" value="${bean.map.category_name}" maxlength="100"/></td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
					optexts="Active,Inactive" />
			</td>
			<td class="formlabel">Passport Details Required:</td>
			<td> <insta:selectoptions name="passport_details_required" value="${bean.map.passport_details_required}" opvalues="Y,N"
					optexts="Yes,No" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Case File Required:</td>
			<td> <insta:selectoptions name="case_file_required" value="${bean.map.case_file_required}" opvalues="Y,N"
					optexts="Yes,No" /> </td>
			<td class="formlabel">Registration Charge Applicable:</td>
			<td>
				<insta:selectoptions name="registration_charge_applicable" value="${bean.map.registration_charge_applicable}" opvalues="Y,N"
					optexts="Yes,No" />
			</td>
			<td></td>
		</tr>

		<tr>
			<td class="formlabel">Allowed  IP Rate Plans:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('ip_alwd_rate_plan','ip_allowed_rate_plans','ip_deflt_rate_plan','','ip_allRateplans');" id="ip_allRateplans">
				Select All
				<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Rate Plans, implies that any of the available rate plans can be selected as the Default Rate Plan, for Patient Category."
	 				src="${cpath}/images/help.png"/>
				<insta:selectdb name="ip_alwd_rate_plan" id="ip_alwd_rate_plan" multiple="true"  class="noClass" optionTitle="true"
					table="organization_details" valuecol="org_id" displaycol="org_name" orderby="org_name"
					style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('ip_alwd_rate_plan','ip_allowed_rate_plans','ip_deflt_rate_plan');" onBlur="setSelectedInHidden('ip_alwd_rate_plan','ip_allowed_rate_plans','ip_deflt_rate_plan');" />

			</td>
			<td class="formlabel">Allowed IP Sponsors:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('ip_alwd_sponsor','ip_allowed_sponsors','ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor','ip_allSponsors')" id="ip_allSponsors">
					Select All
					<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Sponsors, implies that any of the available TPA/Sponsor's can be selected as the Default Sponsor, for Patient Category."
						src="${cpath}/images/help.png"/>
				<insta:selectdb name="ip_alwd_sponsor" id="ip_alwd_sponsor" multiple="true"  class="noClass" optionTitle="true"
					table="tpa_master" valuecol="tpa_id" displaycol="tpa_name" orderby="tpa_name"
					 style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('ip_alwd_sponsor','ip_allowed_sponsors','ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor');" onBlur="setSelectedInHidden('ip_alwd_sponsor','ip_allowed_sponsors','ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor');"/>

			</td>
			<td class="formlabel">Allowed IP Insurance Companies:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('ip_alwd_company','ip_allowed_insurance_co_ids','ip_deflt_primary_company','ip_deflt_secondary_company','ip_allCompanies')" id="ip_allCompanies">
					Select All
					<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Insurance Companies, implies that any of the available Insurance Company can be selected as the Default Insurance Company, for Patient Category."
						src="${cpath}/images/help.png"/>
				<insta:selectdb name="ip_alwd_company" id="ip_alwd_company" multiple="true"  class="noClass" optionTitle="true"
					table="insurance_company_master" valuecol="insurance_co_id" displaycol="insurance_co_name" orderby="insurance_co_name"
					 style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('ip_alwd_company','ip_allowed_insurance_co_ids','ip_deflt_primary_company','ip_deflt_secondary_company');" onBlur="setSelectedInHidden('ip_alwd_company','ip_allowed_insurance_co_ids','ip_deflt_primary_company','ip_deflt_secondary_company');"/>

			</td>
		</tr>
		<tr>
			<td class="formlabel">IP Default Rate Plan:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="ip_deflt_rate_plan" id="ip_deflt_rate_plan" onclick="setSelectedInHidden('ip_deflt_rate_plan','ip_rate_plan_id');" onBlur="setSelectedInHidden('ip_deflt_rate_plan','ip_rate_plan_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">IP Default Primary Sponsor:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="ip_deflt_primary_sponsor" id="ip_deflt_primary_sponsor" onchange="return validateSecondary('ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor');"  onclick="setSelectedInHidden('ip_deflt_primary_sponsor','primary_ip_sponsor_id');" onBlur="setSelectedInHidden('ip_deflt_primary_sponsor','primary_ip_sponsor_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">IP Default Primary Insurance Company:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="ip_deflt_primary_company" id="ip_deflt_primary_company"  onchange="return validateSecondary('ip_deflt_primary_company','ip_deflt_secondary_company');"  onclick="setSelectedInHidden('ip_deflt_primary_company','primary_ip_insurance_co_id');" onBlur="setSelectedInHidden('ip_deflt_primary_company','primary_ip_insurance_co_id');">
					<option value="">--Select--</option>
				</select>
			</td>
		</tr>

		<tr>
			<td></td>
			<td></td>
			<td class="formlabel">IP Default Secondary Sponsor:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="ip_deflt_secondary_sponsor" id="ip_deflt_secondary_sponsor"  onchange="return validatePrimary('ip_deflt_primary_sponsor','ip_deflt_secondary_sponsor');"  onclick="setSelectedInHidden('ip_deflt_secondary_sponsor','secondary_ip_sponsor_id');" onBlur="setSelectedInHidden('ip_deflt_secondary_sponsor','secondary_ip_sponsor_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">IP Default Secondary Insurance Company:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="ip_deflt_secondary_company" id="ip_deflt_secondary_company"  onchange="return validatePrimary('ip_deflt_primary_company','ip_deflt_secondary_company');"  onclick="setSelectedInHidden('ip_deflt_secondary_company','secondary_ip_insurance_co_id');" onBlur="setSelectedInHidden('ip_deflt_secondary_company','secondary_ip_insurance_co_id');">
					<option value="">--Select--</option>
				</select>
			</td>
		</tr>


		<tr>
			<td class="formlabel">Allowed  OP Rate Plans:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('op_alwd_rate_plan','op_allowed_rate_plans','op_deflt_rate_plan','','op_allRateplans');" id="op_allRateplans">
				Select All
				<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Rate Plans, implies that any of the available rate plans can be selected as the Default Rate Plan, for Patient Category."
	 				src="${cpath}/images/help.png"/>
				<insta:selectdb name="op_alwd_rate_plan" id="op_alwd_rate_plan" multiple="true"  class="noClass" optionTitle="true"
					table="organization_details" valuecol="org_id" displaycol="org_name" orderby="org_name"
					style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('op_alwd_rate_plan','op_allowed_rate_plans','op_deflt_rate_plan');" onBlur="setSelectedInHidden('op_alwd_rate_plan','op_allowed_rate_plans','op_deflt_rate_plan');" />

			</td>
			<td class="formlabel">Allowed OP Sponsors:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('op_alwd_sponsor','op_allowed_sponsors','op_deflt_primary_sponsor','op_deflt_secondary_sponsor','op_allSponsors')" id="op_allSponsors">
					Select All
					<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Sponsors, implies that any of the available TPA/Sponsor's can be selected as the Default Sponsor, for Patient Category."
						src="${cpath}/images/help.png"/>
				<insta:selectdb name="op_alwd_sponsor" id="op_alwd_sponsor" multiple="true"  class="noClass" optionTitle="true"
					table="tpa_master" valuecol="tpa_id" displaycol="tpa_name" orderby="tpa_name"
					 style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('op_alwd_sponsor','op_allowed_sponsors','op_deflt_primary_sponsor','op_deflt_secondary_sponsor');" onBlur="setSelectedInHidden('op_alwd_sponsor','op_allowed_sponsors','op_deflt_primary_sponsor','op_deflt_secondary_sponsor');"/>

			</td>
			<td class="formlabel">Allowed OP Insurance Companies:</td>
			<td>
				<input type="checkbox" onclick="selectAllAndSetInHidden('op_alwd_company','op_allowed_insurance_co_ids','op_deflt_primary_company','op_deflt_secondary_company','op_allCompanies')" id="op_allCompanies">
					Select All
					<img class="imgHelpText" title="Please Note: Selecting None in the Allowed Insurance Companies, implies that any of the available Insurance Company can be selected as the Default Insurance Company, for Patient Category."
						src="${cpath}/images/help.png"/>
				<insta:selectdb name="op_alwd_company" id="op_alwd_company" multiple="true"  class="noClass" optionTitle="true"
					table="insurance_company_master" valuecol="insurance_co_id" displaycol="insurance_co_name" orderby="insurance_co_name"
					 style="width:12em;height:10em;padding-left:3px;color:#666666;" onclick="setSelectedInHidden('op_alwd_company','op_allowed_insurance_co_ids','op_deflt_primary_company','op_deflt_secondary_company');" onBlur="setSelectedInHidden('op_alwd_company','op_allowed_insurance_co_ids','op_deflt_primary_company','op_deflt_secondary_company');"/>

			</td>
		</tr>

		<tr>
			<td class="formlabel">OP Default Rate Plan:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="op_deflt_rate_plan" id="op_deflt_rate_plan" onclick="setSelectedInHidden('op_deflt_rate_plan','op_rate_plan_id');" onBlur="setSelectedInHidden('op_deflt_rate_plan','op_rate_plan_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">OP Default Primary Sponsor:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="op_deflt_primary_sponsor" id="op_deflt_primary_sponsor" onchange="return validateSecondary('op_deflt_primary_sponsor','op_deflt_secondary_sponsor');"  onclick="setSelectedInHidden('op_deflt_primary_sponsor','primary_op_sponsor_id');" onBlur="setSelectedInHidden('op_deflt_primary_sponsor','primary_op_sponsor_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">OP Default Primary Insurance Company:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="op_deflt_primary_company" id="op_deflt_primary_company" onchange="return validateSecondary('op_deflt_primary_company','op_deflt_secondary_company');"  onclick="setSelectedInHidden('op_deflt_secondary_company','secondary_op_insurance_co_id');" onclick="setSelectedInHidden('op_deflt_primary_company','primary_op_insurance_co_id');" onBlur="setSelectedInHidden('op_deflt_primary_company','primary_op_insurance_co_id');">
					<option value="">--Select--</option>
				</select>
			</td>
		</tr>

		<tr>
			<td></td>
			<td></td>
			<td class="formlabel">OP Default Secondary Sponsor:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="op_deflt_secondary_sponsor" id="op_deflt_secondary_sponsor" onchange="return validatePrimary('op_deflt_primary_sponsor','op_deflt_secondary_sponsor');"   onclick="setSelectedInHidden('op_deflt_secondary_sponsor','secondary_op_sponsor_id');" onBlur="setSelectedInHidden('op_deflt_secondary_sponsor','secondary_op_sponsor_id');">
					<option value="">--Select--</option>
				</select>
			</td>
			<td class="formlabel">OP Default Secondary Insurance Company:</td>
			<td>
				<select class="dropDown" style="width:12em;" name="op_deflt_secondary_company" id="op_deflt_secondary_company"  onchange="return validatePrimary('op_deflt_primary_company','op_deflt_secondary_company');"  onclick="setSelectedInHidden('op_deflt_secondary_company','secondary_op_insurance_co_id');" onBlur="setSelectedInHidden('op_deflt_secondary_company','secondary_op_insurance_co_id');">
					<option value="">--Select--</option>
				</select>
			</td>
		</tr>
		
		<tr>
			<td class="formlabel">Separate Number Sequence:</td>
			<td> <insta:selectoptions name="seperate_num_seq" value="${bean.map.seperate_num_seq}" opvalues="N,Y"
					optexts="No,Yes" /> </td>
					
			<td class="formlabel">MRNO Pattern:</td>
			<td>
			<insta:selectdb  name="code" id="code"  value="${bean.map.code}" table="hosp_id_patterns" valuecol="pattern_id" 
			displaycol="pattern_id" dummyvalue="-- Select --" filtercol="transaction_type" filtervalue="MRN"/></td>
		</tr>

	</table>

</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" "><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/PatientCategoryMaster.do?_method=show&category_id=${bean.map.category_id}">Reset</a>
			|
			<a href="${cpath}/master/PatientCategoryMaster.do?_method=add">Add</a>
			|
			</c:if>
			<a href="${cpath}/master/PatientCategoryMaster.do?_method=list" >Patient Category List</a>
		</div>
	</form>
</body>
</html>

