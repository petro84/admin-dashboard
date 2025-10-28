package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.model.Customer;
import com.petro.admin_dashboard.model.Invoice;
import com.petro.admin_dashboard.model.Stats;
import org.springframework.data.domain.Page;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    Page<Customer> getCustomers(int page, int size);
    Iterable<Customer> getCustomers();
    Customer getCustomer(Long id);
    Page<Customer> searchCustomers(String name, int page, int size);

    Invoice createInvoice(Invoice invoice);
    Page<Invoice> getInvoices(int page, int size);
    void addInvoiceToCustomer(Long id, Invoice invoice);
    Iterable<Invoice> getInvoices();
    Invoice getInvoice(Long id);

    Stats getStats();
}
