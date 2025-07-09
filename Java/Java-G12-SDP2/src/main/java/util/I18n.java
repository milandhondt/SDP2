package util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }
    
    public static String convertRole(String role) {
    	if(role == null) {
    		return role;
    	}
    	return switch(role.toLowerCase()) {
    	case "manager" -> I18n.get("user.role.manager");
    	case "administrator" -> I18n.get("user.role.admin");
    	case "verantwoordelijke" -> I18n.get("user.role.site-manager");
    	case "technieker" -> I18n.get("user.role.technician");
    	default -> role;
    	};
    }

	public static String convertStatus(String s) {
		if(s == null) {
			return s;
		}
		return switch(s.toLowerCase()) {
		case "voltooid" -> I18n.get("completed");
		case "in uitvoering" -> I18n.get("in-progress"); 
		case "ingepland" -> I18n.get("planned");
		case "actief" -> I18n.get("active");
		case "inactief" -> I18n.get("inactive");
		case "falend" -> I18n.get("failing");
		case "gezond" -> I18n.get("healthy");
		case "nood onderhoud" -> I18n.get("needs-maintenance");
		case "nood_onderhoud" -> I18n.get("needs-maintenance");
		case "automatisch_gestopt" -> I18n.get("automatically-stopped");
		case "automatisch gestopt" -> I18n.get("automatically-stopped");
		case "draait" -> I18n.get("running");
		case "in_onderhoud" -> I18n.get("under-maintenance");
		case "in onderhoud" -> I18n.get("under-maintenance");
		case "manueel_gestopt" -> I18n.get("manually-stopped");
		case "manueel gestopt" -> I18n.get("manually-stopped");
		case "startbaar" -> I18n.get("startable");
		default -> s;
		};
	}
}
