package net.javapla.jawn.security.spi;

import net.javapla.jawn.core.ApplicationController;

public class LoginHandlingController extends ApplicationController {
        /*public void getLogin() {
            //render().layout("login");
            //render().layout("/admin/login/index");
        }*/
        
        // TODO perhaps create a LoginController or SecurityController with default actions already implemented
        
/* **********
 * Security
 * ******** */
    public void postLogin() {
//            System.err.println("postindex");
//            redirect(AdminController.class);
        // Gets handled elsewhere
    }
    public void getLogin() {
        
    }
    public void getLogout() {
        redirect("/"/*IndexController.class*/);
    }
    public void getNotauth() {
        //render().layout("login");
    }
}
