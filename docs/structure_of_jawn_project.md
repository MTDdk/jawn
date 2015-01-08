# Structure of a JavaWebPlanet project

*To be finished*

## Typical structure of a jawn project
```
\- src
|  \- main
|     \- java
|        \- app
|           \- config
|           |  |- AppBootstrap.java (required)
|           |  |- AppControllerConfig.java
|           |  |- RouteConfig.java
|           \- controllers
|           |  |- ArticleController.java
|           |  |- IndexController.java (standard)
|           |  |- SomeController.java
|           \- models
|              |- Article.java
\- webapp
   \- css
   \- images
   \- js
   |- favico.ico
   \- WEB-INF
      \- views
         \- article
         |  |- index.st
         \- index
         |  |- index.st
         \- some
         |  |- index.st
         |  |- example.st (possible to reference other templates)
         |- index.html.st (default layout)
```
## Configuration



## [Controllers](controllers.md)
Always located in the package structure `app.controllers`.

The naming convention is to always end a controller with **Controller** and extending `AppController`.
This is what the framework is looking for.

## Models
Normally placed in the `app.models` but is not bound to this location.
Contrary to the *configuration* and *controllers*, the placement of *models* are not dictated by the framework.

## [Views](views.md)
Ordered in subfolders within the `WEB-INF/views` folder named as their respective controllers.


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