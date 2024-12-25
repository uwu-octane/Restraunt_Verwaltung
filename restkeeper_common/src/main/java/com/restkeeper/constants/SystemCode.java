package com.restkeeper.constants;

//system constants
public class SystemCode {

    // 口味描述
    public final static String DISH_FLAVOR ="flavor";

    public final static String Email_ACCOUNT_QUEUE = "account_queue";
    //1 集团类型  2 门店类型
    public  final  static String  USER_TYPE_SHOP="1"; //集团用户类型
    public  final  static String  USER_TYPE_STORE_MANAGER="2"; //门店管理员类型
    public  final  static String  USER_TYPE_STAFF="3";  //普通员工

    //禁用
    public  final  static int  FORBIDDEN=0;
    //开启
    public  final  static int  ENABLED=1;

    public  final  static int DISH_TYPE_NORMAL =1; //普通菜品
    public  final  static int  DISH_TYPE_SETMEAL=2; //套餐

    // 挂账类型：1 个人 2 公司
    public final static int CREDIT_TYPE_USER = 1;
    public final static int CREDIT_TYPE_COMPANY = 2;

    public  final  static int  TABLE_STATUS_FREE=0; // 0空闲
    public  final  static int  TABLE_STATUS_LOCKED=1; // 1 锁定
    public  final  static int  TABLE_STATUS_OPEND=2; // 2 已开桌

    public  final  static int  ORDER_STATUS_UNPAIED=0;
    public  final  static int  ORDER_STATUS_PAIED=1;
    public  final  static int  ORDER_STATUS_PAING=2;

    public  final  static int  ORDER_SOURCE_STORE=0;
    public  final  static int  ORDER_SOURCE_OTHER=1;

    public  final  static int  SUM_OF_TURNOVER=1;
    public  final  static int  SUM_OF_ORDER=2;

    public  final  static  String DICTIONARY_REMARK="remark"; //字典表备注类型

    /***
     容器名称必须遵循严格的命名规范：
     •	只能包含小写字母、数字和短划线 (-)。
     •	不能包含大写字母或其他特殊字符。
     •	不能以短划线开头或结尾。
     ***/
    public final static String IMAGE_CONTAINER_NAME = "image-container";
    public final static String VIDEO_CONTAINER_NAME = "video-container";
    public final static String DOCUMENT_CONTAINER_NAME = "document-container";


    public final static String TENANT_CONDITION_SHOPID = "shopId";
    public final static String TENANT_CONDITION_STOREID = "storeId";
}
