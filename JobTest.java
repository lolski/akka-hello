import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobTest {
    private ActorTestKit testKit;

    @Before
    public void before() {
        testKit = ActorTestKit.create();
    }

    @After
    public void after() {
        testKit.shutdownTestKit();
    }

    // TODO: test full fledged, multi-line script

    @Test
    public void jobMustReturnSuccess() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job = testKit.spawn(Job.Executor.create("job", "echo hello", 2, workflow.getRef()));
        job.tell(new Message.JobMsg.Start());
        workflow.expectMessage(new Message.JobMsg.Success(job, "hello\n"));
    }

    @Test
    public void jobMustThrowAnError() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job = testKit.spawn(Job.Executor.create("job", "false", 2, workflow.getRef()));
        job.tell(new Message.JobMsg.Start());
        workflow.expectMessage(new Message.JobMsg.Fail(job, ""));
    }

    @Test
    public void jobMustThrowAnError_2() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job = testKit.spawn(Job.Executor.create("job", "should-fail-because-executable-does-not-exist", 2, workflow.getRef()));
        job.tell(new Message.JobMsg.Start());
        Message.JobMsg.Fail fail = workflow.expectMessageClass(Message.JobMsg.Fail.class);
        assertEquals(fail.getJob(), job);
        assertTrue(fail.getAnalysis().startsWith("Could not execute "));
    }

    @Test
    public void jobMustThrowAnError_ifTimedOut() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job = testKit.spawn(Job.Executor.create("job", "sleep 20", 2, workflow.getRef()));
        job.tell(new Message.JobMsg.Start());
        Message.JobMsg.Fail fail = workflow.expectMessageClass(Message.JobMsg.Fail.class);
        assertEquals(fail.getJob(), job);
        assertTrue(fail.getAnalysis().startsWith("Timed out waiting for "));
    }
}
