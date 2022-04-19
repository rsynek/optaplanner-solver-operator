package org.optaplanner.k8s.operator.solver;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.k8s.operator.solver.model.Solver;
import org.optaplanner.k8s.operator.solver.model.SolverSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddress;

@WithKubernetesTestServer
@QuarkusTest
public class SolverReconcilerTest {

    @KubernetesTestServer
    KubernetesServer mockServer;

    @Test
    void canReconcile() {
        final Solver solver = new Solver();
        final String solverName = "test-solver";
        solver.getMetadata().setName(solverName);
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
                    .isEqualTo(solverName + "-" + MessagingAddress.INPUT.getName());
            assertThat(updatedSolver.getStatus().getOutputMessagingAddress())
                    .isEqualTo(solverName + "-" + MessagingAddress.OUTPUT.getName());
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
