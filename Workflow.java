import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleImmutableEntry;

public class Workflow {
    private String name;
    private Map<String, Set<String>> dependsOn;
    private Map<String, Set<String>> dependedBy;
    private ActorRef<Message> executor;

    Workflow(String name, ActorContext<Void> context) {
        this.name = "performance";
        this.dependedBy = Stream.<Map.Entry<String, Set<String>>>of(
                new SimpleImmutableEntry<>("run-grakn-1", new HashSet<>(Arrays.asList("test-performance-big"))),
                new SimpleImmutableEntry<>("run-grakn-2", new HashSet<>(Arrays.asList("test-performance-big"))),
                new SimpleImmutableEntry<>("test-performance-big", new HashSet<>())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.dependsOn = Stream.<Map.Entry<String, Set<String>>>of(
                new SimpleImmutableEntry<>("test-performance-big", new HashSet<>(Arrays.asList("run-grakn-1", "run-grakn-2"))),
                new SimpleImmutableEntry<>("run-grakn-1", new HashSet<>()),
                new SimpleImmutableEntry<>("run-grakn-2", new HashSet<>())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.executor = context.spawn(Executor.create(dependsOn, dependedBy, this), name);
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
        private Map<String, Set<String>> dependsOn;
        private Map<String, Set<String>> dependedBy;
        private Workflow workflow;
        private Map<String, Object> result;

        static Behavior<Message> create(Map<String, Set<String>> dependsOn, Map<String, Set<String>> dependedBy, Workflow workflow) {
            return Behaviors.setup(context -> new Executor(dependsOn, dependedBy, workflow, context));
        }

        private Executor(Map<String, Set<String>> dependsOn, Map<String, Set<String>> dependedBy, Workflow workflow, ActorContext<Message> context) {
            super(context);
            this.dependsOn = dependsOn;
            this.dependedBy = dependedBy;
            this.workflow = workflow;
            this.result = new HashMap<>();
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Workflow.Start.class, msg -> onStart())
                    .onMessage(Message.Job.Success.class, msg -> onSuccess(msg))
                    .onMessage(Message.Job.Fail.class, msg -> onFail(msg))
                    .build();
        }

        private Behavior<Message> onStart() {
            for (Map.Entry<String, Set<String>> runnable: dependsOn.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).collect(Collectors.toSet())) {
                new Job(runnable.getKey(), "echo 'hello world'", workflow, getContext()).start();
            }
            return this;
        }

        private Behavior<Message> onSuccess(Message.Job.Success msg) {
            Set<String> jobs = dependedBy.remove(msg.getJobName());
            jobs.forEach(job -> dependsOn.get(job).remove(msg.getJobName()));
            result.put(msg.getJobName(), msg);
            Set<Map.Entry<String, Set<String>>> collect = dependsOn.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).collect(Collectors.toSet());
            for (Map.Entry<String, Set<String>> runnable: collect) {
                new Job(runnable.getKey(), "echo 'hello world'", workflow, getContext()).start();
            }
            return this;
        }

        private Behavior<Message> onFail(Message.Job.Fail msg) {
            Set<String> jobs = dependedBy.get(msg.getJobName());
            jobs.forEach(job -> dependsOn.get(job).remove(msg.getJobName()));
            result.put(msg.getJobName(), msg);
            for (Map.Entry<String, Set<String>> runnable: dependsOn.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).collect(Collectors.toSet())) {
                new Job(runnable.getKey(), "echo 'hello world'", workflow, getContext()).start();
            }
            return this;
        }
    }
}
