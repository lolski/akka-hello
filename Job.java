import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Job {
    public static class Description {
        private String job;
        private String script;
        private long timeoutSec;

        Description(String job, String script, long timeoutSec) {
            this.job = job;
            this.script = script;
            this.timeoutSec = timeoutSec;
        }

        public String getJob() {
            return job;
        }

        String getScript() {
            return script;
        }

        long getTimeoutSec() {
            return timeoutSec;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Description that = (Description) o;
            return timeoutSec == that.timeoutSec &&
                    Objects.equals(job, that.job) &&
                    Objects.equals(script, that.script);
        }

        @Override
        public int hashCode() {
            return Objects.hash(job, script, timeoutSec);
        }
    }

    public static class Executor extends AbstractBehavior<Job.Executor.Message> {
        private Description description;
        private final ActorRef<Workflow.Executor.Message> workflowRef;

        static Behavior<Message> create(Description desc, ActorRef<Workflow.Executor.Message> workflowRef) {
            return Behaviors.setup(context -> new Job.Executor(desc, workflowRef, context));
        }

        private Executor(Description description, ActorRef<Workflow.Executor.Message> workflowRef, ActorContext<Job.Executor.Message> context) {
            super(context);
            this.description = description;
            this.workflowRef = workflowRef;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.Start.class, msg -> onJobStart(msg))
                    .build();
        }

        private Behavior<Message> onJobStart(Message.Start msg) {
            try {
                ProcessResult out = new ProcessExecutor()
                        .command(description.getScript().split(" "))
                        .readOutput(true)
                        .timeout(description.getTimeoutSec(), TimeUnit.SECONDS)
                        .execute();
                if (out.getExitValue() == 0) {
                    workflowRef.tell(new Workflow.Executor.Message.Job_.Success(description, getContext().getSelf(), out.getOutput().getUTF8()));
                }
                else {
                    workflowRef.tell(new Workflow.Executor.Message.Job_.Fail(description, getContext().getSelf(), out.getOutput().getUTF8()));
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                workflowRef.tell(new Workflow.Executor.Message.Job_.Fail(description, getContext().getSelf(), e.getMessage()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                workflowRef.tell(new Workflow.Executor.Message.Job_.Fail(description, getContext().getSelf(), e.getMessage()));
            }
            return this;
        }

        interface Message {
            class Start implements Message {}
        }
    }
}
