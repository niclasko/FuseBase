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