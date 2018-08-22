# DEMO APP (simple bug tracker)

front: react, clojurescript, rum, milligram  
back: scala, playframework, slick, lucene  

## How to run

postgres connect settings _conf/application.conf_
```
slick.dbs.default.db.url="jdbc:postgresql://localhost/bug-tracker"
slick.dbs.default.db.user="postgres"
slick.dbs.default.db.password="****"
```

Start the Play app:

```bash
sbt run
```

And open [http://localhost:9000/](http://localhost:9000/)


![](https://raw.githubusercontent.com/skyfion/demo_project_scala_playframework_cljs_react/master/demo.gif)



