import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

public class WorkflowTest {
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
    public void workflowMustBeExecutedSuccessfully_noDependencyScenario() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(
                organisation,repository,commit,"build","performance", pipeline.getRef()),"performance"
        );
        workflow.tell(new Message.WorkflowMsg.Start());
//        pipeline.expectMessageClass(Message.WorkflowMsg.Success.class);
//        pipeline.expectNoMessage();
    }

    @Test
    public void workflowMustNotBeExecuted_ifDependenciesUnmet() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        TestProbe<Message> workflow1 = testKit.createTestProbe();
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation,repository,commit,"build","performance", pipeline.getRef()),"performance"
        );
        workflow2.tell(new Message.WorkflowMsg.Dependencies(new HashSet<>(Arrays.asList(workflow1.getRef())), new HashSet<>()));
        workflow2.tell(new Message.WorkflowMsg.Start());
        pipeline.expectNoMessage(Duration.ofSeconds(3));
    }

    @Test
    public void workflowMustBeExecutedSuccessfully_dependencyScenario1() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        ActorRef<Message> workflow1 = testKit.spawn(Workflow.Executor.create(
                organisation, repository, commit,"build","performance-1", pipeline.getRef()),"performance-1");
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "performance-2", pipeline.getRef()),"performance-2");
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
                organisation, repository, commit,"build","performance-1", pipeline.getRef()),"performance-1");
        ActorRef<Message> workflow2 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "performance-2", pipeline.getRef()),"performance-2");
        ActorRef<Message> workflow3 = testKit.spawn(Workflow.Executor.create(
                organisation, repository,commit, "build", "performance-3", pipeline.getRef()),"performance-3");
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


}
