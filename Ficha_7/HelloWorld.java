import jade.core.Agent;

public class HelloWorld extends Agent{
	public void setup() {
		Object[] args = getArguments();
		String arg1 = args[0].toString();
		String arg2 = args[1].toString();
		System.out.println("Hello world! Meu nome completo é "+arg1+" "+arg2);
	}
}
