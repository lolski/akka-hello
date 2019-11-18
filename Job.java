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
    private Workflow workflow;
    private ActorRef<Message> executor;

    Job(String name, String script, Workflow workflow, ActorContext<Message> context) {
        this.name = name;
        this.executor = context.spawn(Executor.create(name, script, workflow.getExecutor()), name);
        this.workflow = workflow;
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

    void start() {
        getExecutor().tell(new Message.Job.Start());
    }

    public static class Executor extends AbstractBehavior<Message> {
        private String name;
        private String script;
        private ActorRef<Message> workflow;

        static Behavior<Message> create(String name, String script, ActorRef<Message> workflow) {
            return Behaviors.setup(context -> new Executor(name, script, workflow, context));
        }

        private Executor(String name, String script, ActorRef<Message> workflow, ActorContext<Message> context) {
            super(context);
            this.name = name;
            this.script = script;
            this.workflow = workflow;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Job.Start.class, msg -> onStart())
                    .build();
        }

        private Behavior<Message> onStart() throws InterruptedException, TimeoutException, IOException {
            String output = script; // execute(script, Arrays.asList());
            System.out.println(name + ": executed");
            workflow.tell(new Message.Job.Success(name, output));
            return this;
        }

        private ProcessResult execute(String script, List<String> input) throws IOException, TimeoutException, InterruptedException {
            return new ProcessExecutor().command(Paths.get("/", "tmp", name + ".sh").toAbsolutePath().toString()).execute();
        }
    }
}