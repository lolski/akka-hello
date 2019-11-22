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
    public static class Executor extends AbstractBehavior<Message> {
        private final String job;
        private final String script ;

        private final ActorRef<Message> workflowRef;
        private final long timeoutSec;
        private String analysis = "{analysis result placeholder}";

        static Behavior<Message> create(String job, String script, long timeoutSec, ActorRef<Message> workflowRef) {
            return Behaviors.setup(context -> new Job.Executor(job, script, timeoutSec, workflowRef, context));
        }

        private Executor(String job, String script, long timeoutSec, ActorRef<Message> workflowRef, ActorContext<Message> context) {
            super(context);
            this.job = job;
            this.script = script;
            this.timeoutSec = timeoutSec;
            this.workflowRef = workflowRef;
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Message.JobMsg.Start.class, msg -> onJobStart(msg))
                    .build();
        }

        private Behavior<Message> onJobStart(Message.JobMsg.Start msg) {
            try {
                ProcessResult out = new ProcessExecutor().command(script.split(" ")).readOutput(true).timeout(timeoutSec, TimeUnit.SECONDS).execute();
                if (out.getExitValue() == 0) {
                    workflowRef.tell(new Message.JobMsg.Success(getContext().getSelf(), out.getOutput().getUTF8()));
                }
                else {
                    workflowRef.tell(new Message.JobMsg.Fail(getContext().getSelf(), out.getOutput().getUTF8()));
                }
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                workflowRef.tell(new Message.JobMsg.Fail(getContext().getSelf(), e.getMessage()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                workflowRef.tell(new Message.JobMsg.Fail(getContext().getSelf(), e.getMessage()));
            }
            return this;
        }
    }
}
