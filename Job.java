import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


public class Job {
    private String name;
    private ActorRef<Executor.Message> executor;

    Job(String name, String script, ActorContext<Void> context) {
        this.name = name;
        this.executor = context.spawn(Executor.create(name, script), name);
    }

    public String getName() {
        return name;
    }

    ActorRef<Executor.Message> getExecutor() {
        return executor;
    }

    public void dependencies(Set<Job> dependsOn, Set<Job> dependedBy) {
        Set<ActorRef<Executor.Message>> dependsOn_ = dependsOn.stream().map(Job::getExecutor).collect(Collectors.toSet());
        Set<ActorRef<Executor.Message>> dependedBy_ = dependedBy.stream().map(Job::getExecutor).collect(Collectors.toSet());
        getExecutor().tell(new Executor.Message.Dependencies(dependsOn_, dependedBy_));
    }

    public void start() {
        getExecutor().tell(new Executor.Message.Start());
    }

    public static class Executor extends AbstractBehavior<Executor.Message> {
        private String name;
        private String script;
        private Set<ActorRef<Executor.Message>> dependsOn;
        private Set<ActorRef<Executor.Message>> dependedBy;

        static Behavior<Executor.Message> create(String name, String script) {
            return Behaviors.setup(context -> new Executor(name, script, context));
        }

        private Executor(String name, String script, ActorContext<Message> context) {
            super(context);
            this.name = name;
            this.script = script;
        }

        @Override
        public Receive<Executor.Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Dependencies.class, msg -> onDependencies(msg))
                    .onMessage(Message.Start.class, msg -> onStart())
                    .onMessage(Message.Success.class, msg -> onSuccess(msg.getJob()))
                    .onMessage(Message.Fail.class, msg -> onFail(msg.getJob()))
                    .build();
        }

        private Behavior<Executor.Message> onDependencies(Message.Dependencies dependencies) {
            System.out.println(name + ": job dependencies declared.");
            this.dependsOn = dependencies.getDependsOn();
            this.dependedBy = dependencies.getDependedBy();
            return this;
        }

        private Behavior<Executor.Message> onStart() throws InterruptedException, TimeoutException, IOException {
            System.out.println(name + ": start");
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Executor.Message> job: dependedBy) {
                    String output = "1"; // execute(script, Arrays.asList());
                    job.tell(new Message.Success(this.getContext().getSelf(), output));
                }
            }
            return this;
        }

        private Behavior<Executor.Message> onSuccess(ActorRef<Message> job) throws IOException, TimeoutException, InterruptedException {
            System.out.println(this.name + ": " + job + " succeeded. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(job);
            if (!remove) {
                throw new RuntimeException(this.name + ": Unable to remove " + job + " from the list of dependencies of job actor ");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Executor.Message> j: dependedBy) {
                    String output = "1"; // execute("", Arrays.asList());
                    j.tell(new Message.Success(job, output));
                }
            }
            return this;
        }

        private Behavior<Executor.Message> onFail(ActorRef<Message> job) throws IOException, TimeoutException, InterruptedException {
            System.out.println(this.name + ": " + job + " failed. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(job);
            if (!remove) {
                throw new RuntimeException(this.name + ": Unable to remove " + job + " from the list of dependencies of job actor ");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Executor.Message> j: dependedBy) {
                    String output = "1"; // execute("", Arrays.asList());
                    j.tell(new Message.Success(job, output));
                }
            }
            return this;
        }

        private ProcessResult execute(String script, List<String> input) throws IOException, TimeoutException, InterruptedException {
            return new ProcessExecutor().command(Paths.get("/", "tmp", name + ".sh").toAbsolutePath().toString()).execute();
        }

        public interface Message {
            class Dependencies implements Message {
                private final Set<ActorRef<Message>> dependsOn;
                private final Set<ActorRef<Message>> dependedBy;

                Dependencies(Set<ActorRef<Message>> dependsOn, Set<ActorRef<Message>> dependedBy) {
                    this.dependsOn = dependsOn;
                    this.dependedBy = dependedBy;
                }

                Set<ActorRef<Message>> getDependsOn() {
                    return dependsOn;
                }

                Set<ActorRef<Message>> getDependedBy() {
                    return dependedBy;
                }
            }

            class Start implements Message {}

            class Success implements Message {
                private ActorRef<Message> job;
                private String output;

                Success(ActorRef<Message> job, String output) {
                    this.job = job;
                    this.output = output;
                }

                ActorRef<Message> getJob() {
                    return job;
                }

                public String getOutput() {
                    return output;
                }
            }

            class Fail implements Message {
                private ActorRef<Message> job;
                private String error;

                Fail(ActorRef<Message> job, String error) {
                    this.job = job;
                    this.error = error;
                }

                ActorRef<Message> getJob() {
                    return job;
                }

                public String getError() {
                    return error;
                }
            }
        }
    }
}
