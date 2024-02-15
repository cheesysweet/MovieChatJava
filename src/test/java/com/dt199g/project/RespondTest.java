package com.dt199g.project;

import com.dt199g.project.Response.Respond;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RespondTest {
    Respond respond = new Respond();
    @Test
    public void testGetResponse() {
        String question1 = "What is the plot of \"Avengers\"?";
        String expected1 = "The Avengers is about Earth's mightiest heroes must come together and learn to fight as a " +
                "team if they are going to stop the mischievous Loki and his alien army from enslaving humanity.";

        String question2 = "Who directed \"The Godfather\"?";
        String expected2 = "The Godfather was directed by Francis Ford Coppola";

        String question3 = "What is your name?";
        String expected3 = "Sorry I can only answer movie questions";

        String question4 = "What awards did \"Bazinga\" win?";
        String expected4 = "Sorry could not find a movie named bazinga";

        String question5 = "Who starred in \"Star wars\"?";
        String expected5 = "Mark Hamill, Harrison Ford, Carrie Fisher starred in Star Wars";

        String question6 = "Who wrote \"Star Wars\"";
        String expected6 = "Star Wars was written by George Lucas";

        String question7 = "Who directed \"Titanic\" and who starred in it. What is the plot of the movie and what " +
                "awards did it get?";
        String expected7 = "\nTitle: Titanic\n" +
                "Directed by James Cameron\n" +
                "Awards: Won 11 Oscars. 126 wins & 83 nominations total\n" +
                "It is about A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the luxurious, ill-fated R.M.S. Titanic.\n" +
                "Actors: Leonardo DiCaprio, Kate Winslet, Billy Zane";

        assertEquals(expected1, respond.getResponse(question1));
        assertEquals(expected2, respond.getResponse(question2));
        assertEquals(expected3, respond.getResponse(question3));
        assertEquals(expected4, respond.getResponse(question4));
        assertEquals(expected5, respond.getResponse(question5));
        assertEquals(expected6, respond.getResponse(question6));
        assertEquals(expected7, respond.getResponse(question7));
    }

}
