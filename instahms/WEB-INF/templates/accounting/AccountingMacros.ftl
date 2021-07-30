[#function pm_mode_spl_account record spl_acc_name]
	[#assign cols = spl_acc_name?matches("\\$\\{(.*)\\}")]

	[#assign match = ""]
	[#assign acc_name = spl_acc_name]
	[#list cols as match]
		[#assign col = match?replace("\\$\\{", "", "r")] <!-- regexp replacement -->
		[#assign col = col?replace("\\}", "", "r")] <!-- regexp replacement -->
		[#assign val = "${record[col]!}"]
		[#assign acc_name = acc_name?replace(match, val)]
	[/#list]
	[#return acc_name]
[/#function]
