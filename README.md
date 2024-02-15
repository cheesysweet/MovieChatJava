# Project

## Purpose
The purpose of the project is to showcase the use of the different programming paradigms
such as regular expression, functional programming, streams and reactive programming.
This is done by creating a "dum-down" version of a chatbot.

## Procedures
The chatbot works by accepting user input in the java swing program it accepts questions
about movies and gives information about them in response. The user can ask questions about a
movie by specifying the movie "title" in quotations and optionally the "year" it was released to
specify the exact year. This will result is a generic response of the tile, release date and the
plot of the movie. The user can also ask specific questions about the movie either when writing
title or after it has gotten the generic response. It uses OMDB to fetch the movie information.

### ChatDisplay
The chat display package handles the java swing interface the user is interacting with.

#### ChatFrame
Chat frame contains the java swing frame that is contains a scroll panel with the ChatPanel and
the InputArea at the bottom of the frame. It is responsible for observing the input area and
with adding a new response to the ChatPanel and clearing the InputArea after it has gotten a
response.

The inputArea uses an observable when fetching user inputs this utilizes the subscribeOn method
that enables the input to be handled on a different thread.
````
inputArea.getTextEvent()
    .subscribeOn(Schedulers.io())
    .subscribe(res -> {
        chatPanel.addConversation(res, respond.getResponse(res));
        inputArea.clearTextField();
    }, err -> System.err.println(err.getMessage()));
````

#### ChatPanel
Chat panel is used to display the conversation and formatting the user/bot messages with line
separations so that they are easier to read. It also prints a message to the user at the start
of the chat to inform the user on how to ask questions about movies.

#### InputArea
The input area is used to observe the user inputs and submits a user question when it either
presses the submit button or presses enter in the text field.
````
private Observable<String> getActionListener(JTextField text, JButton submit) {
    return Observable.create(subscribe -> {

        ActionListener actionListener = e ->
                subscribe.onNext(e.getActionCommand().equals("Submit") ? text.getText().trim() : e.getActionCommand());

        text.addActionListener(actionListener);
        submit.addActionListener(actionListener);
    });
}
````

### Response
The response package is used to fetch and format a response for the question the user asked.

#### OMDBRequest
This is responsible for connecting to the OMDB api and fetch a movie depending on requested
title and year if submitted. This is done using a bufferedReader with a inputStream to get
collect all lines into a string

#### Respond
The Respond class is used to handle the actual response by calling the getResponse function 
with a question. If the user entered a greeting the bot will respond with a greeting asking if
it can help with any movie related questions. If the user instead entered question matching the movie
name and year pattern. 
````
Pattern namePattern = Pattern.compile("\"([^\"]+)\"|\\((\\w+(?:\\s+\\w+)*)\\)");
Pattern yearPattern = Pattern.compile("\\((\\d{4})\\)|\"(\\d{4})\"|(\\d{4})");
````
This is then stored with a functional if statement, that stores the new name and year if entered and reuses old
name and year if present. Then by utilizing two imperative if statements to write error message for
the user if a movie could not be found in OMDB, or the user did not enter a movie name in the right format.
Last it calls getMovie with information from OMDBRequest that either fetches a movie with only the name
or a movie with name and year.

##### getMovie
This function creates a hashMap of the movies string that is a string response from OMDB where
it uses regex to split the string and store the key and value for each of the different information
fetched from OMDB. This is then used to either format a response or return a generic response of
the release year and plot of the movie.
````
HashMap<String, String> movieMap = Arrays.stream(
        // removes brackets at quotations from the string and splits at , not followed by space or digit
        movies.replaceAll("[{}\"]", "").split(",(?![\\s\\d])"))
        .map(pair -> pair.split(":"))
        .collect(Collectors.toMap(
                pair -> pair[0],
                pair -> pair[1],
                (v1,v2) -> v1, HashMap::new // saves first key in case of duplicate
));
````      
It also contains a function to check for response error and returns error in that case 
````
if (movieMap.get("Response").equals("False")) {
   return "Sorry could not find a result for this";
}
````

