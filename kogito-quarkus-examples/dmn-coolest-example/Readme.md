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

Now here it is
http://localhost:8080/q/swagger-ui

Always include quarkus.swagger-ui.always-include=true in quarkus applications!!!

Debug, debug, debug...
======================

Code generation
---------------
1. open org.kie.kogito.codegen.core.AbstractGenerator inside kogito-runtimes 
2. put breakpoints at line 42 and 69
3. issue mvn clean compile quarkus:dev -Dsuspend
4. connect kogito-runtimes remote debugger at port 5005
5. debug code generation
6. stop debugger on kogito-runtimes

Application runtime
-------------------
1. open target/generated-sources/https_58_47_47github_46com_47kiegroup_47kogito_45examples_47dmn_45quarkus_45listener_45example/LoanEligibilityResource.java
2. put breakpoint at line 65
3. connect kogito-examples remote debugger at port 5005 
4. execute post on http://localhost:8080/LoanEligibility
