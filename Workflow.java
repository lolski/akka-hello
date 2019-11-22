import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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

        Map<Job.Description, Set<Job.Description>> getJobs() {
            return jobs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Description that = (Description) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(jobs, that.jobs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, jobs);
        }
    }

    public static class Executor extends AbstractBehavior<Message> {
        private Description description;
        private final ActorRef<Message> pipelineRef;

        private Set<Job.Description> remaining;
        private Set<Job.Description> executing;
        private Set<Job.Description> succeeded;
        private Set<Job.Description> failed;
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

            this.remaining = new HashSet<>(description.getJobs().keySet());
            this.executing = new HashSet<>();
            this.succeeded = new HashSet<>();
            this.failed = new HashSet<>();
        }

        private Behavior<Message> onWorkflowStart(Message.WorkflowMsg.Start msg) {
            Set<Job.Description> execute = getJobsWithSatisfiedDeps();
            for (Job.Description job: execute) {
                ActorRef<Message> executor = getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                executor.tell(new Message.JobMsg.Start());
                remaining.remove(job);
                executing.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Behavior<Message> onJobSuccess(Message.JobMsg.Success msg) {
            executing.remove(msg.getDescription());
            succeeded.add(msg.getDescription());
            Set<Job.Description> execute = getJobsWithSatisfiedDeps();
            for (Job.Description job: execute) {
                ActorRef<Message> executor = getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                executor.tell(new Message.JobMsg.Start());
                remaining.remove(job);
                executing.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Behavior<Message> onJobFail(Message.JobMsg.Fail msg) {
            executing.remove(msg.getDescription());
            failed.add(msg.getDescription());
            Set<Job.Description> execute = getJobsWithSatisfiedDeps();
            for (Job.Description job: execute) {
                ActorRef<Message> executor = getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                executor.tell(new Message.JobMsg.Start());
                remaining.remove(job);
                executing.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Set<Job.Description> getJobsWithSatisfiedDeps() {
            Set<Job.Description> satisfied = new HashSet<>();
            for (Job.Description key: remaining) {
                if (description.getJobs().get(key).isEmpty()) {
                    satisfied.add(key);
                }
                else if (succeeded.containsAll(description.getJobs().get(key))) {
                    satisfied.add(key);
                }
            }

            return satisfied;
        }

        private Set<Job.Description> getJobsWithUnsatisfiedDeps() {
            Set<Job.Description> unsatisfied = new HashSet<>();
            for (Job.Description key: remaining) {
                if (description.getJobs().get(key).isEmpty()) {
                    unsatisfied.add(key);
                }
                else if (succeeded.containsAll(description.getJobs().get(key))) {
                    unsatisfied.add(key);
                }
            }

            return unsatisfied;
        }
        private void notifyAndStop() {
            if (failed.isEmpty()) {
                pipelineRef.tell(new Message.WorkflowMsg.Success(description, getContext().getSelf(), analysis));
            }
            else {
                pipelineRef.tell(new Message.WorkflowMsg.Fail(description, getContext().getSelf(), analysis));
            }
            getContext().stop(getContext().getSelf());
        }
    }
}
