<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%--
  usage <insta:CsvDataHandler divid="upload1" action="StoresItemMaster.do"/>

  This will create the Download/Upload etc suitable for any single table master. The methods are
  hardcoded to exportMaster and importMaster.

  Note: the above tag should NOT be inside a form.

  Please ensure that the following are added to the JSP that includes this.
  <insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
  <insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css"/>

  Other things to do:
  * Derive the master action class from AbstractDataHandlerAction
  * Implement the abstract method getTableDataHandler() in that
  * In struts-config, add name="UploadForm" in the action (restart tomcat)
--%>

<%@ attribute name="action" required="true" %>
<%@ attribute name="divid" required="true" %>
<%@ attribute name="method" required="false" %>

<c:set var="methodToUse" value="${(empty method) ? method : '_method'}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<div class="resultList">
	<div id="${divid}" class="CollapsiblePanel">
		<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>

		<table>
			<tr>
				<td>Export: </td>
				<td>
					<form name="exportForm" action="${action}" method="GET"
						style="padding:0; margin:0">
						<div style="float: left">
							<input type="hidden" name="_method" value="exportMaster">
							<button type="submit">Download</button>
						</div>
						<div style="float: left;white-space: normal">
							<img class="imgHelpText"
							src="${cpath}/images/help.png"
							title="Note: The export gives a CSV file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
						</div>
					</form>
				</td>
			</tr>

			<tr>
				<td>Import:</td>
				<td>
					<form name="importForm" action="${action}" method="POST"
						enctype="multipart/form-data">
						<input type="hidden" name="_method" value="importMaster">
						<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
						<button type="button" onclick="return doUpload()">Upload</button>
					</form>
				</td>
			</tr>
		</table>
	</div>
</div>

<script>
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("${divid}", {contentIsOpen: false});
	function doUpload() {
		if (document.importForm.uploadFile.value == '') {
			alert("Please browse and select a file to upload");
			return false;
		}
		document.importForm.submit();
	}
</script>

