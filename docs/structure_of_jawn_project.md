# Structure of a jawn project

*To be finished*

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
   \- WEB-INF
      \- views
         \- article
         |  |- index.st
         \- index
         |  |- index.st
         \- some
         |  |- index.st
         |  |- example.st (possible to reference other templates)
         |  |- index.html.st (possible to override default layout for a single controller)
         |- index.html.st (default layout)
```
## Configuration

## Controllers

## Models

## Views
index.st

Using StringTemplate as default.
The original usage of FreeMarker is still possible, however.