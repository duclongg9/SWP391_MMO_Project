package service;

import dao.inventory.InventoryDAO;

import java.util.Objects;

/**
 * High level façade for reserving and releasing inventory during the order
 * processing pipeline.
 */
public class InventoryService {

    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this(new InventoryDAO());
    }

    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = Objects.requireNonNull(inventoryDAO, "inventoryDAO");
    }

    public boolean reserve(int productId, int quantity) {
        try {
            return inventoryDAO.reserveStock(productId, quantity);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể khóa tồn kho sản phẩm.", ex);
        }
    }

    public void release(int productId, int quantity) {
        try {
            inventoryDAO.releaseStock(productId, quantity);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể hoàn trả tồn kho cho sản phẩm.", ex);
        }
    }
}
