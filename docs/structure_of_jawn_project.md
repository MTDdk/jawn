# Structure of a jawn project

## Typical structure of a jawn project
```
+-- src
|   \-- main
|       \-- java
|           \-- app
|               +-- config
|               |   +-- AppBootstrap.java (required)
|               |   +-- AppControllerConfig.java
|               |   \-- RouteConfig.java
|               +-- controllers
|               |   +-- IndexController.java (standard)
|               |   +-- MovieController.java
|               |   \-- SomeController.java
|               \-- models
|                   \-- Movie.java
\-- webapp
    +-- css
    +-- images
    +-- js
    +-- favico.ico
    \-- WEB-INF
        \-- views
            +-- index
            |   \-- index.st
            +-- movie
            |   \-- index.st
            +-- some
            |   +-- index.st
            |   \-- example.st (possible to reference other templates)
            \-- index.html.st (default layout)
```
## Configuration
All configuration is done in code located in `app.config`.
The `AppBootstrap.java` is the only required as this is where the most vital configuration is set.
Even though no configuration is essential, this class still needs to be present for running the framework.
More in [AppContext](appcontext.md)

Other configuration classes can be added for controlling URL-routes, database connections, or special behaviour of controllers.



## [Controllers](controllers.md)
The *controller* part of MVC, always located in the package structure `app.controllers`.

The naming convention is to always end a controller with **Controller** and extending `AppController`.
This is what the framework is looking for.

## Models
The *model* part of MVC, normally placed in the `app.models` but is not bound to this location.
Contrary to the *configuration* and *controllers*, the placement of *models* are not dictated by the framework.

## [Views](views.md)
The *view* part of MVC, ordered in subfolders within the `WEB-INF/views` named as their respective controllers.
It is essential for the naming of controllers and their corresponding views to be named identically as the view is looked up
on basis of the name of the controller.


## Static content - subfolders of `webapp`
Every direct subfolder or file within `webapp` (outside of `WEB-INF`) is automatically exposed by the web server.
This means that images in the `images` folder, without further ado, can be found on:
```
http://host:port/images/something.jpg
```
And so on for every other file or folder as well.

The folders in the example structure are only often used folders, but can be omitted and others can be added.

Because of this dynamic loading of static content, any controller can not be named likewise
as a folder of static content. If any name-clashing occurs, the static content will be given precedence.