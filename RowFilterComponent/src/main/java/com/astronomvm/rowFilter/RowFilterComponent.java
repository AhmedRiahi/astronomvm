package com.astronomvm.rowFilter;

import com.astronomvm.component.BaseComponent;
import com.astronomvm.component.exception.ComponentException;
import com.astronomvm.core.data.output.ResultFlow;
import com.astronomvm.core.data.output.ResultSet;
import com.astronomvm.core.data.row.AstronomObject;
import com.astronomvm.core.data.type.DataType;
import com.astronomvm.core.data.row.Row;
import com.astronomvm.core.meta.ComponentMeta;
import com.astronomvm.core.meta.ParameterMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


public class RowFilterComponent extends BaseComponent {

    private static final String INPUT_FLOW_NAME_PARAMETER_NAME = "INPUT_FLOW_NAME";
    private static final String VALID_OUTPUT_FLOW_NAME_PARAMETER_NAME = "VALID_OUTPUT_FLOW_NAME";
    private static final String INVALID_OUTPUT_FLOW_NAME_PARAMETER_NAME = "INVALID_OUTPUT_FLOW_NAME";
    private static final String FILTER_COLUMN_PARAMETER_NAME = "FILTER_COLUMN";
    private static final String FILTER_OPERATOR_PARAMETER_NAME = "FILTER_OPERATOR";
    private static final String FILTER_VALUE_PARAMETER_NAME = "FILTER_VALUE";

    private ResultSet inputFlowResultSet;
    private String filterColumnName;
    String filterOperatorString;
    String filterValueString;
    String validOutputFlowParameterName;
    String invalidOutputFlowParameterName;


    @Override
    public ComponentMeta getComponentMeta() {
        ComponentMeta componentMeta = new ComponentMeta();
        componentMeta.setName("ROW_FILTER");

        ParameterMeta filterColumnParameterMeta = new ParameterMeta();
        filterColumnParameterMeta.setName(FILTER_COLUMN_PARAMETER_NAME);
        filterColumnParameterMeta.setType(DataType.STRING);

        ParameterMeta filterOperatorParameterMeta = new ParameterMeta();
        filterOperatorParameterMeta.setName(FILTER_OPERATOR_PARAMETER_NAME);
        filterOperatorParameterMeta.setType(DataType.STRING);

        ParameterMeta filterValueParameterMeta = new ParameterMeta();
        filterValueParameterMeta.setName(FILTER_VALUE_PARAMETER_NAME);
        filterValueParameterMeta.setType(DataType.STRING);

        ParameterMeta inputFlowNameParameterMeta = new ParameterMeta();
        inputFlowNameParameterMeta.setName(INPUT_FLOW_NAME_PARAMETER_NAME);
        inputFlowNameParameterMeta.setType(DataType.INPUT_FLOW_NAME);

        ParameterMeta validOutputFlowParameterMeta = new ParameterMeta();
        validOutputFlowParameterMeta.setName(VALID_OUTPUT_FLOW_NAME_PARAMETER_NAME);
        validOutputFlowParameterMeta.setType(DataType.STRING);

        ParameterMeta invalidOutputFlowParameterMeta = new ParameterMeta();
        invalidOutputFlowParameterMeta.setName(INVALID_OUTPUT_FLOW_NAME_PARAMETER_NAME);
        invalidOutputFlowParameterMeta.setType(DataType.STRING);

        componentMeta.addParameterMeta(filterColumnParameterMeta);
        componentMeta.addParameterMeta(filterOperatorParameterMeta);
        componentMeta.addParameterMeta(filterValueParameterMeta);
        componentMeta.addParameterMeta(inputFlowNameParameterMeta);
        componentMeta.addParameterMeta(validOutputFlowParameterMeta);
        componentMeta.addParameterMeta(invalidOutputFlowParameterMeta);
        return componentMeta;
    }

    @Override
    public void readInputs() {
        String inputFlowParameterName = this.inputParameters.getParameterByName(INPUT_FLOW_NAME_PARAMETER_NAME).getValue().toString();
        this.inputFlowResultSet = (ResultSet) this.inputParameters.getParameterByName(inputFlowParameterName).getValue().getUnderlying();
        this.filterColumnName =  this.inputParameters.getParameterByName(FILTER_COLUMN_PARAMETER_NAME).getValue().toString();
        this.filterOperatorString = this.inputParameters.getParameterByName(FILTER_OPERATOR_PARAMETER_NAME).getValue().toString();
        this.filterValueString =  this.inputParameters.getParameterByName(FILTER_VALUE_PARAMETER_NAME).getValue().toString();
        this.validOutputFlowParameterName =  this.inputParameters.getParameterByName(VALID_OUTPUT_FLOW_NAME_PARAMETER_NAME).getValue().toString();
        this.invalidOutputFlowParameterName =  this.inputParameters.getParameterByName(INVALID_OUTPUT_FLOW_NAME_PARAMETER_NAME).getValue().toString();
    }

    @Override
    public ResultFlow execute() throws ComponentException {


        BiFunction<AstronomObject,AstronomObject,Boolean> comparisonOperation = buildComparisonOperation(this.filterOperatorString);
        Integer filterColumnIndex = this.inputFlowResultSet.getRowHeader().getColumnNameIndex(this.filterColumnName);
        AstronomObject filterValue = new AstronomObject(this.filterValueString);

        Map<Boolean,List<Row>> filterGroups =  this.inputFlowResultSet.getRows().stream().collect(Collectors.groupingBy(row -> comparisonOperation.apply(row.getColumnAt(filterColumnIndex).getValue(), filterValue)));


        ResultSet validFilterResultSet = new ResultSet();
        validFilterResultSet.setRows(filterGroups.get(true) == null ? new ArrayList<>() : filterGroups.get(true));

        ResultSet invalidFilterResultSet = new ResultSet();
        invalidFilterResultSet.setRows(filterGroups.get(false) == null ? new ArrayList<>() : filterGroups.get(false));

        ResultFlow resultFlow = new ResultFlow();
        resultFlow.addResultSet(this.validOutputFlowParameterName,validFilterResultSet);
        resultFlow.addResultSet(this.invalidOutputFlowParameterName,invalidFilterResultSet);

        return resultFlow;
    }

    private BiFunction<AstronomObject,AstronomObject,Boolean> buildComparisonOperation(String operation){
        switch (operation){
            case "=": return (AstronomObject a, AstronomObject b) -> a.equals(b);
            default: throw new UnsupportedOperationException();
        }
    }
}
