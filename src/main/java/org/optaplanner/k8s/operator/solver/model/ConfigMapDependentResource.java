package org.optaplanner.k8s.operator.solver.model;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import java.util.HashMap;
import java.util.Map;

@KubernetesDependent
public class ConfigMapDependentResource extends CRUKubernetesDependentResource<ConfigMap, Solver> {

    public static final String SOLVER_MESSAGE_INPUT_KEY = "solver.message.input";
    public static final String SOLVER_MESSAGE_OUTPUT_KEY = "solver.message.output";
    public static final String SOLVER_KAFKA_BOOTSTRAP_SERVERS_KEY = "solver.kafka.bootstrap.servers";

    public ConfigMapDependentResource(KubernetesClient kubernetesClient) {
        super(ConfigMap.class);
        setKubernetesClient(kubernetesClient);
    }

    @Override
    protected ConfigMap desired(Solver solver, Context<Solver> context) {
        Map<String, String> data = new HashMap<>();
        // TODO: find a better way to pass the messa
        if (solver.getStatus() != null) {
            data.put(SOLVER_MESSAGE_INPUT_KEY, solver.getStatus().getInputMessageAddress());
            data.put(SOLVER_MESSAGE_OUTPUT_KEY, solver.getStatus().getOutputMessageAddress());
        }
        data.put(SOLVER_KAFKA_BOOTSTRAP_SERVERS_KEY, solver.getSpec().getKafkaBootstrapServers());

        return new ConfigMapBuilder()
                .withNewMetadata()
                .withName(solver.getConfigMapName())
                .withNamespace(solver.getNamespace())
                .endMetadata()
                .withData(data)
                .build();
    }

    @Override
    public ConfigMap update(ConfigMap actual, ConfigMap target, Solver solver, Context<Solver> context) {
        ConfigMap resultingConfigMap = super.update(actual, target, solver, context);
        String namespace = actual.getMetadata().getNamespace();
        getKubernetesClient()
                .pods()
                .inNamespace(namespace)
                .withLabel("app", solver.getMetadata().getName())
                .delete();
        return resultingConfigMap;
    }
}
