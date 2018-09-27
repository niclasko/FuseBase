var fuseBaseTables;

function listFuseBaseTables(container) {
	container.innerHTML = '';
	
	var xhr = new XMLHttpRequest();

	xhr.open(
		"GET",
		"/fusebase/tables",
		false
	);
	xhr.send(null);

	if(xhr.status === 200) {
		var json = JSON.parse(xhr.responseText);
		
		fuseBaseTables = json.data;
		
		var table, column;
		
		var tableAnchor,
			tableInfo,
			columnAnchor,
			columnList;
			
		var tableColumnListId, columnId;
		
		for(var i=0; i<fuseBaseTables.length; i++) {
			
			table = fuseBaseTables[i];
			
			tableColumnListId = table.tableName + '_columnList';
			
			tableAnchor = document.createElement('a');
			
			tableAnchor.href = 'javascript:void(null)';
			tableAnchor.appendChild(document.createTextNode(table.tableName));
			tableAnchor.className = 'fusebase_table_link';
			tableAnchor.onclick = (
				function(id, table) {
					return function() {
						toggle(e(id));
					}
				}
			)(tableColumnListId, table);
			
			container.appendChild(
				document.createTextNode(padding(1, '\u00A0'))
			);
			container.appendChild(tableAnchor);
			
			container.appendChild(document.createElement('br'));
			
			columnList = document.createElement('div');
			
			columnList.id = tableColumnListId;
			
			hide(columnList);
			
			for(var j=0; j<table.columns.length; j++) {
				column = table.columns[j];
				
				columnId = table.tableName + '_' + column.columnName;
				
				columnAnchor = document.createElement('a');
				
				columnAnchor.href = 'javascript:void(null)';
				columnAnchor.appendChild(document.createTextNode(column.columnName));
				columnAnchor.className = 'fusebase_table_link';
				columnAnchor.onclick = (
					function(id, table, column) {
						return function() {
							;
						}
					}
				)(columnId, table, column);
				
				columnList.appendChild(
					document.createTextNode(padding(3, '\u00A0'))
				);
				
				columnList.appendChild(columnAnchor);
				columnList.appendChild(document.createElement('br'));
				
			}
			
			container.appendChild(columnList);
			
		}
	}
}

function fuseBaseQuery(sql, outputType, targetElement) {
	var xhr = new XMLHttpRequest();
	
	var link = "/fusebase/query?query=" + encodeURIComponent(sql) + "&outputType=" + outputType;
	
	xhr.open(
		"GET",
		link,
		false
	);
	xhr.send(null);

	if(xhr.status === 200) {
		
		var resultsElement;
		
		if(outputType == 'JSON') {
			var json = JSON.parse(xhr.responseText);
			
			resultsElement = document.createElement('span');
			resultsElement.innerHTML = tabularJSONToHTML(json);
			
		} else if(outputType == 'CSV') {
			resultsElement = document.createElement('pre');
			resultsElement.innerHTML = xhr.responseText;
		}
		
		if(targetElement.childNodes != undefined) {
			if(targetElement.childNodes.length == 1) {
				targetElement.removeChild(
					targetElement.childNodes[0]
				);
			}
		}
		
		targetElement.appendChild(resultsElement);
	}
	
	return link;
}

function createAddTableDialog(event, cleanup) {
	
	var cc = clickCoordinates(event);
	
	createDialog(
		'New table',
		'<iframe src="fusebaseupload/" class="iFrame" style="height: 92%;"></iframe>',
		cc.x,
		cc.y,
		840,
		510,
		cleanup
	);
}