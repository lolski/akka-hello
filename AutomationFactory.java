import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class AutomationFactory {
    static class Executor extends AbstractBehavior<Message> {
        // description
        private final String organisation;
        private final String repository;
        private final String commit;
        private final Set<String> pipelines = new HashSet<>(Arrays.asList("build"));
        private Map<String, String> pipelineAnalyses = new HashMap<>();

        static Behavior<Message> create(String organisation, String repository, String commit) {
            return Behaviors.setup(context -> new Executor(organisation, repository, commit, context));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.AutomationFactory.Start.class, msg -> onAutomationFactoryStart(msg))
                    .onMessage(Message.PipelineMsg.Success.class, msg -> onPipelineSuccess(msg))
                    .onMessage(Message.PipelineMsg.Fail.class, msg -> onPipelineFail(msg))
                    .onSignal(Terminated.class, signal -> Behaviors.stopped())
                    .build();
        }

        @Override
        public String toString() {
            return organisation + "/" + repository + "@" + commit;
        }

        private Executor(String organisation, String repository, String commit, ActorContext<Message> context) {
            super(context);
            this.organisation = organisation;
            this.repository = repository;
            this.commit = commit;
        }

        private Behavior<Message> onAutomationFactoryStart(Message.AutomationFactory.Start msg) {
            // TODO: execute all pipelines, not just build
            executeAll();
            return this;
        }

        private Behavior<Message> onPipelineSuccess(Message.PipelineMsg.Success msg) {
            pipelineAnalyses.put(msg.getName(), msg.getResult());
            if (pipelineAnalyses.size() == pipelines.size()) {
                shutdown();
            }
            return this;
        }

        private Behavior<Message> onPipelineFail(Message.PipelineMsg.Fail msg) {
            // TODO
            return this;
        }

        private void executeAll() {
            System.out.println(this + ": started.");
            ActorRef<Message> build = getContext().spawn(Pipeline.Build.Executor.create(organisation, repository, commit, getContext().getSelf()), pipelines.stream().findFirst().get());
            build.tell(new Message.PipelineMsg.Start());
        }

        private void shutdown() {
            System.out.println(this + ": all pipelines have completed. terminating...");
            getContext().stop(getContext().getSelf());
        }
    }
}
