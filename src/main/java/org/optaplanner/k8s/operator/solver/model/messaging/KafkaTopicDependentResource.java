package org.optaplanner.k8s.operator.solver.model.messaging;

import java.util.Map;

import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.PrimaryToSecondaryMapper;
import org.optaplanner.k8s.operator.solver.model.Solver;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.strimzi.api.kafka.model.KafkaTopic;
import io.strimzi.api.kafka.model.KafkaTopicBuilder;

@KubernetesDependent
public final class KafkaTopicDependentResource extends CRUKubernetesDependentResource<KafkaTopic, Solver>
        implements PrimaryToSecondaryMapper<Solver> {

    private static final String STRIMZI_LABEL = "strimzi.io/cluster";

    private final MessageAddress messageAddress;

    public KafkaTopicDependentResource(MessageAddress messageAddress, KubernetesClient kubernetesClient) {
        super(KafkaTopic.class);
        this.messageAddress = messageAddress;
        setKubernetesClient(kubernetesClient);
    }

    @Override
    protected KafkaTopic desired(Solver solver, Context<Solver> context) {
        final String topicName = solver.getMessageAddressName(messageAddress);
        return new KafkaTopicBuilder()
                .withNewMetadata()
                .withName(topicName)
                .withNamespace(solver.getNamespace())
                .withLabels(Map.of(STRIMZI_LABEL, solver.getSpec().getKafkaCluster()))
                .endMetadata()
                .withNewSpec()
                .withTopicName(topicName)
                .withReplicas(1)
                .withPartitions(solver.getSpec().getReplicas())
                .endSpec()
                .build();
    }

    @Override
    public ResourceID toSecondaryResourceID(Solver solver) {
        return new ResourceID(solver.getMessageAddressName(messageAddress), solver.getNamespace());
    }
}
