var oupPatientSetHrefs = function(params, id, enableList, toolbarKey, event, validateOnRClick) {
	if (empty(gToolbars[toolbarKey])) return ;
	
	var i=0;
	var toolbar = gToolbars[toolbarKey];
	for (var key in toolbar) {
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		var href = data.href;
		if (!empty(anchor)) {
			if (key === 'TeleConsult') {
					for (var paramname in params) {
						var paramvalue = params[paramname];
						if (paramname === 'teleconsult') {
							href = paramvalue;
						}
					}
			anchor.href = href;
			} else {
				for (var paramname in params) {
					var paramvalue = params[paramname];
					if (paramname !== 'teleconsult') {
						if (paramname.charAt(0) == '%') {
					// replace a component of the href
							href = href.replace(paramname, paramvalue);
						} else {
					// append as param=value
							href += "&" + paramname + "=" + encodeURIComponent(paramvalue);
							}
					}
				}
				anchor.href = cpath + "/" + href;	
			}
		
		var enable = true;
		if (enableList) {
			enableToolbarItem(key, enableList[i], toolbarKey);
			enable = enableList[i];
		} else {
			enableToolbarItem(key, enable, toolbarKey);
		}

		if (!empty(data.onclick) && enable) {
			setParams(anchor, params, id, toolbar, validateOnRClick);
		}
	}
		else {
			debug("No anchor for " + 'toolbarAction'+ toolbarKey + key + ":");
		}
		i++;
	}
}