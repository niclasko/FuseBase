select
    ga.`ga:city`,
    ga.`ga:month`,
    ga.`ga:year`,
    ga.`ga:country`,
    ga.`ga:latitude`,
    ga.`ga:longitude`,
    ga.`ga:sessions`,
    ga.`ga:users`,
    ga.`ga:newUsers`,
    ga.`ga:newUsers`/coalesce(_xl.Value, 1) as newusers_div_by_value_from_excel,
    coalesce(_xl.Value, '') as ValueFromExcel
from
    q("select ga:city, ga:month, ga:year, ga:country, ga:latitude, ga:longitude,
        ga:sessions, ga:users, ga:newUsers from '154354697'
        where date between 10DaysAgo and today", "ga") as ga
    left join
    q("/Users/niclas/Documents/data/excel/test.xlsx", "excel") as _xl
    on (ga.`ga:country` = _xl.Country)

select
    ga.`ga:country` as country, count(1) as cnt
from
    q("select ga:city, ga:month, ga:year, ga:country, ga:latitude, ga:longitude,
        ga:sessions, ga:users, ga:newUsers from '154354697'
        where date between 10DaysAgo and today", "ga") as ga
    inner join
    q("/Users/niclas/Documents/data/excel/test.xlsx", "excel") as _xl
    on (ga.`ga:country` = _xl.Country) group by ga.`ga:country`