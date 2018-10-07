package com.astronomvm.designer.transformer;

import com.astronomvm.core.data.EnvironmentVariables;
import com.astronomvm.designer.persistence.entity.configuration.EnvironmentVariablesEntity;

public class EnvironmentVariablesTransformer {

    private EnvironmentVariablesTransformer(){}

    public synchronized static EnvironmentVariables fromEntity(EnvironmentVariablesEntity entity){
        EnvironmentVariables environmentVariables = new EnvironmentVariables();
        environmentVariables.setFunctionalRepositoryServiceUrl(entity.getFunctionalRepositoryServiceUrl());
        return environmentVariables;
    }


    public synchronized static EnvironmentVariablesEntity fromPayload(EnvironmentVariables payload){
        EnvironmentVariablesEntity environmentVariablesEntity = new EnvironmentVariablesEntity();
        environmentVariablesEntity.setFunctionalRepositoryServiceUrl(payload.getFunctionalRepositoryServiceUrl());
        return environmentVariablesEntity;
    }

}
