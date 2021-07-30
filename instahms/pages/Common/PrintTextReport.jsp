<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<body>
		<head><title>Print Text Report - Insta HMS</title>
		<insta:link type="script" file="jquery-2.2.4.min.js" />
		<script type="text/javascript">
			var userid = '<%= session.getAttribute("userid")%>';
			var nexusToken = '<%= session.getAttribute("nexus_token")%>';
			$(function(){
				$("#printBtn").click(function(){
					var data = {
						"document_name": "HMS RAW Document", 
						"data": $("#printData").text(),
						"target": '${printerType}',
					};
					$.ajax({
						"type": "POST",
						"url": "//127.0.0.1:9876/devices/rawprint/print", 
						"headers": { 
							"x-insta-nexus-token": nexusToken,
							"x-insta-nexus-user": userid,
						},
						"beforeSend": function(){
							$("#printStatus").html("");
						},
						"data": JSON.stringify(data),
						"contentType": "application/json",
						"dataType": "json"
					}).done(function(){
						$("#printStatus").html("<b>Print Spooled!</b>");
					}).fail(function(){
						alert("Print Failed. Ensure nexus app is configured to accept raw print requests");
					});
				});
			});
		</script>
		</head>
			<table cellpadding="0" cellspacing="0">
				<tr><td>&nbsp;</td>	</tr>
				<c:set var="cols" value="${empty textColumns ? 125 : textColumns}"/>
				<tr>
					<td>
						<textarea id="printData" rows="30" cols="${cols}" readonly="true" >${ifn:cleanHtml(textReport)}</textarea>
					</td>
				</tr>
				<tr>
					<td style="padding-bottom: 0 10px 20px 10px;">
						<p id="printStatus"></p>
						<input type="button" id="printBtn" value="Print"/>
					</td>
				</tr>
			</table>
	</body>
</html>
