import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.AbstractMap.SimpleImmutableEntry;

public class Workflow {
    private String name;
    private ActorRef<Message> executor;
    List<String> jobs_ = new ArrayList<>(Arrays.asList("run-grakn-1", "run-grakn-2", "test-performance-big")); // auxillary
    Set<String> jobs = new HashSet<>(jobs_);
    List<Map.Entry<String, String>> dependencies = Arrays.asList(
            new SimpleImmutableEntry<>(jobs_.get(0), jobs_.get(2)),
            new SimpleImmutableEntry<>(jobs_.get(1), jobs_.get(2))
    );

    Workflow(String name, ActorContext<Void> context) {
        this.name = "performance";
        this.executor = context.spawn(Executor.create(jobs, dependencies), name);
    }

    void start() {
        getExecutor().tell(new Message.Workflow.Start());
    }

    public String getName() {
        return name;
    }

    ActorRef<Message> getExecutor() {
        return executor;
    }

    static class Executor extends AbstractBehavior<Message> {
        private List<Map.Entry<String, String>> jobs; // name, status
        private List<Map.Entry<String, String>> dependencies; // x is-depended-by y

        private Set<String> nextJobs;

        static Behavior<Message> create(Set<String> jobs, List<Map.Entry<String, String>> dependencies) {
            return Behaviors.setup(context -> new Executor(jobs, dependencies, context));
        }

        private Executor(Set<String> jobs, List<Map.Entry<String, String>> dependencies, ActorContext<Message> context) {
            super(context);
            this.jobs = jobs.stream().collect();
            this.dependencies = dependencies;
            nextJobs = new HashSet<>(jobs);
            nextJobs.removeAll(dependencies.stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
        };

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Workflow.Start.class, msg -> onStart())
                    .onMessage(Message.Job.Success.class, msg -> onSuccess(msg))
                    .onMessage(Message.Job.Fail.class, msg -> onFail(msg))
                    .build();
        }

        private Behavior<Message> onStart() {
            getNextJobs().forEach(job -> new Job(job, "echo 'hello world'", getContext()));
            return this;
        }

        private Behavior<Message> onSuccess(Message.Job.Success msg) {
            completedJobs.add(new SimpleImmutableEntry<>(msg.getJob(), msg.getOutput()));
            getNextJobs().forEach(job -> new Job(job, "echo 'hello world'", getContext()));
            return this;
        }

        private Behavior<Message> onFail(Message.Job.Fail msg) {
            return this;
        }

        private Set<String> getNextJobs() {
            return nextJobs;
        }
    }
}
