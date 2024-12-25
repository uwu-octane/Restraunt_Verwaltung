package com.restkeeper.email;

import com.restkeeper.utils.MessageType;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmailObject implements Serializable {
    //网络传输对象必须序列化 Network transport objects must be serialized
    private static final long serialVersionUID = -6986749569115643762L;


    private MessageType messageType;

    private String recipientEmail;

    private String subject;

    //private String messageBody;
    private String shopId;

    private String password;
}
