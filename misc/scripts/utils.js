function flattenCols(e, cols, prefix) {
    var sep = (prefix !== '' ? '_' : '');
    for(var k in e) {
        if(e[k].constructor.name === 'Object') {
            flattenCols(e[k], cols, prefix + sep + k);
        } else if(e[k].constructor.name !== 'Array') {
            cols[prefix + sep + k] = typeof e[k];
        }
    }
}

function row(cols) {
    var r = {};
    for(var c in cols) {
        r[c] = "";
    }
    return r;
}

function flattenEntry(e, cols, prefix, r) {
    var sep = (prefix !== '' ? '_' : '');
    for(var k in e) {
        if(e[k].constructor.name === 'Object') {
            flattenEntry(e[k], cols, prefix + sep + k, r);
        } else if(e[k].constructor.name !== 'Array') {
            r[prefix + sep + k] = e[k];
        }
    }
}

function escape(v, sep) {
    if(!v.replace) {
        return v;
    }
    var ret = v.replace('\n', '');
    if(ret.indexOf(sep) > -1) {
        return '"' + ret.replace('"', '"""') + '"';
    }
    return ret;
}

/* Usage:

    d = [
        {name: "test", id: 0},
        {name: "test2", id: 1, type="other"}
    ];

    var cols = {};
    for(var i=0; i<d.length; i++) {
        flattenCols(d[i], cols, '');
    }
    var records = [];
    for(var i=0; i<d.length; i++) {
        var r = row(cols);
        flattenEntry(d[i], cols, '', r);
        records.push(r);
    }

    var o="";
    var i=0;
    for(var c in cols) {
        o += (i++ > 0 ? ";" : "") + c;
    }
    o += "\n";
    for(var j=0; j<records.length; j++) {
        i=0;
        for(var c in cols) {
            o += (i++ > 0 ? ";" : "") + escape(records[j][c], ';');
        }
        o += "\n";
    }
    print(o);

 */