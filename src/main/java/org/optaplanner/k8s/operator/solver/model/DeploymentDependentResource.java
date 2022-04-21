package org.optaplanner.k8s.operator.solver.model;

import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent
public final class DeploymentDependentResource extends CRUKubernetesDependentResource<Deployment, Solver> {

    public DeploymentDependentResource(KubernetesClient k8s) {
        super(Deployment.class);
        setKubernetesClient(k8s);
    }

    @Override
    protected Deployment desired(Solver solver, Context<Solver> context) {
        String deploymentName = solver.getDeploymentName();

        Container container = new ContainerBuilder()
                .withName(deploymentName)
                .withImage(solver.getSpec().getSolverImage())
                .withEnv(buildEnvironmentVariablesMapping(solver.getConfigMapName()))
                .build();
        return new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentName)
                .withNamespace(solver.getNamespace())
                .endMetadata()
                .withNewSpec()
                .withNewSelector().withMatchLabels(Map.of("app", deploymentName))
                .endSelector()
                .withReplicas(1)
                .withNewTemplate()
                .withNewMetadata().withLabels(Map.of("app", deploymentName)).endMetadata()
                .withNewSpec()
                .withContainers(container)
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private List<EnvVar> buildEnvironmentVariablesMapping(String configMapName) {
        EnvVar envVarMessageInput = new EnvVarBuilder()
                .withName("SOLVER_MESSAGE_INPUT")
                .withNewValueFrom()
                .withNewConfigMapKeyRef(ConfigMapDependentResource.SOLVER_MESSAGE_INPUT_KEY, configMapName, false)
                .endValueFrom()
                .build();

        EnvVar envVarMessageOutput = new EnvVarBuilder()
                .withName("SOLVER_MESSAGE_OUTPUT")
                .withNewValueFrom()
                .withNewConfigMapKeyRef(ConfigMapDependentResource.SOLVER_MESSAGE_OUTPUT_KEY, configMapName, false)
                .endValueFrom()
                .build();

        EnvVar envVarKafkaServers = new EnvVarBuilder()
                .withName("SOLVER_KAFKA_BOOTSTRAP_SERVERS")
                .withNewValueFrom()
                .withNewConfigMapKeyRef(ConfigMapDependentResource.SOLVER_KAFKA_BOOTSTRAP_SERVERS_KEY, configMapName, false)
                .endValueFrom()
                .build();
        return List.of(envVarMessageInput, envVarMessageOutput, envVarKafkaServers);
    }
}
