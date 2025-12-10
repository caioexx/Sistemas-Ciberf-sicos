import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class DF_register extends Agent{    
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book");
        dfd.addServices(sd);

        sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("TEST-book");
        dfd.addServices(sd);
        
        sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("RANDOM-book");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    protected void takeDown()
    {
        try{
            DFService.deregister(this);
        }
        catch(FIPAException e) {
            e.printStackTrace();
        }
    }
}
