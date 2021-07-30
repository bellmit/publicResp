<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Printer Settings - Insta HMS</title>

	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="masters/printmaster.js"/>
	<insta:link type="css" file="widgets.css"/>

	<script>
		function doClose() {
		    window.location.href = "${cpath}/master/PrinterSettingsMaster.do?method=list";
		}
		var page_number = '${bean.map.page_number}';
	</script>

	<style type="text/css">
		.formtable td.hint {
			white-space: normal;
			font-family: Arial;
		}
		#autocomplete_font_name {
			width: 12em;
		}
		div.helpText {
			width: 200px;
		}
	</style>
</head>

<body onload="focus();enableDisableFields()" class="yui-skin-sam">

	<h1>${param.method == 'add' ? 'Add' : 'Edit'} Printer Settings </h1>

	<insta:feedback-panel/>

	<form action="PrinterSettingsMaster.do" method="POST" >

		<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="status" value="A"/>

		<c:if test="${param.method == 'show'}">
			<input type="hidden" name="printer_id" value="${bean.map.printer_id}"/>
		</c:if>

		<fieldset class="fieldSetBorder" >
			<table class="formtable">
				<tr>
					<td class="formlabel">Settings Name:</td>
					<td>
						<c:choose>
							<c:when test="${param.method == 'add'}">
								<input type="text" class="field" style="width: 200px;" name="printer_definition_name" value="" />
							</c:when>
							<c:otherwise>
								<input type="text" class="field" style="width: 200px" name="printer_definition_name" value="${bean.map.printer_definition_name}"
										readonly />
							</c:otherwise>
						</c:choose>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				<tr>
					<td class="formlabel">Print Mode:</td>
					<td>
						<input type="radio" name="print_mode"  value="P" ${bean.map.print_mode=='P'?'checked=1':''}/
							onclick="enableDisableFields()">PDF
						<input type="radio" name="print_mode"  value="T" ${bean.map.print_mode=='T'?'checked=1':''}
							onclick="enableDisableFields()"/>Text
					</td>
					<td colspan="4">
						<label>Use text mode for fast printing on Dot Matrix Printers. PDF is appropriate for Laser printers,
							but can also be used in Dot Matrix Printers.</label>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Logo/Header:</td>
					<td>
						<insta:selectoptions name="logo_header" value="${bean.map.logo_header}" class="dropdown" opvalues="Y,L,H,N" optexts="Logo and Header,Logo Only,Header Only,None"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Footer:</td>
					<td>
						<input type="radio" name="footer"  value="Y" ${bean.map.footer=='Y'?'checked=1':''}/>Yes
						<input type="radio" name="footer"  value="N" ${bean.map.footer=='N'?'checked=1':''} />No
					</td>
				</tr>
				<tr>
					<td class="formlabel">Footer Vertical Position:</td>
					<td>
						<insta:selectoptions name="footer_vertical_position" value="${bean.map.footer_vertical_position}" class="dropdown" opvalues="t,m,b" optexts="Top,Middle,Bottom"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Continuous Feed:</td>
					<td>
						<input type="radio" name="continuous_feed" value="Y" ${bean.map.continuous_feed=='Y'?'checked':''}
									onclick="enableDisableFields();"/>Yes
						<input type="radio" name="continuous_feed"  value="N" ${bean.map.continuous_feed=='N'?'checked':''}
									onclick="enableDisableFields();"/>No
					</td>
					<td colspan="4">
						<label>Continuous feed is useful for Dot Matrix Printers, when you don't want the paper to scroll to the beginning
							of the next page. Printing will stop as soon as the text ends. <b>Note:</b> Works for jrxml prints(built in prints).</label>

					</td>
				</tr>
				<tr>
					<td class="formlabel">Page Number:</td>
					<td>
						<input type="radio" onchange="showPageNumberOptions()" name="page_number" value="Y" ${bean.map.page_number == 'Y'?'checked':''}>Yes
						<input type="radio" onchange="showPageNumberOptions()" name="page_number" value="N" ${bean.map.page_number == 'N'?'checked':''}>No
					</td>
					<td colspan="4">
						<label>Works for custom prints. For template mode 'HTML' and print mode 'PDF'.</label>
					</td>
				</tr>
				<tr style="" id="pg_position">
					<td class="formlabel">Page Number Position':</td>
					<td>
						<input type="radio" name="pg_no_position" value="R" ${bean.map.pg_no_position == 'R'?'checked':''}>Right
						<input type="radio" name="pg_no_position" value="C" ${bean.map.pg_no_position == 'C'?'checked':''}>Center
					</td>
				</tr>
				<tr style="" id="pg_vertical_position">
					<td class="formlabel">Page No. Vertical Position:</td>
					<td>
						<insta:selectoptions name="pg_no_vertical_position" value="${bean.map.pg_no_vertical_position}" class="dropdown" opvalues="t,m,b" optexts="Top,Middle,Bottom"/>
					</td>
				</tr>
				<tr style="" id="pg_no_style">
					<td class="formlabel">Page Number Font Size:</td>
					<td>
						<input type="text" name="pg_no_font_size" class="number" size="10" value="${bean.map.pg_no_font_size}"/>pts
					</td>
				</tr>
				<tr>
					<td class="formlabel">Page Height:</td>
					<c:choose>
						<c:when test="${bean.map.continuous_feed =='Y'}">
							<td class="formpg">
								<input type="text" name="page_height" class="number" size="10"
											value="${bean.map.page_height}" disabled="true"/> pts
							</td>
						</c:when>
						<c:otherwise>
							<td class="formpg">
								<input type="text" name="page_height" class="number" size="10"
											value="${bean.map.page_height}" /> pts
							</td>
						</c:otherwise>
					</c:choose>
					<td colspan="4">
						<label>1 inch = 72 pts. Normal A4 size Page Height is 11.69 inches (= 842 pts).</label>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Top Margin:</td>
					<td>
						<input type="text" name="top_margin" class="number" size="10"
								value="${bean.map.top_margin}"/>pts
					</td>
					<td colspan="4">
						<label>Top Margin space is used for any repeating headers, so remember to leave sufficient space
							for Logo/Header and patient information (if repeating patient information).</label>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Bottom Margin:</td>
					<td><input type="text" name="bottom_margin" class="number" size="10" value="${bean.map.bottom_margin}"/>pts</td>
				</tr>
				<tr>
					<td class="formlabel">Left Margin:</td>
					<td><input type="text" name="left_margin" class="number" size="10" value="${bean.map.left_margin}"/>pts</td>
				</tr>

				<tr>
					<td class="formlabel">Text Mode Columns:</td>
					<td>
						<input type="text" name="text_mode_column" class="number" size="10"
						value="${bean.map.text_mode_column}" />characters
					</td>
					<td colspan="4">
						<label>Text Mode Columns should match the character width of the dot matrix printer.
							Typical values for a 80 column printer are: 80 (normal font), 96 (12 CPI font) and 120 (Condensed).
							<b>Note:</b> Works for custom prints. For template mode 'HTML' and print mode 'Text'.

					</td>
				</tr>

				<tr>
					<td class="formlabel">Text Mode Extra Lines:</td>
					<td>
						<input type="text" name="text_mode_extra_lines" class="number" size="10"
						value="${bean.map.text_mode_extra_lines}" />lines
					</td>
					<td colspan="4">
						<label>After printing, the printer paper fed an extra number of lines equal to this amount. Use this for moving the paper upwards for easy tear-off.
							<b>Note:</b> Works for custom prints. For template mode 'HTML' and print mode 'Text'.</label>
					</td>
				</tr>
				<tr><td>&nbsp;</td></tr>
				<tr>
					<td colspan="3"><b>The following settings are NOT appilcable for some prints*</b></td>
				</tr>

				<tr>
					<td class="formlabel">Default Font Name:</td>
					<td valign="top" class="formpg">
						<div id="autocomplete_font_name">
							<input type="text" name="font_name" id="font_name" class="field"/>
							<div id="container_font_name"></div>
						</div>
						<script>initAutoComplete("font_name","container_font_name",'${bean.map.font_name}'); </script>
					</td>
					<td colspan="4">
						<label>Where the document is editable, the font can be set in the document. If no font is set, this font will be used.</label>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Default Font Size:</td>
					<td>
						<input type="text" name="font_size" class="number" size="10" value="${bean.map.font_size}"/>pts
					</td>
					<td colspan="4">
						<label>Where the document/report is editable, the font size can be set in the document.
							If no size is set, this size will be used.</label>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Page Width:</td>
					<td>
						<input type="text" name="page_width" class="number" size="10" value="${bean.map.page_width}" />pts
					</td>
					<td colspan="4">
						<label>1 inch = 72 pts. Normal A4 Page Width is 8.26 inches (= 595 pts).</label>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Right Margin:</td>
					<td>
						<input type="text" name="right_margin" class="number" size="10"
							value="${bean.map.right_margin}" />pts
					</td>
				</tr>

				<tr>
					<td class="formlabel">Orientation :</td>
					<td>
						<insta:selectoptions name="orientation" class="field" value="${bean.map.orientation}" opvalues="P,L" optexts="Portrait,LandScape"/>
			 		</td>
		 		</tr>

				<tr>
					<td class="formlabel">Repeat Patient Info:</td>
					<td>
						<input type="radio" name="repeat_patient_info" value="Y"
		 						${bean.map.repeat_patient_info == 'Y'?'checked':''} >Yes
		 				<input type="radio" name="repeat_patient_info" value="N"
								${bean.map.repeat_patient_info == 'N'?'checked':''}>No
					</td>
					<td colspan="4">
						<label>When Repeat Patient Info is Yes, then the patient demography information will appear on all pages just below
							the Logo/Header, if any. Note that this will occupy Top Margin area, so remember to increase the top margin
							when setting to Yes, and to reduce top margin when setting to No.
							<br/><b>Note:</b> Works for custom prints. For template mode 'HTML' and print mode 'PDF'. </label>
					</td>
		 		</tr>
			</table>
		</fieldset>

		<div class="screenActions">
			<b>* Extra settings are NOT applicable for the following prints:</b>
			<table>
				<tr><td style="padding-top:2px;padding-bottom:2px">Built in bill prints</td></tr>
				<tr><td>PDF Forms</td></tr>
			</table>
		</div>
		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return validateFields();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Printer Settings List</a></td>
		</tr>
		</table>
	</form>
</body>
</html>

