importScripts('fieldTypes.js');

var browserupload = new BrowserUpload();

var LF = '\n',
	CR = '\r',
	CRLF = '\r\n';
	
var _LF = 0,
	_CR = 1,
	_CRLF = 2;

onmessage = function(event) {
	if(event.data.action == 'sampleFile') {
		postMessage({
			'fileSampleInfo': browserupload.sampleFile(event.data.file),
			'reply': event.data.action,
			'file': event.data.file
		});
	} else if(event.data.action == 'parseRows') {
		postMessage({
			'table': browserupload.parseRows(event.data.rowData, event.data.lineSeparator, event.data.fieldSeparator),
			'reply': event.data.action
		});
	} else if(event.data.action == 'uploadDataForDbTable') {
		
		var cols = event.data.columns;
		
		for(var i=0; i<cols.length; i++) {
			cols[i].type.p = fieldTypes[cols[i].type.type].p;
		}
		
		browserupload.uploadDataForDbTable(event.data.file, cols, event.data.targetTableName);
	} else if(event.data.action == 'uploadFileData') {
		browserupload.uploadFileData(
			event.data.file,
			event.data.parameters
		);
	}
}

function BrowserUpload() {
	
	this.lineSamples = 200;
	this.lastCharacterOfLineIsFieldSeparator = false;
	this.fieldSeparatorSuggestion;
	this.fieldSeparator;
	this.lineSeparator;
	this._lineSeparator;
	
	this._byteChunk = 600000;
	
	this.sampleFile = function(_file) {
		
		var file = _file;
		var byteChunk = this._byteChunk;
		var start = 0, stop = (byteChunk < file.size ? byteChunk : file.size);
		var blob;
		var reader = new FileReaderSync();
		var dataChunk;
		var charIdx;
		var c, pc, lastCharacterOfHeaderLine;
		var lineCount = 0;
		var fileSample = '';
		var lineSeparator, _lineSeparator, lineSeparatorSet = false;
		
		var probableFieldSeparators = {',': 0, ';': 0, '|': 0, '\t': 0};
		var characterCountFirstLine = {};
		
		// Fetch file header and count records
		try {
			while(stop <= file.size && lineCount < this.lineSamples) {
				
				if(file.isNetworkFile) {
					blob = file.fileData.substring(start, stop + 1);
					dataChunk = blob;
				} else if(file.slice == undefined) { // Safari
					blob = file.webkitSlice(start, stop + 1);
					dataChunk = reader.readAsText(blob);
				} else if(file.slice != undefined) { // IE10, Firefox, Chrome
					blob = file.slice(start, stop + 1);
					dataChunk = reader.readAsText(blob);
				}
				
				for(charIdx = 0; charIdx<dataChunk.length; charIdx++) {
					pc = c;
					c = dataChunk.charAt(charIdx);
					
					if(lineCount == 0) {
						if(!lineSeparatorSet) {
							if(c == LF && pc != CR) {
								lineSeparator = LF;
								_lineSeparator = _LF;
								lineSeparatorSet = true;
								
								lastCharacterOfHeaderLine = pc;
								
							} else if(c == CR) {
								lineSeparator = CR;
								_lineSeparator = _CR;
								lineSeparatorSet = true;

								if(charIdx<dataChunk.length-1 && dataChunk.charAt(charIdx+1) == LF) {
									lineSeparator = CRLF;
									_lineSeparator = _CRLF;
								}
								
								lastCharacterOfHeaderLine = pc;
								
							}
						}
						
						if(probableFieldSeparators[c] != undefined) {
							probableFieldSeparators[c]++;
						}
						
						if(characterCountFirstLine[c] == undefined) {
							characterCountFirstLine[c] = 1;
						} else {
							characterCountFirstLine[c]++;
						}
						
					}
					
					if(lineSeparatorSet) {
						if(_lineSeparator == _LF && c == LF) {
							lineCount++;
						} else if(_lineSeparator == _CR && c == CR) {
							lineCount++;
						} else if(_lineSeparator == _CRLF && c == LF && pc == CR) {
							lineCount++;
						}
					}
					
					if(lineCount == this.lineSamples) {
						break;
					}
					
					fileSample += c;
					
				}
				
				if(lineCount == this.lineSamples) {
					break;
				}
				
				start = stop + 1;
				stop += (stop < file.size ? byteChunk : file.size);
			}
		} catch(err) {
			postMessage({"reply": "feedback", "message": err.message});
		}
		this.fileSample = fileSample;
		this.lineSeparator = lineSeparator;
		this._lineSeparator = _lineSeparator;
		
		
		var mostOccuringCharacter = {'character': null, 'occurences': 0};
		
		for(var c in probableFieldSeparators) {
			if(probableFieldSeparators[c] > mostOccuringCharacter['occurences']) {
				mostOccuringCharacter['character'] = c;
				mostOccuringCharacter['occurences'] = probableFieldSeparators[c];
			}
		}
		
		if(mostOccuringCharacter['occurences'] == 0) {
			mostOccuringCharacter = {'character': null, 'occurences': 0};
			
			for(var c in characterCountFirstLine) {
				if(characterCountFirstLine[c] > mostOccuringCharacter['occurences']) {
					mostOccuringCharacter['character'] = c;
					mostOccuringCharacter['occurences'] = characterCountFirstLine[c];
				}
			}	
		}
		
		this.fieldSeparatorSuggestion = mostOccuringCharacter['character'];
		
		if(lastCharacterOfHeaderLine == this.fieldSeparatorSuggestion) {
			this.lastCharacterOfLineIsFieldSeparator = true;
		}
		
		return {
			'fileSample': fileSample,
			'lineSeparator': lineSeparator,
			'_lineSeparator': lineSeparator,
			'fieldSeparatorSuggestion': mostOccuringCharacter['character']
		};
	};
	
	this.parseRows = function(rowData, lineSeparator, fieldSeparator) {
		
		this.fieldSeparator = fieldSeparator;
		
		var rows = rowData.split(lineSeparator);
		var table = new Array();
		var tableRow;
		
		var c = '', pc = '';
		
		var field = '';
		var row;
		
		var inQuote = false;
		
		for(var i=0; i<rows.length; i++) {
			row = rows[i];
			tableRow = new Array();
			
			for(var j=0; j<row.length; j++) {
				
				pc = c;
				c = row.charAt(j);
				if(j == (row.length-1) && !inQuote && c != fieldSeparator) {
					field += c;
				}
				
				if(j == (row.length-1) && inQuote && c == '"') {
					c = '';
					inQuote = false;
				}
				
				if(c == '"' && pc != '\\' && !inQuote) {
					inQuote = true;
				} else if(c == '"' && pc != '\\' && inQuote) {
					inQuote = false;
				} else if((c == fieldSeparator && !inQuote) || j == (row.length-1)) {
					tableRow.push(field);
					field = '';
				} else {
					field += c;
				}
				
			}
			
			table.push(tableRow);
			
		}
		
		return table;
	};
	
	this.runSQL = function(sql) {
		
		var xhr = new XMLHttpRequest();
		xhr.open("GET", '/?query=' + encodeURIComponent(sql) + '&queryType=DML', false);
		xhr.send(null);
		
		if(xhr.status === 200) {
			xhr.responseText;
		}
	};
	
	this.writeToFile = function(fileName, fileData) {
		var xhr = new XMLHttpRequest();
		
		xhr.open("POST","/" + fileName, false);
		xhr.setRequestHeader("Connection","close");

		xhr.send(fileData);

		if(xhr.status === 200) {
			postMessage({"reply": "debug", "message": xhr.responseText});
		}
		
	};
	
	this.writeToFuseBase = function(parameters, fileData, progress) {
		var xhr = new XMLHttpRequest();
		
		xhr.open("POST","/fusebase/loadfilefromclient" + parameters, false);
		xhr.setRequestHeader("Connection","close");

		xhr.send(fileData);

		if(xhr.status === 200) {
			
			var json = JSON.parse(xhr.responseText);
			
			postMessage({
				"reply": "fuseBaseLoadProgress",
				"loadProgress": progress,
				"recordCount":  json.data[0]['recordCount']
			});
		}
	}
	
	this.loadToDB = function(fileName, includedColumns, insertSQL, fieldSep) {
		var xhr = new XMLHttpRequest();
		
		xhr.open("GET","/loadtodb?filename=" +
			encodeURIComponent(fileName) + "&includedcolumns=" +
			includedColumns + "&insertsql=" +
			encodeURIComponent(insertSQL) +
			"&fieldsep=" + encodeURIComponent(fieldSep), 
			false);
		xhr.send(null);

		if(xhr.status === 200) {
			postMessage({"reply": "debug", "message": xhr.responseText});
		}
	};
	
	this.uploadDataForDbTable = function(file, columns, targetTableName) {			
		var sqlColumnList = '',
			sqlValueList = '',
			baseSql = 'INSERT INTO ' + targetTableName + ' (',
			backendInsertSQL = '',
			sql = '',
			includedColumns = '';
		
		for(var i=0; i<columns.length; i++) {
			if(columns[i].include) {
				
				if(includedFieldIdx > 0) {
					sqlColumnList += ',';
				}
				
				sqlColumnList += columns[i].name;
				
				includedColumns += '1';
				
				includedFieldIdx++;
			} else {
				includedColumns += '0';
			}
		}
		
		includedFieldIdx = 0;
		
		baseSql += sqlColumnList + ') VALUES(';
		
		backendInsertSQL = baseSql;
		
		for(var i=0; i<columns.length; i++) {
			if(columns[fieldIdx].include) {
				if(includedFieldIdx > 0) {
					backendInsertSQL += ',';
				}
				backendInsertSQL += columns[fieldIdx].type.p('$' + i);
				
				includedFieldIdx++;
			}
		}
		
		backendInsertSQL += ')';
		
		includedFieldIdx = 0;
		
		this.uploadFileData(file, '');
		
		this.loadToDB(file.name, includedColumns, backendInsertSQL, fieldSep);
	};
	
	this.uploadFileData = function(file, parameters) {
		
		var byteChunk = this._byteChunk;
		var start = 0, stop = (byteChunk < file.size ? byteChunk : file.size);
		var blob;
		var reader = new FileReaderSync();
		var dataChunk;
		
		try {
			while(1) {
				
				if(file.slice == undefined) { // Safari
					blob = file.webkitSlice(start, stop + 1);
					dataChunk = reader.readAsText(blob);
				} else if(file.slice != undefined) { // IE10, Firefox, Chrome
					blob = file.slice(start, stop + 1);
					dataChunk = reader.readAsText(blob);
				} else if(file.isNetworkFile) {
					blob = file.fileData.substring(start, stop + 1);
					dataChunk = blob;
				}
				
				this.writeToFuseBase(parameters, dataChunk, ((start/file.size)*100).toFixed(1));

				start = stop + 1;
				if(stop == file.size) {
					break;
				} else if(stop + byteChunk < file.size) {
					stop += byteChunk;
				} else if(stop + byteChunk >= file.size) {
					stop = file.size;
				}
			}
		} catch(err) {
			postMessage({"reply": "feedback", "message": 'Error: ' + err.message});
		}
		
		this.writeToFuseBase(parameters, '__CLOSEFILE__', ((start/file.size)*100).toFixed(1));
	}
}