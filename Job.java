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
    private ActorRef<Message> executor;

    Job(String name, String script, ActorContext<Void> context) {
        this.name = name;
        this.executor = context.spawn(Executor.create(name, script), name);
    }

    public String getName() {
        return name;
    }

    ActorRef<Message> getExecutor() {
        return executor;
    }

    public void dependencies(Set<Job> dependsOn, Set<Job> dependedBy) {
        Set<ActorRef<Message>> dependsOn_ = dependsOn.stream().map(Job::getExecutor).collect(Collectors.toSet());
        Set<ActorRef<Message>> dependedBy_ = dependedBy.stream().map(Job::getExecutor).collect(Collectors.toSet());
        getExecutor().tell(new Message.Job.Dependencies(dependsOn_, dependedBy_));
    }

    public void start() {
        getExecutor().tell(new Message.Job.Start());
    }

    public static class Executor extends AbstractBehavior<Message> {
        private String name;
        private String script;
        private Set<ActorRef<Message>> dependsOn;
        private Set<ActorRef<Message>> dependedBy;

        static Behavior<Message> create(String name, String script) {
            return Behaviors.setup(context -> new Executor(name, script, context));
        }

        private Executor(String name, String script, ActorContext<Message> context) {
            super(context);
            this.name = name;
            this.script = script;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Job.Dependencies.class, msg -> onDependencies(msg))
                    .onMessage(Message.Job.Start.class, msg -> onStart())
                    .onMessage(Message.Job.Success.class, msg -> onSuccess(msg.getJob()))
                    .onMessage(Message.Job.Fail.class, msg -> onFail(msg.getJob()))
                    .build();
        }

        private Behavior<Message> onDependencies(Message.Job.Dependencies dependencies) {
            System.out.println(name + ": job dependencies declared.");
            this.dependsOn = dependencies.getDependsOn();
            this.dependedBy = dependencies.getDependedBy();
            return this;
        }

        private Behavior<Message> onStart() throws InterruptedException, TimeoutException, IOException {
            System.out.println(name + ": start");
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Message> job: dependedBy) {
                    String output = "1"; // execute(script, Arrays.asList());
                    job.tell(new Message.Job.Success(this.getContext().getSelf(), output));
                }
            }
            return this;
        }

        private Behavior<Message> onSuccess(ActorRef<Message> job) throws IOException, TimeoutException, InterruptedException {
            System.out.println(this.name + ": " + job + " succeeded. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(job);
            if (!remove) {
                throw new RuntimeException(this.name + ": Unable to remove " + job + " from the list of dependencies of job actor ");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Message> j: dependedBy) {
                    String output = "1"; // execute("", Arrays.asList());
                    j.tell(new Message.Job.Success(job, output));
                }
            }
            return this;
        }

        private Behavior<Message> onFail(ActorRef<Message> job) throws IOException, TimeoutException, InterruptedException {
            System.out.println(this.name + ": " + job + " failed. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(job);
            if (!remove) {
                throw new RuntimeException(this.name + ": Unable to remove " + job + " from the list of dependencies of job actor ");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(name + ": executed");
                for (ActorRef<Message> j: dependedBy) {
                    String output = "1"; // execute("", Arrays.asList());
                    j.tell(new Message.Job.Success(job, output));
                }
            }
            return this;
        }

        private ProcessResult execute(String script, List<String> input) throws IOException, TimeoutException, InterruptedException {
            return new ProcessExecutor().command(Paths.get("/", "tmp", name + ".sh").toAbsolutePath().toString()).execute();
        }
    }
}
