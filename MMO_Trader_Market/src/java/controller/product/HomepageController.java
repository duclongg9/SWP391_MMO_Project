package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Products;
import model.Shops;
import model.view.MarketplaceSummary;
import service.HomepageService;

import java.io.IOException;
import java.util.List;

/**
 * Handles requests for the public facing homepage where visitors discover
 * available account products.
 */
@WebServlet(name = "HomepageController", urlPatterns = {"/home"})
public class HomepageController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final HomepageService homepageService = new HomepageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Chợ tài khoản MMO - Trang chủ");
        request.setAttribute("bodyClass", "layout layout--landing");
        request.setAttribute("headerTitle", "MMO Trader Market");
        request.setAttribute("headerSubtitle", "Nền tảng demo mua bán tài khoản an toàn");

        populateHomepageData(request);

        forward(request, response, "product/home");
    }

    private void populateHomepageData(HttpServletRequest request) {
        MarketplaceSummary summary = homepageService.loadMarketplaceSummary();
        request.setAttribute("summary", summary);

        List<Products> featuredProducts = homepageService.loadFeaturedProducts();
        request.setAttribute("featuredProducts", featuredProducts);

        List<Shops> shops = homepageService.loadActiveShops();
        request.setAttribute("shops", shops);
    }
}
