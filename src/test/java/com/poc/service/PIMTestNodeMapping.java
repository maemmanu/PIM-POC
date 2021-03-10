package com.poc.service;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jbpm.process.audit.JPAAuditLogService;
import javax.persistence.EntityManagerFactory;
import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.admin.MigrationReport;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class PIMTestNodeMapping {
    
    static final String ARTIFACT_ID = "PIMTestNodeMap-kjar";
    static final String GROUP_ID = "com.poc";
    static final String VERSION = "1.0-SNAPSHOT";
    static final String VERSION_2 = "1.1-SNAPSHOT";
    static final String PROCESS_ID = "PIMTestNodeMap-kjar.process";

    private KModuleDeploymentUnit unit = null;
    
    private KModuleDeploymentUnit unitV2 = null;
    
    @Autowired
    private ProcessService processService;
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private ProcessInstanceMigrationService processInstanceMigrationService;
    
    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setup() {
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
        
        unitV2 = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION_2);
        deploymentService.deploy(unitV2);
    }
    
    @After
    public void cleanup() {

        deploymentService.undeploy(unit);
        deploymentService.undeploy(unitV2);
    }
 
    @Test
    public void testProcessStartAndAbort() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "example");
		parameters.put("param2", "example");
        parameters.put("param3", "example");
        parameters.put("param4", "example");
        long processInstanceId = processService.startProcess(unit.getIdentifier(),PROCESS_ID, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        JPAAuditLogService auditService = new JPAAuditLogService(entityManagerFactory);
        ProcessInstanceLog log = auditService.findProcessInstance(processInstanceId);
        assertNotNull(log);
        assertEquals(PROCESS_ID, log.getProcessId());
        assertEquals(unit.getIdentifier(), log.getExternalId());
        
        try {
            ProcessInstanceDesc piLog = runtimeDataService.getProcessInstanceById(processInstanceId);
            assertNotNull(piLog);
            assertEquals(unit.getIdentifier(), piLog.getDeploymentId());

            Map<String, String> nodeMapping = new HashMap<>();
            nodeMapping.put("_15F3927B-8234-48CE-9EDA-8ACE24D58F6E", "_7D93E332-6FAE-4161-BA8A-E4B9E1B4499A");
            
            MigrationReport report = processInstanceMigrationService.migrate(unit.getIdentifier(), processInstanceId, unitV2.getIdentifier(), PROCESS_ID, nodeMapping);
            assertTrue(report.isSuccessful());
            
            piLog = runtimeDataService.getProcessInstanceById(processInstanceId);
            assertNotNull(piLog);
            assertEquals(unitV2.getIdentifier(), piLog.getDeploymentId());

        } finally {
            processService.abortProcessInstance(processInstanceId);
        }
    }
    
    
}
