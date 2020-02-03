package controllers;

import models.Notification;

public class Notifications extends ControllerBase {

    public static void delete(Long id){
        Notification notification = Notification.findById(id);
        notification.delete();
        // return the number of remaining notifications
        renderText(Security.connectedUser().notifications.size());
    }
}
