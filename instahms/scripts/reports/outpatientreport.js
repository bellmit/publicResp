
function disableFilter() {
	var selectedFilter = document.getElementById("filterBy").value;
	var filters = ["dept_name","doctor", "complaint","dummy"];
	for (var i=0; i<filters.length; i++) {
	if (selectedFilter == filters[i]){
		document.getElementById(filters[i]).style.display = 'block';		
	}else{
		document.getElementById(filters[i]).style.display = 'none';

	}
	}
}

function getFilterValue(filter){
	var filterBy = document.getElementById("filterBy").value;
	var doc = document.getElementById("doctor");
	var dep = document.getElementById("dept_name");
	var comp = document.getElementById("complaint");
	if (filterBy == 'doctor'){
		dep.value = "";
		comp.value="";
	}else if (filterBy == 'dept_name'){
		doc.value = "";
		comp.value="";
	}else if (filterBy == 'complaint'){
		doc.value = "";
		dep.value = "";
	}else {
	}

}
