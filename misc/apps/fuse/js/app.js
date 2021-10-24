var cypher = new Cypher();

var app = new Vue({
    el: '#app',
    data: {
        fuseQuery: '',
        queryResults: [],
        loading: false
    },
    methods: {
        q: function() {
            var me = this;
            var q = encodeURIComponent(me.fuseQuery.replace(/\n/g, " "))
                        .replace(/'/g, "%27");
            me.loading = true;
            cypher.execute(
                "load json from 'http://localhost:4444/api/scripts/run?name=fuse&q=" + q + "' as l\
                return l",
                function(results) {
                    me.queryResults = results.output;
                    me.loading = false;
                }
            );
        }
    }
});