# Views
As is custom in the MVC framework, the *view* part is split into
*layouts*, which can contain *templates*.

A *layout* can represent the overall structure of a webpage like header and footer and maybe a menu.
A *template* is the result of a controller action run and populated by one or several models.
The *template* is then injected into the *layout*, and thereby various pages are generated
by equally various templates enveloped by the *layout*.


## Renderer / template engine
**jawn** is using [StringTemplate](http://www.stringtemplate.org/) as default template engine
and every example is therefore using that syntax.

The framework exposes some of the possible configurations of the template engine, like [adaptors](https://theantlrguy.atlassian.net/wiki/display/ST4/Model+adaptors) and [renderers](https://theantlrguy.atlassian.net/wiki/display/ST4/Renderers). 
Read more of the configuration in [Configuration](configuration.md)

The original usage of [FreeMarker](http://freemarker.org/) is still somewhat supported, however, it has to be enabled.
(This has not yet been tested or documented)

### StringTemplate
The syntax of the renderer used in **jawn** is using dollar signs `$` around keywords.
```html
<section>
    <h2>$headline$</h2>
    <div>Title: $movie.title$</div>
    <div>Year:  $movie.year$</div>
</section>
```

The keywords are defined in the action simply:
```java
public void index() {
	Movie m = new Movie("Guardians of the Galaxy", 2014);
    view("movie", m);
    view("headline", "Movie listing");
}
```

#### Objects
Notice that StringTemplate can access properties of a keyword if it represents an object.
This is possible if the object has public fields or has public methods starting with `get`, like so:
```java
public class Movie {
	private String title; // public as 'title' through getTitle()
	public int year; // a public field
	
	public Movie(String t, int y) {
		title = t;
		year = y;
	}
	public String getTitle() { return title; }
}
```

#### Maps
The same approach applies for representations of `Map`. In such a case the properties of the keyword is the keys of the map:
```html
<div>$some_map.key1$ <br> $some_map.key2$</div>
```

#### Lists
Objects of the type `List` or `Collection` can be iterated easily:
```html
<table>
    <thead>
    	<tr><th>Name</th><th>Year</th></tr>
    </thead>
    
    <tbody>
        $movies: {movie | <tr><td>$movie.name$</td><td>$movie.year$</td></tr>}$
    </tbody>
</table>
```
Here, the renderer does a *foreach* on the list `movies`, names the current object `movie`, and uses this 
object in an inlined template.

It is also possible to use the *foreach* with an external template:
```html
<table>
    <thead>
    	<tr><th>Name</th><th>Year</th></tr>
    </thead>
    
    <tbody>
        $movies:/common/movierow$
    </tbody>
</table>
```

For a more thorough introduction see [StringTemplate introduction](https://theantlrguy.atlassian.net/wiki/display/ST4/Introduction#Introduction-Accessingpropertiesofmodelobjects)

## Templates
All templates are named after their respective action of the controller.
So `index.st` is used when executing:
```java
public void index(){}
```

### An example: 
```
http://host:port/movies/newest
```

Translates to:
```java
public class MoviesController extends AppController {
    public void getNewest() {}
}
```
Which uses the view `WEB-INF/views/movies/newest.st`.

### Action to template mapping
In most cases the action has a simple name like `getNew` or `getMovie`, but this is of course not always viable.
Whenever an action is named more complex the template needs to reflect this, and it rather simply done by converting 
CamelCase to underscore_case:
```java
public void getMovie() {}				=> WEB-INF/views/movies/movie.st
public void getEveryMovie() {}			=> WEB-INF/views/movies/every_movie.st
public void getSomethingFromAfar() {}	=> WEB-INF/views/movies/something_from_afar.st
```


### Override
It is of course possible to render a specific template instead of the one by convention.
It is done simply by, lastly in an action, calling:
```java
render("different_template"); // renders template within same controller folder
```
This renders the template of that name in the folder belonging to the controller.

You could instead pick a different template from a different folder if desired:
```java
render("/common/mail"); // renders template of a chosen folder
```

Lastly, it is possible to just render a template without the layout. This might come in handy when generating an email template or the like.
```java
render().noLayout();
```


Templates can be nested.
> Some example

For more information, refer to the [template guide](https://theantlrguy.atlassian.net/wiki/display/ST4/Templates) of StringTemplate.



## Layouts
Generally used to outline the structure of the HTML generated as a result of any controller action.

Whereas templates are named after their respective controller action, the layouts have a reserved name: `index.html.st`.
 
The layout located in the root of `WEB-INF/views` serves as base for all controller templates to generate HTML. This is the *main layout*.

Is the `index.html.st` layout present within a view subfolder, then this layout overrides the default layout for that controller.
For example:
```
webapp/WEB-INF/views/some/index.html.st
```

A layout should most often contain a `$site.content$` where the controller template will be injected.


## Reserved keyword
The keyword **site** is reserved in the layouts and will always be overridden if used by the user.

It contains the following properties:
* `site.content` - the placeholder of the template. If templates are used in your application, this should always be present in your layout.
* `site.language` - if enabled in [AppContext](appcontext.md)


If provided in the [site.json](#sitejson):
* `site.title` - a string to be used in the template.
* `site.scripts` - a compiled list of the javascripts given.
* `site.styles` - a compiled list of the given stylesheets.


## site.json
When declaring stylesheets and javascript files for your webpage, you can of course put them all in the
main layout, but if your webpages needs different combinations of includes, 
it can quickly become heavy to load pages, if even the most minimal of pages have to include every script and style needed for all the pages in your webapp.

This problem is addressed by **yawn** in special `site.json` files.
In these you can put the javascript files and stylesheets needed in the context of the folder in which they are placed.

So a `site.json` placed next to the main layout in the root of the `WEB-INF/views` 
will become *global* and used by every page using the main layout. 

Is a `site.json` placed in `WEB-INF/views/movies` 
it will by standard *add* the scripts and styles to the layout.
If this is undesired behaviour, set the property `"overrideDefault" : true`,
which will make the framework disregard any global `site.json`.

```java
{ 
    "title": "Movie headline",
	
    // it is possible to use comments in this file
	
    "scripts": [
        "lib/bootstrap/bootstrap.js",
        "http://code.jquery.com/jquery-2.1.3.min.js",
        "localscript.js"
    ], 
	
    "styles": [
        "lib/bootstrap/bootstrap.css",
        "localstyle.css"
    ],
	
    "overrideDefault" : false
}
```

The parser of the file distinguishes between local and full paths, and will decorate the links
with valid HTML, like so:
```html
<script src="/js/lib/bootstrap/bootstrap.js"></script>
<script src="http://code.jquery.com/jquery-2.1.3.min.js"></script>
...
<link rel="stylesheet" type="text/css" href="/css/lib/bootstrap/bootstrap.css">
```

Notice the framework automatically assumes that "scripts" are located in the folder `/js`,
and the "styles" are located in `/css`.
This may be configurable in the future.

Remember to use valid JSON.