# Application wide context

Sometimes you need to have some state shared for the lifetime of the application,
and this is done by using the `AppContext` in the `app.config.AppBootstrap`:

```java
public class AppBootstrap extends Bootstrap {
    public void init(AppContext context) {
        context.set("framework_name", "JavaWebPlanet");
    }
}
```
Everything in the `AppContext` can then be retrieved from every controller:

```java
public class IndexController extends AppController {
    public void index(){
         view("framework_name", appContext().get("app_name"));
    }
}
```

The `AppContext` is mostly just a `Map`, but it has some extra methods. Most notable:

```java
/*
 * Sets the encoding of every generated content
 */
context.setEncoding("UTF-8"); // this is the standard encoding

/*
 * Enables the application to be language sensitive.
 * 
 * First element is the default language, which is used,
 * when no language is defined
 */
context.setSupportedLanguages("en", "de", "fr");
```

Setting the supported languages enables the notion of localisation of webpages.
The strings can, in principle, be of any language format, but the given strings are used directly in the URL,
so it might not be sensible to have them in a format too hard for the users to remember.

```
http://host:port/movies (same as 'en')
http://host:port/en/movies
http://host:port/de/movies
http://host:port/fr/movies
```
These URLs becomes allowed and will set the `language()` available in the *Controller*, and if no language is set in the URL, it will default to the first stated language.