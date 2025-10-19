package dao;

import model.Products;

public interface ProductsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Products row);
    int insertSelective(Products row);
    Products selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Products row);
    int updateByPrimaryKeyWithBLOBs(Products row);
    int updateByPrimaryKey(Products row);
}