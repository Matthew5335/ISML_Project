package org.example;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AlertAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("[AlertAgent] " + getAID().getName() + " started.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if (content.contains("HEATWAVE")) {
                        System.out.println("🔥 " + content);
                    }
                    else if (content.contains("HEAVY RAIN")) {
                        System.out.println("🌧️ " + content);
                    }
                    else {
                        System.out.println("⛈️ " + content);
                    }
                }
                block();
            }
        });
    }
}