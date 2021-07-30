<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.DISCOUNT_PLAN_PATH %>"/>
<c:set var="genPrefs" value="${genPrefs[0]}" scope="request"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value="${ genPrefs.max_centers_inc_default}" scope="request"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Discount Plan Master- Insta HMS</title>
	
	<insta:link type="script" file="masters/discountPlan.js" />
	<%-- <insta:link type="js" file="orderdialog.js" />  --%>

	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  
	<c:set var="orderItemsUrl" value="${cpath}/master/orderItems.do?method=getOrderableItems"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&filter=Equipment,Laboratory,Radiology,Service,Operation,Package,MultiVisitPackage&orderable=Y"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&directBilling=&operationOrderApplicable=${genPrefs.operation_apllicable_for}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&orgId=${param.org_id}&visitType=${param.visit_type}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&center_id=${centerId}&tpa_id=${param.primary_sponsor_id}&ignoreCenter=true"/>
	
	<script src="${orderItemsUrl}&orgId=${ifn:cleanURL(param.org_id)}&packageApplicable=&visitType=&isMultiVisitPackage=true"></script> 
	
	  
<script>
 var insuranceCategoryList =  ${ifn:convertListToJson(insuranceCategoryList)};// "${insuranceCategoryList}";
 var discountPlanDetailsbean  = ${ifn:convertListToJson(list_discount_plan_details)}; //${discountPlanDetailsbean};
 var chargeHeadList = ${ifn:convertListToJson(chargeHeadList)}; //"${chargeHeadList}";
 var discountPlanbean = ${ifn:convertListToJson(discountPlanbean)}; //"${discountPlanbean}";
 var itemCategoryList = ${ifn:convertListToJson(itemCategoryList)};// "${itemCategoryList}";
 	  
 function init() {
 	  
		for (var i=0; i<discountPlanDetailsbean.length; i++) {
			discountPlanDetails = discountPlanDetailsbean[i];
		 	setRows();

			if(discountPlanDetails["applicable_type"].trim() == "I") {
				autoDiscountPlanMaster(i);
			}
		 	document.getElementById('applicable_to_id'+i).className= 'dropdown';
		 	setSelectedValues(i);
		}	
  }
	    
 
 var items = rateplanwiseitems;
function autoDiscountPlanMaster(autoid) {
	
		// getOrderableItems JSON result
		var ds = new YAHOO.util.LocalDataSource(items);
		ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
		ds.responseSchema = { resultsList : "result",
			fields: [ {key: "name"}, {key: "code"}, {key: "type"}, {key: "id"}, {key: "department"},
			          {key: "groupid"},{key: "subgrpid"},{key: "conduction_applicable"},
			          {key: "prior_auth_required"},{key:"insurance_category_id"},{key:"conducting_doc_mandatory"},
			          {key: "results_entry_applicable"},{key: "tooth_num_required"},{key: "multi_visit_package"}],  };   
		
		var rAutoComp = new YAHOO.widget.AutoComplete('applicable_to_name'+autoid,'itemcontainer'+autoid, ds);
		rAutoComp.minQueryLength = 0;
	 	rAutoComp.maxResultsDisplayed = 20;
	    rAutoComp.forceSelection = true;
	 	rAutoComp.animVert = false;
	 	rAutoComp.resultTypeList = false;
	 	rAutoComp.typeAhead = true;
	 	rAutoComp.allowBrowserAutocomplete = false;
	 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		rAutoComp.autoHighlight = false;
		rAutoComp.useShadow = false; 
		/*
	 	if (rAutoComp._elTextbox.value != '') {
			  rAutoComp._bItemSelected = true;
			  rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
		}    
		*/
 		var myHandler = function(type, args) {
	 		  var objData = args[2];
              document.getElementById("applicable_to_name"+autoid).value = objData.name;
              document.getElementById("applicable_to_id"+autoid).value = objData.id;
              document.getElementById("applicable_to_id_subgroup"+autoid).value = objData.type;   
		};
	 	
	 	rAutoComp.itemSelectEvent.subscribe(myHandler); 
  } 
 

 
		
</script>
</head>

<body onload="init();" >

	<form  name="DiscountPlanMasterForm" action="${cpath}${pagePath}/update.htm" method="post"> 

 			<input type="hidden" name="discount_plan_id" value="${ifn:cleanHtmlAttribute(param.discount_plan_id)}"/>
	
			<table width="100%">
					<tr>
						<td width="100%"><h1>Edit Discount Plan Details</h1></td>
						<td>&nbsp; &nbsp;</td>
					</tr>
			</table>		
		
		<insta:feedback-panel/>
			
		<fieldset class="fieldSetBorder" id="corporateInsu1">
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" > 
				<tr>
					<td class="formlabel" >Discount Plan Name: </td>
					<td class="forminfo" >
						<input type="text" name="discount_plan_name" id="discount_plan_name" maxlength="100" value="${bean.discount_plan_name}" />
						<input type="hidden" name="discount_plan_name1" id="discount_plan_name1" value="${bean.discount_plan_name}"/>
					</td>
					<td class="formlabel" width="100px" >Description: </td>
					<td class="forminfo" >
						<input type="text" name="discount_plan_description" id="discount_plan_description"  value="${bean.discount_plan_description}" />
					</td>
					<td class="formlabel">Status :</td>
					<td>
						<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I"
						optexts="Active,Inactive" />
					</td>
					<td></td>  
				</tr>
				<tr>
					<td class="formlabel">Valid From: </td>
					<fmt:parseDate value="${bean.validity_start}" pattern="yyyy-MM-dd" var="dt"/>
					<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="frm"/>
					<td><insta:datewidget name="validity_start" id="validity_start" value="${frm}"/></td>
					<td class="formlabel">Valid Upto: </td>
					<fmt:parseDate value="${bean.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
					<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
					<td><insta:datewidget name="validity_end" id="validity_end" value="${to}"/></td>
					<td></td>
					<td></td>
				</tr>
			 </table> 
		 </fieldset>
		
		<fieldset class="fieldSetBorder" id="corporateInsu1"><legend class="fieldSetLabel">Discount Rules</legend>
		<table class="dashboard" id="baseItemTbl" cellpadding="0" cellspacing="0">
			<tr class="header">
				 <th style="width: 180px">Applicable Category</th>
				<th style="width: 280px">Applicable To</th>  
				<th style="width: 100px">Discount</th>
				<th style="width: 170px">Disc. Type</th>
				<th style="width: 20px">Priority</th> 
				<th>&nbsp;</th> 
			</tr>
			<tr id="" style="display: none">
			</tr>
			<tr>
				<td colspan="5"></td>
				<td>
					<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRow(this)" >
						<img src="${cpath}/icons/Add.png" align="right"/>
					</button>
				</td>
			</tr>
		</table>
		</fieldSet>
		
		
		<div class="screenActions">
				  <button type="button" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>  
			<a href="${cpath}${pagePath}/add.htm">|&nbsp; Add &nbsp;|</a>
			<a href="${cpath}${pagePath}/list.htm?sortOrder=discount_plan_name">Discount Plan List</a>
			<c:if test="${max_centers > 1 && param._method != 'add'}">
				<insta:screenlink screenId="mas_discount_plan_center_ass" addPipe="true" label="Center Association"
					extraParam="?_method=getScreen&discount_plan_id=${param.discount_plan_id}"
					title="Center Association"/>
			</c:if>
		</div>			
	</form>
</body>

</html>
		