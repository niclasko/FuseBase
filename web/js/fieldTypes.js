var fieldTypes = {
	'string': {
		'type': 'string',
		'lhs': '\'',
		'rhs': '\'',
		p: function(v) { return this.lhs + v + this.rhs; },
		'sqlType': 'VARCHAR(100)'
	},
	'number': {
		'type': 'number',
		'value': '',
		p: function(v) { return v; },
		'sqlType': 'NUMBER'
	},
	'date': {
		'type': 'date',
		'lhs': 'to_date(\'',
		'format': 'DD-MON-YYYY',
		'rhs': ')',
		p: function(v) { return this.lhs + v + '\', \'' + this.format + '\'' + this.rhs; },
		'sqlType': 'DATE'
	},
	'timestamp': {
		'type': 'timestamp',
		'lhs': 'to_timestamp(\'',
		'format': 'DD-MON-YYYY HH24:MI:SS',
		'rhs': ')',
		p: function(v) { return this.lhs + v + '\', \'' + this.format + '\'' + this.rhs; },
		'sqlType': 'TIMESTAMP'
	}
};