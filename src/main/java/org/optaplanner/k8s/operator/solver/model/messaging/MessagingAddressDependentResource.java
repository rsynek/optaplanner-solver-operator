package org.optaplanner.k8s.operator.solver.model.messaging;

import org.optaplanner.k8s.operator.solver.model.Solver;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUKubernetesDependentResource;

public abstract class MessagingAddressDependentResource<MessagingResource extends HasMetadata>
        extends CRUKubernetesDependentResource<MessagingResource, Solver> {

    private final MessagingAddress messagingAddress;
    public MessagingAddressDependentResource(Class<MessagingResource> resourceType, MessagingAddress messagingAddress,
                                             KubernetesClient kubernetesClient) {
        super(resourceType);
        this.messagingAddress = messagingAddress;
        setKubernetesClient(kubernetesClient);
    }

    public MessagingAddress getMessagingAddress() {
        return messagingAddress;
    }
}
