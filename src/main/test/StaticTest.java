import com.fasterxml.jackson.core.JsonProcessingException;
import com.superyc.heiniu.bean.User;
import com.superyc.heiniu.utils.MqttUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class StaticTest {
    @Test
    public void testDeviceInfo() throws JsonProcessingException {
        // DeviceServiceImpl deviceService = new DeviceServiceImpl();
        // DeviceServiceImpl.StateResult result = deviceService.new StateResult();
        // result.setId(1);
        // result.setDeviceName("deviceName");
        // result.setDeviceNumber("num123");
        // result.setPricePerHour(12);
        // List<DeviceServiceImpl.PortInfo> ports = new ArrayList<>();
        // List<ChargeRank> chargeRanks = new ArrayList<>();
        //
        // int i = 0;
        // while (i < 10) {
        //     DeviceServiceImpl.PortInfo portInfo = deviceService.new PortInfo();
        //     portInfo.setId(i);
        //     portInfo.setStatus(1);
        //     ports.add(portInfo);
        //     ChargeRank chargeRank = new ChargeRank();
        //     chargeRank.setId(i);
        //     chargeRank.setTime(10);
        //     chargeRanks.add(chargeRank);
        //     i++;
        // }
        //
        // result.setPorts(ports);
        //
        // String string = JsonUtils.getString(result);
        // System.out.println(string);
        System.out.println(MqttUtils.getPassword());
    }

    @Test
    public void testDate() {
        System.out.println(new SimpleDateFormat("yyMMdd").format(new Date()));
    }

    @Test
    public void testString() {
        String[] baseNames = {"f", "s"};
        String jobName;
        String groupName;
        String triggerName;

        for (String baseName : baseNames) {
            jobName = "job-" + baseName;
            groupName = "group-" + baseName;
            triggerName = "trigger-" + baseName;
            System.out.println(jobName + groupName + triggerName);
        }
    }


    @Test
    public void testUser() {
        User user = null;
        testObj(user);
        System.out.println(user);
    }
    private void testObj(User user) {
        user = new User();
        user.setGroupId(1);
    }
}
