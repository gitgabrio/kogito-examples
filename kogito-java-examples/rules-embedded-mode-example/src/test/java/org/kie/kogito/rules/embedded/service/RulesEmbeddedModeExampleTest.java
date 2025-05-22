package org.kie.kogito.rules.embedded.service;

import java.util.Arrays;

import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.drools.core.reteoo.ReteDumper;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.kogito.rules.embedded.Person;

public class RulesEmbeddedModeExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(RulesEmbeddedModeExample.class);

    @Test
    public void testExecution() {
        KieSession kieSession = getKieSession();
        logger.info("-----> KieSession retrieved {} <-----", kieSession);

        logger.info("-----> Now we execute rules in the stateful session <-----");
        kieSession.addEventListener(new DebugRuleRuntimeEventListener());
        kieSession.addEventListener(new DebugAgendaEventListener());

        ExecutionResults executionResults = kieSession.execute(
                CommandFactory.newBatchExecution(Arrays.asList(
                        CommandFactory.newInsert(getPerson(1)),
                        CommandFactory.newInsert(getPerson(2)),
                        CommandFactory.newFireAllRules()
                ))
        );

        logger.info("application: " + executionResults.getResults().get("application"));

        logger.info("-----> Now we dispose kieSession <-----");
        kieSession.dispose();
    }

    @Test
    public void testReteDumping() {
        KieSession kieSession = getKieSession();
        logger.info("-----> KieSession retrieved {} <-----", kieSession);

        KieBase kieBase = kieSession.getKieBase();
        logger.info("-----> Now we dump rete <-----");
        ReteDumper.dumpRete(kieBase);
        logger.info("-----> Now we dump associated rules map <-----");
        ReteDumper.dumpAssociatedRulesRete(kieBase);
        logger.info("-----> Now we dispose kieSession <-----");
        kieSession.dispose();
    }

    private KieSession getKieSession() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();

        return kieContainer.newKieSession();
    }

    private static Person getPerson(int i) {
        Person toReturn = new Person();
        toReturn.setAge(i+20);
        toReturn.setHair(String.format("Hair-%d", i));
        return toReturn;
    }
}
