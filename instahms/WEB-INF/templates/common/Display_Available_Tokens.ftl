[#list .data_model?keys as k]
	[#if .data_model[k]?has_content]
		[#if (.data_model[k]?is_string) || .data_model[k]?is_boolean || .data_model[k]?is_number || .data_model[k]?is_date]
			[#if .data_model[k]?is_string && .data_model[k]?contains("BasicDynaBean@")]
				[@iterate_bean .data_model[k] k/]
			[#else]
				${k}
			[/#if]
		[#elseif .data_model[k]?is_sequence ]
			[@iterate_list .data_model[k] k/]
		[#elseif .data_model[k]?is_hash]
			[@iterate_hash .data_model[k] k/]
		[/#if]
	[/#if]
	<br/>
[/#list]

[#macro iterate_list l key]
	[#list l as item]
		[#if item?is_string || item?is_boolean || item?is_number || item?is_date]
			[#if item?is_string && item?contains("BasicDynaBean@")]
				[@iterate_bean item key/]
			[#else]
				${key}.${item}
			[/#if]

		[#elseif item?is_hash]
			[@iterate_hash item key/]
		[#elseif item?is_sequence]
			[@iterate_list item key/]
		[/#if]
		<br/>
	[/#list]
[/#macro]

[#macro iterate_hash h key]
	[#list h?keys as ik]
		[#if h[ik]!?is_string || h[ik]!?is_boolean || h[ik]!?is_number || h[ik]!?is_date]
			[#if  h[ik]!?is_string && h[ik]!?contains("BasicDynaBean@")]
				${key}.[@iterate_bean h[ik] key/]
			[#else]
				${key}.${ik}
			[/#if]
		[#elseif h[ik]!?is_hash]
			[@iterate_hash h[ik] key/]
		[#elseif h[ik]!?is_sequence]
			[@iterate_list h[ik] key/]
		[/#if]
		<br/>
	[/#list]
[/#macro]

[#macro iterate_bean h key]
	[#list h.map?keys as k]
		${key}.${k}<br/>
	[/#list]
[/#macro]