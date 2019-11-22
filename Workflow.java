import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.HashSet;
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

        private Set<Job.Description> remaining;
        private Set<Job.Description> executing;
        private Set<Job.Description> succeeded;
        private Set<Job.Description> failed;
        private Map<Job.Description, Set<Job.Description>> dependsOn;
        private Map<Job.Description, Set<Job.Description>> dependedBy;
        private String analysis = "{analysis result placeholder}";

        public static Behavior<Message> create(Description description, ActorRef<Message> pipelineRef) {
            return Behaviors.setup(context -> new Executor(description, pipelineRef, context));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.WorkflowMsg.Start.class, msg -> onWorkflowStart(msg))
                    .onMessage(Message.JobMsg.Success.class, msg -> onJobSuccess(msg))
                    .onMessage(Message.JobMsg.Fail.class, msg -> onJobFail(msg))
                    .build();
        }

        private Executor(Description description, ActorRef<Message> pipelineRef, ActorContext<Message> context) {
            super(context);
            this.description = description;
            this.pipelineRef = pipelineRef;

            this.remaining = new HashSet<>(description.jobs.keySet());
            this.executing = new HashSet<>();
            this.succeeded = new HashSet<>();
            this.failed = new HashSet<>();
            this.dependsOn = description.jobs;
            this.dependedBy = createDependedBy(dependsOn);
        }

        private Behavior<Message> onWorkflowStart(Message.WorkflowMsg.Start msg) {
            Set<Job.Description> execute = getJobsWithSatisfiedDeps();
            for (Job.Description job: execute) {
                getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                remaining.remove(job);
                executing.add(job);
            }
            return this;
        }

        private Behavior<Message> onJobSuccess(Message.JobMsg.Success msg) {
            Set<Job.Description> execute = getJobsWithSatisfiedDeps();
            execute.forEach(e -> getContext().spawn(Job.Executor.create(e, getContext().getSelf()), e.getJob()));
            return this;
        }

        private Behavior<Message> onJobFail(Message.JobMsg.Fail msg) {

            return this;
        }

        private Map<Job.Description, Set<Job.Description>> createDependedBy(Map<Job.Description, Set<Job.Description>> dependsOn) {
            Map<Job.Description, Set<Job.Description>> dependedBy = new HashMap<>();
            // TODO
            return dependedBy;
        }

        private Set<Job.Description> getJobsWithSatisfiedDeps() {
            Set<Job.Description> satisfied = new HashSet<>();
            for (Job.Description key: dependsOn.keySet()) {
                if (dependsOn.get(key).isEmpty()) {
                    satisfied.add(key);
                }
                else if (succeeded.containsAll(dependsOn.get(key))) {
                    satisfied.add(key);
                }
            }

            return satisfied;
        }
    }
}
