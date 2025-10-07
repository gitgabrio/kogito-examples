package org.kie.kogito.decisions.embedded;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.AfterInvokeBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeInvokeBKMEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.kogito.decisions.a.Driver;
import org.kie.kogito.decisions.a.Violation;
import org.kie.kogito.decisions.b.Applicant;
import org.kie.kogito.decisions.b.LoanApplication;
import org.kie.util.maven.support.ReleaseIdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionsEmbeddedModeExample {

    private static final Logger logger = LoggerFactory.getLogger(DecisionsEmbeddedModeExample.class);

    private static final String KIEBASE_A = "KBaseA";
    private static final String KIEBASE_B = "KBaseB";
    private static final Map<String, EmbeddingDTO> executionMap = new HashMap<>();

    static {
        Driver driver = new Driver();
        driver.setPoints(20);
        Violation violation = new Violation();
        violation.setType("speed");
        violation.setActualSpeed(BigDecimal.valueOf(120));
        violation.setSpeedLimit(BigDecimal.valueOf(100));
        Map<String, Object> kiebaseAInputData = Map.of("Driver", driver,
                                                       "Violation", violation);
        executionMap.put(KIEBASE_A, new EmbeddingDTO("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F" +
                                                             "-93B2-A2598F19ECFF",
                                                     "Traffic Violation",
                                                     kiebaseAInputData));

        Map<String, Object> kiebaseBInputData = Map.of("Applicant", new Applicant("#0001", 20),
                                                       "Application", new LoanApplication("#0001"));
        executionMap.put(KIEBASE_B, new EmbeddingDTO("https://kie.org/dmn/_C83DFD16-A42A-46BE-A843-370444580E0F",
                                                     "loan-application-age-limit",
                                                     kiebaseBInputData));
    }

    public static void main(String[] args) {
        KieServices kieServices = KieServices.Factory.get();
        executeByClasspath(kieServices);
        // THIS SEEMS CURRENTLY BROKEN DUE TO
//        Exception in thread "main" java.lang.NoSuchMethodError: 'java.util.List org.eclipse.aether.spi.connector
//        .layout.RepositoryLayout.getChecksums(org.eclipse.aether.metadata.Metadata, boolean, java.net.URI)'
//        at org.eclipse.aether.connector.basic.BasicRepositoryConnector.get(BasicRepositoryConnector.java:231)
//        at org.eclipse.aether.internal.impl.DefaultMetadataResolver$ResolveTask.run(DefaultMetadataResolver.java:556)
//        at org.eclipse.aether.util.concurrency.RunnableErrorForwarder.lambda$wrap$0(RunnableErrorForwarder.java:66)
        //executeByReleaseId(kieServices);
    }

    private static void executeByClasspath(KieServices kieServices) {
        logger.info("-----> Now we execute decisions by classpath <-----");
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        execute(kieContainer);
    }

    private static void executeByReleaseId(KieServices kieServices) {
        logger.info("-----> Now we execute decisions by release ID <-----");
        ReleaseId releaseId = new ReleaseIdImpl("org.kie.kogito.decisions.embedded:decisions-jar:999-SNAPSHOT");
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        execute(kieContainer);
    }

    private static void execute(KieContainer kieContainer) {
        logger.info("-----> Now we execute for all kiebases/decisions with kieContainer {}<-----", kieContainer);
        executionMap.forEach((kieBaseName, embeddingDTO) -> execute(kieContainer, kieBaseName, embeddingDTO.modelName
                , embeddingDTO.namespace, embeddingDTO.inputData));
    }

    private static void execute(KieContainer kieContainer, String kieBaseName, String modelName, String nameSpace,
                                Map<String, Object> inputData) {
        logger.info("-----> Now we execute decisions for {} <-----", kieBaseName);

        KieBase kieBase = kieContainer.getKieBase(kieBaseName);
        logger.info("-----> kieBase <-----");
        logger.info(kieBase.toString());
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieBase).get(DMNRuntime.class);
        dmnRuntime.addListener(new LoggerListener());

        DMNModel dmnModel = dmnRuntime.getModel(nameSpace, modelName);

        DMNContext dmnContext = dmnRuntime.newContext();
        inputData.forEach(dmnContext::set);
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);

        for (DMNDecisionResult dr : dmnResult.getDecisionResults()) {
            logger.info(
                    "Decision: '{} ', Result: {}", dr.getDecisionName(), dr.getResult());
        }
    }

    private static class LoggerListener implements DMNRuntimeEventListener {
        @Override
        public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {
            logger.info("beforeEvaluateDecision {}", event);
        }

        @Override
        public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {
            logger.info("afterEvaluateDecision {}", event);
        }

        @Override
        public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {
            logger.info("beforeEvaluateBKM {}", event);
        }

        @Override
        public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {
            logger.info("afterEvaluateBKM {}", event);
        }

        @Override
        public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {
            logger.info("beforeEvaluateDecisionTable {}", event);
        }

        @Override
        public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
            logger.info("afterEvaluateDecisionTable {}", event);
        }

        @Override
        public void beforeEvaluateDecisionService(BeforeEvaluateDecisionServiceEvent event) {
            logger.info("beforeEvaluateDecisionService {}", event);
        }

        @Override
        public void afterEvaluateDecisionService(AfterEvaluateDecisionServiceEvent event) {
            logger.info("afterEvaluateDecisionService {}", event);
        }

        @Override
        public void beforeInvokeBKM(BeforeInvokeBKMEvent event) {
            logger.info("beforeInvokeBKM {}", event);
        }

        @Override
        public void afterInvokeBKM(AfterInvokeBKMEvent event) {
            logger.info("afterInvokeBKM {}", event);
        }
    }

    private static class EmbeddingDTO {

        private final String namespace;
        private final String modelName;
        private final Map<String, Object> inputData;

        public EmbeddingDTO(String namespace, String modelName, Map<String, Object> inputData) {
            this.namespace = namespace;
            this.modelName = modelName;
            this.inputData = inputData;
        }
    }
}
