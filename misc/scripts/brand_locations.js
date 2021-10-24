function print(v) {
    output.writeBytes(v);
}

function http_ok_header() {
    print(FuseBase.DEFAULT_HEADER);
}

function http_plaintext_header() {
    http_ok_header();
    print(FuseBase.CSV_HEADERS + FuseBase.NEW_LINE);
}

function http_json_header() {
    http_ok_header();
    print(FuseBase.JSON_HEADERS + FuseBase.NEW_LINE);
}

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

/*
Get bearer by authorising at
https://iam.datahub-prod.mestergruppen.cloud/swagger-ui/#/
*/
var requestProperties = {
    "accept": "application/json",
    "authorization": "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1FTXpORU14TmtZME1EWkJOVU16UlRFNVFUYzRNelkzT0VWQ056TTBNamt6TlRNelJUUkJSZyJ9.eyJodHRwczovL21lc3RlcmdydXBwZW4ubm8vYXV0aG9yaXphdGlvbiI6eyJhcHBzIjpbIm1vdGltYXRlIiwicHJpc2lubnNpa3QiLCJwdXJlc2VydmljZSIsIndvcmtwbGFjZSJdLCJicmFuZHMiOlsieGxieWdnIl0sImRhdGFvd25lcnMiOlsibm8teGxieWdnLW1nYiJdLCJsb2NhdGlvbnMiOlsibm8teGxieWdnLWtqZWRla29udG9yIl0sIm1nIjp0cnVlLCJ0eXBlIjoiZW1wbG95ZWUifSwiaXNzIjoiaHR0cHM6Ly9pZC5tZXN0ZXJncnVwcGVuLmNsb3VkLyIsInN1YiI6IndhYWR8M3l5dGhFSXZ4WjVhYUt4NktWUjQzWXU4WXdHUTVra1NHQkgyQVh3RV9oNCIsImF1ZCI6Im1nLmlhbSIsImlhdCI6MTYyMTI0NjE5OSwiZXhwIjoxNjIxMzMyNTk5LCJhenAiOiJETGZzZ1o3U3FBcVp4dTNQNmk3ZkxZVmhTQ2RyanNWaCJ9.WnZLd0XyTc00IRAbhnKBsi8BAh6sSMZvW3XVb3KpPxvFAan_ewXt0s_8XouimgPR0L7DM5ZXGmhF_z4bh5P5IjXRzQq2J0f3tWHsFJMLSuZyLjHvF0Kr-8qF8hjnIiPYJKyjTpmdTbbUQuaxxlD8I6Vz6zUXZ0dC9c8ghm4KXolYLEmkqWv2QGD8HCtdoJDc3fIjY4MmmOKM-4NY_I_qscsAQfm-kd_yx4psy4QeYaZf_GdDyoMY5p4MhwrEe4EcQMaEJRR4rrISRXIVxtvpBR-5A1wRmwD3VeIP89J5bOHeK612pTrrkxxVcVklDiw7mp4KqJPIcplsTrzFNNE1uw"
};

var brands =
    JSON.parse(FuseBase.getURL("https://iam.datahub-prod.mestergruppen.cloud/v1/brands/",
                requestProperties).responseBody());

var brand_locations = [];
for(var i=0; i<brands.length; i++) {
    brand_locations.push(
        JSON.parse(FuseBase.getURL(
            'https://iam.datahub-prod.mestergruppen.cloud/v1/brands/{brand}/locations'.replace(
                '{brand}', brands[i].key),
            requestProperties).responseBody())
    );
}

var cols = {};
function getColumns(d, cols) {
    for(var i=0; i<d.length; i++) {
        flattenCols(d[i], cols, '');
    }
}

for(var i=0; i<brand_locations.length; i++) {
    getColumns(brand_locations[i], cols);
}

function getData(d, cols, header) {
    var records = [];
    for(var i=0; i<d.length; i++) {
        var r = row(cols);
        flattenEntry(d[i], cols, '', r);
        records.push(r);
    }

    var o="";
    var i=0;
    if(header) {
        for(var c in cols) {
            o += (i++ > 0 ? ";" : "") + c;
        }
        o += "\n";
    }
    for(var j=0; j<records.length; j++) {
        i=0;
        for(var c in cols) {
            o += (i++ > 0 ? ";" : "") + escape(records[j][c], ';');
        }
        o += "\n";
    }
    return o;
}

var _output = "";

for(var i=0; i<brand_locations.length; i++) {
    _output += getData(brand_locations[i], cols, (i==0));
}

http_plaintext_header();
print(_output);