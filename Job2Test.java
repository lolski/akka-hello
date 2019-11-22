import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class Job2Test {
    private final String organisation = "graknlabs-test";
    private final String repository = "grakn";
    private final String commit = "1234567";

    private ActorTestKit testKit;

    @Before
    public void before() {
        testKit = ActorTestKit.create();
    }

    @After
    public void after() {
        testKit.shutdownTestKit();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_noDependencies() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        Behavior<Message> jobBehavior =
                Job2.Executor.create(organisation, repository, commit, "build", "performance", "job", workflow.getRef());
        ActorRef<Message> job = testKit.spawn(jobBehavior);
        job.tell(new Message.JobMsg.Start());
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustNotBeExecuted_ifDependenciesUnmet() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        Behavior<Message> jobBehavior =
                Job2.Executor.create(organisation, repository, commit, "build", "workflow", "test-job", workflow.getRef());
        TestProbe<Message> job1 = testKit.createTestProbe();
        ActorRef<Message> job2 = testKit.spawn(jobBehavior);
        job2.tell(new Message.JobMsg.Dependencies(new HashSet<>(Arrays.asList(job1.getRef())), new HashSet<>()));
        job2.tell(new Message.JobMsg.Start());
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_dependencyScenario1() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job1 = testKit.spawn(Job2.Executor.create(
                organisation, repository, commit,"build","workflow", "test-job-1", workflow.getRef()),"test-job-1");
        ActorRef<Message> job2 = testKit.spawn(Job2.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-2", workflow.getRef()),"test-job-2");
        job1.tell(new Message.JobMsg.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(job2))));
        job2.tell(new Message.JobMsg.Dependencies(new HashSet<>(Arrays.asList(job1)), new HashSet<>()));
        job1.tell(new Message.JobMsg.Start());
        job2.tell(new Message.JobMsg.Start());
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_dependencyScenario2() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job1 = testKit.spawn(Job2.Executor.create(
                organisation, repository, commit,"build","workflow", "test-job-1", workflow.getRef()),"test-job-1");
        ActorRef<Message> job2 = testKit.spawn(Job2.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-2", workflow.getRef()),"test-job-2");
        ActorRef<Message> job3 = testKit.spawn(Job2.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-3", workflow.getRef()),"test-job-3");
        job1.tell(new Message.JobMsg.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(job2))));
        job2.tell(new Message.JobMsg.Dependencies(new HashSet<>(Arrays.asList(job1)), new HashSet<>()));
        job1.tell(new Message.JobMsg.Start());
        job2.tell(new Message.JobMsg.Start());
        job3.tell(new Message.JobMsg.Start());
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectMessageClass(Message.JobMsg.Success.class);
        workflow.expectNoMessage();
    }
}
