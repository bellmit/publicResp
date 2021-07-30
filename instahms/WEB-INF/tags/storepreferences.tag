<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="addBox" required="false" %>
<%@ attribute name="showtype" required="true" %>  <%-- 'SE' for Stock Entry and 'Sales' for Sales --%>
<%@ attribute name="vatPref" required="true" %>
<%

	com.insta.hms.master.GenericPreferences.GenericPreferencesDTO  pref =  com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getGenericPreferences();
	request.setAttribute("pref", pref);
%>
<jsp:useBean id="vattype" class="java.util.HashMap"/>
<c:set target="${vattype}" property="C" value="Cost Price Based (without bonus)"/>
<c:set target="${vattype}" property="CB" value="Cost Price Based (with bonus)"/>
<c:set target="${vattype}" property="MB" value="MRP Based (with bonus)"/>
<c:set target="${vattype}" property="M" value="MRP Based (without bonus)"/>

<jsp:useBean id="defsel" class="java.util.HashMap"/>
<c:set target="${defsel}" property="Y" value="Yes"/>
<c:set target="${defsel}" property="N" value="No"/>

<jsp:useBean id="defatt" class="java.util.HashMap"/>
<c:set target="${defatt}" property="A" value="Allow"/>
<c:set target="${defatt}" property="D" value="Disallow"/>
<c:set target="${defatt}" property="W" value="Warn"/>
<c:set target="${defatt}" property="N" value="Disallow"/>

<jsp:useBean id="pttype" class="java.util.HashMap"/>
<c:set target="${pttype}" property="H" value="Hospital"/>
<c:set target="${pttype}" property="R" value="Retail"/>
<c:set target="${pttype}" property="C" value="Retail Credit"/>

<jsp:useBean id="salereturn" class="java.util.HashMap"/>
<c:set target="${salereturn}" property="Y" value="Required"/>
<c:set target="${salereturn}" property="N" value="Optional"/>

<c:if test="${empty addBox}"><c:set var="addBox" value="true"/></c:if>
<c:if test="${addBox}">
	<fieldset class="fieldSetBorder" style="margin-bottom: 5px;">
	<legend class="fieldSetLabel">Stores Generic Preferences</legend>
	<table class="infotable" cellpadding="0" cellspacing="0" width="100%">
</c:if>
<c:choose>
<c:when test="${showtype eq 'SE'}">
	<tr>
		<td class="formlabel">Use Max Cost Price:</td>
		<td class="forminfo">${defsel[pref.pharmacyValidateCostPrice] }</td>
		<td class="formlabel">Qty Default To Issue Units:</td>
		<td class="forminfo">${defsel[pref.qtyDefaultToIssueUnit] }</td>
		<td class="formlabel">Allow decimals for qty:</td>
		<td class="forminfo">${defsel[pref.allowdecimalsforqty] }</td>
	</tr>
	<tr>
		<td class="formlabel">Stock Entry With PO:</td>
		<td class="forminfo">${defsel[pref.seWithPO] }</td>
	</tr>

</c:when>
<c:when test="${showtype eq 'Sales'}">
	<tr>
		<td class="formlabel">Default Sale Option:</td>
		<td class="forminfo">${pttype[pref.pharmacyPatientType] }</td>
		<td class="formlabel"> Stock Negative Sale:</td>
		<td class="forminfo">${defatt[pref.stockNegativeSale] }</td>
		<td class="formlabel">Auto Round Off in Sales:</td>
		<td class="forminfo">${defsel[pref.pharmaAutoRoundOff] }</td>
	</tr>
	<tr>
		<td class="formlabel">Expired Items Sale:</td>
		<td class="forminfo">${defatt[pref.saleOfExpiredItems] }</td>
		<td class="formlabel">Expiry Warn Days:</td>
		<td class="forminfo">${pref.warnForExpiry }</td>
		<td class="formlabel">Schedule H Drug Alert:</td>
		<td class="forminfo">${defsel[pref.hdrugAlert] }</td>
	</tr>
	<tr>
		<td class="formlabel">Original Bill for Returns:</td>
		<td class="forminfo">${salereturn[pref.pharmaReturnRestricted] }</td>
		<td class="formlabel">Allow CP Based Sale:</td>
		<td class="forminfo">${defsel[pref.pharmaAllowCpSale] }</td>
		<td class="formlabel">CP Based Sale Margin:</td>
		<td class="forminfo">${pref.pharmacySaleMargin } %</td>
	</tr>
</c:when>
<c:otherwise>
		<tr>
		<td class="formlabel">Stock Entry With PO:</td>
		<td class="forminfo">${defsel[pref.seWithPO] }</td>
	</tr>
	<tr>
		<td class="formlabel">Use Max Cost Price:</td>
		<td class="forminfo">${defsel[pref.pharmacyValidateCostPrice] }</td>
		<td class="formlabel">Qty Default To Issue Units:</td>
		<td class="forminfo">${defsel[pref.qtyDefaultToIssueUnit] }</td>
		<td class="formlabel">Allow decimals for qty:</td>
		<td class="forminfo">${defsel[pref.allowdecimalsforqty] }</td>

	</tr>
	<tr>
		<td class="formlabel">Default Sale Option:</td>
		<td class="forminfo">${pttype[pref.pharmacyPatientType] }</td>
		<td class="formlabel"> Stock Negative Sale:</td>
		<td class="forminfo">${defatt[pref.stockNegativeSale] }</td>
		<td class="formlabel">Auto Round Off in Sales:</td>
		<td class="forminfo">${defsel[pref.pharmaAutoRoundOff] }</td>
	</tr>
	<tr>
		<td class="formlabel">Expired Items Sale:</td>
		<td class="forminfo">${defatt[pref.saleOfExpiredItems] }</td>
		<td class="formlabel">Expiry Warn Days:</td>
		<td class="forminfo">${pref.warnForExpiry }</td>
		<td class="formlabel">Schedule H Drug Alert:</td>
		<td class="forminfo">${defsel[pref.hdrugAlert] }</td>
	</tr>
	<tr>
		<td class="formlabel">Original Bill for Returns:</td>
		<td class="forminfo">${salereturn[pref.pharmaReturnRestricted] }</td>
		<td class="formlabel">Allow CP Based Sale:</td>
		<td class="forminfo">${defsel[pref.pharmaAllowCpSale] }</td>
		<td class="formlabel">CP Based Sale Margin:</td>
		<td class="forminfo">${pref.pharmacySaleMargin } %</td>
	</tr>
</c:otherwise>
</c:choose>
 <c:if test="${addBox}">
    </table>
    </fieldset>
 </c:if>
