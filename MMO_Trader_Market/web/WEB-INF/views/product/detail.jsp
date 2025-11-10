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
            <c:set var="primaryVariantImage" value="" />
            <c:set var="variantThumbnailCount" value="0" />
            <c:if test="${product.hasVariants}">
                <c:forEach var="variant" items="${variantOptions}">
                    <c:if test="${not empty variant.imageUrl}">
                        <c:if test="${empty primaryVariantImage}">
                            <c:set var="primaryVariantImage" value="${variant.imageUrl}" />
                        </c:if>
                        <c:set var="variantThumbnailCount" value="${variantThumbnailCount + 1}" />
                    </c:if>
                </c:forEach>
            </c:if>

            <c:choose>
                <c:when test="${not empty primaryVariantImage}">
                    <c:set var="mainImage" value="${primaryVariantImage}" />
                </c:when>
                <c:when test="${not empty galleryImages}">
                    <c:set var="mainImage" value="${galleryImages[0]}" />
                </c:when>
                <c:otherwise>
                    <c:set var="mainImage" value="" />
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${empty mainImage}">
                    <div class="product-detail__placeholder">Chưa có ảnh minh họa</div>
                </c:when>
                <c:otherwise>
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

                    <c:set var="shouldRenderThumbnails"
                           value="${variantThumbnailCount gt 0 or fn:length(galleryImages) gt 1}" />
                    <c:if test="${shouldRenderThumbnails}">
                        <ul class="product-detail__thumbnails">
                            <c:if test="${variantThumbnailCount gt 0}">
                                <c:forEach var="variant" items="${variantOptions}" varStatus="status">
                                    <c:set var="variantImageUrl" value="" />
                                    <c:if test="${not empty variant.imageUrl}">
                                        <c:set var="variantImageSource" value="${variant.imageUrl}" />
                                        <c:choose>
                                            <c:when test="${fn:startsWith(variantImageSource, 'http://')
                                                            or fn:startsWith(variantImageSource, 'https://')
                                                            or fn:startsWith(variantImageSource, '//')
                                                            or fn:startsWith(variantImageSource, 'data:')
                                                            or fn:startsWith(variantImageSource, cPath)}">
                                                <c:set var="variantImageUrl" value="${variantImageSource}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:url var="variantImageUrl" value="${variantImageSource}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                    <c:if test="${not empty variantImageUrl}">
                                        <c:set var="variantId" value="variant-${status.index}" />
                                        <li>
                                            <button class="product-detail__thumbnail" type="button"
                                                    data-image="${variantImageUrl}"
                                                    data-variant="${variantId}"
                                                    aria-label="Biến thể ${fn:escapeXml(variant.variantCode)}"
                                                    <c:if test="${variantImageUrl eq mainImageUrl}">aria-current="true"</c:if>>
                                                <img src="${variantImageUrl}" alt="Ảnh biến thể ${fn:escapeXml(variant.variantCode)}"
                                                     loading="lazy" />
                                            </button>
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </c:if>

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
                                    <button class="product-detail__thumbnail" type="button" data-image="${thumbnailUrl}"
                                            <c:if test="${thumbnailUrl eq mainImageUrl}">aria-current="true"</c:if>>
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
            <c:url var="purchaseCheckUrl" value="/product/detail/${productToken}/availability" />
            <style>
                .product-detail__availability {
                    margin-top: 1rem;
                }

                .product-detail__availability-card {
                    border-radius: 12px;
                    padding: 1rem 1.25rem;
                    background: #f8fafc;
                    border: 1px solid #cbd5f5;
                    color: #1e293b;
                    display: flex;
                    flex-direction: column;
                    gap: 0.4rem;
                }

                .product-detail__availability-card--ok {
                    border-color: #22c55e;
                    background: #ecfdf5;
                    color: #166534;
                }

                .product-detail__availability-card--warn {
                    border-color: #f97316;
                    background: #fff7ed;
                    color: #9a3412;
                }

                .product-detail__availability-meta {
                    font-size: 0.95rem;
                }
            </style>
            <form class="product-detail__form" method="post" action="${cPath}/order/buy-now" data-check-endpoint="${purchaseCheckUrl}">
                <input type="hidden" name="productId" value="${product.encodedId}" />
                <c:if test="${product.hasVariants}">
                    <fieldset class="product-detail__variants">
                        <legend>Chọn gói sản phẩm</legend>
                        <c:forEach var="variant" items="${variantOptions}" varStatus="status">
                            <c:set var="variantId" value="variant-${status.index}" />
                            <label class="variant-option" for="${variantId}">
                                <c:set var="variantImageUrl" value="" />
                                <c:if test="${not empty variant.imageUrl}">
                                    <c:set var="variantImageSource" value="${variant.imageUrl}" />
                                    <c:choose>
                                        <c:when test="${fn:startsWith(variantImageSource, 'http://')
                                                        or fn:startsWith(variantImageSource, 'https://')
                                                        or fn:startsWith(variantImageSource, '//')
                                                        or fn:startsWith(variantImageSource, 'data:')
                                                        or fn:startsWith(variantImageSource, cPath)}">
                                            <c:set var="variantImageUrl" value="${variantImageSource}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="variantImageUrl" value="${variantImageSource}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <input type="radio" id="${variantId}" name="variantCode" value="${variant.variantCode}"
                                       data-price="${variant.price}" data-inventory="${variant.inventoryCount}"
                                       data-image="${variantImageUrl}" />
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
                <div class="product-detail__availability" id="purchaseCheckStatus">
                    <div class="product-detail__availability-card product-detail__availability-card--warn">
                        <div class="product-detail__availability-meta">Hệ thống sẽ kiểm tra ví và tồn kho sau khi bạn chọn biến thể.</div>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${canBuy}">
                        <button class="button button--primary" id="buyButton" type="submit" >Mua ngay</button>
                    </c:when>
                    <c:when test="${not isAuthenticated}">
                        <!--<a class="button button--primary" href="${cPath}/login.jsp">Đăng nhập để mua hàng</a>-->
                        <c:url var="loginUrl" value="/auth" />
                        <a class="button button--primary" href="${loginUrl}">Đăng nhập để mua hàng</a>
                    </c:when>
                    <c:otherwise>
                        <div class="product-detail__soldout">
                            <c:choose>
                                <c:when test="${isProductOwner}">
                                    Bạn không thể mua sản phẩm từ gian hàng của mình.
                                </c:when>
                                <c:when test="${not empty purchaseDisabledReason}">
                                    <c:out value="${purchaseDisabledReason}" />
                                </c:when>
                                <c:otherwise>
                                    Sản phẩm tạm hết hàng
                                </c:otherwise>
                            </c:choose>
                        </div>
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
        const purchaseForm = document.querySelector('.product-detail__form');
        const availabilityContainer = document.getElementById('purchaseCheckStatus');
        const checkEndpoint = purchaseForm ? purchaseForm.dataset.checkEndpoint : null;
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
        let inventoryAllowsPurchase = true;
        let walletAllowsPurchase = true;

        function normalizeUrl(url) {
            if (!url) {
                return '';
            }
            try {
                return new URL(url, window.location.origin).href;
            } catch (error) {
                return url;
            }
        }

        function updateMainImage(newUrl) {
            if (!mainImage || !newUrl) {
                return;
            }
            mainImage.src = newUrl;
        }

        function setActiveThumbnail(targetUrl) {
            if (!thumbnails.length) {
                return;
            }
            const normalizedTarget = normalizeUrl(targetUrl);
            let matched = false;
            thumbnails.forEach(button => {
                const buttonUrl = normalizeUrl(button.dataset.image);
                if (normalizedTarget && buttonUrl === normalizedTarget) {
                    button.setAttribute('aria-current', 'true');
                    matched = true;
                } else {
                    button.removeAttribute('aria-current');
                }
            });
            if (!matched && mainImage) {
                const currentUrl = normalizeUrl(mainImage.src);
                thumbnails.forEach(button => {
                    const buttonUrl = normalizeUrl(button.dataset.image);
                    if (buttonUrl && buttonUrl === currentUrl) {
                        button.setAttribute('aria-current', 'true');
                        matched = true;
                    }
                });
            }
            if (!matched) {
                thumbnails.forEach(button => button.removeAttribute('aria-current'));
            }
        }

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
                setActiveThumbnail(mainImage ? mainImage.src : '');
                inventoryAllowsPurchase = true;
                requestPurchasePreview(null);
                updateBuyButton();
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
            inventoryAllowsPurchase = variantInventory > 0;
            const variantImage = radio.dataset.image;
            if (variantImage) {
                updateMainImage(variantImage);
            }
            setActiveThumbnail(variantImage || (mainImage ? mainImage.src : ''));
            requestPurchasePreview(radio.value || null);
            updateBuyButton();
        }

        function initializeVariants() {
            if (!variantInputs.length) {
                updatePriceRange(minPrice, maxPrice);
                setActiveThumbnail(mainImage ? mainImage.src : '');
                requestPurchasePreview(null);
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

        function updateBuyButton() {
            if (!buyButton) {
                return;
            }
            if (!inventoryAllowsPurchase) {
                buyButton.disabled = true;
                buyButton.textContent = 'Hết hàng';
                return;
            }
            if (!walletAllowsPurchase) {
                buyButton.disabled = true;
                buyButton.textContent = 'Không đủ số dư ví';
                return;
            }
            buyButton.disabled = false;
            buyButton.textContent = 'Mua ngay';
        }

        function renderPurchaseStatus(payload) {
            if (!availabilityContainer) {
                return;
            }
            availabilityContainer.innerHTML = '';
            const wrapper = document.createElement('div');
            wrapper.className = 'product-detail__availability-card';
            if (!payload) {
                wrapper.textContent = 'Không thể kiểm tra thông tin ví và tồn kho.';
                availabilityContainer.appendChild(wrapper);
                return;
            }
            if (payload.canPurchase) {
                wrapper.classList.add('product-detail__availability-card--ok');
                wrapper.textContent = 'Ví đủ số dư và kho sẵn sàng cho đơn hàng này.';
                if (payload.totalPrice) {
                    const totalNumber = Number(payload.totalPrice);
                    if (Number.isFinite(totalNumber)) {
                        const priceInfo = document.createElement('div');
                        priceInfo.className = 'product-detail__availability-meta';
                        priceInfo.textContent = 'Tổng tiền dự kiến: ' + formatter.format(totalNumber);
                        wrapper.appendChild(priceInfo);
                    }
                }
            } else {
                wrapper.classList.add('product-detail__availability-card--warn');
                wrapper.textContent = 'Không thể hoàn tất thanh toán ngay.';
                if (Array.isArray(payload.blockers) && payload.blockers.length) {
                    payload.blockers.forEach(message => {
                        const item = document.createElement('div');
                        item.className = 'product-detail__availability-meta';
                        item.textContent = message;
                        wrapper.appendChild(item);
                    });
                }
            }
            availabilityContainer.appendChild(wrapper);
        }

        function requestPurchasePreview(selectedVariantCode) {
            if (!checkEndpoint) {
                return;
            }
            const quantity = qtyInput ? Math.max(parseInt(qtyInput.value || '1', 10), 1) : 1;
            const params = new URLSearchParams();
            params.set('quantity', String(quantity));
            if (selectedVariantCode) {
                params.set('variantCode', selectedVariantCode);
            }
            fetch(checkEndpoint + '?' + params.toString(), {headers: {'Accept': 'application/json'}})
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Preview failed');
                    }
                    return response.json();
                })
                .then(data => {
                    if (data && data.hasInventory === false) {
                        inventoryAllowsPurchase = false;
                    }
                    const walletReady = Boolean(data.walletActive) && Boolean(data.walletHasBalance);
                    walletAllowsPurchase = walletReady && Boolean(data.hasCredentials !== false);
                    renderPurchaseStatus(data);
                    updateBuyButton();
                })
                .catch(() => {
                    walletAllowsPurchase = true;
                    renderPurchaseStatus(null);
                    updateBuyButton();
                });
        }

        if (qtyInput) {
            qtyInput.addEventListener('change', () => {
                const selected = Array.from(variantInputs).find(input => input.checked);
                requestPurchasePreview(selected ? selected.value : null);
            });
            qtyInput.addEventListener('input', () => {
                const selected = Array.from(variantInputs).find(input => input.checked);
                requestPurchasePreview(selected ? selected.value : null);
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
                        updateMainImage(newImage);
                    }
                    setActiveThumbnail(newImage || (mainImage ? mainImage.src : ''));
                    const targetVariantId = thumbnail.dataset.variant;
                    if (targetVariantId) {
                        const variantInput = document.getElementById(targetVariantId);
                        if (variantInput) {
                            if (!variantInput.checked) {
                                variantInput.checked = true;
                            }
                            variantInput.dispatchEvent(new Event('change', {bubbles: true}));
                        }
                    }
                });
            });
            setActiveThumbnail(mainImage ? mainImage.src : '');
        }

        initializeVariants();
        initGallery();
    })();
</script>
