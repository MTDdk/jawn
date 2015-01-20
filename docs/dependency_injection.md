# Dependency Injection

As dependency injection is a central part of many application does **jawn** naturally also support this
by using [Google Guice](https://github.com/google/guice/wiki/Motivation). 
How to use *Guice* in all its glory can be read in the documentation for it.

Here I will just describe basic usage.

## Interface
The intention of using dependency injection is to have some standard interfaces that applications adhere to,
but the actual implementation is unnecessary to know anything about. Also called *separation of concerns*.

So, for starters we create an interface:
```java
public interface MoviesDB {
    public List<Movie> listMovies();
    public Movie fetch(int id);
}
```

## Implementation
An implementation of an interface can be built as the programmer so pleases that day.
In this case we have a mechanism for retrieving from a store of some kind.

How the data is stored, is unknown to the user of the interface, and here we just use
a simple array.
```java
@Singleton
class ArrayMoviesDB implements MoviesDB {
    
    private List<Movie> movies = new ArrayList<>();
    
    ArrayMoviesDB() {
        Collections.addAll( movies,
            new Movie("Guardians of the Galaxy", 2014),
            new Movie("Taken", 2008),
            new Movie("The Matrix", 1999)
         );
    }

    @Override
    public List<Movie> listMovies() {
        return Collections.unmodifiableList(movies);
    }

    @Override
    public Movie fetch(int id) {
        return movies.get(id);
    }
}
```

## Module
The heart of the Google Guice framework is the *Modules* that do the actual binding between an interface and its
corresponding implementation.

```java
public class DbModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MoviesDB.class).to(ArrayMoviesDB.class);
    }
}
```

A module has to extend `AbstractModule` to be used in the dependency injection framework.

## Injection
The actual injection is done by adding the module to **jawn** via the `app.config.AppBootstrap`.

```java
public class AppBootstrap extends Bootstrap {
    @Override
    public void init(AppContext context) {
        putModules(new DbModule());
    }
}
```

The `putModules` takes a vararg of `AbstractModule`, so you can have a module for each service your application needs
to use.

## Usage
Whenever you need to use your injections, you can simply state the service inside your controller
using annotations provided by Google Guice
```java
public class MovieController extends AppController {
    
    @Inject // injects fields on the fly
    MoviesDB movies;

    public void index() {
        view("movies", movies.listMovies());
        render("list");
    }
    
    public void getSingle() {
        Movie movie = movies.fetch(getIdInt());
        
        view("movies", Arrays.asList(movie));
        render("list");
    }
    
    public void getXml() {
        respond().xml(movies.listMovies());
    }
    
    public void getJson() {
        respond().json(movies.listMovies());
    }
}

```

There are two types of components in the framework, that have the dependencies injected:
* Controllers
* Controller Filters

It may seem like a bit more work, but this makes all your controllers agnostic on
the actual implementation, and it is therefore possible to change on the fly.
When testing, for example.