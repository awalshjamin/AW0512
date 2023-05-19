package org.data;

public enum Brand {
  STIHL,
  WERNER,
  DEWALT,
  RIDGID;

  public static Brand getBrandFromString(String name){
    switch (name.toLowerCase())
    {
      case "stihl":
        return Brand.STIHL;
      case "werner":
        return Brand.WERNER;
      case "dewalt":
        return Brand.DEWALT;
      case "ridgid":
        return Brand.RIDGID;
      default:
        throw new IllegalArgumentException("The String " + name + " is not a valid tool brand");
    }
  }
}
