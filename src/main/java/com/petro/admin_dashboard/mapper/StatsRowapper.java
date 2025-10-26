package com.petro.admin_dashboard.mapper;

import com.petro.admin_dashboard.model.Stats;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsRowapper implements RowMapper<Stats> {
    @Override
    public Stats mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Stats.builder()
                .totalCustomers(rs.getInt("total_customers"))
                .totalInvoices(rs.getInt("total_invoices"))
                .totalBilled(rs.getDouble("total_billed"))
                .build();
    }
}
