package com.blyp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "planes")
public class PlanesConfig {

    private Plan user = new Plan();
    private Plan pro  = new Plan();

    public Plan getUser() { return user; }
    public Plan getPro()  { return pro; }

    public static class Plan {
        private int ticketsMes = 2;
        public int getTicketsMes()             { return ticketsMes; }
        public void setTicketsMes(int v)       { this.ticketsMes = v; }
    }

    public int getLimiteTickets(String role) {
        if ("ROLE_PRO".equals(role) || "ROLE_ADMIN".equals(role)) return pro.ticketsMes;
        return user.ticketsMes;
    }
}
