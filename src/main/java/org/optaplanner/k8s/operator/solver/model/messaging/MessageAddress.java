package org.optaplanner.k8s.operator.solver.model.messaging;

public enum MessageAddress {

    INPUT("problem"),
    OUTPUT("solution");

    private final String name;

    MessageAddress(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
