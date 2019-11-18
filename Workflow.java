import akka.actor.typed.javadsl.ActorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Workflow {
    List<String> jobs_ = new ArrayList<>(Arrays.asList("run-grakn-1", "run-grakn-2", "test-performance-big")); // auxillary

    private String name;
    private Executor executor;
    Set<String> jobs = new HashSet<>(jobs_);
    List<Map.Entry<String, String>> dependencies = Arrays.asList(
            new HashMap.SimpleImmutableEntry<>(jobs_.get(0), jobs_.get(2)),
            new HashMap.SimpleImmutableEntry<>(jobs_.get(1), jobs_.get(2))
    );

    public Workflow(String name, ActorContext<Void> context) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void dependencies(Set<Workflow> dependsOn, Set<Workflow> dependedBy) {
    }

    public void start() {

    }

    public static class Executor {

    }
}
