
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Dialysis Score Card - Insta HMS</title>
	<#assign logopath=getScreenLogoPath()>
	<style>
		@page {
			size: 595pt 842pt;
			margin: 36pt 36pt 36pt 36pt;
		}


		body {
			font-family: Arial, sans-serif;
			font-size: 14px;
		}

		div.resultTable {
			overflow: auto;
			width: 953px;
		}

		table.resultTable {
			border: 1px #666666 solid;
			empty-cells: show;
			width: 70%;
			border-collapse:collapse;
		}

		table.resultTable td {
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			color: #000000;
			padding-left:2px;
		}

		table.resultTable td.blue{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			background: #4592e4;

		}

		input.blue {
			background: #4592e4;
		}

		table.resultTable td.lightblue{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			background: #bedce5;

		}

		input.lightblue {
			background: #bedce5;
		}

		table.resultTable td.darkgreen{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: center;
			background: #00b456;

		}

		input.darkgreen {
			background: #00b456;
		}


		table.resultTable td.green{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			background: #c3f94a;

		}

		input.green {
			background: #c3f94a;
		}

		table.resultTable td.yellow{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			background: #ffff00;

		}

		input.yellow {
			background: #ffff00;
		}


		table.resultTable td.red{
			border-top: 1px #666666 solid;
			border-bottom: none;
			border-right: 1px #666666 solid;
			height: 30px;
			text-align: left;
			background: #ff0000;
		}

		input.red{
			background: #ff0000;
		}

		table.resultTable th {
			text-align: left;
			margin-top: 1px;
			color: #333;
			background-image: url(../images/clmn_hd_bg.jpg);
			border: none;
			height: 26px;
			font-weight: normal;
		}

		img#bg {
		  height:60px;
		  position:absolute;
		  right:0px;
		  z-index: -100;
		}

		div#content {
		  width:89px;
		  height:30px;
		}

		#content {
		  position: relative;
		  top: -30px;
		  z-index: 1;
		  width:89px;
		  height:30px;
		}
	</style>
</head>
<#escape x as x?html>
<#macro getMarkerStars severity>
	<#if severity??>
		<#if severity == 'red'>
			<span style="vertical-align: top;  position:relative; top:-10; right:0;">**</span>
		</#if>
		<#if severity == 'yellow'>
		 	<span style="vertical-align: top;  position:relative; top:-10; right:0;">*</span>
		</#if>
	<#else>
	</#if>
</#macro>

