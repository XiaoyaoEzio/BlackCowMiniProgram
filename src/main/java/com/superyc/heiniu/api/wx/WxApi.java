package com.superyc.heiniu.api.wx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.bean.ProxyCilentBankInfo;
import com.superyc.heiniu.bean.TransferOrder;
import com.superyc.heiniu.enums.ResponseCodeEnum;
import com.superyc.heiniu.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * 与微信服务器交互的api接口
 */
@Component
@SuppressWarnings("unused")
public class WxApi {
    private static Logger LOG = LoggerFactory.getLogger(WxApi.class);

    private static String code2sessionUrl;
    private static String accessTokenUrl;
    private static String transferToBankUrl;
    private static String appIdKey;
    private static String appIdValue;
    private static String secretKey;
    private static String secretValue;
    private static String codeKey;
    private static String grantTypeKey;
    private static String grantTypeValue4Code;
    private static String grantTypeValue4Access;
    private static String unifiedOrderUrl;
    private static String mchId;
    private static String callbackUrlForPay;
    private static String tradeType;
    private static String payKey;
    private static String payBody;
    private static String pemPath;

    private static final String FAIL = "FAIL";
    public static final String SUCCESS = "SUCCESS";

    private static final AccessTokenResult ACCESS_TOKEN_RESULT = new AccessTokenResult();

    /**
     * 使用code从微信后台换取用户相关信息
     * 文档：https://developers.weixin.qq.com/miniprogram/dev/api/code2Session.html
     *
     * @param code 小程序方法 wx.login() 返回的code
     * @return openid，session_key，unionid等参数组成的对象
     */
    public static WxCode2SessionResult code2Session(String code) throws IOException {

        String authUrl = baseUrl(code2sessionUrl, grantTypeValue4Code).append("&")
                .append(codeKey).append("=").append(code)
                .toString();

        String response = HttpUtils.get(authUrl);
        LOG.info("wx code2session response: {}", response);
        return JsonUtils.parseString(response, WxCode2SessionResult.class);
    }

    /**
     * 统一下单
     * 文档：https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_1&index=1
     */
    public static CommonResponse unifiedOrder(String orderNumber, String openId, String payment, String userIp) throws IOException {
        UnifiedOrderParam param = new UnifiedOrderParam();
        param.setAppId(appIdValue);
        param.setMchId(mchId);
        param.setNonceStr(RandomUtils.getUUID());
        param.setBody(payBody);
        param.setOutTradeNo(orderNumber);
        param.setTotalFee(payment);
        param.setSpbillCreateIp(userIp);
        param.setNotifyUrl(callbackUrlForPay);
        param.setTradeType(tradeType);
        param.setOpenId(openId);
        param.setSign(generateSign(param, UnifiedOrderParam.class, payKey));

        // 生成参数xml
        /*JAXBContext context = JAXBContext.newInstance(UnifiedOrderParam.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        Writer writer = new StringWriter();
        marshaller.marshal(param, writer);*/
        String xmlParam = XmlUtils.getXmlStr(param, UnifiedOrderParam.class);

        String response = HttpUtils.postXml(unifiedOrderUrl, xmlParam);

        // 解析响应参数
        //context = JAXBContext.newInstance(UnifiedOrderResult.class);
        //Unmarshaller unmarshaller = context.createUnmarshaller();
        //UnifiedOrderResult result = (UnifiedOrderResult) unmarshaller.unmarshal(new StringReader(response));
        UnifiedOrderResult result = XmlUtils.parseXml(response, UnifiedOrderResult.class);
        if (result.getReturnCode().equals(FAIL)) {
            LOG.error("order number: {}; result: {}", orderNumber, result.getReturnMsg());
            return CommonResponse.failure(result.getReturnMsg());
        }

        if (result.getResultCode().equals(FAIL)) {
            String errMsg = result.getErrCode() + "-" + result.getErrCodeDes();
            LOG.error("order number: {}; result: {}", orderNumber, errMsg);
            return CommonResponse.failure(errMsg);
        }

        // return_code 和 result_code 都是 SUCCESS 时
        boolean checkSign = checkSign(result, UnifiedOrderResult.class);
        if (!checkSign) {
            LOG.error("签名校验失败，orderNumber：{}", orderNumber);
        }

        return CommonResponse.success(result.getPrepayId());
    }

