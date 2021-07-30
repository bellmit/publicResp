<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.CARD_TYPE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Card Type - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=card_type" +
						"&sortReverse=false";
		}
		function focus() {
			document.cardtypemasterform.card_type.focus();
		}
       function checkMaximumLength(commission) {
    		var val = parseFloat(commission);
    		if ( val < 0.01 || val > 9.99) {
    			alert("Commission Percentage Should be between 0 and 10");
    			return false;
    		}
    		return true;
       }
       function validateForm() {
    		var commission = document.cardtypemasterform.commission_percentage.value;
    		if(commission != '') {
    			if(!isDecimal(commission,2)) {
    				alert("Invalid Commission Percentage");
    				return false;
    			}
    			if(!checkMaximumLength(commission)) 
    				return false;
    		}

    		return true;
    	}

   	</script>
</head>

<body>
 <h1>Add Card Type</h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
 
<form action="${actionUrl}"  name="cardtypemasterform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Card Type:</td>
			<td>
				<input type="text" name="card_type" value="" onblur="capWords(card_type)" class="required validate-length" length="50" 
						title="Card type is required and max length of name can be 50"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Commission (%):</td>
			<td>
				<input type="text" name="commission_percentage"  value="" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="" opvalues="A,I" optexts="Active,InActive"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();">Card Type List</a>
	</div>
</form>
</body>
</html>
