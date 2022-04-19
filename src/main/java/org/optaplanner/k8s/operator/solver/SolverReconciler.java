package org.optaplanner.k8s.operator.solver;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.optaplanner.k8s.operator.solver.model.ConfigMapDependentResource;
import org.optaplanner.k8s.operator.solver.model.DeploymentDependentResource;
import org.optaplanner.k8s.operator.solver.model.Solver;
import org.optaplanner.k8s.operator.solver.model.SolverStatus;
import org.optaplanner.k8s.operator.solver.model.messaging.KafkaTopicDependentResource;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddress;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.strimzi.api.kafka.model.KafkaTopic;

@ControllerConfiguration
public class SolverReconciler implements Reconciler<Solver>, ErrorStatusHandler<Solver>, EventSourceInitializer<Solver> {

    private KubernetesClient kubernetesClient;

    private final DeploymentDependentResource deploymentDependentResource;
    private final KafkaTopicDependentResource inputKafkaTopicDependentResource;
    private final KafkaTopicDependentResource outputKafkaTopicDependentResource;
    private final ConfigMapDependentResource configMapDependentResource;

    @Inject
    public SolverReconciler(KubernetesClient kubernetesClient) {
        deploymentDependentResource = new DeploymentDependentResource(kubernetesClient);
        inputKafkaTopicDependentResource = new KafkaTopicDependentResource(MessagingAddress.INPUT, kubernetesClient);
        outputKafkaTopicDependentResource = new KafkaTopicDependentResource(MessagingAddress.OUTPUT, kubernetesClient);
        configMapDependentResource = new ConfigMapDependentResource(kubernetesClient);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<Solver> context) {
        return EventSourceInitializer.nameEventSources(deploymentDependentResource.initEventSource(context),
                inputKafkaTopicDependentResource.initEventSource(context),
                outputKafkaTopicDependentResource.initEventSource(context),
                configMapDependentResource.initEventSource(context));
    }

    @Override
    public UpdateControl<Solver> reconcile(Solver solver, Context<Solver> context) {
        deploymentDependentResource.reconcile(solver, context);
        inputKafkaTopicDependentResource.reconcile(solver, context);
        outputKafkaTopicDependentResource.reconcile(solver, context);
        configMapDependentResource.reconcile(solver, context);

        Optional<KafkaTopic> inputKafkaTopic = inputKafkaTopicDependentResource.getSecondaryResource(solver);
        Optional<KafkaTopic> outputKafkaTopic = outputKafkaTopicDependentResource.getSecondaryResource(solver);

        SolverStatus solverStatus = SolverStatus.success();
        solver.setStatus(solverStatus);
        if (inputKafkaTopic.isPresent()) {
            solverStatus.setInputMessagingAddress(inputKafkaTopic.get().getSpec().getTopicName());
        }
        if (outputKafkaTopic.isPresent()) {
            solverStatus.setOutputMessagingAddress(outputKafkaTopic.get().getSpec().getTopicName());
        }
        return UpdateControl.updateStatus(solver);
    }

    @Override
    public ErrorStatusUpdateControl<Solver> updateErrorStatus(Solver solver, Context<Solver> context, Exception e) {
        solver.setStatus(SolverStatus.error(e));
        return ErrorStatusUpdateControl.updateStatus(solver);
    }
}
