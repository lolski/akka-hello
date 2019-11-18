import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ActorSystem<Void> automation = ActorSystem.create(automation(), "automation");
        automation.getWhenTerminated().toCompletableFuture().get();
    }

    public static Behavior<Void> automation() {
        return Behaviors.setup(
                context -> {
                    Workflow performance = new Workflow("performance", context);
                    context.watch(performance.getExecutor());
                    performance.start();
                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }
}