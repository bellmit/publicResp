<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div id="break-the-glass-modal" style="display: none">
	<div class="bd">
		<form name="break_the_glass_form" id="break_the_glass_form">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="ui.label.break.the.glass"/></legend>
			<table width="100%">
				<tr>
					<td style="padding-top: 10px">
							<label for="mr_no"><insta:ltext key="ui.label.mr.number.slash.name"/><span class="star">*</span></label>
						<div id="break_the_glass_form_patient">
							<input type="text" id="break_the_glass_form_patient_name" value=""/>
							<div id="break_the_glass_form_patient_container" class="scrolForContainer" style="width: 250px"></div>
							<input type="hidden" id="break_the_glass_form_mr_no" value="" />
						</div>
					</td>
				</tr>
				<tr>
					<td style="padding-top: 40px">
						<label for="remarks"><insta:ltext key="ui.label.remarks"/><span class="star">*</span></label>
						<textarea name="break_the_glass_form_remarks" id="break_the_glass_form_remarks" style="width: 100%" rows="7"></textarea>
					</td>
				</tr>
			</table>
		</fieldset>
			<table>
			<tr>
				<td>
					<input type="button" name="btnAmendX" value="Cancel" onclick="closeBreakTheGlassModal()"/>
				</td>
				<td>
					<input type="button" name="btnAmendOk" value="Save" onclick="submitBreakTheGlassForm()"/>
				</td>
			</tr>
			</table>
		</form>
	</div>
</div>