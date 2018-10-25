package util

import java.text.SimpleDateFormat
import java.util.Calendar

import com.twitter.util.logging.Logging

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import PipeOperator.Pipe
class TimestampUtils extends Logging {
  val format      = new SimpleDateFormat("yyyy-MM-dd")
  val monthFormat = new SimpleDateFormat("yyyy-MM")

  def currentTimestamp(): Long =
    System.currentTimeMillis()

  def timestampToString(ts: Long): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    format.format(ts)
  }

  def timestampToDate(ts: Long): String =
    format.format(ts)

  def getTimeStamp(date: String): Long =
    format.parse(date).getTime

  def dateInRange(startDate: String, endDate: String, date: String): Boolean = {
    info(s"[dateInRange] startDate: $startDate, endDate: $endDate, date: $date")
    val start  = getTimeStamp(startDate)
    val end    = getTimeStamp(endDate)
    val target = getTimeStamp(date)

    info(s"[dateInRange] start: $start, end:$end, target: $target")

    (target >= start, target <= end) match {
      case (true, true) => true
      case _            => false
    }
  }

  def getPeriodDates(startDate: String, endDate: String): List[String] = {
    val startDateInMillis                  = getTimeStamp(startDate)
    val endDateInMillis                    = getTimeStamp(endDate)
    val requestPeriods: ListBuffer[String] = ListBuffer.empty[String]
    endDateInMillis - startDateInMillis match {
      case s if s == 0 => requestPeriods += timestampToDate(getTimeStamp(startDate))
      case s if s > 0 => {
        for (dateInMillis <- startDateInMillis to endDateInMillis by 86400000L) {
          requestPeriods += format.format(dateInMillis)
        }
      }
      case _ =>
        error(s"startDate ${startDate} should smaller than endDate ${endDate}")
    }

    requestPeriods.toList.#!("dates")
  }

  def getPeriodMonths(startMonth: String, endMonth: String): List[String] = {
    val startMonthDateInMillis             = getDateOfMonthInMillis(startMonth)
    val endMonthDateInMillis               = getDateOfMonthInMillis(endMonth)
    val requestPeriods: ListBuffer[String] = ListBuffer.empty[String]
    endMonthDateInMillis - startMonthDateInMillis match {
      case s if s == 0 =>
        requestPeriods += monthFormat.format(startMonthDateInMillis)
      case s if s > 0 => {
        for (dateInMillis <- startMonthDateInMillis to endMonthDateInMillis by 86400000L) {
          requestPeriods += monthFormat.format(dateInMillis)
        }
      }
      case _ =>
        error(s"startMonth ${startMonth} should smaller than endMonth ${endMonth}")
    }

    requestPeriods.toList.distinct.#!("month")
  }

  def isValidMonthFormat(month: String): Boolean =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => true
      case Failure(_) => false
    }

  def isValidDateFormat(date: String): Boolean =
    Try(format.parse(date).getTime) match {
      case Success(s) => true
      case Failure(_) => false
    }

  def getMonthlyDates(month: String): List[String] =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(s)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DATE, -1)
        getPeriodDates(format.format(s), format.format(calendar.getTimeInMillis))
      }
      case Failure(_) => List.empty[String]
    }

  def getDateOfMonthInMillis(month: String): Long =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => {
        s
      }
      case Failure(_) => 0l
    }

  def getStartDateOfMonth(month: String): String =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => {
        format.format(s)
      }
      case Failure(_) => ""
    }

  def getEndDateOfMonth(month: String): String =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(s)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DATE, -1)
        format.format(calendar.getTimeInMillis)
      }
      case Failure(_) => ""
    }

  def isCurrentMonth(month: String): Boolean =
    Try(monthFormat.parse(month).getTime) match {
      case Success(s) => {
        val currentMonth = monthFormat.format(currentTimestamp())
        currentMonth.#!("currentMonth ").equalsIgnoreCase(format.format(s))
      }
      case Failure(_) => false
    }
}
