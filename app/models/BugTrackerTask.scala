package models

import javax.inject.Inject
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author fion
  * @since 31.07.2018
  */
case class BugTrackerTask(id: Long = 0L, name: String, description: String, status: TaskStatus.Value)


object TaskStatus extends Enumeration {
  val todo = Value("todo")
  val in_progress = Value("in_progress")
  val done = Value("done")
}

object BugTrackerTask {

  implicit val taskStausReads = Reads.enumNameReads(TaskStatus)
  implicit val taskStausWrites: Writes[TaskStatus.Value] = Writes.enumNameWrites

  implicit val taskWrites: Writes[BugTrackerTask] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "status").write[TaskStatus.Value]
    ) (unlift(BugTrackerTask.unapply))

  implicit val taskReads: Reads[BugTrackerTask] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "status").read[TaskStatus.Value]
    ) (BugTrackerTask.apply _)
}

class TaskRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val db = dbConfig.db

  import dbConfig.profile.api._

  private val tasks = TableQuery[TasksTable]
  implicit val taskStatusColumnType = MappedColumnType.base[TaskStatus.Value, String](
    _.toString, string => TaskStatus.withName(string))

  val indexSearcher = new TaskSearch()
  // init index
  all.map(indexSearcher.index)

  def all: Future[List[BugTrackerTask]] = db.run(tasks.sortBy(_.id).to[List].result)

  def delete(id: Long): Future[Unit] = {
    db.run(tasks.filter(_.id === id).delete)
      .map(_ => indexSearcher.delete(id))
  }

  def update(task: BugTrackerTask) = {
    db.run(tasks.filter(_.id === task.id).update(task))
      .map(_ => indexSearcher.update(task))
  }

  def insert(task: BugTrackerTask) = {
    db.run((tasks returning tasks.map(_.id)) += task)
      .map(newId =>  {
        Logger.info("id => " + newId.toString)
        indexSearcher.add(task.copy(id = newId))
      })
  }

  def changeStatus(id: Long, value: TaskStatus.Value) = {
    val q = for {t <- tasks if t.id === id} yield t.status
    db.run(q.update(value)).map(_ => ())
  }

  def filter(ids: Array[Long]): Future[List[BugTrackerTask]] = {
    db.run(tasks.filter(_.id inSet ids.toSet).sortBy(_.id).to[List].result)
  }

  def search(term: String): Future[List[BugTrackerTask]] = {
    filter(indexSearcher.search(term))
  }

  private[models] class TasksTable(tag: Tag) extends Table[BugTrackerTask](tag, "task") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def status = column[TaskStatus.Value]("status")

    def * = (id, name, description, status) <> ((BugTrackerTask.apply _).tupled, BugTrackerTask.unapply)

  }

}

