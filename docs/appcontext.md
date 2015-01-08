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
```java
public class IndexController extends AppController {
    public void index(){
         view("framework_name", appContext().get("app_name"));
    }
}
```

The `AppContext` is mostly

context.setEncoding("UTF-8");