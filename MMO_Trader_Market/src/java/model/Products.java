package model;

import java.math.BigDecimal;
import java.util.Date;

public class Products {

    private Integer id;
    private Integer shopId;
    private String productType;
    private String productSubtype;
    private String name;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private String primaryImageUrl;
    private String galleryJson;
    private Integer inventoryCount;
    private Integer soldCount;
    private String status;
    private String variantSchema;
    private String variantsJson;
    private Date createdAt;
    private Date updatedAt;

    public Products() {
    }  // no-args constructor cần cho JDBC/MBG

    public Products(Integer id, Integer shopId, String productType, String productSubtype,
            String name, String shortDescription, String description, BigDecimal price,
            String primaryImageUrl, String galleryJson, Integer inventoryCount, Integer soldCount,
            String status, String variantSchema, String variantsJson,
            Date createdAt, Date updatedAt) {
        this.id = id;
        this.shopId = shopId;
        this.productType = productType;
        this.productSubtype = productSubtype;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.price = price;
        this.primaryImageUrl = primaryImageUrl;
        this.galleryJson = galleryJson;
        this.inventoryCount = inventoryCount;
        this.soldCount = soldCount;
        this.status = status;
        this.variantSchema = variantSchema;
        this.variantsJson = variantsJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductSubtype() {
        return productSubtype;
    }

    public void setProductSubtype(String productSubtype) {
        this.productSubtype = productSubtype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public String getGalleryJson() {
        return galleryJson;
    }

    public void setGalleryJson(String galleryJson) {
        this.galleryJson = galleryJson;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVariantSchema() {
        return variantSchema;
    }

    public void setVariantSchema(String variantSchema) {
        this.variantSchema = variantSchema;
    }

    public String getVariantsJson() {
        return variantsJson;
    }

    public void setVariantsJson(String variantsJson) {
        this.variantsJson = variantsJson;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Products{"
                + "id=" + id
                + ", shopId=" + shopId
                + ", productType='" + productType + '\''
                + ", productSubtype='" + productSubtype + '\''
                + ", name='" + name + '\''
                + ", shortDescription='" + shortDescription + '\''
                + ", price=" + price
                + ", primaryImageUrl='" + primaryImageUrl + '\''
                + ", inventoryCount=" + inventoryCount
                + ", soldCount=" + soldCount
                + ", status='" + status + '\''
                + ", variantSchema='" + variantSchema + '\''
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
}
