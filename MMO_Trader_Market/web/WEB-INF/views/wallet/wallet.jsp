<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    java.util.List<String> extraStylesheets = new java.util.ArrayList<>();
    extraStylesheets.add("https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css");
    extraStylesheets.add(request.getContextPath() + "/assets/css/components/filterbar.css");
    request.setAttribute("extraStylesheets", extraStylesheets);
%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");

    List<Map<String, String>> navItems = new ArrayList<>();
    String contextPath = request.getContextPath();

    Map<String, String> homeLink = new HashMap<>();
    homeLink.put("href", contextPath + "/home");
    homeLink.put("label", "Trở về trang chủ");
    navItems.add(homeLink);


    request.setAttribute("navItems", navItems);
%>

<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content grid-2">
    <section class="panel profile-grid" >





        <!--Alerts -->
        <div style="grid-column: 1 / -1;">
            <c:if test="${not empty msg}">
                <div class="alert alert--success" role="status" aria-live="polite">${msg}</div>
            </c:if>
            <c:if test="${not empty emg}">
                <div class="alert alert--danger" role="alert" aria-live="assertive">${emg}</div>
            </c:if>
        </div>
        <!--Alerts -->
        
        <!--Thông tin ví -->
        <input type="hidden" name="action" value="updateProfile">
        <div class="panel__header">
            <h2 class="panel__title">Ví của bạn</h2>
        </div>

        <table class="table table--interactive" role="presentation">
            <tbody>
                <tr>
                    <th scope="row">Số dư còn lại</th>
                    <td style="
                        font-size: 2.8rem;
                        font-weight: 800;
                        line-height: 1.05;
                        padding: 0.9rem 1rem;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        min-height: 4.8rem; /* đảm bảo chiều cao giống ~3 hàng */
                        color: #005b96; /* màu xanh dương (hoặc đổi) */
                        background: rgba(0,91,150,0.04);
                        border-radius: 8px;
                        "
                        aria-label="Số dư hiện có">
                        <fmt:formatNumber value="${wallet.balance}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                    </td>
                </tr>
                        <!-- Nút rút tiền -->
        <tr>
            <td></td>
            <td colspan="1" style="text-align:right; padding-top: 1rem;">
                <a href="${pageContext.request.contextPath}/withdraw" 
                   class="button button--primary" 
                   style="padding: 0.6rem 1.2rem; font-size: 1rem;">
                   Rút tiền
                </a>
            </td>
        </tr>
                
            </tbody>
        </table>

        <div class="form__group" style="margin-top:1.6rem;">
            <a class="btn btn--primary" href="${pageContext.request.contextPath}/wallet/deposit">Nạp tiền qua VNPAY</a>
            <span class="form__hint">Thanh toán an toàn, số dư sẽ cập nhật sau khi IPN được xác nhận.</span>
        </div>


        <!--Lịch sử ví -->

        <input type="hidden" name="action" value="updateProfile">
        <div class="panel__header">
            <h2 class="panel__title">Lịch sử giao dịch</h2>
        </div>
        <!-- Bộ lọc giao dịch -->
        <form class="filters filterbar" method="get"
              action="${pageContext.request.contextPath}/wallet">

            <div class="filterbar__row">
                <!-- Loại giao dịch -->
                <div class="filterbar__field">
                    <label class="filterbar__label" for="f-type">Loại giao dịch</label>
                    <select id="f-type" name="type" class="select">
                        <option value="">-- Tất cả --</option>
                        <option value="Deposit"    ${param.type=='Deposit'    ?'selected':''}>Nạp tiền</option>
                        <option value="Withdrawal" ${param.type=='Withdrawal' ?'selected':''}>Rút tiền</option>
                        <option value="Purchase"   ${param.type=='Purchase'   ?'selected':''}>Mua hàng</option>
                        <option value="Payout"     ${param.type=='Payout'     ?'selected':''}>Chi trả</option>
                        <option value="Refund"     ${param.type=='Refund'     ?'selected':''}>Hoàn tiền</option>
                        <option value="Fee"        ${param.type=='Fee'        ?'selected':''}>Phí</option>
                    </select>
                </div>

                <!-- Khoảng thời gian -->
                <div class="filterbar__field">
                    <label class="filterbar__label">Khoảng thời gian</label>
                    <div class="time-range">
                        <select name="preset" class="select">
                            <option value="">Chọn nhanh</option>
                            <option value="today" ${param.preset=='today'?'selected':''}>Hôm nay</option>
                            <option value="7d"    ${param.preset=='7d'   ?'selected':''}>7 ngày</option>
                            <option value="30d"   ${param.preset=='30d'  ?'selected':''}>30 ngày</option>
                        </select>
                        <input type="date" name="start" class="form-control"
                               value="${fn:escapeXml(param.start)}" aria-label="Từ ngày">
                        <span class="amount-range__sep">–</span>
                        <input type="date" name="end" class="form-control"
                               value="${fn:escapeXml(param.end)}" aria-label="Đến ngày">
                    </div>
                </div>

                <!-- Số tiền -->
                <div class="filterbar__field">
                    <label class="filterbar__label">Số tiền</label>
                    <div class="amount-range">
                        <input type="number" name="minAmount" class="form-control"
                               placeholder="Từ" value="${fn:escapeXml(param.minAmount)}">
                        <span class="amount-range__sep">–</span>
                        <input type="number" name="maxAmount" class="form-control"
                               placeholder="Đến" value="${fn:escapeXml(param.maxAmount)}">
                    </div>
                </div>

                <!-- Actions -->
                <div class="filterbar__actions">
                    <button type="submit" class="btn btn--primary">Lọc</button>
                    <a class="btn btn--reset"
                       href="${pageContext.request.contextPath}/wallet">Xóa bộ lọc</a>
                </div>
            </div>

            <!-- Khi lọc, về trang 1 -->
            <input type="hidden" name="page" value="1">
        </form>

        <table class="table table--interactive" role="presentation">
            <thead>
                <tr>
                    <th scope="col">Mã</th>
                    <th scope="col">Loại giao dịch</th>
                    <th scope="col">Số tiền</th>
                    <th scope="col">Số tiền trước giao dịch</th>
                    <th scope="col">Số tiền sau giao dịch</th>
                    <th scope="col">ghi chú</th>
                    <th scope="col">Thời gian tạo</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${listTransaction}">
                    <tr>
                        <td scope="row"><label>${p.id}</label></td>
                        <td scope="row"><label>${p.transactionType}</label></td>
                        <td><fmt:formatNumber value="${p.amount}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                        <td><fmt:formatNumber value="${p.balanceBefore}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                        <td><fmt:formatNumber value="${p.balanceAfter}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                        <td scope="row"><label>${p.note}</label></th>
                        <td>
                            <c:choose>
                                <%-- ưu tiên getter trả java.util.Date --%>
                                <c:when test="${not empty p.createdAt}">
                                    <fmt:formatDate value="${p.createdAt}"
                                                    pattern="dd/MM/yyyy HH:mm"
                                                    timeZone="Asia/Ho_Chi_Minh"/>
                                </c:when>
                                <c:when test="${not empty p.createdAt}">
                                    <c:out value="${p.createdAt}" />
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <!--Pagination -->

        <ul class="pagination">
            <c:if test="${currentPage > 1}">
                <li class="pagination__item">
                    <a href="${pageContext.request.contextPath}/wallet?index=${currentPage - 1}" class="pagination-item__link">
                        <i class="pagination-item__icon fa-solid fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            <c:forEach begin="1" end="${endPage}" var="i">
                <li class="pagination__item">
                    <a href="${pageContext.request.contextPath}/wallet?index=${i}" class="pagination-item__link ${currentPage == i?"pagination__item--active":""} ">${i}</a>
                </li>
            </c:forEach>
            <li class="pagination__item">
                <a>...</a>
            </li>
            <li class="pagination__item">
                <a href="${pageContext.request.contextPath}/wallet?index=${endPage}" class="pagination-item__link">${endPage}</a>
            </li>
            <c:if test="${currentPage < endPage}">
                <li class="pagination__item">
                    <a href="${pageContext.request.contextPath}/wallet?index=${currentPage + 1}" class="pagination-item__link">
                        <i class="pagination-item__icon fa-solid fa-chevron-right"></i>
                    </a>

                </li>
            </c:if>
        </ul>

        <!--Pagination end-->

    </section>

</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
