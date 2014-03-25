package com.carematics.gvws

import groovyx.gpars.dataflow.DataflowVariable

import java.util.concurrent.TimeUnit

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Handler
import org.vertx.java.core.impl.DefaultFutureResult
import org.vertx.java.core.json.JsonObject
import org.vertx.java.platform.PlatformLocator
import org.vertx.java.platform.PlatformManager

class MainVerticle extends Verticle {
  static def port = 8888
  static def webRoot = 'src/main/web'
  static PlatformManager vertxPlatformManager = PlatformLocator.factory.createPlatformManager()
  
  def JsonObject getWebServerConfig() {
    new JsonObject([
      port: port,
      'web_root': webRoot,
      bridge: true,
      inbound_permitted: [[:]],
      outbound_permitted: [[:]]])
  }
  
  def deployWebServer() {
    String webModule = 'io.vertx~mod-web-server~2.0.0-final'
    DataflowVariable<Boolean> vertxWebServerStarted = new DataflowVariable()
    Handler handler = [
      handle: { DefaultFutureResult result ->
        vertxWebServerStarted << result.succeeded()
      }
    ] as Handler
    vertxPlatformManager.deployModule(webModule, getWebServerConfig(), 1, handler)
    def timeout = 10
    try {
      if(!vertxWebServerStarted.get(timeout, TimeUnit.SECONDS)) {
        //System.err.println 'Vert.x web server startup failed!'
      }
    } catch (Throwable t) {
      //System.err.println "Vert.x web server startup timed out!"
    }
  }
  
  def start() {
    deployWebServer()
    println 'started MainVerticle at ' + new Date()
  }
  
  def stop() {
    Handler handler = [
      handle: { println 'undeployed all verticles and modules' }
    ] as Handler
    vertxPlatformManager.undeployAll(handler)
  }
}