    /**
     * 再次签名，生成前端响应
     * 文档：https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=7_7&index=3
     */
    public static CommonResponse reSign(String prePayId) {
        ReSignParam param = new ReSignParam();
        param.setAppId(appIdValue);
        param.setNonceStr(RandomUtils.getUUID());
        param.setPackages("prepay_id=" + prePayId);
        param.setSignType("MD5");
        param.setTimeStamp(Long.toString(System.currentTimeMillis() / 1000));
        param.setPaySign(generateSign(param, ReSignParam.class, payKey));

        return CommonResponse.success(param);
    }

    /**
     * 获取access_token, 用于与微信后端交互
     * 文档：https://developers.weixin.qq.com/miniprogram/dev/api/getAccessToken.html
     * errCode 类型
     * -1	    系统繁忙，此时请开发者稍候再试
     * 0	    请求成功
     * 40001	AppSecret 错误或者 AppSecret 不属于这个小程序，请开发者确认 AppSecret 的正确性
     * 40002	请确保 grant_type 字段值为 client_credential
     * 40013	不合法的 AppID，请开发者检查 AppID 的正确性，避免异常字符，注意大小写
     */
    private static String getAccessToken() throws IOException, InterruptedException {
        long expiredTime = ACCESS_TOKEN_RESULT.getExpiredTime();
        if (expiredTime > System.currentTimeMillis()) {
            return ACCESS_TOKEN_RESULT.getAccessToken();
        }

        String url = baseUrl(accessTokenUrl, grantTypeValue4Access).toString();
        String response = HttpUtils.get(url);
        LOG.info("access_token response: {}", response);
        AccessTokenResult result = JsonUtils.parseString(response, AccessTokenResult.class);
        int errCode = result.getErrCode();
        if (errCode == 0) {
            ACCESS_TOKEN_RESULT.update(result);
            return ACCESS_TOKEN_RESULT.getAccessToken();
        } else if (errCode == -1) {
            sleep(500);
            return getAccessToken();
        } else {
            LOG.error("access_token error: {}", response);
            return null;
        }
    }

