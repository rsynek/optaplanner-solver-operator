package org.optaplanner.k8s.operator.solver.model;

public class SolverStatus {
    private String errorMessage;
    private String inputMessagingAddress;
    private String outputMessagingAddress;

    public SolverStatus() {
        // required by Jackson
    }

    private SolverStatus(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static SolverStatus success() {
        return new SolverStatus(null);
    }

    public static SolverStatus error(Exception exception) {
        return new SolverStatus(exception.getMessage());
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getInputMessagingAddress() {
        return inputMessagingAddress;
    }

    public void setInputMessagingAddress(String inputMessagingAddress) {
        this.inputMessagingAddress = inputMessagingAddress;
    }

    public String getOutputMessagingAddress() {
        return outputMessagingAddress;
    }

    public void setOutputMessagingAddress(String outputMessagingAddress) {
        this.outputMessagingAddress = outputMessagingAddress;
    }
}
