package SellerBehaviours;

import java.util.List;

import Etc.BehaviourKiller;
import Etc.Book;
import Etc.BookTitle;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class WaitingForRequest extends Behaviour {

    private Agent agent;
    private boolean msgArrived = false;
    private List<Book> bookList;

    public WaitingForRequest(Agent agent, DataStore ds){
        super();
        setDataStore(ds);
        this.agent = agent;
        bookList = (List<Book>) getDataStore().get("bookList");

    }
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchProtocol("bookBuying"));
        ACLMessage request = agent.receive(mt);

        if (request!= null){
            ACLMessage answer =request.createReply();
            msgArrived = true;
            BookTitle requestedBook = BookTitle.valueOf(request.getContent());

            double offeredPrice = 0;
            for (Book book:bookList){
                if (book.getTitle() == requestedBook){
                    offeredPrice = book.getPrice();
                }
            }
            if (offeredPrice > 0) {
                answer.setContent(offeredPrice + "");
                answer.setPerformative(ACLMessage.INFORM);
                System.out.println("Agent " + agent.getLocalName()  +
                        " said:"  + "I have this book, the price is " + offeredPrice );

            }
            else{
                System.out.println("Agent "  + agent.getLocalName()  +  " said:"
                        + "I don't have this book! " );
                answer.setPerformative(ACLMessage.CANCEL);
            }
            agent.send(answer);
        }
        else{
            block();
        }

    }
    @Override
    public boolean done() {

        return msgArrived;
    }
    @Override
    public int onEnd(){
        WaitingForProposal behaviour = new WaitingForProposal(agent, getDataStore());
        agent.addBehaviour(behaviour);
        agent.addBehaviour(new BehaviourKiller(agent, 5000, behaviour));
//        agent.addBehaviour(new WaitingForProposal(agent, getDataStore()));
        return super.onEnd();
    }
}
