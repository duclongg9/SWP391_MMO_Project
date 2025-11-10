package service;

/**
 * Exception cho các lỗi nghiệp vụ (business rule violations).
 * Dùng để báo lỗi khi không đáp ứng các quy tắc nghiệp vụ như:
 * - Vượt quá giới hạn 5 shop
 * - Tên shop không hợp lệ
 * - Không có quyền thao tác trên shop
 */
public class BusinessException extends Exception {
	public BusinessException(String message) {
		super(message);
	}
}


