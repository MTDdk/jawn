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

Furthermore, the *CamelCase* naming of a controller is translated to *underscore_case* in the URL:
```java
package app.controllers;
public class WhatIsUpController extends AppController {} => http://host:port/what_is_up
```
```java
package app.controllers.danish;
public class LocalMoviesController extends AppController {} => http://host:port/danish/local_movies
```

## File handling

### Upload files

### Send files

## AJAX

### JSON
### XML

## Session

## Cookies

## Logging