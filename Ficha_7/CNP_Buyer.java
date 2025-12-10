import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetInitiator;

import java.util.Vector;



public class CNP_Buyer extends Agent {
    private String targetBookTitle;
    protected void setup() {
        Object[] args = getArguments();
        if (args==null) {
            System.out.println("Buyer " + getLocalName() + ": No book title specified.");
            doDelete();
            return;
        }

        targetBookTitle = (String) args[0];

        // Update the list of seller agents
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                DFAgentDescription dfd = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("book-selling");
                dfd.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, dfd);
                    
                    if (result.length > 0) {
                        myAgent.addBehaviour(new BookNegotiator(myAgent, targetBookTitle, result));
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    // --- Inner Class for Negotiation Logic ---
    private class BookNegotiator extends ContractNetInitiator {
        private String bookTitle;

        public BookNegotiator(Agent a, String title, DFAgentDescription[] sellers) {
            super(a, null);
            this.bookTitle = title;

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < sellers.length; ++i) {
                cfp.addReceiver(sellers[i].getName());
            }
            cfp.setContent(bookTitle);
            reset(cfp);
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            int bestPrice = -1;
            ACLMessage bestProposal = null;
            ACLMessage accept = null;

            // Checking if is best price
            for (Object obj : responses) {
                ACLMessage msg = (ACLMessage) obj;
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    try {
                        int price = Integer.parseInt(msg.getContent());
                        
                        if (bestProposal == null || price < bestPrice) {
                            bestPrice = price;
                            bestProposal = msg;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }

            // Accept best price
            if (bestProposal != null) {
                accept = bestProposal.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                accept.setContent(bookTitle);
                acceptances.add(accept);
            } else {
                System.out.println("Buyer " + myAgent.getLocalName() + ": No books found.");
            }

            // Reject other proposals
            for (Object obj : responses) {
                ACLMessage msg = (ACLMessage) obj;
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    if (bestProposal == null || !msg.getSender().equals(bestProposal.getSender())) {
                        ACLMessage reject = msg.createReply();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reject.setContent(bookTitle);
                        acceptances.add(reject);
                    }
                }
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Buyer " + myAgent.getLocalName() + ": Book purchased successfully from " + inform.getSender().getLocalName() + " for €" + inform.getContent());
            myAgent.doDelete();
        }
    }
}