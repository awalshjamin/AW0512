package org.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import org.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HolidayTest {

  private Session session;
  private Transaction transaction;

  @Before
  public void setUp() throws IOException {
    session = HibernateUtil.getSession();
    transaction = session.beginTransaction();
    createTestData();
  }

  @After
  public void tearDown() {
    transaction.rollback();
    session.close();
  }

  @Test
  public void testIsAWeekend() {
    LocalDate saturday = LocalDate.of(2023, 5, 13);
    Assert.assertEquals(Holiday.isAWeekend(saturday), true);

    LocalDate sunday = LocalDate.of(2023, 5, 14);
    Assert.assertEquals(Holiday.isAWeekend(sunday), true);

    LocalDate weekday = LocalDate.of(2023, 5, 10);
    Assert.assertEquals(Holiday.isAWeekend(weekday), false);
  }

  private void createTestData() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("holiday-test-data.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] fields = line.split(",");

        String name = fields[0];
        boolean isFixedDate = Boolean.parseBoolean(fields[1]);
        Month month = Month.valueOf(fields[2]);
        int dayOfMonth = Integer.parseInt(fields[3]);
        boolean followsWeekendObserv = Boolean.parseBoolean(fields[4]);
        boolean followsPatternObserv = Boolean.parseBoolean(fields[5]);
        DayOfWeek dayOfWeek = fields[6].equals("null") ? null : DayOfWeek.valueOf(fields[6]);
        int weekNumForPattern = Integer.parseInt(fields[7]);

        Holiday holiday = new Holiday(name, isFixedDate, month, dayOfMonth,
            followsWeekendObserv, followsPatternObserv, dayOfWeek, weekNumForPattern);
        session.save(holiday);

    }
    reader.close();
  }

  @Test
  public void testGetHolidays() {

    List<Holiday> holidays = session.createQuery("SELECT h FROM Holiday h WHERE h.name = 'New Year''s Day'").getResultList();

    assertEquals(1, holidays.size());
    assertEquals("New Year's Day", holidays.get(0).getName());
    assertEquals(true, holidays.get(0).isFixedDate());
    assertEquals(Month.JANUARY, holidays.get(0).getMonthEnum());
    assertEquals(1, holidays.get(0).getDayOfMonth());
    assertEquals(false, holidays.get(0).doesFollowsWeekendObserv());
    assertEquals(false, holidays.get(0).doesFollowPatternObserv());
    assertEquals(null, holidays.get(0).getDayOfWeek());
    assertEquals(0, holidays.get(0).getWeekNumForPattern());

  }

  @Test
  public void test4thOfJuly() {
    List<Holiday> holidays = session.createQuery("SELECT h FROM Holiday h WHERE h.name = 'Independence Day'").getResultList();

    assertEquals(DayOfWeek.TUESDAY, holidays.get(0).getHoliday(Year.of(2023)).getDayOfWeek());
    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2022)).getDayOfWeek());
    assertEquals(DayOfWeek.THURSDAY, holidays.get(0).getHoliday(Year.of(2024)).getDayOfWeek());
    assertEquals(DayOfWeek.FRIDAY, holidays.get(0).getHoliday(Year.of(2020)).getDayOfWeek());
    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2021)).getDayOfWeek());
  }

  @Test
  public void testLaborDay() {

    List<Holiday> holidays = session.createQuery("SELECT h FROM Holiday h WHERE h.name = 'Labor Day'").getResultList();

    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2023)).getDayOfWeek());
    assertEquals(4, holidays.get(0).getHoliday(Year.of(2023)).getDayOfMonth());
    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2022)).getDayOfWeek());
    assertEquals(5, holidays.get(0).getHoliday(Year.of(2022)).getDayOfMonth());
    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2024)).getDayOfWeek());
    assertEquals(2, holidays.get(0).getHoliday(Year.of(2024)).getDayOfMonth());
    assertEquals(DayOfWeek.MONDAY, holidays.get(0).getHoliday(Year.of(2020)).getDayOfWeek());
    assertEquals(7, holidays.get(0).getHoliday(Year.of(2020)).getDayOfMonth());
  }

  @Test
  public void testisAHoliday() {
    assertTrue(Holiday.isAHoliday(LocalDate.of(2023, 7, 4)));
    assertFalse(Holiday.isAHoliday(LocalDate.of(2020, 7, 4)));
    assertTrue(Holiday.isAHoliday(LocalDate.of(2020, 7, 3)));

    assertTrue(Holiday.isAHoliday(LocalDate.of(2020, 9, 7)));
    assertTrue(Holiday.isAHoliday(LocalDate.of(2021, 9, 6)));
    assertTrue(Holiday.isAHoliday(LocalDate.of(2022, 9, 5)));

    assertFalse(Holiday.isAHoliday(LocalDate.of(2022, 9, 15)));
  }
}

