package com.superyc.heiniu.controller;

import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.exception.RegisterException;
import com.superyc.heiniu.mapper.UserMapper;
import com.superyc.heiniu.utils.HttpUtils;
import com.superyc.heiniu.utils.JsonUtils;
import com.superyc.heiniu.utils.OrderNumberUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;

/**
 *
 */
@Controller
@ResponseBody
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public WxApi.WxCode2SessionResult test() {
        return new WxApi.WxCode2SessionResult("openid", "sessionKey", "unionid", 1, "errmsg");
    }

    @RequestMapping("/ex")
    public String testException() throws Exception {
        throw new RegisterException();
    }

    @RequestMapping("/order")
    public String testOrderNumber() throws ParseException {
        String rechargeOrderNumber = OrderNumberUtils.getRechargeOrderNumber();
        System.out.println(rechargeOrderNumber);
        int orderType = OrderNumberUtils.getOrderType(rechargeOrderNumber);
        System.out.println(orderType);
        Date orderTime = OrderNumberUtils.getOrderTime(rechargeOrderNumber);
        System.out.println(orderTime);
        return "1";
    }

    @RequestMapping(value = "/xmlReceive", method = RequestMethod.POST)
    public CommonResponse testXmlReceive(@RequestParam("xml") String xml) throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            JAXBException, IOException {
        /*JAXBContext context = JAXBContext.newInstance(WxApi.UnifiedOrderParam.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        WxApi.UnifiedOrderParam result = (WxApi.UnifiedOrderParam) unmarshaller.unmarshal(new StringReader(xml));

        String sign = result.getSign();
        result.setSign(null);
        String generateSign = WxApi.generateSign(result, WxApi.UnifiedOrderParam.class);
        if (sign.equals(generateSign)) {
            return CommonResponse.success(result);
        } else {
            return CommonResponse.failure(ResponseCodeEnum.TEST);
        }*/
        return null;
    }

    @RequestMapping("/xmlRequest")
    public CommonResponse textXmlRequest() throws InvocationTargetException, NoSuchMethodException, JAXBException,
            IOException, IllegalAccessException {
        return WxApi.unifiedOrder(OrderNumberUtils.getRechargeOrderNumber(), "123", "20", "1234567");
    }

    @RequestMapping("/user")
    public CommonResponse testuser() {
        String openId = userMapper.getMiniOpenIdByUserId(1);
        return CommonResponse.success(openId);
    }

    @RequestMapping("/post")
    public CommonResponse testPost() throws IOException {
        String result = HttpUtils.postForm("http://localhost:8080/test/prePost", "param", "测试数据");
        CommonResponse response = JsonUtils.parseString(result, CommonResponse.class);
        return CommonResponse.success(response.getData());
    }

    @RequestMapping(value = "/prePost", method = RequestMethod.POST)
    public CommonResponse prePost(@RequestParam("param") String param) {
        return CommonResponse.success(param);
    }

    @RequestMapping("/mqtt")
    public void testMqtt() throws InterruptedException, MqttException, IOException {
        /*Map<String, String> stateMap = DeviceApi.getDeviceState("407111840324319");
        for (Map.Entry<String, String> entry : stateMap.entrySet()) {
            System.out.println("Port:" + entry.getKey() + " Status:" + entry.getValue());
        }*/
    }
}
