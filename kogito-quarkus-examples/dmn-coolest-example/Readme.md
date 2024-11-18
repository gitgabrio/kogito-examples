DMN Coolest Example
===================

Let's try to open http://localhost:8080/q/swagger-ui/ 

Ok, now it seems to work, let's experiment some rest endpoint call:


### POST /hello

Post "hello":

```sh
curl -H "Content-Type: application/json" -X POST -d '{"strings":["world"]}' http://localhost:8080/hello
```

### POST /LoanEligibility

Post:

```sh
curl -X POST 'http://localhost:8080/LoanEligibility' -H 'Content-Type: application/json' \
    -d '{
        "Client": {"age": 43,"salary": 1950, "existing payments": 100},
        "Loan": {"duration": 15,"installment": 180}, 
        "SupremeDirector" : "Yes", 
        "Bribe": 1000
    }'
```

```sh
mvn clean verify
```

Ok, now some test going on... 

And the new module has been referenced in [parent pom](../pom.xml)

But... what about executing jar ?

```
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

Where is http://localhost:8080/q/swagger-ui
?