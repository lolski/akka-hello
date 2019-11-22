import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Job {
    public static class Description {
        private String job;
        private String script;
        private long timeoutSec;
        private ActorRef<Message> workflowRef;

        Description(String job, String script, long timeoutSec, ActorRef<Message> workflowRef) {
            this.job = job;
            this.script = script;
            this.timeoutSec = timeoutSec;
            this.workflowRef = workflowRef;
        }

        public String getJob() {
            return job;
        }

        public String getScript() {
            return script;
        }

        public long getTimeoutSec() {
            return timeoutSec;
        }

        public ActorRef<Message> getWorkflowRef() {
            return workflowRef;
        }
    }

    public static class Executor extends AbstractBehavior<Message> {
        private Description description;

        static Behavior<Message> create(Description desc) {
            return Behaviors.setup(context -> new Job.Executor(desc, context));
        }

        private Executor(Description description, ActorContext<Message> context) {
            super(context);
            this.description = description;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.JobMsg.Start.class, msg -> onJobStart(msg))
                    .build();
        }

        private Behavior<Message> onJobStart(Message.JobMsg.Start msg) {
            try {
                ProcessResult out = new ProcessExecutor()
                        .command(description.getScript().split(" "))
                        .readOutput(true)
                        .timeout(description.getTimeoutSec(), TimeUnit.SECONDS)
                        .execute();
                if (out.getExitValue() == 0) {
                    description.getWorkflowRef().tell(new Message.JobMsg.Success(getContext().getSelf(), out.getOutput().getUTF8()));
                }
                else {
                    description.getWorkflowRef().tell(new Message.JobMsg.Fail(getContext().getSelf(), out.getOutput().getUTF8()));
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                description.getWorkflowRef().tell(new Message.JobMsg.Fail(getContext().getSelf(), e.getMessage()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                description.getWorkflowRef().tell(new Message.JobMsg.Fail(getContext().getSelf(), e.getMessage()));
            }
            return this;
        }
    }
}
