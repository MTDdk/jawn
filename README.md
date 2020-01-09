```
     ____.  _____  __      _________   
    |    | /  _  \/  \    /  \      \  
    |    |/  /_\  \   \/\/   /   |   \ 
/\__|    /    |    \        /    |    \ 
\________\____|__  /\__/\  /\____|__  /
  web framework  \/      \/         \/ http://www.javapla.net
```
# Continue being lazy, almost drowsy

**jawn** strives to be a super simple and productive framework for web development. 

```java
import net.javapla.jawn.core.Jawn;

public class App extends Jawn {
  
  {
    get("/", Results.text("Up and running!"));
    get("/json", Results.json(new Object()));
  }

  public static void main(String[] args) {
    run(App.class, args);
  }
}
```


## Documentation
Head over to the **[wiki](https://github.com/MTDdk/jawn/wiki)** for a thorough walk-through.

If you ever want to try this framework out for yourself or in the wild, do not hesitate to give me a ping alvn@alvn.dk

## Latest version
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.javapla.jawn/jawn-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.javapla.jawn/jawn-core)
[![Build Status](https://travis-ci.org/MTDdk/jawn.svg?branch=1.0.x)](https://travis-ci.org/MTDdk/jawn)
