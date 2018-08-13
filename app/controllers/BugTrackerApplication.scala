package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import models.{BugTrackerTask, TaskRepo, TaskSearch, TaskStatus}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * @author fion
  * @since 01.08.2018
  */
class BugTrackerApplication @Inject()(implicit ec: ExecutionContext, taskRepo: TaskRepo,
                                      val controllerComponents: ControllerComponents) extends BaseController {

  private val logger = Logger(getClass)

  def index = Action.async { rs =>
    Future {
      Ok(views.html.index())
    }
  }

  def newTask = Action.async(parse.json) { implicit request =>
    val placeResult = request.body.validate[BugTrackerTask]
    placeResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
        }
      },
      task => {
        taskRepo.insert(task).map(
          u => Ok(Json.obj("status" -> "OK", "message" -> ("Task '" + task.name + "' saved.")))
        )
      }
    )
  }

  def editTask = Action.async(parse.json) { implicit request =>
    val placeResult = request.body.validate[BugTrackerTask]
    placeResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
        }
      },
      task => {
        taskRepo.update(task).map(u =>
          Ok(Json.obj("status" -> "OK", "message" -> ("Task '" + task.name + "' saved."))))
      }
    )
  }

  def deleteTask(id: Long) = Action.async {
    taskRepo.delete(id).map(_ => Ok("ok"))
  }

  def statusTask(id: Long, status: String) = Action.async {
    if (TaskStatus.values.map(_.toString).exists(_.equals(status))) {
      taskRepo.changeStatus(id, TaskStatus.withName(status))
        .map(_ => Ok("ok"))
    } else {
      Future {
        NotFound("Status not found")
      }
    }
  }

  def listTask = Action.async {
    taskRepo.all.map(all => {
      val json = Json.toJson(all)
      Ok(json)
    })
  }

  def search(term: String) = Action.async {
    taskRepo.search(term).map(result => {
      val json = Json.toJson(result)
      Ok(json)
    })
  }
}


