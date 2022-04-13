package org.optaplanner.k8s.operator.solver;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.k8s.operator.solver.model.Solver;
import org.optaplanner.k8s.operator.solver.model.SolverSpec;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddress;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

@WithKubernetesTestServer
@QuarkusTest
public class SolverReconcilerTest {

    @KubernetesTestServer
    KubernetesServer mockServer;

    @Test
    void canReconcile() {
//        GenericKubernetesResource topicCrd = new GenericKubernetesResourceBuilder().with
//        mockServer.getClient().genericKubernetesResources("kafka.strimzi.io/v1beta2", "KafkaTopic")
//                .createOrReplace()
//
        final Solver solver = new Solver();
        solver.getMetadata().setName("test-solver");
        solver.setSpec(new SolverSpec());
        solver.getSpec().setSolverImage("solver-project-image");
        solver.getSpec().setKafkaBootstrapServers("kafkaServers");
        solver.getSpec().setKafkaCluster("my-kafka-cluster");
        mockServer.getClient().resources(Solver.class).create(solver);

        await().ignoreException(NullPointerException.class).atMost(1, MINUTES).untilAsserted(() -> {
            Solver updatedSolver = mockServer.getClient()
                    .resources(Solver.class)
                    .inNamespace(solver.getMetadata().getNamespace())
                    .withName(solver.getMetadata().getName())
                    .get();
            assertThat(updatedSolver.getStatus()).isNotNull();
            assertThat(updatedSolver.getStatus().getInputMessagingAddress())
                    .isEqualTo("test-solver" + "-" + MessagingAddress.INPUT.getName());
            assertThat(updatedSolver.getStatus().getOutputMessagingAddress())
                    .isEqualTo("test-solver" + "-" + MessagingAddress.OUTPUT.getName());
        });

        List<Deployment> deployments = mockServer.getClient()
                .resources(Deployment.class)
                .inNamespace(solver.getMetadata().getNamespace())
                .list()
                .getItems();
        assertThat(deployments).hasSize(1);
        assertThat(deployments.get(0).getMetadata().getName()).isEqualTo("test-solver");
    }
}