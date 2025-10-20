<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Tạo gian hàng mới</h2>
            <p class="panel__subtitle">Hãy tạo gian hàng của bạn để bắt đầu bán sản phẩm</p>
        </div>

        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error">
                    <p><c:out value="${error}" /></p>
                </div>
            </c:if>

            <c:if test="${canCreateShop}">
                <form method="post" action="${pageContext.request.contextPath}/seller/shop?action=create" class="form">
                    <div class="form__group">
                        <label for="shopName" class="form__label">
                            Tên gian hàng <span class="form__required">*</span>
                        </label>
                        <input type="text" 
                               id="shopName" 
                               name="shopName" 
                               class="form__input" 
                               value="<c:out value='${shopName}' />"
                               placeholder="Nhập tên gian hàng (3-50 ký tự)"
                               maxlength="50"
                               required>
                        <small class="form__help">
                            Tên gian hàng sẽ hiển thị công khai cho khách hàng
                        </small>
                    </div>

                    <div class="form__group">
                        <label for="description" class="form__label">
                            Mô tả gian hàng <span class="form__required">*</span>
                        </label>
                        <textarea id="description" 
                                  name="description" 
                                  class="form__textarea" 
                                  placeholder="Mô tả về gian hàng, loại sản phẩm bạn bán..."
                                  rows="5"
                                  minlength="10"
                                  maxlength="500"
                                  required><c:out value="${description}" /></textarea>
                        <small class="form__help">
                            Mô tả chi tiết về gian hàng (10-500 ký tự)
                        </small>
                    </div>

                    <div class="form__actions">
                        <button type="submit" class="button button--primary">
                            Tạo gian hàng
                        </button>
                        <a href="${pageContext.request.contextPath}/home" class="button button--ghost">
                            Hủy
                        </a>
                    </div>
                </form>

                <div class="panel panel--info" style="margin-top: 2rem;">
                    <div class="panel__body">
                        <h3>📋 Lưu ý quan trọng:</h3>
                        <ul style="margin: 1rem 0; padding-left: 2rem;">
                            <li>Sau khi tạo gian hàng, bạn cần chờ admin duyệt để kích hoạt</li>
                            <li>Gian hàng phải tuân thủ quy định về nội dung và sản phẩm</li>
                            <li>Mỗi seller chỉ được tạo 1 gian hàng duy nhất</li>
                            <li>Thông tin gian hàng có thể chỉnh sửa sau khi được tạo</li>
                        </ul>
                    </div>
                </div>
            </c:if>

            <c:if test="${not canCreateShop}">
                <div class="alert alert--warning">
                    <p>Bạn không thể tạo gian hàng mới.</p>
                    <p><a href="${pageContext.request.contextPath}/seller/shop" class="button button--primary">
                        Quay lại quản lý gian hàng
                    </a></p>
                </div>
            </c:if>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
