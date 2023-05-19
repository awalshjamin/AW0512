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

public class RentalTransactionTest {

  private Session session;
  private Transaction transaction;

  private final Charges CHAINSAW = new Charges("Chainsaw", new BigDecimal(1.49), true, false, true);
  private final Charges LADDER = new Charges("Ladder", new BigDecimal(1.99), true, true, false);
  private final Charges JACKHAMMER = new Charges("Jackhammer", new BigDecimal(2.99), true, false,
      false);

  private final ToolForTesting CHNS = new ToolForTesting("CHNS", "Chainsaw", Brand.STIHL, CHAINSAW);
  private final ToolForTesting LADW = new ToolForTesting("LADW", "Ladder", Brand.WERNER, LADDER);
  private final ToolForTesting JAKD = new ToolForTesting("JAKD", "Chainsaw", Brand.DEWALT, JACKHAMMER);
  private final ToolForTesting JARK = new ToolForTesting("JARK", "Chainsaw", Brand.RIDGID, JACKHAMMER);

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

  private void createTestData() throws IOException {
    InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream("holiday-test-data.txt");
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
  public void testCheckoutThrows() {

    Rental rentalNoWeekend = new Rental("LADW", 3, 10, LocalDate.of(2020, 7, 2));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    Assert.assertThrows(RuntimeException.class, () -> {
      transaction.Checkout("JAKR", 5, 101, LocalDate.of(2015, 9, 3));
    });

  }

  @Test
  public void testCheckoutCase1() {
    Rental rentalNoWeekend = new Rental("LADW", 3, 10, LocalDate.of(2020, 7, 2));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    transaction.Checkout("LADW", 3, 10, LocalDate.of(2020, 7, 2));

    Assert.assertEquals(agreement.formatPrice(transaction.getRentalAgreement().getTotal()),
        "$3.57");
  }

  @Test
  public void testCheckoutCase2() {
    Rental rentalNoWeekend = new Rental("CHNS", 5, 25, LocalDate.of(2015, 7, 2));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    transaction.Checkout("CHNS", 5, 25, LocalDate.of(2015, 7, 2));
    Assert.assertEquals(agreement.formatPrice(transaction.getRentalAgreement().getTotal()),
        "$3.35");
  }

  @Test
  public void testCheckoutCase3() {
    Rental rentalNoWeekend = new Rental("JAKD", 6, 0, LocalDate.of(2015, 9, 3));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    transaction.Checkout("JAKD", 6, 0, LocalDate.of(2015, 9, 3));
    Assert.assertEquals(agreement.formatPrice(transaction.getRentalAgreement().getTotal()),
        "$8.97");
  }

  @Test
  public void testCheckoutCase4() {
    Rental rentalNoWeekend = new Rental("JAKR", 9, 0, LocalDate.of(2015, 7, 2));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    transaction.Checkout("JAKR", 9, 0, LocalDate.of(2015, 7, 2));
    Assert.assertEquals(agreement.formatPrice(transaction.getRentalAgreement().getTotal()),
        "$17.94");
  }

  @Test
  public void testCheckoutCase5() {
    Rental rentalNoWeekend = new Rental("JAKR", 4, 50, LocalDate.of(2020, 7, 2));
    RentalAgreement agreement = new RentalAgreementForTest(rentalNoWeekend, CHNS);
    RentalTransaction transaction = new RentalTransactionForTest(agreement);

    transaction.Checkout("JAKR", 4, 50, LocalDate.of(2020, 7, 2));
    Assert.assertEquals(agreement.formatPrice(transaction.getRentalAgreement().getTotal()),
        "$1.49");
  }

  private class RentalTransactionForTest extends RentalTransaction {

    private final RentalAgreement agreement;

    public RentalTransactionForTest(RentalAgreement agreement){
      this.agreement = agreement;
    }

    @Override
    public RentalAgreement getRentalAgreement() {
      return this.agreement;
    }
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

  private class HolidayForTest extends Holiday{
    public static void addHoliday(String name, LocalDate holiday){
      Holiday.holidayDateCache.put(name, holiday);
    }
  }


}
