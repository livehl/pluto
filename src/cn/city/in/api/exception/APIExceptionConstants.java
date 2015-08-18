package cn.city.in.api.exception;

public class APIExceptionConstants {
	/** 参数错误1 */
	public static final int PROPERTY_NOT_MATCH_CODE = 1001;
	public static final int PROPERTY_IS_NULL_CODE = 1002;
	public static final int PROPERTY_NOT_ENOUGH_CODE = 1003;
	public static final int PROPERTY_NOT_VALIDATE_CODE = 1004;
	public static final int PROPERTY_TOO_LONG = 1005;
	public static final int PROPERTY_TOO_SHORT = 1006;
	public static final int UID_TX_OUT_OF_RANGE = 1007;
	//
	/** 数据库错误2 */
	public static final int QUERY_DATABASE_ERROR_CODE = 2001;
	public static final int DATA_UPDATE_FAIL_CODE = 2002;
	public static final int POST_STATUS_SHIELD = 2003;
	public static final int DATA_NOT_BELONG_USER = 2004;
	public static final int DATA_DUMPULICATE = 2005;
	//
	/** 账户错误4 */
	public static final int USER_NO_EXIST_CODE_OR_PASSWORD_NOT_CORRECT = 4201;
	public static final int USER_NO_COMPLETE = 4202;
	public static final int USER_NAME_EXIST_CODE = 4203;
	public static final int USER_NO_EXIST_CODE = 4205;
	public static final int DEVICE_BANNED_CODE = 4206;

	public static final int USER_NOT_EXIST = 4401;
	public static final int PASSWORD_NOT_CORRECT = 4402;
	public static final int BANDED_USER = 4403;
	public static final int ACCESS_TOKEN_EXPIRED = 4501;
	public static final int USER_NOT_BIND = 4502;
	public static final int STAGE_ACCOUNT_USED = 4503;
	public static final int USER_NAME_NO_CHANGE = 4504;
	public static final int USER_ACCESS_CANCEL = 4505;
	public static final int USER_SINA_ACCESS_TOKEN_EXPIRED = 4506;
	public static final int USER_SINA_SAME_MESSAGE = 4509;
	public static final int USER_TENCENT_CACHE_EXPIRE = 4508;
	public static final int USER_SINA_SHARE_FAILED = 4510;

	public static final int USERNAME_HAS_TABOO = 6002;
	public static final int NICKNAME_HAS_TABOO = 6009;
	public static final int DESCRIPTION_HAS_TABOO = 6010;
	public static final int USERNAME_EXSIST = 6003;
	public static final int EMAIL_EXSIST = 6004;
	public static final int DATA_NOT_EXIST = 6008;
	public static final int PLACE_DELETED = 6011;
	public static final int PLACE_NOT_VERIFY = 6012;
	public static final int USER_ALREADY_HAS_MEMBER_CARD = 6013;
	public static final int BRAND_MEMBER_CARD_STATUS_ERRO = 6014;
	public static final int PREFER_NOT_BELONG_USER = 6015;
	public static final int USER_BAG_FULL = 6016;
	public static final int ITEM_CAN_NOT_ADD_TO_BAG = 6017;
	public static final int BAG_NOT_BELONG_USER = 6018;
	public static final int BAG_NOT_EXSIST = 6019;
	public static final int PREFER_IS_UNIQUE = 6020;
	public static final int PREFER_PRIVILCY_NOT_ENOUGH = 6021;
	public static final int PREFER_LIFE_LIMIT = 6022;
	public static final int PREFER_ERXPIRED = 6023;
	public static final int PREFER_INSTANCE_EXPIRED = 6024;
	public static final int PLACE_COLLECTION_PLACE_LIMITED = 6025;
	public static final int CANNOT_FAVOR_SELFCREATE_COLLECTION = 6026;
	public static final int HAS_FAVOR_COLLECTION = 6027;
	public static final int POST_DELETE_BY_USER = 6028;
	public static final int PLACE_COLLECTION_DELETE_BY_USER = 6029;
	public static final int NOT_PHONE_NUM = 6030;
	public static final int SMS_CODE_IS_SENDING = 6031;
	public static final int SMS_CODE_NOT_EXIST = 6032;
	public static final int SMS_CODE_NOT_CORRECT = 6033;
	public static final int CELLPHONE_DUMPULICATE = 6034;
	public static final int USER_GOT_CONNECT_POINT = 6035;
	public static final int DEVICE_GOT_CONNECT_POINT = 6036;
	public static final int CONNECT_GOT_POINT_OUT_OF_RANGE = 6037;
	//
	//
	/** 服务器内部错误(代码出错造成的异常用这个异常) */
	public static final int SERVICE_ERROR_CODE = 8001;
	//
	// /** 其他错误 **/
	public static final int CHECKIN_IS_COOLINGDOWN = 6005;
	public static final int CHECKIN_OUT_OF_RANGE = 6006;
	
