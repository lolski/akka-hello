import akka.actor.typed.ActorRef;

import java.util.Set;

public interface Message {
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
        private String result;

        Success(ActorRef<Message> job, String result) {
            this.job = job;
            this.result = result;
        }

        ActorRef<Message> getJob() {
            return job;
        }

        public String getResult() {
            return result;
        }
    }

    class Fail implements Message {
        private ActorRef<Message> job;
        private String result;

        Fail(ActorRef<Message> job, String result) {
            this.job = job;
            this.result = result;
        }

        ActorRef<Message> getJob() {
            return job;
        }

        public String getResult() {
            return result;
        }
    }
}