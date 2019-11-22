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
        TestProbe<Workflow.Executor.Message> workflow = testKit.createTestProbe();
        Job.Description desc = new Job.Description("job", "echo hello", 2);
        ActorRef<Job.Executor.Message> job = testKit.spawn(Job.Executor.create(desc, workflow.getRef()));
        job.tell(new Job.Executor.Message.Start());
        workflow.expectMessage(new Workflow.Executor.Message.Job_.Success(desc, job, "hello\n"));
    }

    @Test
    public void jobMustThrowAnError() {
        TestProbe<Workflow.Executor.Message> workflow = testKit.createTestProbe();
        Job.Description desc = new Job.Description("job", "false", 2);
        ActorRef<Job.Executor.Message> job = testKit.spawn(Job.Executor.create(desc, workflow.getRef()));
        job.tell(new Job.Executor.Message.Start());
        workflow.expectMessage(new Workflow.Executor.Message.Job_.Fail(desc, job, ""));
    }

    @Test
    public void jobMustThrowAnError_2() {
        TestProbe<Workflow.Executor.Message> workflow = testKit.createTestProbe();
        Job.Description desc = new Job.Description("job", "should-fail-because-executable-does-not-exist", 2);
        ActorRef<Job.Executor.Message> job = testKit.spawn(Job.Executor.create(desc, workflow.getRef()));
        job.tell(new Job.Executor.Message.Start());
        Workflow.Executor.Message.Job_.Fail fail = workflow.expectMessageClass(Workflow.Executor.Message.Job_.Fail.class);
        assertEquals(fail.getDescription(), desc);
        assertEquals(fail.getExecutor(), job);
        assertTrue(fail.getAnalysis().startsWith("Could not execute "));
    }

    @Test
    public void jobMustThrowAnError_ifTimedOut() {
        TestProbe<Workflow.Executor.Message> workflow = testKit.createTestProbe();
        Job.Description desc = new Job.Description("job", "sleep 20", 2);
        ActorRef<Job.Executor.Message> job = testKit.spawn(Job.Executor.create(desc, workflow.getRef()));
        job.tell(new Job.Executor.Message.Start());
        Workflow.Executor.Message.Job_.Fail fail = workflow.expectMessageClass(Workflow.Executor.Message.Job_.Fail.class);
        assertEquals(fail.getDescription(), desc);
        assertEquals(fail.getExecutor(), job);
        assertTrue(fail.getAnalysis().startsWith("Timed out waiting for "));
    }
}
