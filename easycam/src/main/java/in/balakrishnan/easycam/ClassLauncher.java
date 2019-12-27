package in.balakrishnan.easycam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ClassLauncher {
    private final Context context;

    public ClassLauncher(Context context) {
        this.context = context;
    }

    public void launchActivity(String className) throws Exception {
        Intent intent = new Intent(context, getActivityClass(className));
        context.startActivity(intent);
    }

    public void launchActivity(String className, Bundle bundle) throws Exception {
        Intent intent = new Intent(context, getActivityClass(className));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private Class<? extends Activity> getActivityClass(String target) throws Exception {
        ClassLoader classLoader = context.getClassLoader();

        @SuppressWarnings("unchecked")
        Class<? extends Activity> activityClass = (Class<? extends Activity>) classLoader.loadClass(target);

        return activityClass;
    }
}