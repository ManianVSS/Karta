package org.mvss.karta.server;

public class Constants extends org.mvss.karta.Constants {

    public static final String PV_ZERO = "0";
    public static final String PV_HUNDRED = "100";
    public static final String PV_ID = "id";
    public static final String PV_TRUE = "true";

    public static final String OP_EXISTS = "exists";
    public static final String OP_DOES_NOT_EXISTS = "doesNotExists";
    public static final String OP_NWEQ = "!";
    public static final String OP_NEQ = "!=";
    public static final String OP_EQ = "=";
    public static final String OP_DEQ = "==";
    public static final String OP_LIKEEQ = "~=";
    public static final String OP_LIKE = "like";
    public static final String OP_NOTLIKEEQ = "!~=";
    public static final String OP_NOTLIKE = "notLike";
    public static final String OP_LTE = "<=";
    public static final String OP_LT = "<";
    public static final String OP_GTE = ">=";
    public static final String OP_GT = ">";
    public static final String OP_IN = "in";
    public static final String OP_NOT_IN = "notIn";
    public static final int LENGTH_DESCRIPTION_MAX = 256;
    public static final int LENGTH_OBJECT_MAX = 20480;

    public static final String NULL_STRING = null;
    public static final String EMPTY_STRING = "";

    public static final String TOKEN = "TOKEN";
    // public static final String REFRESH_COUNT = "refreshCount";
    public static final String JWT_ROLES = "roles";

    public static final String HAS_ROLE = "hasRole('";
    public static final String HAS_AUTHORITY = "hasAuthority('";
    public static final String CLOSING_BRACKET = "') ";
    public static final String SLASH_ID = "/{id}";
    public static final String ID_VERSION = SLASH_ID + "/currentversion";
    // public static final String API_AUTHENTICATION = "/authenticate";

    public static final String SWAGGER_UI_CONTROLLER = "/swagger-ui/";

    public static final String DEFAULT_VIEW = "index";


    // public static final String ANT_MATCHER_FAVICON_ICO = "/favicon.ico";

    public static final String ANT_MATCHER_TEMPLATES = "/templates/**";
    public static final String ANT_MATCHER_API = "/api/**";
    public static final String ANT_MATCHER_WEBUI = "/WebUI/**";
    public static final String ANT_MATCHER_CSS = "/css/**";
    public static final String ANT_MATCHER_IMG = "/img/**";
    public static final String ANT_MATCHER_JQ_TEMPLATES = "/jqtemplates/**";
    public static final String ANT_MATCHER_JS = "/js/**";
    public static final String ANT_MATCHER_FAVICON = "/favicon.png";
    public static final String ANT_MATCHER_ADMIN = "/admin/**";
    public static final String ANT_MATCHER_GENERIC = "/**";

    public static final String ERROR_OCCURRED = "An Error Occurred: ";

    public static final String HIDDEN_PASSWORD = "******";
    public static final String SUCCESSFUL = "successful";

    public static final String APPLICATION_ROUTE_MAP_YAML = "ApplicationRouteMap.yaml";


    public static final String API_ID = "/{id}";
    public static final String API_SEARCH = "Search";
    public static final String PATH_API = "/api";
    public static final String PATH_TEST_CATEGORIES = "/testCategories";
    public static final String PATH_TESTS = "/tests";
}
