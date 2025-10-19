package service.dto;

import model.Products;

import java.util.List;

public record ProductSearchResult(List<Products> items,
                                  long total,
                                  int page,
                                  int pageSize,
                                  int totalPages) {
}
