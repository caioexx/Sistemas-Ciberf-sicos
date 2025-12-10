import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetResponder;

public class CNP_Spots extends Agent {
    private String spotPosition;
    private int spotPrice;
    private boolean spotOccupied;

    protected void setup() {
        Object[] args = getArguments();
        if (args==null || args.length < 2) {
            System.out.println("Spot " + getLocalName() + ": Invalid arguments. Usage: Spot(Position,Price)");
            doDelete();
            return;
        }
        
        this.spotPosition = (String) args[0];
        try {
        	this.spotPrice = Integer.parseInt((String) args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Spot " + getLocalName() + ": Invalid price.");
            doDelete();
            return;
        }
        this.spotOccupied = false;

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("parking-spot");
        sd.setName(spotPosition);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);

        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp){
                ACLMessage reply = cfp.createReply();
                if (!spotOccupied) {
                	reply.setPerformative(ACLMessage.PROPOSE);
                	reply.setContent(String.valueOf(spotPrice));
                } else {
                	reply.setPerformative(ACLMessage.REFUSE);
                }
                return reply;
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            	ACLMessage reply = accept.createReply();
            	if (!spotOccupied) {
	                reply.setPerformative(ACLMessage.INFORM);
	                reply.setContent(spotPosition+";;"+String.valueOf(spotPrice));
	                spotOccupied = true;
            	}
            	else {
	                reply.setPerformative(ACLMessage.REFUSE);
	                System.out.println("REFUSING");
            	}
            	return reply;
            }

        });
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}