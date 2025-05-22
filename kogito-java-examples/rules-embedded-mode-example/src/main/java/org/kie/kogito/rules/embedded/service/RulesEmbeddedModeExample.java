package org.kie.kogito.rules.embedded.service;

import java.util.Arrays;

import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.drools.core.reteoo.ReteDumper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.kogito.rules.embedded.Person;

public class RulesEmbeddedModeExample {

        private static final Logger logger = LoggerFactory.getLogger(RulesEmbeddedModeExample.class);


    public static void main(String[] args) {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();

        KieSession kieSession = kieContainer.newKieSession();


        KieBase kieBase = kieSession.getKieBase();
        logger.info("-----> Now we dump rete <-----");
        ReteDumper.dumpRete(kieBase);
        ReteDumper.dumpAssociatedRulesRete(kieBase);
        logger.info("-----> Now we execute rules in the stateful session <-----");
        kieSession.addEventListener(new DebugRuleRuntimeEventListener());

        ExecutionResults executionResults = kieSession.execute(
            CommandFactory.newBatchExecution(Arrays.asList(
                    CommandFactory.newInsert(getPerson(1)),
                    CommandFactory.newInsert(getPerson(2)),
                CommandFactory.newFireAllRules()
            ))
        );

        logger.info("application: " + executionResults.getResults().get("application"));

        kieSession.dispose();

//        logger.info("-----> Now we execute rules in the stateless session <-----");

//        StatelessKieSession statelessKieSession = kieContainer.newStatelessKieSession();
//        statelessKieSession.addEventListener(new DebugRuleRuntimeEventListener());
//
//        ExecutionResults statelessExecutionResults = statelessKieSession.execute(
//            CommandFactory.newBatchExecution(Arrays.asList(
//                CommandFactory.newInsert(getPerson(3)),
//                CommandFactory.newInsert(getPerson(4))))
//        );
//
//        logger.info("application: " + statelessExecutionResults.getResults().get("application"));
    }

    private static Person getPerson(int i) {
        Person toReturn = new Person();
        toReturn.setAge(i);
        toReturn.setHair(String.format("Hair-%d", i));
        return toReturn;
    }
}
