<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Samples Bar Code Print Template</title>
	<insta:link type="css" file="hmsNew.css"/>
	<insta:link type="js" file="editor.js" />
	<script>

		function doSave() {
			var reason = document.samplesbarcodeTemplate.reason.value;
			if (reason == '') {
				alert('Please enter the Reason for Customization');
				document.samplesbarcodeTemplate.reason.focus();
				return false;
			} else if (reason.length >= 500) {
				alert("Reason for Customization should not exceed 500 chars");
				document.samplesbarcodeTemplate.reason.focus();
				return false;
			}
			document.samplesbarcodeTemplate.action= "SampleBarcodePrintTemplate.do?method="+document.samplesbarcodeTemplate.method.value;
			document.samplesbarcodeTemplate.submit();
		}

		function doReset() {

			var reason = document.samplesbarcodeTemplate.reason.value;
	        if (reason != '' && reason.length >= 500) {
			    alert("Reason for Customization should not exceed 500 chars");
			    document.samplesbarcodeTemplate.reason.focus();
			    return false;
	         }
	      document.samplesbarcodeTemplate.resetToDefault.value = true;
	      document.samplesbarcodeTemplate.action= "SampleBarcodePrintTemplate.do?method=update";
	      document.samplesbarcodeTemplate.submit();
       }

	</script>
</head>

<body>
	<h1>Samples Bar Code Print Template</h1>

	<insta:feedback-panel/>

	<form method="POST"  name="samplesbarcodeTemplate" action="SampleBarcodePrintTemplate.do">
		<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}"/>
		<input type="hidden" name="customized" value="${ifn:cleanHtmlAttribute(param.customized)}"/>
		<input type="hidden" name="resetToDefault" value="false"/>
		<input type="hidden" name="title" value="${ifn:cleanHtmlAttribute(param.title)}">


		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td >
					<input type="text" class="field" style="width: 170px" name="template_name" ${ifn:cleanHtmlAttribute(param.method=='show' ? 'readonly' : '')}
								value="${template.map.template_name}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr >
				<td class="formlabel">Reason for Customization :</td>
				<td>
					<input type="text" name="reason" id="reason" size="50" value='<c:out value="${template.map.reason}"/>'/>
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
				<c:url var="dashboardUrl" value="SampleBarcodePrintTemplate.do">
						<c:param name="method" value="list"/>
				</c:url>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" name="close" onclick="return gotoLocation('${dashboardUrl}')"/>Sample BarCode Templates</a></td>
			</tr>
		</table>
	</form>
</body>
</html>

