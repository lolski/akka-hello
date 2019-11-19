import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        ActorSystem<String> automation = ActorSystem.create(Automation.create(), "automation");
        automation.tell("");
    }
}

