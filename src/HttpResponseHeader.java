/*
 * Copyright (c) 2018 "Niclas Kjäll-Ohlsson, Bjørnar Fjøren"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.HashMap;

public enum HttpResponseHeader {

    FILE_NOT_FOUND("FILE_NOT_FOUND", "HTTP/1.0 404 Not Found" + HttpResponse.NEW_LINE),
    SEE_OTHER("SEE_OTHER",
              "HTTP/1.1 303 See Other" + HttpResponse.NEW_LINE),
    PERMANENTLY_MOVED("PERMANENTLY_MOVED",
                      "HTTP/1.1 301 Permanently Moved" + HttpResponse.NEW_LINE),
    TEMPORARY_REDIRECT("TEMPORARY_REDIRECT",
                       "HTTP/1.1 307 Temporary Redirect" + HttpResponse.NEW_LINE),
    NO_CACHE_MUST_REVALIDATE("NO_CACHE_MUST_REVALIDATE",
                             "Cache-Control: max-age=0, no-cache, must-revalidate, proxy-revalidate, private" +
                                        HttpResponse.NEW_LINE + "Edge-Control: no-store" + HttpResponse.NEW_LINE),
    CONNECTION_CLOSE("CONNECTION_CLOSE",
                     "Connection: close" + HttpResponse.NEW_LINE),
    AGE_ZERO("AGE_ZERO",
             "Age: 0" + HttpResponse.NEW_LINE),
    DEFAULT_HEADER("DEFAULT_HEADER",
                   "HTTP/1.0 200 OK" + HttpResponse.NEW_LINE +
                    "Access-Control-Allow-Origin: *" + HttpResponse.NEW_LINE),
    JSON_HEADERS(
        "JSON_HEADERS",
        "Content-Type: application/json; charset=UTF-8" + HttpResponse.NEW_LINE +
        "Cache-Control: no-cache, no-store, must-revalidate" + HttpResponse.NEW_LINE +
        "Pragma: no-cache" + HttpResponse.NEW_LINE +
        "Expires: 0" + HttpResponse.NEW_LINE
    ),
    CSV_HEADERS(
        "CSV_HEADERS",
        "Content-Type: text/plain; charset=UTF-8" + HttpResponse.NEW_LINE +
        "Cache-Control: no-cache, no-store, must-revalidate" + HttpResponse.NEW_LINE +
        "Pragma: no-cache" + HttpResponse.NEW_LINE +
        "Expires: 0" + HttpResponse.NEW_LINE
    ),
    UNKNOWN("UNKNOWN", ""),
    NEW_LINE("NEW_LINE", HttpResponse.NEW_LINE);

    private static final HashMap<String, HttpResponseHeader> displayNameMap;

    static {

        displayNameMap = new HashMap<String, HttpResponseHeader>();

        for (HttpResponseHeader httpResponseHeader : HttpResponseHeader.values()) {
            displayNameMap.put(httpResponseHeader.displayName, httpResponseHeader);
        }
    }

    public static HttpResponseHeader findByDisplayName(String displayName) {

        HttpResponseHeader httpResponseHeader =
                displayNameMap.get(displayName);

        if(httpResponseHeader == null) {
            httpResponseHeader = HttpResponseHeader.UNKNOWN;
        }

        return httpResponseHeader;
    }

    private String displayName;
    private String headerData;

    HttpResponseHeader(String displayName, String headerData) {
        this.displayName = displayName;
        this.headerData = headerData;
    }

    public String displayName() {
        return this.displayName;
    }

    public String headerData() {
        return this.headerData;
    }

    public String toString() {
        return this.headerData;
    }

}