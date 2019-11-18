import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.AbstractMap.SimpleImmutableEntry;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String workflowName = "performance";
        List<String> jobs = new ArrayList<>(Arrays.asList("run-grakn-1", "run-grakn-2", "test-performance-big"));
        List<Map.Entry<String, String>> dependencies = Arrays.asList(
                new HashMap.SimpleImmutableEntry<>(jobs.get(0), jobs.get(2)),
                new HashMap.SimpleImmutableEntry<>(jobs.get(1), jobs.get(2))
        );

        Behavior<Void> grabl = grabl(new HashSet<>(jobs), dependencies);
        ActorSystem<Void> actorSystem = ActorSystem.create(grabl, workflowName);
        actorSystem.getWhenTerminated().toCompletableFuture().get();
    }

    public static Behavior<Void> grabl(Set<String> jobNames, List<Map.Entry<String, String>> jobDependencyNames) {
        return Behaviors.setup(
                ctx -> {
                    List<Map.Entry<String, String>> jobDepNamesInverted = jobDependencyNames.stream()
                            .map(kv -> new SimpleImmutableEntry<>(kv.getValue(), kv.getKey()))
                            .collect(Collectors.toList());

                    Map<String, Job> jobs = jobNames.stream().map(jobName ->
                            new Job(jobName, "echo hello", ctx)).collect(Collectors.toMap(Job::getName, value -> value));

                    for (String jobName: jobNames) {
                        Job job = jobs.get(jobName);
                        Set<Job> dependsOn = jobDepNamesInverted.stream()
                                .filter(keyVal -> keyVal.getKey().equals(jobName))
                                .map(keyVal -> jobs.get(keyVal.getValue()))
                                .collect(Collectors.toSet());
                        Set<Job> dependedBy = jobDependencyNames.stream()
                                .filter(keyVal -> keyVal.getKey().equals(jobName))
                                .map(keyVal -> jobs.get(keyVal.getValue()))
                                .collect(Collectors.toSet());
                        job.dependencies(dependsOn, dependedBy);
                    }

                    jobs.values().forEach(Job::start);

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }
}