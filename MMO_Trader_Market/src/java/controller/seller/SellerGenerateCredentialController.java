package controller.seller;

import dao.order.CredentialDAO;
import dao.shop.ShopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Shops;

/**
 * Cho phép người bán chủ động sinh thêm credential ảo cho sản phẩm trước khi mở
 * bán.
 */
@WebServlet(name = "SellerGenerateCredentialController", urlPatterns = {"/seller/credentials/generate"})
public class SellerGenerateCredentialController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SellerGenerateCredentialController.class.getName());

    private final CredentialDAO credentialDAO = new CredentialDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Kiểm tra userId
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Lấy shopId từ request và verify quyền sở hữu
        String shopIdParam = request.getParameter("shopId");
        Integer shopId = null;
        
        if (shopIdParam != null && !shopIdParam.trim().isEmpty()) {
            try {
                int parsedShopId = Integer.parseInt(shopIdParam.trim());
                // Verify quyền sở hữu shop
                Optional<Shops> shopOpt;
                try {
                    shopOpt = shopDAO.findByIdAndOwner(parsedShopId, userId);
                } catch (SQLException e) {
                    throw new ServletException("Lỗi khi kiểm tra quyền sở hữu shop", e);
                }
                
                if (shopOpt.isPresent()) {
                    shopId = parsedShopId;
                } else {
                    session.setAttribute("sellerInventoryFlashError",
                            "Bạn không có quyền sinh credential cho shop này.");
                    response.sendRedirect(request.getContextPath() + "/seller/inventory");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("sellerInventoryFlashError",
                        "Mã shop không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/seller/inventory");
                return;
            }
        }
        
        try {
            // Chỉ sinh credential cho sản phẩm của shop hiện tại
            CredentialDAO.BulkGenerationSummary summary = credentialDAO.seedAllProductsFromInventory(shopId);
            if (summary.generatedCredentials() > 0) {
                session.setAttribute("sellerInventoryFlashSuccess",
                        String.format("Đã bổ sung %d credential cho %d SKU.",
                                summary.generatedCredentials(), summary.skuTouched()));
            } else {
                session.setAttribute("sellerInventoryFlashSuccess",
                        "Tất cả sản phẩm đã có đủ credential để bàn giao.");
            }

        } catch (IllegalArgumentException ex) {
            session.setAttribute("sellerInventoryFlashError",
                    "Tham số không hợp lệ, vui lòng kiểm tra mã sản phẩm và số lượng.");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo", ex);
            session.setAttribute("sellerInventoryFlashError",
                    "Hệ thống không thể sinh credential ảo, vui lòng thử lại sau.");
        }
        
        // Redirect về đúng shop
        String redirectUrl = request.getContextPath() + "/seller/inventory";
        if (shopId != null) {
            redirectUrl += "?shopId=" + shopId;
        }
        response.sendRedirect(redirectUrl);
    }
}
