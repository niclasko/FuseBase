load("/Users/niclas/Documents/code/FuseBase/misc/libs/alasql.min.js");

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

function fix(s) {
    var r = s;
    if(r[0] == '"') {
        r = r.substring(1);
    }
    if(r[r.length-1] == '"') {
        r = r.substring(0,r.length-1);
    }
    return r;
}

params = {};
for(var i=0;i<parameters.length;i++) {
    params[parameters[i][0]] = parameters[i][1];
}

var fuseQuery = decodeURIComponent(params["q"]);

fuse_obj = JSON.parse(FuseBase.fuse(fuseQuery));

datasets = [];
var q = null;
var ds = null;
for(var i=0;i<fuse_obj.FuseBaseQueries.length; i++) {
    q = fuse_obj.FuseBaseQueries[i];
    ds = JSON.parse(FuseBase.query(
            fix(q.connection),
            fix(q.sql.replaceAll("''", "'"))
        )).data;
    datasets.push(ds);
}

var result = alasql(fuse_obj.sql, datasets);

http_json_header();

print(JSON.stringify(result));