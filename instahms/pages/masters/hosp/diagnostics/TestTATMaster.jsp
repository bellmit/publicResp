<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="dtoList" value="${dtoList}"/>
	
	
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="common.js"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Test TAT Master</title>
	<script>
	function init(){
		var lessOption=document.getElementById("aMore");
		lessOption.innerHTML=null;
		populateCities();
		loadcity();
	}
	var citiesJSON = ${cities_json};
	function validate() {
		var lth=document.getElementsByName("logistics_tat_hours");
		var cst=document.getElementsByName("conduction_start_time");
		var cth=document.getElementsByName("conduction_tat_hours");
		
		if(lth!=undefined){
			for (var index = 0; index < lth.length; index++) {
				if(lth[index]!=undefined && lth[index].value!=undefined 
						&& lth[index].value!=null && !empty(lth[index].value)){
					if(!CheckDecimal(lth[index])) {
	    				alert("More than two digits after decimal is not allowed.");
	    				lth[index].focus();
	    				return false;
	    			}
					if(parseFloat(lth[index].value)>1000.00){
						alert("Logistics TAT should not exceed 1000 hours");
						lth[index].focus();
						return false;
						}
				}
			}
		}
		if(cth!=undefined){
			for (var index = 0; index < cth.length; index++) {
				if(cth[index]!=undefined && cth[index].value!=undefined 
						&& cth[index].value!=null && !empty(cth[index].value)){
					if(!CheckDecimal(cth[index])) {
	    				alert("More than two digits after decimal is not allowed.");
	    				cth[index].focus();
	    				return false;
	    			}
					if(parseFloat(cth[index].value)>1000.00){
						alert("Conduction TAT should not exceed 1000 hours");
						cth[index].focus();
						return false;
							}
				}
			}
		}
		if(cst!=undefined){
			for (var index = 0; index < cst.length; index++) {
				if(cst[index]!=undefined && cst[index].value!=undefined 
					&& !empty(cst[index].value)){
					if(validateTime(cst[index]))
						continue;
					return false;					
				 }
			}
		}
	
		return true;
	}
	function populateCities() {
		
		var stateId = document.getElementById('state_id').value;
		var city = document.getElementById('city_id');
		var cityArray = [];
		
		var index =0;
		for (var i=0; i<citiesJSON.length; i++) {
			
			var record = citiesJSON[i];
			if (empty(stateId) || stateId == record.state_id) {
				cityArray[index]=record;
				index++;
			}
		}
		loadSelectBox(city, cityArray, "city_name", "city_id", "--Select--", "");
	}
    function CheckDecimal(inputtxt)   
    {   
    var decimal = new RegExp(/^\d*(?:\.\d{0,2})?$/);   
    if(inputtxt.value.match(decimal))
    return true;
    else
    return false;
    }   
	
	function loadcity(){
		var paramValue = '${ifn:cleanJavaScript(param.city_id)}';
		var cityobj = document.getElementById('city_id');
		setSelectedIndex(cityobj, paramValue);		
	}
	function applyForAll(object){
		if (object.checked) {
				var tbl = document.getElementById("resultTable");
				if (tbl != undefined && tbl != null) {
					var rowCount = tbl.rows.length;
					var firstRow = document.getElementById("resultTable").rows[1];

					if (firstRow != undefined && firstRow != null) {
						for (var i = 2; i < rowCount; i++) {
							var row = tbl.rows[i];
							if (row != undefined && row != null) {
								row.cells[3].children[0].value = firstRow.cells[3].children[0].value;
								var cell4 = row.cells[4].children;
								for (var j = 0; j < cell4.length; j++) {
									cell4[j].checked = firstRow.cells[4].children[j].checked;
								}
								row.cells[6].children[0].value = firstRow.cells[6].children[0].value;
								row.cells[7].children[2].value = firstRow.cells[7].children[2].value;
							}
						}
					}
				}
			}
		}
	</script>	
	<insta:js-bundle prefix="common"/>
	</head>
	<body onload="init();" >
	
		<h1>Test TAT Details</h1>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Definition</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Test Name:</td>
					<td class="forminfo">${testDeatils.map.test_name }</td>
					<td class="formlabel">Department:</td>
					<td class="forminfo">${testDeatils.map.ddept_name }</td>
					<td class="formlabel">Status:</td>
					<td class="forminfo">${testDeatils.map.status == 'A' ?'Active':'Inactive'}</td>
				</tr>
			</table>
		</fieldset>
	<form name="searchForm" >
		<input type="hidden" name="_searchMethod" value="getDetails"/>
		<input type="hidden" name="_method" value="getDetails"/>
		<input type="hidden" name="test_id" value="${ifn:cleanHtmlAttribute(param.test_id)}" />		
		<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="">
		<div class="searchBasicOpts" >
		<div class="sboField">
				<div class="sboFieldLabel">State</div>
				<div class="sboFieldInput">
					<select name="state_id" id="state_id" class="dropdown" onchange="populateCities()">
							<option value="">-- Select --</option>
							<c:forEach var="record" items="${states}">
								<option value="${record.STATE_ID}" ${param['state_id'] == record.STATE_ID ? 'selected' : ''}>
								   <insta:truncLabel value="${record.STATE_NAME}" length="15"/>
								</option>
							</c:forEach>
						</select>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">City</div>
				<div class="sboFieldInput">
					<select name="city_id" id="city_id" class="dropdown">
					</select>
				</div>
			</div>
			<div class="sboField">
					<div class="sboFieldLabel">Source Center </div>
					<div class="sboFieldInput">
						<select name="source_center_id" id="source_center_id" class="dropdown">
							<option value="">-- Select --</option>
							<c:forEach items="${centers}" var="center">
								<option value="${center.map.center_id}" ${param['source_center_id'] == center.map.center_id ? 'selected' : ''}>
									${center.map.center_name}
								</option>
							</c:forEach>
						</select>
						<input type="hidden" name="source_center_id@cast" value="y"/>
					</div>
			</div>
			<div class="sboField" style="padding-bottom:50px">
					<div class="sboFieldLabel">Outsource Center</div>
					<div class="sboFieldInput">
						<select name="outsource_name" id="outsource_name" class="dropdown">
							<option value="">-- Select --</option>
							<c:forEach var="record" items="${outHouseNames}">
								<option value="${record.OUTSOURCE_NAME}" ${param['outsource_name'] == record.OUTSOURCE_NAME ? 'selected' : ''}>
								   <insta:truncLabel value="${record.OUTSOURCE_NAME}" length="35"/>
								</option>
							</c:forEach>
						</select>
						<input type="hidden" name="outsource_name@op" value="ico" />
					</div>
				</div>
				
		</div>
		</insta:search>
	</form>
	<form action="testtatmaster.do?_method=save" name="testtatmaster" method="POST">
	<input type="hidden" name="_method" value="save"/>
	<input type="hidden" name="test_id" value="${ifn:cleanHtmlAttribute(param.test_id)}" />
	<div class="resultList" >
		<table class="dataTable" cellspacing="0" cellpadding="0" id="resultTable">
			<tr>
				<th>#</th>
				<th>Center Name</th>
				<th>Outsource Dest</th>
				<th>Logistics TAT<label style="font-size: 9px;" >(hours)</label></th>
				<th>Processing Days</th>
				<th></th>
				<th>Receive Cut Off Time<label style="font-size: 9px;" >(HH:MM)</label></th>
				<th>Conduction TAT<label style="font-size: 9px;" >(hours)</label></th>
			</tr>
			<c:forEach items="${dtoList}"  var="bean" varStatus="st">
					<tr>					
						<td>
							${st.index + 1}
						</td>					
						<td >${bean.map.center_name}</td>
						<td>
							${bean.map.outsource_name}
						</td>
						<td >
						<input type="text" name="logistics_tat_hours" size="10" value="${bean.map.logistics_tat_hours}"
				                 onkeypress="return enterNumAndDot(event)" class="number"/>
						</td>
						
						<td >
						<input type="checkbox" name="processing_Day${st.index + 1}0"  ${(_processing_days[st.index][0]) == true ? "checked" : ""}>S
						<input type="checkbox" name="processing_Day${st.index + 1}1"  ${(_processing_days[st.index][1]) == true ? "checked" : ""}>M
						<input type="checkbox" name="processing_Day${st.index + 1}2"  ${(_processing_days[st.index][2]) == true ? "checked" : ""}>T
						<input type="checkbox" name="processing_Day${st.index + 1}3"  ${(_processing_days[st.index][3]) == true ? "checked" : ""}>W
						<input type="checkbox" name="processing_Day${st.index + 1}4"  ${(_processing_days[st.index][4]) == true ? "checked" : ""}>Th
						<input type="checkbox" name="processing_Day${st.index + 1}5"  ${(_processing_days[st.index][5]) == true ? "checked" : ""}>Fr
						<input type="checkbox" name="processing_Day${st.index + 1}6"  ${(_processing_days[st.index][6]) == true ? "checked" : ""}>Sa
						</td>
						<td></td>
						<td >
						<input type="text" name="conduction_start_time" class="timefield"   value="${_conduction_start_time[st.index]}" maxlength="5"/>
						</td>
						<td>
							<input type="hidden" name="tat_center_id" value="${bean.map.tat_center_id}"/>
							<input type="hidden" name="center_id" value="${bean.map.center_id}"/>
								<input type="text" name="conduction_tat_hours" size="10" value="${bean.map.conduction_tat_hours}"
				                  onkeypress="return enterNumAndDot(event)" class="number"/>
				    	</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${empty dtoList}">
			<insta:noresults hasResults="${hasResults}"/>	
			</c:if>	
		</div>
		<table class="screenAction">
		<tr>
			<td>&nbsp;
			Apply For All:
			</td>
			<td>
				<input id="ApplyAll" type="checkbox"  value="" name="ApplyAll" onclick="applyForAll(this)" ></input>
			</td>
		</tr>
		
		<tr>
			<td>
				<input id="save" type="submit"  value="Save" name="save" onclick="return validate()" ></input>
			</td>
			<td>|&nbsp;
				<a href="${cpath }/master/addeditdiagnostics/show.htm?&testid=${ifn:cleanURL(param.test_id)}&orgId=ORG0001">
						Test Details
				</a>
			</td>
			
		</tr>
		
		</table>
	
					
    </form>	
</body>
</html>