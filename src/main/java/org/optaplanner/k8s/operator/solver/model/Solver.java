package org.optaplanner.k8s.operator.solver.model;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("org.optaplanner.solver")
@Version("v1")
public class Solver extends CustomResource<SolverSpec, SolverStatus> implements Namespaced {

}
