package org.kie.kogito.rules.embedded;

import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.drools.commands.SetActiveAgendaGroup;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.kogito.rules.a.Applicant;
import org.kie.kogito.rules.a.LoanApplication;
import org.kie.kogito.rules.b.Hello;
import org.kie.util.maven.support.ReleaseIdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesEmbeddedModeExample {

    private static final Logger logger = LoggerFactory.getLogger(RulesEmbeddedModeExample.class);

    private static final String KIEBASE_A = "KBaseA";
    private static final String KIEBASE_B = "KBaseB";
    private static final Map<String, EmbeddingDTO> executionMap = new HashMap<>();

    static {
        List<Command> kiebaseACommands = Arrays.asList(
                CommandFactory.newInsert(new Applicant("#0001", 20), "applicant"),
                CommandFactory.newInsert(new LoanApplication("#0001"), "application"),
                new SetActiveAgendaGroup("applicationGroup"),
                CommandFactory.newFireAllRules());
        executionMap.put(KIEBASE_A, new EmbeddingDTO("application", kiebaseACommands));
        List<Command> kiebaseBCommands = Arrays.asList(
                CommandFactory.newInsert(new Hello("foo", false), "hello"),
                CommandFactory.newFireAllRules());
        executionMap.put(KIEBASE_B, new EmbeddingDTO("hello", kiebaseBCommands));
    }

    public static void main(String[] args) {
        KieServices kieServices = KieServices.Factory.get();
        executeByClasspath(kieServices);
        // THIS SEEMS CURRENTLY BROKEN DUE TO
//        Exception in thread "main" java.lang.NoSuchMethodError: 'java.util.List org.eclipse.aether.spi.connector.layout.RepositoryLayout.getChecksums(org.eclipse.aether.metadata.Metadata, boolean, java.net.URI)'
//        at org.eclipse.aether.connector.basic.BasicRepositoryConnector.get(BasicRepositoryConnector.java:231)
//        at org.eclipse.aether.internal.impl.DefaultMetadataResolver$ResolveTask.run(DefaultMetadataResolver.java:556)
//        at org.eclipse.aether.util.concurrency.RunnableErrorForwarder.lambda$wrap$0(RunnableErrorForwarder.java:66)
        //executeByReleaseId(kieServices);
    }

    private static void executeByClasspath(KieServices kieServices) {
        logger.info("-----> Now we execute rules by classpath <-----");
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        execute(kieContainer);
    }

    private static void executeByReleaseId(KieServices kieServices) {
        logger.info("-----> Now we execute rules by release ID <-----");
        ReleaseId releaseId = new ReleaseIdImpl("org.kie.kogito.rules.embedded:rules-jar:999-SNAPSHOT");
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        execute(kieContainer);
    }

    private static void execute(KieContainer kieContainer) {
        logger.info("-----> Now we execute for all kiebases/rules with kieContainer {}<-----", kieContainer);
        executionMap.forEach((kieBaseName, embeddingDTO) -> execute(kieContainer, kieBaseName, embeddingDTO.commands, embeddingDTO.output));
    }


    private static void execute(KieContainer kieContainer, String kieBaseName, List<Command> commands, String output) {
        logger.info("-----> Now we execute rules for {} in the stateful session <-----", kieBaseName);

        KieBase kieBase = kieContainer.getKieBase(kieBaseName);
        logger.info("-----> kieBase <-----");
        logger.info(kieBase.toString());
        String statefulSession = kieBaseName + "_stateful";
        KieSession kieSession = kieContainer.newKieSession(statefulSession);
        kieSession.addEventListener(new DebugRuleRuntimeEventListener());

        ExecutionResults executionResults = kieSession.execute(
                CommandFactory.newBatchExecution(commands));

        logger.info(output + ": " + executionResults.getResults().get(output));

        kieSession.dispose();

        logger.info("-----> Now we execute rules in the stateless session <-----");

        String statelessKieBase = kieBaseName + "_stateless";
        StatelessKieSession statelessKieSession = kieContainer.newStatelessKieSession(statelessKieBase);
        statelessKieSession.addEventListener(new DebugRuleRuntimeEventListener());

        ExecutionResults statelessExecutionResults = statelessKieSession.execute(
                CommandFactory.newBatchExecution(commands));

        logger.info(output + ": " + statelessExecutionResults.getResults().get(output));
    }

    private static class EmbeddingDTO {

        private final String output;
        private final List<Command> commands;

        public EmbeddingDTO(String output, List<Command> commands) {
            this.output = output;
            this.commands = commands;
        }
    }
}
