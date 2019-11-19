import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Workflow {
    private final String pipelineName;
    private final String workflowName;
    private final Set<String> jobs = new HashSet<>();
    private final Set<Map.Entry<String, String>> dependencies = new HashSet<>();
    private ActorRef<Message> executor;

    public Workflow(String pipelineName, String workflowName, ActorContext<Message> context) {
        this.pipelineName = pipelineName;
        this.workflowName = workflowName;
        this.executor = context.spawn(Executor.create(this.pipelineName, this.workflowName), this.workflowName);
    }

    public String getName() {
        return workflowName;
    }

    public ActorRef<Message> getExecutor() {
        return executor;
    }

    public String toString() {
        return pipelineName + "/" + workflowName;
    }

    public void dependencies(Set<Workflow> dependsOn, Set<Workflow> dependedBy) {
        Set<ActorRef<Message>> dependsOn_ = dependsOn.stream().map(Workflow::getExecutor).collect(Collectors.toSet());
        Set<ActorRef<Message>> dependedBy_ = dependedBy.stream().map(Workflow::getExecutor).collect(Collectors.toSet());
        getExecutor().tell(new Message.WorkflowMsg.Dependencies(dependsOn_, dependedBy_));
    }

    public void start() {
        getExecutor().tell(new Message.WorkflowMsg.Start());
    }

    static class Executor extends AbstractBehavior<Message> {
        private final String pipelineName;
        private final String workflowName;
        private Set<ActorRef<Message>> dependsOn;
        private Set<ActorRef<Message>> dependedBys;

        static Behavior<Message> create(String pipelineName, String workflowName) {
            return Behaviors.setup(context -> new Executor(pipelineName, workflowName, context));
        }

        private Executor(String pipelineName, String workflowName, ActorContext<Message> context) {
            super(context);
            this.pipelineName = pipelineName;
            this.workflowName = workflowName;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.WorkflowMsg.Dependencies.class, msg -> onWorkflowDependencies(msg))
                    .onMessage(Message.WorkflowMsg.Start.class, msg -> onWorkflowStart(msg))
                    .onMessage(Message.WorkflowMsg.Success.class, msg -> onWorkflowSuccess(msg))
                    .onMessage(Message.WorkflowMsg.Fail.class, msg -> onWorkflowFail(msg))
                    .build();
        }

        private Behavior<Message> onWorkflowDependencies(Message.WorkflowMsg.Dependencies msg) {
            System.out.println(this + ": dependencies declared.");
            this.dependsOn = msg.getDependsOn();
            this.dependedBys = msg.getDependedBy();
            return this;
        }

        private Behavior<Message> onWorkflowStart(Message.WorkflowMsg.Start msg) {
            System.out.println(this + ": started.");
            if (dependsOn.isEmpty()) {
                System.out.println(this + ": succeeded");
                for (ActorRef<Message> dependedBy: dependedBys) {
                    String output = "1"; // execute(script, Arrays.asList());
                    dependedBy.tell(new Message.WorkflowMsg.Success(workflowName, getContext().getSelf(), output));
                }
            }
            return this;
        }

        private Behavior<Message> onWorkflowSuccess(Message.WorkflowMsg.Success msg) {
            System.out.println(this + ": " + msg.getName() + " succeeded. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(msg.getExecutor());
            if (!remove) {
                throw new RuntimeException(this + ": Unable to remove " + msg.getName() + " from the list of dependencies.");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(this + ": succeeded");
                for (ActorRef<Message> dependedBy: dependedBys) {
                    String output = "1"; // execute(script, Arrays.asList());
                    dependedBy.tell(new Message.WorkflowMsg.Success(workflowName, getContext().getSelf(), output));
                }
            }
            return this;
        }

        private Behavior<Message> onWorkflowFail(Message.WorkflowMsg.Fail msg) {
            System.out.println(this + ": " + msg.getName() + " failed. removing from the list of dependencies.");
            boolean remove = dependsOn.remove(msg.getExecutor());
            if (!remove) {
                throw new RuntimeException(this + ": Unable to remove " + msg.getName() + " from the list of dependencies.");
            }
            if (dependsOn.isEmpty()) {
                System.out.println(this + ": failed");
                for (ActorRef<Message> dependedBy: dependedBys) {
                    String output = "1"; // execute(script, Arrays.asList());
                    dependedBy.tell(new Message.WorkflowMsg.Success(workflowName, getContext().getSelf(), output));
                }
            }
            return this;
        }

        public String toString() {
            return pipelineName + "/" + workflowName;
        }
    }
}