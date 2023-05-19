package org.data;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.HibernateUtil;
import org.hibernate.Session;

@Entity
@Table(name = "tool")
public class Tool {
  @Id
  @Column(name = "code")
  private String code;

  @JoinColumn(name = "type", referencedColumnName = "tool_type")
  @OneToOne
  private Charges type;

  @Enumerated(EnumType.STRING)
  @Column(name = "brand")
  private Brand toolBrand;

  public Tool(){

  }

  public Tool(String code, String type, Brand toolBrand) {
    this.code = code;
    Session session = HibernateUtil.getSession();
    this.type = session.get(Charges.class, type);
    this.toolBrand = toolBrand;
  }

  public String getCode() {
    return code;
  }

  public Charges getType() {
    return type;
  }

  public Brand getToolBrand() {
    return toolBrand;
  }
}
