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
            private ActorRef<Message> job;
            private String output;

            Success(ActorRef<Message> job, String output) {
                this.job = job;
                this.output = output;
            }

            ActorRef<Message> getJob() {
                return job;
            }

            public String getOutput() {
                return output;
            }
        }

        class Fail implements Message {
            private ActorRef<Message> job;
            private String error;

            Fail(ActorRef<Message> job, String error) {
                this.job = job;
                this.error = error;
            }

            ActorRef<Message> getJob() {
                return job;
            }

            public String getError() {
                return error;
            }
        }
    }
}
