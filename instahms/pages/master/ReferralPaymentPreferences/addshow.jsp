<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Referral Payments Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	function dashboard(contextPath) {
    window.location.href = contextPath + "/master/ReferralPaymentPreferences.do?method=list"
	}

	function percentValue(obj){
	if(obj.checked){
		document.forms[0].payment_percent.value ='Y';
	}	
	else
		document.forms[0].payment_percent.value ='N';
	}

	function validateForm(){
	var amt = document.getElementById("amount");
	var per = document.forms[0].percent;
	var valid = true;
		if(amt.value == ""){
				alert("Enter amount");
				valid = false;
			}else{
					if(per.checked){
						 formatAmountObj(amt, true);
					 	 valid = valid && validateDecimal(amt, "Amount must be a decimal number");
								if (valid && (amt.value > 100)){
										alert("Amount percentage  can not be greater than 100");
										valid = false;
								}
						}
				}	
		 
				if(!valid){
						amt.value="";
						amt.focus();
					}
				return valid;		
		}	
				
</script>
</head>
<body class="yui-skin-sam">

<form action="ReferralPaymentPreferences.do" method="POST" >
	<input type="hidden" name="method" value="${param.method == 'add'? 'create': 'update'}">
	<div class="pageHeader"> Referral Payment Preferences </div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>
	<table align="center"> 
		<c:forEach var="charge" items="${bean}">
		<tr>
				<td>Charge Group Name :</td>
				<td>${charge.map.chargegroup_name}
					<input type="hidden" name="chargegroup_id" value="${charge.map.chargegroup_id}" />
				</td>
		</tr>
		<tr>
				<td>Amount :</td>
				<td><input type="text" name="amount" id="amount" value="${charge.map.amount}" class="number"/></td>
			</tr>
			<tr>
				<td>Precent :</td>
				<td><input type="checkbox" name="percent" id="percent" 
					"${charge.map.payment_percent=='Y'?'checked':''}" onclick="percentValue(this)" />
					<input type="hidden" name="payment_percent" value="N"/>
				</td>
		</tr>
		<tr> 
			<td>Status :</td>
			 <td><insta:selectoptions name="status"  id="status"  style="width:8em;" 
				 value="${charge.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>

		 </tr>
		 </c:forEach> 
	</table>
<div>
<table align="center">
	<tr>
		<td align="center">
				<input type="submit" value="Save" onclick="return validateForm();"/>
				<input type="button" value="Cancel" 
				onclick="return dashboard('${pageContext.request.contextPath}');" />
			</td>
	</tr>
</table>
</div>

</form>

</body>
</html>
