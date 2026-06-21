package model;

public class Provide {
    private int productId;
    private String productName;
    private String barcode;
    private String categoryName;
    private String supplierName;
    private double supplyCost;
    private double retailPrice;

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public double getSupplyCost() { return supplyCost; }
    public void setSupplyCost(double supplyCost) { this.supplyCost = supplyCost; }

    public double getRetailPrice() { return retailPrice; }
    public void setRetailPrice(double retailPrice) { this.retailPrice = retailPrice; }
}