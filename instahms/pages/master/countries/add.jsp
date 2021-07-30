<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.COUNTRY_MASTER_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Country - Insta HMS</title>

	<script>
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=country_name&sortReverse=false&status=A";
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
<h1>Add Country</h1>
<insta:feedback-panel/>
<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
<form action="${actionUrl}" method="POST" onsubmit = "return validateForm()">
	<input type="hidden" name="_method" value="create">

	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Country</td>
				<td>
				<select id="country_name" class="dropdown"  name="country_name">
					<c:if test="${empty param.country_name}"> <%--Show -select- only when no country is selected --%>
						<option value='' selected> - Select - </option>	
					</c:if>
					<c:forEach items="${countryList}" var="list">
								<c:choose>								
									<c:when test="${list[1] == param.country_name}">		
										<option value='${list[0]}' selected> ${list[1]}  </option>
									</c:when>	
									<c:otherwise>
										<option value='${list[0]}'> ${ list[1]}   </option>	
									</c:otherwise>
								</c:choose>														  
					</c:forEach>
				</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status</td>
				<td>
					<select name="status" class="dropdown">
						<option value="A" selected="true">Active</option>
						<option value="I" >InActive</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Country Code</td>
				<td><input type="text" name="country_code" id = "country_code" readonly/></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S">
			<b><u>S</u></b>ave</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Country List</a></td>
		</tr>
	</table>
</form>
<insta:link type="js" file="select2.min.js"/>
<script type="text/javascript">	 
	var countryCode = $("#country_code");
	var country = $("#country_name");
	country.select2();
	
	country.on('change', function (e) {
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.onreadystatechange = function(){
			if (this.readyState == 4 && this.status == 200) {
		    	try{
			    	var json = JSON.parse(this.responseText);
			    	countryCode.val(json['result']);
		    	}
		    	catch(ex){
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
			countryCode.val('');
		}
		//focus the  select2 element. Because when TAB is pressed,We want to shift focus to next element of select2
		$("[aria-labelledby = select2-" + country.attr("id") + "-container]").focus();
		
	});
	if(country.val() != ''){
		country.trigger("change");
	}
	else{ //country is not  selected
		countryCode.val('');
	}
	
	function validateForm(){
		if(country.val() == ''){
			alert('Please select a valid country');
			return false;
		}
		return true;
	}
</script>
</body>
</html>
