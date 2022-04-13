package org.optaplanner.k8s.operator.solver.model.messaging;

public enum MessagingAddress {

    INPUT("problem"),
    OUTPUT("solution");

    private final String name;

    MessagingAddress(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
