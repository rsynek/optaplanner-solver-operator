package org.optaplanner.k8s.operator.solver.model.messaging.kafka;

import java.util.Map;

import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddress;
import org.optaplanner.k8s.operator.solver.model.Solver;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddressDependentResource;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.strimzi.api.kafka.model.KafkaTopic;
import io.strimzi.api.kafka.model.KafkaTopicBuilder;

@KubernetesDependent
public final class KafkaTopicDependentResource extends MessagingAddressDependentResource<KafkaTopic> {
    public KafkaTopicDependentResource(MessagingAddress messagingAddress, KubernetesClient k8s) {
        super(KafkaTopic.class, messagingAddress, k8s);
    }

    @Override
    protected KafkaTopic desired(Solver solver, Context<Solver> context) {
        return new KafkaTopicBuilder()
                .withNewMetadata()
                .withName(getTopicName(solver.getMetadata().getName()))
                .withNamespace(solver.getMetadata().getNamespace())
                .withLabels(Map.of("strimzi.io/cluster", solver.getSpec().getKafkaCluster()))
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withPartitions(1)
                .endSpec()
                .build();
    }

    private String getTopicName(String solverName) {
        return solverName + "-" + getMessagingAddress().getName();
    }

}
