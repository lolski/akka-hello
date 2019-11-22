import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.AbstractMap.SimpleImmutableEntry;

interface Pipeline2 {
    class Build implements Pipeline2 {
        static class Executor extends AbstractBehavior<Message> {
            // description
            private final String organisation;
            private final String repository;
            private final String commit;
            private final String name = "build";
            private final Set<String> workflows = new HashSet<>(Arrays.asList("correctness", "performance"));
            private final Set<Map.Entry<String, String>> dependencies = new HashSet<>(Arrays.asList(
                    new SimpleImmutableEntry<>("correctness", "performance")
            ));

            private ActorRef<Message> automationFactory;
            private Map<String, ActorRef<Message>> workflowActive = new HashMap<>();
            private Map<String, String> workflowAnalyses = new HashMap<>();

            static Behavior<Message> create(String organisation, String repository, String commit, ActorRef<Message> automationFactory) {
                return Behaviors.setup(context -> new Executor(organisation, repository, commit, context, automationFactory));
            }

            @Override
            public Receive<Message> createReceive() {
                return newReceiveBuilder()
                        .onMessage(Message.PipelineMsg.Start.class, msg -> onPipelineStart(msg))
                        .onMessage(Message.WorkflowMsg.Success.class, msg -> onWorkflowSuccess(msg))
                        .onMessage(Message.WorkflowMsg.Fail.class, msg -> onWorkflowFail(msg))
                        .build();
            }

            @Override
            public String toString() {
                return organisation + "/" + repository + "@" + commit + "/" + name;
            }

            private Executor(String organisation, String repository, String commit, ActorContext<Message> context, ActorRef<Message> automationFactory) {
                super(context);
                this.organisation = organisation;
                this.repository = repository;
                this.commit = commit;
                this.automationFactory = automationFactory;
            }

            private Behavior<Message> onPipelineStart(Message.PipelineMsg.Start msg) {
                System.out.println(this + ": started.");
                executeAll();
                if (workflowAnalyses.size() == workflows.size()) {
                    notifyAndShutdown();
                }
                return this;
            }

            private Behavior<Message> onWorkflowSuccess(Message.WorkflowMsg.Success msg) {
                workflowAnalyses.put(msg.getDescription(), msg.getAnalysis());
                if (workflowAnalyses.size() == workflows.size()) {
                    notifyAndShutdown();
                }
                return this;
            }

            private Behavior<Message> onWorkflowFail(Message.WorkflowMsg.Fail msg) {
                // TODO
                return this;
            }

            private void executeAll() {
                workflowActive = createWorkflows(this.workflows, dependencies);
                workflowActive.values().forEach(e -> e.tell(new Message.WorkflowMsg.Start()));
            }

            private void notifyAndShutdown() {
                System.out.println(this + ": all workflows have completed. terminating...");
                automationFactory.tell(new Message.PipelineMsg.Success(name, getContext().getSelf(), "1"));
                getContext().stop(getContext().getSelf());
            }

            private Map<String, ActorRef<Message>> createWorkflows(Set<String> workflows, Set<Map.Entry<String, String>> dependencies) {
                Map<String, ActorRef<Message>> workflowMap = new HashMap<>();
                for (String workflow: workflows) {
                    workflowMap.put(workflow, getContext().spawn(Workflow2.Executor.create(organisation, repository, commit, this.name, workflow, getContext().getSelf()), workflow));
                }

                List<Map.Entry<String, String>> dependenciesInverted = dependencies.stream()
                        .map(dep -> new SimpleImmutableEntry<>(dep.getValue(), dep.getKey()))
                        .collect(Collectors.toList());

                for (String workflowName: workflows) {
                    ActorRef<Message> workflow = workflowMap.get(workflowName);
                    Set<ActorRef<Message>> dependsOn = dependenciesInverted.stream()
                            .filter(keyVal -> keyVal.getKey().equals(workflowName))
                            .map(keyVal -> workflowMap.get(keyVal.getValue()))
                            .collect(Collectors.toSet());
                    Set<ActorRef<Message>> dependedBy = dependencies.stream()
                            .filter(keyVal -> keyVal.getKey().equals(workflowName))
                            .map(keyVal -> workflowMap.get(keyVal.getValue()))
                            .collect(Collectors.toSet());
                    workflow.tell(new Message.WorkflowMsg.Dependencies(dependsOn, dependedBy));
                }
                return workflowMap;
            }
        }
    }
}
