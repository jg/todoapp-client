import com.android.todoapp._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.OneInstancePerTest
import android.content.Context

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

    describe("attributes") {
      task.created_at = 12
      task.created_at should equal (12)

      task.updated_at = 15
      task.updated_at should equal (15)
    }

    describe("Tasks.list") {
      it("should delegate to ListBuffer") {
        Tasks.length should equal(0)
      }
    }
  }
}
