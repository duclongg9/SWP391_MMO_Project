(function () {
    const config = document.getElementById('homepage-config');
    if (!config) {
        return;
    }

    const contextPath = config.dataset.contextPath || '';
    const productsUrl = config.dataset.productsUrl || (contextPath + '/products');
    const detailBase = config.dataset.productDetailBase || (contextPath + '/product/detail/');

    const numberFormatter = new Intl.NumberFormat('vi-VN');
    const currencyFormatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0,
        minimumFractionDigits: 0
    });

    const fragments = document.querySelectorAll('[data-fragment-endpoint]');
    if (!fragments.length) {
        return;
    }

    const context = {
        contextPath,
        productsUrl,
        detailBase,
        numberFormatter,
        currencyFormatter
    };

    const renderers = {
        summary(container, payload, ctx) {
            const summary = payload && payload.summary;
            if (!summary) {
                setStatus(container, 'Không có dữ liệu thống kê.');
                return;
            }
            setMetric(container, 'totalCompletedOrders', summary.totalCompletedOrders, ctx.numberFormatter);
            setMetric(container, 'activeShopCount', summary.activeShopCount, ctx.numberFormatter);
            setMetric(container, 'activeBuyerCount', summary.activeBuyerCount, ctx.numberFormatter);
            setStatus(container, '');
        },
        categories(container, payload, ctx) {
            const list = container.querySelector('[data-fragment-list]');
            if (!list) {
                setStatus(container, 'Không thể hiển thị danh mục.');
                return;
            }
            clearChildren(list);
            const categories = payload && Array.isArray(payload.categories) ? payload.categories : [];
            if (!categories.length) {
                list.appendChild(createTextItem('Chưa có danh mục khả dụng.'));
                setStatus(container, '');
                return;
            }
            categories.forEach((category) => {
                list.appendChild(renderCategory(category, ctx));
            });
            setStatus(container, '');
        },
        highlights(container, payload, ctx) {
            const statusEl = container.querySelector('[data-fragment-status]');
            if (statusEl) {
                statusEl.remove();
            }
            clearChildren(container);
            const products = payload && Array.isArray(payload.products) ? payload.products : [];
            if (!products.length) {
                container.appendChild(createTextParagraph('Chưa có dữ liệu.'));
                return;
            }
            products.forEach((product) => {
                container.appendChild(renderProductCard(product, ctx));
            });
        },
        systemNotes(container, payload) {
            const list = container.querySelector('[data-fragment-list]');
            if (!list) {
                setStatus(container, 'Không thể hiển thị ghi chú.');
                return;
            }
            clearChildren(list);
            const notes = payload && Array.isArray(payload.notes) ? payload.notes : [];
            if (!notes.length) {
                list.appendChild(createTextItem('Chưa có ghi chú hệ thống.'));
                setStatus(container, '');
                return;
            }
            notes.forEach((note) => {
                const li = document.createElement('li');
                const strong = document.createElement('strong');
                strong.textContent = note.description || note.key || '---';
                li.appendChild(strong);
                list.appendChild(li);
            });
            setStatus(container, '');
        }
    };

    fragments.forEach((container) => {
        const endpoint = container.dataset.fragmentEndpoint;
        const type = container.dataset.fragmentType || container.dataset.fragment;
        if (!endpoint || !type || !(type in renderers)) {
            return;
        }
        const statusEl = ensureStatusElement(container);
        if (statusEl && statusEl.textContent.trim().length === 0) {
            statusEl.textContent = 'Đang tải dữ liệu...';
        }
        fetch(endpoint, {
            headers: {
                'Accept': 'application/json'
            }
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then((data) => {
                if (data && data.error) {
                    throw new Error(data.error);
                }
                renderers[type](container, data || {}, context);
            })
            .catch((error) => {
                console.error('Không thể tải fragment', type, error);
                setStatus(container, 'Không tải được dữ liệu. Vui lòng thử lại.');
            });
    });

    function ensureStatusElement(container) {
        let statusEl = container.querySelector('[data-fragment-status]');
        if (!statusEl) {
            statusEl = document.createElement('p');
            statusEl.className = 'fragment__status';
            statusEl.setAttribute('data-fragment-status', '');
            statusEl.textContent = '';
            container.appendChild(statusEl);
        }
        return statusEl;
    }

    function setMetric(container, field, value, formatter) {
        const target = container.querySelector('[data-field="' + field + '"]');
        if (!target) {
            return;
        }
        if (value === null || value === undefined) {
            target.textContent = '--';
            return;
        }
        target.textContent = typeof value === 'number' ? formatter.format(value) : value;
    }

    function setStatus(container, message) {
        const statusEl = ensureStatusElement(container);
        if (!statusEl) {
            return;
        }
        if (!message) {
            statusEl.textContent = '';
            statusEl.style.display = 'none';
        } else {
            statusEl.textContent = message;
            statusEl.style.display = '';
        }
    }

    function clearChildren(element) {
        if (!element) {
            return;
        }
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    function createTextItem(content) {
        const li = document.createElement('li');
        li.textContent = content;
        return li;
    }

    function createTextParagraph(content) {
        const p = document.createElement('p');
        p.textContent = content;
        return p;
    }

    function renderCategory(category, ctx) {
        const li = document.createElement('li');
        li.className = 'category-menu__item';

        const icon = document.createElement('span');
        icon.className = 'category-menu__icon';
        icon.textContent = '🏷️';
        li.appendChild(icon);

        const wrapper = document.createElement('div');
        const strong = document.createElement('strong');
        const link = document.createElement('a');
        link.href = buildCategoryUrl(category.typeCode, ctx.productsUrl);
        link.textContent = category.typeLabel || category.typeCode || 'Danh mục';
        strong.appendChild(link);
        wrapper.appendChild(strong);

        const description = document.createElement('p');
        const total = typeof category.availableProducts === 'number'
            ? ctx.numberFormatter.format(category.availableProducts)
            : '0';
        description.textContent = total + ' sản phẩm khả dụng';
        wrapper.appendChild(description);

        li.appendChild(wrapper);
        return li;
    }

    function buildCategoryUrl(typeCode, baseUrl) {
        if (!typeCode) {
            return baseUrl;
        }
        const url = new URL(baseUrl, window.location.origin);
        url.searchParams.set('type', typeCode);
        return url.toString().replace(window.location.origin, '');
    }

    function renderProductCard(product, ctx) {
        const article = document.createElement('article');
        article.className = 'product-card product-card--featured product-card--grid';

        const media = document.createElement('div');
        media.className = 'product-card__image product-card__media';
        const imageUrl = resolveImageUrl(product.primaryImageUrl, ctx.contextPath);
        if (imageUrl) {
            const img = document.createElement('img');
            img.className = 'product-card__img';
            img.src = imageUrl;
            img.loading = 'lazy';
            img.alt = 'Ảnh sản phẩm ' + (product.name || '');
            media.appendChild(img);
        } else {
            const placeholder = document.createElement('div');
            placeholder.className = 'product-card__placeholder';
            placeholder.textContent = 'Không có ảnh';
            media.appendChild(placeholder);
        }
        article.appendChild(media);

        const body = document.createElement('div');
        body.className = 'product-card__body product-card__body--stack';

        const header = document.createElement('header');
        header.className = 'product-card__header';
        const title = document.createElement('h4');
        title.textContent = product.name || 'Sản phẩm';
        header.appendChild(title);
        body.appendChild(header);

        const meta = document.createElement('p');
        meta.className = 'product-card__meta';
        const typeSpan = document.createElement('span');
        const typeLabel = product.productTypeLabel || '';
        const subtypeLabel = product.productSubtypeLabel || '';
        const separator = typeLabel && subtypeLabel ? ' • ' : '';
        typeSpan.textContent = typeLabel + separator + subtypeLabel;
        const shopSpan = document.createElement('span');
        shopSpan.textContent = 'Shop: ';
        const shopStrong = document.createElement('strong');
        shopStrong.textContent = product.shopName || '---';
        shopSpan.appendChild(shopStrong);
        meta.appendChild(typeSpan);
        meta.appendChild(shopSpan);
        body.appendChild(meta);

        const description = document.createElement('p');
        description.className = 'product-card__description';
        description.textContent = product.shortDescription || '';
        body.appendChild(description);

        const price = document.createElement('p');
        price.className = 'product-card__price';
        const minPrice = toNumber(product.minPrice);
        const maxPrice = toNumber(product.maxPrice);
        if (minPrice !== null && maxPrice !== null) {
            if (minPrice === maxPrice) {
                price.textContent = 'Giá ' + ctx.currencyFormatter.format(minPrice);
            } else {
                price.textContent = 'Giá từ ' + ctx.currencyFormatter.format(minPrice) + ' – '
                    + ctx.currencyFormatter.format(maxPrice);
            }
        } else {
            price.textContent = 'Liên hệ shop để biết giá.';
        }
        body.appendChild(price);

        const stats = document.createElement('ul');
        stats.className = 'product-card__meta product-card__meta--stats';
        const inventory = document.createElement('li');
        inventory.textContent = 'Tồn kho: ';
        const inventoryStrong = document.createElement('strong');
        inventoryStrong.textContent = renderNumber(product.inventoryCount, ctx.numberFormatter);
        inventory.appendChild(inventoryStrong);
        const sold = document.createElement('li');
        sold.textContent = 'Đã bán: ';
        const soldStrong = document.createElement('strong');
        soldStrong.textContent = renderNumber(product.soldCount, ctx.numberFormatter);
        sold.appendChild(soldStrong);
        stats.appendChild(inventory);
        stats.appendChild(sold);
        body.appendChild(stats);

        const footer = document.createElement('footer');
        footer.className = 'product-card__footer';
        const actions = document.createElement('div');
        actions.className = 'product-card__actions product-card__actions--justify';
        const link = document.createElement('a');
        link.className = 'button button--primary product-card__cta';
        link.href = buildDetailUrl(product.encodedId, ctx.detailBase);
        link.textContent = 'Xem chi tiết';
        actions.appendChild(link);
        footer.appendChild(actions);
        body.appendChild(footer);

        article.appendChild(body);
        return article;
    }

    function resolveImageUrl(source, contextPath) {
        if (!source) {
            return null;
        }
        const trimmed = source.trim();
        if (!trimmed) {
            return null;
        }
        if (/^(https?:)?\/\//i.test(trimmed) || trimmed.startsWith('data:')) {
            return trimmed;
        }
        if (trimmed.startsWith(contextPath)) {
            return trimmed;
        }
        if (trimmed.startsWith('/')) {
            return contextPath + trimmed;
        }
        return contextPath + '/' + trimmed.replace(/^\/+/, '');
    }

    function toNumber(value) {
        if (typeof value === 'number') {
            return Number.isFinite(value) ? value : null;
        }
        if (typeof value === 'string' && value.trim() !== '') {
            const parsed = Number(value);
            return Number.isFinite(parsed) ? parsed : null;
        }
        return null;
    }

    function renderNumber(value, formatter) {
        const numeric = toNumber(value);
        return numeric === null ? '0' : formatter.format(numeric);
    }

    function buildDetailUrl(encodedId, base) {
        if (!encodedId) {
            return base;
        }
        return base + encodedId;
    }
})();
