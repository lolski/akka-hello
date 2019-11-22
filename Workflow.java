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

class Workflow {
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

    public static class Executor extends AbstractBehavior<Workflow.Executor.Message> {
        private Description description;
        private final ActorRef<Pipeline.Executor.Message> pipelineRef;

        private Set<Job.Description> remaining;
        private Set<Job.Description> executing;
        private Set<Job.Description> succeeded;
        private Set<Job.Description> failed;
        private String analysis = "{analysis result placeholder}";

        static Behavior<Message> create(Description description, ActorRef<Pipeline.Executor.Message> pipelineRef) {
            return Behaviors.setup(context -> new Executor(description, pipelineRef, context));
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Start.class, msg -> onWorkflowStart(msg))
                    .onMessage(Message.Job_.Success.class, msg -> onJobSuccess(msg))
                    .onMessage(Message.Job_.Fail.class, msg -> onJobFail(msg))
                    .build();
        }

        private Executor(Description description, ActorRef<Pipeline.Executor.Message> pipelineRef, ActorContext<Message> context) {
            super(context);
            this.description = description;
            this.pipelineRef = pipelineRef;

            this.remaining = new HashSet<>(description.getJobs().keySet());
            this.executing = new HashSet<>();
            this.succeeded = new HashSet<>();
            this.failed = new HashSet<>();
        }

        private Behavior<Message> onWorkflowStart(Message.Start msg) {
            Set<Job.Description> execute = getJobsWithSuccessfulDeps();
            for (Job.Description job: execute) {
                ActorRef<Job.Executor.Message> executor = getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                executor.tell(new Job.Executor.Message.Start());
                remaining.remove(job);
                executing.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Behavior<Message> onJobSuccess(Message.Job_.Success msg) {
            executing.remove(msg.getDescription());
            succeeded.add(msg.getDescription());
            Set<Job.Description> execute = getJobsWithSuccessfulDeps();
            for (Job.Description job: execute) {
                ActorRef<Job.Executor.Message> executor = getContext().spawn(Job.Executor.create(job, getContext().getSelf()), job.getJob());
                executor.tell(new Job.Executor.Message.Start());
                remaining.remove(job);
                executing.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Behavior<Message> onJobFail(Message.Job_.Fail msg) {
            executing.remove(msg.getDescription());
            failed.add(msg.getDescription());
            Set<Job.Description> bar = getJobsWithFailedDeps();
            for (Job.Description job: bar) {
                getContext().getSelf().tell(new Message.Job_.Fail(job, null, ""));
                remaining.remove(job);
                failed.add(job);
            }
            if (remaining.isEmpty() && executing.isEmpty()) {
                notifyAndStop();
            }
            return this;
        }

        private Set<Job.Description> getJobsWithSuccessfulDeps() {
            Set<Job.Description> jobs = new HashSet<>();
            for (Job.Description job: remaining) {
                Set<Job.Description> deps = description.getJobs().get(job);
                if (deps.isEmpty() || succeeded.containsAll(deps)) {
                    jobs.add(job);
                }
            }
            return jobs;
        }

        private Set<Job.Description> getJobsWithFailedDeps() {
            Set<Job.Description> jobs = new HashSet<>();
            for (Job.Description job: remaining) {
                Set<Job.Description> deps = description.getJobs().get(job);
                boolean depsFailed = failed.stream().anyMatch(f -> deps.contains(f));
                if (depsFailed) {
                    jobs.add(job);
                }
            }
            return jobs;
        }

        private void notifyAndStop() {
            if (failed.isEmpty()) {
                pipelineRef.tell(new Pipeline.Executor.Message.Workflow_.Success(description, getContext().getSelf(), analysis));
            }
            else {
                pipelineRef.tell(new Pipeline.Executor.Message.Workflow_.Fail(description, getContext().getSelf(), analysis));
            }
            getContext().stop(getContext().getSelf());
        }

        interface Message {
            class Start implements Message {}

            class Job_ {
                static class Success implements Message {
                    private Job.Description description;
                    private ActorRef<Job.Executor.Message> executor;
                    private String analysis;

                    Success(Job.Description description, ActorRef<Job.Executor.Message> executor, String analysis) {
                        this.description = description;
                        this.executor = executor;
                        this.analysis = analysis;
                    }

                    public Job.Description getDescription() {
                        return description;
                    }

                    ActorRef<Job.Executor.Message> getExecutor() {
                        return executor;
                    }

                    String getAnalysis() {
                        return analysis;
                    }

                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        Success success = (Success) o;
                        return Objects.equals(description, success.description) &&
                                Objects.equals(executor, success.executor) &&
                                Objects.equals(analysis, success.analysis);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(description, executor, analysis);
                    }
                }

                static class Fail implements Message {
                    private Job.Description description;
                    private ActorRef<Job.Executor.Message> executor;
                    private String analysis;

                    Fail(Job.Description description, ActorRef<Job.Executor.Message> executor, String analysis) {
                        this.description = description;
                        this.executor = executor;
                        this.analysis = analysis;
                    }

                    Job.Description getDescription() {
                        return description;
                    }

                    ActorRef<Job.Executor.Message> getExecutor() {
                        return executor;
                    }

                    String getAnalysis() {
                        return analysis;
                    }

                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        Fail fail = (Fail) o;
                        return Objects.equals(description, fail.description) &&
                                Objects.equals(executor, fail.executor) &&
                                Objects.equals(analysis, fail.analysis);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(description, executor, analysis);
                    }
                }
            }
        }
    }
}
