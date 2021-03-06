import com.android.todoapp._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.OneInstancePerTest
import android.content.Context

class Specs extends FunSpec with ShouldMatchers with OneInstancePerTest {
  describe("Period") {
    it("should initialize") {
      // Period("After a Year") should equal(Period.AfterAYear)
    }

  }
  describe("Date") {
    val d1 = Date("2013-02-27T22:07:47.183")
    val d2 = Date("2013-02-27T23:07:47.183")
    d2.hourDifference(d1) should equal(1)
    d1.hourDifference(d2) should equal(-1)

    d2.minuteDifference(d1) should equal(60)
    d1.minuteDifference(d2) should equal(-60)
  }

  describe("Priority") {
    (new Priority(Priority.High)) should equal(Priority.High)
    (Priority("low")) should equal(Priority.Low)
    (Priority("high")) should equal(Priority.High)
    (Priority("stuffs")) should equal(Priority.Normal)
    (Priority("Normal")) should equal(Priority.Normal)
    (Priority("High")) should equal(Priority.High)
  }
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
