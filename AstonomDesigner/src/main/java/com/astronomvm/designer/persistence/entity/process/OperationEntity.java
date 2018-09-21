package com.astronomvm.designer.persistence.entity.process;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "OPERATION")
public class OperationEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Column(name = "name")
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private List<StepEntity> steps;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TransitionEntity> transitions;
}