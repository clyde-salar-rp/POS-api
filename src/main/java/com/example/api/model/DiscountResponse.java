package com.example.api.model;

import java.util.List;

public class DiscountResponse {
    private double subtotal;
    private double tax;
    private double total;
    private double totalDiscount;
    private List<AppliedDiscount> appliedDiscounts;

    public DiscountResponse() {}

    public DiscountResponse(double subtotal, double tax, double total,
                            double totalDiscount, List<AppliedDiscount> appliedDiscounts) {
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.totalDiscount = totalDiscount;
        this.appliedDiscounts = appliedDiscounts;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(double totalDiscount) { this.totalDiscount = totalDiscount; }

    public List<AppliedDiscount> getAppliedDiscounts() { return appliedDiscounts; }
    public void setAppliedDiscounts(List<AppliedDiscount> appliedDiscounts) {
        this.appliedDiscounts = appliedDiscounts;
    }
}