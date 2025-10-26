// Simple Variant Manager for Seller Product Forms

document.addEventListener('DOMContentLoaded', () => {
    const schemaContainer = document.getElementById('variant-schema-builder');
    const variantsContainer = document.getElementById('variants-table-container');
    const addOptionBtn = document.getElementById('add-variant-option');
    const productForm = document.getElementById('product-form');
    
    // Hidden inputs
    const variantSchemaInput = document.getElementById('variantSchema');
    const variantsJsonInput = document.getElementById('variantsJson');

    if (!schemaContainer || !addOptionBtn || !productForm) {
        return; // Not on a page with variant management
    }

    // --- INITIALIZE UI FROM EXISTING DATA (FOR EDIT PAGE) ---
    const initializeUI = () => {
        try {
            const initialSchema = JSON.parse(variantSchemaInput.value || '[]');
            const initialVariants = JSON.parse(variantsJsonInput.value || '[]');
            
            if (initialSchema.length > 0) {
                schemaContainer.innerHTML = ''; // Clear placeholder
                initialSchema.forEach(option => {
                    addSchemaOption(option.label, option.values);
                });
                renderVariantsTable(initialVariants);
            }
        } catch (e) {
            console.error("Failed to parse initial variant JSON:", e);
            variantSchemaInput.value = '';
            variantsJsonInput.value = '';
        }
    };
    
    // --- EVENT LISTENERS ---
    addOptionBtn.addEventListener('click', () => {
        if (schemaContainer.querySelector('.empty-schema')) {
            schemaContainer.innerHTML = '';
        }
        addSchemaOption();
    });

    schemaContainer.addEventListener('click', (e) => {
        if (e.target.classList.contains('remove-option')) {
            e.target.closest('.variant-option-group').remove();
            renderVariantsTable();
        }
        if (e.target.classList.contains('add-option-value')) {
            addSchemaValue(e.target.previousElementSibling);
        }
        if (e.target.classList.contains('remove-value')) {
            e.target.closest('.option-value-tag').remove();
            renderVariantsTable();
        }
    });

    schemaContainer.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && e.target.classList.contains('option-value-input')) {
            e.preventDefault();
            addSchemaValue(e.target);
        }
    });
    
    productForm.addEventListener('submit', serializeData);

    // --- SCHEMA BUILDER FUNCTIONS ---

    const addSchemaOption = (label = '', values = []) => {
        const optionId = `opt_${Date.now()}`;
        const group = document.createElement('div');
        group.className = 'variant-option-group';
        group.innerHTML = `
            <div class="form-group">
                <label>Tên tùy chọn (VD: Màu sắc, Dung lượng)</label>
                <div class="input-with-button">
                    <input type="text" class="form-input option-label-input" placeholder="Tên tùy chọn" value="${label}">
                    <button type="button" class="button button--danger remove-option">Xóa</button>
                </div>
            </div>
            <div class="form-group">
                <label>Các giá trị (VD: Xanh, Đỏ, 128GB)</label>
                <div class="option-values-container">
                    ${values.map(val => `<span class="option-value-tag">${val}<button type="button" class="remove-value">&times;</button></span>`).join('')}
                </div>
                <div class="input-with-button">
                     <input type="text" class="form-input option-value-input" placeholder="Thêm giá trị rồi Enter">
                     <button type="button" class="button button--secondary add-option-value">Thêm</button>
                </div>
            </div>
        `;
        schemaContainer.appendChild(group);
        group.querySelector('.option-label-input').addEventListener('input', () => renderVariantsTable());
    };

    const addSchemaValue = (inputElement) => {
        const value = inputElement.value.trim();
        if (value) {
            const container = inputElement.closest('.form-group').querySelector('.option-values-container');
            const tag = document.createElement('span');
            tag.className = 'option-value-tag';
            tag.innerHTML = `${value}<button type="button" class="remove-value">&times;</button>`;
            container.appendChild(tag);
            inputElement.value = '';
            inputElement.focus();
            renderVariantsTable();
        }
    };

    // --- VARIANTS TABLE FUNCTIONS ---

    const renderVariantsTable = (existingVariants = []) => {
        const schema = getSchemaFromUI();
        if (schema.length === 0) {
            variantsContainer.innerHTML = '<p class="text-muted">Thêm tùy chọn để bắt đầu tạo biến thể.</p>';
            return;
        }

        const combinations = cartesian(...schema.map(opt => opt.values));
        
        let tableHTML = `
            <table class="table variants-table">
                <thead>
                    <tr>
                        ${schema.map(opt => `<th>${opt.label}</th>`).join('')}
                        <th>Giá (VNĐ) <span class="required">*</span></th>
                        <th>Số lượng <span class="required">*</span></th>
                    </tr>
                </thead>
                <tbody>
                    ${combinations.map(combo => {
                        const attributes = {};
                        schema.forEach((opt, i) => attributes[opt.id] = combo[i]);
                        
                        // Find existing variant to pre-fill data
                        const existing = existingVariants.find(v => {
                            let match = true;
                            for(const key in attributes) {
                                if (v.attributes[key] !== attributes[key]) match = false;
                            }
                            return match;
                        });

                        const price = existing ? existing.price : '';
                        const inventory = existing ? existing.inventory_count : '';

                        return `
                            <tr data-variant-attributes='${JSON.stringify(attributes)}'>
                                ${combo.map(val => `<td>${val}</td>`).join('')}
                                <td><input type="number" class="form-input variant-price" value="${price}" placeholder="Giá biến thể" required></td>
                                <td><input type="number" class="form-input variant-inventory" value="${inventory}" placeholder="Số lượng" required></td>
                            </tr>
                        `
                    }).join('')}
                </tbody>
            </table>
        `;
        variantsContainer.innerHTML = tableHTML;
    };

    // --- DATA & UTILITY FUNCTIONS ---

    const getSchemaFromUI = () => {
        const schema = [];
        schemaContainer.querySelectorAll('.variant-option-group').forEach(group => {
            const label = group.querySelector('.option-label-input').value.trim();
            if (label) {
                const values = Array.from(group.querySelectorAll('.option-value-tag')).map(tag => tag.innerText.replace('×', '').trim());
                if (values.length > 0) {
                    schema.push({
                        id: label.toLowerCase().replace(/\s+/g, '_'),
                        label: label,
                        values: values
                    });
                }
            }
        });
        return schema;
    };

    const serializeData = () => {
        const schema = getSchemaFromUI();
        
        const variants = [];
        variantsContainer.querySelectorAll('tbody tr').forEach(row => {
            const attributes = JSON.parse(row.dataset.variantAttributes);
            const price = row.querySelector('.variant-price').value;
            const inventory_count = row.querySelector('.variant-inventory').value;
            
            if(price && inventory_count) {
                 variants.push({
                    attributes,
                    price: parseFloat(price),
                    inventory_count: parseInt(inventory_count, 10)
                });
            }
        });

        variantSchemaInput.value = schema.length > 0 ? JSON.stringify(schema, null, 2) : '';
        variantsJsonInput.value = variants.length > 0 ? JSON.stringify(variants, null, 2) : '';
    };

    const cartesian = (...args) => {
        const r = [], max = args.length - 1;
        function helper(arr, i) {
            for (let j = 0, l = args[i].length; j < l; j++) {
                let a = arr.slice(0);
                a.push(args[i][j]);
                if (i === max) r.push(a);
                else helper(a, i + 1);
            }
        }
        if (args.length > 0) helper([], 0);
        return r;
    };

    // --- START ---
    initializeUI();
});
