import akka.actor.typed.ActorRef;

import java.util.Objects;

public class Pipeline {
    static class Executor {
        interface Message {
            class Workflow_ {
                static class Success implements Message {
                    private Workflow.Description description;
                    private ActorRef<Workflow.Executor.Message> executor;
                    private String analysis;

                    Success(Workflow.Description description, ActorRef<Workflow.Executor.Message> executor, String analysis) {
                        this.description = description;
                        this.executor = executor;
                        this.analysis = analysis;
                    }

                    public Workflow.Description getDescription() {
                        return description;
                    }

                    ActorRef<Workflow.Executor.Message> getExecutor() {
                        return executor;
                    }

                    String getAnalysis() {
                        return analysis;
                    }

                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        Success success = (Success) o;
                        return Objects.equals(description, success.description) &&
                                Objects.equals(executor, success.executor) &&
                                Objects.equals(analysis, success.analysis);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(description, executor, analysis);
                    }
                }

                static class Fail implements Message {
                    private Workflow.Description description;
                    private ActorRef<Workflow.Executor.Message> executor;
                    private String analysis;

                    Fail(Workflow.Description description, ActorRef<Workflow.Executor.Message> executor, String analysis) {
                        this.description = description;
                        this.executor = executor;
                        this.analysis = analysis;
                    }

                    public Workflow.Description getDescription() {
                        return description;
                    }

                    ActorRef<Workflow.Executor.Message> getExecutor() {
                        return executor;
                    }

                    String getAnalysis() {
                        return analysis;
                    }

                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        Fail fail = (Fail) o;
                        return Objects.equals(description, fail.description) &&
                                Objects.equals(executor, fail.executor) &&
                                Objects.equals(analysis, fail.analysis);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(description, executor, analysis);
                    }
                }
            }
        }
    }
}