	/**
	 * 地点
	 */
	public static final int ROB_MAYOR_OUT_OF_RANGE=7001;
	public static final int IS_NOT_PLACE_MAYOR=7002;
	public static final int NOT_ENOUGH_TIME=7003;
	public static final int HAS_LEVY=7004;
	public static final int IS_MAYOR=7005;
	public static final int ROB_MAYOR_NO_AVABLE=7006;
	public static final int ROB_MAYOR_NO_ENOUGH_POINT=7007;
	public static final int PLACE_NOT_ENOUGH_POINT=7008;
	public static final int ACTIVATE_MAYOR_NO_AVABLE=7009;
	public static final int ROB_MAYOR_HAS_DOG=7010;
	/**
	 * 控制层错误
	 */
	public static final int API_NO_SAFE = 9001;
	public static final int API_REQUEST_LOGIN = 9002;
	public static final int API_REQUEST_FOBBIDEN = 9003;
	public static final int API_USER_ALREADY_BINDED = 9004;
	public static final int API_FILM_VERSION_LOWWER = 9005;
	public static final int API_DEVICE_VERSION_UNKNOW = 9006;
	public static final int API_CLIENT_TYPE_ERROR = 9007;
	public static final int API_VERSION_LOWWER = 9008;

	/**
	 * 团购
	 */
	public static final int GROUPON_NET_ERROR = 10001;
	public static final int GROUPON_ITEM_OFF = 10002;
	public static final int GROUPON_ORDER_NOT_EXIST = 10003;
	public static final int GROUPON_ORDER_ALREADY_PAYED = 10004;
	public static final int GROUPON_PREPARE_PAY_FAILED = 10005;
	public static final int GROUPON_ITEM_STATUS_ERROR = 10006;
	public static final int GROUPON_ITEM_DOWN = 11003;
	public static final int GROUPON_SHIPINFO_NOT_EXSIST = 10007;
	public static final int GROUPON_PICKUP_PLACE_ERROR = 10008;
	public static final int GROUPON_STOCK_OUT_OF_RANGE = 10009;
	/***
	 * 电影票
	 */
	public static final int PRODUCT_OFF = 11001;
	public static final int PRODUCT_END = 11002;
	public static final int PRODUCT_DOWN = 11003;
	public static final int PRODUCT_STATUS_ERROR = 11004;
	/**
	 * 订单异常
	 */
	public static final int ORDER_OUT_OF_RANGE = 12001;
	public static final int ORDER_CANNOT_PAY = 12002;
	public static final int ORDER_STOCK_NOT_ENOUGH = 12003;
	public static final int ORDER_HAS_DELELTED = 12004;
	public static final int TRADE_NOT_START = 12005;
	public static final int TRADE_ALREADY_END = 12006;
	public static final int USER_LEVEL_NOT_ENOUGH = 12007;
	public static final int ORDER_NOT_EXSIST = 12008;
	public static final int ORDER_NOT_BELONG_USER = 12009;
	public static final int USER_POINT_NOT_ENOUGH = 12010;
	public static final int PRODUCT_STOCK_NOT_ENOUGH = 12011;
	public static final int ORDER_DEVICE_OUT_OF_RANGE = 12012;
	public static final int ORDER_PHONENUM_OUT_OF_RANGE = 12013;
    public static final int ORDER_ACCOUNT_OUT_OF_RANGE = 12014;
    public static final int ORDER_QUANTITY_MUST_VARIABLE = 12015;
	
