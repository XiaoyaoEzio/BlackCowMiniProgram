import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyc.heiniu.api.wx.WxApi;
import com.superyc.heiniu.bean.CommonResponse;
import com.superyc.heiniu.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/springMVC.xml",
        "classpath:spring/spring-mybatis.xml",
        "classpath:spring/spring-context.xml",
        "classpath:spring/spring-quartz.xml",
})
@WebAppConfiguration
public class MockTest {
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext wac;

    @Before
    public void setUp() {
        // 初始化 mock 对象
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testJacksonMap() throws IOException {
        String json = "{\"op\":\"st\",\"data\":{\"01\":\"1000\",\"02\":\"0500\", \"10\":\"0500\"}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        System.out.println(json);
        String op = jsonNode.get("op").asText();
        Map<String, String> status = null;
        if ("st".equals(op)) {
            String data = jsonNode.get("data").toString();
            System.out.println("data" + data);
            status = mapper.readValue(data, new TypeReference<Map<String, String>>() {
            });
        }
        if (status != null) {
            Iterator<Map.Entry<String, String>> iterator = status.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                System.out.println(next.getKey() + next.getValue());
            }
        }
    }

    @Test
    public void testResign() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            JsonProcessingException {
        CommonResponse response = WxApi.reSign("prepay_id");
        System.out.println(JsonUtils.getString(response));
    }

    @Test
    public void testGenerateSign() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            JsonProcessingException, JAXBException {

        /*WxApi.AfterPayCallbackParam param = new WxApi.AfterPayCallbackParam();
        param.setBankType("bank_Type111");
        param.setSign(WxApi.generateSign(param, WxApi.AfterPayCallbackParam.class));
        System.out.println(JsonUtils.getString(param));

        JAXBContext context = JAXBContext.newInstance(WxApi.AfterPayCallbackParam.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        Writer writer = new StringWriter();
        marshaller.marshal(param, writer);
        String xmlParam = writer.toString();
        System.out.println(xmlParam);*/
    }

    @Test
    public void testPayBody() {
        System.out.printf(WxApi.getPayBody());
    }

    @Test
    public void testRechargeRank() throws Exception {
        String result = mockMvc.perform(
                post("/user/recharge/rank")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        System.out.println(result);
    }

    @Test
    public void testCallback() throws Exception {
        String result = mockMvc.perform(
                post("/callback/pay")
                        .contentType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
                        .content("<xml>\n" +
                                "    <sign>7A682AA869314543A52803A467A23123</sign>\n" +
                                "    <bank_type>bank_Type111</bank_type>\n" +
                                "</xml>")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        System.out.println(result);
    }

    @Test
    public void testOrderList() throws Exception {
        String result = mockMvc.perform(
                post("/user/recharge/list")
                        .param("page", "0")
                        .param("size", "10")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        System.out.println(result);
    }

    @Test
    public void testDeviceInfo() throws Exception {
        String result = mockMvc.perform(
                get("/device/info")
                        .param("deviceNum", "407111840324319")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        System.out.println(result);
    }

    @Test
    public void testStartCharge() throws Exception {
        String result = mockMvc.perform(
                post("/device/handle")
                        .param("deviceId", "2")
                        .param("pathId", "1")
                        .param("rankId", "1")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        System.out.println(result);
    }
}
