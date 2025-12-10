import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ReceiverAgent extends Agent {
	private static final long serialVersionUID = 5L;
	
    @Override
    protected void setup() {
        System.out.println("AgentCyclical started.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = receive();

                if (msg != null) {
                    System.out.println("Cyclical Agent received: " + msg.getContent());
                } else {
                    block(); // saves CPU, waits for next message
                }
            }
        });
    }
}