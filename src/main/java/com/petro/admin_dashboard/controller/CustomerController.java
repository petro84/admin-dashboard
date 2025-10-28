package com.petro.admin_dashboard.controller;

import com.petro.admin_dashboard.model.Customer;
import com.petro.admin_dashboard.model.HttpResponse;
import com.petro.admin_dashboard.model.Invoice;
import com.petro.admin_dashboard.model.dto.UserDTO;
import com.petro.admin_dashboard.report.CustomerReport;
import com.petro.admin_dashboard.report.InvoiceReport;
import com.petro.admin_dashboard.service.CustomerService;
import com.petro.admin_dashboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.parseMediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/customer")
public class CustomerController {
    private final CustomerService customerSvc;
    private final UserService userSvc;

    @GetMapping("/list")
    public ResponseEntity<HttpResponse> getCustomers(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page,
                                                     @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "page", customerSvc.getCustomers(page.orElse(0), size.orElse(10)),
                                "stats", customerSvc.getStats()))
                        .message("Customers Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<HttpResponse> getCustomer(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "customer", customerSvc.getCustomer(id)))
                        .message("Customer Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/search")
    public ResponseEntity<HttpResponse> searchCustomers(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page,
                                                        @RequestParam Optional<Integer> size, @RequestParam Optional<String> name) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "page", customerSvc.searchCustomers(name.orElse(""), page.orElse(0), size.orElse(10))))
                        .message("Customers Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PutMapping("/update")
    public ResponseEntity<HttpResponse> updateCustomer(@AuthenticationPrincipal UserDTO user, @RequestBody Customer customer) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "customer", customerSvc.updateCustomer(customer)))
                        .message("Customer Updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createCustomer(@AuthenticationPrincipal UserDTO user, @RequestBody Customer customer) {
        return ResponseEntity.created(URI.create("")).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "page", customerSvc.createCustomer(customer)))
                        .message("Customer Created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    @PostMapping("/invoice/create")
    public ResponseEntity<HttpResponse> createInvoice(@AuthenticationPrincipal UserDTO user, @RequestBody Invoice invoice) {
        return ResponseEntity.created(URI.create("")).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "invoice", customerSvc.createInvoice(invoice)))
                        .message("Invoice Created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    @GetMapping("/invoice/list")
    public ResponseEntity<HttpResponse> getInvoices(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page,
                                                    @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "page", customerSvc.getInvoices(page.orElse(0), size.orElse(10))))
                        .message("Invoices Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/invoice/new")
    public ResponseEntity<HttpResponse> newInvoice(@AuthenticationPrincipal UserDTO user) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "customers", customerSvc.getCustomers()))
                        .message("Invoice Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/invoice/get/{id}")
    public ResponseEntity<HttpResponse> getInvoice(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id) {
        Invoice invoice = customerSvc.getInvoice(id);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "invoice", invoice,
                                "customer", invoice.getCustomer()))
                        .message("Invoice Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping("/invoice/addtocustomer/{id}")
    public ResponseEntity<HttpResponse> addInvoiceToCustomer(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id,
                                                             @RequestBody Invoice invoice) {
        customerSvc.addInvoiceToCustomer(id, invoice);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getUserByEmail(user.getEmail()),
                                "page", customerSvc.getCustomers()))
                        .message(String.format("Invoice added to customer with ID: %s", id))
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/download/report")
    public ResponseEntity<Resource> downloadReport() {
        List<Customer> customers = new ArrayList<>();
        customerSvc.getCustomers().iterator().forEachRemaining(customers::add);

        CustomerReport report = new CustomerReport(customers);
        HttpHeaders headers = new HttpHeaders();
        headers.add("File-Name", "customer-report.xlsx");
        headers.add(CONTENT_DISPOSITION, "attachment;File-Name=customer-report.xlsx");

        return ResponseEntity.ok().contentType(parseMediaType("application/vnd.ms-excel"))
                .headers(headers)
                .body(report.export());
    }

    @GetMapping("/invoice/download/report")
    public ResponseEntity<Resource> downloadInvoiceReport() {
        List<Invoice> invoices = new ArrayList<>();
        customerSvc.getInvoices().iterator().forEachRemaining(invoices::add);

        InvoiceReport report = new InvoiceReport(invoices);
        HttpHeaders headers = new HttpHeaders();
        headers.add("File-Name", "invoice-report.xlsx");
        headers.add(CONTENT_DISPOSITION, "attachment;File-Name=invoice-report.xlsx");

        return ResponseEntity.ok().contentType(parseMediaType("application/vnd.ms-excel"))
                .headers(headers)
                .body(report.export());
    }

}
