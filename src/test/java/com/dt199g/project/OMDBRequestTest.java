package com.dt199g.project;

import org.junit.jupiter.api.Test;
import com.dt199g.project.Response.OMDBRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OMDBRequestTest {

    @Test
    public void testFetchTitle() {
        OMDBRequest omdbApi = new OMDBRequest();

        String title = "The Matrix";
        String expected = "{\"Title\":\"The Matrix\",\"Year\":\"1999\",\"Rated\":\"R\",\"Released\":\"31 Mar 1999\"," +
                "\"Runtime\":\"136 min\",\"Genre\":\"Action, Sci-Fi\",\"Director\":\"Lana Wachowski, Lilly Wachowski\"," +
                "\"Writer\":\"Lilly Wachowski, Lana Wachowski\",\"Actors\":\"Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss" +
                "\",\"Plot\":\"When a beautiful stranger leads computer hacker Neo to a forbidding underworld, " +
                "he discovers the shocking truth--the life he knows is the elaborate deception of an evil cyber-intelligence." +
                "\",\"Language\":\"English\",\"Country\":\"United States, Australia\",\"Awards\":\"Won 4 Oscars. 42 wins & 51 " +
                "nominations total\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BNzQzOTk3OTAtNDQ0Zi00ZTVkLWI0MTEtMDllZ" +
                "jNkYzNjNTc4L2ltYWdlXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database" +
                "\",\"Value\":\"8.7/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"88%\"},{\"Source\":\"Metacritic\",\"Value\":" +
                "\"73/100\"}],\"Metascore\":\"73\",\"imdbRating\":\"8.7\",\"imdbVotes\":\"1,929,568\",\"imdbID\":\"tt0133093\",\"Type" +
                "\":\"movie\",\"DVD\":\"15 May 2007\",\"BoxOffice\":\"$172,076,928\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response" +
                "\":\"True\"}";

        String actual = omdbApi.fetchTitle(title, "1999");

        assertEquals(expected, actual);
    }
}