##### getMovieInformation
It uses a list and a hashmap in order to parse what information about a movie the user wants
The first list shows all the different regex patterns used to distinguish the information.
````
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
````
The hashMap is used to take the base pattern without the brackets as key and return a value.
````
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
````
These are then used in a stream to filter out witch information was requested and add the value
from the HashMap in a list that is then used to know what information should be fetched for
the movie.

##### formatResponse
Uses the movieResponse.txt file that stores a large number of different type of responses. By
utilizing the list of infoToGet that contains the information to get it then looks for all the 
responses that contain that same amount of placeholders as information. It then looks for the
strings whose placeholders matches those of the infoToGet list and chooses a random one as the
return message.

It utilizes subscribe on to fetch and collect all the lines in the txt file. The observeOn to
perform the filtering and mapping on collected data.
````
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
                return list.isEmpty() ? generateResponse(movieMap) : list.get(0);
            })
            .map(template -> infoToGet.stream()
                    .reduce(template, (result, tag) -> result.replace("<" + tag + ">", movieMap.get(tag)), (a, b) -> a)
                    .replaceAll("\"", "")
            );
}
````

##### generateResponse
This is utilized if an existing response message is not found and a more generic 
response is returned. This response only states the facts about the movie and is not
formatting it into any type of message as the text file dose.

##### getGreeting
This fetches different greetings from the greetings text file that contains different
greetings depending on the users greeting. It looks for a matching response and greets the user with that.

## Discussion
The chatbot utilizes java swing which in turn gives a good opportunity to utilize observable
for handling the users inputs. The RxJava framework is also utilized when fetching the 
and filtering the pre generated response messages.

The input is then combined with regex to find out the movie name and year, Also to find 
what information about a movie the user wants, and replace the placeholders in the 
response messages. 

Functional programming is used in both streams and RxJava, it also uses functional if 
statements for deciding between different outcomes. It also mostly conform to immutability 
as it mostly uses streams to perform operations on the input that in return gives a new 
object. Recursion is also used if the movie was not found and checks if multiple movie
titles was entered.

The usage of imperative if statements was used to create exit return statements if the
code enters a state where it should exit. If it contains an if else or a generic
return it uses the functional if statement instead.

In order to handle sequential question about the same movie the last title entered is stored and reused
if the user enters any questions about the same movie.

#### Paradigms
The use of regular expressions gives a powerful tool for text pattern matching and can be used
to detect user intents in natural language processing applications. The use of character classes,
alternation, and quantifiers allows the chatbot to detect a wide range of patterns in user input
and find appropriate responses. With either a response of movie information if one is found or error message,
if the chatbot could not detect a name.

Functional programming concepts such as immutability, recursion, and lambda expressions can lead to more
robust, maintainable, and efficient code. The use of these concepts on the chatbot makes it easier to
understand, modify and reduce the risk of error and making it easier to add new features.

The Java Stream Api provides a powerful and expressive way to manipulate collections of data, and its use
in the chatbot can make the code more concise and readable. By using stream operations such as filer, map
and collect the chatbot can perform complex data transformations with a few lines of code, improving
performance and readability.

RxJava provides a reactive programming library that allows developers to write asynchronous, event-driven
code in a simple and elegant way. Its use in the chatbot codebase can make the code more maintainable and 
scalable, allowing the chatbot to handle user interactions with minimal overhead. By using RxJava operators
the chatbot easily handles multiple tasks with the help of subscribeOn.

### Test
The test cases checks the OMDB connection and test cases to check the complete Respond getMovie function
The OMDB request is tested by fetching a movie and checking if it gets the response.
The Respond class is tested by inputting different questions to check the response is correct and
return error messages if the question could not be answered.

### Problems
The usage of RxJava functions was hard to find a good usage and implementing for.
It was straight forward to use it when implementing the user input and for the collection
and filtering for the text file. But other than that it felt easier and more effective to 
use streams.

The implementation of how to distinguish what information to collect when using regex for the
user input was hard to implement. How to distinguish a movie name from the rest a text was
hard to figure out a good solution for which is why the usage of quotations and parenthesis is 
used to find the movie name. 

these questions for example can contradict each other and how to figure out if it just the first
"star wars" that is the name and not the complete "star wars the last jedi".\
    when was "star wars the last jedi" released and who starred in it?\
    who starred in "avengers"?
