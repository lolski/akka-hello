import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;
import java.util.Set;

class Workflow {
    static class Executor extends AbstractBehavior<Message> {
        // description
        private final String organisation;
        private final String repository;
        private final String commit;
        private final String pipeline;
        private final String workflow;

        private final ActorRef<Message> pipelineRef;
        private Set<ActorRef<Message>> dependsOn = new HashSet<>();
        private Set<String> dependsOnAnalyses = new HashSet<>();
        private Set<ActorRef<Message>> dependedBy = new HashSet<>();
        private String analysis = "{analysis result placeholder}";

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
                    .onSignal(PostStop.class, this::onPostStop)
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
                workflowShutdown();
            }
            return this;
        }

        private Behavior<Message> onWorkflowSuccess(Message.WorkflowMsg.Success msg) {
            System.out.println(this + ": " + msg.getName() + " succeeded.");
            dependsOnAnalyses.add(msg.getAnalysis());
            if (dependsOnAnalyses.size() == dependsOn.size()) {
                workflowShutdown();
            }

            return this;
        }

        private Behavior<Message> onWorkflowFail(Message.WorkflowMsg.Fail msg) {
            // TODO
            return this;
        }

        private Behavior<Message> onPostStop(PostStop signal) {
            System.out.println(this + ": stopped with signal " + signal);
            return this;
        }

        private void workflowShutdown() {
            System.out.println(this + ": succeeded");
            for (ActorRef<Message> dep : dependedBy) {
                // TODO: execute(script, Arrays.asList());
                dep.tell(new Message.WorkflowMsg.Success(workflow, getContext().getSelf(), analysis));
            }
            pipelineRef.tell(new Message.WorkflowMsg.Success(workflow, getContext().getSelf(), analysis));
            getContext().stop(getContext().getSelf());
        }
    }
}