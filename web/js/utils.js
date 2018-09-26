var _idFactory = 0;

function e(id) {
	return document.getElementById(id);
}

function show(el) {
	el.style.display = 'inline';
}

function hide(el) {
	el.style.display = 'none';
}

function showBlock(el) {
	el.style.display = 'block';
}

function toggle(el) {
	if(el.style.display == undefined) {
		showBlock(el);
		return true;
	}
	
	if(el.style.display == 'block') {
		hide(el);
		return false;
	} else if(el.style.display == 'none') {
		showBlock(el);
		return true;
	}
}

function tabularJSONToHTML(json) {
	var html = '<table>';

	html += '<tr>';
	for(var col in json.data[0]) {
		html += '<td class="nobreak"><b>' + col + '</b></td>';
	}
	html += '</tr>';

	for(var i=0; i<json.data.length; i++) {
		html += '<tr>';
		for(var col in json.data[i]) {
			html += '<td class="nobreak">' + json.data[i][col] + '</td>';
		}
		html += '</tr>';
	}
	
	return html;
}

function jsonUnescape(s) {
	var r = s;
	
	r = r.replace('\"', '"');
	r = r.replace('\\n', '\n');
	r = r.replace('\\t', '\t');
	
	return r;
}

function htmlUnescape(s) {
	var r = s;
	
	r = r.replace(/&gt;/g, '>');
	r = r.replace(/&lt;/g, '<');
	
	return r;
}

function tableJSONToHTML(json) {
	var html = '<table>';

	html += '<tr>';
	for(var i in json.data[0]) {
		html += '<td class="nobreak"><b>' + json.data[0][i] + '</b></td>';
	}
	html += '</tr>';

	for(var i=1; i<json.data.length; i++) {
		html += '<tr>';
		for(var j in json.data[i]) {
			html += '<td class="nobreak">' + json.data[i][j] + '</td>';
		}
		html += '</tr>';
	}
	
	return html;
}

function buildSelect(json, displayValue, idValue, selectID, onchange, selectedValue) {
	var html = '<select id="' + selectID + '" onchange="' + onchange + '">';

	for(var i=0; i<json.data.length; i++) {
		html += '<option value="' + json.data[i][idValue]  +
			'"' + (json.data[i][idValue] == selectedValue ? ' selected' : '') +
			'>' + json.data[i][displayValue] + '</option>';
	}
	
	html += '</select>';
	
	return html;
}

function getSelectedValue(selectElementId) {
	var el = e(selectElementId);
	if(el == null) {
		return null;
	}
	if(el.options.length == 0) {
		return null;
	}
	return el.options[el.selectedIndex].value;
}

function setSelectedValue(selectElementId, index) {
	var el = e(selectElementId);
	for(var i=0; i<el.options.length; i++) {
		if(i == index) {
			el.options[i].selected = true;
		} else {
			el.options[i].selected = false;
		}
	}
}

function remove(e) {
	if(e) {
		e.parentNode.removeChild(e);
	}
}

function clickCoordinates(event) {
	return {
		x: event.pageX - window.scrollX,
		y: event.pageY - window.scrollY
	};
}

function createDialog(title, contents, x, y, width, height, cleanup) {
	var dialogId = 'dlg' + (_idFactory++);
	
	var dialog = document.createElement('div');
	var header = document.createElement('div');
	var container = document.createElement('div');
	var footer = document.createElement('div');
	
	var anchorButton = document.createElement('a');
	
	dialog.id = dialogId;
	dialog.className = 'dialog';
	
	dialog.style.position = 'absolute';
	dialog.style.width = width + 'px';
	dialog.style.height = height + 'px';
	dialog.style.left = x;
	dialog.style.top = y;
	
	header.appendChild(document.createTextNode(title));
	header.className = 'dialogHeader';
	
	container.className = 'dialogContent';
	container.innerHTML = contents;
	
	anchorButton.href = 'javascript:void(null)';
	anchorButton.appendChild(document.createTextNode('Close'));
	anchorButton.onclick = (
		function(dialogId, cleanup) {
			return function() {
				remove(e(dialogId));
				if(cleanup != undefined) {
					cleanup();
				}
			} 
		}
	)(dialogId, cleanup);
	
	footer.className = 'dialogFooter';
	
	footer.appendChild(anchorButton);
	
	dialog.appendChild(header);
	dialog.appendChild(container);
	dialog.appendChild(footer);
	
	document.body.appendChild(dialog);
}

function padding(depth, padString) {
	if(depth == 0) {
		return '';
	}
	
	var pad = '';
	
	for(var i=0; i<=depth; i++) {
		pad += padString;
	}
	
	return pad;
}

function performClick(elementId) {
	var el = e(elementId);
	if(el && document.createEvent) { // sanity check
		var evt = document.createEvent("MouseEvents");
		evt.initEvent("click", true, false);
		el.dispatchEvent(evt);
   }
}

function createProgressCounter(elementId, initialPercentage) {
	var element = document.createElement('div');
	
	element.id = elementId;
	element.className = 'circle';
	setProgressCounter(element, (initialPercentage != undefined ? initialPercentage : 0));
	
	return element;
}

function setProgressCounter(element, percentage) {
	var css = '';
	var degreeDelta = Math.round(percentage % 25 / 25 * 90);
	var percentageText = (percentage == 100.0 ? 100 : percentage);
	
	if(percentage >= 0 && percentage < 25) {
		deg = 90 + degreeDelta;
		css =
			'linear-gradient(' + deg + 'deg, transparent 50%, white 50%),' +
			'linear-gradient(90deg, white 50%, transparent 50%)';
	} else if(percentage >= 25 && percentage < 50) {
		deg = 180 + degreeDelta;
		css =
			'linear-gradient(' + deg + 'deg, transparent 50%, white 50%),' +
			'linear-gradient(90deg, white 50%, transparent 50%)';
	} else if(percentage == 50) {
		deg = 0;
		css = 'linear-gradient(90deg, white 50%, transparent 50%)';
	} else if(percentage > 50 && percentage < 75) {
		deg = 90 + degreeDelta;
		css =
			'linear-gradient(' + deg + 'deg, transparent 50%, lightgray 50%),' +
			'linear-gradient(90deg, white 50%, transparent 50%)';
	} else if(percentage >= 75 && percentage < 100) {
		deg = 180 + degreeDelta;;
		css =
			'linear-gradient(' + deg + 'deg, transparent 50%, lightgray 50%),' +
	        'linear-gradient(90deg, white 50%, transparent 50%)';
	} else if(percentage >= 100) {
		css = 'none';
	}
	
	element.innerHTML = percentageText;
	element.style.backgroundImage = css;
}

function getCookieHash() {
	
	var cookieList = document.cookie.split(";");
	var cookieParts, cookieKey, cookieValue;
	var cookieHash = {};
	
	if(cookieList.length == 0) {
		return;
	}
	
	for(var idx in cookieList) {
		
		cookieParts = cookieList[idx].split("=");
		
		if(cookieParts.length < 2) {
			continue;
		}
		
		cookieKey = cookieParts[0].trim();
		cookieValue = cookieParts[1].trim();
		
		cookieHash[cookieKey] = cookieValue;
		
	}
	
	return cookieHash;
	
}

function randomString(size) {
	
	var letters = 'abcdefghijklmnopqrstuvxyz'.split('');
	var rs = '';
	
	for(var i=0; i<size; i++) {
		rs += letters[Math.round(Math.random()*letters.length)];
	}
	
	return rs;
	
}