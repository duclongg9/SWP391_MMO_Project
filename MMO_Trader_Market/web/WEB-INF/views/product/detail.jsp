<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="product-detail">
        <div class="product-detail__gallery">
            <c:choose>
                <c:when test="${empty galleryImages}">
                    <div class="product-detail__placeholder">Chưa có ảnh minh họa</div>
                </c:when>
                <c:otherwise>
                    <c:set var="mainImage" value="${galleryImages[0]}" />
                    <c:choose>
                        <c:when test="${fn:startsWith(mainImage, 'http://')
                                        or fn:startsWith(mainImage, 'https://')
                                        or fn:startsWith(mainImage, '//')
                                        or fn:startsWith(mainImage, 'data:')
                                        or fn:startsWith(mainImage, cPath)}">
                            <c:set var="mainImageUrl" value="${mainImage}" />
                        </c:when>
                        <c:otherwise>
                            <c:url var="mainImageUrl" value="${mainImage}" />
                        </c:otherwise>
                    </c:choose>
                    <div class="product-detail__main-image">
                        <img id="mainImage" src="${mainImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(product.name)}" />
                    </div>
                    <c:if test="${fn:length(galleryImages) gt 1}">
                        <ul class="product-detail__thumbnails">
                            <c:forEach var="image" items="${galleryImages}">
                                <c:set var="thumbnailSource" value="${image}" />
                                <c:choose>
                                    <c:when test="${fn:startsWith(thumbnailSource, 'http://')
                                                    or fn:startsWith(thumbnailSource, 'https://')
                                                    or fn:startsWith(thumbnailSource, '//')
                                                    or fn:startsWith(thumbnailSource, 'data:')
                                                    or fn:startsWith(thumbnailSource, cPath)}">
                                        <c:set var="thumbnailUrl" value="${thumbnailSource}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="thumbnailUrl" value="${thumbnailSource}" />
                                    </c:otherwise>
                                </c:choose>
                                <li>
                                    <button class="product-detail__thumbnail" type="button" data-image="${thumbnailUrl}">
                                        <img src="${thumbnailUrl}" alt="Thumbnail sản phẩm" loading="lazy" />
                                    </button>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="product-detail__info">
            <header class="product-detail__header">
                <h2><c:out value="${product.name}" /></h2>
                <p class="product-detail__type">
                    <span class="badge">${product.productTypeLabel}</span>
                    <span class="badge badge--ghost">${product.productSubtypeLabel}</span>
                </p>
                <p class="product-detail__shop">Gian hàng: <strong><c:out value="${product.shopName}" /></strong></p>
            </header>
            <c:if test="${not empty purchaseError}">
                <div class="alert alert--error" role="alert" aria-live="assertive">
                    <c:out value="${purchaseError}" />
                </div>
            </c:if>
            <div class="product-detail__pricing" data-min-price="${priceMin}" data-max-price="${priceMax}">
                <span>Giá hiện tại:</span>
                <strong id="priceDisplay">
                    <c:choose>
                        <c:when test="${priceMin eq priceMax}">
                            <fmt:formatNumber value="${priceMin}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                        </c:when>
                        <c:otherwise>
                            <fmt:formatNumber value="${priceMin}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                            –
                            <fmt:formatNumber value="${priceMax}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                        </c:otherwise>
                    </c:choose>
                </strong>
            </div>
            <ul class="product-detail__stats">
                <li>Tồn kho: <strong id="inventoryDisplay"><c:out value="${product.inventoryCount}" default="0" /></strong></li>
                <li>Đã bán: <strong><c:out value="${product.soldCount}" default="0" /></strong></li>
            </ul>
            <c:if test="${not empty product.shortDescription}">
                <p class="product-detail__summary"><c:out value="${product.shortDescription}" /></p>
            </c:if>
            <form class="product-detail__form" method="post" action="${cPath}/order/buy-now">
                <input type="hidden" name="productId" value="${product.encodedId}" />
                <c:if test="${product.hasVariants}">
                    <fieldset class="product-detail__variants">
                        <legend>Chọn gói sản phẩm</legend>
                        <c:forEach var="variant" items="${variantOptions}" varStatus="status">
                            <c:set var="variantId" value="variant-${status.index}" />
                            <label class="variant-option" for="${variantId}">
                                <input type="radio" id="${variantId}" name="variantCode" value="${variant.variantCode}"
                                       data-price="${variant.price}" data-inventory="${variant.inventoryCount}" />
                                <span class="variant-option__content">
                                    <strong class="variant-option__name"><c:out value="${variant.variantCode}" /></strong>
                                </span>
                            </label>
                        </c:forEach>
                    </fieldset>
                </c:if>
                <div class="product-detail__quantity">
                    <label class="product-detail__label" for="qty">Số lượng</label>
                    <input class="product-detail__input" type="number" id="qty" name="qty" min="1" max="${product.inventoryCount != null ? product.inventoryCount : 1}" value="1"
                           <c:if test="${not canBuy}">disabled</c:if> />
                    </div>
                <c:choose>
                    <c:when test="${canBuy}">
                        <button class="button button--primary" id="buyButton" type="submit">Mua ngay</button>
                    </c:when>
                    <c:when test="${not isAuthenticated}">
                        <!--<a class="button button--primary" href="${cPath}/login.jsp">Đăng nhập để mua hàng</a>-->
                        <c:url var="loginUrl" value="/auth" />
                        <a class="button button--primary" href="${loginUrl}">Đăng nhập để mua hàng</a>
                    </c:when>
                    <c:otherwise>
                        <div class="product-detail__soldout">Sản phẩm tạm hết hàng</div>
                    </c:otherwise>
                </c:choose>
            </form>
            <c:if test="${product.hasVariants}">
                <p class="product-detail__note">* Giá và tồn kho sẽ thay đổi theo biến thể.</p>
            </c:if>
        </div>
    </section>
    <section class="product-detail__description">
        <h3>Mô tả chi tiết</h3>
        <c:choose>
            <c:when test="${not empty product.description}">
                <p><c:out value="${product.description}" /></p>
            </c:when>
            <c:otherwise>
                <p>Chưa có mô tả chi tiết cho sản phẩm này.</p>
            </c:otherwise>
        </c:choose>
    </section>
    <c:if test="${not empty similarProducts}">
        <section class="product-detail__related">
            <h3>Sản phẩm tương tự</h3>

            <div class="product-grid product-grid--compact">
                <c:forEach var="item" items="${similarProducts}">
                    <article class="product-card">
                        <!-- MEDIA -->
                        <div class="product-card__media">
                            <c:choose>
                                <c:when test="${not empty item.primaryImageUrl}">
                                    <c:set var="relatedImageSource" value="${item.primaryImageUrl}" />
                                    <c:choose>
                                        <c:when test="${fn:startsWith(relatedImageSource, 'http://')
                                                        or fn:startsWith(relatedImageSource, 'https://')
                                                        or fn:startsWith(relatedImageSource, '//')
                                                        or fn:startsWith(relatedImageSource, 'data:')
                                                        or fn:startsWith(relatedImageSource, cPath)}">
                                            <c:set var="relatedImageUrl" value="${relatedImageSource}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="relatedImageUrl" value="${relatedImageSource}" />
                                        </c:otherwise>
                                    </c:choose>
                                    <img class="product-card__img"
                                         src="${relatedImageUrl}"
                                         alt="Ảnh sản phẩm ${fn:escapeXml(item.name)}"
                                         loading="lazy" />
                                </c:when>
                                <c:otherwise>
                                    <div class="product-card__placeholder">Không có ảnh</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- BODY -->
                        <div class="product-card__body">
                            <h4 class="product-card__title"><c:out value="${item.name}" /></h4>
                            <p class="product-card__meta">
                                <c:out value="${item.productSubtypeLabel}" /> • <c:out value="${item.shopName}" />
                            </p>
                            <p class="product-card__price">
                                <c:choose>
                                    <c:when test="${item.minPrice eq item.maxPrice}">
                                        <fmt:formatNumber value="${item.minPrice}" type="currency" currencySymbol="đ"
                                                          minFractionDigits="0" maxFractionDigits="0" />
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatNumber value="${item.minPrice}" type="currency" currencySymbol="đ"
                                                          minFractionDigits="0" maxFractionDigits="0" /> –
                                        <fmt:formatNumber value="${item.maxPrice}" type="currency" currencySymbol="đ"
                                                          minFractionDigits="0" maxFractionDigits="0" />
                                    </c:otherwise>
                                </c:choose>
                            </p>

                            <c:url var="relatedUrl" value="/product/detail/${item.encodedId}" />
                            <a class="button button--ghost product-card__cta" href="${relatedUrl}">Xem chi tiết</a>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </section>
    </c:if>

