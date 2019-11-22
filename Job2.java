import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;
import java.util.Set;

class Job2 {
    public static class Executor extends AbstractBehavior<Message> {
        private String organisation;
        private final String repository;
        private final String commit;
        private final String pipeline;
        private final String workflow;
        private final String job;
        private final String script = "echo hello";

        private final ActorRef<Message> workflowRef;
        private Set<ActorRef<Message>> dependsOn = new HashSet<>();
        private Set<String> dependsOnAnalyses = new HashSet<>();
        private Set<ActorRef<Message>> dependedBy = new HashSet<>();
        private String analysis = "{analysis result placeholder}";

        static Behavior<Message> create(String organisation, String repository, String commit, String pipeline, String workflow, String job, ActorRef<Message> workflowRef) {
            return Behaviors.setup(context -> new Executor(organisation, repository, commit, pipeline, workflow, job, context, workflowRef));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.JobMsg.Dependencies.class, msg -> onJobDependencies(msg))
                    .onMessage(Message.JobMsg.Start.class, msg -> onJobStart(msg))
                    .onMessage(Message.JobMsg.Success.class, msg -> onJobSuccess(msg))
                    .onMessage(Message.JobMsg.Fail.class, msg -> onJobFail(msg))
                    .onSignal(PostStop.class, signal -> onPostStop(signal))
                    .build();
        }

        @Override
        public String toString() {
            return organisation + "/" + repository + "@" + commit + "/" + pipeline + "/" + workflow + "/" + job;
        }

        private Executor(String organisation, String repository, String commit, String pipeline, String workflow, String job, ActorContext<Message> context, ActorRef<Message> workflowRef) {
            super(context);
            this.organisation = organisation;
            this.repository = repository;
            this.commit = commit;
            this.pipeline = pipeline;
            this.workflow = workflow;
            this.job = job;
            this.workflowRef = workflowRef;
        }

        private Behavior<Message> onJobDependencies(Message.JobMsg.Dependencies msg) {
            System.out.println(this + ": dependencies declared.");
            dependsOn = msg.getDependsOn();
            dependedBy = msg.getDependedBy();
            return this;
        }

        private Behavior<Message> onJobStart(Message.JobMsg.Start msg) {
            System.out.println(this + ": started.");
            executeAll();
            if (dependsOnAnalyses.size() == dependsOn.size()) {
                notifyAndShutdown();
            }
            return this;
        }

        private Behavior<Message> onJobSuccess(Message.JobMsg.Success msg) {
            System.out.println(this + ": " + msg.getExecutor().path().name() + " succeeded.");
            dependsOnAnalyses.add(msg.getAnalysis());
            if (dependsOnAnalyses.size() == dependsOn.size()) {
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

        }

        private void notifyAndShutdown() {
            System.out.println(this + ": succeeded.");
            for (ActorRef<Message> dep: dependedBy) {
                dep.tell(new Message.JobMsg.Success(getContext().getSelf(), analysis));
            }
            workflowRef.tell(new Message.JobMsg.Success(getContext().getSelf(), analysis));
            getContext().stop(getContext().getSelf());
        }
    }
}