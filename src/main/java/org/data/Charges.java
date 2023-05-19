package org.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;


@Entity
@Table(name = "charges")
public class Charges {

  @Id
  @Column(name = "tool_type")
  private String toolType;

  @Column(name = "daily_charge", precision = 8, scale = 2)
  private BigDecimal dailyRate;

  @Column(name = "weekday")
  private boolean isWeekdayCharge;

  @Column(name = "weekend")
  private boolean isWeekendCharge;

  @Column(name = "holiday")
  private boolean isHolidayCharge;

  public Charges(){

  }

  public Charges(String toolType, BigDecimal dailyRate, boolean isWeekdayCharge,
      boolean isWeekendCharge, boolean isHolidayCharge) {
    this.toolType = toolType;
    this.dailyRate = dailyRate;
    this.isWeekdayCharge = isWeekdayCharge;
    this.isWeekendCharge = isWeekendCharge;
    this.isHolidayCharge = isHolidayCharge;
  }

  public String getToolType() {
    return toolType;
  }

  public BigDecimal getDailyRate() {
    return dailyRate;
  }

  public boolean isWeekdayCharge() {
    return isWeekdayCharge;
  }

  public boolean isWeekendCharge() {
    return isWeekendCharge;
  }

  public boolean isHolidayCharge() {
    return isHolidayCharge;
  }
}
