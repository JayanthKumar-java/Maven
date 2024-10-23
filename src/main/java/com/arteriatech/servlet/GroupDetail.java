package com.arteriatech.servlet;

import java.util.Objects;

public class GroupDetail {
    private static String serialNo;
    private static String name;
    private static String vendorAddress1;
    private static String street;
    private static String city;
    private static String district;
    private static String postalCode;
    private static String country;
 
    public GroupDetail(String serialNo, String name, String vendorAddress1, String street, String city, String district,
            String postalCode, String country) {
        //TODO Auto-generated constructor stub
        this.serialNo = serialNo;
        this.name = name;
        this.vendorAddress1 = vendorAddress1;
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
    }
    public GroupDetail() {

    }

    public static String getSerialNo() {
		return serialNo;
	}

	// public static void setSerialNo(int i) {
	// 	GroupDetail.serialNo = i;
	// }

	public static String getName() {
		return name;
	}

	// public static void setName(String name) {
	// 	GroupDetail.name = name;
	// }

	public static String getVendorAddress1() {
		return vendorAddress1;
	}

	// public static void setVendorAddress1(String vendorAddress1) {
	// 	GroupDetail.vendorAddress1 = vendorAddress1;
	// }

	public static String getStreet() {
		return street;
	}

	// public static void setStreet(String street) {
	// 	GroupDetail.street = street;
	// }

	public static String getCity() {
		return city;
	}

	// public static void setCity(String city) {
	// 	GroupDetail.city = city;
	// }

	public static String getDistrict() {
		return district;
	}

	// public static void setDistrict(String district) {
	// 	GroupDetail.district = district;
	// }

	public static String getPostalCode() {
		return postalCode;
	}

	// public static void setPostalCode(String postalCode) {
	// 	GroupDetail.postalCode = postalCode;
	// }

	public static String getCountry() {
		return country;
	}

	// public static void setCountry(String country) {
	// 	GroupDetail.country = country;
	// }
   

    
}