<body>
	<div align="center">
		<span style="font-size: 12pt;font-weight: bold;margin-top:10px;"> Dialysis Score Card </span>
		<#if logopath?? && logopath != ' ' && logopath != ''>
		<img src="${logopath}" alt="background" id="bg" class="bg"/>
		</#if>
		<p style="margin-top:2px;">For ${monthyear!} <span style="font-size: 8pt">(as of ${currtime!})</span></p>

	</div>
	<table id="grid" class="resultTable" style="width:100%;overflow: hidden;">
		<tr id="1">
			<td class="blue" width="20%">
				Patient Name:
			</td>
			<td class="blue">
				${patname!}
			</td>

			<td class="blue">
				${monthyear!}
			</td>
			<td class="blue">
				MR No: ${mr_no!}
			</td>

			<td class="blue">
				Age/Gender: ${ageGender!}
			</td>
		</tr>

		<tr id="2">
			<td class="blue" style="border-top-width: 1px;border-bottom-color:#4592e4;">
				Dialysis Adequacy
			</td>
			<td id="ktvRow" class="${ktvSeverity!"green"}">
				kt/v = ${ktv!"NOT DONE"}
				<@getMarkerStars  severity=ktvSeverity />
			</td>

			<td  id="urrRow" class="${urrSeverity!"green"}">
				URR = ${urr!"NOT DONE"}
				<@getMarkerStars  severity=urrSeverity />
			</td>
			<td id="bloodRow" class="${bloodSeverity!"green"}">
				Blood Flow(Qb) = ${blood!"NOT DONE"}
				<@getMarkerStars  severity=bloodSeverity />
			</td>

			<td class="blue">
			</td>
		</tr>

		<tr id="3">
			<td class="blue" style="border-top-color:#4592e4;">

			</td>
			<td class="darkgreen">
				${ktvRange!">1.2"}
			</td>

			<td class="darkgreen">
				${urrRange!">65%"}
			</td>
			<td class="darkgreen">
				${bloodRange!">250 ml/Min"}
			</td>
			<td class="darkgreen" style="text-align:left;">
				Target
			</td>
		</tr>

		<tr id="4">
			<td  class="blue"  style="border-top-width: 1px;border-bottom-color:#4592e4;">
				Anemia and Fluid Management
			</td>
			<td  id="hbRow" class="${hbSeverity!"green"}">
				Hb = ${hb!"NOT DONE"}
				<@getMarkerStars  severity=hbSeverity />
			</td>
			<td id="weightRow" class="${weightSeverity!"green"}">
				Weight Gain = ${weight!"NOT DONE"}
				<@getMarkerStars  severity=weightSeverity />
			</td>
			<td id="bpRow" class="${bpSeverity!"green"}">
				Post Blood Pressure = ${bp!"NOT DONE"}
				<@getMarkerStars  severity=bpSeverity />
			</td>
			<td class="blue">
			</td>
		</tr>

		<tr id="5">
			<td  class="blue" style="border-top-color:#4592e4;">
			</td>
			<td class="darkgreen">
				${hbRange!"11 F, 12 M"}
			</td>

			<td class="darkgreen">
				${weightRange!"< 5% Body Weight"}
			</td>
			<td class="darkgreen">
				${bpRange!" "}
			</td>
			<td class="darkgreen" style="text-align:left;">
				Target
			</td>
		</tr>

		<tr id="6">
			<td class="blue" style="border-top-width: 1px;border-bottom-color:#4592e4;">
 				Nutrition
			</td>
			<td id="albuminRow" class="${albuminSeverity!"green"}">
				 Albumin = ${albumin!"NOT DONE"}
				 <@getMarkerStars  severity=albuminSeverity />
			</td>

			<td id="proteinRow" class="${proteinSeverity!"green"}">
				 Total Protein = ${protein!"NOT DONE"}
				 <@getMarkerStars  severity=proteinSeverity />
			</td>
			<td id="potassiumRow" class="${potassiumSeverity!"green"}">
				Potassium = ${potassium!"NOT DONE"}
				<@getMarkerStars  severity=potassiumSeverity />
			</td>
			<td class="blue">
			</td>
		</tr>

		<tr id="7">
			<td  class="blue" style="border-top-color:#4592e4;">


			</td>
			<td class="darkgreen">
				${albuminRange!"> 4.0g/dl"}
			</td>

			<td class="darkgreen">
				${proteinRange!"> 6.0"}
			</td>

			<td class="darkgreen">
				${potassiumRange!"3.5-5.5 mEg/L"}
			</td>

			<td class="darkgreen" style="text-align:left;">
				Target
			</td>
		</tr>
		<tr id="8">
			<td   class="blue" style="border-top-width: 1px;border-bottom-color:#4592e4;">
				Bone Management
			</td>

			<td id="caxpoRow" class="${caxpoSeverity!"green"}">
				Caxpo4 = ${caxpo!"NOT DONE"}
				<@getMarkerStars  severity=caxpoSeverity />
			</td>

			<td id="pthRow" class="${pthSeverity!"green"}">
				pTh = ${pth!"NOT DONE"}
				<@getMarkerStars  severity=pthSeverity />
			</td>
			<td id="caRow" class="${caSeverity!"green"}">
				Corrected ca+ = ${ca!"NOT DONE"}
				<@getMarkerStars  severity=caSeverity />
			</td>

			<td class="blue">
			</td>
		</tr>

		<tr id="9" >
			<td  class="blue" style="border-top-color:#4592e4;">

			</td>

			<td  class="darkgreen">
				${caxpoRange!"< 55mg2/dL2"}
			</td>

			<td  class="darkgreen">
				${pthRange!"150-300 pg/ml"}
			</td>

			<td  class="darkgreen">
				${caRange!"8.8 - 9.5 mg/dl"}
			</td>

			<td  class="darkgreen" style="text-align:left;">
				Target
			</td>
		</tr>
		<tr>
			<td colspan="1" class="lightblue" style="text-align:right;border-right-color:#bedce5;">
			Nephrologist:
			</td>
			<td  class="lightblue" style="text-align:left;border-right-color:#bedce5;">
				${nephrologist!}
			</td>
			<td colspan="1"  class="lightblue" style="text-align:right;border-right-color:#bedce5;">
				Patient/Attender:
			</td>
			<td class="lightblue" colspan="2">
			</td>
		</tr>
	</table>
	<br/>
	<br/>
	<table class="resultTable" style="width:100%;overflow: hidden;font-family: Arial, sans-serif;
			font-size: 10px;">
		<tr>
			<td width="20%" class="lightblue">
			</td>
			<td class="lightblue" style="text-align:center;">
				Description of what the value indicates
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Intradialytic Weight Gain
			</td>
			<td class="lightblue">
			Excessive inter dialytic weight gain is usually related to overload of sodium in water, and is the most important
			factor for arterial hypertension in dialysis.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			 <b>NUTRITION</b>
			</td>
			<td class="lightblue">
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Albumin
			</td>
			<td class="lightblue">
				Albumin is a protein that helps fluid balance inside the blood vessels-Nutritional Marker
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Total Protein
			</td>
			<td class="lightblue">
			Nutritional Marker
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Potassium
			</td>
			<td class="lightblue">
			 K+ is an electrolyte that plays a  vital  major role in nerve and muscle function. High and low levels
			 can cause heart problems, heart failure and death.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			 <b>ADEQUACY </b>
			</td>
			<td class="lightblue">
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			URR
			</td>
			<td class="lightblue">
				Gives an idea about clearance of urea.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			kt/v
			</td>
			<td class="lightblue">
			 A measure of dialysis adequacy using a logarithm formula.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			 <b>BONE HEALTH </b>
			</td>
			<td class="lightblue">
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Corrected Ca+
			</td>
			<td class="lightblue">
			An estimate of what calcium level would be if the albumin were within normal ranges.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
				CaXpo4
			</td>
			<td class="lightblue">
				If Ca X P product is high, the patient is at the risk
				for calcium deposits on the blood vessels and soft tissues.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
				PTH
			</td>
			<td class="lightblue">
				Controls Calcium and Phosphorus in the Blood.
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
				<b>ANEMIA</b>
			</td>
			<td class="lightblue">
			</td>
		</tr>

		<tr>
			<td width="20%" class="lightblue">
			Hb
			</td>
			<td class="lightblue">
			Hb is the oxygen carrying protein pigment in blood,
			specifically in the red blood cells.
			</td>
		</tr>
	</table>

	<table style="font-family: Arial, sans-serif;
			font-size: 10px;">
		<tr>
			<td width="60%" style="background: #ff0000;">**</td>
			<td>Higher or lower levels</td>
		</tr>
		<tr>
			<td width="60%" style="background: #ffff00;">*</td>
			<td>Borderline levels</td>
		</tr>
		<tr>
			<td width="60%" style="background: #c3f94a;"></td>
			<td>Achieved</td>
		</tr>
		<tr>
			<td width="60%" style="background: #00b456;"></td>
			<td>Target Values</td>
		</tr>
	</table>
	<div style="padding-top:5px;h-align:center;text-align:center;font-family: Arial, sans-serif;font-size: 10px;margin: 0 auto;">
	<i>IMPROVING THE QUALITY AND LONGEVITY OF LIFE FOR EVERY PATIENT</i></div>

</body>
</#escape>

</html>

