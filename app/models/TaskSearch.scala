package models

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index._
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.QueryBuilder

class TaskSearch() {
  val MAX_RESULT = 10
  val INDEX_FIELD = "description"
  val analyzer = new StandardAnalyzer()
  val indexStory = new TaskSearchIndexStore(analyzer, docFromTask)

  def docFromTask(task: BugTrackerTask): Document = {
    val doc = new Document()
    // add description
    val fieldType = new FieldType()
    fieldType.setStored(true)
    fieldType.setTokenized(true)
    doc.add(new Field(INDEX_FIELD, task.description, fieldType))
    // add id
    val idFieldType = new FieldType()
    idFieldType.setStored(true)
    idFieldType.setTokenized(false)
    idFieldType.setIndexOptions(IndexOptions.NONE)
    doc.add(new Field("id", task.id.toString, idFieldType))
    doc
  }

  def search(term: String): Array[Long] = {
    val indexReader: DirectoryReader = _
    try {
      val indexReader = DirectoryReader.open(indexStory.store)
      val indexSearcher = new IndexSearcher(indexReader)
      val q = new QueryBuilder(analyzer).createPhraseQuery(INDEX_FIELD, term)
      val topDocs = indexSearcher.search(q, MAX_RESULT)
      topDocs.scoreDocs.map(d => {
        indexSearcher.doc(d.doc).get("id").toLong
      })
    } finally {
      indexReader.close()
    }
  }
  // init
  def index(tasks: List[BugTrackerTask]): Unit = {
    indexStory.addTasks(tasks)
  }

  def update(task: BugTrackerTask): Unit = {
    indexStory.update(task)
  }

  def delete(taskId: Long): Unit = {
    indexStory.delete(taskId)
  }
}

class TaskSearchIndexStore(analyzer: Analyzer, toDocFun: Function[BugTrackerTask, Document]) {
  val store = new RAMDirectory()

  def addTasks(tasks: List[BugTrackerTask]): Unit = {
    val writer: IndexWriter = _
    try {
      val writer = new IndexWriter(store, new IndexWriterConfig(analyzer))
      tasks.foreach(task => writer.addDocument(toDocFun(task)))
    } finally {
      writer.close()
    }
  }

  def update(task: BugTrackerTask): Unit = {
    val writer: IndexWriter = _
    try {
      writer.updateDocument(
        new Term("id", task.id.toString),
        toDocFun(task)
      )
    } finally {
      writer.close()
    }
  }

  def delete(id: Long): Unit = {
    val writer: IndexWriter = _
    try {
      writer.deleteDocuments(new Term("id", id.toString))
    } finally {
      writer.close()
    }
  }


}


