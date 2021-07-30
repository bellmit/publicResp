<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.COUNTRY_MASTER_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Country - Insta HMS</title>

	<script>
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=country_name&sortReverse=false&status=A";
		}
		
		function restrictInactiveCountry() {
			var countryId = document.forms[0].country_id.value;
			var countryName =  document.forms[0].country_name.value;
			var status = document.forms[0].status.value;
			var stateList = ${ifn:convertListToJson(statesList)};
			
			for (var i=0;i<stateList.length;i++) {
				if(status == 'I'){
					if(stateList[i].country_id == countryId && stateList[i].status == 'A') {
						alert("Active states are mapped with this country "+countryName+". Hence, it can not be marked as Inactive.");
							return false;
					}
				}
			}
			return true;
		}
	</script>
	<insta:link type="css" file="select2.min.css"/>
	<insta:link type="css" file="select2Override.css"/>
	<style type="text/css">
		.select2-selection__rendered{
			text-align:left;
			padding-left:4px !important;	
		}
	</style>

</head>
<body>
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
<form action="${actionUrl}" method="POST" onsubmit ="return validateForm()" autocomplete="off">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="country_id" value="${bean.country_id}"/>

	<h1>Edit Country</h1>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Country:</td>
				<td>
					<div style="margin-top:13px" id="country_name_input_div">
						<input type="text" name="country_name" id="country_name_input" value="${bean.country_name}" class="required validate-length" length="50" title="Name is required and max length of name can be 50" />
					</div>
					<div style="display:none;" id="country_name_dropdown_div">
						<select id="country_name_dropdown" class="dropdown"  name="country_name" disabled>
							<c:if test="${not isCountryValid}">
								<option value='' selected> - Select - </option>
							</c:if>
							<c:forEach items="${countryList}" var="list">
									<c:choose>					
										<c:when test="${fn:containsIgnoreCase(list[1], bean.country_name)}">		
											<option value='${list[0]}' selected> ${list[1]}  </option>
										</c:when>	
										<c:otherwise>
											<option value='${list[0]}'> ${ list[1]}   </option>	
										</c:otherwise>
									</c:choose>														  
							</c:forEach>
						</select>
					</div>
					<div  id="country_error">
						<c:if test="${not isCountryValid}">
							<span style="color:red"> Please select a valid country </span>
						</c:if>
					</div>
				</td>
				<td>&nbsp; </td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Country Code:</td>
				<td><input type="text" name="country_code" id="country_code" value="${bean.country_code}" readonly/></td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return restrictInactiveCountry()"><b><u>S</u></b>ave</button></td>	
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Country List</a></td>
		</tr>
	</table>
</form>
<insta:link type="js" file="select2.min.js"/>
<script>
   var countryDropdown = $("#country_name_dropdown");
    $("#country_name_input").focus(function(){
    	$("#country_name_input_div").css("display","none");
    	$("#country_name_input").prop('disabled',true);
    	
    	$("#country_name_dropdown_div").css("display", "block");     	
    	countryDropdown.prop("disabled",false).select2().select2("open");
    	countryDropdown.trigger("change"); //update country code
    	$("#country_error").css('display','none'); //clear error text
    	$("#country_name_input_div").css("margin-top",'');
    });
    countryDropdown.on('change',function(e){
    	var ajaxobj = newXMLHttpRequest();
		ajaxobj.onreadystatechange = function(){
			if (this.readyState == 4 && this.status == 200) {
		    	try{
			    	var json = JSON.parse(this.responseText);
			    	$("#country_code").val(json['result']);
		    	}
		    	catch(e){
		    		console.log('Invalid response');
		    	}
		    }
		};
		if(this.value != ''){ 
			var url = cpath + "/master/countries/getCountryCodeFromRegion.json?region_code=" + encodeURIComponent(this.value);
		    ajaxobj.open("GET", url.toString(), false);
		    ajaxobj.send();
		}
		else{ //country is not  selected
			$("#country_code").val('');
		}
		//focus the  select2 element. Because when TAB is pressed,We want to shift focus to next element of select2
		$("[aria-labelledby = select2-" + countryDropdown.attr("id") + "-container]").focus();
    	
    });   
    function validateForm(){   	
    	if($("#country_code").val() == ''){
			alert('Please select a valid country');
			return false;
		}
    	<c:if test="${not isCountryValid}">
    		if($("#country_name_input").prop('disabled') != true){ // Country input field is disbled if dropdown is opened
    			alert('Please select a valid country');
    			return false;
    		}
    	</c:if>
    	
		return true;
	}
    <c:if test="${isCountryValid}"> 
    	$("#country_name_input_div").css("margin-top",'');// since error text is not displayed. Reset margin-top
	</c:if>
    
   
</script>
</body>
</html>
