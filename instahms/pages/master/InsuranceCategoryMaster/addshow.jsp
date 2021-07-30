<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance Plan Type - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
		function doCancel() {
			window.location.href="${cpath}/master/InsuranceCatMaster.do?_method=list";
		}

		var insuNameList = ${insuranceCompaniesLists};
		var catNameList = ${categoryLists};

		function init() {
			if(document.getElementById("_insu_comp_tbox"))
				initInsuAutoCmplt();
		}

		var itAutoComplete = null;
		function initInsuAutoCmplt() {
			if (itAutoComplete != undefined) {
				itAutoComplete.destroy();
			}

			YAHOO.example.itemArray = [];
			var i=0;
			for(var j=0; j<insuNameList.length; j++) {
				YAHOO.example.itemArray.length = i+1;
				YAHOO.example.itemArray[i] = insuNameList[j];
				i++;
			}

				YAHOO.example.ACJSArray = new function() {
					datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
					datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
					datasource.responseSchema = {
						resultsList : 'result',
						fields : [ 	{key : 'insurance_co_name'},
									{key : 'insurance_co_id'}
								]
					};

					itAutoComplete = new YAHOO.widget.AutoComplete('_insu_comp_tbox','_insu_comp_dropdown', datasource);
					itAutoComplete.prehightlightClassName = "yui-ac-prehighlight";
					itAutoComplete.typeAhead = true;
					itAutoComplete.useShadow = true;
					itAutoComplete.allowBrowserAutocomplete = false;
					itAutoComplete.minQueryLength = 0;
					itAutoComplete.maxResultsDisplayed = 20;
					itAutoComplete.autoHighlight = true;
					itAutoComplete.forceSelection = true;
					itAutoComplete.animVert = false;
					itAutoComplete.useIFrame = true;
					itAutoComplete.formatResult = Insta.autoHighlight;

					itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
						var ele = new Array();
					  	ele= elItem[2];
					  	document.insuranceCatMasterForm.insurance_co_name.value = ele[0];
						document.insuranceCatMasterForm.insurance_co_id.value = ele[1];
					});
			       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			       		var ele = new Array();
					  	ele= elItem[2];
			       		document.insuranceCatMasterForm.insurance_co_id.value = ele[1];
			       		alert(document.insuranceCatMasterForm.insurance_co_id.value);
					});
			}
		}
		function validateForm(){
			if(document.getElementById('_method').value != 'update'){
				for(var i=0; i<catNameList.length; i++){
						var temp = catNameList[i];
						if(temp.category_name == document.insuranceCatMasterForm.category_name.value
							&& temp.insurance_co_id == document.insuranceCatMasterForm.insurance_co_id.value){
							alert("This plan type already exists...Please try another");
							return false;
						}
				}
			}
			if(document.insuranceCatMasterForm.insurance_co_id.value==""||document.insuranceCatMasterForm.insurance_co_id.value==null)
			{
				alert("Insurance company name is required");
				document.insuranceCatMasterForm.insurance_co_name.focus();
				return false;
			}
			if(document.insuranceCatMasterForm.category_name.value==""||document.insuranceCatMasterForm.category_name.value==null){
				alert("Plan type is required");
				document.insuranceCatMasterForm.category_name.focus();
				return false;
			}
			return true;
		}

		<c:if test="${param._method != 'add'}">
	      Insta.masterData=${categoryLists};
        </c:if>

	</script>
</head>

<body>
<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit Insurance Plan Type Details</h1>
         <c:url var ="searchUrl" value="/master/InsuranceCatMaster.do"/>
         <insta:findbykey keys="category_display,category_id" fieldName="category_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Insurance Plan Type Details</h1>
    </c:otherwise>
</c:choose>


	<form onsubmit="return validateForm();" name="insuranceCatMasterForm" action="InsuranceCatMaster.do" method="POST" >
		<insta:feedback-panel/>

		<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}">


		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Insurance Plan type Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Insurance Plan Type:</td>
					<td><input type="text" name="category_name" value="${bean.map.category_name}"/></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				</tr>
				<tr>
					<td class="formlabel">Insurance Company Name:</td>
					<td>
						<insta:selectdb name="insurance_co_id" id="insurance_co_id" table="insurance_company_master" valuecol="insurance_co_id" displaycol="insurance_co_name"
								dummyvalue="---Select---" dummyvalueId="" value="${bean.map.insurance_co_id}" orderby="insurance_co_name"/>

						<input type="hidden" name="category_id" value="${ifn:cleanHtmlAttribute(param.category_id==null?bean.map.category_id:param.category_id)}" />
						<input type="hidden" name="insurance_co_name" id="insurance_co_name" value="${ifn:cleanHtmlAttribute(param.insurance_co_name==null?bean.map.insurance_co_name:param.insurance_co_name)}"/>
					</td>


					<%-- <td <c:if test="${param._method =='add'}">style="padding-bottom:22px" </c:if>>
						<c:choose>
						<c:when test="${param._method eq'add'}">
						<div id="_insu_comp_wrapper" >
							<input type="text" name="_insu_comp_tbox" id="_insu_comp_tbox" style="width:138px;" value="" />
							<div id="_insu_comp_dropdown"></div>
							<input type="hidden" name="insurance_co_name" id="insurance_co_name" />
							<input type="hidden" name="insurance_co_id"   name="insurance_co_id"  />
						</div>
						</c:when>
						<c:otherwise>
							<input type="text" readOnly name="insurance_co_name" value="${param.insurance_co_name==null?bean.map.insurance_co_name:param.insurance_co_name}"/>
							<input type="hidden" name="insurance_co_id" value="${param.insurance_co_id==null?bean.map.insurance_co_id:param.insurance_co_id}" />
							<input type="hidden" name="category_id" value="${param.category_id==null?bean.map.category_id:param.category_id}" />
						</c:otherwise>
						</c:choose>
					</td>--%>
				</tr>
				<tr>
					<td class="formlabel">Status :</td>
					<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
					optexts="Active,Inactive" /></td>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/InsuranceCatMaster.do?_method=add">Add</a>
			|
			</c:if>
			<a href="${cpath}/master/InsuranceCatMaster.do?_method=list" onclick="doCancel();">Insurance Plan Type List</a>
			<c:if test="${max_centers_inc_default > 1}">
		 	<c:if test="${param._method=='show'}">
				&nbsp;|&nbsp;
					<insta:screenlink screenId="mas_insurance_cat_center" extraParam="?_method=getScreen&category_id=${param.category_id}"
						label="Center Applicability" />
			</c:if>
			</c:if>
		</div>
	</form>
</body>
</html>

