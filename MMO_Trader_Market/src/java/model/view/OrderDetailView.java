package model.view;

import model.Orders;
import model.Products;

import java.util.List;

/**
 * Gói dữ liệu chi tiết của một đơn hàng bao gồm bản ghi {@link model.Orders}, thông tin sản phẩm và
 * danh sách credential plaintext. Controller truyền record này xuống JSP {@code order/detail.jsp}
 * để trình bày rõ luồng bàn giao cho người mua.
 *
 * @author longpdhe171902
 */
public record OrderDetailView(Orders order, Products product, List<String> credentials) {
}
