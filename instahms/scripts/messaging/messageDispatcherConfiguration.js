function initPage() {
	onProtocolChange();
}

function onProtocolChange() {
	var protocol = document.getElementById("protocol").value;
	var host_name = document.getElementById("host_name");
	var port_no = document.getElementById("port_number");
	var auth_required = document.getElementById("auth_required");
	var use_tls = document.getElementById("use_tls");
	var use_ssl = document.getElementById("use_ssl");
	var attachment = document.getElementById("attachment");
	var max_attachment_kb = document.getElementById("max_attachment_kb");
	var custom_param_1 = document.getElementById("custom_param_1");
	var custom_param_2 = document.getElementById("custom_param_2");
	var country_code_prefix = document.getElementById("country_code_prefix");
	var userName = document.getElementById("username");
	var password = document.getElementById("password");
	var templateType = document.getElementById("templateType");
	var httpMethod = document.getElementById("httpMethod");
	var url = document.getElementById("url");
	var httpheader = document.getElementById("httpheader");
	var httpbody = document.getElementById("httpbody");
	if (protocol != "http") {
		host_name.removeAttribute("disabled");
		port_no.removeAttribute("disabled");
		auth_required.removeAttribute("disabled");
		use_tls.removeAttribute("disabled");
		use_ssl.removeAttribute("disabled");
		attachment.removeAttribute("disabled");
		max_attachment_kb.removeAttribute("disabled");
		custom_param_1.removeAttribute("disabled");
		custom_param_2.removeAttribute("disabled");
		country_code_prefix.removeAttribute("disabled");
		templateType.disabled = "true";
		httpMethod.disabled = "true";
		url.disabled = "true";
		httpheader.disabled = "true";
		httpheader.visibility = "hidden";
		httpsuccessresponse.disabled = "true";
		httpsuccessresponse.visibility = "hidden";
		httpbody.disabled = "true";
		httpbody.visibility = "hidden";
		userName.removeAttribute("disabled");
		password.removeAttribute("disabled");
	} else {
		host_name.disabled = "true";
		port_no.disabled = "true";
		auth_required.disabled = "true";
		use_tls.disabled = "true";
		use_ssl.disabled = "true";
		attachment.disabled = "true";
		max_attachment_kb.disabled = "true";
		custom_param_1.disabled = "true";
		custom_param_2.disabled = "true";
		country_code_prefix.disabled = "true";
		templateType.removeAttribute("disabled");
		httpMethod.removeAttribute("disabled");
		url.removeAttribute("disabled");
		httpheader.removeAttribute("disabled");
		httpheader.removeAttribute("visibility");
		httpsuccessresponse.removeAttribute("disabled");
		httpsuccessresponse.removeAttribute("visibility");
		httpbody.removeAttribute("disabled");
		httpbody.removeAttribute("visibility");
		userName.disabled = "true";
		password.disabled = "true";
	}
}