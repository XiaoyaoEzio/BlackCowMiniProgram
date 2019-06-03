package com.superyc.heiniu.schedule.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class StatusCheckJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(StatusCheckJob.class);


    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDataMap dataMap = jobContext.getJobDetail().getJobDataMap();
        int clientId = dataMap.getInt("userId");
        int flag = dataMap.getInt("flag");
        String deviceSn = dataMap.getString("deviceId");
        String path = dataMap.getString("path");

        System.out.println("flag: " + flag);

        /*JsonResult state = new OperatorState().state(new Client(), deviceSn, path);

        if (state.status != 1) {
            // 设备不在线
            return;
        }*/

        // TODO 查询设备状态
        /*String jsonString = ChargeApi.operator(OperatorTypeEnum.State.getCommand(), deviceSn, path);
        String method;
        logger.debug(jsonString);
        try {
            method = HttpMethod.post(Config.Instance.device_Query_URL, jsonString);
        } catch (Exception e) {
            logger.error("查询设备状态异常" + e.getMessage(), e);
            return;
        }

        logger.debug("method: " + method);
        if (method == null || "".equals(method)) {
            logger.error("查询设备状态失败");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(method);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("解析json异常" + e.getMessage());
            return;
        }

        int status = rootNode.get("result").asInt();
        if (status != 1) {
            logger.error(rootNode.get("val").asText());
        }

        JsonNode valNode = rootNode.get("val");
        JsonNode pathStatesArray = valNode.get("path_state");
        int stateOfDevice = 0;

        if (!pathStatesArray.isArray()) {
            logger.error("json解析异常");
            return;
        }

        for (JsonNode pathNode : pathStatesArray) {
            if (path.equals(pathNode.get("path").asText())) {
                stateOfDevice = pathNode.get("state").asInt();
                break;
            }
        }

        if (stateOfDevice == 0) {
            logger.error("通道状态查询异常");
        } else if (stateOfDevice == 1) {
            // 通道打开，正常，进行持续状态查询
            QuartzJobUtils.setStatusCheck(clientId, deviceSn, path, 0);
            logger.info("端口输出正常  用户：" + clientId + " 设备："+ deviceSn +" 通道：" + path);
        } else if (stateOfDevice == 2) {
            *//*
             * 通道关闭
             *      flag == 0       持续查询状态，打开端口，执行一次断电确认查询
             *      flag == -1      断电确认查询，结束业务
             *      1 <= flag <= 2  初始查询前两次，打开端口，执行下一次初始查询
             *      flag == 3       第三次初始查询，结束业务
             *//*
            if (flag == -1 || flag == 3) {
                commandService.stopBySystem(clientId, deviceSn, path);
                logger.info("端口无输出，结束业务，用户：" + clientId + ", flag: " + flag);
            } else if (flag == 0) {
                openPath(deviceSn, path);
                logger.info("重新打开端口 用户：" + clientId + " flag: " + flag);
                QuartzJobUtils.setStatusCheck(clientId, deviceSn, path, -1);
            } else if (flag == 1 || flag == 2) {
                openPath(deviceSn, path);
                logger.info("重新打开端口 用户：" + clientId + " flag: " + flag);
                QuartzJobUtils.setStatusCheck(clientId, deviceSn, path, ++flag);
            }
        }*/

    }

    private void openPath(String deviceSn, String path) {
        /*String  jsonString = ChargeApi.operator(OperatorTypeEnum.Start.getCommand(), deviceSn, path);
        String method;
        try {
            method = HttpMethod.post(Config.Instance.device_Query_URL, jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送开启端口请求错误");
            logger.error(e.getMessage());
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        if (method != null && !"".equals(method)) {
            try {
                rootNode = objectMapper.readTree(method);
            } catch (IOException e) {
                logger.error("解析json错误");
                logger.error(e.getMessage());
                e.printStackTrace();
                return;
            }
            int status = rootNode.get("result").asInt();
            if (status != 1) {
                logger.error("重新打开端口错误：" +rootNode.get("val").asText());
            }
        }*/
    }
}