	/**
	 * 道具类
	 */
    public static final int ITEM_MESSAGE_NOT_BELONG_USER = 13001;
    public static final int ITEM_CAN_NOT_SELL = 13002;
    public static final int ITEM_OFF = 13003;
    public static final int ITEM_OUT_OF_RANGE = 13004;
    public static final int ITEM_CANNOT_PRESENT = 13005;
    public static final int ITEM_STATUS_ERROR = 13006;
    public static final int ITEM_CANNOT_USE = 13007;
    public static final int ITEM_USER_LEVEL_NOT_ENOUGH = 13008;
    public static final int ITEM_CONDITION_EFFECTION_JSON_ERROR = 13009;
    public static final int ITEM_USER_BAG_LIMIT_NOT_ENOUGH = 13010;
    public static final int ITEM_ALREADY_ADD = 13011;
    public static final int ITEM_EQUIPMENT_DUPLICATE = 13012;
    public static final int ITEM_TARGET_ID_NULL = 13013;
    public static final int ITEM_TARGET_CONNOT_BE_SELF = 13014;
    public static final int ITEM_TARGET_USER_BAG_EMPTY = 13015;
    public static final int ITEM_TARGET_USER_ALREADY_FOLLOW = 13016;
    public static final int ITEM_TARGET_USER_NOT_FOLLOW = 13017;
    public static final int ITEM_USER_BAG_DONT_NEED_UPDATE = 13018;
    public static final int ITEM_EQUIPMENT_CANNOT_TAKE_OFF = 13019;
    public static final int ITEM_NOT_EQUIP_ITEM = 13020;
    public static final int ITEM_RARE_ITEM_NULL = 13021;
    public static final int ITEM_PLACE_NOT_MAYOR = 13022;
    public static final int ITEM_TARGET_USER_BAG_FULL = 13023;
    public static final int ITEM_USER_BAG_NOT_NEED_UPDATE = 13024;
    public static final int ITEM_USER_BAG_NOT_NEED_UPDATE_16 = 13025;
    public static final int ITEM_USER_BAG_CANT_UPDATE_16=13026;
    public static final int ITEM_USER_BAG_CANT_UPDATE_24=13027;
    public static final int ITEM_DUMPULICATE_AVATAR_DECORATION = 13028;
    public static final int ITEM_DUMPULICATE_MAYOR_PROABILITY = 13029;
    public static final int ITEM_QIEGAO_CANNOT_THROW_BACK = 13030;
    public static final int ITEM_CANNOT_RECYCLE = 13031;
    public static final int ITEM_CANNOT_SEARCH_TREASURE_WITHOUT_POSITION = 13032;
    public static final int ITEM_NO_TREASURE = 13033;
    public static final int ITEM_DISCOVER_TREASURE_OUT_OF_RANGE = 13034;
    public static final int ITEM_TREASURE_HAS_BEEN_GOT = 13035;
    public static final int ITEM_PLACE_HAS_NO_TREASURE = 13036;
    public static final int ITEM_CANT_USE_TO_PLACE = 13037;
    public static final int ITEM_PLACE_ALREADY_HAS = 13038;
    public static final int ITEM_HAS_NO_TARGET = 13039;
    public static final int ITEM_CAN_NOT_PRESENT_WITH_SAME_DEVICE = 13040;
    public static final int ITEM_ANNIVERSARY_CONDITION_FAIL = 13041;
    public static final int ITEM_ANNIVERSARY_USER_DUMPULICATE = 13042;
    public static final int ITEM_ANNIVERSARY_DEVICE_DUMPULICATE = 13043;
    /**
     * 积分票
     */
    public static final int POINT_TICKET_NOT_ACTIVE = 14001;
    public static final int POINT_TICKET_USED = 14002;
    public static final int POINT_TICKET_EXPIRED = 14003;
    public static final int POINT_TICKET_NOT_EXIST = 14004;
    /**
     * 特殊功能
     */
	public static final int FUNCTION_NOT_EXIST = 15001;
	public static final int FUNCTION_SONGTU_DUPLICATE = 15002;
	public static final int FUNCTION_SONGTU_OUT_OF_RANGE = 15003;
	public static final int FUNCTION_USE_ITEM_NO_ITEM = 15004;
	
	public static final String DEVICE_ORDER_OUT_OF_RANGE = "该商品在x天内只能购买y份";
	public static final String ACCOUNT_ORDER_OUT_OF_RANGE = "您在x天内只能购买y份,或者您有相同商品的订单尚未支付";
	public static final String STORCK_NOT_ENOUGH_MESSAGE = "库存不足,该商品只能购买x件";
	public static final String ITEM_DUMPULICATE_AVATAR_DECORATION_MSG="不能装备多个ITEM_TYPE道具，你可以去个人主页卸下已装备的ITEM_TYPE道具，再装备本道具!";
	public static final String USER_LEVEL_NOT_ENOUGH_TO_USE_ITEM = "该道具需要LVi及以上等级的用户才可以使用，你的当前等级过低，无法使用！";
}
