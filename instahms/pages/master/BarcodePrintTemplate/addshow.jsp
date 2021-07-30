<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Bar Code Print Template</title>
	<insta:link type="css" file="hmsNew.css"/>
	<insta:link type="js" file="editor.js" />
	<script>

		function doSave() {
			var reason = document.barcodeTemplate.reason.value;
			if (reason == '') {
				alert('Please enter the Reason for Customization');
				document.barcodeTemplate.reason.focus();
				return false;
			} else if (reason.length >= 500) {
				alert("Reason for Customization should not exceed 500 chars");
				document.barcodeTemplate.reason.focus();
				return false;
			}
			document.barcodeTemplate.action= "BarcodePrintTemplate.do?method=update";
			document.barcodeTemplate.submit();
		}

		function doReset() {

			var reason = document.barcodeTemplate.reason.value;
	       if (reason != '' && reason.length >= 500) {
		   alert("Reason for Customization should not exceed 500 chars");
		   document.barcodeTemplate.reason.focus();
		   return false;
	      }
	      document.barcodeTemplate.resetToDefault.value = true;
	      document.barcodeTemplate.action= "BarcodePrintTemplate.do?method=update";
	      document.barcodeTemplate.submit();
       }

	</script>
</head>

<body>
	<h1>Bar Code Print Template</h1>

	<insta:feedback-panel/>

	<form method="POST"  name="barcodeTemplate" >
		<input type="hidden" name="method" value="update"/>
		<input type="hidden" name="customized" value="${ifn:cleanHtmlAttribute(param.customized)}"/>
		<input type="hidden" name="resetToDefault" value="false"/>
		<input type="hidden" name="title" value="${ifn:cleanHtmlAttribute(param.title)}">
		<input type="hidden" name="template_type" value="${ifn:cleanHtmlAttribute(param.template_type)}">


		<table class="formtable">
			<tr >
				<td class="formlabel">Reason for Customization :</td>
				<td>
					<input type="text" name="reason" id="reason" size="50" value='<c:out value="${reason}"/>'/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td colspan="4">
					<textarea id="print_template_content" name="print_template_content"
							style="width: 450; height:500;font-family: Courier,fixed"><c:out value="${print_template_content}"/></textarea>
				</td>
			</tr>
		</table>

		<table class="screenActions">
			<tr>
			    <td><input type="button" value="Save" onclick="doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:url var="dashboardUrl" value="BarcodePrintTemplate.do">
						<c:param name="method" value="list"/>
				</c:url>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" name="close" onclick="return gotoLocation('${dashboardUrl}')"/>BarCodeTemplate</a></td>
			</tr>
		</table>
	</form>
</body>
</html>

