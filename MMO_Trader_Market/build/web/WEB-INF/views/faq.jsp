<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="faq">
        <header class="faq__header">
            <h1>Câu hỏi thường gặp</h1>
        </header>
        <div class="faq__groups">
            <article class="faq__group">
                <h2>Người mua</h2>
                <details class="faq__item">
                    <summary>Làm sao để mua sản phẩm?</summary>
                    <p>Nạp tiền vào tài khoản của bạn, sau đó sử dụng số dư thanh toán cho các đơn hàng của bạn. Lưu ý quá trình nạp tiền và giao trả hàng hoàn toàn tự động, có thể bạn sẽ phải chờ 1 chút để cho hệ thống xử lý.</p>
                </details>
                <details class="faq__item">
                    <summary>Email không trùng lặp là gì?</summary>
                    <p>Vấn đề mua bán tài khoản trên các group tiềm ẩn rất nhiều nguy cơ, đặc biệt là người bán không uy tín, sau khi bán cho bạn sẽ đem bán cho những người khác nữa. Trên trang chúng tôi sẽ có cơ chế kiểm tra trùng lặp ở tất cả tài khoản bán ra (gian hàng có cam kết không trùng lặp), nhằm ngăn chặn nguy cơ đó, để đảm bảo tài khoản bạn mua chưa từng được bán cho ai khác trên hệ thống.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao để nạp tiền?</summary>
                    <p>Chọn phần &quot;Nạp tiền&quot; trên thanh menu, sau đó tiến hành theo hướng dẫn, chuyển khoản đúng nội dung thì tiền sẽ tự động vào tài khoản của bạn, bạn có thể nạp bất kỳ số tiền nào ( từ 1đ)</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi có thể trả hàng hay không?</summary>
                    <p>Sau khi một giao dịch thành công, chúng tôi sẽ giữ tiền của người bán 3 ngày, trong khoản thời gian đó, bạn cần kiểm tra kỹ chất lượng hàng hóa, nếu có gì đó không đúng, hãy vào mục lịch sử đơn hàng và chọn vào &quot;Khiếu nại&quot;, chúng tôi sẽ tiến hành giữ tiền đơn hàng đó và mở tranh chấp cho 2 bên.</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi nạp tiền bị lỗi?</summary>
                    <p>Đôi lúc do lỗi của hệ thống, lỗi bên ngân hàng hoặc lỗi chủ quan của người dùng nạp tiền không đúng cú pháp, thì số dư của bạn sẽ không được cập nhật, hãy yên tâm và liên hệ ngay với hỗ trợ để được cập nhật lại.</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi chuyển nhầm tiền?</summary>
                    <p>Chúng tôi đã gặp trường hợp này rất nhiều, vui lòng kiểm tra thật kỹ khi nạp tiền, nếu trường hợp bạn chuyển tiền nhầm, xin thông báo ngay với hỗ trợ trên fb, chúng tôi sẽ kiểm tra và trả lại tiền. Lưu ý: Tiền chỉ được trả lại cho đúng người chuyển đến, để tránh trường hợp scam ABC.</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi có cần thuê trung gian không?</summary>
                    <p>Chính sách của chúng tôi sẽ giữ tiền của người bán 3 ngày, như vậy taphoammo đã gần như trở thành trung gian cho các bạn yên tâm giao dịch. Lưu ý, chúng tôi chỉ hổ trợ với những đơn hàng được mua trực tiếp trên trang, vì vậy để đảm bảo quyền lợi của mình cũng như hạn chế tình trạng lừa đảo tràn lan trên mạng xã hội hiện nay các bạn hãy đưa ra lựa chọn đúng đắn của mình nhé.</p>
                </details>
            </article>
            <article class="faq__group">
                <h2>Người bán</h2>
                <details class="faq__item">
                    <summary>Làm sao để đăng ký bán hàng?</summary>
                    <p>Hãy đăng nhập trước, sau đó chọn vào &quot;Đăng ký bán hàng&quot;, nhập đầy đủ các thông tin để xác thực. Chúng mình sẽ duyệt qua để bạn có thể bán hàng trên trang.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao để tạo một gian hàng?</summary>
                    <p>Đầu tiên hãy đăng ký để trở thành người bán hàng, vào mục &quot;Quản lý gian hàng&quot; để tạo một gian hàng mới, lưu ý chọn đúng danh mục hàng hóa.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao để tối ưu gian hàng?</summary>
                    <p>Các bạn nên đầu tư vào hình ảnh gian hàng, các từ khóa tìm kiếm, tiêu đề, chú thích phải rõ ràng. Bước tiếp theo, để cho một kế hoạch lâu dài, hãy đảm bảo chất lượng hàng hóa . Chúng tôi đã tối ưu để cho gian hàng của bạn trở nên đẹp nhất khi được đưa lên các trang mạng xã hội, hãy tận dụng nó. Ngoài ra thứ tự xếp hạng gian hàng sẽ được cập nhật hàng tuần dựa vào kết quả bán hàng của bạn</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao gian hàng của tôi lên đầu trang?</summary>
                    <p>Tất cả sẽ dựa vào kết quả kinh doanh của bạn và đánh giá từ khách hàng. Xếp hạng sẽ được cập nhật mới vào mỗi tuần.</p>
                </details>
                <details class="faq__item">
                    <summary>Tiền tôi bán được sẽ giải quyết như thế nào?</summary>
                    <p>Trên mỗi đơn hàng thành công, tiền đó sẽ ở trạng thái &quot;Pending&quot; trong 3 ngày, nếu không có tranh chấp gì từ người mua, bạn có thể rút ngay số tiền đó về. Giới hạn rút các bạn vui lòng theo dõi trên trang.</p>
                </details>
                <details class="faq__item">
                    <summary>Mặt hàng nào bị cấm?</summary>
                    <p>Danh sách các mặt hàng không được bán trên trang sẽ liên tục được cập nhật trên trang, các bạn chú ý theo dõi. Chủ yếu các sản phẩm liên quan tới đánh cắp tài khoản người dùng ví dụ như các loại tài khoản ngân hàng, tài khoản fb, email có được nhờ các hành vi h.a.c.k, các hoạt động liên quan tới chính trị, tôn giáo...</p>
                </details>
                <details class="faq__item">
                    <summary>Chiết khấu cho đơn hàng như thế nào?</summary>
                    <p>Chúng tôi sẽ lấy chiết khấu trên mỗi đơn hàng bán được, mong các bạn hiểu cho mối quan hệ của 2 bên là cùng nhau phát triển. Ngoài ra, cơ chế reseller là một tùy chọn để các bạn có thể đẩy mạnh khả năng bán hàng, tất nhiên là bạn phải có chiết khấu cho bạn reseller đó.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao để rút tiền?</summary>
                    <p>Đối với số dư đã được duyệt, bạn có thể tùy chọn rút về tài khoản.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao tôi tin tưởng để hàng trên trang của bạn?</summary>
                    <p>Bạn có 1 lựa chọn khác là tự tạo server để hàng hóa riêng và cung cấp API cho bên mình, đội ngũ kỹ thuật sẽ liên hệ để 2 bên map được với nhau. Tuy nhiên, vì mục đích phát triển lâu dài các bạn có thể thử lựa chọn tin tưởng.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao tôi đổi hàng cho khách?</summary>
                    <p>Nếu đơn hàng của bạn có sản phẩm bị lỗi, hãy vào phần đơn hàng đã bán, đến mục đơn hàng cần bảo hành, chọn vào thao tác Bảo Hành -> nhập số lượng bảo hành. Hệ thống sẽ tự động tạo 1 mã giảm giá đúng với số lượng cần BH và gởi cho khách.</p>
                </details>
            </article>
            <article class="faq__group">
                <h2>Cộng tác viên (Reseller)</h2>
                <details class="faq__item">
                    <summary>Làm sao để trở thành reseller?</summary>
                    <p>Thành viên đã đăng ký thành công và cập nhật đầy đủ các thông tin cá nhân(Tên, trang mạng xã hội, tài khoản ngân hàng- dùng để rút tiền...) đều có thể trở thành reseller trên trang.</p>
                </details>
                <details class="faq__item">
                    <summary>Làm sao tôi có thể bán hàng được?</summary>
                    <p>Hãy xem danh sách các gian hàng, những gian hàng cho phép resell sẽ có nút bấm cho reseller, bạn hãy chủ động deal % thích hợp với chủ shop, đừng quên lời giới thiệu về bản thân nhé. Sau khi chủ shop duyệt qua và đồng ý, bạn sẽ nhận được link ref của riêng mình, hãy giới thiệu nó cho khách hàng của bạn, chiết khấu sẽ được chia tự động sau khi khách hàng hoàn tất mua 1 đơn hàng.</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi có thể rút tiền bán được không?</summary>
                    <p>Có. Cũng giống như chủ shop, tiền chiết khấu từ đơn hàng sẽ bị giữ 3 ngày trên trang, sau đó bạn hoàn toàn có khả năng yêu cầu rút số tiền đó về nếu như đơn hàng không có tranh chấp gì.</p>
                </details>
                <details class="faq__item">
                    <summary>Tôi có được thưởng thêm không?</summary>
                    <p>Có. Sau mỗi tháng, dựa vào doanh thu bán hàng của bạn, chúng tôi sẽ thưởng thêm % nếu doanh thu đó đạt các mốc.</p>
                </details>
            </article>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
