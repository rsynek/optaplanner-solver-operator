package org.optaplanner.k8s.operator.solver.model;

public class SolverSpec {
    private String solverImage;
    private String kafkaBootstrapServers;
    private String kafkaCluster;

    public SolverSpec() {
        // required by Jackson
    }

    public SolverSpec(String solverImage, String kafkaBootstrapServers, String kafkaCluster) {
        this.solverImage = solverImage;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.kafkaCluster = kafkaCluster;
    }

    public String getSolverImage() {
        return solverImage;
    }

    public void setSolverImage(String solverImage) {
        this.solverImage = solverImage;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public String getKafkaCluster() {
        return kafkaCluster;
    }

    public void setKafkaCluster(String kafkaCluster) {
        this.kafkaCluster = kafkaCluster;
    }
}
