package com.astronomvm.kernel.engine;

import com.astronomvm.component.BaseComponent;
import com.astronomvm.component.exception.ComponentException;
import com.astronomvm.core.data.input.InputParameter;
import com.astronomvm.core.data.output.ResultFlow;
import com.astronomvm.core.data.output.ResultSet;
import com.astronomvm.core.data.output.ResultStorage;
import com.astronomvm.core.data.row.AstronomObject;
import com.astronomvm.core.data.row.DataType;
import com.astronomvm.core.meta.AstronomMetaFlow;
import com.astronomvm.core.meta.ParameterMeta;
import com.astronomvm.core.meta.StepMeta;
import com.astronomvm.core.meta.Transition;
import com.astronomvm.kernel.workflow.AstronomWorkflow;

import javax.xml.crypto.Data;
import javax.xml.transform.Result;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

public class AstronomOrchestra {

    private ResultStorage resultStorage = new ResultStorage();

    public void play(AstronomWorkflow workflow){
        HashMap<Integer,List<StepMeta>> stepsIndex = this.buildWorkflowExecutionOrder(workflow.getAstronomMetaFlow());
        stepsIndex.keySet().forEach(level -> {
            List<StepMeta> steps = stepsIndex.get(level);
            steps.forEach(step -> this.executeStep(workflow,step));
        });
    }

    private HashMap<Integer,List<StepMeta>> buildWorkflowExecutionOrder(AstronomMetaFlow metaFlow){
        HashMap<Integer,List<StepMeta>> stepsIndex = new HashMap<>();
        List<StepMeta> currentSteps = metaFlow.getStepMetaList();
        List<Transition> currentTransitions = metaFlow.getTransitions();
        final AtomicInteger level = new AtomicInteger(0);
        while(!currentSteps.isEmpty()){
            List<StepMeta> processedSteps = new ArrayList<>();
            currentSteps.forEach(step -> {
                boolean stepInTargetTransition = currentTransitions.stream().anyMatch(transition -> transition.getTarget().equals(step));
                if(!stepInTargetTransition){
                    processedSteps.add(step);
                    currentTransitions.removeIf(transition -> transition.getSource().equals(step));
                    stepsIndex.putIfAbsent(level.get(),new ArrayList<>());
                    stepsIndex.get(level.get()).add(step);
                }
            });
            currentSteps.removeAll(processedSteps);
            level.incrementAndGet();
        }
        return stepsIndex;
    }


    private void executeStep(AstronomWorkflow workflow,StepMeta stepMeta){
        BaseComponent component = workflow.getComponentByName(stepMeta.getComponentMeta().getName());
        ComponentExecutor componentExecutor = new ComponentExecutor();
        try {
            this.prepareResultFlowParameterInputs(workflow,stepMeta);
            ResultFlow resultFlow = componentExecutor.execute(component,stepMeta.getInputParameters());
            this.resultStorage.addStepResultFlow(stepMeta,resultFlow);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }


    private void prepareResultFlowParameterInputs(AstronomWorkflow workflow,StepMeta stepMeta){
        List<StepMeta> sourceSteps = workflow.getAstronomMetaFlow().getSourceSteps(stepMeta);
        List<ParameterMeta> inputFlowParameterMetas = stepMeta.getComponentMeta().getParameterMetas().stream().filter(parameterMeta -> parameterMeta.getType().equals(DataType.INPUT_FLOW_NAME)).collect(Collectors.toList());
        inputFlowParameterMetas.stream().forEach(parameterMeta -> {
            String inputFlowName = stepMeta.getInputParameters().getParameterByName(parameterMeta.getName()).getValue().toString();
            Map<String,ResultSet> resultSetMap = sourceSteps.stream().map(sourceStepMeta -> this.resultStorage.getStepMetaResultFlow(sourceStepMeta).getResultSetMap()).filter(map -> map.containsKey(inputFlowName)).findAny().get();
            ResultSet inputFlowResultSet = resultSetMap.get(inputFlowName);
            stepMeta.getInputParameters().addParameter(new InputParameter(inputFlowName,new AstronomObject(inputFlowResultSet)));
        });
    }

}
