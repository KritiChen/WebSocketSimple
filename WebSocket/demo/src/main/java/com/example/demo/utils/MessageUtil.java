package com.example.demo.utils;

import com.alibaba.fastjson2.JSON;
import com.example.demo.pojo.MessageSend;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;

public class MessageUtil {

    public static String getMsgStr(Boolean system, String from, String msgStr) {
        if (StringUtils.isEmpty(msgStr)) {
            return "";
        }
        MessageSend msg = new MessageSend();
        msg.setSystem(system);
        msg.setFrom(from);
        msg.setMessage(msgStr);
        return JSON.toJSONString(msg);
    }
}
