package com.ops.zen.support;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/3 15:46
 * @Description
 */
public interface SupportConstant {

    String BASE_PACKAGES = "com.ops.zen";

    String BASE_PACKAGE_CONTROLLERS = BASE_PACKAGES + ".controller";

    /**
     * &&（and）表达式
     */
    String POINT_CUT_CONTROLLER_METHOD_AROUND = "execution(* " + BASE_PACKAGES + ".*.controller.*.*(..))";

}
