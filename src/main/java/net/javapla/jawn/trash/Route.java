package net.javapla.jawn.trash;

import java.util.List;

import net.javapla.jawn.AppController;
import net.javapla.jawn.HttpMethod;
import net.javapla.jawn.util.StringUtil;

/**
 *  Instance of this class will contain routing information.
 *
 * @author Igor Polevoy: 1/8/13 4:21 PM
 */
@Deprecated
public class Route {

//    private static final String DEFAULT_ACTION = "index";
//    
//    private AppController controller;
//    private String language, actionName, id, wildCardName, wildCardValue;
//    private List<IgnoreSpec> ignoreSpecs;
//    private HttpMethod httpMethod = HttpMethod.GET;
//    private String format;
//
//    /*public Route(String language, AppController controller, String actionName) {
//        this.language = language;
//        this.controller = controller;
//        this.actionName = actionName;
//    }*/
//
//    /*public Route(String language, AppController controller, String actionName, String id) {
//        this(language, controller, actionName);
//        this.id = id;
//    }*/
//    
//    public Route(String language, AppController controller, String actionName, HttpMethod httpMethod) {
//        this.language = language;
//        this.controller = controller;
//        this.actionName = actionName;
//        this.httpMethod = httpMethod;
//    }
//
//    public Route(String language, AppController controller, String actionName, String id, HttpMethod httpMethod) {
//        this(language, controller, actionName, httpMethod);
//        this.id = id;
//    }
//    
//    public Route(RouteBuilder builder) {
//        language = builder.getLanguage();
//        controller = builder.getController();
//        actionName = builder.getActionName();
//        httpMethod = builder.getHttpMethod();
//        id = builder.getId();
//        this.wildCardName = builder.getWildcardName();
//        this.wildCardValue = builder.getWildCardValue();
//    }
//
//    public Route(String language, AppController controller) {
//        this.language = language;
//        this.controller = controller;
//        this.actionName = DEFAULT_ACTION;
//    }
//    
//    public void setFormat(String format) {
//        this.format = format;
//    }
//    public String getFormat() {
//        return format;
//    }
//
//    public boolean isWildCard(){
//        return wildCardName != null;
//    }
//
//    public String getWildCardName() {
//        return wildCardName;
//    }
//
//    public String getWildCardValue() {
//        return wildCardValue;
//    }
//
//    public AppController getController() {
//        return controller;
//    }
//
//    public String getAction() {
//        if (DEFAULT_ACTION.equals(actionName))
//            return actionName;
//        return httpMethod.name().toLowerCase() + StringUtil.camelize(actionName.replace('-', '_'), true);
//    }
//    
//    public String getActionName() {
////        if (DEFAULT_ACTION.equals(actionName))
////            return actionName;
////        return StringUtil.underscore(actionName);
//        return actionName;
//    }
//
//    public String getId() {
//        return id;
//    }
//    
//    public String getLanguage() {
//        return language;
//    }
//    public boolean hasLanguage() {
//        return !StringUtil.blank(language);
//    }
//
//    protected String getControllerPath(){
//        return Router.getControllerPath(controller.getClass());
//    }
//
//    /**
//     * This is used in specs
//     *
//     * @return controller class name
//     */
//    protected String getControllerClassName() {
//        return controller.getClass().getName();//controller != null ? controller.getClass().getName() : type.getName();
//    }
//
//    protected void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
//        this.ignoreSpecs = ignoreSpecs;
//    }
//
//    protected boolean ignores(String path) {
//        if(ignoreSpecs == null){
//            return false;
//        }else{
//            for(IgnoreSpec ignoreSpec: ignoreSpecs){
//                if(ignoreSpec.ignores(path))
//                    return true;
//            }
//        }
//        return false;
//    }
}
