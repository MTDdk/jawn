# JavaWebPlanet

**JavaWebPlanet** strives to be a simple library for web MVC development. 

It is abbreviated "**jawn**", which is a homophone of "yawn", as this project is supposed to be so simple to use
that you can do it almost do it your sleep.

## Principles

## Documentation
* [Getting started](docs/getting_started)
* [Structure of a JavaWebPlanet project](docs/structure_of_jawn_project)
* [Controllers](docs/controllers)

* [Image manipulation](docs/imagemanipulation)
* [Environments](docs/environments)
* [Application wide context](docs/appcontext)

## Get it

### Maven
```xml
<dependency>
  <groupId>net.javapla.jawn</groupId>
  <artifactId>jawn</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle
```groovy
compile 'net.javapla.jawn:jawn:0.1.0'
```



## Acknowledgement
This started out as a fork of [ActiveWeb](https://github.com/javalite/activeweb),
but it has moved pretty far away from the original way of thinking of ActiveWeb
when it comes to handling URLs, writing controllers and the inner workings.
Also the notion of setting a controller to be RESTful has been removed.

When this is said, however, much of the usage is the same, so is should be
seamless to migrate from one to the other.
