package org.optaplanner.k8s.operator.solver;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.optaplanner.k8s.operator.solver.model.DeploymentDependentResource;
import org.optaplanner.k8s.operator.solver.model.Solver;
import org.optaplanner.k8s.operator.solver.model.SolverStatus;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddress;
import org.optaplanner.k8s.operator.solver.model.messaging.MessagingAddressDependentResource;
import org.optaplanner.k8s.operator.solver.model.messaging.kafka.KafkaTopicDependentResource;

import io.fabric8.kubernetes.api.model.apps.Deployment;
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

    private KubernetesClient k8s;

    private final DeploymentDependentResource deploymentDependentResource;
    private final MessagingAddressDependentResource<KafkaTopic> inputKafkaTopicDependentResource;
    private final MessagingAddressDependentResource<KafkaTopic> outputKafkaTopicDependentResource;

    @Inject
    public SolverReconciler(KubernetesClient k8s) {
        deploymentDependentResource = new DeploymentDependentResource(k8s);
        inputKafkaTopicDependentResource = new KafkaTopicDependentResource(MessagingAddress.INPUT, k8s);
        outputKafkaTopicDependentResource = new KafkaTopicDependentResource(MessagingAddress.OUTPUT, k8s);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<Solver> context) {
        return EventSourceInitializer.nameEventSources(deploymentDependentResource.initEventSource(context),
                inputKafkaTopicDependentResource.initEventSource(context),
                outputKafkaTopicDependentResource.initEventSource(context));
    }

    @Override
    public UpdateControl<Solver> reconcile(Solver solver, Context<Solver> context) {
        deploymentDependentResource.reconcile(solver, context);
        inputKafkaTopicDependentResource.reconcile(solver, context);
        outputKafkaTopicDependentResource.reconcile(solver, context);

        Optional<KafkaTopic> inputKafkaTopic = inputKafkaTopicDependentResource.getSecondaryResource(solver);
        Optional<KafkaTopic> outputKafkaTopic = outputKafkaTopicDependentResource.getSecondaryResource(solver);

        Optional<Deployment> deployment = deploymentDependentResource.getSecondaryResource(solver);
        if (deployment.isPresent()) {
            System.out.println(deployment);
        }

        SolverStatus solverStatus = SolverStatus.success();
        solver.setStatus(solverStatus);
        if (inputKafkaTopic.isPresent()) {
            solverStatus.setInputMessagingAddress(inputKafkaTopic.get().getStatus().getTopicName());
        }
        if (outputKafkaTopic.isPresent()) {
            solverStatus.setOutputMessagingAddress(outputKafkaTopic.get().getStatus().getTopicName());
        }
        return UpdateControl.updateStatus(solver);
    }

    @Override
    public ErrorStatusUpdateControl<Solver> updateErrorStatus(Solver solver, Context<Solver> context, Exception e) {
        solver.setStatus(SolverStatus.error(e));
        return ErrorStatusUpdateControl.updateStatus(solver);
    }
}
