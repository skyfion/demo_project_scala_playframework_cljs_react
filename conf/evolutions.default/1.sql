# DC schema

# --- !Ups

CREATE TABLE TASK (
    ID long NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    status varchar(255) NOT NULL
);



# --- !Downs

DROP TABLE TASK;
