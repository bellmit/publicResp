/**

This Script give Colors to DashBoard of different screens in HMS.

**/

	function getColors(ccode, rowid){

			//alert("Hello...");

			switch(ccode){

			case "a":
			rowid.bgColor = "#339966"
			break;

			case "b":
			rowid.bgColor = "#6495ED"
			break;

			case "c":
			rowid.bgColor = "#B0C4DE"
			break;

			case "d":
			rowid.bgColor = "#8FBC8F"
			break;

			case "e":
			rowid.bgColor = "#20B2AA"
			break;

			case "f":
			rowid.bgColor = "#FFA07A"
			break;

			case "g":
			rowid.bgColor = "#C9BE62"
			break;

			case "h":
			rowid.bgColor = "#DDDA9B"
			break;

			case "i":
			rowid.bgColor = "#AABDAO"
			break;

			case "j":
			rowid.bgColor = "#CFDAAF"
			break;

			case "k":
			rowid.bgColor = "#C0CFAF"
			break;

			case "l":
			rowid.bgColor = "#C5D9A3"
			break;

			default: rowid.bgColor = "ffffff";

			}

	}

	/*
	function getresults(){
		alert("Hello...");
	}
	*/
