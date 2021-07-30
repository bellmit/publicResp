<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
	<title><insta:ltext key="stores.procurement.stockreorderhelper.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
</head>

<body>
<table width="740px">
	<tr>
		<td>
		<table width="100%">
			<tr width="100%">
				<td align="center">
					<h1><insta:ltext key="stores.procurement.stockreorderhelper.understandingvariousfilters.stockreorder"/></h1>
				</td>
			</tr>
			<tr>
				<td>
				<insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.filters"/>
					<ul>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.byreorderlevels"/></b><insta:ltext key="stores.procurement.stockreorderhelper.basedonreorderlevels.item"/></li>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.indent"/></b><insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.basedonindentcriteria"/></li>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.salesandconsumption"/></b><insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.basedonpastsales.consumptionstatistics"/></li>
					</ul>
					<br/>
				</td>
			</tr>
			<tr>
				<td>
					<h3><insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.byreorderlevels"/></h3>
				</td>
			</tr>
			<tr>
				<td>
								&nbsp; &nbsp;<insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.template1"/>
				</td>
			</tr>
			<tr>
				<td>
					<insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.thevariousreorderlevels"/>
					<ul>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.belowreorderlevel"/></b>:
						 <insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.filterreturnitems.stocklevel.belowthereorderlevel"/></li>
						<br/>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.belowdangerlevel"/></b>:
						<insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.filterreturnitems.stocklevel.belowdangerlevel"/></li>
						<br/>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.belowminimumlevel"/></b>:
						<insta:ltext key="stores.procurement.stockreorderhelper.thestockreorderscreen.filterreturnitems.stocklevel.belowtheminimumlevel"/></li>
						<br/>
						<li><b><insta:ltext key="stores.procurement.stockreorderhelper.none"/></b>:
						<insta:ltext key="stores.procurement.stockreorderhelper.template2"/></li>

					</ul>
				</td>
			</tr>
			<tr>
				<td>
					<p>
						<h3><insta:ltext key="stores.procurement.stockreorderhelper.byindent"/></h3>
						<p><li> &nbsp;&nbsp;<insta:ltext key="stores.procurement.stockreorderhelper.filterretrieves.items.marked"/>
						<br/>&nbsp;&nbsp;<insta:ltext key="stores.procurement.stockreorderhelper.thefollowingnoteexplains"/>
						<ul>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.flagedforpurchase"/></b>:
							<insta:ltext key="stores.procurement.stockreorderhelper.filterreturnitems.flaggedforpurchase"/></li>
							<br/>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.byindentnumber"/></b>:
							<insta:ltext key="stores.procurement.stockreorderhelper.itemsraised.specifiedindentnumber.retrieved"/></li>
							<br/>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.pendingindentsage"/></b>
							<insta:ltext key="stores.procurement.stockreorderhelper.itemsraisedindents.pendingforthepastxdays"/>
							</li>
							<br/>
						</ul>
					</p>
					<br/>
				</p>
			</td>
		</tr>
		<tr>
			<td>
				<p>
					<h3><insta:ltext key="stores.procurement.stockreorderhelper.salesheuristics"/></h3>
					<p><li>&nbsp;&nbsp;<insta:ltext key="stores.procurement.stockreorderhelper.filterreturnsitems.pastsale.consumptionstatistics"/></li>
					<br/>&nbsp;&nbsp;<insta:ltext key="stores.procurement.stockreorderhelper.noteexplainsthevariouscriteria"/>
						<ul>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.salesexceedingqtyxinymonths"/></b>:
							<insta:ltext key="stores.procurement.stockreorderhelper.template3"/>
							</li>
							<br/>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.consumptionbasedonpastxdays"/></b>:
							<insta:ltext key="stores.procurement.stockreorderhelper.template4"/>
							<br/>
							<insta:ltext key="stores.procurement.stockreorderhelper.template5"/>
							</li>
							<br/>
							<li><b><insta:ltext key="stores.procurement.stockreorderhelper.orderquantityfornextydays"/></b>:
							<insta:ltext key="stores.procurement.stockreorderhelper.filterreturnsthequantity.ordered"/>
							</li>
						</ul>
					</p>
				</p>
			</td>
		</tr>
	</table>
	</td>
</tr>
</table>
<br/>
<table align="center">
		<tr>
			<td align="center">
				<form name="helpForm" method="GET">
					<input type="button" value="Back" onclick="javascript:history.go(-1);"/>
			</form>
			</td>
		</tr>
</table>

</body>
</html>
