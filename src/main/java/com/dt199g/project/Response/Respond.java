package com.dt199g.project.Response;


import com.dt199g.project.Project;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.core.Observable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles the response message for the bot
 * @author Anton Byström
 */
public class Respond {
    private Map<String, String> matches = new HashMap<>();
    private final String notFound = "Sorry but I can only answer movie questions!";
    public Respond(){}

    /**
     * checks if the user greeted the bot then
     * returns a greeting question
     * @param question user question
     * @return bot message
     */
    public String getResponse(String question) {
        Matcher greeting = Pattern.compile("hello|hey|hi|good (morning|afternoon|evening|day)|gretings")
                .matcher(question);
        return (greeting.find() && getMovieResponse(question).equals(notFound))?
                getGreeting(greeting.group()): getMovieResponse(question);

    }

    /**
     * parse the message to find movie name and year.
     * handles if no movie name was entered or if used
     * @param question user question
     * @return bot message
     */
    private String getMovieResponse(String question) {
        Pattern namePattern = Pattern.compile("\"([^\"]+)\"|\\((\\w+(?:\\s+\\w+)*)\\)");
        Pattern yearPattern = Pattern.compile("\\((\\d{4})\\)|\"(\\d{4})\"|(\\d{4})");

        Matcher nameMatcher = namePattern.matcher(question);
        Matcher yearMatcher = yearPattern.matcher(question);

        Map<String, String> result = Stream.of(nameMatcher, yearMatcher)
                .flatMap(matcher -> {
                    List<String> matches = new ArrayList<>();
                    while (matcher.find()) {
                        matches.add(matcher.group());
                    }
                    return matches.stream();
                })
                .map(match -> {
                    if (namePattern.matcher(match).matches()) {
                        String name = match.replaceAll("(\"|\\(|\\))", "");
                        return new AbstractMap.SimpleEntry<>("name", name);
                    } else if (yearPattern.matcher(match).matches()) {
                        String year = Arrays.stream(match.split("[\"()]"))
                                .filter(str -> str.matches("\\d{4}"))
                                .findFirst()
                                .orElse(null);
                        return new AbstractMap.SimpleEntry<>("year", year);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.matches = result.isEmpty() ? this.matches : result;

        // response if user enters something other than movie question
        if (getMovieInformation(question).stream().allMatch(s -> s.equals("Title")) && result.size() == 0) {
            return notFound;
        }

        // No movie found or stored
        if (this.matches.size() == 0 || !this.matches.containsKey("name")) {
            return "Could you please write the movie name like this \"name\" or (name)";
        }

        return (this.matches.size() >= 2)?
                    getMovie(new OMDBRequest().fetchTitle(this.matches.get("name"), this.matches.get("year")),
                        getMovieInformation(question)):
                    getMovie(new OMDBRequest().fetchTitle(this.matches.get("name")), getMovieInformation(question));
    }

    /**
     * loads the movie information into a hashMap
     * @param movies information about the movie
     * @param infoToGet information to get from the movie
     * @return bot message
     */
    private String getMovie(String movies, List<String> infoToGet) {
        HashMap<String, String> movieMap = Arrays.stream(
                // removes brackets at quotations from the string and splits at , not followed by space or digit
                movies.replaceAll("[{}\"]", "").split(",(?![\\s\\d])"))
                .map(pair -> pair.split(":"))
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1],
                        (v1,v2) -> v1, HashMap::new // saves first key in case of duplicate
        ));

        // handles if the user typed in a movie that could not be found
        if (movieMap.get("Response").equals("False")) {
           return "I could unfortunately not find a movie named " + this.matches.get("name");
        }

        return (infoToGet.size() >= 2)? formatResponse(infoToGet, movieMap).blockingGet():
                String.format("Here are som information I found about %s was released in %s and was about %s",
                    movieMap.get("Title"), movieMap.get("Year"), movieMap.get("Plot"));
    }

