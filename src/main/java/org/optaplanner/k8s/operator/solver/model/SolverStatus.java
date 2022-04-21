package org.optaplanner.k8s.operator.solver.model;

public class SolverStatus {
    private String errorMessage;
    private String inputMessageAddress;
    private String outputMessageAddress;

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

    public String getInputMessageAddress() {
        return inputMessageAddress;
    }

    public void setInputMessageAddress(String inputMessageAddress) {
        this.inputMessageAddress = inputMessageAddress;
    }

    public String getOutputMessageAddress() {
        return outputMessageAddress;
    }

    public void setOutputMessageAddress(String outputMessageAddress) {
        this.outputMessageAddress = outputMessageAddress;
    }
}
