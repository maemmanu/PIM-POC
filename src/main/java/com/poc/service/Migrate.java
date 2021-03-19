package com.poc.service;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.admin.MigrationReport;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migration")
public class Migrate {
    
    static final String ARTIFACT_ID = "PIMTest-kjar";
    static final String GROUP_ID = "com.poc";
    static final String VERSION = "1.0-SNAPSHOT";
    
    static final String VERSION_2 = "1.1-SNAPSHOT";

    private KModuleDeploymentUnit unit = null;
    
    private KModuleDeploymentUnit unitV2 = null;
    
    @Autowired
    private ProcessService processService;
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private ProcessInstanceMigrationService processInstanceMigrationService;

    @RequestMapping(value="/migrate", method=RequestMethod.POST)
    public MigrationReport migrate() {

         unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
        
        unitV2 = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION_2);
        deploymentService.deploy(unitV2);

        Map<String, Object> params = new HashMap<String, Object>();

        parameters.put("param1", "example");
		parameters.put("param2", "example");
        parameters.put("param3", "example");
        parameters.put("param4", "example");

        long processInstanceId = processService.startProcess(unit.getIdentifier(), "PIMTest.process", params);
        MigrationReport report = processInstanceMigrationService.migrate(unit.getIdentifier(), processInstanceId, unitV2.getIdentifier(), "PIMTest.process");

        return report;
    	
    }

}
