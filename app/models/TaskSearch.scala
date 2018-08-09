package models

import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index._
import org.apache.lucene.search.{IndexSearcher, TermQuery}
import org.apache.lucene.store.RAMDirectory

class TaskSearch {

}

//class TaskIndexer(tasks: List[BugTrackerTask]) {
//  val directory = new RAMDirectory()
//  lazy val searcher = new IndexSearcher()
//
//  def fromTask(task: BugTrackerTask): Document = {
//    var doc = new Document
//    var indexableFieldType = new FieldType()
//    indexableFieldType.setStored(true)
//    indexableFieldType.setIndexOptions(IndexOptions.DOCS)
//    indexableFieldType.setTokenized(true)
//    var notTokenizedType = new FieldType()
//    notTokenizedType.setTokenized(false)
//    notTokenizedType.setStored(true)
//    doc.add(new Field("id", task.id.toString, notTokenizedType))
//    doc.add(new Field("description", task.description, notTokenizedType))
//    doc
//  }
//
//  def indexAll() {
//    val writer = new IndexWriter(directory, )
//    tasks.foreach(writer.addDocument(fromTask(_)))
//    writer.close()
//  }
//
//  def search(term: String): List[Long] = {
//    val query = new TermQuery(new Term("description", term))
//    val results = searcher.search(query, 10)
//    results.scoreDocs
//      .map(r => searcher.doc(r.doc).getField("id").numericValue().longValue())
//      .toList
//  }
//
//  def delTask(id: Long): Unit = {
//
//  }
//
//  def updateTask(task: BugTrackerTask) = {
//
//  }
//
//  indexAll()
//}
