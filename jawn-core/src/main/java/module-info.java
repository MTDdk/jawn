module net.javapla.jawn.core {

  exports net.javapla.jawn.core;
  exports net.javapla.jawn.core.util;
  
  /*
   * Core dependencies
   */
  requires transitive org.slf4j;
  requires transitive typesafe.config;
  requires jakarta.inject;
  
  requires org.objectweb.asm;
  requires org.objectweb.asm.util;
  requires org.objectweb.asm.tree;
  
}