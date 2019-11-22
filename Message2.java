import akka.actor.typed.ActorRef;

import java.util.Objects;
import java.util.Set;

public interface Message2 {
    class AutomationFactoryMsg {
        static class Start implements Message {}
    }

    class PipelineMsg {
        static class Start implements Message {}

        static class Success implements Message {
            private String name;
            private ActorRef<Message> pipelineExecutor;
            private String result;

            Success(String name, ActorRef<Message> pipelineExecutor, String result) {
                this.name = name;
                this.pipelineExecutor = pipelineExecutor;
                this.result = result;
            }

            public String getName() {
                return name;
            }

            public ActorRef<Message> getPipelineExecutor() {
                return pipelineExecutor;
            }

            String getResult() {
                return result;
            }
        }

        static class Fail implements Message {
            private String name;
            private ActorRef<Message> executor;
            private String result;

            Fail(String name, ActorRef<Message> executor, String result) {
                this.name = name;
                this.executor = executor;
                this.result = result;
            }

            public String getName() {
                return name;
            }

            public ActorRef<Message> getExecutor() {
                return executor;
            }

            String getResult() {
                return result;
            }
        }
    }

    class WorkflowMsg {
        static class Dependencies implements Message {
            private final Set<ActorRef<Message>> dependsOn;
            private final Set<ActorRef<Message>> dependedBy;

            Dependencies(Set<ActorRef<Message>> dependsOn, Set<ActorRef<Message>> dependedBy) {
                this.dependsOn = dependsOn;
                this.dependedBy = dependedBy;
            }

            Set<ActorRef<Message>> getDependsOn() {
                return dependsOn;
            }

            Set<ActorRef<Message>> getDependedBy() {
                return dependedBy;
            }
        }

        static class Start implements Message {}

        static class Success implements Message {
            private String name;
            private ActorRef<Message> executor;
            private String analysis;

            Success(String name, ActorRef<Message> executor, String analysis) {
                this.name = name;
                this.executor = executor;
                this.analysis = analysis;
            }

            ActorRef<Message> getExecutor() {
                return executor;
            }

            String getAnalysis() {
                return analysis;
            }

            public String getName() {
                return name;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Success success = (Success) o;
                return Objects.equals(name, success.name) &&
                        Objects.equals(executor, success.executor) &&
                        Objects.equals(analysis, success.analysis);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, executor, analysis);
            }
        }

        static class Fail implements Message {
            private String name;
            private ActorRef<Message> executor;
            private String analysis;

            Fail(String name, ActorRef<Message> executor, String analysis) {
                this.name = name;
                this.executor = executor;
                this.analysis = analysis;
            }

            public String getName() {
                return name;
            }

            ActorRef<Message> getExecutor() {
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
                return Objects.equals(name, fail.name) &&
                        Objects.equals(executor, fail.executor) &&
                        Objects.equals(analysis, fail.analysis);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, executor, analysis);
            }
        }
    }

    class JobMsg {
        static class Dependencies implements Message {
            private final Set<ActorRef<Message>> dependsOn;
            private final Set<ActorRef<Message>> dependedBy;

            Dependencies(Set<ActorRef<Message>> dependsOn, Set<ActorRef<Message>> dependedBy) {
                this.dependsOn = dependsOn;
                this.dependedBy = dependedBy;
            }

            Set<ActorRef<Message>> getDependsOn() {
                return dependsOn;
            }

            Set<ActorRef<Message>> getDependedBy() {
                return dependedBy;
            }
        }

        static class Start implements Message {}

        static class Success implements Message {
            private ActorRef<Message> job;
            private String analysis;

            Success(ActorRef<Message> job, String analysis) {
                this.job = job;
                this.analysis = analysis;
            }

            ActorRef<Message> getExecutor() {
                return job;
            }

            String getAnalysis() {
                return analysis;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Success success = (Success) o;
                return Objects.equals(job, success.job) &&
                        Objects.equals(analysis, success.analysis);
            }

            @Override
            public int hashCode() {
                return Objects.hash(job, analysis);
            }
        }

        static class Fail implements Message {
            private ActorRef<Message> job;
            private String analysis;

            Fail(ActorRef<Message> job, String analysis) {
                this.job = job;
                this.analysis = analysis;
            }

            ActorRef<Message> getJob() {
                return job;
            }

            public String getAnalysis() {
                return analysis;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Fail fail = (Fail) o;
                return Objects.equals(job, fail.job) &&
                        Objects.equals(analysis, fail.analysis);
            }

            @Override
            public int hashCode() {
                return Objects.hash(job, analysis);
            }
        }
    }
}