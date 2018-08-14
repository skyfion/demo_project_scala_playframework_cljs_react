package models

import java.io.Closeable

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index._
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class TaskSearch() {
  val MAX_RESULT = 10
  val INDEX_FIELD = "description"
  val analyzer = new StandardAnalyzer()
  val indexStory = new TaskSearchIndexStore(analyzer, docFromTask)

  def docFromTask(task: BugTrackerTask): Document = {
    val doc = new Document()
    doc.add(new TextField(INDEX_FIELD, task.description, Field.Store.YES))
    doc.add(new StringField("id", task.id.toString, Field.Store.YES))
    doc
  }

  def search(term: String): Array[Long] = {

    val res = TryWith(DirectoryReader.open(indexStory.store)) {
      indexReader => {
        val indexSearcher = new IndexSearcher(indexReader.asInstanceOf[DirectoryReader])
        val qp = new QueryParser(INDEX_FIELD, analyzer)
        val topDocs = indexSearcher.search(qp.parse(term), MAX_RESULT)
        topDocs.scoreDocs.map(d => {
          indexSearcher.doc(d.doc).get("id").toLong
        })
      }
    }

    res match {
      case Success(result) => result
      case Failure(e) =>
        e.printStackTrace()
        Array()
    }
  }

  // init
  def index(tasks: List[BugTrackerTask]): Unit = {
    indexStory.initTasks(tasks)
  }

  def add(task: BugTrackerTask): Unit = {
    indexStory.addTasks(task)
  }

  def update(task: BugTrackerTask): Unit = {
    println("update")
    indexStory.update(task)
  }

  def delete(taskId: Long): Unit = {
    indexStory.delete(taskId)
  }
}

class TaskSearchIndexStore(analyzer: Analyzer, toDocFun: Function[BugTrackerTask, Document]) {
  val store = new RAMDirectory()

  def initTasks(tasks: List[BugTrackerTask]): Unit = {
    val defWriterConfig = new IndexWriterConfig(analyzer)
    defWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
    defWriterConfig.setCommitOnClose(true)
    TryWith(new IndexWriter(store, defWriterConfig))(writer => {
      tasks.foreach(task => writer.asInstanceOf[IndexWriter].addDocument(toDocFun(task)))
    })
  }

  def addTasks(task: BugTrackerTask): Unit = {
    val writerConfig = new IndexWriterConfig(analyzer)
    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.APPEND)

    TryWith(new IndexWriter(store, writerConfig))(writer => {
      writer.asInstanceOf[IndexWriter].addDocument(toDocFun(task))
    })
  }

  def update(task: BugTrackerTask): Unit = {
    val writerConfig = new IndexWriterConfig(analyzer)
    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
    writerConfig.setCommitOnClose(true)
    TryWith(new IndexWriter(store, writerConfig))(writer => {
      writer.asInstanceOf[IndexWriter].updateDocument(
        new Term("id", task.id.toString),
        toDocFun(task)
      )
    })
  }

  def delete(id: Long): Unit = {
    val writerConfig = new IndexWriterConfig(analyzer)
    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.APPEND)
    writerConfig.setCommitOnClose(true)
    TryWith(new IndexWriter(store, writerConfig))(writer => {
      writer.asInstanceOf[IndexWriter].deleteDocuments(new Term("id", id.toString))
    })
  }

}

object TryWith {
  def apply[C <: Closeable, R](resGen: => C)(r: Closeable => R): Try[R] =
    Try(resGen).flatMap(closeable => {
      try {
        Success(r(closeable))
      }
      catch {
        case NonFatal(e) => Failure(e)
      }
      finally {
        try {
          closeable.close()
        }
        catch {
          case e: Exception =>
            System.err.println("Failed to close Resource:")
            e.printStackTrace()
        }
      }
    })
}


