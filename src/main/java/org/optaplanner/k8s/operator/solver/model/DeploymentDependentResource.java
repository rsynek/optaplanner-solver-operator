package org.optaplanner.k8s.operator.solver.model;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
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
        String deploymentName = getDeploymentName(solver);
        Container container = new ContainerBuilder()
                .withName(deploymentName)
                .withImage(solver.getSpec().getSolverImage())
                .build();
        return new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentName)
                .withNamespace(getDeploymentNamespace(solver))
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

    private String getDeploymentName(Solver solver) {
        return solver.getMetadata().getName();
    }

    private String getDeploymentNamespace(Solver solver) {
        return solver.getMetadata().getNamespace();
    }
}
