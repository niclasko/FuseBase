# FastBase
Data Convergence Engine

FastBase is a data convergence server for JDBC-enabled data sources. 

FastBase offers the following capabilities:
* JDBC connections
* Javascript engine to run scripts server-side
* SQL parser
* Job Scheduler for registered scripts
* Fast data offloading to disk (CSV) from any JDBC data source
* Fast data ingestion via JDBC
* Data virtualization (via extension)
* Config done using WEB GUI
* Usage is via API

Quick start:
```Shell
$ java -jar fastbase.jar [-port=4444]
```
Then
* **Config** - Point your browser to [http://localhost:4444](http://localhost:4444) (user/pass: admin/peregrine)
* **API listing**: [http://localhost:4444/web/api/](http://localhost:4444/web/api/)

**API usage example (SQL parser)**
1. Login
2. Users tab
3. For a user click "Client Keys"
4. Click "Generate client key"
5. Copy "Authentication Link" and call it (next step)
    
    ```curl http://localhost:4444/clientkey/authenticate?clientKey=YOUR_GENERATED_CLIENT_KEY&outputType=JSON```
6. Copy sessionKey and use it in API call (next step)
    
    ```curl http://localhost:4444/sql/parse?sql=select%20*%20from%20dual%20a%20inner%20join%20dual%20b%20on%20(1%3D1)&fastbase_sk=YOUR_SESSION_KEY_FROM_PREVIOUS_STEP```
6. Output
    ```
    SQL Parse tree:

    - LITERAL (ATOM)
    -- SELECT (CLAUSE)
    --- MULTIPLY (BINARY_OPERATOR)
    -- FROM (CLAUSE)
    --- LITERAL (ATOM): dual
    --- LITERAL (ATOM): a
    --- INNER_JOIN (JOIN)
    --- LITERAL (ATOM): dual
    --- LITERAL (ATOM): b
    --- ON (JOIN_FILTER)
    --- BEGIN_PARANTHESES (PARANTHESES)
    --- NUMBER_ATOM (ATOM): 1.0
    --- EQUALS (BINARY_OPERATOR)
    --- NUMBER_ATOM (ATOM): 1.0
    --- END_PARANTHESES (PARANTHESES)

    SQL Parse tree as JSON:

    {"KEYWORD": "ROOT","children": [{"KEYWORD": "SELECT","TYPE": "CLAUSE","TOKEN": "select","children": [{"KEYWORD": "MULTIPLY","TYPE": "BINARY_OPERATOR","TOKEN": "*"}]},{"KEYWORD": "FROM","TYPE": "CLAUSE","TOKEN": "from","children": [{"KEYWORD": "LITERAL","TYPE": "ATOM","TOKEN": "dual"},{"KEYWORD": "LITERAL","TYPE": "ATOM","TOKEN": "a"},{"KEYWORD": "INNER_JOIN","TYPE": "JOIN","TOKEN": "inner join"},{"KEYWORD": "LITERAL","TYPE": "ATOM","TOKEN": "dual"},{"KEYWORD": "LITERAL","TYPE": "ATOM","TOKEN": "b"},{"KEYWORD": "ON","TYPE": "JOIN_FILTER","TOKEN": "on"},{"KEYWORD": "BEGIN_PARANTHESES","TYPE": "PARANTHESES","TOKEN": "("},{"KEYWORD": "NUMBER_ATOM","TYPE": "ATOM","TOKEN": "1"},{"KEYWORD": "EQUALS","TYPE": "BINARY_OPERATOR","TOKEN": "="},{"KEYWORD": "NUMBER_ATOM","TYPE": "ATOM","TOKEN": "1"},{"KEYWORD": "END_PARANTHESES","TYPE": "PARANTHESES","TOKEN": ")"}]}]}
    ```
