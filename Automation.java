import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Automation extends AbstractBehavior<String> {
    static Behavior<String> create() {
        return Behaviors.setup(Automation::new);
    }

    private Automation(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessage(String.class, msg -> onString(msg))
                .onSignal(Terminated.class, signal -> Behaviors.stopped())
                .build();
    }

    // TODO: message should be Automation(org, repo, commit)
    private Behavior<String> onString(String msg) {
        Pipeline.Build build = new Pipeline.Build(getContext());
        build.start();
        return this;
    }
}
