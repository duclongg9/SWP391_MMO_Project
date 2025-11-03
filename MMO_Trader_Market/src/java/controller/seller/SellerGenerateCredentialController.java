package controller.seller;

import dao.order.CredentialDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cho phép người bán chủ động sinh thêm credential ảo cho sản phẩm trước khi mở
 * bán.
 */
@WebServlet(name = "SellerGenerateCredentialController", urlPatterns = {"/seller/credentials/generate"})
public class SellerGenerateCredentialController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SellerGenerateCredentialController.class.getName());

    private final CredentialDAO credentialDAO = new CredentialDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        HttpSession session = request.getSession();
        String productIdRaw = request.getParameter("productId");
        String quantityRaw = request.getParameter("quantity");
        String variantCode = request.getParameter("variantCode");
        try {
            int productId = Integer.parseInt(productIdRaw);
            int quantity = Integer.parseInt(quantityRaw);
            if (productId <= 0 || quantity <= 0) {
                throw new IllegalArgumentException("Tham số không hợp lệ");
            }
            int inserted = credentialDAO.generateFakeCredentials(productId, variantCode, quantity);
            if (inserted > 0) {
                String normalizedVariant = variantCode == null ? null : variantCode.trim();
                if (normalizedVariant != null && normalizedVariant.isEmpty()) {
                    normalizedVariant = null;
                }
                StringBuilder message = new StringBuilder();
                message.append("Đã sinh ").append(inserted).append(" credential ảo cho sản phẩm #").append(productId);
                if (normalizedVariant != null) {
                    message.append(" (biến thể ").append(normalizedVariant).append(")");
                }
                session.setAttribute("sellerInventoryFlashSuccess", message.append('.').toString());
            } else {
                session.setAttribute("sellerInventoryFlashError",
                        "Không thể sinh credential ảo, vui lòng thử lại sau.");
            }
        } catch (NumberFormatException | IllegalArgumentException ex) {
            session.setAttribute("sellerInventoryFlashError",
                    "Tham số không hợp lệ, vui lòng kiểm tra mã sản phẩm và số lượng.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo", ex);
            session.setAttribute("sellerInventoryFlashError",
                    "Hệ thống không thể sinh credential ảo, vui lòng thử lại sau.");
        }
        response.sendRedirect(request.getContextPath() + "/seller/inventory");
    }
}
