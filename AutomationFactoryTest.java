import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

public class AutomationFactoryTest {
    private final String organisation = "graknlabs-test";
    private final String repository = "grakn";
    private final String commit = "1234567";

    @Rule public TestName testName = new TestName();
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
    public void automationFactoryMustExecuteSuccessfully() {
        ActorRef<Message> automationFactory = testKit.spawn(
                AutomationFactory.Executor.create("graknlabs-test", "grakn", "1234567"), "graknlabs-test-grakn-1234567");
        automationFactory.tell(new Message.AutomationFactory.Start());
    }

    @Test
    public void pipelineMustExecuteSuccessfully() {
        TestProbe<Message> automationFactory = testKit.createTestProbe();
        ActorRef<Message> pipeline = testKit.spawn(
                Pipeline.Build.Executor.create(organisation, repository, commit, automationFactory.getRef()), "build");
        pipeline.tell(new Message.PipelineMsg.Start());
        automationFactory.expectMessageClass(Message.PipelineMsg.Success.class);
        automationFactory.expectNoMessage();
    }

    @Test
    public void workflowMustBeExecutedSuccessfully_noDependencyScenario() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(
                organisation,repository,commit,"build","test-workflow", pipeline.getRef()),"test-workflow"
        );
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectNoMessage();
    }

    @Test
    public void workflowMustNotBeExecuted_ifDependenciesUnmet() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        TestProbe<Message> workflow1 = testKit.createTestProbe();
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation,repository,commit,"build","test-workflow", pipeline.getRef()),"test-workflow"
        );
        workflow2.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(Arrays.asList(workflow1.getRef())), new HashSet<>()));
        workflow2.tell(new Message.WorkflowMsg.Start());
        pipeline.expectNoMessage(Duration.ofSeconds(3));
    }

    @Test
    public void workflowMustBeExecutedSuccessfully_dependencyScenario1() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        ActorRef<Message> workflow1 = testKit.spawn(Workflow.Executor.create(
                organisation, repository, commit,"build","test-workflow-1", pipeline.getRef()),"test-workflow-1");
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "test-workflow-2", pipeline.getRef()),"test-workflow-2");
        workflow1.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(workflow2))));
        workflow2.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(Arrays.asList(workflow1)), new HashSet<>()));
        workflow1.tell(new Message.WorkflowMsg.Start());
        workflow2.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectNoMessage();
    }

    @Test
    public void workflowMustBeExecutedSuccessfully_dependencyScenario2() {
        TestProbe<Message> pipeline = testKit.createTestProbe();

        ActorRef<Message> workflow1 = testKit.spawn(Workflow.Executor.create(
                organisation, repository, commit,"build","test-workflow-1", pipeline.getRef()),"test-workflow-1");
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "test-workflow-2", pipeline.getRef()),"test-workflow-2");
        ActorRef<Message> workflow3 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "test-workflow-3", pipeline.getRef()),"test-workflow-3");
        workflow1.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(workflow2))));
        workflow2.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(Arrays.asList(workflow1)), new HashSet<>()));
        workflow1.tell(new Message.WorkflowMsg.Start());
        workflow2.tell(new Message.WorkflowMsg.Start());
        workflow3.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
        pipeline.expectNoMessage();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_noDependencies() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        Behavior<Message> jobBehavior =
                Job.Executor.create(organisation, repository, commit, "build", "test-workflow", "job", workflow.getRef());
        ActorRef<Message> job = testKit.spawn(jobBehavior);
        job.tell(new Message.Job.Start());
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustNotBeExecuted_ifDependenciesUnmet() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        Behavior<Message> jobBehavior =
                Job.Executor.create(organisation, repository, commit, "build", "workflow", "test-job", workflow.getRef());
        TestProbe<Message> job1 = testKit.createTestProbe();
        ActorRef<Message> job2 = testKit.spawn(jobBehavior);
        job2.tell(new Message.Job.Dependencies(new HashSet<>(Arrays.asList(job1.getRef())), new HashSet<>()));
        job2.tell(new Message.Job.Start());
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_dependencyScenario1() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job1 = testKit.spawn(Job.Executor.create(
                organisation, repository, commit,"build","workflow", "test-job-1", workflow.getRef()),"test-job-1");
        ActorRef<Message> job2 = testKit.spawn(Job.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-2", workflow.getRef()),"test-job-2");
        job1.tell(new Message.Job.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(job2))));
        job2.tell(new Message.Job.Dependencies(new HashSet<>(Arrays.asList(job1)), new HashSet<>()));
        job1.tell(new Message.Job.Start());
        job2.tell(new Message.Job.Start());
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectNoMessage();
    }

    @Test
    public void jobMustBeExecutedSuccessfully_dependencyScenario2() {
        TestProbe<Message> workflow = testKit.createTestProbe();
        ActorRef<Message> job1 = testKit.spawn(Job.Executor.create(
                organisation, repository, commit,"build","workflow", "test-job-1", workflow.getRef()),"test-job-1");
        ActorRef<Message> job2 = testKit.spawn(Job.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-2", workflow.getRef()),"test-job-2");
        ActorRef<Message> job3 = testKit.spawn(Job.Executor.create(
                organisation, repository,commit, "build", "workflow", "test-job-3", workflow.getRef()),"test-job-3");
        job1.tell(new Message.Job.Dependencies(new HashSet<>(), new HashSet<>(Arrays.asList(job2))));
        job2.tell(new Message.Job.Dependencies(new HashSet<>(Arrays.asList(job1)), new HashSet<>()));
        job1.tell(new Message.Job.Start());
        job2.tell(new Message.Job.Start());
        job3.tell(new Message.Job.Start());
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectMessageClass(Message.Job.Success.class);
        workflow.expectNoMessage();
    }
}