package models

import javax.inject.Inject
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
  def edit(task: BugTrackerTask) = {

  }


  def changeStatus(id: Long, status: TaskStatus.Value) = {
    list.find(_.id == id).map(_.copy(status = status)).foreach(
      t => {
        delete(id)
        list = list ::: List(t)}
    )
  }


  var list: List[BugTrackerTask] = List(
    BugTrackerTask(1L, "test 1", "desc 1", TaskStatus.todo),
    BugTrackerTask(2L, "test 2", "desc 2", TaskStatus.in_progress),
    BugTrackerTask(3L, "test 3", "desc 3", TaskStatus.done)
  )

  def delete(id: Long) = {
    list = list.filter(_.id != id)
  }

  def save(task: BugTrackerTask): Unit = {
    val id = list.map(task => task.id).max + 1
    list = list ::: List(task.copy(id = id))
  }

  implicit val taskStausReads = Reads.enumNameReads(TaskStatus)
  implicit val taskStausWrites: Writes[TaskStatus.Value] = Writes.enumNameWrites

  implicit val taskWrites: Writes[BugTrackerTask] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "status").write[TaskStatus.Value]
    ) (unlift(BugTrackerTask.unapply))

  implicit  val taskReads: Reads[BugTrackerTask] = (
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

  def all: Future[List[BugTrackerTask]] = db.run(tasks.sortBy(_.id).to[List].result)

  def delete(id: Long): Future[Unit] = {
    db.run(tasks.filter(_.id === id).delete).map(_ => ())
  }

  def update(task: BugTrackerTask) = {
    db.run(tasks.filter(_.id === task.id).update(task)).map(_ => ())
  }

  def insert(task: BugTrackerTask) = {
    db.run(tasks += task).map(_ => ())
  }

  def changeStatus(id: Long, value: TaskStatus.Value) = {
    val q = for {t <- tasks if t.id === id} yield t.status
    db.run(q.update(value)).map(_ => ())
  }
  //  def create(name: String, description: String, status: Status) todo

  private[models] class TasksTable(tag: Tag) extends Table[BugTrackerTask](tag, "task") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def status = column[TaskStatus.Value]("status")

    def * = (id, name, description, status) <> ((BugTrackerTask.apply _).tupled, BugTrackerTask.unapply)

  }

}