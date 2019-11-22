import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class Workflow2 {
    static class Executor extends AbstractBehavior<Message> {
        // description
        private final String organisation;
        private final String repository;
        private final String commit;
        private final String pipeline;
        private final String workflow;
        private final Set<String> jobs = new HashSet<>(Arrays.asList("test-performance-big", "run-grakn-1", "run-grakn-2"));
        private final Set<Map.Entry<String, String>> dependencies = new HashSet<>(Arrays.asList(
                new SimpleImmutableEntry<>("run-grakn-1", "test-performance-big"),
                new SimpleImmutableEntry<>("run-grakn-2", "test-performance-big")
        ));

        private final ActorRef<Message> pipelineRef;
        private Set<ActorRef<Message>> dependsOn = new HashSet<>();
        private Set<String> dependsOnAnalyses = new HashSet<>();
        private Set<ActorRef<Message>> dependedBy = new HashSet<>();
        private String analysis = "{analysis result placeholder}";

        private Map<String, ActorRef<Message>> jobActive = new HashMap<>();
        private Map<String, String> jobAnalyses = new HashMap<>();

        static Behavior<Message> create(String organisation, String repository, String commit, String pipelineName, String workflowName, ActorRef<Message> pipeline) {
            return Behaviors.setup(context -> new Executor(organisation, repository, commit, pipelineName, workflowName, context, pipeline));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.WorkflowMsg.Dependencies.class, msg -> onWorkflowDependencies(msg))
                    .onMessage(Message.WorkflowMsg.Start.class, msg -> onWorkflowStart(msg))
                    .onMessage(Message.WorkflowMsg.Success.class, msg -> onWorkflowSuccess(msg))
                    .onMessage(Message.WorkflowMsg.Fail.class, msg -> onWorkflowFail(msg))
                    .onMessage(Message.JobMsg.Success.class, msg -> onJobSuccess(msg))
                    .onMessage(Message.JobMsg.Fail.class, msg -> onJobFail(msg))
                    .onSignal(PostStop.class, signal -> onPostStop(signal))
                    .build();
        }

        @Override
        public String toString() {
            return organisation + "/" + repository + "@" + commit + "/" + pipeline + "/" + workflow;
        }

        private Executor(String organisation, String repository, String commit, String pipeline, String workflow, ActorContext<Message> context, ActorRef<Message> pipelineRef) {
            super(context);
            this.organisation = organisation;
            this.repository = repository;
            this.commit = commit;
            this.pipeline = pipeline;
            this.workflow = workflow;
            this.pipelineRef = pipelineRef;
        }

        private Behavior<Message> onWorkflowDependencies(Message.WorkflowMsg.Dependencies msg) {
            System.out.println(this + ": dependencies declared.");
            this.dependsOn = msg.getDependsOn();
            this.dependedBy = msg.getDependedBy();
            return this;
        }

        private Behavior<Message> onWorkflowStart(Message.WorkflowMsg.Start msg) {
            System.out.println(this + ": started.");
            if (dependsOnAnalyses.size() == dependsOn.size()) {
                executeAll();
            }
            return this;
        }

        private Behavior<Message> onWorkflowSuccess(Message.WorkflowMsg.Success msg) {
            System.out.println(this + ": " + msg.getDescription() + " succeeded.");
            dependsOnAnalyses.add(msg.getAnalysis());
            if (dependsOnAnalyses.size() == dependsOn.size()) {
                executeAll();
            }

            return this;
        }

        private Behavior<Message> onWorkflowFail(Message.WorkflowMsg.Fail msg) {
            // TODO
            return this;
        }

        private Behavior<Message> onJobSuccess(Message.JobMsg.Success msg) {
            System.out.println(this + ": job '" + msg.getExecutor().path().name() + "' succeeded.");
            jobAnalyses.put(msg.getExecutor().path().name(), msg.getAnalysis());
            if (jobAnalyses.size() == jobs.size()) {
                notifyAndShutdown();
            }
            return this;
        }

        private Behavior<Message> onJobFail(Message.JobMsg.Fail msg) {
            // TODO
            return this;
        }

        private Behavior<Message> onPostStop(PostStop signal) {
            System.out.println(this + ": stopped with signal " + signal);
            return this;
        }

        private void executeAll() {
            System.out.println(this + ": dependencies met. executing jobs...");
            jobActive = createJobs(jobs, dependencies);
            jobActive.values().forEach(e -> e.tell(new Message.JobMsg.Start()));
        }

        private void notifyAndShutdown() {
            System.out.println(this + ": succeeded");
            for (ActorRef<Message> dep : dependedBy) {
                // TODO: execute(script, Arrays.asList());
                dep.tell(new Message.WorkflowMsg.Success(workflow, getContext().getSelf(), analysis));
            }
            pipelineRef.tell(new Message.WorkflowMsg.Success(workflow, getContext().getSelf(), analysis));
            getContext().stop(getContext().getSelf());
        }

        private Map<String, ActorRef<Message>> createJobs(Set<String> jobs, Set<Map.Entry<String, String>> dependencies) {
            Map<String, ActorRef<Message>> jobMap = new HashMap<>();
            for (String job: jobs) {
                jobMap.put(job, getContext().spawn(Job2.Executor.create(organisation, repository, commit, pipeline, workflow, job, getContext().getSelf()), job));
            }

            List<Map.Entry<String, String>> dependenciesInverted = dependencies.stream()
                    .map(dep -> new SimpleImmutableEntry<>(dep.getValue(), dep.getKey()))
                    .collect(Collectors.toList());

            for (String jobName: jobs) {
                ActorRef<Message> job = jobMap.get(jobName);
                Set<ActorRef<Message>> dependsOn = dependenciesInverted.stream()
                        .filter(keyVal -> keyVal.getKey().equals(jobName))
                        .map(keyVal -> jobMap.get(keyVal.getValue()))
                        .collect(Collectors.toSet());
                Set<ActorRef<Message>> dependedBy = dependencies.stream()
                        .filter(keyVal -> keyVal.getKey().equals(jobName))
                        .map(keyVal -> jobMap.get(keyVal.getValue()))
                        .collect(Collectors.toSet());
                job.tell(new Message.JobMsg.Dependencies(dependsOn, dependedBy));
            }

            return jobMap;
        }
    }
}