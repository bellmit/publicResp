<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>Bed Util Trend : Insta HMS</title>
	<style type="text/css">


	body {
		font-family: Arial, sans-serif;
		font-size: 4pt;
	}


</style>
</head>
<#setting number_format="##.##">
<#setting date_format="yy">
<#assign months=["01","02","03","04","05","06","07","08","09","10","11","12"]>
<#assign days = [31,28,31,30,31,30,31,31,30,31,30,31]>
<#assign monthNames={"01":"Jan","02":"Feb","03":"Mar","04":"Apr","05":"May","06":"Jun","07":"July","08":"Aug","09":"Sep","10":"Oct","11":"Nov","12":"Dec"} >
<#assign monthCount=0>
<body  style=" font: 8px;">
	<div align="center" style="font-size: 12pt;"><b>Bed Utilization Trend Report</b></div>
	<table style="border-collapse: collapse;border: 1px solid;" >
		<tr>
			<td style="border: 1px solid;"><b>Ward Name/Bed Type</b></td>
			<td style="border: 1px solid;"><b>No Of Beds</b></td>
			<td style="border: 1px solid;"><b>Particulars/Description</b></td>
			<#assign year = startYear>
			<#list monthsList as selectedMonths>
				<#list months as monthNumber>
					<#if monthNumber == selectedMonths.monthNumber!"">
						<td style="border: 1px solid;"><b>${year} /    ${selectedMonths.monthName}</b></td>
					</#if>
				</#list>
				<#assign monthnum = selectedMonths.monthNumber!"">
				<#if monthnum == "12">
					<#assign year = year + 1>
				</#if>
			</#list>
		</tr>

	<#assign totalRev = 0>
	<#assign totalBeds = 0>
	<#list bedUtilList as bed>
		<tr>
			<td style="border: 1px solid;"><b>${bed.WARD}</b></td>

			<#list noOfBeds as bedsInWard>
				<#if ("${bedsInWard.BED_TYPE}" == "${bed.BED_TYPE}") && ("${bedsInWard.WARD_NO}" == "${bed.WARD_NO}")>
					<td  style="border: 1px solid;" align="center">${bedsInWard.NOOFBEDS}</td>
				</#if>
			</#list>

			<td style="border: 1px solid;"><b>No. of patients</b></td>

			<#assign year = startYear>
			<#list monthsList as selectedMonths>
				<#list months as jspMonthlist>
					<#if jspMonthlist == selectedMonths.monthNumber!"" >
						<#assign coun = "COUNT${selectedMonths.monthNumber}${year}">
						<td style="border: 1px solid;" align="right">${bed[coun]!""}</td>
					</#if>
				</#list>
				<#assign monthnum = selectedMonths.monthNumber!"">
					<#if monthnum == "12">
						<#assign year = year + 1>
					</#if>
			</#list>

		</tr>
		<tr>

			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Bill Days</b></td>

			<#assign year = startYear>
			<#list monthsList as selectedMonths>
				<#list months as jspMonthlist>
					<#if jspMonthlist == selectedMonths.monthNumber!"">
						<#assign dcon = "DAYS${selectedMonths.monthNumber}${year}">
						<td style="border: 1px solid;" align="right">${bed[dcon]!""}</td>
					</#if>
				</#list>
				<#assign monthnum = selectedMonths.monthNumber!"">
				<#if monthnum == "12">
					<#assign year = year + 1>
				</#if>
			</#list>

		</tr>

		<tr>
			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Bill Revenue</b></td>

			<#assign year = startYear>
			<#list monthsList as selectedMonths>
				<#list  months as jspMonthlist>
					<#if jspMonthlist == selectedMonths.monthNumber!"">
						<#assign amt = "AMOUNT${selectedMonths.monthNumber}${year}">
						<#assign totalRev = totalRev + bed[amt]!0?number>
						<td style="border: 1px solid;" align="right">${bed[amt]!""}</td>
					</#if>
				</#list>
				<#assign monthnum = selectedMonths.monthNumber!"">
				<#if monthnum == "12">
					<#assign year = year + 1>
				</#if>
			</#list>

		</tr>
		<tr>

			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Occupancy%</b></td>

			<#assign avgOccupancy = 0>
			<#assign year = startYear>

			<#list monthsList as selectedMonths>
				<#list months as monthNumber>
					<#if  monthNumber == selectedMonths.monthNumber!"">
						<#assign billedDays = "DAYS"+selectedMonths.monthNumber+year>
						<#list months as month>
							<#if selectedMonths.monthNumber == month>
								<#assign index = month_index>
								<#list days as totalDaysInMonth>
									<#if index == totalDaysInMonth_index>
										<#assign totalDays = totalDaysInMonth?number>
										<#list noOfBeds as bedsInWard>
											<#if bedsInWard.BED_TYPE == bed.BED_TYPE && bedsInWard.WARD_NO == bed.WARD_NO>
												<#assign totalNumOfBedsInWard = bedsInWard.NOOFBEDS?number>
												<#assign billDays = bed[billedDays]!0?number>
												<#assign avgOccupancy = avgOccupancy + (billDays * 100)/(totalNumOfBedsInWard * totalDays)>
													<td style="border: 1px solid;" align="right">${(billDays * 100) / (totalNumOfBedsInWard * totalDaysInMonth)}</td>
											</#if>
										</#list>
									</#if>
								</#list>
							</#if>
						</#list>
					</#if>
				</#list>
				<#assign monthnum = selectedMonths.monthNumber!"">
				<#if monthnum == "12">
					<#assign year = year + 1>
				</#if>
			</#list>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b> Monthly Average Revenue/Bed</b></td>

				<#assign year = startYear>
				<#assign total = 0>
				<#assign avgRevenue= 0>

				<#list monthsList as selectedMonths>
					<#list months as monthNumber>
						<#if monthNumber == selectedMonths.monthNumber!"">
							<#assign amount = "AMOUNT${selectedMonths.monthNumber}${year}">
							<#assign monthAmt = bed[amount]!0?number>
							<#assign total = total+monthAmt>
						</#if>
					</#list>

					<#assign monthnum = selectedMonths.monthNumber!"">

					<#if monthnum == "12">
						<#assign year = year + 1>
					</#if>

				</#list>

					<#assign avgRevenue = total/monCount>

				<#list noOfBeds as bedsInWard>
					<#if bedsInWard.BED_TYPE == bed.BED_TYPE && bedsInWard.WARD_NO == bed.WARD_NO>
						<#assign totalNumOfBedsInWard = bedsInWard.NOOFBEDS?number>
						<#assign totalBeds = totalBeds + totalNumOfBedsInWard>
						<#assign billDays = bed[billedDays]!0?number>
							<td style="border: 1px solid;" colspan="${monCount}" align="right">${(avgRevenue) / (totalNumOfBedsInWard)}</td>
					</#if>
				</#list>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Monthly Occupancy</b></td>
			<td style="border: 1px solid;" colspan="${monCount}" align="right">${avgOccupancy/monCount}</td>
		</tr>
	</#list>

		<tr>
			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Total Revenue</b></td>
				<#assign year = startYear>
				<#list monthsList as selectedMonths>
				<#list months as monthNumber>
						<#assign total = 0>
						<#if monthNumber == selectedMonths.monthNumber!"">
							<#assign amount = "AMOUNT"+selectedMonths.monthNumber+year>
							<#assign amtTotal = 0>
							<#list bedUtilList as bedlist>
								<#assign amtTotal = amtTotal + bedlist[amount]!0?number>
							</#list>
						</#if>
						<#assign total = amtTotal!0>
				</#list>
					<td style="border: 1px solid;" align="right">${amtTotal!0}</td>
					<#assign monthnum = selectedMonths.monthNumber!"">
					<#if monthnum == "12">
						<#assign year = year + 1>
					</#if>
				</#list>

		</tr>
		<tr>
			<td></td>
			<td></td>
			<td style="border: 1px solid;"><b>Total Revenue/Bed</b></td>
			<#assign year = startYear>
			<#list monthsList as selectedMonths>
				<#list months as monthNumber>
						<#assign tot = 0>
						<#assign totbed = 0>
						<#if monthNumber == selectedMonths.monthNumber!"">
						<#assign amount = "AMOUNT"+selectedMonths.monthNumber+year>
						<#assign amtTotal = 0>
						<#assign totalBeds = 0>
							<#list bedUtilList as bedlist>
								<#list noOfBeds as no>
									<#if no.BED_TYPE == bedlist.BED_TYPE && no.WARD_NO == bedlist.WARD_NO>
										<#assign totalBeds = totalBeds + no.NOOFBEDS?number>
									</#if>
								</#list>
								<#assign amtTotal = amtTotal + bedlist[amount]!0?number>
							</#list>
						</#if>
						<#assign totbed = totalBeds>
						<#assign tot = amtTotal>
					</#list>
					<td style="border: 1px solid;" align="right">${amtTotal /totalBeds}</td>
					<#assign monthnum = selectedMonths.monthNumber!"">
					<#if monthnum == "12">
						<#assign year = year + 1>
					</#if>
				</#list>

		</tr>

	</table>
</body>
</html>