package com.atguigu.gmall.order.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
public class OrderSubmitVo {
    @NotEmpty(message = "收获人姓名必须填写")
    private String consignee;
    @Pattern(regexp = "^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[235-8]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|66\\d{2})\\d{6}$",
            message = "必须填写合法的手机号码")
    private String consigneeTel;
    @NotEmpty(message = "收货地址必须填写")
    private String deliveryAddress;
    private String orderComment;
    private List<OrderDetailListDTO> orderDetailList;

    @NoArgsConstructor
    @Data
    public static class OrderDetailListDTO {
        @NotNull
        private Long skuId;
        private String imgUrl;
        private String skuName;
        private BigDecimal orderPrice;
        private Integer skuNum;
        private String hasStock;
    }
}
