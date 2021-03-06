package org.kevin.app.bookcrawler.actor

import akka.actor.{Actor, ActorPath, ActorRef, Props, PoisonPill}
import scala.collection.mutable
import org.kevin.app.bookcrawler.{AbstractProcessor, Common}

object StorerActor {
    case class Saving()
    case class Collecting(url: String, section: String)
    case class Checking(url: String)
    case class CounterReducing()
}

class StorerActor(processor:AbstractProcessor, masterRefPath: String) extends Actor {

    val list = new mutable.ListBuffer[String]()
    val map = new mutable.HashMap[String,String]()
    var counter: Int = 0

    def receive = {
        case StorerActor.Collecting(url: String, section: String) => {
            if(map.contains(url)) {
                map += (url -> section)
                counter -= 1
                Common.log(s"${self.path.name}  -------->   Current map pool has ${map.size} records. Counter size is ${counter}")
                if(0 == counter) {
                    self ! StorerActor.Saving()
                    Common.log(s"${self.path.name} : Saving => ${self.path.name}")
                } else if(counter < 0) {
                    Common.log("Counter < 0")
                } 
            }

        }

        case StorerActor.Checking(url: String) => {
            if(!map.contains(url)) {
                map += (url -> "")
                counter += 1
                Common.log(s"counter ++: ${counter}")
                sender() ! ParserActor.UrlNonExisting(url)
            } else {
                //Common.log(s"${self.path.name} : has already Existed in => ${sender().path.name} %% url: ${url} | basicUrl： ${basicUrl})")
            }
        }

        case StorerActor.Saving() => {
            processor.store(map)
            context.actorSelection(masterRefPath) ! MasterActor.Ending()
        }        
    }
}
