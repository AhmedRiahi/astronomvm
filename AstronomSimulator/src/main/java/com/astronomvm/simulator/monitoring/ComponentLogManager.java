package com.astronomvm.simulator.monitoring;

import com.astronomvm.core.service.IComponentLogManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Data
public class ComponentLogManager implements IComponentLogManager {

    private OrchestraEventsPublisher orchestraEventsPublisher;

    @Override
    public void log(String message){
        log.info(message);
        this.orchestraEventsPublisher.publishLog(message);
    }

}
