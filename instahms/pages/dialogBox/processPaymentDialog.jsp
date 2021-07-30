<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div id="processPaymentDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder" style="cursor: pointer; height: 78px; padding: 11px;">
			<legend class="fieldSetLabel">Payment Process</legend>
			<div id="pineLabsResponse">Processing payment on EDC machine and waiting for response...</div>
		</fieldset>
		
		<div style="margin-bottom:28px">
			<button type="button"value="Cancel" id="cancelTransaction" style="float:left" onclick="closeProcessPaymentDialog();"><b>Cancel Transaction</b></button>
			<input type="hidden" name="paymentRowNum" id="paymentRowNum" value="">
		</div>
	</div>
</div>
<div id="salucroIframeDialog" style="display:none">
	<div class="hd" id="headerSalucro"> </div>
	<div class="bd">
		<iframe id="salucroIframe" src="" width="800px" height="600px"></iframe>
	</div>
</div>