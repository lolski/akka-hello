import akka.actor.typed.ActorRef;

import java.util.Set;

public interface Message {
    interface Workflow {
        class Start implements Message {}
    }

    interface Job {
        class Dependencies implements Message {
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

        class Start implements Message {}

        class Success implements Message {
            private String jobName;
            private String output;

            Success(String jobName, String output) {
                this.jobName = jobName;
                this.output = output;
            }

            String getJobName() {
                return jobName;
            }

            public String getOutput() {
                return output;
            }
        }

        class Fail implements Message {
            private String jobName;
            private String error;

            Fail(String jobName, String failure) {
                this.jobName = jobName;
                this.error = failure;
            }

            String getJobName() {
                return jobName;
            }

            public String getFailure() {
                return error;
            }
        }
    }
}
