function listDirectory(path, depth, nodeId, targetTextBoxId, targetLabelId) {
	
	var directoryListingId = 'directory_' + nodeId;
	var directoryListing = e(directoryListingId);
	
	if(directoryListing.childNodes.length == 1) {
		directoryListing.removeChild(directoryListing.childNodes[0]);
		return;
	}
	
	directoryListing = directoryListing.appendChild(document.createElement('span'));
	
	var xhr = new XMLHttpRequest();

	xhr.open(
		"GET",
		"/files/directorylisting?path=" + encodeURIComponent(path),
		false
	);
	xhr.send(null);

	if(xhr.status === 200) {
		var json = JSON.parse(xhr.responseText);
		var el;
		var nextNodeId;
		
		for(var i=0; i<json.data.length; i++) {
			el = json.data[i];
			nextNodeId = nodeId + '' + i;
			
			directoryListing.appendChild(
				document.createTextNode(padding(depth, '\u00A0'))
			);
			
			var anchor = document.createElement('a');
			var nextDirectoryListing = document.createElement('div');
			
			anchor.href = 'javascript:void(null)';
			anchor.appendChild(document.createTextNode(el.fileName));
			
			if(el.isDirectory) {
				anchor.onclick = (
					function(fullPath, depth, nextNodeId, targetTextBoxId, targetLabelId) {
						return function() {
							listDirectory(fullPath, depth, nextNodeId, targetTextBoxId, targetLabelId);
						}
					}
				)(el.fullPath, (depth+1), nextNodeId, targetTextBoxId, targetLabelId);
				anchor.className = 'directory_dir';
			} else if(!el.isDirectory) {
				anchor.onclick = (
					function(targetTextBoxId, targetLabelId, val) {
						return function() {
							e(targetTextBoxId).value = val;
							e(targetLabelId).innerHTML = val;
						} 
					}
				)(targetTextBoxId, targetLabelId, el.fullPath);
				anchor.className = 'directory_file';
			}
			
			nextDirectoryListing.id = 'directory_' + nextNodeId;
			
			directoryListing.appendChild(anchor);
			directoryListing.appendChild(nextDirectoryListing);
		}
	}
}

function directoryListingDialog(event, targetTextBoxId, targetLabelId, cleanup) {
	
	var cc = clickCoordinates(event);
	
	createDialog(
		'Select server file',
		'<div id="directory_0" class="directory_listing"></div>',
		cc.x,
		cc.y,
		300,
		400,
		cleanup
	);
	listDirectory(
		'/',
		0,
		0,
		targetTextBoxId,
		targetLabelId
	);
}