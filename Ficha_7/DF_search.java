import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import java.util.Iterator;

public class DF_search extends Agent {
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();

        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling"); 
        dfd.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, dfd);

            for (int i = 0; i < result.length; i++) {
                String out = result[i].getName().getLocalName() + " provê";
                Iterator iter = result[i].getAllServices();
                
                while (iter.hasNext()) {
                    ServiceDescription SD = (ServiceDescription) iter.next();
                    out += " " + SD.getName();
                }
                
                System.out.println(out);
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}