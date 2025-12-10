import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.TimeUnit;

import jade.core.AID;

public class SenderAgent extends Agent {
	private static final long serialVersionUID = 7L;
	private String message;

    @Override
    protected void setup() {
    	Object[] args = getArguments();
        if (args==null) {
            System.out.println("No message specified.");
            doDelete();
            return;
        }

        message = (String) args[0];
        
        System.out.println("AgentOneShot started at "+java.time.LocalDateTime.now().toLocalTime());

        addBehaviour(new OneShotBehaviour() {
			
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

                msg.addReceiver(new AID("AgentCyclical", AID.ISLOCALNAME));
                msg.setContent("["+java.time.LocalDateTime.now().toLocalTime()+"]: "+message);

                send(msg);
			}
		});
    }
}