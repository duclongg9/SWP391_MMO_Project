package dao;

import model.ProductCredentials;

public interface ProductCredentialsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(ProductCredentials row);
    int insertSelective(ProductCredentials row);
    ProductCredentials selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(ProductCredentials row);
    int updateByPrimaryKeyWithBLOBs(ProductCredentials row);
    int updateByPrimaryKey(ProductCredentials row);
}