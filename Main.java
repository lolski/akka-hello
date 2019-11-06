import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        ActorSystem<String> main = ActorSystem.create(Actor.create(), "main");
    }
}