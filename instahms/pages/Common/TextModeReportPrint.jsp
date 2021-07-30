<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<insta:link type="css" file="hmsNew.css"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<body>
		<head><title>Text Report - Insta HMS</title>
		<insta:link type="script" file="jquery-2.2.4.min.js" />
		<script>

			$(function(){
				$("#printBtn").click(function(){
					$.get(getUrl()).done(function(data){
						printData(data);
					});
				});
			});
			function printData(printData) {
				var data = {
					"document_name": "HMS RAW Document", 
					"data": printData,
					"target": '${printerType}',
				};
				var nexusToken = '<%= session.getAttribute("nexus_token")%>';
				var userid ='<%= session.getAttribute("userid")%>';
				$.ajax({
					"type": "POST",
					"headers": { 
						"x-insta-nexus-token": nexusToken,
						"x-insta-nexus-user": userid,
					},
					"beforeSend": function(){
						$("#printStatus").html("");
					},
					"url": "//127.0.0.1:9876/devices/rawprint/print", 
					"data": JSON.stringify(data),
					"contentType": "application/json",
					"dataType": "json"
				}).done(function(){
					$("#printStatus").html("<b>Print Spooled!</b>");
				}).fail(function(){
					alert("Print Failed. Ensure nexus app is configured to accept raw print requests");
				});				
			}
			function getUrl() {
				var url = '${ifn:cleanJavaScript(url)}';
				return url;
			}
			function getTextReport() {
				var url = getUrl();
				window.open(url);
			}
		</script>

		</head>
		<form action="">
		<table align="center"></table>
		<table cellpadding="0" cellspacing="0" align="center">
			<tr style="height: 2em"></tr>
			<tr style="text-align:right">
				<td style="padding-bottom: 0 10px 20px 10px;">
					<p id="printStatus"></p>
					<input type="button" id="printBtn" value="Print"/>
					<input type="button" value="Download" align="middle" onclick="getTextReport();" style="margin-left:20px;"/>
 				</td>
			</tr>
		</table>
		</form>
	</body>
</html>
