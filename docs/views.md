# Views
**Layouts** contains one or several **templates**. 


## Renderer / template engine
Using [StringTemplate](http://www.stringtemplate.org/) as default.

The framework supports some configuration of the template engine, like [adaptors](https://theantlrguy.atlassian.net/wiki/display/ST4/Model+adaptors) and [renderers](https://theantlrguy.atlassian.net/wiki/display/ST4/Renderers). 

The original usage of [FreeMarker](http://freemarker.org/) is still somewhat supported, however.

> How to switch between template engines needs to be documented.



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
...
	public void getNewest() {}
...
}
```

Which uses the view `WEB-INF/views/movies/newest.st`.



Templates can be nested.
> Some example

For more information, refer to the [template guide](https://theantlrguy.atlassian.net/wiki/display/ST4/Templates) of StringTemplate.



## Layouts
Generally used to outline the structure of the HTML generated as a result of any controller action.


`index.html.st` = default layout. This is located in the root of `views` folder
and base for all controller templates to generate HTML.

Is the `index.html.st` layout present within a view subfolder as illustrated in the [example structure](structure_of_jawn_project.md), then this layout overrides the default layout for that view.
Like so:
```
webapp/views/WEB-INF/some/index.html.st
```

A layout should always contain a `$site.content$` where the controller template will be injected.

## Reserved keyword
The keyword *site* is reserved in the layouts and will always be overridden if used by the user.

It contains the following properties:
* `$site.content$` - the placeholder of the template. If templates are used in your application, this should always be present in your layout.
* `$site.language$` - if enabled in [AppContext](appcontext.md)
* If provided in the **site.json**
  * `$site.title$` - a string to be used in the template.
  * `$site.scripts$` - a compiled list of the javascripts given.
  * `$site.styles$` - a compiled list of the given stylesheets.


## site.json
Place declaration of scripts and stylesheets in one place.
It distinguishes between local and full paths

```json
{ 
	"title": "Movie headline",
	
	/* it is possible to use comments in this file */
	
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
 
> elaborate