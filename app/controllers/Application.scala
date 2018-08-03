package controllers

import javax.inject.Inject
import models.TaskRepo
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author fion
  * @since 01.08.2018
  */
class Application @Inject()(implicit ec: ExecutionContext, taskRepo: TaskRepo,
                            val controllerComponents: ControllerComponents) extends BaseController {

  def index = Action.async { rs =>
    Future {
      Ok(views.html.index())
    }
  }

//  def listTask(taskId: Long) = Action.async {
//    implicit rs =>
//      taskRepo.all.map(tasks => )
//  }

}
