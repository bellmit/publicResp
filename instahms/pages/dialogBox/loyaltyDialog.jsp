<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div id="loyaltyCardDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder" style="cursor: pointer; height: 78px; padding: 11px;">
			<legend class="fieldSetLabel loyaltyPointsLagendLabel">Loyalty Points</legend>
			
		<table class="formtable">
			<tbody>
				<tr>
					<td><span><b>Mobile Number:</b></span>
					<td style="width:200px"><span>+91</span><input type="text" id="loyaltyPointMob" maxlength=10 onkeypress="return enterNumOnlyzeroToNine(event)" onkeyup="MobvalueChanged();">
					<td><button type="button"value="Save" id="getPointsBtn" onClick="getLoyaltyPoints($('#loyaltyPointMob').val(),$('#rowIndex').val());"><b>Get Points</b></button></td>
				</tr>
				<tr>
					<td><span><b>Available Points:</b></span>
					<td><span id="loyaltyPointAmount"></span>
				</tr>
			</tbody>
		</table>
		</fieldset>
		
		<fieldset class="fieldSetBorder" style="cursor: pointer; height: 56px; padding: 11px;">
			<legend class="fieldSetLabel">Redeem Points</legend>
			
		<table class="formtable">
			<tbody>
				<tr>
					<td><span><b>Redeem Points:</b></span>
					<td><input type="text" id="redeemPoints" onkeypress="return enterNumOnlyANDdot(event)" onkeyup="(  parseInt($('#loyaltyPointAmount').attr('data-amount'))+1 > parseInt($('#redeemPoints').val()))?$('#redeemPoints').val($('#redeemPoints').val()):$('#redeemPoints').val(0)">
					<td><input type="hidden" id="rowIndex">
				</tr>
			</tbody>
		</table>
		</fieldset>
		<div style="margin-bottom:28px">
			<button type="button"value="Save" id="sendOTPBtn" style="float:right" disabled onclick="getLoyaltyOTP($('#loyaltyPointMob').val(),$('#redeemPoints').val(),$('#rowIndex').val());"><b>Send OTP</b></button></td>
		</div>
	</div>
</div>