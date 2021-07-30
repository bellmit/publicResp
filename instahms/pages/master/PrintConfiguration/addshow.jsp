<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add/Edit Print Configuration - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="masters/printmaster.js"/>

		<script>
			function doClose() {
				window.location.href = "${cpath}/master/PrintConfiguration.do?method=list";
			}
			function focus(){
				document.forms[0].header1.focus();
			}

			function showLogo(){
				window.open("${cpath}/showLogo.do?cache=false");
			}

			function deleteLogo(){
				if (!confirm("Do you want to delete Custom Registration Template"))	return false;
				window.location.href ="${cpath}/master/PrintConfiguration.do?method=delete";
			}
		</script>

	</head>
	<body onload="focus();" class="yui-skin-sam">

		<h1>${ifn:cleanHtml(param.print_type)} Print Configuration</h1>

		<insta:feedback-panel/>

		<form action="PrintConfiguration.do?method=${param.method == 'add'?'create' : 'update'}"
			method="POST" >

			<c:if test="${param.method == 'show'}">
				<input type="hidden" name="print_type" value="${ifn:cleanHtmlAttribute(param.print_type)}"/>
			</c:if>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Default Values: </legend>
				<c:set var="makeReadOnly" value="${centerId != 0}"/>
				<table class="formtable" >
					<tr>
						<td class="formlabel">Default Printer Setting:</td>
						<td><insta:selectdb name="printer_id" value="${bean.map.printer_id}" class="dropdown" style="width: 200px;"
							table="printer_definition" 	valuecol="printer_id" displaycol="printer_definition_name" /></td>
						<td class="formlabel">Draft Watermark:</td>
						<td>
							<insta:selectoptions name="pre_final_watermark" value="${bean.map.pre_final_watermark}"
								opvalues="draft,provisional" optexts="Draft,Provisional" dummyvalue="None" />
						</td>
						<td class="formlabel">Duplicate Watermark:</td>
						<td>
							<insta:selectoptions name="duplicate_watermark" value="${bean.map.duplicate_watermark}"
								opvalues="copy,duplicate" optexts="Copy,Duplicate" dummyvalue="None" />
						</td>

					</tr>
					<tr>
						<td class="formlabel">Header Line 1:</td>
						<td><input type="text" class="field" style="width: 200px" name="header1" id="header1" value="${bean.map.header1}" ${makeReadOnly ? 'readonly' : ''}/></td>
						<td class="formlabel">Header Line 2:</td>
						<td><input type="text" class="field" style="width: 200px" name="header2" id="header2" value="${bean.map.header2}" ${makeReadOnly ? 'readonly' : ''}/> </td>
						<td class="formlabel">Header Line 3:</td>
						<td><input type="text" class="field" style="width: 200px" name="header3" id="header3" value="${bean.map.header3}" ${makeReadOnly ? 'readonly' : ''}/></td>
					</tr>
					<tr>
						<td class="formlabel">Footer Line 1:</td>
						<td><input type="text" class="field" style="width: 200px" name="footer1" id="footer1" value="${bean.map.footer1}" ${makeReadOnly ? 'readonly' : ''}/></td>
						<td class="formlabel">Footer Line 2:</td>
						<td><input type="text" class="field" style="width: 200px" name="footer2" id="footer2" value="${bean.map.footer2}" ${makeReadOnly ? 'readonly' : ''}/> </td>
						<td class="formlabel">Footer Line 3:</td>
						<td><input type="text" class="field" style="width: 200px" name="footer3" id="footer3" value="${bean.map.footer3}" ${makeReadOnly ? 'readonly' : ''}/></td>
					</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Center Values: </legend>
				<table class="formtable" >
					<tr>
						<td class="formlabel">Default Printer Setting:</td>
						<td><insta:selectdb name="center_printer_id" value="${centerbean.map.printer_id}" class="dropdown" style="width: 200px;"
							table="printer_definition" 	valuecol="printer_id" displaycol="printer_definition_name" /></td>
						<td class="formlabel">Draft Watermark:</td>
						<td>
							<insta:selectoptions name="center_pre_final_watermark" value="${centerbean.map.pre_final_watermark}"
								opvalues="draft,provisional" optexts="Draft,Provisional" dummyvalue="None"/>
						</td>
						<td class="formlabel">Duplicate Watermark:</td>
						<td>
							<insta:selectoptions name="center_duplicate_watermark" value="${centerbean.map.duplicate_watermark}"
								opvalues="copy,duplicate" optexts="Copy,Duplicate" dummyvalue="None" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Header Line 1:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_header1" id="center_header1" value="${centerbean.map.header1}"/></td>
						<td class="formlabel">Header Line 2:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_header2" id="center_header2" value="${centerbean.map.header2}" /> </td>
						<td class="formlabel">Header Line 3:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_header3" id="center_header3" value="${centerbean.map.header3}" /></td>
					</tr>
					<tr>
						<td class="formlabel">Footer Line 1:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_footer1" id="center_footer1" value="${centerbean.map.footer1}"/></td>
						<td class="formlabel">Footer Line 2:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_footer2" id="center_footer2" value="${centerbean.map.footer2}"/> </td>
						<td class="formlabel">Footer Line 3:</td>
						<td><input type="text" class="field" style="width: 200px" name="center_footer3" id="center_footer3" value="${centerbean.map.footer3}" /></td>
					</tr>
				</table>
			</fieldset>
			<table class="screenActions">
				<tr>
					<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="javascript:void(0)" onclick="doClose();">Print Configurations</a></td>
				</tr>
			</table>
		</form>

	</body>
</html>
