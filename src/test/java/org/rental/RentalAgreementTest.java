package org.rental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.data.Brand;
import org.data.Charges;
import org.data.Holiday;
import org.data.Rental;
import org.data.Tool;
import org.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RentalAgreementTest {

  private static Session session;
  private static Transaction transaction;

  private final Charges CHAINSAW = new Charges("Chainsaw", new BigDecimal(1.49), true, false, true);
  private final Charges LADDER = new Charges("Ladder", new BigDecimal(1.99), true, true, false);
  private final Charges JACKHAMMER = new Charges("Jackhammer", new BigDecimal(2.99), true, false,
      false);

  private final ToolForTesting CHNS = new ToolForTesting("CHNS", "Chainsaw", Brand.STIHL, CHAINSAW);
  private final ToolForTesting LADW = new ToolForTesting("LADW", "Ladder", Brand.WERNER, LADDER);
  private final ToolForTesting JAKD = new ToolForTesting("JAKD", "Chainsaw", Brand.DEWALT, JACKHAMMER);
  private final ToolForTesting JARK = new ToolForTesting("JARK", "Chainsaw", Brand.RIDGID, JACKHAMMER);

  @Before
  public void setUp() {
    session = HibernateUtil.getSession();
    transaction = session.beginTransaction();
  }

  @After
  public void tearDown() {
    transaction.rollback();
    session.close();
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
  public void testCalculateDueDate() {
    Rental rental = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreement(rental);
    Assert.assertEquals(agreement.getDueDate(), LocalDate.of(2023, 5, 26));
  }

  @Test
  public void testCalculateChargeDays() {
    Rental rentalNoWeekend = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    Assert.assertEquals(agreement.getChargeDays(), 8);

    Rental rentalWithWeekend = new Rental("LADW", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement2 = new RentalAgreementForTest(rentalWithWeekend, LADW);
    Assert.assertEquals(agreement2.getChargeDays(), 10);

    Rental rentalWithNoHolidayNoWeekend = new Rental("JARK", 4, 70, LocalDate.of(2023, 5, 26));
    RentalAgreement agreement3 = new RentalAgreementForTest(rentalWithNoHolidayNoWeekend, JARK);
    Assert.assertEquals(agreement3.getChargeDays(), 1);
  }

  @Test
  public void testCalculatePreDiscountCharge() throws IOException {
    createTestData();
    Rental rentalNoWeekend = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    Assert.assertEquals(agreement.formatPrice(agreement.getPreDiscountCharge()), "$11.92");

    Rental rentalWithWeekend = new Rental("LADW", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement2 = new RentalAgreementForTest(rentalWithWeekend, LADW);
    Assert.assertEquals(agreement2.formatPrice(agreement2.getPreDiscountCharge()), "$19.90");

    Rental rentalWithNoHolidayNoWeekend = new Rental("JARK", 4, 70, LocalDate.of(2023, 5, 26));
    RentalAgreement agreement3 = new RentalAgreementForTest(rentalWithNoHolidayNoWeekend, JARK);
    Assert.assertEquals(agreement3.formatPrice(agreement3.getPreDiscountCharge()), "$2.99");
  }

  @Test
  public void testCalculateDiscount() throws IOException {
    createTestData();

    Rental rentalNoWeekend = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    Assert.assertEquals(agreement.formatPrice(agreement.getDiscountAmount()), "$8.34");

    Rental rentalWithWeekend = new Rental("LADW", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement2 = new RentalAgreementForTest(rentalWithWeekend, LADW);
    Assert.assertEquals(agreement2.formatPrice(agreement2.getDiscountAmount()), "$13.93");

    Rental rentalWithNoHolidayNoWeekend = new Rental("JARK", 4, 50, LocalDate.of(2023, 5, 26));
    RentalAgreement agreement3 = new RentalAgreementForTest(rentalWithNoHolidayNoWeekend, JARK);
    Assert.assertEquals(agreement3.formatPrice(agreement3.getDiscountAmount()), "$1.50");
  }

  @Test
  public void testCalculateTotal() throws IOException {
    createTestData();

    Rental rentalNoWeekend = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    Assert.assertEquals(agreement.formatPrice(agreement.getTotal()), "$3.58");

    Rental rentalWithWeekend = new Rental("LADW", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement2 = new RentalAgreementForTest(rentalWithWeekend, LADW);
    Assert.assertEquals(agreement2.formatPrice(agreement2.getTotal()), "$5.97");

    Rental rentalWithNoHolidayNoWeekend = new Rental("JARK", 4, 50, LocalDate.of(2023, 5, 26));
    RentalAgreement agreement3 = new RentalAgreementForTest(rentalWithNoHolidayNoWeekend, JARK);
    Assert.assertEquals(agreement3.formatPrice(agreement3.getTotal()), "$1.49");
  }

  @Test
  public void testToString() throws IOException {
    createTestData();

    Rental rentalNoWeekend = new Rental("CHNS", 10, 70, LocalDate.of(2023, 5, 16));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);

    StringBuilder builder = new StringBuilder();
    builder.append("Tool code: ");
    builder.append("CHNS");
    builder.append("/n");
    builder.append("Tool type: ");
    builder.append("Chainsaw");
    builder.append("/n");
    builder.append("Tool brand: ");
    builder.append("STIHL");
    builder.append("/n");
    builder.append("Rental days: ");
    builder.append(10);
    builder.append("/n");
    builder.append("Check out date: ");
    builder.append("05/16/23");
    builder.append("/n");
    builder.append("Due date: ");
    builder.append("05/26/23");
    builder.append("/n");
    builder.append("Daily rental charge: ");
    builder.append("$1.49");
    builder.append("/n");
    builder.append("Charge Days: ");
    builder.append(8);
    builder.append("/n");
    builder.append("Pre-discount charge: ");
    builder.append("$11.92");
    builder.append("/n");
    builder.append("Discount percent: ");
    builder.append("70%");
    builder.append("/n");
    builder.append("Discount amount: ");
    builder.append("$8.34");
    builder.append("/n");
    builder.append("Final charge: ");
    builder.append("$3.58");

    Assert.assertEquals(builder.toString(), agreement.toString());
  }

  private class RentalAgreementForTest extends RentalAgreement {

    private Tool tool;

    public RentalAgreementForTest(Rental rental, Tool tool) {
      super(rental);
      this.tool = tool;
    }

    public Tool getTool() {
      return this.tool;
    }

  }

  private class ToolForTesting extends Tool {

    private Charges charges;

    public ToolForTesting(String code, String type, Brand toolBrand, Charges charges) {
      super(code, type, toolBrand);
      this.charges = charges;
    }

    public Charges getType() {
      return charges;
    }
  }

}




