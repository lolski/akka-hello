import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AutomationFactoryTest {
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
    public void automationFactoryMustExecuteSuccessfully() {
        ActorRef<Message> automationFactory = testKit.spawn(
                AutomationFactory.Executor.create("graknlabs-test", "grakn", "1234567"), "graknlabs-test-grakn-1234567");
        automationFactory.tell(new Message.AutomationFactoryMsg.Start());
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
}