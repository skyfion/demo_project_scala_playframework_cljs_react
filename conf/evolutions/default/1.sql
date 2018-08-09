# DC schema

# --- !Ups

CREATE TABLE TASK (
    id SERIAL PRIMARY KEY,
    name varchar(255) NOT NULL,
    description varchar(255),
    status varchar(255) NOT NULL
);



# --- !Downs

DROP TABLE TASK;
