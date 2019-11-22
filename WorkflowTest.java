import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", new HashMap<>());
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Success(worflowDesc, workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_oneJob() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Success(worflowDesc, workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_twoJobs_noDeps() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(jobSuccess("run-kgms-node-1"), new HashSet<>());
        jobs.put(jobSuccess("run-kgms-node-2"), new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Success(worflowDesc, workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_twoJobs_withDeps() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Job.Description performance = jobSuccess("test-performance");
        Job.Description runKgms = jobSuccess("run-kgms-node");
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(performance, new HashSet<>(Arrays.asList(runKgms)));
        jobs.put(runKgms, new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Success(worflowDesc, workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnSuccess_threeJobs() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Job.Description performance = jobSuccess("test-performance");
        Job.Description kgmsNode = jobSuccess("run-kgms-node");
        Job.Description independentJob = jobSuccess("an-independent-job");
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(performance, new HashSet<>(Arrays.asList(kgmsNode)));
        jobs.put(kgmsNode, new HashSet<>());
        jobs.put(independentJob, new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Success(worflowDesc, workflow, "{analysis result placeholder}"));
    }


    @Test
    public void workflowMustReturnFail_oneOutOfThreeJobsFailed_scenario1() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Job.Description kgmsNode1 = jobFail("run-kgms-node-1");
        Job.Description kgmsNode2 = jobSuccess("run-kgms-node-2");
        Job.Description performance = jobSuccess("test-performance");
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(performance, new HashSet<>(Arrays.asList(kgmsNode1, kgmsNode2)));
        jobs.put(kgmsNode1, new HashSet<>());
        jobs.put(kgmsNode2, new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Fail(worflowDesc, workflow, "{analysis result placeholder}"));
    }

    @Test
    public void workflowMustReturnFail_oneOutOfThreeJobsFailed_scenario2() {
        TestProbe<Pipeline.Executor.Message> pipeline = testKit.createTestProbe();
        Job.Description kgmsNode1 = jobFail("run-kgms-node-1");
        Job.Description kgmsNode2 = jobSuccess("run-kgms-node-2");
        Job.Description performance = jobSuccess("test-performance");
        Map<Job.Description, Set<Job.Description>> jobs = new HashMap<>();
        jobs.put(performance, new HashSet<>(Arrays.asList(kgmsNode2)));
        jobs.put(kgmsNode2, new HashSet<>(Arrays.asList(kgmsNode1)));
        jobs.put(kgmsNode1, new HashSet<>());
        Workflow.Description worflowDesc = new Workflow.Description( " workflow", jobs);
        ActorRef<Workflow.Executor.Message> workflow = testKit.spawn(Workflow.Executor.create(worflowDesc, pipeline.getRef()));
        workflow.tell(new Workflow.Executor.Message.Start());
        pipeline.expectMessage(new Pipeline.Executor.Message.Workflow_.Fail(worflowDesc, workflow, "{analysis result placeholder}"));
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
