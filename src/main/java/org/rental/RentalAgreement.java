package org.rental;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.data.Holiday;
import org.data.Rental;
import org.data.Tool;

public class RentalAgreement {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
  private LocalDate dueDate;
  private Integer chargeDays;
  private BigDecimal preDiscountCharge;
  private BigDecimal discountAmount;
  private BigDecimal total;
  private final Tool tool;
  private final Rental rental;

  public RentalAgreement(Rental rental) {
    this.tool = rental.getTool();
    this.rental = rental;
  }

  private int calculateIncrement(LocalDate dayToCheck) {
    if (!getTool().getType().isHolidayCharge()) {
      if (!Holiday.isAHoliday(dayToCheck)) {
        return 1;
      }
    } else {
      return 1;
    }
    return 0;
  }

  private static boolean isAWeekend(LocalDate dayToCheck) {
    return dayToCheck.getDayOfWeek() == DayOfWeek.SUNDAY
        || dayToCheck.getDayOfWeek() == DayOfWeek.SATURDAY;
  }

  private void calculateDueDate() {
    this.dueDate = getCheckoutDate().plusDays(getNumberDays());
  }

  private void calculateChargeDays() {
    int numberOfDays = 0;
    for (int i = 0; i < getNumberDays(); i++) {
      LocalDate dayToCheck = getCheckoutDate().plusDays(i);

      if (dayToCheck.isAfter(getDueDate())) {
        break;
      }

      boolean isAWeekend = isAWeekend(dayToCheck);

      if (getTool().getType().isWeekendCharge() && isAWeekend) {
        numberOfDays += calculateIncrement(dayToCheck);
      } else if (getTool().getType().isWeekdayCharge() && !isAWeekend) {
        numberOfDays += calculateIncrement(dayToCheck);
      }
    }

    chargeDays = numberOfDays;
  }

  private void calculatePreDiscountCharge() {
    this.preDiscountCharge = getDailyCharge().multiply(BigDecimal.valueOf(getChargeDays()));
  }

  private void calculateDiscount() {
    this.discountAmount = getPreDiscountCharge().multiply(BigDecimal.valueOf(getDiscountPercent())).divide(BigDecimal.valueOf(100));
  }

  private void calculateTotal() {
    this.total = getPreDiscountCharge().subtract(getDiscountAmount());
  }

  @Override
  public String toString() {
    return
        "Tool code: " + getToolCode() + "/n" +
            "Tool type: " + getToolType() + "/n" +
            "Tool brand: " + getBrand() + "/n" +
            "Rental days: " + getNumberDays() + "/n" +
            "Check out date: " + getCheckoutDate().format(formatter) + "/n" +
            "Due date: " + getDueDate().format(formatter) + "/n" +
            "Daily rental charge: " + formatPrice(getDailyCharge()) + "/n" +
            "Charge Days: " + getChargeDays() + "/n" +
            "Pre-discount charge: " + formatPrice(getPreDiscountCharge()) + "/n" +
            "Discount percent: " + getDiscountPercent() + "%" + "/n"  +
            "Discount amount: " + formatPrice(getDiscountAmount()) + "/n" +
            "Final charge: " + formatPrice(getTotal());
  }

  public String formatPrice(BigDecimal number) {
    return NumberFormat.getCurrencyInstance().format(number);
  }

  public Tool getTool() {
    return this.tool;
  }

  public void printReport() {
    System.out.println(this);
  }

  public String getToolCode() {
    return getTool().getCode();
  }

  public String getToolType() {
    return getTool().getType().getToolType();
  }

  public String getBrand() {
    return getTool().getToolBrand().name();
  }

  public int getNumberDays() {
    return rental.getDays();
  }

  public LocalDate getCheckoutDate() {
    return rental.getCheckoutDate();
  }

  public LocalDate getDueDate() {
    if(dueDate == null){
      calculateDueDate();
    }
    return dueDate;
  }

  public BigDecimal getDailyCharge() {
    return getTool().getType().getDailyRate();
  }

  public int getChargeDays() {
    if(chargeDays == null) {
      calculateChargeDays();
    }
    return chargeDays;
  }

  public BigDecimal getPreDiscountCharge() {
    if(preDiscountCharge == null) {
      calculatePreDiscountCharge();
    }
    return preDiscountCharge;
  }

  public int getDiscountPercent() {
    return rental.getDiscount();
  }

  public BigDecimal getDiscountAmount() {
    if(discountAmount == null) {
      calculateDiscount();
    }
    return discountAmount;
  }

  public BigDecimal getTotal() {
    if (total == null) {
      calculateTotal();
    }
    return total;
  }
}
