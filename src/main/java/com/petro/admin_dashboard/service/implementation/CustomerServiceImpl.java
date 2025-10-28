package com.petro.admin_dashboard.service.implementation;

import com.petro.admin_dashboard.mapper.StatsRowapper;
import com.petro.admin_dashboard.model.Customer;
import com.petro.admin_dashboard.model.Invoice;
import com.petro.admin_dashboard.model.Stats;
import com.petro.admin_dashboard.repository.CustomerRepository;
import com.petro.admin_dashboard.repository.InvoiceRepository;
import com.petro.admin_dashboard.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.petro.admin_dashboard.query.customerQuery.STATS_QUERY;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.data.domain.PageRequest.of;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepo;
    private final InvoiceRepository invoiceRepo;
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    @Override
    public Page<Customer> getCustomers(int page, int size) {
        return customerRepo.findAll(of(page, size));
    }

    @Override
    public Iterable<Customer> getCustomers() {
        return customerRepo.findAll();
    }

    @Override
    public Customer getCustomer(Long id) {
        return customerRepo.findById(id).get();
    }

    @Override
    public Page<Customer> searchCustomers(String name, int page, int size) {
        return customerRepo.findByNameContaining(name, of(page, size));
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        return invoiceRepo.save(invoice);
    }

    @Override
    public Page<Invoice> getInvoices(int page, int size) {
        return invoiceRepo.findAll(of(page, size));
    }

    @Override
    public void addInvoiceToCustomer(Long id, Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        Customer customer = customerRepo.findById(id).get();
        invoice.setCustomer(customer);
        invoiceRepo.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepo.findById(id).get();
    }

    @Override
    public Iterable<Invoice> getInvoices() {
        return invoiceRepo.findAll();
    }

    @Override
    public Stats getStats() {
        return jdbc.queryForObject(STATS_QUERY, Map.of(), new StatsRowapper());
    }
}