    /**
     * 生成签名，熊猫云的签名方法与微信的签名方法一致
     */
    public static <T> String generateSign(Object param, Class<T> clazz, String secret) {
        // 获取所有字段
        Field[] fieldsAsArray = clazz.getDeclaredFields();

        // 处理父类字段
        List<Field> fieldList = new ArrayList<>(Arrays.asList(fieldsAsArray));
        Class<? super T> superclass = clazz.getSuperclass();
        if (superclass == WxPayBaseResult.class) {
            fieldList.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
        Field[] fields = fieldList.toArray(new Field[0]);

        // 将所有非空字段排序
        SortedMap<String, String> map = new TreeMap<>();

        for (Field field : fields) {
            String fieldName = field.getName();

            // 处理内部类的特殊字段
            if ("this$0".equals(fieldName)) {
                continue;
            }

            String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            Method getMethod = null;
            try {
                getMethod = clazz.getMethod("get" + methodName);
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
            Assert.notNull(getMethod);

            Method setMethod = null;
            try {
                setMethod = clazz.getMethod("set" + methodName, String.class);
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
            Assert.notNull(setMethod);

            String value = null;
            try {
                value = (String) getMethod.invoke(param);
            } catch (InvocationTargetException | IllegalAccessException e) {
                LOG.error(e.getMessage());
                e.printStackTrace();
            }

            if (value != null) {
                String mapKey = fieldName;
                // 处理 JsonProperty 注解的属性名
                boolean annotationOnField = field.isAnnotationPresent(JsonProperty.class);
                if (annotationOnField) {
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    mapKey = jsonProperty.value();
                }

                // 处理 XmlElement 注解的属性名
                boolean annotationOnMethod = setMethod.isAnnotationPresent(XmlElement.class);
                if (annotationOnMethod) {
                    XmlElement xmlElement = setMethod.getAnnotation(XmlElement.class);
                    mapKey = xmlElement.name();
                }

                map.put(mapKey, value);
            }
        }

        // 拼接所有字段和key
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("key=").append(secret);
        LOG.debug("param string with key: {}", sb.toString());

        return DigestUtils.md5DigestAsHex(sb.toString().getBytes()).toUpperCase();
    }

    /**
     * 校验签名
     */
    public static <T> boolean checkSign(Signable param, Class<T> clazz) {
        String resultSign = param.getSign();
        if (StringUtils.isBlank(resultSign)) {
            LOG.error("sign为空");
            return false;
        }

        param.setSign(null);
        String generateSign = generateSign(param, clazz, payKey);
        if (!resultSign.equals(generateSign)) {
            LOG.error("校验sign失败");
            return false;
        }

        return true;
    }

    /**
     * 向代理商银行卡转账
     * 文档：https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=24_2
     */
    public static WxApi.TransferResult transferToBank(ProxyCilentBankInfo bankInfo, TransferOrder order) throws IOException {
        TransferParam param = new TransferParam();
        param.setAmount(order.getAmount());
        param.setBankCode(bankInfo.getBankCode());
        param.setDesc("用户充值");
        param.setEncBankNo(RSAUtils.encrypt(bankInfo.getCardNum(), pemPath));
        param.setEncTrueName(RSAUtils.encrypt(bankInfo.getPayeeName(), pemPath));
        param.setMchId(mchId);
        param.setNoticeStr(RandomUtils.getUUID());
        param.setPartnerTradeNo(order.getOrderNumber());
        param.setSign(generateSign(param, TransferParam.class, payKey));
        String xmlParam = XmlUtils.getXmlStr(param, TransferParam.class);

        String response = HttpUtils.postXml(transferToBankUrl, xmlParam);

        return XmlUtils.parseXml(response, TransferResult.class);
    }

    private static StringBuilder baseUrl(String uri, String grantType) {
        return new StringBuilder()
                .append(uri).append("?")
                .append(appIdKey).append("=").append(appIdValue).append("&")
                .append(secretKey).append("=").append(secretValue).append("&")
                .append(grantTypeKey).append("=").append(grantType);
    }

    /**
     * 用于接收微信后台传来的参数
     */
    @SuppressWarnings("unused")
    public static class WxCode2SessionResult {
        private String openid;
        private String session_key;
        private String unionid;
        private Integer errcode;
        private String errmsg;

        public WxCode2SessionResult() {
        }

        public WxCode2SessionResult(String openid, String session_key, String unionid) {
            this.openid = openid;
            this.session_key = session_key;
            this.unionid = unionid;
        }

        public WxCode2SessionResult(String openid, String session_key, String unionid, Integer errcode, String errmsg) {
            this.openid = openid;
            this.session_key = session_key;
            this.unionid = unionid;
            this.errcode = errcode;
            this.errmsg = errmsg;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getSession_key() {
            return session_key;
        }

        public void setSession_key(String session_key) {
            this.session_key = session_key;
        }

        public String getUnionid() {
            return unionid;
        }

        public void setUnionid(String unionid) {
            this.unionid = unionid;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }

    /**
     * 微信返回的errcode有效值
     */
    public enum ErrcodeEnum {
        BUSYNESS(-1),
        SUCCESS(0),
        INVALID_CODE(40029),
        FREQUENCY_LIMITATION(45011);
        private int code;

        ErrcodeEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        /**
         * 通过微信返回的错误值，获取自定义的错误值
         */
        public static ResponseCodeEnum getResponseCodeEnum(int errCode) {
            if (errCode == BUSYNESS.getCode()) {
                return ResponseCodeEnum.BUSYNESS;
            }

            if (errCode == INVALID_CODE.getCode()) {
                return ResponseCodeEnum.INVALID_CODE;
            }

            if (errCode == FREQUENCY_LIMITATION.getCode()) {
                return ResponseCodeEnum.FREQUENCY_LIMITATION;
            }

            return null;
        }
    }

    /**
     * 再次签名参数
     */
    @SuppressWarnings("SameParameterValue")
    public static class ReSignParam {
        @JsonProperty(value = "appid")
        private String appId;
        // 秒值
        private String timeStamp;
        private String nonceStr;
        @JsonProperty(value = "package")
        private String packages;
        private String signType;
        private String paySign;

        public String getAppId() {
            return appId;
        }

        void setAppId(String appId) {
            this.appId = appId;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getPackages() {
            return packages;
        }

        void setPackages(String packages) {
            this.packages = packages;
        }

        public String getSignType() {
            return signType;
        }

        void setSignType(String signType) {
            this.signType = signType;
        }

        public String getPaySign() {
            return paySign;
        }

        void setPaySign(String paySign) {
            this.paySign = paySign;
        }
    }

    /**
     * 统一下单请求参数
     */
    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    public static class UnifiedOrderParam {
        private String appId;
        private String mchId;
        private String deviceInfo;
        private String nonceStr;
        private String sign;
        private String signType;
        private String body;
        private String detail;
        private String attach;
        private String outTradeNo;
        private String feeType;
        private String totalFee;
        private String spbillCreateIp;
        private String timeStart;
        private String timeExpire;
        private String goodsTag;
        private String notifyUrl;
        private String tradeType;
        private String productId;
        private String limitPay;
        private String openId;
        private String receipt;
        private String sceneInfo;

        public String getAppId() {
            return appId;
        }

        @XmlElement(name = "appid")
        void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        @XmlElement(name = "mch_id")
        void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        @XmlElement(name = "device_info")
        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        @XmlElement(name = "nonceStr")
        void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getSign() {
            return sign;
        }

        @XmlElement(name = "sign")
        void setSign(String sign) {
            this.sign = sign;
        }

        public String getSignType() {
            return signType;
        }

        @XmlElement(name = "sign_type")
        public void setSignType(String signType) {
            this.signType = signType;
        }

        public String getBody() {
            return body;
        }

        @XmlElement(name = "body")
        void setBody(String body) {
            this.body = body;
        }

        public String getDetail() {
            return detail;
        }

        @XmlElement(name = "detail")
        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getAttach() {
            return attach;
        }

        @XmlElement(name = "attach")
        public void setAttach(String attach) {
            this.attach = attach;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        @XmlElement(name = "out_trade_no")
        void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public String getFeeType() {
            return feeType;
        }

        @XmlElement(name = "fee_type")
        public void setFeeType(String feeType) {
            this.feeType = feeType;
        }

        public String getTotalFee() {
            return totalFee;
        }

        @XmlElement(name = "total_fee")
        void setTotalFee(String totalFee) {
            this.totalFee = totalFee;
        }

        public String getSpbillCreateIp() {
            return spbillCreateIp;
        }

        @XmlElement(name = "spbill_create_ip")
        void setSpbillCreateIp(String spbillCreateIp) {
            this.spbillCreateIp = spbillCreateIp;
        }

        public String getTimeStart() {
            return timeStart;
        }

        @XmlElement(name = "time_start")
        public void setTimeStart(String timeStart) {
            this.timeStart = timeStart;
        }

        public String getTimeExpire() {
            return timeExpire;
        }

        @XmlElement(name = "time_expire")
        public void setTimeExpire(String timeExpire) {
            this.timeExpire = timeExpire;
        }

        public String getGoodsTag() {
            return goodsTag;
        }

        @XmlElement(name = "goods_tag")
        public void setGoodsTag(String goodsTag) {
            this.goodsTag = goodsTag;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        @XmlElement(name = "notify_url")
        void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public String getTradeType() {
            return tradeType;
        }

        @XmlElement(name = "trade_type")
        void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getProductId() {
            return productId;
        }

        @XmlElement(name = "product_id")
        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getLimitPay() {
            return limitPay;
        }

        @XmlElement(name = "limit_pay")
        public void setLimitPay(String limitPay) {
            this.limitPay = limitPay;
        }

        public String getOpenId() {
            return openId;
        }

        @XmlElement(name = "openid")
        void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getReceipt() {
            return receipt;
        }

        @XmlElement(name = "receipt")
        public void setReceipt(String receipt) {
            this.receipt = receipt;
        }

        public String getSceneInfo() {
            return sceneInfo;
        }

        @XmlElement(name = "scene_info")
        public void setSceneInfo(String sceneInfo) {
            this.sceneInfo = sceneInfo;
        }
    }

    /**
     * 微信支付的基本返回参数
     */
    private static class WxPayBaseResult implements Serializable, Signable {
        private String returnCode;
        private String returnMsg;
        private String appId;
        private String mchId;
        private String deviceInfo;
        private String nonceStr;
        private String openId;
        private String sign;
        private String resultCode;
        private String errCode;
        private String errCodeDes;
        private String tradeType;

        public String getDeviceInfo() {
            return deviceInfo;
        }

        @XmlElement(name = "device_info")
        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        String getErrCode() {
            return errCode;
        }

        @XmlElement(name = "err_code")
        public void setErrCode(String errCode) {
            this.errCode = errCode;
        }

        String getErrCodeDes() {
            return errCodeDes;
        }

        @XmlElement(name = "err_code_des")
        public void setErrCodeDes(String errCodeDes) {
            this.errCodeDes = errCodeDes;
        }

        String getReturnCode() {
            return returnCode;
        }

        @XmlElement(name = "return_code")
        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        String getReturnMsg() {
            return returnMsg;
        }

        @XmlElement(name = "return_msg")
        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }

        public String getAppId() {
            return appId;
        }

        @XmlElement(name = "appid")
        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        @XmlElement(name = "mch_id")
        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        @XmlElement(name = "nonceStr")
        public void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getOpenId() {
            return openId;
        }

        @XmlElement(name = "openid")
        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getSign() {
            return sign;
        }

        @XmlElement(name = "sign")
        public void setSign(String sign) {
            this.sign = sign;
        }

        String getResultCode() {
            return resultCode;
        }

        @XmlElement(name = "result_code")
        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getTradeType() {
            return tradeType;
        }

        @XmlElement(name = "trade_type")
        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }
    }

    /**
     * 统一下单返回参数
     */
    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    public static class UnifiedOrderResult extends WxPayBaseResult {
        private String prepayId;
        private String codeUrl;

        public String getCodeUrl() {
            return codeUrl;
        }

        @XmlElement(name = "code_url")
        public void setCodeUrl(String codeUrl) {
            this.codeUrl = codeUrl;
        }

        String getPrepayId() {
            return prepayId;
        }

        @XmlElement(name = "prepay_id")
        public void setPrepayId(String prepayId) {
            this.prepayId = prepayId;
        }
    }

    /**
     * 支付完成后回调参数
     */
    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    public static class AfterPayCallbackParam extends WxPayBaseResult {
        private String signType;
        private String isSubscribe;
        private String bankType;
        private String totalFee;
        private String cashFee;
        private String transactionId;
        private String outTradeNo;
        private String timeEnd;

        public String getSignType() {
            return signType;
        }

        @XmlElement(name = "sign_type")
        public void setSignType(String signType) {
            this.signType = signType;
        }

        public String getIsSubscribe() {
            return isSubscribe;
        }

        @XmlElement(name = "is_subscribe")
        public void setIsSubscribe(String isSubscribe) {
            this.isSubscribe = isSubscribe;
        }

        public String getBankType() {
            return bankType;
        }

        @XmlElement(name = "bank_type")
        public void setBankType(String bankType) {
            this.bankType = bankType;
        }

        public String getTotalFee() {
            return totalFee;
        }

        @XmlElement(name = "total_fee")
        public void setTotalFee(String totalFee) {
            this.totalFee = totalFee;
        }

        public String getCashFee() {
            return cashFee;
        }

        @XmlElement(name = "cash_fee")
        public void setCashFee(String cashFee) {
            this.cashFee = cashFee;
        }

        public String getTransactionId() {
            return transactionId;
        }

        @XmlElement(name = "transaction_id")
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        @XmlElement(name = "is_subscribe")
        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public String getTimeEnd() {
            return timeEnd;
        }

        @XmlElement(name = "time_end")
        public void setTimeEnd(String timeEnd) {
            this.timeEnd = timeEnd;
        }
    }

    /**
     * 处理完回调后，返回给微信后端的参数
     */
    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    public static class AfterPayCallbackResponse {
        private String returnCode;
        private String returnMsg;

        public String getReturnCode() {
            return returnCode;
        }

        @XmlElement(name = "return_code")
        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMsg() {
            return returnMsg;
        }

        @XmlElement(name = "return_msg")
        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }
    }

    /**
     * 请求access_token后获取的参数
     */
    @SuppressWarnings("WeakerAccess")
    public static class AccessTokenResult {
        @JsonProperty(value = "access_token")
        private String accessToken;
        @JsonProperty(value = "expires_in")
        private int expiresIn;
        @JsonProperty(value = "errcode")
        private int errCode;
        @JsonProperty(value = "errmsg")
        private String errMsg;

        private long expiredTime;

        public void update(AccessTokenResult result) {
            this.accessToken = result.getAccessToken();
            this.expiresIn = result.getExpiresIn();
            this.errCode = result.getErrCode();
            this.errMsg = result.getErrMsg();
            this.expiredTime = System.currentTimeMillis() + this.expiresIn * 1000;
        }


        public long getExpiredTime() {
            return expiredTime;
        }

        public void setExpiredTime(long expiredTime) {
            this.expiredTime = expiredTime;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public int getErrCode() {
            return errCode;
        }

        public void setErrCode(int errCode) {
            this.errCode = errCode;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }
    }

    /**
     * 转账给代理商银行卡使用的参数
     */
    @SuppressWarnings("WeakerAccess")
    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private static class TransferParam {
        private int amount;
        private String bankCode;
        private String desc;
        private String encBankNo;
        private String encTrueName;
        private String mchId;
        private String noticeStr;
        private String partnerTradeNo;
        private String sign;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getBankCode() {
            return bankCode;
        }

        @XmlElement(name = "bank_code")
        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getEncBankNo() {
            return encBankNo;
        }

        @XmlElement(name = "enc_bank_no")
        public void setEncBankNo(String encBankNo) {
            this.encBankNo = encBankNo;
        }

        public String getEncTrueName() {
            return encTrueName;
        }

        @XmlElement(name = "enc_true_name")
        public void setEncTrueName(String encTrueName) {
            this.encTrueName = encTrueName;
        }

        public String getMchId() {
            return mchId;
        }

        @XmlElement(name = "mch_id")
        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getNoticeStr() {
            return noticeStr;
        }

        @XmlElement(name = "notice_str")
        public void setNoticeStr(String noticeStr) {
            this.noticeStr = noticeStr;
        }

        public String getPartnerTradeNo() {
            return partnerTradeNo;
        }

        @XmlElement(name = "partner_trade_no")
        public void setPartnerTradeNo(String partnerTradeNo) {
            this.partnerTradeNo = partnerTradeNo;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }
    }

    @XmlRootElement(name = "xml")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    public static class TransferResult {
        private String returnCode;
        private String returnMsg;
        private String resultCode;
        private String errCode;
        private String errCodeDes;
        private String mchId;
        private String partnerTradeNo;
        private int amount;
        private String nonceStr;
        private String sign;
        private String paymentNo;
        private int cmmsAmt;

        public String getReturnCode() {
            return returnCode;
        }

        @XmlElement(name = "return_code")
        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMsg() {
            return returnMsg;
        }

        @XmlElement(name = "return_msg")
        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }

        public String getResultCode() {
            return resultCode;
        }

        @XmlElement(name = "result_code")
        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getErrCode() {
            return errCode;
        }

        @XmlElement(name = "err_code")
        public void setErrCode(String errCode) {
            this.errCode = errCode;
        }

        public String getErrCodeDes() {
            return errCodeDes;
        }

        @XmlElement(name = "err_code_des")
        public void setErrCodeDes(String errCodeDes) {
            this.errCodeDes = errCodeDes;
        }

        public String getMchId() {
            return mchId;
        }

        @XmlElement(name = "mch_id")
        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getPartnerTradeNo() {
            return partnerTradeNo;
        }

        @XmlElement(name = "partner_trade_no")
        public void setPartnerTradeNo(String partnerTradeNo) {
            this.partnerTradeNo = partnerTradeNo;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        @XmlElement(name = "nonce_str")
        public void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getPaymentNo() {
            return paymentNo;
        }

        @XmlElement(name = "payment_no")
        public void setPaymentNo(String paymentNo) {
            this.paymentNo = paymentNo;
        }

        public int getCmmsAmt() {
            return cmmsAmt;
        }

        @XmlElement(name = "cmms_amt")
        public void setCmmsAmt(int cmmsAmt) {
            this.cmmsAmt = cmmsAmt;
        }

        @Override
        public String toString() {
            return "TransferResult{" +
                    "returnCode='" + returnCode + '\'' +
                    ", returnMsg='" + returnMsg + '\'' +
                    ", resultCode='" + resultCode + '\'' +
                    ", errCode='" + errCode + '\'' +
                    ", errCodeDes='" + errCodeDes + '\'' +
                    ", mchId='" + mchId + '\'' +
                    ", partnerTradeNo='" + partnerTradeNo + '\'' +
                    ", amount=" + amount +
                    ", nonceStr='" + nonceStr + '\'' +
                    ", sign='" + sign + '\'' +
                    ", paymentNo='" + paymentNo + '\'' +
                    ", cmmsAmt=" + cmmsAmt +
                    '}';
        }
    }

    public interface Signable {
        String getSign();

        void setSign(String sign);
    }


    @Value("#{wx['transferToBankUrl']}")
    public void setTransferToBankUrl(String transferToBankUrl) {
        WxApi.transferToBankUrl = transferToBankUrl;
    }

    @Value("#{wx['pemPath']}")
    public void setPemPath(String pemPath) {
        WxApi.pemPath = pemPath;
    }

    @Value("#{wx['payBody']}")
    public void setPayBody(String payBody) {
        WxApi.payBody = payBody;
    }

    public static String getPayBody() {
        return payBody;
    }

    @Value("#{wx['code2sessionUrl']}")
    public void setCode2sessionUrl(String code2sessionUrl) {
        WxApi.code2sessionUrl = code2sessionUrl;
    }

    @Value("#{wx['accessTokenUrl']}")
    public void setAccessTokenUrl(String accessTokenUrl) {
        WxApi.accessTokenUrl = accessTokenUrl;
    }

    @Value("#{wx['appidKey']}")
    public void setAppIdKey(String appIdKey) {
        WxApi.appIdKey = appIdKey;
    }

    @Value("#{wx['secretKey']}")
    public void setSecretKey(String secretKey) {
        WxApi.secretKey = secretKey;
    }

    @Value("#{wx['grantTypeKey']}")
    public void setGrantTypeKey(String grantTypeKey) {
        WxApi.grantTypeKey = grantTypeKey;
    }

    @Value("#{wx['appidValue']}")
    public void setAppIdValue(String appIdValue) {
        WxApi.appIdValue = appIdValue;
    }

    @Value("#{wx['secretValue']}")
    public void setSecretValue(String secretValue) {
        WxApi.secretValue = secretValue;
    }

    @Value("#{wx['grantTypeValue4Code']}")
    public void setGrantTypeValue4Code(String grantTypeValue4Code) {
        WxApi.grantTypeValue4Code = grantTypeValue4Code;
    }

    @Value("#{wx['grantTypeValue4Access']}")
    public void setGrantTypeValue4Access(String grantTypeValue4Access) {
        WxApi.grantTypeValue4Access = grantTypeValue4Access;
    }

    @Value("#{wx['codeKey']}")
    public void setCodeKey(String codeKey) {
        WxApi.codeKey = codeKey;
    }

    @Value("#{wx['unifiedOrderUrl']}")
    public void setUnifiedOrderUrl(String unifiedOrderUrl) {
        WxApi.unifiedOrderUrl = unifiedOrderUrl;
    }

    @Value("#{wx['mchId']}")
    public void setMchId(String mchId) {
        WxApi.mchId = mchId;
    }

    @Value("#{wx['callbackUrlForPay']}")
    public void setCallbackUrlForPay(String callbackUrlForPay) {
        WxApi.callbackUrlForPay = callbackUrlForPay;
    }

    @Value("#{wx['tradeType']}")
    public void setTradeType(String tradeType) {
        WxApi.tradeType = tradeType;
    }

    @Value("#{wx['payKey']}")
    public void setPayKey(String payKey) {
        WxApi.payKey = payKey;
    }
}
