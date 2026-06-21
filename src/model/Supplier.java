package model;

import java.util.ArrayList;

public class Supplier {
	private int supplierId;
	private String companyName;
	private String contactPerson;
	private String email;
	private boolean isActive;

	// This stores your multi-valued phone numbers
	private ArrayList<String> phoneNumbers = new ArrayList<>();

	// --- Getter and Setter for Phone Numbers ---
	public ArrayList<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	// --- Standard Getters and Setters for the rest ---
	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}