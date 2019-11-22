import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkflowTest {
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
    public void workflowMustReturnSuccess_emptyJobs() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", new HashMap<>());
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_oneJob() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_twoJobs_noDeps() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>());
        jobs.put(jobSuccess("run-kgms-node-2"), new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_twoJobs_withDeps() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>(Arrays.asList(jobSuccess("test-performance"))));
        jobs.put(jobSuccess("run-kgms-node-2"), new HashSet<>(Arrays.asList(jobSuccess("test-performance"))));
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_threeJobs() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>(Arrays.asList(jobSuccess("test-performance"))));
        jobs.put(jobSuccess("an-independent-job"), new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnFail_oneOutOfTwoJobsFailed() {
        TestProbe<Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>(Arrays.asList(jobSuccess("test-performance"))));
        jobs.put(jobFail("run-kgms-node-2"), new HashSet<>(Arrays.asList(jobSuccess("test-performance"))));
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()), worflowDesc.getName());
        workflow.tell(new Message.WorkflowMsg.Start());
        pipeline.expectMessage(new Message.WorkflowMsg.Success(worflowDesc.getName(), workflow, "{analysis result placeholder}"));
    }

    private Job.Description jobSuccess(String name) {
        return new Job.Description(name, "echo hello", 2);
    }

    private Job.Description jobFail(String name) {
        return new Job.Description(name, "false", 2);
    }

    private Job.Description jobTimeout(String name) {
        return new Job.Description(name, "sleep 5", 2);
    }
}
