package org.rental;

import java.time.LocalDate;
import org.data.Rental;

public class RentalTransaction {

  private Rental order;
  private RentalAgreement rentalAgreement;

  public void Checkout(String toolCode, int numDays, int discount, LocalDate date) throws RuntimeException{
    if (numDays < 1) {
      throw new RuntimeException("The number of rental days is " + numDays + ". It is required to be greater than or equal to 1");
    }
    else if (discount < 0 || discount > 100) {
      throw new RuntimeException("Discount percentage is: " + discount + ". It should be in the range 0-100 inclusive");
    }

    this.order = new Rental(toolCode, numDays, discount, date);
    this.rentalAgreement = new RentalAgreement(getOrder());
  }

  public void PrintRentalAgreement() {
    if (this.rentalAgreement != null) {
      this.rentalAgreement.printReport();
    }
  }

  public Rental getOrder() {
    return order;
  }

  public RentalAgreement getRentalAgreement() {
    return rentalAgreement;
  }
}
