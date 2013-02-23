import com.android.todoapp._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.OneInstancePerTest
import android.content.Context

class Specs extends FunSpec with ShouldMatchers with OneInstancePerTest {
  describe("Time") {
    val time = new Time(8, 12)

    it("toString") {
      time.toString should equal("08:12")
    }

    it(".fromMinutes") {
      Time.fromMinutes(123).toString should equal("02:03")
      Time.fromMinutes(3).toString should equal("00:03")
      Time.fromMinutes(60).toString should equal("01:00")
      Time.fromMinutes(60*24).toString should equal("24:00")
    }

    it(".toInt") {
      Time(4, 23).toInt should equal (4*60+23)
    }

  }
  /*

  describe("Tasks") {
    val task = new Task("title")

    it("has created_at/updated_at attributes") {
      task.created_at = 12
      task.created_at should equal (12)

      task.updated_at = 15
      task.updated_at should equal (15)
    }
  }
  */
}
