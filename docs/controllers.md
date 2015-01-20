# Controllers

Controllers are the heart of the application, and are used to process HTTP requests and form the appropriate responses.
Much inspired by *Grails* and works to some extend like *Spring Controllers*

## Introduction
A simple controller looks like this:
```java
public class MovieController extends AppController {
   public void index(){}
}
```

`public void index(){}` is always implicit set, so this is similar:
```java
public class MovieController extends AppController {}
```

This controller would be mapped to the following URL:
```
http://host:port/movie
```

All controllers must be placed in the package `app.controllers`, or else they will not be found by the framework.

## Actions
Actions is the name of the public methods of a controller.

`index()` is always present, and is called when nothing else is specified in the URL, like we saw in the [introduction](#introduction).


### HTTP methods
Actions have a special naming convention in order to break with having annotations for each controller and action.

This means that an action has to have its HTTP method as a part of its name:
```java
public class MovieController extends AppController {
   public void getMovie() {}
   public void postMovie() {}
   public void deleteMovie() {}
   public void getTitle() {}
}
```

Currently, the following HTTP methods are supported: `GET`, `POST`, `PUT`, `DELETE` and `HEAD`.

This convention promotes the REST-style web programming greatly if needed. 

Actions always have the return value `void`. It is possible to let them return something, but it will be ignored by the framework.


## Controller paths
The path to a controller is always the name of the controller with the end, "*Controller*", stripped.
If the controller is placed in a subpackage, then this is reflected in the URL to which it is mapped:

```java
package app.controllers;
public class MovieController extends AppController {} => http://host:port/movie
```
```java
package app.controllers.sub;
public class HelloController extends AppController {} => http://host:port/sub/hello
```

Furthermore, the *CamelCase* naming of a controller is interchangeable translated to *underscore_case* **or** *hyphen-case* in the URL:
```java
package app.controllers;
public class WhatIsUpController extends AppController {} 
```
`=> http://host:port/what_is_up` *or* `http://host:port/what-is-up`

Another example:
```java
package app.controllers.danish;
public class LocalMoviesController extends AppController {}
```
`=> http://host:port/danish/local_movies` *or* `http://host:port/danish/local-movies`

## Views
Much on how controller actions are mapped to views can be studied in the [views documentation](views.md#action-to-template-mapping).

### Passing data to views
Every controller action has the method `view(name, value)`, which is used to send data of any type to the view.

```java
public class MovieController extends AppController {
	public void index() {
		// do some work
		view("title", "Movie title");
		view("year", 1999);
		view("producer", "Some name");
	}
}
```
This can be used in the view like so:
```
Title of movie $title$
Year $year$
Producer $producer$
```

## Request parameters
Getting, and using, parameters in a web application is essential, and **jawn** exposes a few ways for this.
First off, *parameter* in this framework covers both URL parameters and *POST*ed data.

### Single parameter
```java
public class MovieController extends AppController {
	public void index() {
		String name = param("name");
	}
}
```

### All parameters
A simple method for getting all parameters is `params()`. It returns a `MultiList`, which has some neat properties
when working with parameters that can essentially be a series of lists.

The `MultiList` wraps all of this and lets you get the first or last element associated with a given parameter.

```java
public class MovieController extends AppController {
	public void index() {
		MultiList<String> params = params();
		String movie = params.first("movie");
	}
}
```

### All values for a single parameter
```java
public class MovieController extends AppController {
	public void index() {
		List<String> names = params("name");
	}
}
```

### Converting parameters
Sometimes when redirecting or logging you want to convert the parameters into an URL-ready string
```java
public class MovieController extends AppController {
	public void index() {
		String paramString = paramsAsUrlString();
	}
}
```


## File handling

### Receiving files
```java
public class MovieController extends AppController {
	public void postMovieImage() {
		MultiList<FormItem> items = multipartFormItems();
		// process items
		FormItem item = items.first("movieimage");
		if (!item.empty())
			image(item).save();
	}
}
```


### Sending files
```java
public class MovieController extends AppController {
	public void getInfo() {
		File f = /*obtain file*/;
		sendFile(f).contentType("application/pdf").status().ok();
	}
}
```

```java
public class MovieController extends AppController {
	public void getInfo() {
		byte[] b = /*obtain bytes*/;
		outputStream("application/pdf").write(b);
	}
}
```

```java
public class MovieController extends AppController {
	public void getInfo() {
		InputStream[] in = /*obtain stream*/;
		streamOut(in).contentType("application/pdf").header("Expires", "Fri, 31 Dec 1999 23:59:59 GMT").status(200);
	}
}
```


## AJAX
### Detecting if a request is AJAX
```java
public class MovieController extends AppController {
	public void index() {
		if (isXhr()) {
			// ajax request
		} else {
			// something else
		}
	}
}
```

### Responding
Often you do not want to respond to an AJAX request, or some other requests, with a view, but instead send some
minor information instead or information in a different format.

#### Text
```java
public class MovieController extends AppController {
	public void postMovie() {
		// do work
		respond().text("went fine").contentType("text/plain");
	}
}
```

#### JSON
```java
public class MovieController extends AppController {
	public void getMovie() {
		Movie m = new Movie("Guardians of the Galaxy", 2014);
		respond().json(m); // sets the content-type to application/json
	}
}
```

#### XML
```java
public class MovieController extends AppController {
	public void getMovie() {
		Movie m = new Movie("Guardians of the Galaxy", 2014);
		respond().xml(m); // sets the content-type to application/xml
	}
}
```


## Session
It is possible to save some states for a period of a session:
```java
session("name", value); 			// put an object
 
session("name"); 					// get the object
sessionString("name");				// get the value as string

session().remove("name");  			// delete the object
session().setTimeToLive(seconds);	// max inactive interval
session().invalidate();				// discard all attributes
```


## Cookies
Sending:
```java
sendCookie(Cookie);               // sends a cookie to client
sendCookie(name, value);          // simple short cut to do the same as above 
sendPermanentCookie(name, value); // will send a cookie with time to live == 20 years
```
Retrieving:
```java
List<Cookie> cookies(); // gets a list of all cookies sent in request
Cookie cookie(name);    // retrieve an individual cookie
cookieValue(name);      // retrieve a cookie value by name of cookie
```


## Logging
All controllers have a [SLF4J](http://slf4j.org/) logging system integrated. Simply call `log()` to access it:
```java
log().debug("--- reading from DB ----");
log().info("Created");
log().warn("Don't do that");
log().error("Going down");
```
You just plug in to it with a logging framework of your choice.

## HTTP Status
When sending a respond of any kind in the controller actions you sometimes have to state whether something went well
or failed somehow. This is done by using HTTP status codes, and **jawn** has some quick access methods for this:
```java
public class MovieController extends AppController {
	public void postMovie() {
		// do work
		if (ok)
			respond().text("went fine").status().ok();
		else
			respond().text("could not find").status().notFound();
	}
}
```
Not every possible status code is implemented like this, so you also have the option to specify the code of your likings:
```java
respond().json(object).status(statusCode);
```