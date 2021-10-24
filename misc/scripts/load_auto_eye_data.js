function print(v) {
    output.writeBytes(v + "\n");
}

var staging_table_name = "stg_auto_eye_log";

var table_exists_query = "SELECT count(1) as c FROM INFORMATION_SCHEMA.TABLES \
                            WHERE TABLE_SCHEMA = 'dbo' \
                            AND  TABLE_NAME = '{TABLE}'".replace("{TABLE}", staging_table_name);
var d = JSON.parse(FuseBase.query("pretre_sql_server", table_exists_query));

var id=0;

if(d.data[0].c == 0) { // table does not exist
    var create_staging_table_sql = "create table {TABLE} ( \
                                        ID BIGINT PRIMARY KEY,\
                                        LOGTIME datetime NOT NULL,\
                                        EVENT_CATEGORY varchar(100) NOT NULL,\
                                        EVENT_STATE varchar(100) NOT NULL,\
                                        EVENT_DESCRIPTION varchar(4000),\
                                        TEXT4 varchar(4000),\
                                        TEXT5 varchar(4000),\
                                        TEXT6 varchar(4000),\
                                        TEXT7 varchar(4000),\
                                        PREVIOUS_ID BIGINT\
                                    )".replace("{TABLE}", staging_table_name);
    FuseBase.ddl("pretre_sql_server", create_staging_table_sql);
    print("created table {TABLE}".replace("{TABLE}", staging_table_name));
} else if(d.data[0].c == 1) {
    var id_query = "select case when max_id is null then 0 else max_id end as max_id from \
                    (select max(id) as max_id from {TABLE}) a\n".replace("{TABLE}", staging_table_name);
    var r = JSON.parse(FuseBase.query("pretre_sql_server", id_query));
    id = r.data[0].max_id+1;
}

FuseBase.ddl("pretre_sql_server", "truncate table {TABLE}".replace("{TABLE}", staging_table_name));

var files =
    FuseBase.command("ls /Users/niclas/Documents/data/hundegger-datafetcher/data/autoeye/alldata/").split("\n");

var tables = [];

for(var i=0; i<files.length; i++) {
    if(files[i].indexOf("_event") > -1) {
        tables.push(files[i].replace(".log", ""));
    }
}

var state_transitions = {
    "WORKING_ON": "WORKING_OFF",
    "STARTED_ON": "STARTED_OFF",
    "IDLE_ON": "IDLE_OFF",
    "DOWN_ON": "DOWN_OFF",
    "JOB_STARTED": "JOB_DONE",
    "APP_STARTED": "APP_ENDED",
    "ALARM_ON": "ALARM_OFF"
};

var expected_states = {};

var totalRows = 0;
var ps = FuseBase.getPreparedStatement("pretre_sql_server",
        "insert into {TABLE} values(?, cast(? as datetime),?,?,?,?,?,?,?,?)".replace(
            "{TABLE}", staging_table_name));
ps.getConnection().setAutoCommit(false);
for(var i=0; i<tables.length; i++) {
    try {
        var rs = FuseBase.getQueryObject("auto_eye", "select * from " + tables[i]);

        var rows=0;
        var event_state, expected_state;
        while (rs.next()) {
            rows++;

            event_state = rs.getString(3);

            if(event_state.indexOf("ALARM_ON") == 0 && event_state.length > "ALARM_ON".length) {
                var alarm_number = event_state.match(/\d+/g)[0];
                expected_states["ALARM_OFF" + alarm_number] = {previous_id: id};
            }

            ps.setString(1, id);
            for(var j=1; j<=rs.getColumnCount(); j++) {
                ps.setString(j+1, rs.getString(j));
            }

            if(event_state in expected_states) {
                ps.setString(rs.getColumnCount()+2, expected_states[event_state].previous_id);
                delete expected_states[event_state];
            } else if(!(event_state in expected_states)) {
                ps.setString(rs.getColumnCount()+2, null);
            }

            if(event_state in state_transitions) {
                expected_state = state_transitions[event_state];
                if(!(expected_state in expected_states)) {
                    expected_states[expected_state] = {
                        previous_id: id
                    }
                }
            }

            id++;
            ps.addBatch();
        }
        ps.executeBatch();
        ps.getConnection().commit();

        totalRows += rows;

        print("Added " + rows + " rows from " + tables[i] + " to " + staging_table_name);

        rs.close();
    } catch(e) {
        print("Error for file " + tables[i] + ". Error: " + e);
    }
}

print("Rows inserted: " + totalRows);