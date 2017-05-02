package org.kevin.app.bookcrawler.actor

import akka.actor.{Actor, ActorPath, ActorRef, Props, PoisonPill}
import org.kevin.app.bookcrawler.{Common}

object MasterActor {  
    case class Starting(basicUrl: String)

    case class Ending()

    def propsCrawlerActor(masterPath: String): Props = Props(new CrawlerActor(masterPath))

    var masterRefPath: String = ""
    var startTime: Long = 0L
    var endTime: Long = 0L
}

class MasterActor extends Actor {

    def receive = {

        case MasterActor.Ending() => {
            MasterActor.endTime = Common.nanoTime
            val interval: Float = (MasterActor.endTime - MasterActor.startTime) / 1000000
            Common.log(s"interval: ${interval}")
            Common.log("System Shutdown!")
            context.system.shutdown
            // self ! PoisonPill
        }

        case MasterActor.Starting(basicUrl: String) => {
            MasterActor.startTime = Common.nanoTime
            val actorRef = context.actorOf(MasterActor.propsCrawlerActor(self.path.toString), name = "CrawlerActor_Basic")
            actorRef ! CrawlerActor.Crawling(basicUrl)
        }
    }
}