</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<script>
    (function () {
        const priceDisplay = document.getElementById('priceDisplay');
        const inventoryDisplay = document.getElementById('inventoryDisplay');
        const qtyInput = document.getElementById('qty');
        const buyButton = document.getElementById('buyButton');
        const priceContainer = document.querySelector('.product-detail__pricing');
        const minPrice = priceContainer && priceContainer.dataset.minPrice
                ? parseFloat(priceContainer.dataset.minPrice)
                : 0;
        const maxPrice = priceContainer && priceContainer.dataset.maxPrice
                ? parseFloat(priceContainer.dataset.maxPrice)
                : 0;
        const formatter = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND', maximumFractionDigits: 0, minimumFractionDigits: 0});
        const variantInputs = document.querySelectorAll('input[name="variantCode"]');
        const thumbnails = document.querySelectorAll('.product-detail__thumbnail');
        const mainImage = document.getElementById('mainImage');

        function updatePriceRange(rangeMin, rangeMax) {
            if (!priceDisplay) {
                return;
            }
            if (!isFinite(rangeMin) || !isFinite(rangeMax)) {
                priceDisplay.textContent = formatter.format(minPrice || 0);
                return;
            }
            if (Math.abs(rangeMin - rangeMax) < 0.0001) {
                priceDisplay.textContent = formatter.format(rangeMin);
            } else {
                priceDisplay.textContent = formatter.format(rangeMin) + ' – ' + formatter.format(rangeMax);
            }
        }

        function updateForVariant(radio) {
            if (!radio) {
                updatePriceRange(minPrice, maxPrice);
                return;
            }
            const variantPrice = parseFloat(radio.dataset.price || '0');
            const variantInventory = parseInt(radio.dataset.inventory || '0', 10);
            if (isFinite(variantPrice)) {
                updatePriceRange(variantPrice, variantPrice);
            }
            if (inventoryDisplay) {
                inventoryDisplay.textContent = String(Math.max(variantInventory, 0));
            }
            if (qtyInput) {
                const safeInventory = Math.max(variantInventory, 0);
                qtyInput.max = safeInventory > 0 ? safeInventory : 1;
                if (safeInventory > 0) {
                    qtyInput.disabled = false;
                    const currentValue = parseInt(qtyInput.value || '1', 10);
                    if (!currentValue || currentValue > safeInventory) {
                        qtyInput.value = safeInventory;
                    }
                } else {
                    qtyInput.disabled = true;
                    qtyInput.value = 1;
                }
            }
            if (buyButton) {
                if (variantInventory <= 0) {
                    buyButton.disabled = true;
                    buyButton.textContent = 'Hết hàng';
                } else {
                    buyButton.disabled = false;
                    buyButton.textContent = 'Mua ngay';
                }
            }
        }

        function initializeVariants() {
            if (!variantInputs.length) {
                updatePriceRange(minPrice, maxPrice);
                return;
            }
            let selected = Array.from(variantInputs).find(input => input.checked);
            if (!selected) {
                selected = Array.from(variantInputs).find(input => parseInt(input.dataset.inventory || '0', 10) > 0) || variantInputs[0];
                if (selected) {
                    selected.checked = true;
                }
            }
            updateForVariant(selected);
            variantInputs.forEach(input => {
                input.addEventListener('change', () => updateForVariant(input));
            });
        }

        function initGallery() {
            if (!mainImage || !thumbnails.length) {
                return;
            }
            thumbnails.forEach(thumbnail => {
                thumbnail.addEventListener('click', () => {
                    const newImage = thumbnail.dataset.image;
                    if (newImage) {
                        mainImage.src = newImage;
                    }
                });
            });
        }

        initializeVariants();
        initGallery();
    })();
</script>
