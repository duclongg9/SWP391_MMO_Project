<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.*" %>

<%
    // ===== Biến layout chung =====
    request.setAttribute("pageTitle", "Nạp / Rút của tôi");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");

    // Header brand/home
    request.setAttribute("headerHomeHref", request.getContextPath() + "/home");

    // Nav items (menu ngang trong header dùng chung)
    List<Map<String, String>> navItems = new ArrayList<>();
    Map<String, String> homeLink = new HashMap<>();
    homeLink.put("href", request.getContextPath() + "/home");
    homeLink.put("label", "Trang chủ");
    navItems.add(homeLink);

    Map<String, String> walletLink = new HashMap<>();
    walletLink.put("href", request.getContextPath() + "/wallet");
    walletLink.put("label", "Ví của tôi");
    navItems.add(walletLink);

    request.setAttribute("navItems", navItems);

    // Styles bổ sung (Font Awesome + filterbar)
    List<String> extraStylesheets = new ArrayList<>();
    extraStylesheets.add("https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css");
    extraStylesheets.add(request.getContextPath() + "/assets/css/components/filterbar.css");
    request.setAttribute("extraStylesheets", extraStylesheets);
%>

<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <section class="panel" style="max-width:1200px;margin:0 auto;">
        <div class="panel__header" style="display:flex;align-items:center;justify-content:space-between;gap:12px;">
            <h2 class="panel__title" style="margin:0;">Nạp / Rút của tôi</h2>
            <div class="btn-group" style="display:flex;gap:10px;">
                <c:url var="depositUrl" value="/wallet/deposit" />
                <a class="button button--primary" href="${depositUrl}" title="Tạo yêu cầu nạp tiền qua VNPAY">
                    <i class="fa-solid fa-plus-circle" aria-hidden="true"></i> Nạp tiền (VNPAY)
                </a>

                <c:url var="withdrawUrl" value="/withdraw" />
                <a class="button button--secondary" href="${withdrawUrl}" title="Tạo yêu cầu rút tiền">
                    <i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i> Rút tiền
                </a>
            </div>
        </div>

        <%-- ====== FILTER BAR ====== --%>
        <c:url var="filterAction" value="/wallet/cash" />
        <form id="cashFilter" class="filters filterbar" method="get" action="${filterAction}">
            <div class="filterbar__row" style="gap:12px;align-items:flex-end;">
                <div class="filterbar__field">
                    <label class="filterbar__label" for="typeSelect">Loại</label>
                    <select class="select" name="type" id="typeSelect">
                        <option value="all"        ${param.type=='all' || empty param.type ? 'selected':''}>Tất cả</option>
                        <option value="Deposit"    ${param.type=='Deposit'    ? 'selected':''}>Nạp tiền</option>
                        <option value="Withdrawal" ${param.type=='Withdrawal' ? 'selected':''}>Rút tiền</option>
                    </select>
                </div>

                <div class="filterbar__field">
                    <label class="filterbar__label" for="statusSelect">Trạng thái</label>
                    <select class="select" name="status" id="statusSelect">
                        <option value="all"       ${param.status=='all' || empty param.status ? 'selected':''}>Tất cả</option>
                        <option value="Pending"   ${param.status=='Pending'   ? 'selected':''}>Pending</option>
                        <option value="Completed" ${param.status=='Completed' ? 'selected':''}>Completed</option>
                        <option value="Rejected"  ${param.status=='Rejected'  ? 'selected':''}>Rejected</option>
                    </select>
                </div>

                <div class="filterbar__field">
                    <label class="filterbar__label">Số tiền</label>
                    <div class="amount-range">
                        <input type="number" name="minAmount" class="form-control" placeholder="Từ"  value="${fn:escapeXml(param.minAmount)}" />
                        <span class="amount-range__sep">–</span>
                        <input type="number" name="maxAmount" class="form-control" placeholder="Đến" value="${fn:escapeXml(param.maxAmount)}" />
                    </div>
                </div>

                <div class="filterbar__field">
                    <label class="filterbar__label">Thời gian</label>
                    <div class="time-range">
                        <select name="preset" class="select" id="presetSelect">
                            <option value="">Chọn nhanh</option>
                            <option value="today" ${param.preset=='today'?'selected':''}>Hôm nay</option>
                            <option value="7d"    ${param.preset=='7d'   ?'selected':''}>7 ngày</option>
                            <option value="30d"   ${param.preset=='30d'  ?'selected':''}>30 ngày</option>
                        </select>
                        <input type="date" name="start" class="form-control" value="${fn:escapeXml(param.start)}" />
                        <span class="amount-range__sep">–</span>
                        <input type="date" name="end"   class="form-control" value="${fn:escapeXml(param.end)}" />
                    </div>
                </div>

                <div class="filterbar__field">
                    <label class="filterbar__label" for="sortSelect">Sắp xếp</label>
                    <select id="sortSelect" name="sort" class="select">
                        <option value="date_desc"   ${param.sort=='date_desc'  || empty param.sort ? 'selected':''}>Mới nhất</option>
                        <option value="date_asc"    ${param.sort=='date_asc'   ? 'selected':''}>Cũ nhất</option>
                        <option value="amount_desc" ${param.sort=='amount_desc'? 'selected':''}>Số tiền ↓</option>
                        <option value="amount_asc"  ${param.sort=='amount_asc' ? 'selected':''}>Số tiền ↑</option>
                    </select>
                </div>

                <div class="filterbar__actions" style="display:flex; flex-direction: column;gap:8px;">
                    <button type="submit" class="btn btn--primary">Lọc</button>
                    <a class="btn btn--reset" href="${filterAction}">Xóa lọc</a>
                </div>
            </div>

            <input type="hidden" name="page" value="1" />
            <input type="hidden" name="size" value="${empty param.size ? 10 : param.size}" />
        </form>

        <%-- ====== TABLE ====== --%>
        <div class="table-responsive" style="margin-top:12px;">
            <table class="table table--interactive">
                <thead>
                <tr>
                    <th>#</th>
                    <th>Loại</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                    <th>Tạo lúc</th>
                    <th>Cập nhật lúc</th>
                    <th>Admin feedback</th>
                    <th>Chi tiết</th>
                </tr>
                </thead>

                <tbody>
                <c:forEach var="t" items="${cashList}" varStatus="st">
                    <fmt:formatNumber value="${t.amount}" type="number" var="amountText"/>
                    <fmt:formatDate value="${t.createdAt}" pattern="dd/MM/yyyy HH:mm" timeZone="Asia/Ho_Chi_Minh" var="createdText"/>

                    <c:choose>
                        <c:when test="${t.processedAt != null}">
                            <fmt:formatDate value="${t.processedAt}" pattern="dd/MM/yyyy HH:mm" timeZone="Asia/Ho_Chi_Minh" var="processedText"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="processedText" value="—"/>
                        </c:otherwise>
                    </c:choose>

                    <c:set var="adminNoteText" value="${empty t.adminNote ? '—' : fn:escapeXml(t.adminNote)}"/>
                    <c:set var="adminUrlText"  value="${empty t.adminProofUrl ? ''  : fn:escapeXml(t.adminProofUrl)}"/>
                    <c:set var="bankImgText"   value="${empty t.bankAccountInfo ? '' : fn:escapeXml(t.bankAccountInfo)}"/>

                    <tr>
                        <td>${(page-1)*size + st.index + 1}</td>
                        <td>
                            <c:choose>
                                <c:when test="${t.type eq 'Deposit'}"><span class="badge bg-primary">Nạp</span></c:when>
                                <c:otherwise><span class="badge bg-warning text-dark">Rút</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${amountText}</td>
                        <td>
                            <c:set var="statusClass"
                                   value="${t.status eq 'Completed' ? 'bg-success' :
                                           (t.status eq 'Pending'   ? 'bg-secondary' : 'bg-danger')}"/>
                            <span class="badge ${statusClass}">${t.status}</span>
                        </td>
                        <td>${createdText}</td>
                        <td>${processedText}</td>
                        <td>${adminNoteText}</td>
                        <td>
                            <button type="button"
                                    class="button button--secondary js-detail"
                                    data-type="${t.type}"
                                    data-status="${fn:escapeXml(t.status)}"
                                    data-amount="${amountText}"
                                    data-created="${createdText}"
                                    data-processed="${processedText}"
                                    data-admin-url="${adminUrlText}"
                                    data-bank-img="${bankImgText}">
                                Chi tiết
                            </button>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty cashList}">
                    <tr><td colspan="8" class="text-center text-muted">Không có giao dịch</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <%-- ===== MODAL: Chi tiết giao dịch ===== --%>
        <style>
            .modal{position:fixed; inset:0; display:none; z-index:1000;}
            .modal.is-open{display:block;}
            .modal__backdrop{position:absolute; inset:0; background:rgba(0,0,0,.45);}
            .modal__dialog{
                position:relative; z-index:1001; max-width:720px; width:clamp(320px, 92vw, 720px);
                margin:6vh auto; background:#fff; border-radius:12px; box-shadow:0 10px 30px rgba(0,0,0,.2);
            }
            .modal__header,.modal__footer{padding:14px 16px; border-bottom:1px solid #eee;}
            .modal__footer{border-top:1px solid #eee; border-bottom:none;}
            .modal__body{padding:16px;}
            .modal__close{background:none;border:0;font-size:22px;cursor:pointer}
            .body--lock{overflow:hidden}
            .img-frame img{display:block; max-width:100%; height:auto; border-radius:8px;}
        </style>

        <div id="detailModal" class="modal" aria-hidden="true">
            <div class="modal__backdrop" data-close="1"></div>
            <div class="modal__dialog" role="dialog" aria-modal="true" aria-labelledby="detailTitle">
                <div class="modal__header" style="display: flex;
    justify-content: space-between;">
                    <h3 id="detailTitle" class="modal__title" style="margin:0;">Chi tiết giao dịch</h3>
                    <button class="modal__close js-close-detail" aria-label="Đóng">×</button>
                </div>

                <div class="modal__body">
                    <div class="grid-2" style="gap:12px;">
                        <div>
                            <div class="meta"><strong>Loại:</strong> <span id="dType">—</span></div>
                            <div class="meta"><strong>Trạng thái:</strong> <span id="dStatus">—</span></div>
                            <div class="meta"><strong>Số tiền:</strong> <span id="dAmount">—</span></div>
                        </div>
                        <div>
                            <div class="meta"><strong>Tạo lúc:</strong> <span id="dCreated">—</span></div>
                            <div class="meta"><strong>Cập nhật/Xử lý lúc:</strong> <span id="dProcessed">—</span></div>
                        </div>
                    </div>

                    <hr class="divider"/>

                    <div class="panel" style="padding:12px;">
                        <h4 class="panel__title" style="margin:0 0 8px;">Admin URL</h4>
                        <div id="adminUrlWrap"><span class="text-muted">—</span></div>
                    </div>

                    <div class="panel" style="padding:12px;margin-top:12px;">
                        <h4 class="panel__title" style="margin:0 0 8px;">QR / thông tin ngân hàng</h4>
                        <div id="bankInfoWrap" class="img-frame"
                             style="min-height:120px;display:flex;align-items:center;justify-content:center;background:#f7f9fc;border:1px dashed #cfd8e3;border-radius:8px;">
                            <span class="text-muted">Chưa có ảnh</span>
                        </div>
                    </div>
                </div>

                <div class="modal__footer">
                    <button class="button button--secondary js-close-detail">Đóng</button>
                </div>
            </div>
        </div>

        <%-- ===== Pagination đẹp + đủ nút ===== --%>
        <c:set var="current" value="${page}" />
        <c:set var="total" value="${pages}" />

        <%-- Tính cửa sổ 5 trang: [startPage, endPage] --%>
        <c:set var="startPage" value="${current - 2}" />
        <c:if test="${startPage < 1}">
            <c:set var="startPage" value="1" />
        </c:if>
        <c:set var="endPage" value="${startPage + 4}" />
        <c:if test="${endPage > total}">
            <c:set var="endPage" value="${total}" />
        </c:if>
        <c:if test="${endPage - startPage < 4}">
            <c:set var="startPage" value="${endPage - 4}" />
            <c:if test="${startPage < 1}">
                <c:set var="startPage" value="1" />
            </c:if>
        </c:if>

        <nav aria-label="Pagination" style="margin-top:14px;">
            <ul class="pagination">

                <%-- FIRST --%>
                <li class="pagination__item ${current == 1 ? 'is-disabled' : ''}">
                    <c:url var="firstUrl" value="/wallet/cash">
                        <c:param name="page" value="1" />
                        <c:param name="size"      value="${empty param.size ? '10' : param.size}" />
                        <c:param name="type"      value="${empty param.type   ? 'all' : param.type}" />
                        <c:param name="status"    value="${empty param.status ? 'all' : param.status}" />
                        <c:param name="minAmount" value="${param.minAmount}" />
                        <c:param name="maxAmount" value="${param.maxAmount}" />
                        <c:param name="start"     value="${param.start}" />
                        <c:param name="end"       value="${param.end}" />
                        <c:param name="preset"    value="${param.preset}" />
                        <c:param name="sort"      value="${empty param.sort ? 'date_desc' : param.sort}" />
                    </c:url>
                    <a class="pagination-item__link" href="${current == 1 ? '#' : firstUrl}" aria-label="Trang đầu">
                        <i class="fa-solid fa-angles-left" aria-hidden="true"></i>
                    </a>
                </li>

                <%-- PREV --%>
                <li class="pagination__item ${current == 1 ? 'is-disabled' : ''}">
                    <c:url var="prevUrl" value="/wallet/cash">
                        <c:param name="page" value="${current - 1}" />
                        <c:param name="size"      value="${empty param.size ? '10' : param.size}" />
                        <c:param name="type"      value="${empty param.type   ? 'all' : param.type}" />
                        <c:param name="status"    value="${empty param.status ? 'all' : param.status}" />
                        <c:param name="minAmount" value="${param.minAmount}" />
                        <c:param name="maxAmount" value="${param.maxAmount}" />
                        <c:param name="start"     value="${param.start}" />
                        <c:param name="end"       value="${param.end}" />
                        <c:param name="preset"    value="${param.preset}" />
                        <c:param name="sort"      value="${empty param.sort ? 'date_desc' : param.sort}" />
                    </c:url>
                    <a class="pagination-item__link" href="${current == 1 ? '#' : prevUrl}" aria-label="Trang trước">
                        <i class="fa-solid fa-chevron-left" aria-hidden="true"></i>
                    </a>
                </li>

                <%-- NUMBERS --%>
                <c:if test="${startPage > 1}">
                    <li class="pagination__item"><span class="pagination__ellipsis">…</span></li>
                </c:if>

                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                    <li class="pagination__item">
                        <c:url var="iUrl" value="/wallet/cash">
                            <c:param name="page" value="${i}" />
                            <c:param name="size"      value="${empty param.size ? '10' : param.size}" />
                            <c:param name="type"      value="${empty param.type   ? 'all' : param.type}" />
                            <c:param name="status"    value="${empty param.status ? 'all' : param.status}" />
                            <c:param name="minAmount" value="${param.minAmount}" />
                            <c:param name="maxAmount" value="${param.maxAmount}" />
                            <c:param name="start"     value="${param.start}" />
                            <c:param name="end"       value="${param.end}" />
                            <c:param name="preset"    value="${param.preset}" />
                            <c:param name="sort"      value="${empty param.sort ? 'date_desc' : param.sort}" />
                        </c:url>
                        <a class="pagination-item__link ${i == current ? 'pagination__item--active' : ''}" href="${iUrl}">${i}</a>
                    </li>
                </c:forEach>

                <c:if test="${endPage < total}">
                    <li class="pagination__item"><span class="pagination__ellipsis">…</span></li>
                </c:if>

                <%-- NEXT --%>
                <li class="pagination__item ${current == total ? 'is-disabled' : ''}">
                    <c:url var="nextUrl" value="/wallet/cash">
                        <c:param name="page" value="${current + 1}" />
                        <c:param name="size"      value="${empty param.size ? '10' : param.size}" />
                        <c:param name="type"      value="${empty param.type   ? 'all' : param.type}" />
                        <c:param name="status"    value="${empty param.status ? 'all' : param.status}" />
                        <c:param name="minAmount" value="${param.minAmount}" />
                        <c:param name="maxAmount" value="${param.maxAmount}" />
                        <c:param name="start"     value="${param.start}" />
                        <c:param name="end"       value="${param.end}" />
                        <c:param name="preset"    value="${param.preset}" />
                        <c:param name="sort"      value="${empty param.sort ? 'date_desc' : param.sort}" />
                    </c:url>
                    <a class="pagination-item__link" href="${current == total ? '#' : nextUrl}" aria-label="Trang sau">
                        <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
                    </a>
                </li>

                <%-- LAST --%>
                <li class="pagination__item ${current == total ? 'is-disabled' : ''}">
                    <c:url var="lastUrl" value="/wallet/cash">
                        <c:param name="page" value="${total}" />
                        <c:param name="size"      value="${empty param.size ? '10' : param.size}" />
                        <c:param name="type"      value="${empty param.type   ? 'all' : param.type}" />
                        <c:param name="status"    value="${empty param.status ? 'all' : param.status}" />
                        <c:param name="minAmount" value="${param.minAmount}" />
                        <c:param name="maxAmount" value="${param.maxAmount}" />
                        <c:param name="start"     value="${param.start}" />
                        <c:param name="end"       value="${param.end}" />
                        <c:param name="preset"    value="${param.preset}" />
                        <c:param name="sort"      value="${empty param.sort ? 'date_desc' : param.sort}" />
                    </c:url>
                    <a class="pagination-item__link" href="${current == total ? '#' : lastUrl}" aria-label="Trang cuối">
                        <i class="fa-solid fa-angles-right" aria-hidden="true"></i>
                    </a>
                </li>
            </ul>
        </nav>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const $  = s => document.querySelector(s);
        const modal = $('#detailModal');
        const table = document.querySelector('.table.table--interactive');

        const open = () => {
            modal.classList.add('is-open');
            modal.setAttribute('aria-hidden','false');
            document.body.classList.add('body--lock');
        };
        const close = () => {
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden','true');
            document.body.classList.remove('body--lock');
        };

        // Đóng khi bấm nút ×, nút "Đóng" hoặc nền tối
        modal.addEventListener('click', (e) => {
            if (e.target.classList.contains('js-close-detail') || e.target.dataset.close === '1') close();
        });
        // Esc để đóng
        document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });

        // Ủy quyền click cho các nút "Chi tiết"
        if (table) {
            table.addEventListener('click', (e) => {
                const btn = e.target.closest('.js-detail');
                if (!btn) return;

                const type      = btn.dataset.type || '';
                const status    = btn.dataset.status || '';
                const amount    = btn.dataset.amount || '';
                const created   = btn.dataset.created || '—';
                const processed = btn.dataset.processed || '—';
                const adminUrl  = btn.dataset.adminUrl || '';
                const bankImg   = btn.dataset.bankImg || '';

                $('#dType').textContent      = (type === 'Deposit') ? 'Nạp' : 'Rút';
                $('#dStatus').textContent    = status;
                $('#dAmount').textContent    = amount;
                $('#dCreated').textContent   = created;
                $('#dProcessed').textContent = processed;

                const adminWrap = $('#adminUrlWrap');
                adminWrap.innerHTML = adminUrl
                    ? `<a href="${adminUrl}" target="_blank" rel="noopener">Mở admin URL</a>`
                    : `<span class="text-muted">—</span>`;

                const bankWrap = $('#bankInfoWrap');
                bankWrap.innerHTML = bankImg
                    ? `<img src="${bankImg}" alt="Bank account info">`
                    : `<span class="text-muted">Chưa có ảnh</span>`;

                open();
            });
        }
    });
</script>
