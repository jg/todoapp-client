import com.android.todoapp._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.OneInstancePerTest

class Specs extends FunSpec with ShouldMatchers with OneInstancePerTest {

  describe("Task") {
    val task = new Task("title")

    it("should initialize with task title") {
      task.title should equal("title")
    }

    it("should allow for body assignment") {
      task.body = "body"
      task.body should equal("body")
    }
  }

  describe("Tasks") {
    val task = new Task("title")

    it("should allow for adding and accessing tasks") {
      Tasks.add(task)
      Tasks.head.title should equal ("title")
    }
  }
}
