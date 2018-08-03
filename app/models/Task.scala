package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Result
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * @author fion
  * @since 31.07.2018
  */
case class Task(id: Long = 0L, name: String, description: String, status: TaskStatus.Value)


object TaskStatus extends Enumeration {
  val todo = Value("todo")
  val in_progress = Value("in_progress")
  val done = Value("done")
}

//object Task { todo
//  implicit val taskRead: Reads[Task] = (
//    (JsPath \ "id").readNullable[Long] and
//      (JsPath \ "name").read[String] and
//      (JsPath \ "description").read[String] and
//      (JsPath \ "status").read[String]
//    ) (Task.apply _)
//
//  implicit val taskWrite: Writes[Task] = (
//    (JsPath \ "id").write[Long] and
//      (JsPath \ "name").write[String] and
//      (JsPath \ "description").write[String] and
//      (JsPath \ "status").write[String]
//    ) (unlift(Task.unapply))
//}

class TaskRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._
  private[models] val Tasks = TableQuery[TasksTable]

  implicit val taskStatusColumnType = MappedColumnType.base[TaskStatus.Value, String](
    _.toString, string => TaskStatus.withName(string))

  def all: Future[List[Task]] = db.run(Tasks.to[List].result)

//  def create(name: String, description: String, status: Status) todo

  private[models] class TasksTable(tag: Tag) extends Table[Task](tag, "task") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def status = column[TaskStatus.Value]("status")

    def * = (id, name, description, status) <> (Task.tupled, Task.unapply)

  }

}