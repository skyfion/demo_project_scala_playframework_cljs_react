# BUG TRACK TEST APP

front: react, clojurescript, rum, milligram  
back: scala, playframework, slick  

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



