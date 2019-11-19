import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.AbstractMap.SimpleImmutableEntry;

interface Pipeline {
    class Build implements Pipeline {
        private final String name = "build";
        private final Set<String> workflows = new HashSet<>(Arrays.asList("correctness", "performance"));
        private final Set<Map.Entry<String, String>> dependencies = new HashSet<>(Arrays.asList(
                new AbstractMap.SimpleImmutableEntry<>("correctness", "performance")
        ));
        private ActorRef<Message> executor;

        // TODO: take in org, repo, commit
        Build(ActorContext<String> context) {
            this.executor = context.spawn(Executor.create(name, workflows, dependencies), name);
            System.out.println(this + ": created");
        }

        void start() {
            // TODO: define workflow dependencies
            executor.tell(new Message.Pipeline.Start());
        }

        @Override
        public String toString() {
            return name;
        }

        static class Executor extends AbstractBehavior<Message> {
            private final String name;
            private final Set<String> workflows;
            private final Set<Map.Entry<String, String>> dependencies;
            private Map<String, Workflow> workflowActive = new HashMap<>();
            private Map<String, String> workflowResults = new HashMap<>();

            public static Behavior<Message> create(String name, Set<String> workflows, Set<Map.Entry<String, String>> dependencies) {
                return Behaviors.setup(context -> new Executor(name, workflows, dependencies, context));
            }

            private Executor(String name, Set<String> workflows, Set<Map.Entry<String, String>> dependencies, ActorContext<Message> context) {
                super(context);
                this.name = name;
                this.workflows = workflows;
                this.dependencies = dependencies;
            }

            @Override
            public Receive<Message> createReceive() {
                return newReceiveBuilder()
                        .onMessage(Message.Pipeline.Start.class, msg -> onPipelineStart())
                        .onMessage(Message.WorkflowMsg.Success.class, msg -> onWorkflowSuccess(msg))
                        .onMessage(Message.WorkflowMsg.Fail.class, msg -> onWorkflowFail(msg))
                        .build();
            }

            private Behavior<Message> onPipelineStart() {
                System.out.println(this + ": started");
                workflowActive = createWorkflows(this.workflows, dependencies);
                workflowActive.values().forEach(Workflow::start);
                return this;
            }

            private Behavior<Message> onWorkflowSuccess(Message.WorkflowMsg.Success msg) {
                // TODO: kill, store result, and remove from map
                workflowActive.remove(msg.getName());
                workflowResults.put(msg.getName(), msg.getResult());
                getContext().stop(msg.getExecutor());
                return this;
            }

            private Behavior<Message> onWorkflowFail(Message.WorkflowMsg.Fail msg) {
                // TODO: kill, store result, and remove from map
                workflowActive.remove(msg.getName());
                workflowResults.put(msg.getName(), msg.getResult());
                getContext().stop(msg.getExecutor());
                return this;
            }

            private Map<String, Workflow> createWorkflows(Set<String> workflows, Set<Map.Entry<String, String>> dependencies) {
                Map<String, Workflow> workflowMap = workflows.stream()
                        .map(name -> new Workflow(this.name, name, getContext()))
                        .collect(Collectors.toMap(Workflow::getName, value -> value));

                List<Map.Entry<String, String>> dependenciesInverted = dependencies.stream()
                        .map(dep -> new SimpleImmutableEntry<>(dep.getValue(), dep.getKey()))
                        .collect(Collectors.toList());

                for (String workflowName: workflows) {
                    Workflow workflow = workflowMap.get(workflowName);
                    Set<Workflow> dependsOn = dependenciesInverted.stream()
                            .filter(keyVal -> keyVal.getKey().equals(workflowName))
                            .map(keyVal -> workflowMap.get(keyVal.getValue()))
                            .collect(Collectors.toSet());
                    Set<Workflow> dependedBy = dependencies.stream()
                            .filter(keyVal -> keyVal.getKey().equals(workflowName))
                            .map(keyVal -> workflowMap.get(keyVal.getValue()))
                            .collect(Collectors.toSet());
                    workflow.dependencies(dependsOn, dependedBy);
                }
                return workflowMap;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }
}
