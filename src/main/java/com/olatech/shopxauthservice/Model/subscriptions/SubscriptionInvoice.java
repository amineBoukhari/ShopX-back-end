package com.olatech.shopxauthservice.Model.subscriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_invoices")
public class SubscriptionInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "subscription_invoice_sequence", sequenceName = "subscription_invoice_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonIgnore
    private StoreSubscription subscription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime issuedDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    private String paymentMethod;
    
    @Column(unique = true)
    private String invoiceNumber;

    @Column(length = 1000)
    private String notes;

    // Constructors
    public SubscriptionInvoice() {
    }

    public SubscriptionInvoice(StoreSubscription subscription, BigDecimal amount, 
                               LocalDateTime issuedDate, LocalDateTime dueDate, 
                               InvoiceStatus status) {
        this.subscription = subscription;
        this.amount = amount;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StoreSubscription getSubscription() {
        return subscription;
    }

    public void setSubscription(StoreSubscription subscription) {
        this.subscription = subscription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public boolean isPaid() {
        return this.status == InvoiceStatus.PAID;
    }

    public boolean isOverdue() {
        return this.status == InvoiceStatus.PENDING && LocalDateTime.now().isAfter(this.dueDate);
    }

    @Override
    public String toString() {
        return "SubscriptionInvoice{" +
                "id=" + id +
                ", subscriptionId=" + (subscription != null ? subscription.getId() : null) +
                ", amount=" + amount +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", issuedDate=" + issuedDate +
                ", status=" + status +
                '}';
    }
}
