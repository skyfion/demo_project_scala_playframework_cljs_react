package models

import org.scalatestplus.play.PlaySpec

/**
  * @author fion
  * @since 14.08.2018
  */
class SearchTest extends PlaySpec {


  "TaskSearch GET " should {
    "index and search task " in {
      val s = new TaskSearch()

      val tasks = List(
        BugTrackerTask(1L, "test-1", "test1 desc", TaskStatus.todo),
        BugTrackerTask(2L, "test-2", "test2 desc", TaskStatus.todo),
        BugTrackerTask(3L, "test-3", "test3 desc", TaskStatus.todo)
      )
      s.index(tasks)
      assert(s.search("test3").head == 3L)
      assert(s.search("test-3").length == 0)

      val newTask = BugTrackerTask(-1, "test-4", "test4 desc", TaskStatus.todo)
      val t = newTask.copy(id = 4L)
      s.add(t)
      assert(s.search("test4").length == 1)
      assert(s.search("test4").head == 4L)

      s.delete(2L)
      assert(s.search("test2").length == 0)

      s.update(t.copy(description = "test777"))
      assert(s.search("test777").head == 4L)
    }

  }
}
