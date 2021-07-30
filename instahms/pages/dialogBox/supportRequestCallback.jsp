<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div id="request-callback-modal" style="display: none">
	<div class="bd">
		<form name="requestcallbackform" id="requestcallbackform">
		<fieldset class="fieldSetBorder" style="height: 120px !important;">
			<legend class="fieldSetLabel"><insta:ltext key="ui.label.request.callback"/></legend>
			<table width="100%">
				<tr>
					<td style="padding-top: 10px">
							<label for="mr_no"><insta:ltext key="ui.label.callback.requestee"/><span class="star">*</span></label>
							<input type="text" id="request_callback_requestee" value=""/>
					</td>
				</tr>
				<tr>
					<td style="padding-top: 10px">
						<label for="callback_number"><insta:ltext key="ui.label.callback.number"/><span class="star">*</span></label>
						<input type="text" id="request_callback_number" value="" placeholder="<insta:ltext key='ui.label.callback.number.placeholder'/>" />
					</td>
				</tr>
			</table>
		</fieldset>
			<table>
			<tr>
				<td>
					<input type="button" name="btnAmendX" value="Cancel" onclick="closeRequestCallbackModal()"/>
				</td>
				<td>
					<input type="button" name="btnAmendOk" value="Save" onclick="submitRequestCallbackForm()"/>
				</td>
			</tr>
			</table>
		</form>
	</div>
</div>