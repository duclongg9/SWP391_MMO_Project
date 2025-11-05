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
        try {
            CredentialDAO.BulkGenerationSummary summary = credentialDAO.seedAllProductsFromInventory();
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
        response.sendRedirect(request.getContextPath() + "/seller/inventory");
    }
}
