package tud.seemuh.nfcgate.gui;

import android.view.Menu;
import android.view.MenuItem;

public class Util {
    public static void setMenuItemEnabled(Menu menu, int id, boolean enabled) {
        MenuItem item = menu.findItem(id);
        if (item != null) {
            item.setEnabled(enabled);
            item.getIcon().mutate().setAlpha(enabled ? 255 : 130);
        }
    }
}
