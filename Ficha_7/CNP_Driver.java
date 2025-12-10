import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetInitiator;

import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;



public class CNP_Driver extends Agent {
    protected void setup() {

        // Update the list of seller agents
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                DFAgentDescription dfd = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("parking-spot");
                dfd.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, dfd);
                    
                    if (result.length > 0) {
                        myAgent.addBehaviour(new SpotNegotiator(myAgent, result));
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private class SpotNegotiator extends ContractNetInitiator {

        public SpotNegotiator(Agent a, DFAgentDescription[] sellers) {
            super(a, null);

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < sellers.length; ++i) {
                cfp.addReceiver(sellers[i].getName());
            }
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
                acceptances.add(accept);
            } else {
                System.out.println("Driver " + myAgent.getLocalName() + ": No spots found.");
            }

            // Reject other proposals
            for (Object obj : responses) {
                ACLMessage msg = (ACLMessage) obj;
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    if (bestProposal == null || !msg.getSender().equals(bestProposal.getSender())) {
                        ACLMessage reject = msg.createReply();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.add(reject);
                    }
                }
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
        	String answerString = inform.getContent();
            
            try {
            	String[] parts = answerString.split(";;\\s*");
                String spotPosition = parts[0]; 
                double spotPrice = Double.parseDouble(parts[1]);
                System.out.println("Driver " + myAgent.getLocalName() + ": Spot purchased successfully from " + inform.getSender().getLocalName() + " for €" + String.valueOf(spotPrice) + " at spot: " + spotPosition );
            } catch (Exception e) {
                e.printStackTrace();
            }
            myAgent.doDelete();
        }
    }
}