package org.optaplanner.k8s.operator.solver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.optaplanner.k8s.operator.solver.model.messaging.MessageAddress;

@Group("org.optaplanner.solver")
@Version("v1")
public class Solver extends CustomResource<SolverSpec, SolverStatus> implements Namespaced {

    @JsonIgnore
    public String getNamespace() {
        return getMetadata().getNamespace();
    }

    @JsonIgnore
    public String getConfigMapName() {
        return getSolverName();
    }

    @JsonIgnore
    public String getDeploymentName() {
        return getSolverName();
    }

    @JsonIgnore
    public String getInputMessageAddressName() {
        return getMessageAddressName(MessageAddress.INPUT);
    }

    @JsonIgnore
    public String getOutputMessageAddressName() {
        return getMessageAddressName(MessageAddress.OUTPUT);
    }

    @JsonIgnore
    public String getMessageAddressName(MessageAddress messageAddress) {
        return String.format("%s-%s",getSolverName(), messageAddress.getName());
    }

    @JsonIgnore
    private String getSolverName() {
        return getMetadata().getName();
    }
}
