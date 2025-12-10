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

public class CNP_Seller extends Agent {
    private String bookTitle;
    private int bookPrice;

    protected void setup() {
        Object[] args = getArguments();
        if (args==null || args.length < 2) {
            System.out.println("Seller " + getLocalName() + ": Invalid arguments. Usage: Seller(Title,Price)");
            doDelete();
            return;
        }
        
        bookTitle = (String) args[0];
        try {
            bookPrice = Integer.parseInt((String) args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Seller " + getLocalName() + ": Invalid price.");
            doDelete();
            return;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
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
                String requestedTitle = cfp.getContent();
                
                ACLMessage reply = cfp.createReply();
                if (bookTitle.equalsIgnoreCase(requestedTitle)) { //We have the book
                	reply.setPerformative(ACLMessage.PROPOSE);
                	reply.setContent(String.valueOf(bookPrice));
                } else { //We dont have the book
                	reply.setPerformative(ACLMessage.REFUSE);
                }
                return reply;
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent(String.valueOf(bookPrice));
                return inform;
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