    /**
     * filters out the information to create a string message from
     * @param question user question
     * @return List of movie information to get
     */
    private List<String> getMovieInformation(String question) {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("releas(ed|ing|es)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("direct(ed|or|ing)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("act(or|res|ors|ed)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("genr(e|es)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("awar(d|ds)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("writer", Pattern.CASE_INSENSITIVE),
                Pattern.compile("plot", Pattern.CASE_INSENSITIVE),
                Pattern.compile("rat(ed|ing)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("starred", Pattern.CASE_INSENSITIVE),
                Pattern.compile("wrote", Pattern.CASE_INSENSITIVE)
        );

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("starred", "Actors");
        fieldMap.put("act", "Actors");
        fieldMap.put("releas", "Released");
        fieldMap.put("direct", "Director");
        fieldMap.put("writer", "Writer");
        fieldMap.put("wrote", "Writer");
        fieldMap.put("plot", "Plot");
        fieldMap.put("rat", "imdbRating");
        fieldMap.put("awar", "Awards");
        fieldMap.put("genr", "Genre");

        List<String> infoToGet = new ArrayList<>();
        infoToGet.add("Title");

        infoToGet.addAll(patterns.stream()
                .filter(p -> p.matcher(question).find())
                .map(Pattern::toString)
                .map(p -> p.replaceAll("\\([^(]*\\)", ""))
                .flatMap(s -> {
                    String string = s.toLowerCase();
                    List<String> list = new ArrayList<>();
                    if (fieldMap.containsKey(string)) {
                        list.add(fieldMap.get(string));
                    }
                    return list.stream();
                })
                .distinct()
                .toList());

        return infoToGet;
    }

    /**
     * creates the bot return message by using an observable to fetch the data from the file
     * and then filter it to get a response message
     * @param infoToGet list of movie information to get
     * @param movieMap hashMap with what to get as key and the information as value
     * @return bot message
     */
    private Single<String> formatResponse(List<String> infoToGet, HashMap<String, String> movieMap) {
        return Single.fromCallable(() -> Project.class.getResourceAsStream("/movieResponse.txt"))
                .subscribeOn(Schedulers.io())
                .flatMapObservable(inputStream -> {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    return Observable.fromIterable(reader.lines().collect(Collectors.toList()));
                })
                .observeOn(Schedulers.computation())
                .filter(line -> countAngleBrackets(line) == infoToGet.size())
                .filter(line -> infoToGet.stream().allMatch(line::contains))
                .toList()
                .map(list -> {
                    Collections.shuffle(list);
                    return list.isEmpty() ? generateResponse(movieMap, infoToGet) : list.get(0);
                })
                .map(template -> infoToGet.stream()
                        .reduce(template, (result, tag) -> result.replace("<" + tag + ">", movieMap.get(tag)), (a, b) -> a)
                        .replaceAll("\"", "")
                );
    }

    /**
     * method used to generate a fixed response if no response message was found.
     * @param movieMap movie information
     * @param infoToGet what information to print
     * @return formatted string message
     */
    private String generateResponse(Map<String, String> movieMap, List<String> infoToGet) {
        Map<String, String> placeHolders = Map.of(
                "Title", "\nTitle: ",
                "Released", "\nIt released in ",
                "Director", "\nDirected by ",
                "Writer", "\nWritten by ",
                "Actors", "\nActors: ",
                "Plot", "\nIt is about ",
                "imdbRating", "\nIt was rated ",
                "Awards", "\nAwards: ",
                "Genre", "\nGenre: "
        );

        return infoToGet.stream().filter(movieMap::containsKey)
                .map(string -> placeHolders.get(string) + movieMap.getOrDefault(string, ""))
                .collect(Collectors.joining());
    }

    /**
     * counts amount of template information in templates
     * @param line template string
     * @return count
     */
    private int countAngleBrackets(String line) {
        return (int) line.chars().filter(ch -> ch == '<').count();
    }

    /**
     * fetches a greeting from the greetings file
     * @param greeting greeting string
     * @return greeting message
     */
    private String getGreeting(String greeting) {
        return Single.fromCallable(() -> Project.class.getResourceAsStream("/greetings.txt"))
                .subscribeOn(Schedulers.io())
                .flatMapObservable(inputStream -> {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    return Observable.fromIterable(reader.lines().collect(Collectors.toList()));
                })
                .observeOn(Schedulers.computation())
                .filter(line -> line.contains(greeting.split("")[0]))
                .toList()
                .map(list -> {
                    Collections.shuffle(list);
                    return list.get(0);
                })
                .map(result -> result.replaceAll("<([^\"]+)>",
                        greeting.substring(0,1).toUpperCase() + greeting.substring(1))
                )
                .blockingGet();

    }
}

