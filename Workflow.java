import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Workflow {
    public static class Description {
        private String name;
        private Map<Job.Description, Set<Job.Description>> jobs;

        Description(String name, Map<Job.Description, Set<Job.Description>> jobs) {
            this.name = name;
            this.jobs = jobs;
        }

        public String getName() {
            return name;
        }

        public Map<Job.Description, Set<Job.Description>> getJobs() {
            return jobs;
        }
    }

    public static class Executor extends AbstractBehavior<Message> {
        private Description description;
        private final ActorRef<Message> pipelineRef;

        private Set<String> remaining;
        private Set<String> executing;
        private Set<String> succeeded;
        private Set<String> failed;
        private Map<String, Set<String>> dependsOn;
        private Map<String, Set<String>> dependedBy;
        private String analysis = "{analysis result placeholder}";

        public static Behavior<Message> create(Description description, ActorRef<Message> pipelineRef) {
            return Behaviors.setup(context -> new Executor(description, pipelineRef, context));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.WorkflowMsg.Start.class, msg -> onWorkflowStart(msg))
                    .build();
        }

        private Executor(Description description, ActorRef<Message> pipelineRef, ActorContext<Message> context) {
            super(context);
            this.description = description;
            this.pipelineRef = pipelineRef;
        }

        private Behavior<Message> onWorkflowStart(Message.WorkflowMsg.Start msg) {
            return this;
        }
    }
}
