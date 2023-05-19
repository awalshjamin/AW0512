package org.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.HibernateException;
import org.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

@Entity
@Table(name = "holiday")
public class Holiday {

  protected static final ConcurrentHashMap<String, LocalDate> holidayDateCache = new ConcurrentHashMap<>();
  static final ConcurrentHashMap<LocalDate, Boolean> isDateAHoliday = new ConcurrentHashMap<>();

  private static final String QUERY_FOR_PATTERN_HOLIDAYS = "SELECT h FROM Holiday h WHERE h.monthOfYear = :month and h.followsPatternObserv = true";
  private static final String QUERY_FOR_FIXED_HOLIDAYS = "SELECT h FROM Holiday h WHERE h.monthOfYear = :month and h.isFixedDate = true and h.dayOfMonth >= :day1 and h.dayOfMonth <= :day2";


  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name = "holiday_id")
  private Long holidayId;

  @Column(name = "name")
  private String name;

  @Column(name = "is_fixed_date")
  private boolean isFixedDate;

  @Column(name = "month_of_year")
  private int monthOfYear;

  @Column(name = "day_of_month")
  private int dayOfMonth;

  @Column(name = "follows_weekend_observ")
  private boolean followsWeekendObserv;

  @Column(name = "follows_pattern_observ")
  private boolean followsPatternObserv;

  @Column(name = "fixed_day_of_week")
  private int fixedDayOfWeek;

  @Column(name = "week_num")
  private int weekNumForPattern;

  @Transient
  private Month monthEnum;
  @Transient
  private DayOfWeek dayOfWeekEnum;

  public Holiday(){

  }

  public Holiday(String name, boolean isFixedDate, Month month, int dayOfMonth,
      boolean followsWeekendObserv, boolean followsPatternObserv, DayOfWeek dayOfWeek,
      int weekNumForPattern) {
    this.name = name;
    this.isFixedDate = isFixedDate;
    setMonthEnum(month);
    this.dayOfMonth = dayOfMonth;
    this.followsWeekendObserv = followsWeekendObserv;
    this.followsPatternObserv = followsPatternObserv;
    setDayOfWeek(dayOfWeek);
    this.weekNumForPattern = weekNumForPattern;
  }

  protected static boolean isAWeekend(LocalDate date) {
    return date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek()
        .equals(DayOfWeek.SUNDAY);
  }

  public static Boolean isAHoliday(LocalDate dateToCheck) {

    synchronized (isDateAHoliday) {
      if (isDateAHoliday.containsKey(dateToCheck)) {
        return isDateAHoliday.get(dateToCheck);
      }
    }

    int month = dateToCheck.getMonth().getValue();
    int year = dateToCheck.getYear();

    Session session;
    try {
      session = HibernateUtil.getSession();
      Query<Holiday> holidayQueryFixed = session.createQuery(QUERY_FOR_PATTERN_HOLIDAYS, Holiday.class);
      holidayQueryFixed.setParameter("month", month);
      if (processHolidayQuery(holidayQueryFixed, year, dateToCheck)){
        return true;
      };

      Query<Holiday> holidayQueryPattern = session.createQuery(QUERY_FOR_FIXED_HOLIDAYS, Holiday.class);

      int day1 = dateToCheck.getDayOfMonth() - 2 <= 0 ? 1 : dateToCheck.getDayOfMonth() - 2;
      int day2 = dateToCheck.getDayOfMonth() + 2 >= 31 ? 31 : dateToCheck.getDayOfMonth() + 2;

      holidayQueryPattern.setParameter("month", month);
      holidayQueryPattern.setParameter("day1", day1);
      holidayQueryPattern.setParameter("day2", day2);
      if(processHolidayQuery(holidayQueryPattern, year, dateToCheck)){
        return true;
      };
    } catch (HibernateException ex) {
      ex.printStackTrace();
    }

    synchronized (isDateAHoliday) {
      isDateAHoliday.putIfAbsent(dateToCheck, false);
    }
    return false;
  }

  protected static Boolean processHolidayQuery(Query<Holiday> query, int year, LocalDate date){
    List<Holiday> holidays = query.getResultList();

    for (Holiday holiday : holidays) {
      if (holiday.getHoliday(Year.of(year)).equals(date)) {
        return true;
      }
    }

    return false;
  }

  public LocalDate getHoliday(Year year) {
    String key = getKey(year);
    synchronized (holidayDateCache) {
      if (holidayDateCache.containsKey(key)) {
        return holidayDateCache.get(key);
      }
    }

    LocalDate holiday = null;

    if (isFixedDate) {
      holiday = LocalDate.of(year.getValue(), monthEnum, dayOfMonth);

      if (followsWeekendObserv) {
        if (isAWeekend(holiday)) {
          if (holiday.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            holiday = holiday.minusDays(1);
          } else {
            holiday = holiday.plusDays(1);
          }
        }
      }
    } else if (followsPatternObserv) {
      LocalDate firstDay = LocalDate.of(year.getValue(), monthEnum, 1);
      DayOfWeek day = firstDay.getDayOfWeek();

      int daysToFirstOccurrence = ((dayOfWeekEnum.getValue() - day.getValue() + 7) % 7);

      holiday = firstDay.plusDays(daysToFirstOccurrence);
      holiday = holiday.plusWeeks(weekNumForPattern - 1);
    }

    if (holiday != null) {
      synchronized (holidayDateCache) {
        holidayDateCache.put(key, holiday);
      }
      synchronized (isDateAHoliday) {
        isDateAHoliday.put(holiday, true);
      }
    }

    return holiday;
  }

  protected String getKey(Year year) {
    return name + year.getValue();
  }

  public Long getHolidayId() {
    return holidayId;
  }

  public void setHolidayId(Long holidayId) {
    this.holidayId = holidayId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isFixedDate() {
    return isFixedDate;
  }

  public void setFixedDate(boolean fixedDate) {
    isFixedDate = fixedDate;
  }

  public Month getMonthEnum() {
    return monthEnum;
  }

  public void setMonthEnum(Month monthEnum) {
    this.monthEnum = monthEnum;
    this.monthOfYear = monthEnum.getValue();
  }

  public void setMonthOfYear(int monthOfYear)
  {
    this.monthOfYear = monthOfYear;
    setMonthEnum(Month.of(monthOfYear));
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public void setDayOfMonth(int dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  public boolean doesFollowsWeekendObserv() {
    return followsWeekendObserv;
  }

  public void setFollowsWeekendObserv(boolean followsWeekendObserv) {
    this.followsWeekendObserv = followsWeekendObserv;
  }

  public boolean doesFollowPatternObserv() {
    return followsPatternObserv;
  }

  public void setFollowsPatternObserv(boolean followsPatternObserv) {
    this.followsPatternObserv = followsPatternObserv;
  }

  public DayOfWeek getDayOfWeek() {
    return dayOfWeekEnum;
  }

  public void setDayOfWeek(DayOfWeek dayOfWeek) {
    this.dayOfWeekEnum = dayOfWeek;
    this.fixedDayOfWeek = dayOfWeek == null ? 0 : dayOfWeek.getValue();
  }

  public void setFixedDayOfWeek(int fixed_day_of_week) {
    this.fixedDayOfWeek = fixed_day_of_week;
    setDayOfWeek(DayOfWeek.of(fixed_day_of_week));
  }

  public int getWeekNumForPattern() {
    return weekNumForPattern;
  }

  public void setWeekNumForPattern(int weekNumForPattern) {
    this.weekNumForPattern = weekNumForPattern;
  }
}
