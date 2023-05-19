package org.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

@Entity
@Table(name = "rental")
public class Rental {

  private static final String QUERY_FOR_TOOL = "SELECT t FROM Tool t WHERE t.code = :code";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rental_id")
  private long rentalId;

  @JoinColumn(name = "tool", referencedColumnName = "code")
  @OneToOne
  private Tool tool;

  @Column(name = "num_of_days")
  private int days;

  @Column(name = "discount")
  private int discount;

  @Column(name = "checkout_date")
  private LocalDate checkoutDate;

  public Rental(){

  }

  public Rental(String toolCode, int days, int discount, LocalDate checkoutDate)
  {
    try {
      this.tool = processToolQuery(toolCode);
    } catch (HibernateException ex) {
      ex.printStackTrace();
    }

    this.days = days;
    this.discount = discount;
    this.checkoutDate = checkoutDate;
  }

  public long getRentalId() {
    return rentalId;
  }

  public Tool getTool() {
    return tool;
  }

  public int getDays() {
    return days;
  }

  public int getDiscount() {
    return discount;
  }

  public LocalDate getCheckoutDate() {
    return checkoutDate;
  }

  protected static Tool processToolQuery(String toolCode){
    Session session = HibernateUtil.getSession();
    Query<Tool> query = session.createQuery(QUERY_FOR_TOOL, Tool.class);
    query.setParameter("code", toolCode);

    List<Tool> tools = query.getResultList();

    if (tools.size() > 0){
      return tools.get(0);
    }

    return null;
  }
}
