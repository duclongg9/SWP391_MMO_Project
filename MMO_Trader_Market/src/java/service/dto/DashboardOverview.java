package service.dto;

import model.Products;

import java.math.BigDecimal;
import java.util.List;

public record DashboardOverview(long totalProducts,
                                long pendingOrders,
                                BigDecimal monthlyRevenue,
                                List<Products> featuredProducts) {
}
