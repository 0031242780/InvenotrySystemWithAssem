package model;

public class Product {
    private int productId;
    private String productName;
    private String barcode;
    private String description;
    private int categoryId;
    private int supplierId;
    private double price;
    private double discountPrice; // المتغير المسؤول عن قيمة الخصم

    public Product() {}

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getDiscountPrice() { return discountPrice; }

	public void setDiscountPrice(double discountPrice) {
		this.discountPrice = discountPrice;
	}
}